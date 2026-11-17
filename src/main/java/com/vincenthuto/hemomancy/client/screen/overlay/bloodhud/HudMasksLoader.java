package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;

/** Reads every HUD part's silhouette from its texture, the same way {@code HudMasksFixture} does in tests. */
public final class HudMasksLoader {
    private static final int MIN_ALPHA = 21;

    private HudMasksLoader() {
    }

    public static HudMasks load(ResourceManager resources) {
        int[] back = argb(resources, "blood_overlay/vessel_back");
        int[] frame = argb(resources, "blood_overlay/base_degree_7");
        boolean[] window = new boolean[back.length];
        for (int i = 0; i < window.length; i++) window[i] = (back[i] >>> 24) > 0 && (frame[i] >>> 24) < 40;
        return new HudMasks(
                HudMask.union(mask(back, 64, 128), mask(frame, 64, 128)),
                pair(resources, "blood_overlay/gourd_back", "blood_overlay/gourd_frame_red", 32, 64),
                pair(resources, "blood_overlay/curved_horn_back", "blood_overlay/curved_horn_frame", 32, 48),
                pair(resources, "blood_overlay/hemorath_rib_back", "blood_overlay/hemorath_rib_frame", 32, 64),
                pair(resources, "blood_hud/morphling_jar_back", "blood_hud/morphling_jar_front", 28, 40),
                new HudMask(64, 128, window),
                new BindingArt(tile(resources, "binding_band"), tile(resources, "binding_lattice"),
                        tile(resources, "binding_collar")),
                vials(resources),
                name -> baked(resources, name),
                name -> bakedVials(resources, name));
    }

    /** A binding tile's three layers: {@code <name>.png}, {@code <name>_dormant.png} and {@code <name>_apotheos.png}. */
    private static BindingTile tile(ResourceManager resources, String name) {
        int[] size = new int[2];
        int[] art = argb(resources, "blood_hud/" + name, size);
        return new BindingTile(size[0], size[1], art, argb(resources, "blood_hud/" + name + "_dormant"),
                argb(resources, "blood_hud/" + name + "_apotheos"));
    }

    /** A hand-edited layout under {@code blood_hud/layouts/}, or null when there is none. */
    private static BindingTile baked(ResourceManager resources, String name) {
        String path = "blood_hud/layouts/" + name;
        if (resources.getResource(Hemomancy.rloc("textures/gui/" + path + ".png")).isEmpty()) return null;
        int[] size = new int[2];
        int[] art = argb(resources, path, size);
        return new BindingTile(size[0], size[1], art, argb(resources, path + "_dormant"), argb(resources, path + "_apotheos"));
    }

    /** A state's hand-placed vials under {@code blood_hud/layouts/}, or null when there are none. */
    private static BakedVials bakedVials(ResourceManager resources, String name) {
        String path = "blood_hud/layouts/" + name + "_vials";
        if (resources.getResource(Hemomancy.rloc("textures/gui/" + path + ".png")).isEmpty()) return null;
        int[] size = new int[2];
        int[] art = argb(resources, path, size);
        int[] glass = resources.getResource(Hemomancy.rloc("textures/gui/" + path + "_glass.png")).isPresent()
                ? argb(resources, path + "_glass") : null;
        return BakedVials.of(size[0], size[1], art, argb(resources, path + "_liquid"), argb(resources, path + "_mouth"), glass);
    }

    private static TemperVialArt vials(ResourceManager resources) {
        int[] size = new int[2];
        int[] right = argb(resources, "blood_hud/temper_vial_right", size);
        return TemperVialArt.of(size[0], right, argb(resources, "blood_hud/temper_vial_right_liquid"),
                argb(resources, "blood_hud/temper_vial_left"), argb(resources, "blood_hud/temper_vial_left_liquid"),
                argb(resources, "blood_hud/temper_socket_right"), argb(resources, "blood_hud/temper_socket_left"),
                argb(resources, "blood_hud/temper_vial_right_glass"), argb(resources, "blood_hud/temper_vial_left_glass"));
    }

    /** ARGB pixels of a GUI texture under {@code textures/gui/}. */
    public static int[] argb(ResourceManager resources, String name) {
        return argb(resources, name, new int[2]);
    }

    /** As {@link #argb(ResourceManager, String)}, also writing the image's width and height into {@code size}. */
    public static int[] argb(ResourceManager resources, String name, int[] size) {
        try (InputStream stream = resources.open(Hemomancy.rloc("textures/gui/" + name + ".png"));
             NativeImage image = NativeImage.read(stream)) {
            size[0] = image.getWidth();
            size[1] = image.getHeight();
            int[] out = new int[image.getWidth() * image.getHeight()];
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                out[y * image.getWidth() + x] = (abgr & 0xFF00FF00) | ((abgr & 0xFF) << 16) | ((abgr >> 16) & 0xFF);
            }
            return out;
        } catch (IOException error) {
            throw new IllegalStateException("Unable to load HUD texture " + name, error);
        }
    }

    private static HudMask pair(ResourceManager resources, String back, String front, int w, int h) {
        return HudMask.union(mask(argb(resources, back), w, h), mask(argb(resources, front), w, h));
    }

    private static HudMask mask(int[] argb, int w, int h) {
        if (argb.length != w * h) throw new IllegalStateException("HUD texture is " + argb.length + " px, expected " + w + "x" + h);
        return HudMask.fromArgb(w, h, argb, MIN_ALPHA);
    }
}
