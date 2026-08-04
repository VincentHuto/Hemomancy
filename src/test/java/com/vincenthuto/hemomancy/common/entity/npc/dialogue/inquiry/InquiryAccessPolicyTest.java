package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InquiryAccessPolicyTest {
    @Test
    void clarityAlchemistHasNoOperationalInquiry() {
        ItemInquiryContext context = new ItemInquiryContext(0, 100, 25, true, false, true, false, false);
        assertEquals(InquiryAccessPolicy.Access.NONE, InquiryAccessPolicy.accessFor("alchemist", context));
    }

    @Test
    void purifyingAlchemistAndArtificerOnlyIdentify() {
        ItemInquiryContext context = new ItemInquiryContext(4, 50, 0, false, true, true, false, false);
        assertEquals(InquiryAccessPolicy.Access.IDENTIFICATION_ONLY,
                InquiryAccessPolicy.accessFor("alchemist", context));
        assertEquals(InquiryAccessPolicy.Access.IDENTIFICATION_ONLY,
                InquiryAccessPolicy.accessFor("artificer", context));
    }

    @Test
    void normalPractitionerRetainsFullInquiry() {
        ItemInquiryContext context = new ItemInquiryContext(4, 0, 0, false, true, false, false, false);
        assertEquals(InquiryAccessPolicy.Access.FULL, InquiryAccessPolicy.accessFor("alchemist", context));
        assertEquals(InquiryAccessPolicy.Access.FULL, InquiryAccessPolicy.accessFor("artificer", context));
    }
}
