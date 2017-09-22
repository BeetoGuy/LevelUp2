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
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.BOW);
    }

    @Override
    public byte getSkillType() {
        return 2;
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(ArrowNockEvent evt) {
        if (!isActive()) return;
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
