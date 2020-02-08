package levelup2.event;

import levelup2.LevelUp2;
import levelup2.network.SkillPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindEventHandler {
    public static final KeybindEventHandler INSTANCE = new KeybindEventHandler();
    private final KeyBinding keybind = new KeyBinding("LevelUpToggle", Keyboard.KEY_L, "key.categories.gui");
    private final KeyBinding skillPane = new KeyBinding("LevelUpSkills", Keyboard.KEY_B, "key.categories.gui");

    private KeybindEventHandler() {
        ClientRegistry.registerKeyBinding(keybind);
        ClientRegistry.registerKeyBinding(skillPane);
    }

    @SubscribeEvent
    public void openGui(InputEvent.KeyInputEvent evt) {
        //TODO: Create activate/deactivate packet.
        if (keybind.isKeyDown() && Minecraft.getMinecraft().currentScreen == null) {
            SkillPacketHandler.toggleChannel.sendToServer(SkillPacketHandler.getActivationPacket());
        } else if (skillPane.isKeyDown() && Minecraft.getMinecraft().currentScreen == null) {
            LevelUp2.proxy.displayGuiForPlayer(LevelUp2.proxy.getPlayer());
        }
    }
}
