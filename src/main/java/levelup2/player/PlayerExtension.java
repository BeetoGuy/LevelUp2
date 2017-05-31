package levelup2.player;

import levelup2.api.IPlayerSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerExtension implements IPlayerClass {
    private static Map<String, IPlayerSkill> skillMap = new HashMap<>();

    public PlayerExtension() {
        for (IPlayerSkill skill : SkillRegistry.getSkillRegistry()) {
            IPlayerSkill skillCopy = skill.getNewInstance();
            skillMap.put(skill.getSkillName(), skillCopy);
        }
    }

    @Override
    public NBTTagCompound saveNBTData(NBTTagCompound tag) {
        NBTTagCompound combat = new NBTTagCompound();
        NBTTagCompound crafting = new NBTTagCompound();
        NBTTagCompound mining = new NBTTagCompound();
        for (String skillName : skillMap.keySet()) {
            IPlayerSkill skill = skillMap.get(skillName);
            byte skillType = skill.getSkillType();
            switch(skillType) {
                case 1: crafting.setInteger(skill.getSkillName(), skill.getSkillLevel());
                    break;
                case 2: combat.setInteger(skill.getSkillName(), skill.getSkillLevel());
                    break;
                default: mining.setInteger(skill.getSkillName(), skill.getSkillLevel());
            }
        }
        tag.setTag("Combat", combat);
        tag.setTag("Crafting", crafting);
        tag.setTag("Mining", mining);
        return tag;
    }

    @Override
    public void loadNBTData(NBTTagCompound tag) {
        for (String skill : skillMap.keySet()) {
            IPlayerSkill playerSkill = skillMap.get(skill);
            byte skillType = playerSkill.getSkillType();
            String skillName = skillType == 0 ? "Mining" : skillType == 1 ? "Crafting" : "Combat";
            NBTTagCompound skillTag = tag.getCompoundTag(skillName);
            playerSkill.setSkillLevel(skillTag.getInteger(playerSkill.getSkillName()));
            setSkill(skill, playerSkill);
        }
    }

    @Override
    public IPlayerSkill getSkillFromName(String name) {
        if (skillMap.containsKey(name))
            return skillMap.get(name);
        return null;
    }

    @Override
    public void addToSkill(String name, int increase) {
        if (getSkillFromName(name) != null) {
            setSkillLevel(name, getSkillFromName(name).getSkillLevel() + increase);
        }
    }

    @Override
    public void setSkillLevel(String name, int level) {
        if (getSkillFromName(name) != null) {
            IPlayerSkill skill = getSkillFromName(name);
            skill.setSkillLevel(level);
            setSkill(name, skill);
        }
    }

    @Override
    public void setPlayerData(String[] names, int[] data) {
        for (int i = 0; i < names.length && i < data.length; i++) {
            IPlayerSkill skill = getSkillFromName(names[i]);
            if (skill != null) {
                skill.setSkillLevel(data[i]);
                setSkill(names[i], getSkillFromName(names[i]));
            }
        }
    }

    @Override
    public boolean hasClass() {
        return getSkillFromName("levelup:mining_bonus").getSkillLevel() > 0 || getSkillFromName("levelup:craft_bonus").getSkillLevel() > 0 || getSkillFromName("levelup:combat_bonus").getSkillLevel() > 0;
    }

    @Override
    public byte getSpecialization() {
        if (getSkillFromName("levelup:mining_bonus").getSkillLevel() > 0) {
            return 0;
        }
        else if (getSkillFromName("levelup:craft_bonus").getSkillLevel() > 0) {
            return 1;
        }
        else if (getSkillFromName("levelup:combat_bonus").getSkillLevel() > 0) {
            return 2;
        }
        return -1;
    }

    @Override
    public void setSpecialization(byte spec) {
        if (spec > -1) {
            switch (spec) {
                case 0:
                    setSkill("levelup:mining_bonus", 1);
                    break;
                case 1:
                    setSkill("levelup:craft_bonus", 1);
                    break;
                case 2:
                    setSkill("levelup:combat_bonus", 1);
                    break;
            }
        }
    }

    @Override
    public List<IPlayerSkill> getSkills() {
        List<IPlayerSkill> skills = new ArrayList<>();
        skills.addAll(skillMap.values());
        return skills;
    }

    public static Map<String, IPlayerSkill> getSkillMap() {
        return skillMap;
    }

    public static void setSkill(String name, int skill) {
        IPlayerSkill sk = getSkillMap().get(name);
        sk.setSkillLevel(skill);
        setSkill(name, sk);
    }

    public static void setSkill(String name, IPlayerSkill skill) {
        skillMap.put(name, skill);
    }
}
