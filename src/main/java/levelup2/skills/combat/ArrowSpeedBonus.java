package levelup2.skills.combat;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ArrowSpeedBonus extends BaseSkill {
    private int[] levels = {11, 17, 29, 37, 41};

    @Override
    public String getSkillName() {
        return "levelup:arrowspeed";
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.fiveLevels[currentLevel];
        return -1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public String[] getPrerequisites() {
        return new String[] {"levelup:bowdraw"};
    }

    @Override
    public int getSkillRow() {
        return 1;
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
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onArrowLoose(EntityJoinWorldEvent evt) {
        if (evt.getEntity() instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow)evt.getEntity();
            if (arrow.shootingEntity instanceof EntityPlayer) {
                int archer = SkillRegistry.getSkillLevel((EntityPlayer)arrow.shootingEntity, getSkillName());
                if (archer > 0) {
                    arrow.motionX *= 1.0F + archer / 10F;
                    arrow.motionY *= 1.0F + archer / 10F;
                    arrow.motionZ *= 1.0F + archer / 10F;
                }
            }
        }
    }
}
