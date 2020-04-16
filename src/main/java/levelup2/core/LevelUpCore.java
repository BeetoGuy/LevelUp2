package levelup2.core;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(-5001)
public class LevelUpCore implements IFMLLoadingPlugin {
    public static boolean isFastFurnacePresent = false;

    public LevelUpCore() {
        MixinBootstrap.init();
        Mixins.addConfiguration("levelup2.mixins.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return LevelUpSetup.class.getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {
        File modFolder = new File("mods");
        File[] found = modFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("FastFurnace") && name.endsWith(".jar");
            }
        });
        if (found != null && found.length > 0) {
            try {
                File ff = found[0];
                Launch.classLoader.addURL(ff.toURI().toURL());
                CoreModManager.getReparseableCoremods().add(ff.getName());
                isFastFurnacePresent = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
