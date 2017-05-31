package levelup2.skills.mining;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FallDamageBonus extends BaseSkill {
    @Override
    public IPlayerSkill getNewInstance() {
        return new FallDamageBonus();
    }

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
    public void reduceFallDamage(TickEvent.PlayerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START) {
            int skill = SkillRegistry.getSkillLevel(evt.player, getSkillName());
            if (skill > 0) {
                if (evt.player.fallDistance > 0) {
                    evt.player.fallDistance *= 1 - skill / 10F;
                }
            }
        }
    }
}
