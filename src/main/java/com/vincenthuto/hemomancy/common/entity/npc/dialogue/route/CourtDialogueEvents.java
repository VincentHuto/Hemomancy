package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionTrial;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;

/**
 * Every Brined Court member's events: food, rest, gifts, offerings, amends, the lesson, joining a house, and the
 * Head-tender's leave to challenge the Kindled Champion.
 */
final class CourtDialogueEvents {
	private CourtDialogueEvents() {
	}

	/** Takes every {@code court_member_} event; only a Court member can deliver one. */
	public static boolean handle(DialogueEvent event) {
		if (!event.getEventId().startsWith(CourtMemberDialogue.EVENT_PREFIX)) return false;
		if (event.getPlayer().level().getEntity(event.getEntityId()) instanceof CourtMemberEntity member) {
			if (event.getEventId().equals(CourtMemberDialogue.CHALLENGE)) {
				event.setRewardDelivered(member.role() == CourtRole.HEAD_TENDER
						&& KindledChampionTrial.challenge(event.getPlayer(), member) != null);
				return true;
			}
			event.setRewardDelivered(CourtHospitality.handle(event.getPlayer(), member, event.getEventId()));
		}
		return true;
	}
}
