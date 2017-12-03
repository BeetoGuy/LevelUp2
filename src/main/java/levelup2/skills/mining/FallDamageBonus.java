package levelup2.skills.mining;

import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FallDamageBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:fallprotect";
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public byte getSkillType() {
        return 0;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.FEATHER);
    }

    @SubscribeEvent
    public void reduceFallDamage(LivingHurtEvent evt) {
        if (!isActive()) return;
        if (evt.getSource() == DamageSource.FALL && evt.getEntityLiving() instanceof EntityPlayer) {
            int skill = SkillRegistry.getSkillLevel(((EntityPlayer)evt.getEntityLiving()), getSkillName());
            if (skill > 0) {
                float reduction = skill * 0.1F;
                evt.setAmount(evt.getAmount() * (1.0F - reduction));
            }
        }
    }
/*
    @SubscribeEvent
    public void reduceFallDamage(TickEvent.PlayerTickEvent evt) {
        if (!isActive()) return;
        if (evt.phase == TickEvent.Phase.START) {
            int skill = SkillRegistry.getSkillLevel(evt.player, getSkillName());
            if (skill > 0) {
                if (evt.player.fallDistance > 0) {
                    evt.player.fallDistance *= 1 - skill / 10F;
                }
            }
        }
    }*/
}
