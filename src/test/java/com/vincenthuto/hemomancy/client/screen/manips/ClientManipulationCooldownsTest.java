package com.vincenthuto.hemomancy.client.screen.manips;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientManipulationCooldownsTest {
    @AfterEach
    void clearCooldowns() {
        ClientManipulationCooldowns.clear();
    }

    @Test
    void tracksAndExpiresEachManipulationSeparately() {
        ClientManipulationCooldowns.start("blood_binding", 2, 100L);
        ClientManipulationCooldowns.start("ironhearted", 4, 100L);

        assertEquals(0, ClientManipulationCooldowns.remainingTicks("blood_binding", 102L));
        assertEquals(2, ClientManipulationCooldowns.remainingTicks("ironhearted", 102L));
    }

    @Test
    void zeroDurationClearsOnlyTheNamedManipulation() {
        ClientManipulationCooldowns.start("blood_binding", 20, 100L);
        ClientManipulationCooldowns.start("ironhearted", 40, 100L);

        ClientManipulationCooldowns.start("blood_binding", 0, 100L);

        assertEquals(0, ClientManipulationCooldowns.remainingTicks("blood_binding", 100L));
        assertEquals(40, ClientManipulationCooldowns.remainingTicks("ironhearted", 100L));
    }
}
