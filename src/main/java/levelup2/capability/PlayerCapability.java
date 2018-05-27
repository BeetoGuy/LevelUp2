package levelup2.capability;

import levelup2.api.IProcessor;
import levelup2.player.IPlayerClass;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import java.util.UUID;

public class PlayerCapability {
    @CapabilityInject(IPlayerClass.class)
    public static Capability<IPlayerClass> PLAYER_CLASS = null;

    @CapabilityInject(IProcessor.class)
    public static Capability<IProcessor> MACHINE_PROCESSING = null;

    public static class CapabilityPlayerClass<T extends IPlayerClass> implements Capability.IStorage<IPlayerClass> {
        @Override
        public NBTBase writeNBT(Capability<IPlayerClass> capability, IPlayerClass player, EnumFacing side) {
            return player.saveNBTData(new NBTTagCompound());
        }

        @Override
        public void readNBT(Capability<IPlayerClass> capability, IPlayerClass player, EnumFacing side, NBTBase nbt) {
            player.loadNBTData((NBTTagCompound)nbt);
        }
    }

    public static class CapabilityProcessorClass<T extends IProcessor> implements Capability.IStorage<IProcessor> {
        @Override
        public NBTBase writeNBT(Capability<IProcessor> capability, IProcessor process, EnumFacing side) {
            return process.writeToNBT(new NBTTagCompound());
        }

        @Override
        public void readNBT(Capability<IProcessor> capability, IProcessor process, EnumFacing side, NBTBase nbt) {
            process.readFromNBT((NBTTagCompound)nbt);
        }
    }

    public static class CapabilityProcessorDefault implements IProcessor {
        private EntityPlayer player;
        protected UUID playerUUID;
        protected TileEntity tile;

        public CapabilityProcessorDefault(TileEntity entity) {
            tile = entity;
        }

        @Override
        public void setUUID(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public void readFromNBT(NBTTagCompound tag) {
            if (tag.hasKey("player_uuid")) {
                playerUUID = UUID.fromString(tag.getString("player_uuid"));
                player = Library.getPlayerFromUUID(playerUUID);
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound tag) {
            if (playerUUID != null)
                tag.setString("player_uuid", playerUUID.toString());
            return tag;
        }

        @Override
        public EntityPlayer getPlayerFromUUID() {
            if (playerUUID == null) {
                if (player != null) player = null;
                return null;
            }
            if (player == null) {
                player = Library.getPlayerFromUUID(playerUUID);
            }
            return player;
        }

        @Override
        public void extraProcessing(EntityPlayer player) {}
    }
}
