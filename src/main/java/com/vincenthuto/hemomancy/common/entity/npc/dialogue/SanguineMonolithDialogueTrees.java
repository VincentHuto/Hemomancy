package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeRules.ClaimState;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Static factory producing {@link DialogueTree} variants for the Sanguine Monolith block. From the fifth Degree it opens
 * the same four-box hub as the Vicar ({@link #hub}): counsel and the D7 steps under Quest Work, a lore card per question
 * under Conversation, the player's carried items under Inquiries, and its blood prices under Trade. The voice changes
 * with Degree (5 = Illuminatus through 7+ = Archon), and the portrait warms with it.
 */
public final class SanguineMonolithDialogueTrees {

	private static final ResourceLocation COLD_ICON = Hemomancy.rloc("textures/entity/sanguine_monolith_cold.png");
	private static final ResourceLocation SKIN_ICON = Hemomancy.rloc("textures/entity/sanguine_monolith_skin.png");
	private static final ResourceLocation FEVER_ICON = Hemomancy.rloc("textures/entity/sanguine_monolith_fever.png");
	private static final String SPEAKER = "hemomancy.monolith.lodestone_name";
	private static final String OPTION = "hemomancy.dialogue.monolith.option.";
	private static final String LEAVE = OPTION + "leave";
	private static final String BACK = "hemomancy.dialogue.ui.back";
	private static final String ROOT = "root";
	private static final String CORNERSTONE_NODE = "armature_cornerstone";

	/** The stone's speaker id: its {@code dialogue_inquiry/} and {@code npc_trade/} folder and its rapport key. */
	public static final String SPEAKER_ID = "monolith";
	/** Sentinel used in place of an entity ID for this block-based speaker. */
	public static final int BLOCK_ENTITY_ID = -1;
	public static final String EVENT_CORNERSTONE = "monolith_cornerstone_claim";
	public static final String EVENT_SHATTER = "monolith_press_further";

	private SanguineMonolithDialogueTrees() {}

	/** A lore card: its option text is {@code <OPTION><slug>} and its answer is {@code hemomancy.monolith.lore.<slug>.line1..n}. */
	private record Lore(String slug, int lines) {
		DialogueOption option() {
			return new DialogueOption(OPTION + slug, slug, null).withLore();
		}

		DialogueNode node() {
			List<String> keys = new ArrayList<>(lines);
			for (int i = 1; i <= lines; i++) keys.add("hemomancy.monolith.lore." + slug + ".line" + i);
			return answer(slug, keys);
		}
	}

	/**
	 * What the stone opens for a player of the fifth Degree or above, whatever is in hand: the same decorators as the
	 * Vicar, so a held item is one card among the Inquiries instead of the whole conversation.
	 */
	public static DialogueTree hub(ServerPlayer player, int degree) {
		DialogueTree tree = DialogueItemInquiryNodes.withInventoryItemInquiries(forPlayer(player, degree), player, SPEAKER_ID);
		return DialogueHubFactory.decorate(tree, SPEAKER_ID, player);
	}

	private static DialogueTree forPlayer(ServerPlayer player, int degree) {
		if (degree >= 7) {
			return archon(HemoCapabilityAccess.stationUpgrades(player).claimState(player,
					StationUpgradeCatalog.get(UpgradeStation.ARMATURE, 2)),
					ProgressionGates.hasDegree(player, 8) || ChamberOfWillManager.get(player.server).hasGateway(player.getUUID()));
		}
		CovenantProgress covenant = CovenantProgress.of(player);
		return degree == 6 ? sanctified(covenant) : illuminatus(covenant);
	}

	/** Player is below the minimum degree — the monolith is silent. */
	public static DialogueTree unworthy() {
		return DialogueTree.builder(SPEAKER, COLD_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode(ROOT, List.of(
						"hemomancy.monolith.unworthy"
				), List.of(
						new DialogueOption(LEAVE, null, null)
				)))
				.build();
	}

	/**
	 * Degree 5 — Illuminatus, the player's first Vicar-level degree. The stone is warm. It gives the Lodge's
	 * official description and a hint of self-awareness, in the third person.
	 */
	public static DialogueTree illuminatus(CovenantProgress covenant) {
		return stone(COLD_ICON, "illuminatus", counsel(covenant.steps(5)), List.of(), List.of(), null,
				new Lore("what_it_measures", 2), new Lore("who_stood_here", 2), new Lore("why_now", 2));
	}

	/** Degree 6 — Sanctified. The stone no longer feels like stone. The lodestone discloses what it contains. */
	public static DialogueTree sanctified(CovenantProgress covenant) {
		return stone(SKIN_ICON, "sanctified", counsel(covenant.steps(6)), List.of(), List.of(), null,
				new Lore("where_found", 2), new Lore("does_it_know_me", 2), new Lore("the_others", 2));
	}

	/**
	 * Degree 7+ — Archon. The lodestone speaks with intimate recognition; pressing further, the hub's glowing button,
	 * reveals the shatter warning, and a second deliberate choice triggers it through the validated block conversation.
	 * A player who has already carried a Seed out of a stone can still press this one, but it is no longer their next step.
	 */
	public static DialogueTree archon(ClaimState cornerstone, boolean pastTheSeed) {
		String node = cornerstone == ClaimState.CLAIMED ? CORNERSTONE_NODE + "_claimed"
				: cornerstone == ClaimState.READY ? CORNERSTONE_NODE : CORNERSTONE_NODE + "_unready";
		List<DialogueOption> quests = List.of(new DialogueOption(OPTION + "anchor_armature", node, null).withQuest());
		List<DialogueNode> nodes = new ArrayList<>(List.of(new DialogueNode("press_again", List.of(
				"hemomancy.monolith.archon.press_again"
		), List.of(
				new DialogueOption(OPTION + "press_further", null, EVENT_SHATTER, DialogueOptionPresentation.specialAction()),
				new DialogueOption(BACK, ROOT, null)
		))));
		if (cornerstone == ClaimState.CLAIMED) {
			nodes.add(answer(node, List.of("hemomancy.dialogue.event.monolith_cornerstone_known")));
		} else {
			nodes.add(new DialogueNode(node, List.of(
					"hemomancy.monolith.armature_cornerstone.line1",
					"hemomancy.monolith.armature_cornerstone.line2"
			), List.of(
					new DialogueOption(OPTION + "accept_cornerstone", CORNERSTONE_NODE + "_given", EVENT_CORNERSTONE),
					new DialogueOption(BACK, ROOT, null)
			)));
			nodes.add(new DialogueNode(CORNERSTONE_NODE + "_given", List.of(
					"hemomancy.monolith.armature_cornerstone.given"
			), List.of(
					new DialogueOption(LEAVE, null, null)
			)));
		}
		return stone(FEVER_ICON, "archon", counsel(List.of(pastTheSeed ? "past_the_seed" : "press_further")), quests, nodes,
				new DialogueOption(OPTION + "press_further", "press_again", null, DialogueOptionPresentation.specialAction()),
				new Lore("ripening", 2), new Lore("what_opens", 2), new Lore("what_remains", 2));
	}

	private static List<String> counsel(List<String> steps) {
		return steps.stream().map(step -> "hemomancy.monolith.counsel." + step).toList();
	}

	/** An answer with one way out, back to the hub. */
	private static DialogueNode answer(String id, List<String> lines) {
		return new DialogueNode(id, lines, List.of(new DialogueOption(BACK, ROOT, null)));
	}

	/**
	 * One Degree's tree: greeting, counsel, then {@code quests}, the lore cards, the item inquiry, the special
	 * {@code action} if there is one, and the way out.
	 */
	private static DialogueTree stone(ResourceLocation icon, String title, List<String> counsel,
			List<DialogueOption> quests, List<DialogueNode> questNodes, @Nullable DialogueOption action, Lore... lore) {
		List<DialogueOption> options = new ArrayList<>();
		options.add(new DialogueOption(OPTION + "counsel", "counsel", null).withQuest());
		options.addAll(quests);
		// A node per Degree, because the card's read mark follows the node and the answer escalates.
		String whatAreYou = "what_are_you_" + title;
		options.add(new DialogueOption(OPTION + "what_are_you", whatAreYou, null).withLore());
		for (Lore line : lore) options.add(line.option());
		options.add(new DialogueOption(OPTION + "ask_about_item", "item_hint", null));
		if (action != null) options.add(action);
		options.add(new DialogueOption(LEAVE, null, null));

		DialogueTree.Builder builder = DialogueTree.builder(SPEAKER, icon, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode(ROOT, List.of(
						"hemomancy.monolith.guidance." + title + ".line1",
						"hemomancy.monolith.guidance." + title + ".line2"
				), options))
				.addNode(answer("counsel", counsel))
				.addNode(answer(whatAreYou, List.of("hemomancy.monolith.what_are_you." + title)))
				.addNode(answer("item_hint", List.of("hemomancy.monolith.item_hint")));
		questNodes.forEach(builder::addNode);
		for (Lore line : lore) builder.addNode(line.node());
		return builder.build();
	}
}
