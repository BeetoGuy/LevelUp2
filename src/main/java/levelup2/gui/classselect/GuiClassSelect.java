package levelup2.gui.classselect;

import levelup2.api.ICharacterClass;
import levelup2.network.SkillPacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.io.IOException;

public class GuiClassSelect extends GuiScreen {
    private String title = "Select Class";
    private GuiListClassSelection selectionList;
    private GuiButton select;
    private boolean closedWithChoice = false;
    private boolean isReclass = false;

    public static GuiClassSelect withReclass() {
        GuiClassSelect cl = new GuiClassSelect();
        cl.isReclass = true;
        return cl;
    }

    @Override
    public void initGui() {
        this.title = I18n.format("levelup.class.select");
        this.selectionList = new GuiListClassSelection(this, this.mc, this.width, this.height, 32, this.height - 64, 24);
        select = addButton(new GuiButton(0, this.width / 2 - 154, this.height - 52, 150, 20, I18n.format("gui.levelup.confirm")));
        addButton(new GuiButton(1, this.width / 2 + 4, this.height - 52, 150, 20, I18n.format("gui.cancel")));
        select.enabled = false;
    }

    @Override
    public void onGuiClosed() {
        if (closedWithChoice && selectionList.getSelectedClass() != null) {
            FMLProxyPacket pkt = SkillPacketHandler.getClassChangePacket(selectionList.getSelectedClass().getCharacterClass().getClassName(), isReclass);
            SkillPacketHandler.classChannel.sendToServer(pkt);
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id == 0) {
                closedWithChoice = true;
            }
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        selectionList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, title, width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (selectionList.getSelectedClass() != null) {
            ICharacterClass cl = selectionList.getSelectedClass().getCharacterClass();
            this.drawCenteredString(this.fontRenderer, !cl.getLocalizedName().equals("") ? cl.getLocalizedName() : I18n.format(cl.getUnlocalizedName()), width / 2, height - 32, 16777215);
            this.drawCenteredString(this.fontRenderer, !cl.getLocalizedDescription().equals("") ? cl.getLocalizedDescription() : I18n.format(cl.getUnlocalizedDescription()), width / 2, height - 20, 16777215);
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.selectionList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Called when a mouse button is released.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.selectionList.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.selectionList.handleMouseInput();
    }

    public void selectClass(ClassSelectionEntry entry) {
        select.enabled = entry != null;
    }
}
