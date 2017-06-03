package levelup2.skills.crafting;

import levelup2.api.IPlayerSkill;
import levelup2.skills.BaseSkill;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FoodGrowthBonus extends BaseSkill {

    @Override
    public String getSkillName() {
        return "levelup:cropgrowth";
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return Library.tenLevels[currentLevel];
        return -1;
    }

    @Override
    public int getMaxLevel() {
        return 10;
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
    public byte getSkillType() {
        return 1;
    }

    @Override
    public String[] getPrerequisites() {
        return new String[0];
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return new ItemStack(Items.IRON_HOE);
    }

    @Override
    public boolean hasSubscription() {
        return true;
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START) {
            EntityPlayer player = evt.player;
            if (player != null) {
                int skillLevel = SkillRegistry.getSkillLevel(player, getSkillName());
                if (!player.world.isRemote && skillLevel > 0 && player.getRNG().nextFloat() <= skillLevel / 500F) {
                    growCropsAround(player.world, skillLevel, player);
                }
            }
        }
    }

    private void growCropsAround(World world, int range, EntityPlayer player) {
        int posX = (int)player.posX;
        int posY = (int)player.posY;
        int posZ = (int)player.posZ;
        int dist = range / 2 + 2;
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(posX - dist, posY - dist, posZ - dist), new BlockPos(posX + dist + 1, posY + dist + 1, posZ + dist + 1))) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof IPlantable && !SkillRegistry.getCropBlacklist().contains(block)) {
                world.scheduleUpdate(pos, block, block.tickRate(world));
            }
        }
    }
}
