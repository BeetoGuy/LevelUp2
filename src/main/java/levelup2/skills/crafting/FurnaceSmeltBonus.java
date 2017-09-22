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
    public byte getSkillType() {
        return 1;
    }

    @Override
    public boolean hasSubscription() {
        return false;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.COAL);
    }
}
