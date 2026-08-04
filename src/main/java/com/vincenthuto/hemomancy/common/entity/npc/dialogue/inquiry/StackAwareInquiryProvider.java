
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Resolves data-bearing stacks before ordinary registry-ID inquiry lookup. */
@FunctionalInterface
public interface StackAwareInquiryProvider {
    Optional<StackAwareInquiryRegistry.ResolvedStackInquiry> resolve(
            String speakerId, ItemStack stack, ItemInquiryContext context);
}
