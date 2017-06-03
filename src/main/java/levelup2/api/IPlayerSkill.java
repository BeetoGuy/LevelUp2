package levelup2.api;

import net.minecraft.item.ItemStack;

public interface IPlayerSkill {
    boolean hasSubscription();

    String getSkillName();

    int getLevelCost(int currentLevel);

    /**
    *0: Mining; 1: Crafting; 2: Combat
     */
    byte getSkillType();

    String[] getPrerequisites();

    int getSkillColumn();

    int getSkillRow();

    /**
     * The ItemStack that renders in the GUI
     * @return A stack with the skill level.
     */
    ItemStack getRepresentativeStack();

    boolean isMaxLevel(int level);

    int getMaxLevel();

    IPlayerSkill getNewInstance();
}
