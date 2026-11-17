package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.Reserve;
import com.vincenthuto.hemomancy.support.RepoPaths;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bakes every HUD state's binding to {@code textures/gui/blood_hud/layouts/<state>.png} (with {@code _dormant} and
 * {@code _apotheos}) for hand editing. A state with baked files draws them as-is; one without grows from the tiles.
 * Run with {@code ./gradlew.bat exportHudLayouts}; existing files are kept unless {@code -Pforce}.
 */
public final class HudLayoutExport {
    private HudLayoutExport() {
    }

    public static void main(String[] args) throws IOException {
        boolean force = args.length > 0 && args[0].equals("force");
        File dir = RepoPaths.ASSETS.resolve("textures/gui/blood_hud/layouts").toFile();
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("cannot create " + dir);
        HudMasks masks = HudMasksFixture.masks();
        int wrote = 0, kept = 0;
        for (BloodHudLayout layout : everyState()) {
            HudAssembly assembly = HudAssembly.build(layout, masks);
            BindingTile layers = assembly.binding().layers();
            String name = layout.fileName();
            List<int[]> images = new ArrayList<>(List.of(layers.art(), layers.dormant(), layers.apotheos()));
            List<String> suffixes = new ArrayList<>(List.of("", "_dormant", "_apotheos"));
            if (layout.temper()) {
                int[][] vials = vialLayers(assembly);
                images.addAll(List.of(vials));
                suffixes.addAll(List.of("_vials", "_vials_liquid", "_vials_mouth", "_vials_glass"));
            }
            for (int i = 0; i < images.size(); i++) {
                File file = new File(dir, name + suffixes.get(i) + ".png");
                if (file.exists() && !force) {
                    kept++;
                    continue;
                }
                BufferedImage image = new BufferedImage(layers.width(), layers.height(), BufferedImage.TYPE_INT_ARGB);
                image.setRGB(0, 0, layers.width(), layers.height(), images.get(i), 0, layers.width());
                ImageIO.write(image, "png", file);
                wrote++;
            }
        }
        System.out.println("HUD layouts: wrote " + wrote + ", kept " + kept + " in " + dir);
    }

    /** The vials as the shared sprites place them: art, liquid in key colours, a mouth dot each, and the front glass. */
    static int[][] vialLayers(HudAssembly assembly) {
        int w = assembly.layout().width(), h = assembly.layout().height();
        int[] art = new int[w * h], liquid = new int[w * h], mouth = new int[w * h], glass = new int[w * h];
        for (var seat : assembly.vials()) {
            var sprite = assembly.vialArt().sprite(seat.pointsRight());
            int key = 0xFF000000 | BakedVials.KEYS[seat.school()];
            for (int i = 0; i < sprite.dx().length; i++) {
                int x = seat.mouthX() + sprite.dx()[i], y = seat.mouthY() + sprite.dy()[i];
                if (x >= 0 && y >= 0 && x < w && y < h) art[y * w + x] = sprite.argb()[i];
            }
            for (int i = 0; i < sprite.liquidDx().length; i++) {
                int x = seat.mouthX() + sprite.liquidDx()[i], y = seat.mouthY() + sprite.liquidDy()[i];
                if (x >= 0 && y >= 0 && x < w && y < h) liquid[y * w + x] = key;
            }
            var front = assembly.vialArt().glass(seat.pointsRight());
            for (int i = 0; i < front.dx().length; i++) {
                int x = seat.mouthX() + front.dx()[i], y = seat.mouthY() + front.dy()[i];
                if (x >= 0 && y >= 0 && x < w && y < h) glass[y * w + x] = front.argb()[i];
            }
            if (seat.mouthX() >= 0 && seat.mouthY() >= 0 && seat.mouthX() < w && seat.mouthY() < h) {
                mouth[seat.mouthY() * w + seat.mouthX()] = key;
            }
        }
        return new int[][]{art, liquid, mouth, glass};
    }

    /** Every state the HUD can show, on both sides. */
    static List<BloodHudLayout> everyState() {
        List<BloodHudLayout> states = new ArrayList<>();
        for (boolean mirrored : new boolean[]{false, true}) for (Reserve reserve : Reserve.values()) {
            for (int bits = 0; bits < 8; bits++) {
                var layout = new BloodHudLayout((bits & 1) != 0, reserve, (bits & 2) != 0, (bits & 4) != 0, mirrored);
                if (!layout.empty()) states.add(layout);
            }
        }
        return states;
    }
}
