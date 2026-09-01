package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/** Builds presentation metadata around existing dialogue nodes without changing event semantics. */
public final class DialogueHubFactory {
	private static final String NAMESPACE = "hemomancy";

	private DialogueHubFactory() {}

	public static DialogueTree decorate(DialogueTree tree, String npcId, ServerPlayer player) {
		return decorate(tree, npcId, player.getData(HemoAttachmentTypes.DIALOGUE_KNOWLEDGE));
	}

	public static DialogueTree decorate(DialogueTree tree, String npcId, DialogueKnowledge knowledge) {
		DialogueNode root = tree.getStartNode();
		if (root == null) return tree;

		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		List<DialogueTopic> topics = new ArrayList<>();
		List<DialogueOption> conversationOptions = new ArrayList<>();
		int syntheticIndex = 0;

		for (DialogueOption option : root.options()) {
			if (isLeave(option) || isInquiry(option)) continue;
			DialogueCategory category = classify(option);
			if (category == DialogueCategory.CONVERSATION) {
				conversationOptions.add(option);
				continue;
			}

			String slug = slug(option.nextNodeId() != null ? option.nextNodeId() : option.text());
			String target = option.nextNodeId();
			if (target == null) {
				target = "__hub_" + category.name().toLowerCase(Locale.ROOT) + "_" + syntheticIndex++;
				nodes.put(target, new DialogueNode(target, List.of(), List.of(option)));
			}
			String topicId = category.name().toLowerCase(Locale.ROOT) + "/" + slug;
			boolean unread = category == DialogueCategory.LORE
					&& (knowledge == null || !knowledge.hasRead(readId(npcId, topicId)));
			DialogueAttention attention = unread ? DialogueAttention.NOTICE : option.presentation().attention();
			if (attention == DialogueAttention.NONE && category == DialogueCategory.QUESTS) {
				attention = missionAttention(option.eventId());
			}
			DialogueTopicState state = category == DialogueCategory.QUESTS
					? questState(option) : DialogueTopicState.AVAILABLE;
			topics.add(new DialogueTopic(topicId, category, option.text(), summaryKey(category), target,
					null, state, null, null, null, unread, attention));
		}

		if (!conversationOptions.isEmpty()) {
			String target = "__hub_conversation";
			nodes.put(target, new DialogueNode(target, List.of(), List.copyOf(conversationOptions)));
			topics.add(DialogueTopic.available("conversation/speak_freely", DialogueCategory.CONVERSATION,
					"hemomancy.dialogue.topic.speak_freely", summaryKey(DialogueCategory.CONVERSATION), target));
		}

		for (String nodeId : nodes.keySet()) {
			ResourceLocation itemId = DialogueItemInquiryNodes.inventoryItemId(nodeId);
			if (itemId == null) continue;
			topics.add(DialogueTopic.available(
					"inquiries/" + nodeId.substring(DialogueItemInquiryNodes.INVENTORY_NODE_PREFIX.length()),
					DialogueCategory.INQUIRIES, "hemomancy.dialogue.topic.inventory_item",
					"hemomancy.dialogue.topic.inventory_item.summary", nodeId).withItem(itemId));
		}

		ResourceLocation dialogueId = ResourceLocation.fromNamespaceAndPath(NAMESPACE, slug(npcId));
		DialoguePresentation presentation = DialoguePresentation.hub(dialogueId, dialogueId, topics);
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), presentation);
	}

	private static DialogueCategory classify(DialogueOption option) {
		String value = (option.text() + " " + nullToEmpty(option.nextNodeId()) + " "
				+ nullToEmpty(option.eventId())).toLowerCase(Locale.ROOT);
		if (containsAny(value, "quest", "assignment", "reward", "claim", "brief", "task", "report",
				"lesson", "directive", "diagnosis", "observance", "taxonomy", "bestiary")) {
			return DialogueCategory.QUESTS;
		}
		if (containsAny(value, "about", "lore", "history", "order", "rite", "ritual", "machine",
				"degree", "truth", "explain", "what_", "why_", "teach", "blood_crafting", "annetta")) {
			return DialogueCategory.LORE;
		}
		return DialogueCategory.CONVERSATION;
	}

	private static boolean isInquiry(DialogueOption option) {
		String value = (option.text() + " " + nullToEmpty(option.nextNodeId())).toLowerCase(Locale.ROOT);
		return value.contains("item_hint") || value.contains("ask_about_item");
	}

	private static DialogueTopicState questState(DialogueOption option) {
		String value = (option.text() + " " + nullToEmpty(option.nextNodeId()) + " "
				+ nullToEmpty(option.eventId())).toLowerCase(Locale.ROOT);
		if (containsAny(value, "claim", "turn_in", "reward")) return DialogueTopicState.TURN_IN;
		if (containsAny(value, "complete", "completed")) return DialogueTopicState.COMPLETE;
		return DialogueTopicState.ACTIVE;
	}

	private static DialogueAttention missionAttention(String eventId) {
		String event = nullToEmpty(eventId).toLowerCase(Locale.ROOT);
		if (UnstainedObservances.Observance.fromEventId(event) != null) return DialogueAttention.NONE;
		if (containsAny(event, "claim", "reward", "complete", "inspect", "turn_in", "report")) {
			return DialogueAttention.URGENT;
		}
		if (containsAny(event, "brief", "accept", "lesson", "referral", "task", "diagnosis", "directive",
				"starter")) {
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
