package com.vincenthuto.hemomancy.client.screen.manips;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class ThelemicMemoryUiIntegrationSourceTest {

    @Test
    void allChecksPass() throws IOException {
        String hand = read("src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/ThelemicMemoryHandRenderer.java");
        String radial = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java");
        String radialItem = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/ThelemicMemoryRadialMenuItem.java");
        String reliquary = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MnemonicReliquaryScreen.java");

        require(hand.contains("RenderArmEvent"), "Thelemic arm must use NeoForge's first-person arm replacement hook");
        require(hand.contains("event.setCanceled(true)"), "Thelemic arm must replace the vanilla arm instead of rendering behind it");
        require(!hand.contains("RenderHandEvent"), "Thelemic arm must not render before the vanilla hand");
        require(hand.contains("BloodArmModel"), "first-person Thelemic arms must use the expanded overlay geometry");
        require(radial.contains("EnumSet<EnumVeinSections>"), "radial must collapse assigned memories by vascular section");
        require(radialItem.contains("List<Component> tooltip"), "radial tooltip must be separate lines");
        require(radial.contains("ThelemicMemoryPrimingRules.TICKS_PER_DOSE"),
                "radial reserve meter must show the remaining five-minute dose window");
        require(radialItem.contains("left + filled, top + 20"), "radial reserve meter must fill horizontally");
        require(!radialItem.contains("left - 2, y"), "radial reserve meter must not use vertical side bars");
        require(reliquary.contains("drawVascularSectionSlots"), "Reliquary must render fixed vascular section targets");
        require(reliquary.contains("cycleVascularSection"), "vascular section targets must select Thelemic memories");
        require(reliquary.contains("memory.section() != node.section"), "Reliquary must group prepared tinctures by vascular section");
        require(reliquary.contains("reserveTicks(memory) > 0"), "Reliquary must hide tinctures without reserve");
        require(reliquary.contains("MemoryEquipRules.nextPreparedInSection(preparedThelemicMemories, current, section)"),
                "vascular sections must cycle only prepared tinctures");
        require(reliquary.contains("tinctureStackFor(assigned)"),
                "vascular sections must display the selected tincture icon");
        require(reliquary.contains("equippedNames.contains(MemorySlotRef.thelemicMemory(icon.memory).storageKey())"),
                "selected tinctures must be highlighted on the prepared rack");
        require(!reliquary.contains("vascularSectionStack"), "Reliquary must not use the debug vascular gauge");
        require(reliquary.contains("draggingThelemicMemory"),
                "prepared tinctures must be draggable");
        require(reliquary.contains("memory.section() == draggingThelemicMemory.section()"),
                "dropping a tincture must replace only its associated vascular section");
        require(reliquary.contains("equippedIcons.add(new ManipIcon(manip, ix, iy, currentIconSize, manip.getTend()))"),
                "equipped Noetic memories must sit in sockets on the relic");
        require(reliquary.contains("VascularNode[] VASCULAR_NODES"),
                "vascular sockets must share anatomical positions with the plate");
        require(reliquary.contains("getHoveredSection(mouseX, mouseY) == memory.section()"),
                "a tincture dropped on its own section's socket must equip it");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
