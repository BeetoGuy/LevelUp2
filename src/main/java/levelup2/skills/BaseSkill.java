package levelup2.skills;

import levelup2.api.IPlayerSkill;
import net.minecraft.util.ResourceLocation;

public abstract class BaseSkill implements IPlayerSkill {
    private int[] skillLevels = {};
    private String[] prereqs = {};
    private int column = 0;
    private int row = 0;
    private boolean enabled = true;
    private boolean active = true;

    @Override
    public void setLevelCosts(int[] levelCosts) {
        skillLevels = levelCosts;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return skillLevels[currentLevel];
        return -1;
    }

    @Override
    public void setPrerequisites(String[] prerequisites) {
        prereqs = prerequisites;
    }

    @Override
    public String[] getPrerequisites() {
        return prereqs;
    }

    @Override
    public void setSkillColumn(int column) {
        this.column = column;
    }

    @Override
    public int getSkillColumn() {
        return column;
    }

    @Override
    public void setSkillRow(int row) {
        this.row = row;
    }

    @Override
    public int getSkillRow() {
        return row;
    }

    @Override
    public int getMaxLevel() {
        return skillLevels.length;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isMaxLevel(int level) {
        return level == getMaxLevel();
    }

    @Override
    public String getJsonLocation() {
        String category = getSkillType() == 0 ? "mining" : getSkillType() == 1 ? "crafting" : "combat";
        ResourceLocation location = new ResourceLocation(getSkillName());
        return category + "/" + location.getResourcePath();
    }

    @Override
    public boolean hasExternalJson() {
        return true;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean isActive) {
        active = isActive;
    }
}
