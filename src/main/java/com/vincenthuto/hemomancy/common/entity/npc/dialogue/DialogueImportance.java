package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.List;

/**
 * What a hub's Important tab holds: the greeting's special actions, and the cards that need the player now (a turn-in
 * ready to claim, or a card carrying a sigil). Unread lore is never important; it keeps its dot in Conversation. A hub
 * always opens on its four boxes, and the tab is how anything pressing gets in front of the player.
 */
public final class DialogueImportance {
	private DialogueImportance() {
	}

	/** The greeting's options in the special-action style, by their place among its options. */
	public static List<Integer> specialActions(DialogueTree tree) {
		DialogueNode root = tree.getStartNode();
		List<Integer> actions = new ArrayList<>();
		if (root == null) return actions;
		for (int i = 0; i < root.options().size(); i++) {
			if (root.options().get(i).presentation().isSpecialAction()) actions.add(i);
		}
		return actions;
	}

	/** The cards worth raising, in box then card order. */
	public static List<DialogueTopic> topics(DialoguePresentation presentation) {
		List<DialogueTopic> topics = new ArrayList<>();
		for (DialogueTopic topic : presentation.topics()) {
			if (topic.lore() || !topic.state().enabled()) continue;
			if (topic.attention() != DialogueAttention.NONE || topic.state() == DialogueTopicState.TURN_IN) topics.add(topic);
		}
		return topics;
	}

	/** How many things the tab holds; none means the tab is not shown. */
	public static int count(DialogueTree tree) {
		return specialActions(tree).size() + topics(tree.presentation()).size();
	}
}
