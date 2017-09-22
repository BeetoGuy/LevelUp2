package levelup2.skills.combat;

import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SwordCritBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:swordcrit";
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
        return new ItemStack(Items.IRON_SWORD);
    }

    @SubscribeEvent
    public void onHurting(LivingHurtEvent evt) {
        if (!isActive()) return;
        DamageSource source = evt.getSource();
        float amount = evt.getAmount();
        if (source.getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)source.getTrueSource();
            int skill = SkillRegistry.getSkillLevel(player, getSkillName());
            if (skill > 0) {
                if (!(source instanceof EntityDamageSourceIndirect)) {
                    if (!player.getHeldItemMainhand().isEmpty()) {
                        if (player.getRNG().nextDouble() <= skill / 20D)
                            evt.setAmount(amount * 2.0F);
                    }
                }
            }
        }
    }
}
