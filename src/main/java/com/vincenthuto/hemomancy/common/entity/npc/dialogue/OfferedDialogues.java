package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The dialogue tree each player was last sent, so a chosen option can be checked against what the server actually
 * offered: a memo is noted only when the open tree lists its event (RT-18). Session state; cleared through
 * {@code TransientServerStateEvents}.
 */
public final class OfferedDialogues {
	private static final Map<UUID, DialogueTree> OFFERED = new HashMap<>();

	private OfferedDialogues() {
	}

	public static void record(ServerPlayer player, DialogueTree tree) {
		OFFERED.put(player.getUUID(), tree);
	}

	/** The last tree sent to {@code player} speaks for {@code entityId} and lists an option posting {@code eventId}. */
	public static boolean offers(ServerPlayer player, int entityId, String eventId) {
		DialogueTree tree = OFFERED.get(player.getUUID());
		return tree != null && tree.entityId() == entityId && tree.nodes().values().stream()
				.flatMap(node -> node.options().stream())
				.anyMatch(option -> eventId.equals(option.eventId()));
	}

	/**
	 * The last tree sent to {@code player} speaks for {@code entityId} and has a lore topic whose read id is
	 * {@code topicId} ({@code <dialogue id>/<topic read path>}, as the dialogue screen reports it).
	 */
	public static boolean offersTopic(ServerPlayer player, int entityId, net.minecraft.resources.ResourceLocation topicId) {
		DialogueTree tree = OFFERED.get(player.getUUID());
		if (tree == null || tree.entityId() != entityId || tree.presentation() == null) return false;
		net.minecraft.resources.ResourceLocation dialogueId = tree.presentation().dialogueId();
		return tree.presentation().topics().stream().anyMatch(topic -> topicId.equals(
				net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(dialogueId.getNamespace(),
						dialogueId.getPath() + "/" + topic.readPath())));
	}

	public static void logout(ServerPlayer player) {
		OFFERED.remove(player.getUUID());
	}

	public static void clearSessionState() {
		OFFERED.clear();
	}
}
