package levelup2.proxy;

import levelup2.config.LevelUpConfig;
import levelup2.event.KeybindEventHandler;
import levelup2.gui.GuiSpecialization;
import levelup2.skills.SkillRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerGui() {
        MinecraftForge.EVENT_BUS.register(KeybindEventHandler.INSTANCE);
    }

    @Override
    public EntityPlayer getPlayer() {
        return FMLClientHandler.instance().getClient().player;
    }

    @Override
    public void registerItemMeshes() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void openSpecializationGui() {
        Minecraft.getMinecraft().displayGuiScreen(GuiSpecialization.withRespec());
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent evt) {
        setModelLocation(SkillRegistry.surfaceOreChunk, "inventory");
        setModelLocation(SkillRegistry.netherOreChunk, "inventory");
        setModelLocation(SkillRegistry.endOreChunk, "inventory");
        setModelLocation(SkillRegistry.respecBook, "inventory");
    }

    private void setModelLocation(Item item, String variantSettings) {
        ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(item.getRegistryName(), variantSettings));
        ModelLoader.registerItemVariants(item, item.getRegistryName());
    }

    @Override
    public void registerColors() {
        final ItemColors color = Minecraft.getMinecraft().getItemColors();
        color.registerItemColorHandler((stack, tintIndex) -> tintIndex == 1 && stack.getMetadata() < LevelUpConfig.oreColors.size() ? LevelUpConfig.oreColors.get(stack.getMetadata()) : -1, SkillRegistry.surfaceOreChunk);
        color.registerItemColorHandler((stack, tintIndex) -> tintIndex == 1 && stack.getMetadata() < LevelUpConfig.netherOreColors.size() ? LevelUpConfig.netherOreColors.get(stack.getMetadata()) : -1, SkillRegistry.netherOreChunk);
        color.registerItemColorHandler((stack, tintIndex) -> tintIndex == 1 && stack.getMetadata() < LevelUpConfig.endOreColors.size() ? LevelUpConfig.endOreColors.get(stack.getMetadata()) : -1, SkillRegistry.endOreChunk);
    }
}
