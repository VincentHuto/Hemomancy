package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class QliphothBarkMeshKeyTest {
    @Test
    void stageAndSeveredStateSelectDifferentMeshes() {
        QliphothBarkMeshKey standing = QliphothBarkMeshKey.create(8, false);

        assertNotEquals(standing, QliphothBarkMeshKey.create(9, false));
        assertNotEquals(standing, QliphothBarkMeshKey.create(8, true));
    }

    @Test
    void stageIsClampedToTheAuthoredRange() {
        assertEquals(QliphothBarkMeshKey.create(0, false),
                QliphothBarkMeshKey.create(-3, false));
        assertEquals(QliphothBarkMeshKey.create(9, true),
                QliphothBarkMeshKey.create(14, true));
    }
}
