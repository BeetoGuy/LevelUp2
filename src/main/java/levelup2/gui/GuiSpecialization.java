package levelup2.gui;

import levelup2.network.SkillPacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

public class GuiSpecialization extends GuiScreen {
    private boolean closedWithButton = false;
    private byte spec = -1;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        closedWithButton = false;
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 + 96, height / 6 + 168, 96, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(4, width / 2 - 192, height / 6 + 168, 96, 20, I18n.format("gui.cancel")));
        String[] specs = {"mining", "craft", "combat"};
        for (int i = 1; i < 4; i++) {
            buttonList.add(new GuiImage(i, width / 2 - 170 + (i - 1) * 120, 50, 96, 120, specs[i - 1]));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        if (closedWithButton && spec != -1) {
            FMLProxyPacket pkt = SkillPacketHandler.getPacket(Side.SERVER, 1, spec);
            SkillPacketHandler.classChannel.sendToServer(pkt);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            closedWithButton = true;
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (button.id == 4) {
            closedWithButton = false;
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else {
            spec = (byte)(button.id - 1);
        }
    }
}
