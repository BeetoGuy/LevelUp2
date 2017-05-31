package levelup2.event;

import levelup2.capability.PlayerCapability;
import levelup2.config.LevelUpConfig;
import levelup2.network.SkillPacketHandler;
import levelup2.player.IPlayerClass;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityEventHandler {
    @SubscribeEvent
    public void onPlayerEntersWorld(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof EntityPlayer) {
            evt.addCapability(Library.SKILL_LOCATION, new ICapabilitySerializable<NBTTagCompound>() {
                IPlayerClass instance = PlayerCapability.PLAYER_CLASS.getDefaultInstance();

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == PlayerCapability.PLAYER_CLASS;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == PlayerCapability.PLAYER_CLASS ? PlayerCapability.PLAYER_CLASS.<T>cast(instance) : null;
                }

                @Override
                public NBTTagCompound serializeNBT() {
                    return ((NBTTagCompound)PlayerCapability.PLAYER_CLASS.getStorage().writeNBT(PlayerCapability.PLAYER_CLASS, instance, null));
                }

                @Override
                public void deserializeNBT(NBTTagCompound tag) {
                    PlayerCapability.PLAYER_CLASS.getStorage().readNBT(PlayerCapability.PLAYER_CLASS, instance, null, tag);
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone evt) {
        if (!evt.isWasDeath() || !LevelUpConfig.resetClassOnDeath) {
            NBTTagCompound data = new NBTTagCompound();
            SkillRegistry.getPlayer(evt.getOriginal()).saveNBTData(data);
            SkillRegistry.getPlayer(evt.getEntityPlayer()).loadNBTData(data);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        SkillRegistry.loadPlayer(evt.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerChangedDimensionEvent evt) {
        SkillRegistry.loadPlayer(evt.player);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent evt) {
        if (evt.player instanceof EntityPlayerMP) {
            SkillRegistry.loadPlayer(evt.player);
            SkillPacketHandler.configChannel.sendTo(SkillPacketHandler.getConfigPacket(LevelUpConfig.getServerProperties()), (EntityPlayerMP)evt.player);
        }
    }
}
