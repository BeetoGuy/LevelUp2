package levelup2.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public interface IProcessor {
    void extraProcessing(EntityPlayer player);

    void setUUID(UUID placer);

    EntityPlayer getPlayerFromUUID();

    NBTTagCompound writeToNBT(NBTTagCompound tag);

    void readFromNBT(NBTTagCompound tag);
}
