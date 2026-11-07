package com.vincenthuto.hemomancy.common.entity.boss.saint;

/** A move that may start straight after another, after {@code linkGap} ticks instead of the decision delay. */
public record SaintFollowUp(SaintMove next, float chance, int linkGap, FollowCondition when) {
}
