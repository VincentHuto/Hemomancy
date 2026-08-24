package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
			Hemomancy.rloc("hemomancy/blessed_by_the_altar");
	public static final ResourceLocation ADV_TAINTED =
			Hemomancy.rloc("hemomancy/tainted");
	public static final ResourceLocation ADV_CLEANSING =
			Hemomancy.rloc("hemomancy/cleansing");
	public static final ResourceLocation ADV_ABSOLVED =
			Hemomancy.rloc("hemomancy/absolved");
	public static final ResourceLocation ADV_PURIFIED =
			Hemomancy.rloc("hemomancy/purified");

	// ── Clarity advancements ────────────────────────────────────────────────────
	public static final ResourceLocation ADV_CLARITY_AWAKENED =
			Hemomancy.rloc("hemomancy/clarity_awakened");
	public static final ResourceLocation ADV_PLEDGED_UNSTAINED =
			Hemomancy.rloc("hemomancy/pledged_unstained");
	public static final ResourceLocation ADV_DISCERNING =
			Hemomancy.rloc("hemomancy/discerning");
	public static final ResourceLocation ADV_VIGILANT =
			Hemomancy.rloc("hemomancy/vigilant");
	public static final ResourceLocation ADV_RESOLUTE_STAGE =
			Hemomancy.rloc("hemomancy/resolute_stage");
	public static final ResourceLocation ADV_ENLIGHTENED_SEEKER =
			Hemomancy.rloc("hemomancy/enlightened_seeker");

	private UnstainedAdvancementGranter() {}

	/**
	 * Grants the advancement identified by {@code id} to the player if it has
	 * not already been completed. Safe to call repeatedly.
	 *
	 * <p>Book-knowledge unlocks for this advancement are handled automatically
	 * by HutosLib's {@code BookDiscoveryEvents.onAdvancementEarned}, which
	 * consults the mappings registered in {@code BookEntryRegistry}. No
	 * explicit {@code LiberKnowledgeHelper} call is needed here.
	 *
	 * @param player The server-side player to receive the advancement.
	 * @param id     The ResourceLocation of the advancement to grant.
	 */
	public static void grantIfNotDone(ServerPlayer player, ResourceLocation id) {
		// Detached server players are used by semantic GameTests. Awarding an
		// advancement fires HutosLib discovery sync, which correctly assumes a
		// real logged-in client; the underlying progression is still testable
		// without manufacturing a network connection.
		if (player.connection == null) return;

		AdvancementHolder advancement = player.server.getAdvancements().get(id);
		if (advancement == null) return;

		AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
		if (progress.isDone()) return;

		for (String criterion : progress.getRemainingCriteria()) {
			player.getAdvancements().award(advancement, criterion);
		}
		if (ADV_ENLIGHTENED_SEEKER.equals(id) && progress.isDone()) {
			awardVestmentOfTheFinalMolt(player);
		}
	}

	private static void awardVestmentOfTheFinalMolt(ServerPlayer player) {
		ItemStack stack = new ItemStack(ItemInit.vestment_of_the_final_molt.get());
		if (!player.addItem(stack) && !stack.isEmpty()) {
			player.drop(stack, false);
		}
	}
}
