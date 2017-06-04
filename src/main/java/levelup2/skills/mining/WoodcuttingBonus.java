package levelup2.skills.mining;

import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import levelup2.util.PlankCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class WoodcuttingBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:lumbering";
    }

    @Override
    public int getSkillRow() {
        return 1;
    }

    @Override
    public int getSkillColumn() {
        return 2;
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
        return new String[] {"levelup:woodcutting"};
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Blocks.LOG);
    }

    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent evt) {
        if (evt.getHarvester() != null && !evt.getWorld().isRemote) {
            int skill = SkillRegistry.getSkillLevel(evt.getHarvester(), getSkillName());
            IBlockState state = evt.getState();
            Random rand = evt.getHarvester().getRNG();
            if (skill > 0) {
                if (PlankCache.contains(state.getBlock(), state.getBlock().damageDropped(state))) {
                    if (rand.nextDouble() <= skill / 30D) {
                        ItemStack planks = PlankCache.getProduct(state.getBlock(), state.getBlock().damageDropped(state));
                        if (!planks.isEmpty())
                            evt.getDrops().add(planks.copy());
                    }
                    if (rand.nextDouble() <= skill / 30D) {
                        evt.getDrops().add(new ItemStack(Items.STICK, 2));
                    }
                }
            }
        }
    }
}
