package levelup2.skills.combat;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class XPBonusCombat extends BaseSkill {
    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public String getSkillName() {
        return "levelup:combat_bonus";
    }

    @Override
    public int getLevelCost(int currentLevel) {
        return -1;
    }

    @Override
    public byte getSkillType() {
        return 2;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[0];
    }

    @SubscribeEvent
    public void getCombatBonus(LivingDeathEvent evt) {
        if (evt.getEntityLiving() instanceof EntityMob && evt.getSource().getEntity() instanceof EntityPlayer) {
            if (SkillRegistry.getSkillLevel((EntityPlayer)evt.getSource().getEntity(), getSkillName()) > 0) {
                int deathXP = (int) evt.getEntityLiving().getMaxHealth();
                SkillRegistry.addExperience((EntityPlayer) evt.getSource().getEntity(), deathXP / 10);
            }
        }
    }

    @Override
    public int getSkillRow() {
        return 0;
    }

    @Override
    public int getSkillColumn() {
        return 0;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.DIAMOND_SWORD);
    }

    @Override
    public boolean isMaxLevel(int level) {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
