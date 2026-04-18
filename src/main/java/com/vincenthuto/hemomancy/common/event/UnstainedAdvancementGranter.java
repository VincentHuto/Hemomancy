package com.vincenthuto.hemomancy.common.event;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Utility class for programmatically granting Hemomancy Unstained-path
 * advancements when purity/clarity stage thresholds are crossed or when
 * specific in-world events occur.
 *
 * <p>Advancement JSON files that are meant to be granted this way should declare
 * {@code "trigger": "minecraft:impossible"} in their criteria, ensuring they
 * can only be awarded through this helper and not by normal gameplay triggers.
 */
public final class UnstainedAdvancementGranter {

	// ── Purity stage advancements ──────────────────────────────────────────────
	public static final ResourceLocation ADV_BLESSED_BY_ALTAR =
			new ResourceLocation("hemomancy", "hemomancy/blessed_by_the_altar");
	public static final ResourceLocation ADV_TAINTED =
			new ResourceLocation("hemomancy", "hemomancy/tainted");
	public static final ResourceLocation ADV_CLEANSING =
			new ResourceLocation("hemomancy", "hemomancy/cleansing");
	public static final ResourceLocation ADV_ABSOLVED =
			new ResourceLocation("hemomancy", "hemomancy/absolved");
	public static final ResourceLocation ADV_PURIFIED =
			new ResourceLocation("hemomancy", "hemomancy/purified");

	// ── Clarity advancements ────────────────────────────────────────────────────
	public static final ResourceLocation ADV_CLARITY_AWAKENED =
			new ResourceLocation("hemomancy", "hemomancy/clarity_awakened");
	public static final ResourceLocation ADV_DISCERNING =
			new ResourceLocation("hemomancy", "hemomancy/discerning");
	public static final ResourceLocation ADV_VIGILANT =
			new ResourceLocation("hemomancy", "hemomancy/vigilant");
	public static final ResourceLocation ADV_RESOLUTE_STAGE =
			new ResourceLocation("hemomancy", "hemomancy/resolute_stage");
	public static final ResourceLocation ADV_ENLIGHTENED_SEEKER =
			new ResourceLocation("hemomancy", "hemomancy/enlightened_seeker");

	private UnstainedAdvancementGranter() {}

	/**
	 * Grants the advancement identified by {@code id} to the player if it has
	 * not already been completed. Safe to call repeatedly.
	 *
	 * @param player The server-side player to receive the advancement.
	 * @param id     The ResourceLocation of the advancement to grant.
	 */
	public static void grantIfNotDone(ServerPlayer player, ResourceLocation id) {
		Advancement advancement = player.server.getAdvancements().getAdvancement(id);
		if (advancement == null) return;

		AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
		if (progress.isDone()) return;

		for (String criterion : progress.getRemainingCriteria()) {
			player.getAdvancements().award(advancement, criterion);
		}
	}
}
