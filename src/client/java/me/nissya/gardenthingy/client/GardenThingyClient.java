package me.nissya.gardenthingy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;


public class GardenThingyClient implements ClientModInitializer {
    private static KeyBinding keyBinding;
    private static boolean swapped = false;
    private static InputUtil.Key originalJumpKey;
    private static InputUtil.Key originalAttackKey;
    private static Double originalMouseSensitivity;
    private static boolean isOriginalSet = false;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.gardenThingy.swapControls",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.gardenThingy"
        ));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (swapped) {
                client.options.jumpKey.setBoundKey(originalJumpKey);
                client.options.attackKey.setBoundKey(originalAttackKey);
                swapped = false;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.options != null && !isOriginalSet) {
                originalJumpKey = client.options.jumpKey.getDefaultKey();
                originalAttackKey = client.options.attackKey.getDefaultKey();
                originalMouseSensitivity = client.options.getMouseSensitivity().getValue();
                isOriginalSet = true;
            }
            if (keyBinding.wasPressed()) {
                swapped = !swapped;

                if (swapped && client.options != null) {
                    client.options.jumpKey.setBoundKey(InputUtil.Type.MOUSE.createFromCode(0));
                    client.options.attackKey.setBoundKey(InputUtil.fromKeyCode(GLFW.GLFW_KEY_SPACE, 0));
                    if (client.player != null) {
                        client.player.sendMessage(Text.translatable("message.gardenThingy.controls_swapped"), true);
                    }
                    MinecraftClient.getInstance().options.getMouseSensitivity().setValue(0.1);
                    MinecraftClient.getInstance().options.write();
                } else if (client.options != null) {
                    client.options.jumpKey.setBoundKey(originalJumpKey);
                    client.options.attackKey.setBoundKey(originalAttackKey);
                    if (client.player != null) {
                        client.player.sendMessage(Text.translatable("message.gardenThingy.controls_restored"), true);
                    }
                    MinecraftClient.getInstance().options.getMouseSensitivity().setValue(originalMouseSensitivity);
                    MinecraftClient.getInstance().options.write();
                }
                if (client.options != null) {
                    client.options.write();
                }
                KeyBinding.updateKeysByCode();
            }
        });
    }


}
