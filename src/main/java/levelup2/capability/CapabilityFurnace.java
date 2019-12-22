package levelup2.capability;

import levelup2.api.IPlayerSkill;
import levelup2.config.LevelUpConfig;
import levelup2.skills.SkillRegistry;
import levelup2.util.SmeltingBlacklist;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CapabilityFurnace extends PlayerCapability.CapabilityProcessorDefault {
    private static final ResourceLocation FURNACESPEED = new ResourceLocation("levelup", "furnacespeed");
    private static final ResourceLocation FURNACEBONUS = new ResourceLocation("levelup", "furnacebonus");
    public CapabilityFurnace(TileEntityFurnace tile) {
        super(tile);
    }

    @Override
    public void extraProcessing(EntityPlayer player) {
        if (tile != null) {
            TileEntityFurnace furnace = (TileEntityFurnace)tile;
            if (furnace.isBurning()) {
                if (furnace.canSmelt()) {
                    ItemStack stack = furnace.getStackInSlot(0);
                    if (!stack.isEmpty()) {
                        int bonus = SkillRegistry.getSkillLevel(player, FURNACESPEED);
                        if (bonus > 0 || !isSkillActive("levelup:furnacespeed")) {
                            int time = player.getRNG().nextInt(bonus + 1);
                            if (isSkillActive("levelup:furnacespeed") && time > 0 && furnace.getField(2) + time < furnace.getField(3)) {
                                furnace.setField(2, furnace.getField(2) + time);
                            }
                            if (furnace.getField(2) > furnace.getField(3) - 2 && isSkillActive("levelup:furnacebonus")) {
                                bonus = SkillRegistry.getSkillLevel(player, FURNACEBONUS);
                                if (bonus > 0) {
                                    if (isDoublingValid(furnace) && player.getRNG().nextFloat() < bonus / 40F) {
                                        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack).copy();
                                        if (!LevelUpConfig.furnaceEjection) {
                                            if (furnace.getStackInSlot(2).isEmpty()) {
                                                furnace.setInventorySlotContents(2, result);
                                            } else {
                                                ItemStack product = furnace.getStackInSlot(2);
                                                if (ItemStack.areItemsEqual(result, product)) {
                                                    if (product.getCount() + (result.getCount() * 2) <= product.getMaxStackSize()) {
                                                        furnace.getStackInSlot(2).grow(result.getCount());
                                                    }
                                                }
                                            }
                                        } else
                                            ejectExtraItem(result);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isDoublingValid(TileEntityFurnace tile) {
        ItemStack smeltingItem = tile.getStackInSlot(0);
        return !FurnaceRecipes.instance().getSmeltingResult(smeltingItem).isEmpty() && !SmeltingBlacklist.contains(smeltingItem);
    }

    private void ejectExtraItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (tile.getBlockType() == Blocks.FURNACE || tile.getBlockType() == Blocks.LIT_FURNACE) {
                IBlockState furnace = tile.getWorld().getBlockState(tile.getPos());
                EnumFacing facing = furnace.getValue(BlockFurnace.FACING);
                BlockPos offset = tile.getPos().offset(facing);
                EntityItem item = new EntityItem(tile.getWorld(), offset.getX() + 0.5D, offset.getY() + 0.5D, offset.getZ() + 0.5D, stack);
                tile.getWorld().spawnEntity(item);
            }
        }
    }

    private boolean isSkillActive(String skill) {
        IPlayerSkill sk = SkillRegistry.getSkillFromName(new ResourceLocation(skill));
        if (sk != null) {
            return sk.isActive() && sk.isEnabled();
        }
        return false;
    }
}
