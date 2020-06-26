package levelup2.gui.classselect;

import levelup2.api.ICharacterClass;
import levelup2.skills.SkillRegistry;
import levelup2.util.ClassProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClassSelectionEntry implements GuiListExtended.IGuiListEntry {
    private final GuiListClassSelection container;
    private final Minecraft mc;
    private final ICharacterClass charClass;

    public ClassSelectionEntry(GuiListClassSelection parent, Minecraft client, ICharacterClass cl) {
        container = parent;
        mc = client;
        charClass = cl;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        ClassProperties prop = SkillRegistry.getProperty(charClass);
        String className = !prop.getLocalizedName().equals("") ? prop.getLocalizedName() : I18n.format(charClass.getUnlocalizedName());
        ItemStack stack = charClass.getRepresentativeStack();
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemIntoGUI(stack, x + 4, y + 4);
            GlStateManager.popMatrix();
        }
        mc.fontRenderer.drawString(className, x + 23, y + 8, 8421504);
    }

    /**
     * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
     * clicked and the list should not be dragged.
     */
    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        container.selectClass(slotIndex);
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

    @Override
    public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {}

    public ICharacterClass getCharacterClass() {
        return charClass;
    }
}
