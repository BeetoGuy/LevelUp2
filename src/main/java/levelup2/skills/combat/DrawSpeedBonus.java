package levelup2.skills.combat;

import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DrawSpeedBonus extends BaseSkill {
    @Override
    public String getSkillName() {
        return "levelup:bowdraw";
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.tenLevels[currentLevel];
        return -1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.BOW);
    }

    @Override
    public int getSkillRow() {
        return 0;
    }

    @Override
    public int getSkillColumn() {
        return 2;
    }

    @Override
    public byte getSkillType() {
        return 2;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[0];
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(ArrowNockEvent evt) {
        int archery = SkillRegistry.getSkillLevel(evt.getEntityPlayer(), getSkillName());
        if (archery > 0) {
            evt.getEntityPlayer().setActiveHand(evt.getHand());
            setItemUseCount(evt.getEntityPlayer(), archery);
            evt.setAction(new ActionResult<>(EnumActionResult.SUCCESS, evt.getBow()));
        }
    }

    private void setItemUseCount(EntityPlayer player, int archery) {
        player.activeItemStackUseCount -= archery;
    }
}
