package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ProgressionDialogueNpc;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.gametest.CapturedPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Opening a speaker's dialogue the way a right click does, and reading what was served. The copies of
 * {@code NpcTradeGameTests#open} and {@code #topicTexts} live here; that class is left as it is.
 */
public final class Dialogue {
	private Dialogue() {
	}

	/** Interacts like a right click and returns the dialogue the speaker opened; fails when none opened. */
	public static DialogueTree open(GameTestHelper helper, CapturedPlayer captured, Entity speaker, String label) {
		Optional<DialogueTree> tree = tryOpen(captured, speaker);
		helper.assertTrue(tree.isPresent(), label + " opens a dialogue");
		return tree.get();
	}

	/** Interacts like a right click; empty when the speaker opened nothing (a dissolving Hermit, a refusal). */
	public static Optional<DialogueTree> tryOpen(CapturedPlayer captured, Entity speaker) {
		int before = captured.sent(OpenDialoguePacket.class).size();
		speaker.interact(captured.player, InteractionHand.MAIN_HAND);
		List<OpenDialoguePacket> opened = captured.sent(OpenDialoguePacket.class);
		return opened.size() > before ? Optional.of(opened.getLast().tree()) : Optional.empty();
	}

	/** The title keys of a hub box's cards, in order. */
	public static List<String> topicTexts(DialogueTree tree, DialogueCategory category) {
		return tree.presentation().topics(category).stream().map(DialogueTopic::titleKey).toList();
	}

	/** The sigil a progression speaker shows over its head, as the player would see it now. Reading it changes nothing. */
	public static DialogueAttention attention(Entity npc, ServerPlayer player) {
		if (!(npc instanceof ProgressionDialogueNpc speaker)) return DialogueAttention.NONE;
		return speaker.progressionAttention(player);
	}

	/**
	 * The translation key a served line names: a {@code @translate:} line's key without its arguments, or the line
	 * itself. Empty for a {@code @literal:} line, which carries text rather than a key.
	 */
	public static Optional<String> lineKey(String line) {
		if (line == null || line.isEmpty()) return Optional.empty();
		if (line.startsWith(DialogueNode.LITERAL_PREFIX)) return Optional.empty();
		if (line.startsWith(DialogueNode.TRANSLATE_PREFIX)) {
			String rest = line.substring(DialogueNode.TRANSLATE_PREFIX.length());
			int separator = rest.indexOf(DialogueNode.ARGUMENT_SEPARATOR);
			return Optional.of(separator < 0 ? rest : rest.substring(0, separator));
		}
		return Optional.of(line);
	}
}
