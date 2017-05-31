package levelup2.skills.combat;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NaturalArmorBonus extends BaseSkill {
    @Override
    public IPlayerSkill getNewInstance() {
        return new NaturalArmorBonus();
    }

    @Override
    public String getSkillName() {
        return "levelup:naturalarmor";
    }

    @Override
    public int getSkillRow() {
        return 1;
    }

    @Override
    public int getSkillColumn() {
        return 4;
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
        return new String[] {"levelup:shieldblock"};
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.highTenLevels[currentLevel];
        return -1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.IRON_CHESTPLATE);
    }

    @SubscribeEvent
    public void onDamageTaken(LivingHurtEvent evt) {
        if (evt.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)evt.getEntityLiving();
            int skill = SkillRegistry.getSkillLevel(player, getSkillName());
            if (skill > 0) {
                if (!evt.getSource().isUnblockable()) {
                    float amount = evt.getAmount() * (1.0F - skill / 20F);
                    evt.setAmount(amount);
                }
            }
        }
    }
}
