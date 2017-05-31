package levelup2.skills.combat;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StealthDamage extends BaseSkill {
    @Override
    public IPlayerSkill getNewInstance() {
        return new StealthDamage();
    }

    @Override
    public String getSkillName() {
        return "levelup:stealthdamage";
    }

    @Override
    public byte getSkillType() {
        return 2;
    }

    @Override
    public int getSkillColumn() {
        return 3;
    }

    @Override
    public int getSkillRow() {
        return 1;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[] {"levelup:stealth"};
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.fiveLevels[currentLevel];
        return -1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.POISONOUS_POTATO);
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @SubscribeEvent
    public void onDamage(LivingHurtEvent evt) {
        DamageSource src = evt.getSource();
        float dmg = evt.getAmount();
        if (src.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)src.getEntity();
            int level = SkillRegistry.getSkillLevel(player, getSkillName());
            if (level > 0) {
                if (src instanceof EntityDamageSourceIndirect) {
                    if (StealthLib.getDistanceFrom(evt.getEntityLiving(), player) < 256F && player.isSneaking() && !StealthLib.canSeePlayer(evt.getEntityLiving()) && !StealthLib.entityIsFacing(evt.getEntityLiving(), player)) {
                        dmg *= 1.0F + (0.15F * level);
                        player.sendStatusMessage(new TextComponentTranslation("sneak.attack", 1.0 + (0.15 * level)), true);
                    }
                } else {
                    if (player.isSneaking() && !StealthLib.canSeePlayer(evt.getEntityLiving()) && !StealthLib.entityIsFacing(evt.getEntityLiving(), player)) {
                        dmg *= 1.0F + (0.3F * level);
                        player.sendStatusMessage(new TextComponentTranslation("sneak.attack", 1.0 + (0.3 * level)), true);
                    }
                }
            }
        }
        evt.setAmount(dmg);
    }
}
