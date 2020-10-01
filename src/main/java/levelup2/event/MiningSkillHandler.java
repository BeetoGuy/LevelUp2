package levelup2.event;

import levelup2.config.LevelUpConfig;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import levelup2.util.PlankCache;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Random;

public class MiningSkillHandler {
    public static final MiningSkillHandler INSTANCE = new MiningSkillHandler();

    private MiningSkillHandler() {}
    private static final ResourceLocation TREASUREHUNTING = new ResourceLocation("levelup", "treasurehunting");
    private static final ResourceLocation FLINTLOOT = new ResourceLocation("levelup", "flintloot");
    private static final ResourceLocation PROSPECTING = new ResourceLocation("levelup", "prospecting");
    private static final ResourceLocation LUMBERING = new ResourceLocation("levelup", "lumbering");
    private static final ResourceLocation STONECUTTING = new ResourceLocation("levelup", "stonecutting");
    private static final ResourceLocation WOODCUTTING = new ResourceLocation("levelup", "woodcutting");
    private static final ResourceLocation MINING_BONUS = new ResourceLocation("levelup", "mining_bonus");

    @SubscribeEvent
    public void onBlockBreaking(PlayerEvent.BreakSpeed evt) {
        if (evt.getEntityPlayer() != null && !evt.getEntityPlayer().getEntityWorld().isRemote && SkillRegistry.getPlayer(evt.getEntityPlayer()).isActive()) {
            int skill = SkillRegistry.getSkillLevel(evt.getEntityPlayer(), STONECUTTING);
            float speed = evt.getNewSpeed();
            if (skill > 0 && evt.getState().getMaterial() == Material.ROCK) {
                float divisor = (float)(CapabilityEventHandler.getDivisor(STONECUTTING) / 20F);
                float speedMod = 1.0F + (skill * divisor);
                evt.setNewSpeed(speed * speedMod);
                return;
            }
            skill = SkillRegistry.getSkillLevel(evt.getEntityPlayer(), WOODCUTTING);
            if (skill > 0 && evt.getState().getMaterial() == Material.WOOD) {
                float divisor = (float)(CapabilityEventHandler.getDivisor(WOODCUTTING) / 20F);
                float speedMod = 1.0F + (skill * divisor);
                evt.setNewSpeed(speed * speedMod);
            }
        }
    }

    @SubscribeEvent
    public void onBlockHarvest(BlockEvent.HarvestDropsEvent evt) {
        if (evt.getHarvester() != null && !evt.getWorld().isRemote && SkillRegistry.getPlayer(evt.getHarvester()).isActive()) {
            IBlockState state = evt.getState();
            Random rand = evt.getHarvester().getRNG();
            if (!evt.isSilkTouching()) {
                int skill = SkillRegistry.getSkillLevel(evt.getHarvester(), TREASUREHUNTING);
                if (skill > 0) {
                    if (!evt.getDrops().isEmpty() && SkillRegistry.listContains(evt.getDrops().get(0), OreDictionary.getOres("dirt"))) {
                        ItemStack drop = evt.getDrops().get(0).copy();
                        if (rand.nextFloat() <= skill / (float) CapabilityEventHandler.getDivisor(TREASUREHUNTING)) {
                            ItemStack loot = getDigLoot(evt.getHarvester());
                            if (!loot.isEmpty()) {
                                Library.removeFromList(evt.getDrops(), drop);
                                evt.getDrops().add(loot.copy());
                                return;
                            }
                        }
                    }
                }
                skill = SkillRegistry.getSkillLevel(evt.getHarvester(), FLINTLOOT);
                if (skill > 0) {
                    if (state.getBlock() instanceof BlockGravel) {
                        if (rand.nextInt((int) CapabilityEventHandler.getDivisor(FLINTLOOT)) < skill) {
                            Library.removeFromList(evt.getDrops(), new ItemStack(state.getBlock()));
                            evt.getDrops().add(new ItemStack(Items.FLINT));
                            return;
                        }
                    }
                }
                skill = SkillRegistry.getSkillLevel(evt.getHarvester(), PROSPECTING);
                if (skill > 0 && rand.nextDouble() <= skill / CapabilityEventHandler.getDivisor(PROSPECTING)) {
                    getOreChunk(evt);
                } else if (LevelUpConfig.alwaysDropChunks) getSingleChunk(evt);
                skill = SkillRegistry.getSkillLevel(evt.getHarvester(), MINING_BONUS);
                if (skill > 0) {
                    ItemStack test = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                    int experience = Library.getExperienceYield(test);
                    if (experience > 0) {
                        evt.getHarvester().addExperience(experience * (int)CapabilityEventHandler.getDivisor(MINING_BONUS));
                    }
                }
            }
            if (SkillRegistry.getSkillLevel(evt.getHarvester(), LUMBERING) > 0 && PlankCache.contains(state.getBlock(), state.getBlock().damageDropped(state))) {
                int skill = SkillRegistry.getSkillLevel(evt.getHarvester(), LUMBERING);
                double divisor = CapabilityEventHandler.getDivisor(LUMBERING);
                if (rand.nextDouble() <= skill / divisor) {
                    ItemStack planks = PlankCache.getProduct(state.getBlock(), state.getBlock().damageDropped(state));
                    if (!planks.isEmpty())
                        evt.getDrops().add(planks.copy());
                }
                if (rand.nextDouble() <= skill / divisor) {
                    evt.getDrops().add(new ItemStack(Items.STICK, 2));
                }
            }
        }
    }

    private ItemStack getDigLoot(EntityPlayer player) {
        if (!player.getEntityWorld().isRemote) {
            LootContext.Builder build = new LootContext.Builder(((WorldServer)player.getEntityWorld())).withPlayer(player);
            build.withLuck((float) EnchantmentHelper.getMaxEnchantmentLevel(Enchantment.getEnchantmentByLocation("fortune"), player) + player.getLuck());
            ResourceLocation location = getTableFromWeightedPool(player.getRNG());
            if (location != null)
                return Library.getLootManager().getLootTableFromLocation(location).generateLootForPools(player.getRNG(), build.build()).get(0);
        }
        return ItemStack.EMPTY;
    }

    private void getSingleChunk(BlockEvent.HarvestDropsEvent evt) {
        if (!evt.getDrops().isEmpty()) {
            ItemStack test = evt.getDrops().get(0);
            ItemStack drop = Library.getOreChunk(test, evt.getHarvester().getRNG(), 0);
            if (!drop.isEmpty()) {
                drop.setCount(1);
                Library.removeFromList(evt.getDrops(), test);
                evt.getDrops().add(drop.copy());
            }
        }
    }

    private void getOreChunk(BlockEvent.HarvestDropsEvent evt) {
        if (!evt.getDrops().isEmpty()) {
            ItemStack test = evt.getDrops().get(0);
            ItemStack drop = Library.getOreChunk(test, evt.getHarvester().getRNG(), evt.getFortuneLevel());
            if (!drop.isEmpty()) {
                Library.removeFromList(evt.getDrops(), test);
                evt.getDrops().add(drop.copy());
            } else if (!Library.getOreChunk(new ItemStack(evt.getState().getBlock(), 1, evt.getState().getBlock().damageDropped(evt.getState())), evt.getHarvester().getRNG(), evt.getFortuneLevel()).isEmpty()) {
                evt.getDrops().addAll(evt.getState().getBlock().getDrops(evt.getWorld(), evt.getPos(), evt.getState(), evt.getFortuneLevel()));
            }
        }
    }

    private static final ResourceLocation RAREDIG = new ResourceLocation("levelup", "digging/rare_dig");
    private static final ResourceLocation UNCOMMONDIG = new ResourceLocation("levelup", "digging/uncommon_dig");
    private static final ResourceLocation COMMONDIG = new ResourceLocation("levelup", "digging/common_dig");

    private ResourceLocation getTableFromWeightedPool(Random rand) {
        if (LevelUpConfig.combinedChance == 0)
            return null;
        int RNG = rand.nextInt(LevelUpConfig.combinedChance);
        if (LevelUpConfig.rareChance > 0 && RNG <= LevelUpConfig.rareChance) {
            return RAREDIG;
        }
        else if (LevelUpConfig.uncommonChance > 0 && RNG <= LevelUpConfig.rareChance + LevelUpConfig.uncommonChance) {
            return UNCOMMONDIG;
        }
        else
            return COMMONDIG;
    }
}
