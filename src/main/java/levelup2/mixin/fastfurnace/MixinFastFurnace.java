package levelup2.mixin.fastfurnace;

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
import shadows.fastfurnace.block.TileFastFurnace;

@Mixin(TileFastFurnace.class)
public abstract class MixinFastFurnace extends TileEntityFurnace implements ICapabilityProvider {
    private static final ResourceLocation FURNACEBONUS = new ResourceLocation("levelup", "furnacebonus");

    @Shadow(remap = false) protected ItemStack recipeKey;

    @Shadow(remap = false) abstract boolean itemsMatch(ItemStack a, ItemStack b);

    @Inject(method = "smeltItem", at = @At("HEAD"))
    private void levelUpSmeltItem(CallbackInfo ci) {
        IProcessor cap = getCapability();
        if (isDoublingValid() && cap != null && cap.getPlayerFromUUID() != null) {
            EntityPlayer player = cap.getPlayerFromUUID();
            int doubleChance = SkillRegistry.getSkillLevel(player, FURNACEBONUS);
            if (doubleChance > 0 && player.getRNG().nextFloat() < doubleChance / 40F) {
                ItemStack input = this.getStackInSlot(0);
                ItemStack recipeOutput = FurnaceRecipes.instance().getSmeltingList().get(recipeKey);
                ItemStack output = this.getStackInSlot(2);
                if (output.isEmpty()) {
                    this.setInventorySlotContents(2, recipeOutput.copy());
                } else if (this.itemsMatch(output, recipeOutput)) {
                    output.grow(recipeOutput.getCount());
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
