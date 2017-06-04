package levelup2.skills.crafting;

import levelup2.skills.BaseSkill;
import levelup2.util.Library;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class FurnaceSmeltBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:furnacebonus";
    }

    @Override
    public int getSkillRow() {
        return 1;
    }

    @Override
    public int getSkillColumn() {
        return 1;
    }

    @Override
    public byte getSkillType() {
        return 1;
    }

    @Override
    public boolean hasSubscription() {
        return false;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[] {"levelup:furnacespeed"};
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.tenLevels[currentLevel];
        return -1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.COAL);
    }
}
