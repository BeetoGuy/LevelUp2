package levelup2.event;

import levelup2.api.ICharacterClass;
import levelup2.api.IPlayerSkill;
import levelup2.capability.PlayerCapability;
import levelup2.config.LevelUpConfig;
import levelup2.network.SkillPacketHandler;
import levelup2.player.IPlayerClass;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
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

    private static final String BOOK_TAG = "levelup:bookspawn";

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent evt) {
        if (evt.player instanceof EntityPlayerMP) {
            spawnBook(evt.player);
            SkillRegistry.loadPlayer(evt.player);
            SkillPacketHandler.configChannel.sendTo(SkillPacketHandler.getConfigPacket(LevelUpConfig.getServerProperties()), (EntityPlayerMP)evt.player);
            for (ResourceLocation loc : SkillRegistry.getSkills().keySet()) {
                IPlayerSkill skill = SkillRegistry.getSkillFromName(loc);
                SkillPacketHandler.propertyChannel.sendTo(SkillPacketHandler.getPropertyPackets(skill), (EntityPlayerMP)evt.player);
            }
            for (ResourceLocation loc : SkillRegistry.getClasses().keySet()) {
                ICharacterClass cl = SkillRegistry.getClassFromName(loc);
                SkillPacketHandler.classChannel.sendTo(SkillPacketHandler.getClassPackets(cl), (EntityPlayerMP)evt.player);
            }
            SkillPacketHandler.refreshChannel.sendTo(SkillPacketHandler.getRefreshPacket(), (EntityPlayerMP)evt.player);
        }
    }

    private void spawnBook(EntityPlayer player) {
        if (LevelUpConfig.giveSkillBook) {
            NBTTagCompound playerData = player.getEntityData();
            NBTTagCompound data = getTag(playerData, EntityPlayer.PERSISTED_NBT_TAG);
            if (!data.getBoolean(BOOK_TAG)) {
                ItemStack book = new ItemStack(SkillRegistry.skillBook);
                if (!player.addItemStackToInventory(book)) {
                    player.dropItem(book, true);
                }
                data.setBoolean(BOOK_TAG, true);
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            }
        }
    }

    private NBTTagCompound getTag(NBTTagCompound base, String tag) {
        if (base == null)
            return new NBTTagCompound();
        return base.getCompoundTag(tag);
    }

    public static double getDivisor(ResourceLocation skill) {
        IPlayerSkill sk = SkillRegistry.getSkillFromName(skill);
        if (sk != null) {
            return SkillRegistry.getProperty(sk).getDivisor();
        }
        return 1;
    }
}
