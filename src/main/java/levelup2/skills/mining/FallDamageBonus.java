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

public class FallDamageBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:fallprotect";
    }

    @Override
    public int getSkillRow() {
        return 1;
    }

    @Override
    public int getSkillColumn() {
        return 3;
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.fiveLevels[currentLevel];
        return -1;
    }

    @Override
    public byte getSkillType() {
        return 0;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[] {"levelup:sprintspeed"};
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.FEATHER);
    }

    @SubscribeEvent
    public void reduceFallDamage(LivingHurtEvent evt) {
        if (evt.getSource() == DamageSource.FALL && evt.getEntityLiving() instanceof EntityPlayer) {
            int skill = SkillRegistry.getSkillLevel(((EntityPlayer)evt.getEntityLiving()), getSkillName());
            if (skill > 0) {
                float reduction = skill * 0.1F;
                evt.setAmount(evt.getAmount() * (1.0F - reduction));
            }
        }
    }
}
