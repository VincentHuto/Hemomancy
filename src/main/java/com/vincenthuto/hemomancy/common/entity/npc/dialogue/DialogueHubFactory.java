package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCards;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Builds the topic hub around existing dialogue nodes without changing event semantics. Root options become Quest Work
 * cards (an explicit quest flag or a mission id) or Conversation cards, one per option, with lore options carrying the
 * lore glyph and an unread dot. A lore option gated on a rapport tier the player hasn't reached is a locked lore card:
 * the glyph, a lock and the option's teaser, with nowhere to go. The Trade box is filled from the speaker's want cards
 * ({@link #withTrade}); a daily card the player hasn't seen today carries the unread dot.
 */
public final class DialogueHubFactory {
	private static final String NAMESPACE = "hemomancy";
	private static final String MET_VICAR = "hemomancy_met_harbinger_vicar";
	/** The glyph a lore card carries in the Conversation box. */
	public static final ResourceLocation LORE_ICON = Hemomancy.rloc("dialogue/icons/lore");
	public static final ResourceLocation TRADE_ICON = Hemomancy.rloc("dialogue/icons/trade");
	private static final String TRADE_TOPIC_PREFIX = "trade/";
	/** The topic that opens the Trade box on the speaker's refusal line. */
	public static final String TRADE_REFUSAL_TOPIC = TRADE_TOPIC_PREFIX + "refusal";
	/**
	 * Trade topics name no dialogue node: the Trade box draws its cards from the presentation's board, so the screen
	 * opens them through its own Trade view.
	 */
	public static final String TRADE_TARGET = "__trade";
	/** A locked lore card's target: it names no node, and the card can't be opened. */
	public static final String LOCKED_TARGET = "__locked";
	/** The suffix of a rapport-gated lore option's teaser key, shown on its locked card. */
	public static final String TEASER_SUFFIX = ".teaser";

	private DialogueHubFactory() {}

	public static DialogueTree decorate(DialogueTree tree, String npcId, ServerPlayer player) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		// The Vicar answers the question himself, so his own card stays while the player is new; a vow to the Church ends it.
		if (!"hermit".equals(npcId) && degree == 0 && !NpcTradeSides.is(npcId, NpcTradeSides.Side.CIRCUS)
				&& !ProgressionGates.isUnstained(player) && ("vicar".equals(npcId) || !hasMetVicar(player))) {
			// The pavilion takes D0 visitors on its own terms; the Circus is not the Vicar's errand.
			tree = addNewcomerGuidance(tree, npcId);
		}
		if (degree >= 7) {
			EnumArchonPath path = HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath();
			tree = withArchonAcknowledgment(tree, npcId, degree, path,
					HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_VESPER_DEFEATED));
		}
		int rapportTier = NpcTradeRules.tier(HemoCapabilityAccess.npcTrades(player).rapport(npcId));
		// A speaker who won't deal with this player has no rapport to offer them either.
		if (!NpcTradeSides.refuses(npcId, player)) tree = DialogueRapportLore.with(tree, npcId, rapportTier);
		return withTrade(decorate(tree, npcId, HemoCapabilityAccess.dialogueKnowledge(player), rapportTier),
				NpcTradeCards.build(player, npcId));
	}

	/**
	 * Records that the player has spoken with a Vicar, so other NPCs stop sending them to one. Set when the player talks
	 * to him, and kept under PlayerPersisted so a death does not bring the guidance back.
	 */
	public static void markMetVicar(ServerPlayer player) {
		CompoundTag root = player.getPersistentData();
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		durable.putBoolean(MET_VICAR, true);
		root.put(Player.PERSISTED_NBT_TAG, durable);
	}

	private static boolean hasMetVicar(ServerPlayer player) {
		return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(MET_VICAR);
	}

	/**
	 * Fills the Trade box: one {@code TRADE} topic per card, or a single one carrying the speaker's refusal line, so
	 * the box enables itself. The cards ride in the presentation beside the topics. A speaker with no cards and no
	 * refusal leaves the box empty.
	 */
	public static DialogueTree withTrade(DialogueTree tree, NpcTradeBoard board) {
		if (!board.present() || tree.presentation().mode() != DialogueScreenMode.TOPIC_HUB) return tree;
		List<DialogueTopic> topics = new ArrayList<>();
		if (board.refusalKey().isPresent()) {
			topics.add(new DialogueTopic(TRADE_REFUSAL_TOPIC, DialogueCategory.TRADE, "hemomancy.dialogue.category.trade",
					board.refusalKey().get(), TRADE_TARGET, TRADE_ICON, DialogueTopicState.AVAILABLE, null, null,
					null, false));
		}
		for (NpcTradeCard card : board.cards()) {
			ResourceLocation shown = card.give().isEmpty() ? null : card.give().getFirst().item();
			topics.add(new DialogueTopic(tradeTopicId(card.offerId()), DialogueCategory.TRADE, card.titleKey(),
					card.lineKey(), TRADE_TARGET, TRADE_ICON, DialogueTopicState.AVAILABLE, null, null, shown,
					card.fresh()));
		}
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), tree.nodes(),
				tree.entityId(), tree.theme(), tree.presentation().withTrade(board, topics));
	}

	/** The Trade topic id of a card; the card itself is {@code presentation.trade().card(offerId)}. */
	public static String tradeTopicId(ResourceLocation offerId) {
		return TRADE_TOPIC_PREFIX + offerId;
	}

	static DialogueTree withArchonAcknowledgment(DialogueTree tree, String npcId, int degree,
			EnumArchonPath path) {
		return withArchonAcknowledgment(tree, npcId, degree, path, false);
	}

	/** A Silence that has beaten Vesper but not drawn in his memory is sent to the brazier. */
	static DialogueTree withArchonAcknowledgment(DialogueTree tree, String npcId, int degree,
			EnumArchonPath path, boolean vesperDefeated) {
		if (degree < 7 || !Set.of("vicar", "alchemist", "artificer", "mnemonist").contains(npcId)) {
			return tree;
		}
		DialogueNode root = tree.getStartNode();
		if (root == null) return tree;
		String state = switch (path) {
			case NONE -> "undecided";
			case SILENT_PENDING -> vesperDefeated ? "silent_absorb" : "silent_pending";
			case SILENTIUM -> "silent_archon";
			case APOTHEOS_PENDING -> "apotheos_pending";
			case APOTHEOS -> "apotheos";
		};
		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		List<String> lines = new ArrayList<>(root.lines());
		lines.add("hemomancy.dialogue.ending." + npcId + "." + state);
		nodes.put(root.id(), new DialogueNode(root.id(), lines, root.options()));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	private static DialogueTree addNewcomerGuidance(DialogueTree tree, String npcId) {
		DialogueNode root = tree.getStartNode();
		if (root == null) return tree;
		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		List<DialogueOption> options = new ArrayList<>(root.options());
		if ("vicar".equals(npcId)) {
			options.addFirst(new DialogueOption("hemomancy.dialogue.newcomer.ask_path", "newcomer_path", null));
			nodes.put("newcomer_path", new DialogueNode("newcomer_path", List.of(
					"hemomancy.dialogue.newcomer.vicar_path"), List.of(
					new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null))));
		} else {
			options.addFirst(new DialogueOption("hemomancy.dialogue.newcomer.ask_vicar", "newcomer_vicar", null));
			nodes.put("newcomer_vicar", new DialogueNode("newcomer_vicar", List.of(
					"hemomancy.dialogue.newcomer." + npcId), List.of(
					new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null))));
		}
		nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	/** The hub for a player with no rapport with the speaker. */
	public static DialogueTree decorate(DialogueTree tree, String npcId, DialogueKnowledge knowledge) {
		return decorate(tree, npcId, knowledge, NpcTradeRules.STRANGER);
	}

	/** The hub for a player at rapport tier {@code rapportTier} with the speaker. */
	public static DialogueTree decorate(DialogueTree tree, String npcId, DialogueKnowledge knowledge,
			int rapportTier) {
		DialogueNode root = tree.getStartNode();
		if (root == null) return tree;

		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		List<DialogueTopic> topics = new ArrayList<>();
		int syntheticIndex = 0;
		boolean anyLocked = false;

		for (DialogueOption option : root.options()) {
			if (isLeave(option) || isInquiry(option) || option.presentation().isSpecialAction()) continue;
			DialogueCategory category = isQuest(option) ? DialogueCategory.QUESTS : DialogueCategory.CONVERSATION;
			boolean lore = category == DialogueCategory.CONVERSATION && option.presentation().lore();

			String slug = slug(option.nextNodeId() != null ? option.nextNodeId() : option.text());
			if (lore && lockedByRapport(option, rapportTier)) {
				topics.add(new DialogueTopic(DialogueTopic.conversationId(slug), DialogueCategory.CONVERSATION,
						option.text(), option.text() + TEASER_SUFFIX, LOCKED_TARGET, LORE_ICON,
						DialogueTopicState.LOCKED, null, null, null, false, DialogueAttention.NONE, true));
				anyLocked = true;
				continue;
			}
			String target = option.nextNodeId();
			if (target == null || option.eventId() != null) {
				target = "__hub_" + category.name().toLowerCase(Locale.ROOT) + "_" + syntheticIndex++;
				String promptKey = option.presentation().promptKey();
				nodes.put(target, new DialogueNode(target,
						List.of(promptKey == null ? "hemomancy.dialogue.action.confirm" : promptKey),
						List.of(option)));
			}
			String topicId = category == DialogueCategory.QUESTS ? "quests/" + slug : DialogueTopic.conversationId(slug);
			boolean unread = lore
					&& (knowledge == null || !knowledge.hasRead(readId(npcId, DialogueTopic.loreReadPath(slug))));
			DialogueAttention attention = unread ? DialogueAttention.NOTICE : option.presentation().attention();
			if (attention == DialogueAttention.NONE && category == DialogueCategory.QUESTS) {
				attention = missionAttention(option.eventId());
			}
			DialogueTopicState state = category == DialogueCategory.QUESTS
					? questState(option, nodes) : DialogueTopicState.AVAILABLE;
			topics.add(new DialogueTopic(topicId, category, option.text(), summaryKey(category), target,
					lore ? LORE_ICON : null, state, null, null, null, unread, attention, lore));
		}

		for (String nodeId : nodes.keySet()) {
			ResourceLocation itemId = DialogueItemInquiryNodes.inventoryItemId(nodeId);
			if (itemId == null) continue;
			topics.add(DialogueTopic.available(
					"inquiries/" + nodeId.substring(DialogueItemInquiryNodes.INVENTORY_NODE_PREFIX.length()),
					DialogueCategory.INQUIRIES, "hemomancy.dialogue.topic.inventory_item",
					"hemomancy.dialogue.topic.inventory_item.summary", nodeId).withItem(itemId));
		}

		if (anyLocked) {
			// The greeting lists its options too, so a locked line shows there as a disabled teaser.
			List<DialogueOption> rootOptions = new ArrayList<>(root.options().size());
			for (DialogueOption option : root.options()) {
				rootOptions.add(option.presentation().lore() && lockedByRapport(option, rapportTier)
						? new DialogueOption(option.text(), null, null,
								option.presentation().asDisabled(option.text() + TEASER_SUFFIX))
						: option);
			}
			nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), rootOptions));
		}

		ResourceLocation dialogueId = ResourceLocation.fromNamespaceAndPath(NAMESPACE, slug(npcId));
		DialoguePresentation presentation = DialoguePresentation.hub(dialogueId, dialogueId, topics);
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), presentation);
	}

	private static boolean lockedByRapport(DialogueOption option, int rapportTier) {
		return option.presentation().rapportTier() > rapportTier;
	}

	/** An option is quest work when it says so, or when its ids name a mission. */
	private static boolean isQuest(DialogueOption option) {
		if (option.presentation().quest()) return true;
		String value = (option.text() + " " + nullToEmpty(option.nextNodeId()) + " "
				+ nullToEmpty(option.eventId())).toLowerCase(Locale.ROOT);
		return containsAny(value, "quest", "assignment", "reward", "claim", "brief", "task", "report",
				"lesson", "directive", "diagnosis", "observance", "taxonomy", "bestiary",
				"fungal_survey", "voyager_introduction", "morphling_handling", "circus_referral",
				"deep_dark_commission", "phlegethontic_commission", "vagrant_inquiry", "woven_vessel");
	}

	private static boolean isInquiry(DialogueOption option) {
		String value = (option.text() + " " + nullToEmpty(option.nextNodeId())).toLowerCase(Locale.ROOT);
		return value.contains("item_hint") || value.contains("ask_about_item");
	}

	/**
	 * A card is a turn-in only when something can be handed in: its option sends a claim or reward event, or the node
	 * it opens does. A card that only says the reward is not ready stays active, and one that says it was claimed, or
	 * that a lesson was learned, is complete.
	 */
	private static DialogueTopicState questState(DialogueOption option, Map<String, DialogueNode> nodes) {
		String value = (option.text() + " " + nullToEmpty(option.nextNodeId()) + " "
				+ nullToEmpty(option.eventId())).toLowerCase(Locale.ROOT);
		if (option.eventId() != null) {
			if (containsAny(value, "claim", "turn_in", "reward")) return DialogueTopicState.TURN_IN;
			if (containsAny(value, "complete", "completed")) return DialogueTopicState.COMPLETE;
			return DialogueTopicState.ACTIVE;
		}
		if (containsAny(value, "unready", "not_ready")) return DialogueTopicState.ACTIVE;
		if (containsAny(value, "claimed", "complete", "learned")) return DialogueTopicState.COMPLETE;
		DialogueNode target = option.nextNodeId() == null ? null : nodes.get(option.nextNodeId());
		if (target != null) {
			for (DialogueOption next : target.options()) {
				String event = nullToEmpty(next.eventId()).toLowerCase(Locale.ROOT);
				if (next.presentation().enabled() && containsAny(event, "claim", "turn_in", "reward")) {
					return DialogueTopicState.TURN_IN;
				}
			}
		}
		return DialogueTopicState.ACTIVE;
	}

	private static DialogueAttention missionAttention(String eventId) {
		String event = nullToEmpty(eventId).toLowerCase(Locale.ROOT);
		if (UnstainedObservances.Observance.fromEventId(event) != null) return DialogueAttention.NONE;
		if (containsAny(event, "claim", "reward", "complete", "inspect", "turn_in", "report")) {
			return DialogueAttention.URGENT;
		}
		if (containsAny(event, "brief", "accept", "lesson", "referral", "task", "diagnosis", "directive",
				"starter", "mnemonist_blank_memory_recipe")) {
			return DialogueAttention.NOTICE;
		}
		return DialogueAttention.NONE;
	}

	private static boolean isLeave(DialogueOption option) {
		return option.nextNodeId() == null && option.eventId() == null
				&& option.text().toLowerCase(Locale.ROOT).contains("leave");
	}

	private static String summaryKey(DialogueCategory category) {
		return "hemomancy.dialogue.category." + category.name().toLowerCase(Locale.ROOT) + ".summary";
	}

	private static ResourceLocation readId(String npcId, String topicId) {
		return ResourceLocation.fromNamespaceAndPath(NAMESPACE, slug(npcId) + "/" + topicId);
	}

	private static String slug(String value) {
		String result = value == null ? "topic" : value.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9_./-]", "_").replaceAll("_+", "_");
		return result.length() > 80 ? result.substring(result.length() - 80) : result;
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private static boolean containsAny(String value, String... needles) {
		for (String needle : needles) if (value.contains(needle)) return true;
		return false;
	}
}
