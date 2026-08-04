
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

/** Explicitly enforces the instructional limits claimed by refusal dialogue. */
public final class InquiryAccessPolicy {
    public enum Access { FULL, IDENTIFICATION_ONLY, NONE }

    private InquiryAccessPolicy() {}

    public static Access accessFor(String speakerId, ItemInquiryContext context) {
        if ("alchemist".equals(speakerId)) {
            if (context.clarityUnlocked()) return Access.NONE;
            if (context.purifying()) return Access.IDENTIFICATION_ONLY;
        }
        if ("artificer".equals(speakerId) && (context.purifying() || context.clarityUnlocked())) {
            return Access.IDENTIFICATION_ONLY;
        }
        return Access.FULL;
    }

    public static String refusalKey(String speakerId) {
        return "hemomancy." + speakerId + ".item_inquiry.refusal";
    }
}
