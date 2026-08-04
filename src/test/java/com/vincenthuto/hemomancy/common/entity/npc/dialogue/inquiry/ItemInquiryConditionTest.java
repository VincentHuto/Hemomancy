
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemInquiryConditionTest {
    @Test
    void clarityGateRequiresUnlockedClarityAndMinimumValue() {
        ItemInquiryCondition condition = new ItemInquiryCondition(-1, -1, -1F, -1F, 50F, -1F,
                true, null, null, List.of("key"));
        assertFalse(condition.matches(new ItemInquiryContext(0, 100, 49, true, false, true, false, false)));
        assertFalse(condition.matches(new ItemInquiryContext(0, 100, 75, false, false, true, false, false)));
        assertTrue(condition.matches(new ItemInquiryContext(0, 100, 75, true, false, true, false, false)));
    }

    @Test
    void pathAndBloodFlagsAreIndependent() {
        ItemInquiryCondition condition = new ItemInquiryCondition(-1, -1, -1F, -1F, -1F, -1F,
                null, false, true, List.of("key"));
        assertTrue(condition.matches(new ItemInquiryContext(4, 25, 0, false, false, true, false, false)));
        assertFalse(condition.matches(new ItemInquiryContext(4, 25, 0, false, true, true, false, false)));
    }
}
