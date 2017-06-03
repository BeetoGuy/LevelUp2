package levelup2.skills.mining;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class XPBonusMining extends BaseSkill {
    @Override
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public String getSkillName() {
        return "levelup:mining_bonus";
    }

    @Override
    public int getLevelCost(int currentLevel) {
        return -1;
    }

    @Override
    public byte getSkillType() {
        return 0;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[0];
    }

    @SubscribeEvent
    public void giveMiningXP(BlockEvent.HarvestDropsEvent evt) {
        if (evt.getHarvester() != null && !evt.getWorld().isRemote) {
            if (SkillRegistry.getSkillLevel(evt.getHarvester(), getSkillName()) > 0) {
                IBlockState state = evt.getState();
                ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                for (ItemStack ore : SkillRegistry.getOreBonusXP().keySet()) {
                    if (SkillRegistry.stackMatches(stack, ore)) {
                        SkillRegistry.addExperience(evt.getHarvester(), SkillRegistry.getOreBonusXP().get(ore));
                        break;
                    }
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
        return 0;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.DIAMOND_PICKAXE);
    }

    @Override
    public boolean isMaxLevel(int level) {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
