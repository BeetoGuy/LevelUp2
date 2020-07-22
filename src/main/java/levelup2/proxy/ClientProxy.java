package levelup2.proxy;

import levelup2.event.KeybindEventHandler;
import levelup2.gui.GuiSkills;
import levelup2.gui.classselect.GuiClassSelect;
import levelup2.skills.SkillRegistry;
import levelup2.util.Library;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void displayGuiForPlayer(EntityPlayer player) {
        if (!SkillRegistry.getPlayer(player).hasClass())
            Minecraft.getMinecraft().displayGuiScreen(new GuiClassSelect());
        else
            Minecraft.getMinecraft().displayGuiScreen(new GuiSkills());
    }

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
        Minecraft.getMinecraft().displayGuiScreen(GuiClassSelect.withReclass());
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent evt) {
        setModelLocation(SkillRegistry.surfaceOreChunk, "inventory");
        setModelLocation(SkillRegistry.netherOreChunk, "inventory");
        setModelLocation(SkillRegistry.endOreChunk, "inventory");
        setModelLocation(SkillRegistry.respecBook, "inventory");
        setModelLocation(SkillRegistry.skillBook, "inventory");
    }

    private void setModelLocation(Item item, String variantSettings) {
        ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(item.getRegistryName(), variantSettings));
        ModelLoader.registerItemVariants(item, item.getRegistryName());
    }

    @Override
    public void registerColors() {
        final ItemColors color = Minecraft.getMinecraft().getItemColors();
        color.registerItemColorHandler((stack, tintIndex) -> tintIndex == 1 && !Library.SURFACE_ORES.isEmpty() && stack.getMetadata() < Library.SURFACE_ORES.size() ? Library.SURFACE_ORES.get(stack.getMetadata()).getColor() : -1, SkillRegistry.surfaceOreChunk);
        color.registerItemColorHandler((stack, tintIndex) -> tintIndex == 1 && !Library.NETHER_ORES.isEmpty() && stack.getMetadata() < Library.NETHER_ORES.size() ? Library.NETHER_ORES.get(stack.getMetadata()).getColor() : -1, SkillRegistry.netherOreChunk);
        color.registerItemColorHandler((stack, tintIndex) -> tintIndex == 1 && !Library.END_ORES.isEmpty() && stack.getMetadata() < Library.END_ORES.size() ? Library.END_ORES.get(stack.getMetadata()).getColor() : -1, SkillRegistry.endOreChunk);
    }
}
