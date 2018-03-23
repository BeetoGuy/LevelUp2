package levelup2.skills.crafting;

import levelup2.api.IProcessor;
import levelup2.capability.CapabilityFurnace;
import levelup2.capability.PlayerCapability;
import levelup2.skills.BaseSkill;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

public class FurnaceEfficiencyBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:furnacespeed";
    }

    @Override
    public byte getSkillType() {
        return 1;
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Blocks.FURNACE);
    }

    @SubscribeEvent
    public void registerFurnaceCapability(AttachCapabilitiesEvent<TileEntity> evt) {
        if (evt.getObject() instanceof TileEntityFurnace) {
            final TileEntityFurnace furnace = (TileEntityFurnace)evt.getObject();
            evt.addCapability(new ResourceLocation("levelup", "furnacemods"), new ICapabilitySerializable<NBTTagCompound>() {
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
    }

    @SubscribeEvent
    public void onTileInteracted(PlayerInteractEvent.RightClickBlock evt) {
        if (evt.getEntityPlayer() != null && !evt.getWorld().isRemote) {
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

    /**
     * Only need to do this here for all of the IProcessor machines, check tile entities for capability and tick accordingly.
     * (Is there a better way to do this?)
     */
    @SubscribeEvent
    public void doFurnaceTicks(TickEvent.WorldTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START) {
            if (!evt.world.isRemote) {
                try {
                    List<TileEntity> tiles = evt.world.loadedTileEntityList.stream().filter(t -> t.hasCapability(PlayerCapability.MACHINE_PROCESSING, EnumFacing.UP)).collect(Collectors.toList());
                    if (tiles != null && !tiles.isEmpty()) {
                        for (TileEntity tile : tiles) {
                            IProcessor cap = tile.getCapability(PlayerCapability.MACHINE_PROCESSING, EnumFacing.UP);
                            if (cap != null && cap.getPlayerFromUUID() != null) {
                                cap.extraProcessing(cap.getPlayerFromUUID());
                            }
                        }
                    }
                }
                catch (ConcurrentModificationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
