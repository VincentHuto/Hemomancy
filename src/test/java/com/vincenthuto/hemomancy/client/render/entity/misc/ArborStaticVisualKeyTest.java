package com.vincenthuto.hemomancy.client.render.entity.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ArborStaticVisualKeyTest {
    @Test
    void apotheosisIgnoresProgressionFoliageDifferences() {
        ArborStaticVisualKey sparse = ArborStaticVisualKey.create(8, 10, 0.12F, 15728880);
        ArborStaticVisualKey full = ArborStaticVisualKey.create(8, 10, 1.0F, 15728880);

        assertEquals(full, sparse);
        assertEquals(1000, sparse.foliagePermille());
    }

    @Test
    void structuralInputsInvalidateTheKey() {
        ArborStaticVisualKey base = ArborStaticVisualKey.create(7, 10, 0.50F, 120);

        assertNotEquals(base, ArborStaticVisualKey.create(6, 10, 0.50F, 120));
        assertNotEquals(base, ArborStaticVisualKey.create(7, 9, 0.50F, 120));
        assertNotEquals(base, ArborStaticVisualKey.create(7, 10, 0.75F, 120));
        assertNotEquals(base, ArborStaticVisualKey.create(7, 10, 0.50F, 121));
    }
}
