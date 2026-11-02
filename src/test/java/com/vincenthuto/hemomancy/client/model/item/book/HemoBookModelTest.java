package com.vincenthuto.hemomancy.client.model.item.book;

import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class HemoBookModelTest {
    private static final Map<String, Supplier<LayerDefinition>> LAYERS = Map.of(
            "liber_sanguinum", HemoBookLayers::createLiberSanguinumLayer,
            "liber_immaculatus", HemoBookLayers::createLiberImmaculatusLayer,
            "book_of_observances", HemoBookLayers::createBookOfObservancesLayer,
            "harbinger_assignment_ledger", HemoBookLayers::createHarbingerAssignmentLedgerLayer,
            "mnemonic_folio", HemoBookLayers::createMnemonicFolioLayer);
    private static final Map<String, float[]> BOUNDS = Map.of(
            "liber_sanguinum", HemoBookLayers.LIBER_SANGUINUM_BOUNDS,
            "liber_immaculatus", HemoBookLayers.LIBER_IMMACULATUS_BOUNDS,
            "book_of_observances", HemoBookLayers.BOOK_OF_OBSERVANCES_BOUNDS,
            "harbinger_assignment_ledger", HemoBookLayers.HARBINGER_ASSIGNMENT_LEDGER_BOUNDS,
            "mnemonic_folio", HemoBookLayers.MNEMONIC_FOLIO_BOUNDS);

    @Test
    void everyBookOpensWithItsSpineHalvesAndFlipsItsLeaves() {
        LAYERS.forEach((name, layer) -> {
            ModelPart root = layer.get().bakeRoot();
            HemoBookModel model = new HemoBookModel(root);
            ModelPart left = root.getChild("left_lid"), right = root.getChild("right_lid");

            model.setupAnim(0, 0, 1, 0);
            assertEquals(0, left.yRot, name + " shut");
            assertTrue(left.getChild("left_closed").visible && !right.getChild("right_open").visible,
                    name + " shows its clasp fastened while shut");

            model.setupAnim(0, 0.5F, 1, 1);
            assertTrue(left.yRot > 1 && right.yRot < -1, name + " boards open away from each other");
            assertEquals(left.yRot, root.getChild("left_pages").yRot, name + " pages turn with their board");
            assertEquals(0, root.getChild("flip_page1").yRot, 1e-5, name + " a half-flipped leaf stands upright");
            assertEquals(right.yRot, root.getChild("flip_page2").yRot, 1e-5, name + " a flipped leaf lies on the right");
            assertTrue(right.getChild("right_open").visible && !right.getChild("right_closed").visible,
                    name + " lets its strap fall free while open");
            ModelPart spine = root.getChild("spine");
            float reach = spine.cubes.stream().map(c -> c.maxZ).max(Float::compare).orElseThrow();
            assertEquals(-reach * (float) Math.sin(left.yRot), spine.x, 1e-3, name + " spine slides back to the hinges");
            assertEquals(Math.cos(left.yRot), spine.zScale, 1e-3, name + " spine narrows to span the hinges");
        });
    }

    @Test
    void framingBoundsMatchTheShutMesh() {
        LAYERS.forEach((name, layer) -> {
            float[] lo = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
            float[] hi = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            ModelPart root = layer.get().bakeRoot();
            for (String child : new String[]{"left_lid", "right_lid", "spine", "left_pages", "right_pages", "flip_page1", "flip_page2"}) {
                ModelPart part = root.getChild(child);
                include(part, lo, hi);
                if (child.endsWith("_lid")) include(part.getChild(child.replace("lid", "closed")), lo, hi);
            }
            float[] bounds = BOUNDS.get(name);
            for (int axis = 0; axis < 3; axis++) {
                assertEquals(lo[axis], bounds[axis], 1e-4, name + " min " + axis);
                assertEquals(hi[axis], bounds[axis + 3], 1e-4, name + " max " + axis);
            }
        });
    }

    @Test
    void atlasesKeepEachSidesMetals() throws IOException {
        for (String name : LAYERS.keySet()) {
            BufferedImage atlas = ImageIO.read(ResourceReader.path("assets/hemomancy/textures/entity/book/" + name + ".png").toFile());
            assertEquals(128, atlas.getWidth(), name + " is painted at twice block resolution");
            boolean unstained = name.equals("liber_immaculatus") || name.equals("book_of_observances");
            for (int y = 0; y < atlas.getHeight(); y++) {
                for (int x = 0; x < atlas.getWidth(); x++) {
                    int argb = atlas.getRGB(x, y);
                    if (argb >>> 24 == 0) continue;
                    int r = argb >> 16 & 255, g = argb >> 8 & 255, b = argb & 255;
                    if (unstained) assertFalse(r > g + 60 && r > b + 60, name + " carries no blood red at " + x + "," + y);
                    else assertFalse(g > r + 20 && g > b, name + " carries no copper patina at " + x + "," + y);
                }
            }
        }
    }

    private static void include(ModelPart part, float[] lo, float[] hi) {
        for (ModelPart.Cube cube : part.cubes) {
            lo[0] = Math.min(lo[0], cube.minX); lo[1] = Math.min(lo[1], cube.minY); lo[2] = Math.min(lo[2], cube.minZ);
            hi[0] = Math.max(hi[0], cube.maxX); hi[1] = Math.max(hi[1], cube.maxY); hi[2] = Math.max(hi[2], cube.maxZ);
        }
    }
}
