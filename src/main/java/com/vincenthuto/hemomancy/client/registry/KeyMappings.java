package com.vincenthuto.hemomancy.client.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

/**
 * Hemomancy key mappings and their registration.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class KeyMappings {

    private KeyMappings() {
    }


    public static final KeyMapping bloodFormation = new KeyMapping("key.hemomancy.bloodformation.desc", GLFW.GLFW_KEY_J,
            "key.hemomancy.category");
    public static final KeyMapping bloodCrafting = new KeyMapping("key.hemomancy.bloodcrafting.desc", GLFW.GLFW_KEY_K,
            "key.hemomancy.category");
    public static final KeyMapping bloodDraw = new KeyMapping("key.hemomancy.drawtest.desc", GLFW.GLFW_KEY_LEFT_ALT,
            "key.hemomancy.category");
    public static final KeyMapping cycleSelectedManip = new KeyMapping("key.hemomancy.cyclemanip.desc", GLFW.GLFW_KEY_C,
            "key.hemomancy.category");
    public static final KeyMapping useQuickManip = new KeyMapping("key.hemomancy.quickusemanip.desc", GLFW.GLFW_KEY_R,
            "key.hemomancy.category");
    public static final KeyMapping useContManip = new KeyMapping("key.hemomancy.contusemanip.desc", GLFW.GLFW_KEY_G,
            "key.hemomancy.category");
    public static final KeyMapping useManip = new KeyMapping("key.hemomancy.usemanip.desc", GLFW.GLFW_KEY_R,
            "key.hemomancy.category");
    public static final KeyMapping useStillArt = new KeyMapping("key.hemomancy.usestillart.desc", GLFW.GLFW_KEY_R,
            "key.hemomancy.category");
    public static final KeyMapping selectStillArt = new KeyMapping("key.hemomancy.selectstillart.desc", GLFW.GLFW_KEY_Z,
            "key.hemomancy.category");
    public static final KeyMapping OPEN_CHARM_SLOT_KEYBIND = new KeyMapping("key.charm_slot.slot", GLFW.GLFW_KEY_B,
            "key.hemomancy.category");
    public static final KeyMapping openVascCharmMenu = new KeyMapping("key.charm_slot.open", GLFW.GLFW_KEY_X,
            "key.hemomancy.category");
    public static final KeyMapping toggleGourd = new KeyMapping("key.hemomancy.togglegourd.desc", GLFW.GLFW_KEY_H,
            "key.hemomancy.category");
    public static final KeyMapping openMorphlingJarViewer = new KeyMapping("key.hemomancy.openmorphlingjar.desc",
            GLFW.GLFW_KEY_N, "key.hemomancy.category");
    public static final KeyMapping bloodBallDrop = new KeyMapping("key.hemomancy.bloodballdrop.desc", GLFW.GLFW_KEY_V,
            "key.hemomancy.category");

    public static boolean isKeyDown(KeyMapping keybind) {
        if (keybind.isUnbound())
            return false;

        boolean isDown = switch (keybind.getKey().getType()) {
            case KEYSYM ->
                    InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(),
                    keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
            default -> false;
        };
        return isDown && keybind.getKeyConflictContext().isActive()
                && keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(bloodFormation);
        event.register(bloodCrafting);
        event.register(bloodDraw);
        event.register(cycleSelectedManip);
        event.register(useManip);
        event.register(useStillArt);
        event.register(selectStillArt);
        event.register(OPEN_CHARM_SLOT_KEYBIND);
        event.register(openVascCharmMenu);
        event.register(toggleGourd);
        event.register(openMorphlingJarViewer);
        event.register(bloodBallDrop);

    }
}
