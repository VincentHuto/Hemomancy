package com.vincenthuto.hemomancy.common.block.harbinger;

import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EscharianOvergrowthBlockTest {
    @Test void lichenBlocksHaveOneStateAndAFullCubeShape() {
        assertTrue(java.util.Arrays.stream(EscharianOvergrowthBlock.class.getDeclaredFields())
                .noneMatch(field -> Property.class.isAssignableFrom(field.getType())));
        assertEquals(1, EscharianOvergrowthBlock.cachedShapeCount());
        assertFalse(EscharianOvergrowthBlock.shape().isEmpty());
        assertEquals(Shapes.block(), EscharianOvergrowthBlock.shape());
    }
}
