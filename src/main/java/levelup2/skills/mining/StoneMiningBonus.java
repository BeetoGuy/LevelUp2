package levelup2.skills.mining;

import levelup2.config.LevelUpConfig;
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
    public boolean hasSubscription() {
        return true;
    }

    @Override
    public byte getSkillType() {
        return 0;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Blocks.GOLD_ORE);
    }

    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent evt) {
        if (!isActive()) return;
        if (evt.getHarvester() != null && !evt.getWorld().isRemote) {
            int skill = SkillRegistry.getSkillLevel(evt.getHarvester(), getSkillName());
            IBlockState state = evt.getState();
            Random rand = evt.getHarvester().getRNG();

            if (LevelUpConfig.useOreChunks)
                dropOreChunks(evt, skill, state, rand);
            else {
                boolean foundBlock = false;
                for (ItemStack stack : evt.getDrops()) {
                    if (Library.isOre(stack)) {
                        stack.grow(stack.getCount());
                        foundBlock = true;
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

    private void dropOreChunks(BlockEvent.HarvestDropsEvent evt, int skill, IBlockState state, Random rand) {
        if (!Library.getOreList().isEmpty()) {
            if (Library.getOreList().contains(state.getBlock())) {
                if (rand.nextDouble() <= skill / 20D) {
                    boolean foundBlock = false;
                    for (ItemStack stack : evt.getDrops()) {
                        if (!stack.isEmpty() && state.getBlock() == Block.getBlockFromItem(stack.getItem())) {
                            ItemStack replace = getReplacementStack(stack);
                            if (!replace.isEmpty()) {
                                Library.removeFromList(evt.getDrops(), stack);
                                evt.getDrops().add(getReplacementStack(stack));
                                foundBlock = true;
                                break;
                            }
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
                else if (LevelUpConfig.alwaysDropChunks) {
                    for (ItemStack stack : evt.getDrops()) {
                        if (!stack.isEmpty() && state.getBlock() == Block.getBlockFromItem(stack.getItem())) {
                            ItemStack replace = getReplacementStack(stack);
                            if (!replace.isEmpty()) {
                                Library.removeFromList(evt.getDrops(), stack);
                                replace.setCount(1);
                                evt.getDrops().add(replace);
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
