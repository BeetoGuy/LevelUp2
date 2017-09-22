package levelup2.skills.combat;

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
    @Override
    public String getSkillName() {
        return "levelup:arrowspeed";
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.ARROW);
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
    public void onArrowLoose(EntityJoinWorldEvent evt) {
        if (!isActive()) return;
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
