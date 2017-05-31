package levelup2.player;

import levelup2.api.IPlayerSkill;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public interface IPlayerClass {
    NBTTagCompound saveNBTData(NBTTagCompound tag);

    void loadNBTData(NBTTagCompound tag);

    IPlayerSkill getSkillFromName(String skill);

    void setSkillLevel(String name, int level);

    void setPlayerData(String[] skills, int[] data);

    void addToSkill(String name, int value);

    boolean hasClass();

    byte getSpecialization();

    void setSpecialization(byte spec);

    List<IPlayerSkill> getSkills();
}
