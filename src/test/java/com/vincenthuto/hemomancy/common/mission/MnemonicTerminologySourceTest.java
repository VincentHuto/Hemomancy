package com.vincenthuto.hemomancy.common.mission;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MnemonicTerminologySourceTest {
    private static final List<Path> PLAYER_FACING = List.of(
            Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"),
            Path.of("src/main/resources/data/hemomancy/books/fanesanguinium/manipulations/pages/muscle_memories.json"),
            Path.of("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java"),
            Path.of("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MnemonicReliquaryScreen.java"),
            Path.of("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/EquipManipulationPacket.java"));

    @Test
    void formalPlayerFacingMuscleMemoryTerminologyDoesNotReturn() throws IOException {
        for (Path path : PLAYER_FACING) {
            String text = Files.readString(path);
            assertFalse(text.matches("(?s).*(?i:muscle memor(?:y|ies)).*"), path.toString());
        }
    }

    @Test
    void compatibilityIdentifiersRemainDocumentedAndUntouched() throws IOException {
        String ref = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/MemorySlotRef.java"));
        assertTrue(ref.contains("muscle_memory:"));
    }

    @Test
    void successfulThelemicEquipChangesDoNotSpamTheActionBar() throws IOException {
        String packet = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/EquipManipulationPacket.java"));
        assertFalse(packet.contains("Thelemic Memory equipped:"));
        assertFalse(packet.contains("Thelemic Memory unequipped:"));
    }
}
