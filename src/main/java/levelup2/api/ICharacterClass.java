package levelup2.api;

import levelup2.capability.PlayerCapability;
import levelup2.player.IPlayerClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICharacterClass {
    @Nonnull
    ResourceLocation getClassName();

    default String getUnlocalizedName() {
        return "class." + getClassName().toString() + ".name";
    }

    default String getUnlocalizedDescription() {
        return "class." + getClassName().toString() + ".desc";
    }

    String getLocalizedName();

    String getLocalizedDescription();

    @Nonnull
    IPlayerSkill getSpecializationSkill();

    List<PlayerSkillStorage> getSkillBonuses();

    default int getBonusSkillLevel(IPlayerSkill skill) {
        if (getSkillBonuses() != null && !getSkillBonuses().isEmpty()) {
            for (PlayerSkillStorage stor : getSkillBonuses()) {
                if (stor.getSkill().getSkillName().equals(skill.getSkillName())) {
                    return stor.getLevel();
                }
            }
        }
        return 0;
    }

    default ItemStack getRepresentativeStack() {
        return getSpecializationSkill() != null ? getSpecializationSkill().getRepresentativeStack() : ItemStack.EMPTY;
    }

    default void applyBonus(EntityPlayer player) {
        IPlayerClass pClass = player.getCapability(PlayerCapability.PLAYER_CLASS, EnumFacing.UP);
        if (pClass != null) {
            pClass.setSkillLevel(getSpecializationSkill().getSkillName(), 1);
            if (!getSkillBonuses().isEmpty()) {
                for (PlayerSkillStorage skill : getSkillBonuses()) {
                    pClass.setSkillLevel(skill.getSkill().getSkillName(), skill.getLevel());
                }
            }
        }
    }
}
