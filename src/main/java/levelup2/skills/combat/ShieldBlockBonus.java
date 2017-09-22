package levelup2.skills.combat;

import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShieldBlockBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:shieldblock";
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
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.SHIELD);
    }

    @SubscribeEvent
    public void onDamageTaken(LivingHurtEvent evt) {
        if (!isActive()) return;
        if (evt.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)evt.getEntityLiving();
            int skill = SkillRegistry.getSkillLevel(player, getSkillName());
            if (skill > 0) {
                if (isBlocking(player) && player.getRNG().nextFloat() < skill / 10F) {
                    evt.setAmount(0F);
                }
            }
        }
    }

    private boolean isBlocking(EntityPlayer player) {
        return player.isHandActive() && !player.getActiveItemStack().isEmpty() && player.getActiveItemStack().getItem().getItemUseAction(player.getActiveItemStack()) == EnumAction.BLOCK;
    }
}
