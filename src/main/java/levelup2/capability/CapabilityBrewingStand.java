package levelup2.capability;

import levelup2.skills.SkillRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.ResourceLocation;

public class CapabilityBrewingStand extends PlayerCapability.CapabilityProcessorDefault {
    private static final ResourceLocation BREWING = new ResourceLocation("levelup", "brewingspeed");
    public CapabilityBrewingStand(TileEntityBrewingStand stand) {
        super(stand);
    }

    @Override
    public void extraProcessing(EntityPlayer player) {
        if (SkillRegistry.getSkillFromName(BREWING) == null || !SkillRegistry.getSkillFromName(BREWING).isActive() || !SkillRegistry.getSkillFromName(BREWING).isEnabled()) return;
        if (tile != null) {
            if (tile instanceof TileEntityBrewingStand) {
                TileEntityBrewingStand stand = (TileEntityBrewingStand) tile;
                if (stand.getField(0) > 0) {
                    int bonus = SkillRegistry.getSkillLevel(player, BREWING);
                    if (bonus > 0) {
                        int time = player.getRNG().nextInt(bonus + 1);
                        if (time > 0 && stand.getField(0) - time > 0)
                            stand.setField(0, stand.getField(0) - time);
                    }
                }
            }
        }
    }
}
