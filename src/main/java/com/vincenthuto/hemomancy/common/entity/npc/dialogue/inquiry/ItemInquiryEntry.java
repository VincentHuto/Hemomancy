
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import java.util.List;
import java.util.Optional;

/** Loaded representation of one item-inquiry JSON file. */
public record ItemInquiryEntry(List<ItemInquiryCondition> conditions) {
    public Optional<List<String>> resolve(ItemInquiryContext context) {
        for (ItemInquiryCondition branch : conditions) {
            if (branch.matches(context)) return Optional.of(branch.lines());
        }
        return Optional.empty();
    }

    public Optional<List<String>> resolve(int degree, float purity) {
        return resolve(ItemInquiryContext.legacy(degree, purity));
    }
}
