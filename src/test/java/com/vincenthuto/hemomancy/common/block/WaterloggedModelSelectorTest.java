package com.vincenthuto.hemomancy.common.block;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaterloggedModelSelectorTest {
    @Test void acceptsMultipartAndStageFacingVariantsWithoutWaterSelectors() {
        assertDoesNotThrow(() -> WaterloggableDecorativeBlocksTest.assertWaterIndependentModels("multipart",
                "{\"multipart\":[{\"when\":{\"facing\":\"north\"},\"apply\":{\"model\":\"hemomancy:block/gourd\"}}]}"));
        assertDoesNotThrow(() -> WaterloggableDecorativeBlocksTest.assertWaterIndependentModels("variants",
                "{\"variants\":{\"facing=north,stage=1\":{\"model\":\"hemomancy:block/vial_centrifuge_calibrated\"}}}"));
    }

    @Test void rejectsWaterSelectorsInEitherModelDefinition() {
        assertThrows(AssertionError.class, () -> WaterloggableDecorativeBlocksTest.assertWaterIndependentModels("variants",
                "{\"variants\":{\"waterlogged=true\":{\"model\":\"example:block\"}}}"));
        assertThrows(AssertionError.class, () -> WaterloggableDecorativeBlocksTest.assertWaterIndependentModels("multipart",
                "{\"multipart\":[{\"when\":{\"waterlogged\":\"true\"},\"apply\":{\"model\":\"example:block\"}}]}"));
    }
}
