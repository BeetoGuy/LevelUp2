package levelup2.player;

import levelup2.api.IPlayerSkill;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

public interface IPlayerClass {
    NBTTagCompound saveNBTData(NBTTagCompound tag);

    void loadNBTData(NBTTagCompound tag);

    IPlayerSkill getSkillFromName(ResourceLocation skill);

    default int getSkillLevel(ResourceLocation name) {
        return getSkillLevel(name, true);
    }

    int getSkillLevel(ResourceLocation name, boolean checkActive);

    void setSkillLevel(ResourceLocation name, int level);

    void setPlayerData(ResourceLocation[] skills, int[] data);

    void addToSkill(ResourceLocation name, int value);

    boolean hasClass();

    ResourceLocation getPlayerClass();

    boolean isActive();

    void toggleActive();

    void resetClass();

    void setPlayerClass(ResourceLocation location);

    int getLevelBank();

    boolean addLevelFromExperience(EntityPlayer player);

    void changeLevelBank(int levels);

    ResourceLocation getSpecialization();

    Map<ResourceLocation, Integer> getSkills();
}
