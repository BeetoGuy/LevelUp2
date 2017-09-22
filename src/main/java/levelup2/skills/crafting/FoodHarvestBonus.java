package levelup2.skills.crafting;

import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockMelon;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class FoodHarvestBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:harvestbonus";
    }

    @Override
    public byte getSkillType() {
        return 1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.WHEAT);
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBroken(BlockEvent.BreakEvent evt) {
        if (!isActive()) return;
        if (!evt.getWorld().isRemote && evt.getPlayer() != null) {
            if (evt.getState().getBlock() instanceof BlockCrops || evt.getState().getBlock() instanceof BlockStem) {
                if (!((IGrowable)evt.getState().getBlock()).canGrow(evt.getWorld(), evt.getPos(), evt.getState(), false)) {
                    doCropDrops(evt);
                }
            }
            else if (evt.getState().getBlock() instanceof BlockMelon) {
                doCropDrops(evt);
            }
        }
    }

    private void doCropDrops(BlockEvent.BreakEvent evt) {
        Random rand = evt.getPlayer().getRNG();
        int skill = SkillRegistry.getSkillLevel(evt.getPlayer(), getSkillName());
        if (skill > 0) {
            if (rand.nextInt(10) < skill) {
                Item item = evt.getState().getBlock().getItemDropped(evt.getState(), rand, 0);
                if (item == Items.AIR || item == null) {
                    if (evt.getState().getBlock() == Blocks.PUMPKIN_STEM)
                        item = Items.PUMPKIN_SEEDS;
                    else if (evt.getState().getBlock() == Blocks.MELON_STEM)
                        item = Items.MELON_SEEDS;
                }
                if (item != Items.AIR && item != null) {
                    evt.getWorld().spawnEntity(new EntityItem(evt.getWorld(), evt.getPos().getX(), evt.getPos().getY(), evt.getPos().getZ(), new ItemStack(item, Math.max(1, evt.getState().getBlock().quantityDropped(evt.getState(), 0, rand)), evt.getState().getBlock().damageDropped(evt.getState()))));
                }
            }
        }
    }
}
