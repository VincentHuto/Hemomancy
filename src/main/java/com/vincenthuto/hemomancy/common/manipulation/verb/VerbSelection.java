package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionWheelRules;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.CellHandFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * The verb key (CL-2). With an empty main hand it conjures the cell hand of the active verb; sneaking with a cell hand
 * it dispels the hand; otherwise it switches the active verb, and a cell hand in the main hand turns to match. The
 * Living Staff channels whichever verb is active.
 */
public final class VerbSelection {
	private VerbSelection() {
	}

	/** One press of the verb key. */
	public static void press(ServerPlayer player) {
		if (!mayUse(player)) return;
		ItemStack held = player.getMainHandItem();
		if (held.isEmpty()) {
			conjureHand(player);
			return;
		}
		if (player.isShiftKeyDown() && CellHandFormHelper.isCellHandForm(held)) {
			SculptingSessions.end(player, false);
			ConjurationManip.dispelHeld(player);
			return;
		}
		toggle(player);
	}

	/**
	 * A verb picked on the inner wheel: the plain verb. The verb becomes active and the selected memory is put down,
	 * so nothing shapes it; an empty main hand takes that verb's cell hand. The verb key turns the verb and keeps the
	 * memory.
	 */
	public static void choose(ServerPlayer player, Verb verb) {
		if (!mayUse(player)) return;
		SculptingSessions.end(player, false);
		HemoCapabilityAccess.getKnownManipulations(player).setSelectedManip(BloodManipulation.BLANK);
		set(player, verb);
		player.displayClientMessage(Component.translatable("message.hemomancy.verb.switched",
				Component.translatable(verb.langKey())).withStyle(ChatFormatting.DARK_RED), true);
		if (player.getMainHandItem().isEmpty()) conjureHand(player);
	}

	private static boolean mayUse(ServerPlayer player) {
		if (PossessionWheelRules.refusesManipulation(HemoCapabilityAccess.isPossessing(player), false)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.possession.cannot_cast")
					.withStyle(ChatFormatting.DARK_GRAY), true);
			return false;
		}
		if (Paralysis.isParalyzed(player)
				|| UnstainedAccessRules.blocksKnownBloodPowerUse(HemoCapabilityAccess.getUnstainedProgress(player))) {
			return false;
		}
		if (!HemoCapabilityAccess.getBloodVolume(player).isActive()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.use_manip_key_packet.you_lack_the_skill_to_manifest")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		return true;
	}

	/** Switches the active verb; a cell hand in the main hand turns to match. */
	public static Verb toggle(ServerPlayer player) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		Verb next = known.getActiveVerb().other();
		set(player, next);
		player.displayClientMessage(Component.translatable("message.hemomancy.verb.switched",
				Component.translatable(next.langKey())).withStyle(ChatFormatting.DARK_RED), true);
		return next;
	}

	/** Makes {@code verb} active (a held cell hand follows) and tells the owner's client. */
	public static void set(ServerPlayer player, Verb verb) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		if (known.getActiveVerb() != verb) SculptingSessions.end(player, false);
		known.setActiveVerb(verb);
		CellHandFormHelper.applyVerb(player, verb);
		KnownManipulationEvents.syncPlayerEvent(player);
	}

	/**
	 * Conjures the active verb's cell hand into the empty main hand through that verb's own conjuration (its price,
	 * cooldown and gates). Refuses with a line when the verb is not yet known.
	 */
	public static boolean conjureHand(ServerPlayer player) {
		Verb verb = VerbChannel.activeVerb(player);
		String name = verb == Verb.ABSORB ? ManipulationEquipHelper.BLOOD_ABSORPTION : ManipulationEquipHelper.BLOOD_PROJECTION;
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		boolean knows = known.getManipList().stream().anyMatch(manip -> manip != null && name.equals(manip.getName()));
		BloodManipulation conjuration = ManipulationInit.getByName(name);
		if (!knows || conjuration == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.verb.unknown",
					Component.translatable(verb.langKey())).withStyle(ChatFormatting.RED), true);
			return false;
		}
		ItemStack beforeMain = player.getMainHandItem().copy();
		ItemStack beforeOff = player.getOffhandItem().copy();
		boolean cast = conjuration.tryPerformAction(player, player.level(), player.getMainHandItem(), player.blockPosition(), 0.0F);
		LivingStaffWeaponFormHelper.syncMorph(player, beforeMain, beforeOff);
		return cast && CellHandFormHelper.isCellHandForm(player.getMainHandItem());
	}
}
