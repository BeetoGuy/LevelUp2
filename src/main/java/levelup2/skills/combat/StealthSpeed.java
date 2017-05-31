package levelup2.skills.combat;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class StealthSpeed extends BaseSkill {
    @Override
    public IPlayerSkill getNewInstance() {
        return new StealthSpeed();
    }

    @Override
    public String getSkillName() {
        return "levelup:stealthspeed";
    }

    @Override
    public int getSkillRow() {
        return 2;
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
        return new String[] {"levelup:stealth", "levelup:fallprotect"};
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.tenLevels[currentLevel];
        return -1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.LEATHER_BOOTS);
    }

    @SubscribeEvent
    public void onPlayerSneak(TickEvent.PlayerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START) {
            int skill = SkillRegistry.getSkillLevel(evt.player, getSkillName());
            if (skill > 0) {
                IAttributeInstance attrib = evt.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                AttributeModifier mod = new AttributeModifier(Library.sneakID, "SneakingSkillSpeed", skill / 10F, 2);
                if (evt.player.isSneaking()) {
                    if (attrib.getModifier(Library.sneakID) == null)
                        attrib.applyModifier(mod);
                }
                else if (attrib.getModifier(Library.sneakID) != null)
                    attrib.removeModifier(mod);
            }
        }
    }
}
