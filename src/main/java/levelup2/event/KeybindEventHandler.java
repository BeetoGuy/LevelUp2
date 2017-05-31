package levelup2.event;

import levelup2.LevelUp2;
import levelup2.gui.GuiSkills;
import levelup2.gui.GuiSpecialization;
import levelup2.skills.SkillRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindEventHandler {
    public static final KeybindEventHandler INSTANCE = new KeybindEventHandler();
    private final KeyBinding keybind = new KeyBinding("LevelUpGUI", Keyboard.KEY_L, "key.categories.gui");

    private KeybindEventHandler() {
        ClientRegistry.registerKeyBinding(keybind);
    }

    @SubscribeEvent
    public void openGui(InputEvent.KeyInputEvent evt) {
        if (keybind.isKeyDown() && Minecraft.getMinecraft().currentScreen == null && Minecraft.getMinecraft().player != null) {
            if (!SkillRegistry.getPlayer(LevelUp2.proxy.getPlayer()).hasClass() && LevelUp2.proxy.getPlayer().experienceLevel > 4)
                Minecraft.getMinecraft().displayGuiScreen(new GuiSpecialization());
            else if (SkillRegistry.getPlayer(LevelUp2.proxy.getPlayer()).hasClass())
                Minecraft.getMinecraft().displayGuiScreen(new GuiSkills());
            else
                Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentTranslation("level.invalid"), true);
        }
    }
}
