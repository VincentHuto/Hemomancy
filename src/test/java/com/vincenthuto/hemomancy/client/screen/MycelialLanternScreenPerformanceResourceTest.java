package com.vincenthuto.hemomancy.client.screen;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class MycelialLanternScreenPerformanceResourceTest {
    private static final Path SOURCE_ROOT = Path.of("src/main/java");

    private MycelialLanternScreenPerformanceResourceTest() {
    }

    @Test
    void allChecksPass() throws IOException {
        String screen = read(SOURCE_ROOT.resolve(
                "com/vincenthuto/hemomancy/client/screen/tile/crafting/MycelialLanternScreen.java"));
        String menu = read(SOURCE_ROOT.resolve(
                "com/vincenthuto/hemomancy/common/menu/tile/crafting/MycelialLanternMenu.java"));
        String blockEntity = read(SOURCE_ROOT.resolve(
                "com/vincenthuto/hemomancy/common/tile/harbinger/crafting/MycelialLanternBlockEntity.java"));

        assertContains("mycelial lantern should keep tendrils close to scar station cost", screen,
                "private static final int TENDRIL_COUNT = 14;");
        assertContains("mycelial lantern should keep spores lightweight", screen,
                "private static final int SPORE_COUNT = 28;");
        assertContains("mycelial lantern should keep static speckles lightweight", screen,
                "speckleParams = new int[48][5];");
        assertContains("mycelial lantern should scissor its animated background", screen,
                "gfx.enableScissor(gx, gy, gx + gw, gy + gh);");
        assertContains("mycelial lantern should keep its animation behind the bell's glass", screen,
                "renderFungalBackground(gfx, gx + BELL_X0, gy + BELL_Y0, BELL_X1 - BELL_X0, BELL_Y1 - BELL_Y0);");
        assertContains("mycelial lantern should use simple constant-cost borders", screen,
                "ScreenDrawUtils.drawBevelBorder(gfx, rx, ry, RIM_X1 - RIM_X0, RIM_Y1 - RIM_Y0, IRON_LIGHT, IRON_DARK);");
        assertNotContains("mycelial lantern should not draw per-pixel gradient borders every frame", screen,
                "drawGradientBorder");
        assertNotContains("mycelial lantern should not flash a square glow around the culture slot", screen,
                "renderCultureGlow");

        assertNotContains("mycelial lantern menu should rely on slot/data sync instead of full block sync every tick",
                menu, "public void broadcastChanges()");
        assertContains("mycelial lantern should separate world changes from full client sync", blockEntity,
                "boolean worldChanged = syncChanged;");
        assertContains("mycelial lantern should only full-sync when display inventory changes", blockEntity,
                "if (syncChanged) {");
        assertNotContains("mycelial lantern should not full-sync every progress tick", blockEntity,
                "if (changed) {\n            te.sendUpdates();");
    }
}
