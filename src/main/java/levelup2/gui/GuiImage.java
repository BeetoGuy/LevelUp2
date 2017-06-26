package levelup2.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiImage extends GuiButton {
    private static ResourceLocation BUTTON_IMAGE;
    private ItemStack repStack;
    private String type;

    public GuiImage(int buttonID, int x, int y, int width, int height, String type) {
        super(buttonID, x, y, width, height, "");
        BUTTON_IMAGE = new ResourceLocation("levelup2", "textures/gui/button.png");
        this.type = type;
        repStack = type.equals("mining") ? new ItemStack(Items.DIAMOND_PICKAXE) : type.equals("craft") ? new ItemStack(Blocks.CRAFTING_TABLE) : new ItemStack(Items.DIAMOND_SWORD);
    }

    @Override
    protected int getHoverState(boolean mouseOver) {
        int i = 1;
        if (!this.enabled)
            i = 0;
        else if (mouseOver)
            i = 2;
        return i;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(BUTTON_IMAGE);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (i > 0)
                this.drawTexturedModalRect(this.x, this.y, (i - 1) * this.width, 0, this.width, this.height);
            else
                this.drawTexturedModalRect(this.x, this.y, this.width * 2, 0, this.width, this.height);
            mc.getRenderItem().renderItemIntoGUI(repStack, this.x + (this.width / 2) - 8, this.y + (this.height / 2) - 8);
            String locName = I18n.format("skill.levelup:" + type + "_bonus.short");
            this.drawCenteredString(mc.fontRenderer, locName, this.x + this.width / 2, this.y + 20, this.hovered ? 0xFBFD6F : 0xF9F9F9);
            this.drawCenteredString(mc.fontRenderer, I18n.format("skill.levelup:" + type + ".desc"), this.x + this.width / 2, this.y + this.height - 20, this.hovered ? 0xFBFD6F : 0xF9F9F9);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }
}
