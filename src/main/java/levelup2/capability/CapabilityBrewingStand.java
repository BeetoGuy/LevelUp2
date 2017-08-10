package levelup2.capability;

import levelup2.skills.SkillRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityBrewingStand;

public class CapabilityBrewingStand extends PlayerCapability.CapabilityProcessorDefault {
    public CapabilityBrewingStand(TileEntityBrewingStand stand) {
        super(stand);
    }

    @Override
    public void extraProcessing(EntityPlayer player) {
        if (tile != null) {
            if (tile instanceof TileEntityBrewingStand) {
                TileEntityBrewingStand stand = (TileEntityBrewingStand) tile;
                if (stand.getField(0) > 0) {
                    int bonus = SkillRegistry.getSkillLevel(player, "levelup:brewingspeed");
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
