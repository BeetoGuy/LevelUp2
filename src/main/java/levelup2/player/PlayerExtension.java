package levelup2.player;

import com.google.common.collect.Maps;
import levelup2.api.ICharacterClass;
import levelup2.api.IPlayerSkill;
import levelup2.api.PlayerSkillStorage;
import levelup2.config.LevelUpConfig;
import levelup2.skills.SkillRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * BIG TODO: Classes and skills revamp
 * -Classes from classic come back, give skill bonuses based on what class is chosen along with the spec
 * -Multispeccing: Spend skill levels to unlock another of the three main XP gain methods (limit to one?)
 */
public class PlayerExtension implements IPlayerClass {
    private Map<ResourceLocation, Integer> skillMap = new HashMap<>();
    private ResourceLocation playerClass;
    private int levels = 0;
    private boolean isActive = true;

    public PlayerExtension() {
        for (ResourceLocation loc : SkillRegistry.getSkills().keySet()) {
            skillMap.put(loc, 0);
        }
    }

    @Override
    public NBTTagCompound saveNBTData(NBTTagCompound tag) {
        if (playerClass != null)
            tag.setString("Class", playerClass.toString());
        if (levels > 0)
            tag.setInteger("Levels", levels);
        Map<String, NBTTagCompound> types = Maps.newHashMap();
        for (ResourceLocation skillName : skillMap.keySet()) {
            IPlayerSkill skill = getSkillFromName(skillName);
            ResourceLocation skillType = skill.getSkillType();
            String type = skillType.getPath();
            if (type.endsWith("_bonus"))
                type = type.replace("_bonus", "");
            if (!types.containsKey(type)) {
                types.put(type, new NBTTagCompound());
            }
            types.get(type).setInteger(skill.getSkillName().toString(), skillMap.get(skillName));
        }
        for (String type : types.keySet()) {
            tag.setTag(type, types.get(type));
        }
        tag.setByte("Version", (byte)2);
        tag.setBoolean("Active", isActive);
        return tag;
    }

    @Override
    public void loadNBTData(NBTTagCompound tag) {
        if (!tag.hasKey("Version")) {
            for (ResourceLocation skill : skillMap.keySet()) {
                IPlayerSkill sk = getSkillFromName(skill);
                ResourceLocation type = sk.getSkillType();
                String skillName = type.getPath().startsWith("m") ? "Mining" : type.getPath().startsWith("cr") ? "Crafting" : "Combat";
                NBTTagCompound skillTag = tag.getCompoundTag(skillName);
                setSkillLevel(skill, skillTag.getInteger(sk.getSkillName().toString()));
            }
        }
        else {
            for (ResourceLocation skill : skillMap.keySet()) {
                IPlayerSkill playerSkill = getSkillFromName(skill);
                ResourceLocation skillType = playerSkill.getSkillType();
                String skillName = skillType.getPath();
                if (skillName.endsWith("_bonus")) {
                    skillName = skillName.replace("_bonus", "");
                }
                NBTTagCompound skillTag = tag.getCompoundTag(skillName);
                setSkillLevel(skill, skillTag.getInteger(playerSkill.getSkillName().toString()));
            }
        }
        if (tag.hasKey("Class")) {
            playerClass = new ResourceLocation(tag.getString("Class"));
        } else {
            playerClass = getClassFromSpecialization();
        }
        if (tag.hasKey("Levels")) {
            levels = tag.getInteger("Levels");
        }
        if (tag.hasKey("Active")) {
            isActive = tag.getBoolean("Active");
        }
    }

    @Override
    public IPlayerSkill getSkillFromName(ResourceLocation name) {
        return SkillRegistry.getSkillFromName(name);
    }

    @Override
    public void addToSkill(ResourceLocation name, int increase) {
        setSkillLevel(name, skillMap.get(name) + increase);
    }

    @Override
    public int getSkillLevel(ResourceLocation name, boolean checkActive) {
        if (getSkillFromName(name) == null || !getSkillFromName(name).isActive() || (checkActive && !isActive())) return 0;
        return skillMap.get(name);
    }

    @Override
    public void setSkillLevel(ResourceLocation name, int level) {
        IPlayerSkill skill = getSkillFromName(name);
        if (level > skill.getMaxLevel()) level = skill.getMaxLevel();
        skillMap.put(name, level);
    }

    @Override
    public void setPlayerData(ResourceLocation[] names, int[] data) {
        for (int i = 0; i < names.length && i < data.length; i++) {
            setSkillLevel(names[i], data[i]);
        }
    }

    @Override
    public boolean hasClass() {
        return playerClass != null;
    }

    @Override
    public ResourceLocation getPlayerClass() {
        return playerClass;
    }

    @Override
    public int getLevelBank() {
        return levels;
    }

    @Override
    public boolean addLevelFromExperience(EntityPlayer player) {
        if (player.experienceLevel >= LevelUpConfig.levelCost) {
            levels++;
            player.addExperienceLevel(-LevelUpConfig.levelCost);
            return true;
        }
        return false;
    }

    @Override
    public void changeLevelBank(int levels) {
        this.levels = Math.max(0, levels);
    }

    @Override
    public ResourceLocation getSpecialization() {
        if (playerClass != null) {
            return Objects.requireNonNull(SkillRegistry.getClassFromName(playerClass)).getSpecializationSkill().getSkillName();
        }
        return null;
    }

    private ResourceLocation getClassFromSpecialization() {
        if (getSpec() > -1) {
            switch(getSpec()) {
                case 1: return new ResourceLocation("levelup", "artisan");
                case 2: return new ResourceLocation("levelup", "warrior");
                default: return new ResourceLocation("levelup", "miner");
            }
        }
        return null;
    }

    private byte getSpec() {
        if (getSkillLevel(new ResourceLocation("levelup:mining_bonus"), false) > 0) {
            return 0;
        }
        else if (getSkillLevel(new ResourceLocation("levelup:craft_bonus"), false) > 0) {
            return 1;
        }
        else if (getSkillLevel(new ResourceLocation("levelup:combat_bonus"), false) > 0) {
            return 2;
        }
        return -1;
    }

    @Override
    public void resetClass() {
        for (ResourceLocation loc : SkillRegistry.getSkills().keySet()) {
            skillMap.put(loc, 0);
        }
        playerClass = null;
    }

    @Override
    public Map<ResourceLocation, Integer> getSkills() {
        return skillMap;
    }

    @Override
    public void toggleActive() {
        isActive = !isActive;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setPlayerClass(ResourceLocation location) {
        ICharacterClass cl = SkillRegistry.getClassFromName(location);
        if (this.playerClass != null && cl != null) {
            ICharacterClass oldClass = SkillRegistry.getClassFromName(this.playerClass);
            if (oldClass != null && !oldClass.getSkillBonuses().isEmpty()) {
                skillMap.put(oldClass.getSpecializationSkill().getSkillName(), skillMap.get(oldClass.getSpecializationSkill().getSkillName()) - 1);
                for (PlayerSkillStorage sk : oldClass.getSkillBonuses()) {
                    int level = skillMap.get(sk.getSkill().getSkillName());
                    skillMap.put(sk.getSkill().getSkillName(), Math.max(0, level - sk.getLevel()));
                }
            }
        }
        if (cl != null) {
            if (skillMap.get(cl.getSpecializationSkill().getSkillName()) < 1)
                skillMap.put(cl.getSpecializationSkill().getSkillName(), 1);
            if (!cl.getSkillBonuses().isEmpty()) {
                for (PlayerSkillStorage sk : cl.getSkillBonuses()) {
                    if (skillMap.get(sk.getSkill().getSkillName()) < sk.getLevel())
                        skillMap.put(sk.getSkill().getSkillName(), sk.getLevel());
                }
            }
        }
        this.playerClass = location;
    }
}
