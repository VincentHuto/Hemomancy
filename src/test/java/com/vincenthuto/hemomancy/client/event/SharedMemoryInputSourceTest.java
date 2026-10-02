package com.vincenthuto.hemomancy.client.event;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SharedMemoryInputSourceTest {
    @Test
    void useKeyRoutesAndResetsFromTheSharedMemorySelection() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
        String input = source.substring(source.indexOf("private static void handleManipulationInput()"),
                source.indexOf("public static void interruptManipulationCharge()"));

        assertTrue(input.contains("known.getSelectedMemoryRef()"),
                "Use input must follow the selected Noetic or Thelemic memory");
        assertTrue(input.contains("MemoryEntryKind.MUSCLE_MEMORY"));
        assertTrue(input.contains("EnumManipulationType.PASSIVE"),
                "Thelemic activation is a press toggle, not the previous Noetic's charge/channel");
        assertTrue(input.contains("memory.storageKey()"),
                "Changing memory kinds must reset held input even if the legacy Noetic is unchanged");
        assertFalse(input.contains("known.getSelectedManip()"),
                "The stale Noetic selection must not choose the active memory's input mode");
    }

    @Test
    void chargeInterruptionRetiresTheWorldPreviewBeforeClearingHeldTicks() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
        String cleanup = source.substring(source.indexOf("private static void stopCastingChargePresentation()"),
                source.indexOf("public static void manipulationCastAccepted()"));
        assertTrue(cleanup.contains("ManipulationChargeVisualPacket(0)"),
                "Charge cleanup must retire the world preview as well as the player pose");
    }
}
