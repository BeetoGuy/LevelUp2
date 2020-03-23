package levelup2.mixin;

import levelup2.api.IProcessor;
import levelup2.capability.PlayerCapability;
import levelup2.skills.SkillRegistry;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityBrewingStand.class)
public abstract class MixinBrewingStand implements ICapabilityProvider {
    @Shadow private int brewTime;

    private static final ResourceLocation BREWING = new ResourceLocation("levelup", "brewingspeed");

    @Inject(method = "update", at = @At("HEAD"))
    private void levelUpBrewingStandUpdate(CallbackInfo ci) {
        if (brewTime > 1 && getCapability() != null && getCapability().getPlayerFromUUID() != null) {
            int bonus = SkillRegistry.getSkillLevel(getCapability().getPlayerFromUUID(), BREWING);
            if (bonus > 0) {
                int reduce = getCapability().getPlayerFromUUID().getRNG().nextInt(bonus + 1);
                if (reduce > 0) {
                    brewTime = Math.max(1, brewTime - reduce);
                }
            }
        }
    }

    private IProcessor getCapability() {
        return hasCapability(PlayerCapability.MACHINE_PROCESSING, EnumFacing.UP) ? getCapability(PlayerCapability.MACHINE_PROCESSING, EnumFacing.UP) : null;
    }
}
