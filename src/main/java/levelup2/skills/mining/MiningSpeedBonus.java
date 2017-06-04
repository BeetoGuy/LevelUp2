package levelup2.skills.mining;

import levelup2.skills.BaseSkill;
import levelup2.util.Library;

public abstract class MiningSpeedBonus extends BaseSkill {
    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.tenLevels[currentLevel];
        return -1;
    }

    @Override
    public byte getSkillType() {
        return 0;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[0];
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }
}
