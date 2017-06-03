package levelup2.skills.mining;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class StoneMiningBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:prospecting";
    }

    @Override
    public int getSkillRow() {
        return 1;
    }

    @Override
    public int getSkillColumn() {
        return 1;
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
        return new String[] {"levelup:stonecutting"};
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Blocks.GOLD_ORE);
    }

    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent evt) {
        if (evt.getHarvester() != null && !evt.getWorld().isRemote) {
            int skill = SkillRegistry.getSkillLevel(evt.getHarvester(), getSkillName());
            IBlockState state = evt.getState();
            Random rand = evt.getHarvester().getRNG();

            if (!Library.getOreList().isEmpty()) {
                if (Library.getOreList().contains(state.getBlock())) {
                    if (rand.nextDouble() <= skill / 20D) {
                        boolean foundBlock = false;
                        for (ItemStack stack : evt.getDrops()) {
                            if (!stack.isEmpty() && state.getBlock() == Block.getBlockFromItem(stack.getItem())) {
                                Library.removeFromList(evt.getDrops(), stack);
                                evt.getDrops().add(getReplacementStack(stack));
                                foundBlock = true;
                                break;
                            }
                        }
                        if (!foundBlock) {
                            Item item = state.getBlock().getItemDropped(state, rand, evt.getFortuneLevel());
                            if (item != null) {
                                int quantity = state.getBlock().quantityDropped(state, evt.getFortuneLevel(), rand);
                                if (quantity > 0)
                                    evt.getDrops().add(new ItemStack(item, quantity, state.getBlock().damageDropped(state)));
                            }
                        }
                    }
                }
            }
        }
    }

    private ItemStack getReplacementStack(ItemStack stack) {
        return Library.getChunkFromName(Library.getOreNameForBlock(stack));
    }
}
