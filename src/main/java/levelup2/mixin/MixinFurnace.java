package levelup2.mixin;

import levelup2.api.IProcessor;
import levelup2.capability.PlayerCapability;
import levelup2.skills.SkillRegistry;
import levelup2.util.SmeltingBlacklist;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityFurnace.class)
public abstract class MixinFurnace implements ICapabilityProvider {
    private static final ResourceLocation FURNACESPEED = new ResourceLocation("levelup", "furnacespeed");
    private static final ResourceLocation FURNACEBONUS = new ResourceLocation("levelup", "furnacebonus");

    @Shadow public abstract boolean canSmelt();

    @Shadow public abstract ItemStack getStackInSlot(int index);

    @Shadow public abstract void setInventorySlotContents(int index, ItemStack stack);

    @Inject(method = "getCookTime", at = @At("RETURN"), cancellable = true)
    private void levelUpGetCookTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        IProcessor cap = getCapability();
        if (cap != null) {
            int cookTime = 200;
            if (cap.getPlayerFromUUID() != null) {
                int skillLevel = SkillRegistry.getSkillLevel(cap.getPlayerFromUUID(), FURNACESPEED);
                if (skillLevel > 0) {
                    int cookMinus = Math.min(skillLevel * 20, 180);
                    cir.setReturnValue(cookTime - cookMinus);
                }
            }
        }
    }

    @Inject(method = "smeltItem", at = @At("HEAD"))
    private void levelUpSmeltItem(CallbackInfo ci) {
        IProcessor cap = getCapability();
        if (canSmelt() && isDoublingValid() && cap != null && cap.getPlayerFromUUID() != null) {
            EntityPlayer player = cap.getPlayerFromUUID();
            int doubleChance = SkillRegistry.getSkillLevel(player, FURNACEBONUS);
            if (doubleChance > 0 && player.getRNG().nextFloat() < doubleChance / 40F) {
                ItemStack smeltingItem = FurnaceRecipes.instance().getSmeltingResult(getStackInSlot(0));
                ItemStack resultSlot = getStackInSlot(2);

                if (resultSlot.isEmpty()) {
                    setInventorySlotContents(2, smeltingItem.copy());
                } else if (resultSlot.getCount() + (smeltingItem.getCount() * 2) <= resultSlot.getMaxStackSize()) {
                    resultSlot.grow(smeltingItem.getCount());
                }
            }
        }
    }

    private boolean isDoublingValid() {
        ItemStack smeltingItem = getStackInSlot(0);
        return !smeltingItem.isEmpty() && !SmeltingBlacklist.contains(smeltingItem);
    }

    private IProcessor getCapability() {
        return hasCapability(PlayerCapability.MACHINE_PROCESSING, EnumFacing.UP) ? getCapability(PlayerCapability.MACHINE_PROCESSING, EnumFacing.UP) : null;
    }
}
