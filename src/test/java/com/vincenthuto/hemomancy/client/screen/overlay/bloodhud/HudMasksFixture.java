package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.vincenthuto.hemomancy.support.RepoPaths;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

/** The real HUD part silhouettes, read from the shipped textures the way the client loader reads them. */
final class HudMasksFixture {
    private HudMasksFixture() {
    }

    static HudMask vessel() {
        return HudMask.union(mask("blood_overlay/vessel_back"), mask("blood_overlay/base_degree_7"));
    }

    static HudMask gourd() {
        return HudMask.union(mask("blood_overlay/gourd_back"), mask("blood_overlay/gourd_frame_red"));
    }

    static HudMask horn() {
        return HudMask.union(mask("blood_overlay/curved_horn_back"), mask("blood_overlay/curved_horn_frame"));
    }

    static HudMask rib() {
        return HudMask.union(mask("blood_overlay/hemorath_rib_back"), mask("blood_overlay/hemorath_rib_frame"));
    }

    static HudMask jar() {
        return HudMask.union(mask("blood_hud/morphling_jar_back"), mask("blood_hud/morphling_jar_front"));
    }

    static HudMask vesselWindow() {
        int[] back = argb("blood_overlay/vessel_back"), frame = argb("blood_overlay/base_degree_7");
        boolean[] solid = new boolean[back.length];
        for (int i = 0; i < solid.length; i++) solid[i] = (back[i] >>> 24) > 0 && (frame[i] >>> 24) < 40;
        return new HudMask(64, 128, solid);
    }

    static HudMasks masks() {
        return new HudMasks(vessel(), gourd(), horn(), rib(), jar(), vesselWindow(), bindingArt(), vialArt(), name -> null, name -> null);
    }

    static BindingArt bindingArt() {
        return new BindingArt(tile("binding_band"), tile("binding_lattice"), tile("binding_collar"));
    }

    static BindingTile tile(String name) {
        BufferedImage art = image("blood_hud/" + name);
        return new BindingTile(art.getWidth(), art.getHeight(), argb("blood_hud/" + name),
                argb("blood_hud/" + name + "_dormant"), argb("blood_hud/" + name + "_apotheos"));
    }

    static TemperVialArt vialArt() {
        return TemperVialArt.of(image("blood_hud/temper_vial_right").getWidth(), argb("blood_hud/temper_vial_right"),
                argb("blood_hud/temper_vial_right_liquid"), argb("blood_hud/temper_vial_left"),
                argb("blood_hud/temper_vial_left_liquid"), argb("blood_hud/temper_socket_right"),
                argb("blood_hud/temper_socket_left"), argb("blood_hud/temper_vial_right_glass"),
                argb("blood_hud/temper_vial_left_glass"));
    }

    static int[] argb(String name) {
        BufferedImage image = image(name);
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    static BufferedImage image(String name) {
        try {
            return ImageIO.read(RepoPaths.ASSETS.resolve("textures/gui/" + name + ".png").toFile());
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    static HudMask mask(String name) {
        BufferedImage image = image(name);
        return HudMask.fromArgb(image.getWidth(), image.getHeight(), argb(name), 21);
    }
}
