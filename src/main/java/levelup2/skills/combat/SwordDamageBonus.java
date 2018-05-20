package levelup2.skills.combat;

import levelup2.config.LevelUpConfig;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SwordDamageBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:sworddamage";
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
        return new ItemStack(Items.GOLDEN_SWORD);
    }

    @SubscribeEvent
    public void onHurting(LivingHurtEvent evt) {
        if (!isActive()) return;
        DamageSource source = evt.getSource();
        float amount = evt.getAmount();
        if (source.getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)source.getTrueSource();
            int skill = SkillRegistry.getSkillLevel(player, getSkillName());
            if (skill > 0 && !(source instanceof EntityDamageSourceIndirect)) {
                if (!player.getHeldItemMainhand().isEmpty()) {
                    amount *= 1.0F + skill / 20F;
                    if (LevelUpConfig.damageScaling && !(evt.getEntityLiving() instanceof EntityPlayer) && evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() > 20) {
                        double health = evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
                        float skillOutput = skill / 40F;
                        amount += Math.min(health * skillOutput, health * 0.375F);
                    }
                    evt.setAmount(amount);
                }
            } else {
                skill = SkillRegistry.getSkillLevel(player, "levelup:arrowspeed");
                String src = source.getDamageType();
                if (skill > 0 && src.equals("arrow")) {
                    amount *= 1.0F + skill / 20F;
                    if (LevelUpConfig.damageScaling && !(evt.getEntityLiving() instanceof EntityPlayer) && evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() > 20) {
                        double health = evt.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
                        float skillOutput = skill / 40F;
                        amount += Math.min(health * skillOutput, health * 0.375F);
                        evt.setAmount(amount);
                    }
                }
            }
        }
    }
}
