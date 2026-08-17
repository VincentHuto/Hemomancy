package com.vincenthuto.hemomancy.client.screen.manips;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MuscleMemoryUiIntegrationSourceTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();

    public static void main(String[] args) throws IOException {
        String hand = read("src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/MuscleMemoryHandRenderer.java");
        String radial = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java");
        String radialItem = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/MuscleMemoryRadialMenuItem.java");
        String reliquary = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MnemonicReliquaryScreen.java");

        require(hand.contains("RenderArmEvent"), "Thelemic arm must use NeoForge's first-person arm replacement hook");
        require(hand.contains("event.setCanceled(true)"), "Thelemic arm must replace the vanilla arm instead of rendering behind it");
        require(!hand.contains("RenderHandEvent"), "Thelemic arm must not render before the vanilla hand");
        require(hand.contains("BloodArmModel"), "first-person Thelemic arms must use the expanded overlay geometry");
        require(radial.contains("EnumSet<EnumVeinSections>"), "radial must collapse assigned memories by vascular section");
        require(radialItem.contains("List<Component> tooltip"), "radial tooltip must be separate lines");
        require(radial.contains("MuscleMemoryPrimingRules.TICKS_PER_DOSE"),
                "radial reserve meter must show the remaining five-minute dose window");
        require(radialItem.contains("left + filled, top + 20"), "radial reserve meter must fill horizontally");
        require(!radialItem.contains("left - 2, y"), "radial reserve meter must not use vertical side bars");
        require(reliquary.contains("drawVascularSectionSlots"), "Reliquary must render fixed vascular section targets");
        require(reliquary.contains("cycleVascularSection"), "vascular section targets must select Thelemic memories");
        require(reliquary.contains("preparedMuscleIcons"), "Reliquary must show prepared tinctures in tendency groups");
        require(reliquary.contains("reserveTicks(memory) > 0"), "Reliquary must hide tinctures without reserve");
        require(reliquary.contains("List<MuscleMemory> choices = preparedMuscleMemories.stream()"),
                "vascular sections must cycle only prepared tinctures");
        require(reliquary.contains("tinctureStackFor(assigned)"),
                "vascular sections must display the selected tincture icon");
        require(reliquary.contains("equippedNames.contains(MemorySlotRef.muscleMemory(icon.memory).storageKey())"),
                "selected tinctures must be highlighted in their tendency groups");
        require(!reliquary.contains("vascularSectionStack"), "Reliquary must not use the debug vascular gauge");
        require(reliquary.contains("draggingMuscleMemory"),
                "prepared tinctures must drag into the mnemonic wheel");
        require(reliquary.contains("memory.section() == draggingMuscleMemory.section()"),
                "dropping a tincture must replace only its associated vascular section");
        require(reliquary.contains("int equipRadius = currentBrainRadius - scaled(10)"),
                "equipped Noetic memories must use the larger outer ring");
        require(reliquary.contains("(2.0 * Math.PI) / EnumVeinSections.values().length"),
                "vascular sections must use the smaller inner ring");
    }

    private static String read(String path) throws IOException {
        return Files.readString(ROOT.resolve(path));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
