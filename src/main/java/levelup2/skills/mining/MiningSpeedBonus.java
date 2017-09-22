package levelup2.skills.mining;

import levelup2.skills.BaseSkill;
import levelup2.util.Library;

public abstract class MiningSpeedBonus extends BaseSkill {
    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public byte getSkillType() {
        return 0;
    }
}
