package levelup2.player;

import levelup2.api.IPlayerSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlayerExtension implements IPlayerClass {
    private Map<String, Integer> skillMap = new HashMap<>();

    public PlayerExtension() {
        for (IPlayerSkill skill : SkillRegistry.getSkillRegistry()) {
            skillMap.put(skill.getSkillName(), 0);
        }
    }

    @Override
    public NBTTagCompound saveNBTData(NBTTagCompound tag) {
        NBTTagCompound combat = new NBTTagCompound();
        NBTTagCompound crafting = new NBTTagCompound();
        NBTTagCompound mining = new NBTTagCompound();
        for (String skillName : skillMap.keySet()) {
            IPlayerSkill skill = getSkillFromName(skillName);
            byte skillType = skill.getSkillType();
            switch(skillType) {
                case 1: crafting.setInteger(skill.getSkillName(), skillMap.get(skillName));
                    break;
                case 2: combat.setInteger(skill.getSkillName(), skillMap.get(skillName));
                    break;
                default: mining.setInteger(skill.getSkillName(), skillMap.get(skillName));
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
            IPlayerSkill playerSkill = getSkillFromName(skill);
            byte skillType = playerSkill.getSkillType();
            String skillName = skillType == 0 ? "Mining" : skillType == 1 ? "Crafting" : "Combat";
            NBTTagCompound skillTag = tag.getCompoundTag(skillName);
            setSkillLevel(skill, skillTag.getInteger(playerSkill.getSkillName()));
        }
    }

    @Override
    public IPlayerSkill getSkillFromName(String name) {
        return SkillRegistry.getSkillFromName(name);
    }

    @Override
    public void addToSkill(String name, int increase) {
        setSkillLevel(name, skillMap.get(name) + increase);
    }

    @Override
    public int getSkillLevel(String name) {
        return skillMap.get(name);
    }

    @Override
    public void setSkillLevel(String name, int level) {
        skillMap.put(name, level);
    }

    @Override
    public void setPlayerData(String[] names, int[] data) {
        for (int i = 0; i < names.length && i < data.length; i++) {
            setSkillLevel(names[i], data[i]);
        }
    }

    @Override
    public boolean hasClass() {
        return getSkillLevel("levelup:mining_bonus") > 0 || getSkillLevel("levelup:craft_bonus") > 0 || getSkillLevel("levelup:combat_bonus") > 0;
    }

    @Override
    public byte getSpecialization() {
        if (getSkillLevel("levelup:mining_bonus") > 0) {
            return 0;
        }
        else if (getSkillLevel("levelup:craft_bonus") > 0) {
            return 1;
        }
        else if (getSkillLevel("levelup:combat_bonus") > 0) {
            return 2;
        }
        return -1;
    }

    @Override
    public void setSpecialization(byte spec) {
        if (spec > -1) {
            switch (spec) {
                case 0:
                    setSkillLevel("levelup:mining_bonus", 1);
                    setSkillLevel("levelup:craft_bonus", 0);
                    setSkillLevel("levelup:combat_bonus", 0);
                    break;
                case 1:
                    setSkillLevel("levelup:mining_bonus", 0);
                    setSkillLevel("levelup:craft_bonus", 1);
                    setSkillLevel("levelup:combat_bonus", 0);
                    break;
                case 2:
                    setSkillLevel("levelup:mining_bonus", 0);
                    setSkillLevel("levelup:craft_bonus", 0);
                    setSkillLevel("levelup:combat_bonus", 1);
                    break;
            }
        }
    }

    @Override
    public Map<String, Integer> getSkills() {
        return skillMap;
    }
}
