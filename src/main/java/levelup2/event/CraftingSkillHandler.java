package levelup2.event;

import levelup2.api.IProcessor;
import levelup2.capability.CapabilityBrewingStand;
import levelup2.capability.CapabilityFurnace;
import levelup2.capability.PlayerCapability;
import levelup2.config.LevelUpConfig;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class CraftingSkillHandler {
    public static final CraftingSkillHandler INSTANCE = new CraftingSkillHandler();

    private CraftingSkillHandler() {}

    private Random rand = new Random();
    private static final ResourceLocation FURNACEMODS = new ResourceLocation("levelup", "furnacemods");
    private static final ResourceLocation FISHBONUS = new ResourceLocation("levelup", "fishbonus");
    private static final ResourceLocation CROPGROWTH = new ResourceLocation("levelup", "cropgrowth");
    private static final ResourceLocation HARVESTBONUS = new ResourceLocation("levelup", "harvestbonus");
    private static final ResourceLocation CRAFTBONUS = new ResourceLocation("levelup", "craft_bonus");

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent evt) {
        if (SkillRegistry.getSkillLevel(evt.player, CRAFTBONUS) > 0 && !isBlacklistedOutput(evt.crafting)) {
            if (isNotOneItemCrafting(evt.craftMatrix)) {
                int craftingChances = getCraftingItems(evt.craftMatrix);
                if (craftingChances > 0) {
                    int experienceGain = 0;
                    for (int i = 0; i < craftingChances; i++) {
                        if (rand.nextFloat() < 0.55F) {
                            experienceGain++;
                        }
                    }
                    if (experienceGain > 0)
                        SkillRegistry.addExperience(evt.player, experienceGain * (int)CapabilityEventHandler.getDivisor(CRAFTBONUS));
                }
            }
        }
    }

    private boolean isBlacklistedOutput(ItemStack stack) {
        if (!LevelUpConfig.blacklistOutputs.isEmpty()) {
            for (Ingredient ing : LevelUpConfig.blacklistOutputs) {
                if (ing.apply(stack))
                    return true;
            }
        }
        return false;
    }

    private boolean isNotOneItemCrafting(IInventory inv) {
        boolean notSame = false;
        ItemStack firstStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty() && !inv.getStackInSlot(i).getItem().hasContainerItem(inv.getStackInSlot(i))) {
                if (firstStack.isEmpty()) {
                    firstStack = inv.getStackInSlot(i).copy();
                }
                else if (!firstStack.isItemEqual(inv.getStackInSlot(i))) {
                    notSame = true;
                }
            }
        }
        return notSame;
    }

    private int getCraftingItems(IInventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty() && !inv.getStackInSlot(i).getItem().hasContainerItem(inv.getStackInSlot(i))) {
                ItemStack stack = inv.getStackInSlot(i).copy();
                stack.setCount(1);
                if (items.isEmpty())
                    items.add(stack);
                else if (!SkillRegistry.listContains(stack, items))
                    items.add(stack);
            }
        }
        return items.size();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBroken(BlockEvent.BreakEvent evt) {
        if (!evt.getWorld().isRemote && evt.getPlayer() != null && SkillRegistry.getPlayer(evt.getPlayer()).isActive()) {
            if (evt.getState().getBlock() instanceof BlockCrops || evt.getState().getBlock() instanceof BlockStem) {
                if (!((IGrowable)evt.getState().getBlock()).canGrow(evt.getWorld(), evt.getPos(), evt.getState(), false)) {
                    doCropDrops(evt);
                }
            }
            else if (evt.getState().getBlock() instanceof BlockMelon) {
                doCropDrops(evt);
            }
        }
    }

    private void doCropDrops(BlockEvent.BreakEvent evt) {
        Random rand = evt.getPlayer().getRNG();
        int skill = SkillRegistry.getSkillLevel(evt.getPlayer(), HARVESTBONUS);
        if (skill > 0) {
            if (rand.nextInt((int)CapabilityEventHandler.getDivisor(HARVESTBONUS)) < skill) {
                Item item = evt.getState().getBlock().getItemDropped(evt.getState(), rand, 0);
                if (item == Items.AIR || item == null) {
                    if (evt.getState().getBlock() == Blocks.PUMPKIN_STEM)
                        item = Items.PUMPKIN_SEEDS;
                    else if (evt.getState().getBlock() == Blocks.MELON_STEM)
                        item = Items.MELON_SEEDS;
                }
                if (item != Items.AIR && item != null) {
                    evt.getWorld().spawnEntity(new EntityItem(evt.getWorld(), evt.getPos().getX(), evt.getPos().getY(), evt.getPos().getZ(), new ItemStack(item, Math.max(1, evt.getState().getBlock().quantityDropped(evt.getState(), 0, rand)), evt.getState().getBlock().damageDropped(evt.getState()))));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START && SkillRegistry.getPlayer(evt.player).isActive()) {
            EntityPlayer player = evt.player;
            if (player != null) {
                int skillLevel = SkillRegistry.getSkillLevel(player, CROPGROWTH);
                if (!player.world.isRemote && skillLevel > 0 && player.getRNG().nextFloat() <= skillLevel / 500F) {
                    growCropsAround(player.world, skillLevel, player);
                }
            }
        }
    }

    private void growCropsAround(World world, int range, EntityPlayer player) {
        int posX = (int)player.posX;
        int posY = (int)player.posY;
        int posZ = (int)player.posZ;
        int dist = range / 2 + 2;
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(posX - dist, posY - dist, posZ - dist), new BlockPos(posX + dist + 1, posY + dist + 1, posZ + dist + 1))) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof IPlantable && !SkillRegistry.getCropBlacklist().contains(block)) {
                world.scheduleUpdate(pos, block, block.tickRate(world));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onFishInteract(PlayerInteractEvent.RightClickItem evt) {
        if (evt.getResult() != Event.Result.DENY && SkillRegistry.getPlayer(evt.getEntityPlayer()).isActive()) {
            EntityFishHook hook = evt.getEntityPlayer().fishEntity;
            if (hook != null && hook.caughtEntity == null && hook.ticksCatchable > 0) {
                ItemStack loot = getFishingLoot(evt.getWorld(), evt.getEntityPlayer());
                if (!loot.isEmpty()) {
                    ItemStack stack = evt.getEntityPlayer().inventory.getCurrentItem();
                    int i = stack.getCount();
                    int j = stack.getItemDamage();
                    stack.damageItem(1, evt.getEntityPlayer());
                    evt.getEntityPlayer().swingArm(evt.getHand());
                    evt.getEntityPlayer().inventory.setInventorySlotContents(evt.getEntityPlayer().inventory.currentItem, stack);
                    if (evt.getEntityPlayer().capabilities.isCreativeMode) {
                        stack.grow(i);
                        if (stack.isItemStackDamageable()) {
                            stack.setItemDamage(j);
                        }
                    }
                    if (stack.getCount() <= 0) {
                        evt.getEntityPlayer().inventory.setInventorySlotContents(evt.getEntityPlayer().inventory.currentItem, ItemStack.EMPTY);
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(evt.getEntityPlayer(), stack, evt.getHand()));
                    }
                    if (!evt.getEntityPlayer().isHandActive() && evt.getEntityPlayer() instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) evt.getEntityPlayer()).sendContainerToPlayer(evt.getEntityPlayer().inventoryContainer);
                    }
                    evt.setResult(Event.Result.DENY);
                    if (!hook.world.isRemote) {
                        EntityItem item = new EntityItem(hook.world, hook.posX, hook.posY, hook.posZ, loot);
                        double d5 = hook.getAngler().posX - hook.posX;
                        double d6 = hook.getAngler().posY - hook.posY;
                        double d7 = hook.getAngler().posZ - hook.posZ;
                        double d8 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
                        double d9 = 0.1D;
                        item.motionX = d5 * d9;
                        item.motionY = d6 * d9 + MathHelper.sqrt(d8) * 0.08D;
                        item.motionZ = d7 * d9;
                        hook.world.spawnEntity(item);
                        hook.getAngler().world.spawnEntity(new EntityXPOrb(hook.getAngler().world, hook.getAngler().posX, hook.getAngler().posY + 0.5D, hook.getAngler().posZ + 0.5D, evt.getEntityPlayer().getRNG().nextInt(6) + 1));
                    }
                }
            }
        }
    }

    private ItemStack getFishingLoot(World world, EntityPlayer player) {
        if (!world.isRemote) {
            double divisor = 1D / CapabilityEventHandler.getDivisor(FISHBONUS);
            if (player.getRNG().nextDouble() <= SkillRegistry.getSkillLevel(player, FISHBONUS) * divisor) {
                LootContext.Builder build = new LootContext.Builder((WorldServer)world);
                build.withLuck((float) EnchantmentHelper.getMaxEnchantmentLevel(Enchantment.getEnchantmentByLocation("luck_of_the_sea"), player) + player.getLuck());
                return Library.getLootManager().getLootTableFromLocation(new ResourceLocation("levelup", "fishing/fishing_loot")).generateLootForPools(player.getRNG(), build.build()).get(0).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public void registerTileCapability(AttachCapabilitiesEvent<TileEntity> evt) {
        if (evt.getObject() instanceof TileEntityFurnace) {
            final TileEntityFurnace furnace = (TileEntityFurnace)evt.getObject();
            evt.addCapability(FURNACEMODS, new ICapabilitySerializable<NBTTagCompound>() {
                IProcessor instance = new CapabilityFurnace(furnace);

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING ? PlayerCapability.MACHINE_PROCESSING.<T>cast(instance) : null;
                }

                @Override
                public NBTTagCompound serializeNBT() {
                    return ((NBTTagCompound)PlayerCapability.MACHINE_PROCESSING.getStorage().writeNBT(PlayerCapability.MACHINE_PROCESSING, instance, null));
                }

                @Override
                public void deserializeNBT(NBTTagCompound tag) {
                    PlayerCapability.MACHINE_PROCESSING.getStorage().readNBT(PlayerCapability.MACHINE_PROCESSING, instance, null, tag);
                }
            });
        }
        else if (evt.getObject() instanceof TileEntityBrewingStand) {
            final TileEntityBrewingStand stand = (TileEntityBrewingStand)evt.getObject();
            evt.addCapability(FURNACEMODS, new ICapabilitySerializable<NBTTagCompound>() {
                IProcessor instance = new CapabilityBrewingStand(stand);

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == PlayerCapability.MACHINE_PROCESSING ? PlayerCapability.MACHINE_PROCESSING.<T>cast(instance) : null;
                }

                @Override
                public NBTTagCompound serializeNBT() {
                    return ((NBTTagCompound)PlayerCapability.MACHINE_PROCESSING.getStorage().writeNBT(PlayerCapability.MACHINE_PROCESSING, instance, null));
                }

                @Override
                public void deserializeNBT(NBTTagCompound tag) {
                    PlayerCapability.MACHINE_PROCESSING.getStorage().readNBT(PlayerCapability.MACHINE_PROCESSING, instance, null, tag);
                }
            });
        }
    }

    @SubscribeEvent
    public void onTileInteracted(PlayerInteractEvent.RightClickBlock evt) {
        if (!evt.getWorld().isRemote && evt.getEntityPlayer() != null) {
            EntityPlayer player = evt.getEntityPlayer();
            if (player instanceof FakePlayer || !player.isSneaking() || !evt.getItemStack().isEmpty())
                return;
            TileEntity tile = evt.getWorld().getTileEntity(evt.getPos());
            if (tile != null) {
                if (tile.hasCapability(PlayerCapability.MACHINE_PROCESSING, EnumFacing.UP)) {
                    IProcessor cap = tile.getCapability(PlayerCapability.MACHINE_PROCESSING, EnumFacing.UP);
                    if (cap != null) {
                        String name = UsernameCache.getLastKnownUsername(player.getGameProfile().getId());
                        if (cap.getPlayerFromUUID() == null) {
                            player.sendStatusMessage(new TextComponentTranslation("levelup.interact.register", name), true);
                            cap.setUUID(player.getGameProfile().getId());
                        } else if (cap.getPlayerFromUUID().getGameProfile().getId() == player.getGameProfile().getId()) {
                            player.sendStatusMessage(new TextComponentTranslation("levelup.interact.unregister", name), true);
                            cap.setUUID(null);
                        } else {
                            name = UsernameCache.getLastKnownUsername(cap.getPlayerFromUUID().getGameProfile().getId());
                            player.sendStatusMessage(new TextComponentTranslation("levelup.interact.notowned", name), true);
                        }
                    }
                }
            }
        }
    }
}
