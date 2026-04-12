package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A single step in a dialogue tree. Contains the speaker's lines and
 * the options that the player can choose.
 *
 * @param id      Unique identifier within the tree (e.g. {@code "greeting"}).
 * @param lines   Ordered translation keys for the lines spoken by the entity.
 * @param options Clickable responses the player can pick.
 */
public record DialogueNode(String id, List<String> lines, List<DialogueOption> options) {

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeUtf(id);
		buf.writeInt(lines.size());
		for (String line : lines) buf.writeUtf(line);
		buf.writeInt(options.size());
		for (DialogueOption opt : options) opt.toNetwork(buf);
	}

	public static DialogueNode fromNetwork(FriendlyByteBuf buf) {
		String id = buf.readUtf();
		int lineCount = buf.readInt();
		List<String> lines = new ArrayList<>(lineCount);
		for (int i = 0; i < lineCount; i++) lines.add(buf.readUtf());
		int optCount = buf.readInt();
		List<DialogueOption> opts = new ArrayList<>(optCount);
		for (int i = 0; i < optCount; i++) opts.add(DialogueOption.fromNetwork(buf));
		return new DialogueNode(id, lines, opts);
	}
}
