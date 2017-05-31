package levelup2.skills.combat;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StealthBonus extends BaseSkill {
    @Override
    public IPlayerSkill getNewInstance() {
        return new StealthBonus();
    }

    @Override
    public String getSkillName() {
        return "levelup:stealth";
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.tenLevels[currentLevel];
        return -1;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getSkillRow() {
        return 0;
    }

    @Override
    public int getSkillColumn() {
        return 3;
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
    public String[] getPrerequisites() {
        return new String[0];
    }

    @Override
    public ItemStack getRepresentativeStack() {
        ItemStack stack = new ItemStack(Items.POTIONITEM);
        PotionUtils.addPotionToItemStack(stack, PotionType.getPotionTypeForName("invisibility"));
        return stack;
    }

    @SubscribeEvent
    public void onTargetSet(LivingSetAttackTargetEvent evt) {
        if (evt.getTarget() instanceof EntityPlayer && evt.getEntityLiving() instanceof EntityMob) {
            if (evt.getTarget().isSneaking() && !StealthLib.entityHasVisionOf(evt.getEntityLiving(), (EntityPlayer)evt.getTarget())
                    && evt.getEntityLiving().getRevengeTimer() != ((EntityMob) evt.getEntityLiving()).ticksExisted) {
                ((EntityMob) evt.getEntityLiving()).setAttackTarget(null);
            }
        }
    }
}
