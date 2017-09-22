package levelup2.skills.mining;

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
        if (!isActive()) return;
        EntityPlayer player = evt.getEntityPlayer();
        if (player != null) {
            int skill = SkillRegistry.getSkillLevel(evt.getEntityPlayer(), getSkillName());
            if (skill > 0) {
                IBlockState state = evt.getState();
                float speed = evt.getNewSpeed();
                if (state.getMaterial() == Material.ROCK) {
                    evt.setNewSpeed(speed + (skill * 0.3F));
                }
            }
        }
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.IRON_PICKAXE);
    }
}
