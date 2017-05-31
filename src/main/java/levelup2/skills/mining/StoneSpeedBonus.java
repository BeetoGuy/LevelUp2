package levelup2.skills.mining;

import levelup2.api.IPlayerSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StoneSpeedBonus extends MiningSpeedBonus {
    @Override
    public String getSkillName() {
        return "levelup:stonecutting";
    }

    @SubscribeEvent
    public void onBreak(PlayerEvent.BreakSpeed evt) {
        EntityPlayer player = evt.getEntityPlayer();
        if (player != null) {
            IPlayerSkill skill = SkillRegistry.getPlayer(player).getSkillFromName(getSkillName());
            if (skill.getSkillLevel() > 0) {
                IBlockState state = evt.getState();
                float speed = evt.getNewSpeed();
                if (state.getMaterial() == Material.ROCK) {
                    evt.setNewSpeed(speed + (skill.getSkillLevel() * 0.3F));
                }
            }
        }
    }

    @Override
    public int getSkillRow() {
        return 0;
    }

    @Override
    public int getSkillColumn() {
        return 1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.IRON_PICKAXE);
    }

    @Override
    public IPlayerSkill getNewInstance() {
        return new StoneSpeedBonus();
    }
}
