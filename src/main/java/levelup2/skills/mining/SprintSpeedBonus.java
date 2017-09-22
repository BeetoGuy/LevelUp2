package levelup2.skills.mining;

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

public class SprintSpeedBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:sprintspeed";
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
        return new ItemStack(Items.RABBIT_FOOT);
    }

    @SubscribeEvent
    public void onPlayerSprint(TickEvent.PlayerTickEvent evt) {
        if (!isActive()) return;
        if (evt.phase == TickEvent.Phase.START) {
            int skill = SkillRegistry.getSkillLevel(evt.player, getSkillName());
            if (skill > 0) {
                IAttributeInstance attrib = evt.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                AttributeModifier mod = new AttributeModifier(Library.speedID, "SprintingSkillSpeed", skill / 20F, 2);
                if (evt.player.isSprinting()) {
                    if (attrib.getModifier(Library.speedID) == null)
                        attrib.applyModifier(mod);
                }
                else if (attrib.getModifier(Library.speedID) != null)
                    attrib.removeModifier(mod);
            }
        }
    }
}
