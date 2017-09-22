package levelup2.skills.combat;

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
    public String getSkillName() {
        return "levelup:stealthspeed";
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
        return new ItemStack(Items.LEATHER_BOOTS);
    }

    @SubscribeEvent
    public void onPlayerSneak(TickEvent.PlayerTickEvent evt) {
        if (!isActive()) return;
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
