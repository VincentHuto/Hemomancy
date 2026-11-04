package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Static factory that produces {@link DialogueTree} variants for the Unstained
 * Guardian entity. The Guardian speaks only to Unstained matters — weapons,
 * hemolytic coating, and their post. Items outside their domain receive a flat,
 * neutral dismissal with no mention of other factions or NPCs.
 */
public final class GuardianDialogueTrees {

	private static final ResourceLocation GUARDIAN_ICON = Hemomancy
			.rloc("textures/entity/npc/unstained/unstained_guardian/unstained_guardian.png");
	private static final String SPEAKER = "entity.hemomancy.unstained_guardian";

	private GuardianDialogueTrees() {}

	/**
	 * Terse at-post greeting when the player approaches empty-handed. Guards are
	 * not conversationalists.
	 */
	public static DialogueTree ambient(int entityId) {
		return DialogueTree.builder(SPEAKER, GUARDIAN_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.guardian.ambient.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.guardian.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.guardian.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.guardian.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.guardian.option.leave", null, null)
				)))
				.build();
	}

}
