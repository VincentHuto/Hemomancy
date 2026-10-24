package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * A single step in a dialogue tree. Contains the speaker's lines and
 * the options that the player can choose.
 *
 * @param id      Unique identifier within the tree (e.g. {@code "greeting"}).
 * @param lines   Ordered translation keys for the lines spoken by the entity.
 * @param options Clickable responses the player can pick.
 */
public record DialogueNode(String id, List<String> lines, List<DialogueOption> options) {
	public static final String LITERAL_PREFIX = "@literal:";
	public static final String TRANSLATE_PREFIX = "@translate:";
	public static final String ARGUMENT_SEPARATOR = "\u001F";

	public static String literal(String value) {
		return LITERAL_PREFIX + value;
	}

	/** A line that the client translates with plain-text arguments: {@code key} and {@code args} joined for the wire. */
	public static String translate(String key, String... args) {
		StringBuilder line = new StringBuilder(TRANSLATE_PREFIX).append(key);
		for (String arg : args) line.append(ARGUMENT_SEPARATOR).append(arg);
		return line.toString();
	}

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
