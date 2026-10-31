package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.AntecedentDialogue;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CircusSchoolDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.StationUpgradeDialogue;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

/**
 * Routes each {@link DialogueEvent} (a chosen dialogue option) to the one handler that owns it. The table is tried in
 * order and the first route that takes the event ends the search. Shared routes come first (station claims, the
 * antecedent, clinical lessons, Annetta, the circus school and Ringmaster), then each NPC's own events. An event no
 * route takes does nothing.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class DialogueEventRouter {
	/** One dialogue route: handles the event and returns true when it is its own, or returns false. */
	@FunctionalInterface
	interface DialogueRoute {
		boolean handle(DialogueEvent event);
	}

	private static final List<DialogueRoute> ROUTES = List.of(
			DialogueEventRouter::stationUpgradeClaim,
			DialogueEventRouter::antecedent,
			DialogueEventRouter::clinicalLesson,
			AnnettaDialogueEvents::handle,
			DialogueEventRouter::circusSchool,
			CircusRingmasterDialogueEvents::handle,
			MnemonistDialogueEvents::handle,
			AlchemistDialogueEvents::handle,
			ArtificerDialogueEvents::handle,
			VicarDialogueEvents::handle,
			VoyagerDialogueEvents::handle,
			CourtDialogueEvents::handle,
			VeinMasonDialogueEvents::handle,
			AcolyteDialogueEvents::handle,
			ZealotDialogueEvents::handle,
			GuardianDialogueEvents::handle,
			HermitDialogueEvents::handle,
			RecruitmentDialogueEvents::handle,
			SanguineMonolithDialogueEvents::handle,
			FungalWhisperDialogueEvents::handle,
			ScoutDialogueEvents::handle);

	private DialogueEventRouter() {
	}

	@SubscribeEvent
	public static void onDialogueOption(DialogueEvent event) {
		for (DialogueRoute route : ROUTES) {
			if (route.handle(event)) return;
		}
	}

	/** Station upgrade item claims, shared by every teaching NPC; the speaker was checked by the packet. */
	private static boolean stationUpgradeClaim(DialogueEvent event) {
		var stationClaim = StationUpgradeDialogue.parseClaim(event.getEventId());
		if (stationClaim.isEmpty()) return false;
		ServerPlayer player = event.getPlayer();
		event.setRewardDelivered(HemoCapabilityAccess.stationUpgrades(player).claim(player, stationClaim.get()));
		return true;
	}

	private static boolean antecedent(DialogueEvent event) {
		if (!event.getEventId().startsWith("antecedent_")) return false;
		event.setRewardDelivered(AntecedentDialogue.handle(event));
		return true;
	}

	/** Every clinical_lesson_ event stops here, a lesson or not. */
	private static boolean clinicalLesson(DialogueEvent event) {
		if (!event.getEventId().startsWith("clinical_lesson_")) return false;
		ServerPlayer player = event.getPlayer();
		for (var lesson : ClinicalBloodProgress.Lesson.values()) {
			if (lesson.event().equals(event.getEventId())) event.setRewardDelivered(
					ClinicalBloodKnowledge.teach(player, player.level().getEntity(event.getEntityId()), lesson));
		}
		return true;
	}

	/** Checked before the Ringmaster, whose route takes the other circus_ events a Ringmaster speaks. */
	private static boolean circusSchool(DialogueEvent event) {
		if (!event.getEventId().startsWith("circus_school_")) return false;
		event.setRewardDelivered(CircusSchoolDialogue.handle(event));
		return true;
	}
}
