package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Static factory that produces {@link DialogueTree} variants for the Unstained
 * Guardian entity. The Guardian speaks only to Unstained matters — weapons,
 * hemolytic coating, and their post. Items outside their domain receive a flat,
 * neutral dismissal with no mention of other factions or NPCs.
 */
public final class GuardianDialogueTrees {

	private static final ResourceLocation GUARDIAN_ICON = Hemomancy
			.rloc("textures/entity/unstained_guardian/unstained_guardian.png");
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

	/**
	 * Dispatches to the appropriate inquiry tree based on the held item. Unstained
	 * weapons and the hemolytic vial get dedicated explanations; everything else
	 * gets the flat unknown response.
	 */
	public static DialogueTree forItem(ItemStack item, int entityId) {
		Item it = item.getItem();
		if (it == ItemInit.hemolytic_vial.get()) {
			return hemolyticInquiry(entityId);
		} else if (isUnstainedWeapon(it)) {
			return weaponInquiry(item, entityId);
		} else {
			return unknownItem(entityId);
		}
	}

	private static boolean isUnstainedWeapon(Item it) {
		return it == ItemInit.unstained_warhammer.get()
				|| it == ItemInit.silthmere_glaive.get()
				|| it == ItemInit.absolution_dagger.get()
				|| it == ItemInit.pale_silver_pickaxe.get()
				|| it == ItemInit.unstained_shield.get();
	}

	/**
	 * Explains an identified Unstained weapon — purpose, the no-blade doctrine, and
	 * how hemolytic coating applies.
	 */
	private static DialogueTree weaponInquiry(ItemStack item, int entityId) {
		Item it = item.getItem();
		String key;
		if (it == ItemInit.unstained_warhammer.get()) key = "warhammer";
		else if (it == ItemInit.silthmere_glaive.get()) key = "silthmere_glaive";
		else if (it == ItemInit.absolution_dagger.get()) key = "absolution_dagger";
		else if (it == ItemInit.pale_silver_pickaxe.get()) key = "pale_silver_pickaxe";
		else key = "unstained_shield";

		return DialogueTree.builder(SPEAKER, GUARDIAN_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.guardian.item_inquiry." + key + ".line1",
						"hemomancy.guardian.item_inquiry." + key + ".line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.guardian.option.leave", null, null)
				)))
				.build();
	}

	/** Explains hemolytic vial coating mechanics and which weapons accept it. */
	private static DialogueTree hemolyticInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, GUARDIAN_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.guardian.item_inquiry.hemolytic_vial.line1",
						"hemomancy.guardian.item_inquiry.hemolytic_vial.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.guardian.option.leave", null, null)
				)))
				.build();
	}

	/**
	 * Flat non-answer for items outside the Guardian's domain. No faction redirect,
	 * no mention of Harbinger NPCs.
	 */
	private static DialogueTree unknownItem(int entityId) {
		return DialogueTree.builder(SPEAKER, GUARDIAN_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.guardian.item_inquiry.unknown"
				), List.of(
						new DialogueOption("hemomancy.dialogue.guardian.option.leave", null, null)
				)))
				.build();
	}
}
