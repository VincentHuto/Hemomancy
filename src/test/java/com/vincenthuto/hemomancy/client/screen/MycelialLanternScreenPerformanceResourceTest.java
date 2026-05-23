package com.vincenthuto.hemomancy.client.screen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MycelialLanternScreenPerformanceResourceTest {
    private static final Path SOURCE_ROOT = Path.of("src/main/java");

    private MycelialLanternScreenPerformanceResourceTest() {
    }

    public static void main(String[] args) throws IOException {
        String screen = read(SOURCE_ROOT.resolve(
                "com/vincenthuto/hemomancy/client/screen/tile/crafting/MycelialLanternScreen.java"));
        String menu = read(SOURCE_ROOT.resolve(
                "com/vincenthuto/hemomancy/common/menu/tile/crafting/MycelialLanternMenu.java"));
        String blockEntity = read(SOURCE_ROOT.resolve(
                "com/vincenthuto/hemomancy/common/tile/crafting/MycelialLanternBlockEntity.java"));

        assertContains("mycelial lantern should keep tendrils close to scar station cost", screen,
                "private static final int TENDRIL_COUNT = 14;");
        assertContains("mycelial lantern should keep spores lightweight", screen,
                "private static final int SPORE_COUNT = 28;");
        assertContains("mycelial lantern should keep static speckles lightweight", screen,
                "speckleParams = new int[48][5];");
        assertContains("mycelial lantern should scissor its animated background", screen,
                "gfx.enableScissor(gx, gy, gx + gw, gy + gh);");
        assertContains("mycelial lantern should use simple constant-cost borders", screen,
                "private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h)");
        assertDoesNotContain("mycelial lantern should not draw per-pixel gradient borders every frame", screen,
                "drawGradientBorder");
        assertDoesNotContain("mycelial lantern should not flash a square glow around the culture slot", screen,
                "renderCultureGlow");

        assertDoesNotContain("mycelial lantern menu should rely on slot/data sync instead of full block sync every tick",
                menu, "public void broadcastChanges()");
        assertContains("mycelial lantern should separate world changes from full client sync", blockEntity,
                "boolean worldChanged = syncChanged;");
        assertContains("mycelial lantern should only full-sync when display inventory changes", blockEntity,
                "if (syncChanged) {");
        assertDoesNotContain("mycelial lantern should not full-sync every progress tick", blockEntity,
                "if (changed) {\n            te.sendUpdates();");
    }

    private static String read(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new AssertionError("missing " + path);
        }
        return Files.readString(path).replace("\r\n", "\n");
    }

    private static void assertContains(String label, String text, String expected) {
        if (!text.contains(expected)) {
            throw new AssertionError(label + ": missing " + expected);
        }
    }

    private static void assertDoesNotContain(String label, String text, String forbidden) {
        if (text.contains(forbidden)) {
            throw new AssertionError(label + ": found " + forbidden);
        }
    }
}
