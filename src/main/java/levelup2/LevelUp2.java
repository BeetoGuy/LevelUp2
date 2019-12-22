package levelup2;

import levelup2.api.IProcessor;
import levelup2.capability.PlayerCapability;
import levelup2.config.LevelUpConfig;
import levelup2.config.OreChunkStorage;
import levelup2.event.CapabilityEventHandler;
import levelup2.network.SkillPacketHandler;
import levelup2.player.IPlayerClass;
import levelup2.player.PlayerExtension;
import levelup2.proxy.CommonProxy;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

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
        LevelUpConfig.getBlacklistOutputs();
        SkillRegistry.postLoadSkills();
        LevelUpConfig.registerSkillProperties();
        SkillRegistry.registerRecipes();
    }

    @Mod.EventBusSubscriber(modid = LevelUp2.ID)
    public static class RegistryEventHandler {
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> evt) {
            IForgeRegistry<Item> reg = evt.getRegistry();
            registerItem(reg, SkillRegistry.surfaceOreChunk);
            registerItem(reg, SkillRegistry.netherOreChunk);
            registerItem(reg, SkillRegistry.endOreChunk);
            registerItem(reg, SkillRegistry.respecBook);
            registerItem(reg, SkillRegistry.skillBook);
        }

        @SubscribeEvent
        public static void registerRecipes(RegistryEvent.Register<IRecipe> evt) {
            IForgeRegistry<IRecipe> reg = evt.getRegistry();
            reg.register(new ShapelessOreRecipe(new ResourceLocation("levelup2", "reclaim"), new ItemStack(Blocks.GRAVEL, 4), Items.FLINT, Items.FLINT, Items.FLINT, Items.FLINT).setRegistryName(new ResourceLocation("levelup2", "gravel")));
            oreLoad(reg);
            SkillRegistry.initPlankCache();
        }

        private static void oreLoad(IForgeRegistry<IRecipe> reg) {
            if (!Library.ALL_ORES.isEmpty()) {
                for (OreChunkStorage stor : Library.ALL_ORES) {
                    registerOreRecipe(reg, stor);
                }
            }
        }

        private static void registerItem(IForgeRegistry<Item> reg, Item item) {
            reg.register(item);
        }

        private static void registerOreRecipe(IForgeRegistry<IRecipe> reg, OreChunkStorage stor) {
            String oreName = stor.getOreName();
            if (OreDictionary.doesOreNameExist(oreName)) {
                ItemStack ore = SkillRegistry.getOreEntry(oreName);
                ItemStack chunk = new ItemStack(stor.getBaseItem(), 1, stor.getMetadata());
                if (!ore.isEmpty()) {
                    reg.register(new ShapelessOreRecipe(new ResourceLocation("levelup2", "orechunk"), ore.copy(), chunk, chunk).setRegistryName("levelup2", oreName.toLowerCase()));
                }
                OreDictionary.registerOre(oreName, chunk);
            }
        }
    }
}
