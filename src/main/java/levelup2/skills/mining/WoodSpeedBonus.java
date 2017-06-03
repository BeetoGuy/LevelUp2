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

public class WoodSpeedBonus extends MiningSpeedBonus {
    @Override
    public String getSkillName() {
        return "levelup:woodcutting";
    }

    @SubscribeEvent
    public void onBreak(PlayerEvent.BreakSpeed evt) {
        EntityPlayer player = evt.getEntityPlayer();
        if (player != null) {
            int skill = SkillRegistry.getSkillLevel(player, getSkillName());
            if (skill > 0) {
                IBlockState state = evt.getState();
                float speed = evt.getNewSpeed();
                if (state.getMaterial() == Material.WOOD) {
                    evt.setNewSpeed(speed + (skill * 0.2F));
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
        return 2;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.IRON_AXE);
    }
}
