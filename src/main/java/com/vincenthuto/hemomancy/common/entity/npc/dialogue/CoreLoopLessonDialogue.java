package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adds a speaker's core-loop lessons ({@link CoreLoopLessons}) to their greeting: one lore card per lesson the player's
 * Degree reaches, opening a node with a question per branch. Every branch option carries the lesson's event, so asking
 * anything in a lesson opens its Liber pages; the card itself carries none, so the hub adds no confirm step.
 */
public final class CoreLoopLessonDialogue {
	private CoreLoopLessonDialogue() {
	}

	/** The speaker's lessons for a player whose blood is awake, who is not Unstained, at their Degree. */
	public static DialogueTree append(DialogueTree tree, String speaker, ServerPlayer player) {
		if (!ProgressionGates.isBloodActive(player) || ProgressionGates.isUnstained(player)) return tree;
		return append(tree, speaker, HemoCapabilityAccess.getPlayerDegreeNumber(player));
	}

	/** The lessons {@code speaker} offers at {@code degree}, before the greeting's item question and leave option. */
	public static DialogueTree append(DialogueTree tree, String speaker, int degree) {
		DialogueNode root = tree.getStartNode();
		if (root == null) return tree;
		List<CoreLoopLessons.Lesson> lessons = CoreLoopLessons.forSpeaker(speaker).stream()
				.filter(lesson -> lesson.offeredAt(degree)).toList();
		if (lessons.isEmpty()) return tree;
		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		List<DialogueOption> options = new ArrayList<>(root.options());
		int at = closingIndex(options);
		for (CoreLoopLessons.Lesson lesson : lessons) {
			options.add(at++, new DialogueOption(lesson.rootOption(), lesson.node(), null).withLore());
			List<DialogueOption> questions = new ArrayList<>();
			for (CoreLoopLessons.Branch branch : lesson.branches()) {
				questions.add(new DialogueOption(lesson.branchOption(branch), lesson.node(branch), lesson.event()));
				nodes.put(lesson.node(branch), new DialogueNode(lesson.node(branch), lesson.lineKeys(branch), List.of(
						new DialogueOption(lesson.backOption(), lesson.node(), null),
						new DialogueOption(lesson.leaveOption(), null, null))));
			}
			questions.add(new DialogueOption(lesson.leaveOption(), null, null));
			nodes.put(lesson.node(), new DialogueNode(lesson.node(), lesson.lineKeys(), questions));
		}
		nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	/** Where the greeting's closing run starts: its item question and leave option stay last. */
	private static int closingIndex(List<DialogueOption> options) {
		int at = options.size();
		while (at > 0 && closes(options.get(at - 1))) at--;
		return at;
	}

	private static boolean closes(DialogueOption option) {
		String text = (option.text() + " " + (option.nextNodeId() == null ? "" : option.nextNodeId()))
				.toLowerCase(Locale.ROOT);
		boolean leave = option.nextNodeId() == null && option.eventId() == null && text.contains("leave");
		return leave || text.contains("item_hint") || text.contains("ask_about_item");
	}
}
