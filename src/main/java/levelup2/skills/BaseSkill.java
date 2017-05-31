package levelup2.skills;

import levelup2.api.IPlayerSkill;

public abstract class BaseSkill implements IPlayerSkill {
    private int level;

    @Override
    public int getSkillLevel() {
        return level;
    }

    @Override
    public void setSkillLevel(int skill) {
        if (skill < 0) skill = 0;
        level = Math.min(skill, getMaxLevel());
    }

    @Override
    public boolean isMaxLevel() {
        return getSkillLevel() == getMaxLevel();
    }
}
