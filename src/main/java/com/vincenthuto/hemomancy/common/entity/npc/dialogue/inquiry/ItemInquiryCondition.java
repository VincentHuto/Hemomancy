
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import java.util.List;

/** One ordered conditional branch inside an {@link ItemInquiryEntry}. */
public record ItemInquiryCondition(
        int minDegree,
        int maxDegree,
        float minPurity,
        float maxPurity,
        float minClarity,
        float maxClarity,
        Boolean clarityUnlocked,
        Boolean requiresActiveBlood,
        Boolean requiresPurifying,
        List<String> lines
) {
    /** Compatibility constructor for the original degree/purity schema. */
    public ItemInquiryCondition(int minDegree, int maxDegree, float minPurity, float maxPurity,
            List<String> lines) {
        this(minDegree, maxDegree, minPurity, maxPurity, -1F, -1F, null, null, null, lines);
    }

    public static ItemInquiryCondition unconditional(List<String> lines) {
        return new ItemInquiryCondition(-1, -1, -1F, -1F, -1F, -1F, null, null, null, lines);
    }

    public boolean matches(ItemInquiryContext context) {
        if (minDegree >= 0 && context.degree() < minDegree) return false;
        if (maxDegree >= 0 && context.degree() > maxDegree) return false;
        if (minPurity >= 0 && context.purity() < minPurity) return false;
        if (maxPurity >= 0 && context.purity() > maxPurity) return false;
        if (minClarity >= 0 && context.clarity() < minClarity) return false;
        if (maxClarity >= 0 && context.clarity() > maxClarity) return false;
        if (clarityUnlocked != null && context.clarityUnlocked() != clarityUnlocked) return false;
        if (requiresActiveBlood != null && context.activeBlood() != requiresActiveBlood) return false;
        if (requiresPurifying != null && context.purifying() != requiresPurifying) return false;
        return true;
    }

    public boolean matches(int degree, float purity) {
        return matches(ItemInquiryContext.legacy(degree, purity));
    }

    public boolean isUnconstrained() {
        return minDegree < 0 && maxDegree < 0 && minPurity < 0 && maxPurity < 0
                && minClarity < 0 && maxClarity < 0 && clarityUnlocked == null
                && requiresActiveBlood == null && requiresPurifying == null;
    }
}
