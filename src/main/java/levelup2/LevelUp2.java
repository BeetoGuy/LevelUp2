package levelup2;

import levelup2.api.IProcessor;
import levelup2.capability.PlayerCapability;
import levelup2.config.LevelUpConfig;
import levelup2.event.CapabilityEventHandler;
import levelup2.network.SkillPacketHandler;
import levelup2.player.IPlayerClass;
import levelup2.player.PlayerExtension;
import levelup2.proxy.CommonProxy;
import levelup2.skills.SkillRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = LevelUp2.ID, name = "Level Up! Reloaded", version = "${version}")
public class LevelUp2 {
    public static final String ID = "levelup2";
    @Mod.Instance(value = ID)
    public static LevelUp2 INSTANCE;
    @SidedProxy(clientSide = "levelup2.proxy.ClientProxy", serverSide = "levelup2.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        LevelUpConfig.init(evt.getSuggestedConfigurationFile());
        CapabilityManager.INSTANCE.register(IPlayerClass.class, new PlayerCapability.CapabilityPlayerClass<>(), PlayerExtension.class);
        CapabilityManager.INSTANCE.register(IProcessor.class, new PlayerCapability.CapabilityProcessorClass<>(), PlayerCapability.CapabilityProcessorDefault.class);
        MinecraftForge.EVENT_BUS.register(new CapabilityEventHandler());
        SkillRegistry.initItems();
        proxy.registerItemMeshes();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        proxy.registerGui();
        SkillPacketHandler.init();
        SkillRegistry.loadSkills();
        proxy.registerColors();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        SkillRegistry.postLoadSkills();
        SkillRegistry.registerRecipes();
    }
}
