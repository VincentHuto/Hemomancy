package com.vincenthuto.hemomancy.common.brewing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlembicTierTest {
    @Test
    void oldAndInvalidTierValuesLoadAsBase() {
        assertEquals(AlembicTier.BASE, AlembicTier.fromSaved(0));
        assertEquals(AlembicTier.BASE, AlembicTier.fromSaved(-1));
        assertEquals(AlembicTier.BASE, AlembicTier.fromSaved(3));
        assertEquals(AlembicTier.CONDENSER, AlembicTier.fromSaved(1));
        assertEquals(AlembicTier.ATHANOR, AlembicTier.fromSaved(2));
    }

    @Test
    void secondCatalystOpensOnlyAtAthanor() {
        assertFalse(AlembicTier.BASE.hasSecondCatalyst());
        assertFalse(AlembicTier.CONDENSER.hasSecondCatalyst());
        assertTrue(AlembicTier.ATHANOR.hasSecondCatalyst());
    }
}
