package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Where a Harbinger's covenant work stands, which the Vicar and the Monolith both read for their counsel at the fifth
 * and sixth Degree.
 */
public record CovenantProgress(boolean inBloodline, boolean faneComplete, boolean chamberReturned, boolean throneBound,
		boolean vigilComplete) {

	public static CovenantProgress of(ServerPlayer player) {
		return new CovenantProgress(HemoCapabilityAccess.getBloodVolume(player).getBloodLine().isValid(),
				HarbingerChapterProgression.completedChapters(player).contains(HarbingerChapterMilestone.COVENANT_WRITTEN_IN_PLACE),
				HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED),
				HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND),
				HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED));
	}

	/**
	 * What the player still owes at {@code degree}, in order, each step the end of a speaker's counsel key. At the fifth
	 * Degree the written covenant comes first (a leader's Fane or a member's Bloodwell deposit), then membership of any
	 * registered line: a member is sent to the Fane, never to found a line of their own. Empty at any other Degree.
	 */
	public List<String> steps(int degree) {
		if (degree == 5) {
			return List.of(faneComplete ? "perform_bloodline_rite" : !inBloodline ? "found_bloodline" : "found_fane");
		}
		if (degree != 6) return List.of();
		List<String> steps = new ArrayList<>();
		if (!chamberReturned) steps.add("chamber_return");
		if (!throneBound) steps.add("bind_throne");
		if (!vigilComplete) steps.add("complete_vigil");
		if (steps.isEmpty()) steps.add("perform_hematic_order_rite");
		return steps;
	}
}
