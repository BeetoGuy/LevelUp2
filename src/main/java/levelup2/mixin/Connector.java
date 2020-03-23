package levelup2.mixin;

import levelup2.LevelUp2;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class Connector implements IMixinConnector {

    @Override
    public void connect() {
        LevelUp2.LOGGER.info("Loading levelup2 Mixins...");
        Mixins.addConfiguration("levelup2.mixins.json");
    }
}
