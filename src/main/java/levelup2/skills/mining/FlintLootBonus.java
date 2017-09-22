package levelup2.skills.mining;

import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class FlintLootBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:flintloot";
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
        return new ItemStack(Blocks.GRAVEL);
    }

    @SubscribeEvent
    public void gravelLooting(BlockEvent.HarvestDropsEvent evt) {
        if (!isActive()) return;
        if (evt.getHarvester() != null && !evt.getWorld().isRemote) {
            IBlockState state = evt.getState();
            Random rand = evt.getHarvester().getRNG();
            int skill = SkillRegistry.getSkillLevel(evt.getHarvester(), getSkillName());
            if (!evt.isSilkTouching() && skill > 0) {
                if (state.getBlock() instanceof BlockGravel) {
                    if (rand.nextInt(10) < skill) {
                        Library.removeFromList(evt.getDrops(), new ItemStack(state.getBlock()));
                        evt.getDrops().add(new ItemStack(Items.FLINT));
                    }
                }
            }
        }
    }
}
