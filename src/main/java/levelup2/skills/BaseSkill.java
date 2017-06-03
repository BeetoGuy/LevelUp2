package levelup2.skills;

import levelup2.api.IPlayerSkill;

public abstract class BaseSkill implements IPlayerSkill {
    @Override
    public boolean isMaxLevel(int level) {
        return level == getMaxLevel();
    }
}
