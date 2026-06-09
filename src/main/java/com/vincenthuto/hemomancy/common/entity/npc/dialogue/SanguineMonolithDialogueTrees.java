package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Static factory producing {@link DialogueTree} variants for the Sanguine
 * Monolith block. Dialogue varies based on the player's current initiatory
 * degree (5 = Illuminatus through 7+ = Archon).
 */
public final class SanguineMonolithDialogueTrees {

	private static final ResourceLocation MONOLITH_ICON = Hemomancy.rloc("textures/entity/model_sanguine_monolith.png");
	private static final String SPEAKER = "hemomancy.monolith.lodestone_name";

	/** Sentinel used in place of an entity ID for this block-based speaker. */
	public static final int BLOCK_ENTITY_ID = -1;

	private SanguineMonolithDialogueTrees() {}

	/**
	 * Returns the appropriate dialogue tree for the given degree.
	 * Assumes the player is at least degree 4 (Adept).
	 */
	public static DialogueTree forDegree(int degree) {
		return switch (degree) {
			case 4 -> adept();
			case 5 -> illuminatus();
			case 6 -> sanctified();
			default -> archon();
		};
	}

	/** Player is below the minimum degree — the monolith is silent. */
	public static DialogueTree unworthy() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.unworthy"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 4 — Adept. Guidance toward the Sainted Mausoleums; first uncanny hint of awareness. */
	public static DialogueTree adept() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.guidance.adept.line1",
						"hemomancy.monolith.guidance.adept.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.what_are_you", "what_are_you", null),
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.addNode(new DialogueNode("what_are_you", List.of(
						"hemomancy.monolith.what_are_you.adept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.build();
	}

	/** Degree 5 — Illuminatus. The stone is warm. The lodestone speaks with a hint of self-awareness. */
	public static DialogueTree illuminatus() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.guidance.illuminatus.line1",
						"hemomancy.monolith.guidance.illuminatus.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.what_are_you", "what_are_you", null),
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.addNode(new DialogueNode("what_are_you", List.of(
						"hemomancy.monolith.what_are_you.illuminatus"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.build();
	}

	/** Degree 6 — Sanctified. The stone no longer feels like stone. The lodestone discloses what it contains. */
	public static DialogueTree sanctified() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.guidance.sanctified.line1",
						"hemomancy.monolith.guidance.sanctified.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.what_are_you", "what_are_you", null),
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.addNode(new DialogueNode("what_are_you", List.of(
						"hemomancy.monolith.what_are_you.sanctified"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.build();
	}

	/**
	 * Degree 7+ — Archon. The lodestone speaks with intimate recognition; pressing
	 * further reveals the shatter warning. Two interactions with the block trigger
	 * the actual shatter (handled by {@code SanguineMonolithBlockEntity}).
	 */
	public static DialogueTree archon() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.guidance.archon.line1",
						"hemomancy.monolith.guidance.archon.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.what_are_you", "what_are_you", null),
						new DialogueOption("hemomancy.dialogue.monolith.option.press_further", "press_again", null),
						new DialogueOption("hemomancy.dialogue.monolith.option.leave", null, null)
				)))
				.addNode(new DialogueNode("what_are_you", List.of(
						"hemomancy.monolith.what_are_you.archon"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.press_further", "press_again", null),
						new DialogueOption("hemomancy.dialogue.monolith.option.leave", null, null)
				)))
				.addNode(new DialogueNode("press_again", List.of(
						"hemomancy.monolith.archon.press_again"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.leave", null, null)
				)))
				.build();
	}

	public static DialogueTree itemInquiry(ItemStack item, int degree) {
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
		return ItemInquiryRegistry.INSTANCE
				.resolve("monolith", itemId, degree, 0f)
				.map(lines -> basicItemInquiry(lines.toArray(String[]::new)))
				.orElseGet(() -> basicItemInquiry("hemomancy.monolith.item_inquiry.unknown"));
	}

	private static DialogueTree basicItemInquiry(String... lines) {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(lines), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.leave", null, null)
				)))
				.build();
	}
}
