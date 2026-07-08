package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class HarbingerArtificerDialogueTrees {
	private static final ResourceLocation ARTIFICER_ICON = Hemomancy.rloc(
			"textures/entity/harbinger_artificer/harbinger_artificer.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_artificer";

	private HarbingerArtificerDialogueTrees() {
	}

	public static DialogueTree forState(int entityId, int degree, boolean activeBlood, boolean purifying,
			boolean clarity, boolean livingStaffBond) {
		if (purifying || clarity) {
			return refusal(entityId);
		}
		if (!activeBlood || degree <= 0) {
			return unawakened(entityId);
		}
		if (degree == 1) {
			return neophyte(entityId);
		}
		return workshop(entityId, degree, livingStaffBond);
	}

	private static DialogueTree refusal(int entityId) {
		return DialogueTree.builder(SPEAKER, ARTIFICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.artificer.refusal.line1",
						"hemomancy.artificer.refusal.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.artificer.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)
				)))
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree unawakened(int entityId) {
		return DialogueTree.builder(SPEAKER, ARTIFICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.artificer.unawakened.line1",
						"hemomancy.artificer.unawakened.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.artificer.option.who_are_you", "identity", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)
				)))
				.addNode(identityNode())
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree neophyte(int entityId) {
		return DialogueTree.builder(SPEAKER, ARTIFICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.artificer.neophyte.line1",
						"hemomancy.artificer.neophyte.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.artificer.option.who_are_you", "identity", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.armature_hint", "armature_hint", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)
				)))
				.addNode(identityNode())
				.addNode(new DialogueNode("armature_hint", List.of(
						"hemomancy.artificer.armature_hint.line1",
						"hemomancy.artificer.armature_hint.line2"
				), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null))))
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree workshop(int entityId, int degree, boolean livingStaffBond) {
		List<DialogueOption> options = new ArrayList<>();
		options.add(new DialogueOption("hemomancy.dialogue.artificer.option.who_are_you", "identity", null));
		if (degree >= 2) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.teach_armature", "armature", null));
		}
		if (degree >= 3) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.armor_path", "armor_forks", null));
		}
		if (livingStaffBond) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.living_grafts", "living_grafts", null));
		}
		if (degree >= 5) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.late_armature", "late_armature", null));
		}
		if (degree >= 7) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.monolithic_armature",
					"monolithic_armature", null));
		}
		options.add(new DialogueOption("hemomancy.dialogue.artificer.option.ask_about_item", "item_hint", null));
		options.add(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null));

		DialogueTree.Builder builder = DialogueTree.builder(SPEAKER, ARTIFICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.artificer.workshop.line1",
						"hemomancy.artificer.workshop.line2"
				), options))
				.addNode(identityNode());
		if (degree >= 2) {
			builder.addNode(armatureNode());
		}
		if (degree >= 3) {
			builder.addNode(armorForksNode());
		}
		if (livingStaffBond) {
			builder.addNode(livingGraftsNode());
		}
		if (degree >= 5) {
			builder.addNode(lateArmatureNode());
		}
		if (degree >= 7) {
			builder.addNode(monolithicArmatureNode());
		}
		builder.addNode(itemHintNode());
		return builder.build();
	}

	private static DialogueNode identityNode() {
		return new DialogueNode("identity", List.of(
				"hemomancy.artificer.identity.line1",
				"hemomancy.artificer.identity.line2",
				"hemomancy.artificer.identity.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode armatureNode() {
		return new DialogueNode("armature", List.of(
				"hemomancy.artificer.armature.line1",
				"hemomancy.artificer.armature.line2",
				"hemomancy.artificer.armature.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode armorForksNode() {
		return new DialogueNode("armor_forks", List.of(
				"hemomancy.artificer.forks.line1",
				"hemomancy.artificer.forks.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode livingGraftsNode() {
		return new DialogueNode("living_grafts", List.of(
				"hemomancy.artificer.grafts.line1",
				"hemomancy.artificer.grafts.line2",
				"hemomancy.artificer.grafts.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode lateArmatureNode() {
		return new DialogueNode("late_armature", List.of(
				"hemomancy.artificer.late_armature.line1",
				"hemomancy.artificer.late_armature.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode monolithicArmatureNode() {
		return new DialogueNode("monolithic_armature", List.of(
				"hemomancy.artificer.monolithic_armature.line1",
				"hemomancy.artificer.monolithic_armature.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode itemHintNode() {
		return new DialogueNode("item_hint", List.of(
				"hemomancy.artificer.item_hint"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}
}
