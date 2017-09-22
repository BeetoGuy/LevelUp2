package levelup2.api;

import net.minecraft.item.ItemStack;

public interface IPlayerSkill {
    boolean hasSubscription();

    String getSkillName();

    int getLevelCost(int currentLevel);

    void setLevelCosts(int[] levels);

    /**
    *0: Mining; 1: Crafting; 2: Combat
     */
    byte getSkillType();

    String[] getPrerequisites();

    void setPrerequisites(String[] prereqs);

    int getSkillColumn();

    void setSkillColumn(int column);

    int getSkillRow();

    void setSkillRow(int row);

    /**
     * The ItemStack that renders in the GUI
     * @return A stack with the skill level.
     */
    ItemStack getRepresentativeStack();

    boolean isMaxLevel(int level);

    int getMaxLevel();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    String getJsonLocation();

    boolean hasExternalJson();

    boolean isActive();

    void setActive(boolean active);
}
