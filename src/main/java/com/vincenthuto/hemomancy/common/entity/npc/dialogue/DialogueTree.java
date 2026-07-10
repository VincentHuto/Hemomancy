package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A complete dialogue tree that can be sent to the client.
 *
 * @param speakerName  Translation key for the entity's display name.
 * @param speakerIcon  Texture location for the entity portrait shown in the dialogue GUI.
 * @param startNodeId  The id of the first {@link DialogueNode} to display.
 * @param nodes        All nodes in the tree, keyed by their id.
 * @param entityId     The numeric entity id of the speaking entity (for display purposes).
 * @param theme        Visual theme for the dialogue screen (colour palette and background style).
 */
public record DialogueTree(
		String speakerName,
		ResourceLocation speakerIcon,
		String startNodeId,
		Map<String, DialogueNode> nodes,
		int entityId,
		DialogueTheme theme,
		DialoguePresentation presentation
) {
	public DialogueTree(String speakerName, ResourceLocation speakerIcon, String startNodeId,
			Map<String, DialogueNode> nodes, int entityId, DialogueTheme theme) {
		this(speakerName, speakerIcon, startNodeId, nodes, entityId, theme, DialoguePresentation.focused());
	}

	public DialogueNode getStartNode() {
		return nodes.get(startNodeId);
	}

	public DialogueNode getNode(String id) {
		return nodes.get(id);
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeUtf(speakerName);
		buf.writeResourceLocation(speakerIcon);
		buf.writeUtf(startNodeId);
		buf.writeInt(nodes.size());
		for (DialogueNode node : nodes.values()) node.toNetwork(buf);
		buf.writeInt(entityId);
		buf.writeInt(theme.ordinal());
		presentation.toNetwork(buf);
	}

	public static DialogueTree fromNetwork(FriendlyByteBuf buf) {
		String name = buf.readUtf();
		ResourceLocation icon = buf.readResourceLocation();
		String start = buf.readUtf();
		int count = buf.readInt();
		Map<String, DialogueNode> nodes = new LinkedHashMap<>(count);
		for (int i = 0; i < count; i++) {
			DialogueNode node = DialogueNode.fromNetwork(buf);
			nodes.put(node.id(), node);
		}
		int entityId = buf.readInt();
		DialogueTheme theme = DialogueTheme.fromOrdinal(buf.readInt());
		DialoguePresentation presentation = DialoguePresentation.fromNetwork(buf);
		return new DialogueTree(name, icon, start, nodes, entityId, theme, presentation);
	}

	// ── Builder ──

	public static Builder builder(String speakerName, ResourceLocation speakerIcon, int entityId) {
		return new Builder(speakerName, speakerIcon, entityId);
	}

	public static class Builder {
		private final String speakerName;
		private final ResourceLocation speakerIcon;
		private final int entityId;
		private DialogueTheme theme = DialogueTheme.BLOOD;
		private DialoguePresentation presentation = DialoguePresentation.focused();
		private String startNodeId;
		private final Map<String, DialogueNode> nodes = new LinkedHashMap<>();

		Builder(String speakerName, ResourceLocation speakerIcon, int entityId) {
			this.speakerName = speakerName;
			this.speakerIcon = speakerIcon;
			this.entityId = entityId;
		}

		public Builder theme(DialogueTheme theme) {
			this.theme = theme;
			return this;
		}

		public Builder presentation(DialoguePresentation presentation) {
			this.presentation = presentation == null ? DialoguePresentation.focused() : presentation;
			return this;
		}

		public Builder startNode(String id) {
			this.startNodeId = id;
			return this;
		}

		public Builder addNode(DialogueNode node) {
			nodes.put(node.id(), node);
			if (startNodeId == null) startNodeId = node.id();
			return this;
		}

		public DialogueTree build() {
			return new DialogueTree(speakerName, speakerIcon, startNodeId, nodes, entityId, theme, presentation);
		}
	}
}
