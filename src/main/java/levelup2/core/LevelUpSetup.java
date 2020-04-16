package levelup2.core;

import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class LevelUpSetup implements IFMLCallHook {
    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public Void call() {
        if (LevelUpCore.isFastFurnacePresent)
            Mixins.addConfiguration("lu2ff.mixins.json");
        return null;
    }
}
