package levelup2.skills.mining;

import levelup2.config.LevelUpConfig;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
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

            dropOreChunks(evt, skill, state, rand);
        }
    }

    @SubscribeEvent
    public void stopPlacingDupes(PlayerInteractEvent.RightClickBlock evt) {
        ItemStack stack = evt.getItemStack();
        if (!stack.isEmpty() && stack.hasTagCompound()) {
            if (stack.getTagCompound().hasKey("NoPlace")) {
                if (Library.isOre(stack))
                    evt.setUseItem(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void addTooltip(ItemTooltipEvent evt) {
        ItemStack stack = evt.getItemStack();
        if (stack.hasTagCompound()) {
            if (stack.getTagCompound().hasKey("NoPlace"))
                evt.getToolTip().add(I18n.format("flag.noplace"));
        }
    }

    private void dropOreChunks(BlockEvent.HarvestDropsEvent evt, int skill, IBlockState state, Random rand) {
        if (!Library.getOreList().isEmpty()) {
            if (Library.getOreList().contains(state.getBlock())) {
                if (rand.nextDouble() <= skill / 20D) {
                    boolean foundBlock = false;
                    for (ItemStack stack : evt.getDrops()) {
                        if (!stack.isEmpty() && state.getBlock() == Block.getBlockFromItem(stack.getItem())) {
                            ItemStack replace = getReplacementStack(stack, true);
                            if (!replace.isEmpty()) {
                                Library.removeFromList(evt.getDrops(), stack);
                                evt.getDrops().add(replace);
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
                else if (LevelUpConfig.useOreChunks && LevelUpConfig.alwaysDropChunks) {
                    for (ItemStack stack : evt.getDrops()) {
                        if (!stack.isEmpty() && state.getBlock() == Block.getBlockFromItem(stack.getItem())) {
                            ItemStack replace = getReplacementStack(stack, false);
                            if (!replace.isEmpty() && !replace.isItemEqual(stack)) {
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

    private ItemStack getReplacementStack(ItemStack stack, boolean duplication) {
        ItemStack replace = LevelUpConfig.useOreChunks ? Library.getChunkFromName(Library.getOreNameForBlock(stack)) : getDupeStack(stack, duplication);
        if (LevelUpConfig.useOreChunks && replace.isEmpty())
            replace = getDupeStack(stack, duplication);
        return replace;
    }

    private ItemStack getDupeStack(ItemStack stack, boolean duplication) {
        if (Library.isOre(stack)) {
            ItemStack dupe = stack.copy();
            dupe.grow(stack.getCount());
            if (duplication)
                attachNoPlacement(dupe);
            return dupe;
        }
        return ItemStack.EMPTY;
    }

    private void attachNoPlacement(ItemStack stack) {
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        if (tag != null) {
            tag.setBoolean("NoPlace", true);
            stack.setTagCompound(tag);
        }
    }
}
