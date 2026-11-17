package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Where a Harbinger's covenant work stands, which the Vicar and the Monolith both read for their counsel at the fifth
 * and sixth Degree. {@code leadsLine} and {@code lineHasFane} are read live, so a lineless or Fane-less sixth Degree is
 * sent to the step they can take (PT-69).
 */
public record CovenantProgress(boolean inBloodline, boolean faneComplete, boolean chamberReturned, boolean throneBound,
		boolean vigilComplete, boolean leadsLine, boolean lineHasFane) {

	/** A member of a line that has its Fane, or no line at all. */
	public CovenantProgress(boolean inBloodline, boolean faneComplete, boolean chamberReturned, boolean throneBound,
			boolean vigilComplete) {
		this(inBloodline, faneComplete, chamberReturned, throneBound, vigilComplete, false, inBloodline);
	}

	public static CovenantProgress of(ServerPlayer player) {
		Bloodline line = HemoCapabilityAccess.getBloodVolume(player).getBloodLine();
		boolean inLine = line.isValid();
		return new CovenantProgress(inLine,
				HarbingerChapterProgression.completedChapters(player).contains(HarbingerChapterMilestone.COVENANT_WRITTEN_IN_PLACE),
				HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED),
				HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND),
				HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED),
				inLine && line.canManage(player.getUUID()), inLine && hasFane(player, line));
	}

	private static boolean hasFane(ServerPlayer player, Bloodline line) {
		for (ServerLevel level : player.server.getAllLevels()) {
			if (FoundingFaneSavedData.get(level).hasFane(line.getLeaderUUID())) return true;
		}
		return false;
	}

	/**
	 * What the player still owes at {@code degree}, in order, each step the end of a speaker's counsel key. At the fifth
	 * Degree the written covenant comes first (a leader's Fane or a member's Bloodwell deposit), then membership of any
	 * registered line: a member is sent to the Fane, never to found a line of their own. At the sixth, a Throne binds
	 * only in the line's Fane, so a lineless player founds a line, a leader with no Fane raises one, and a member whose
	 * line has none is told so. Empty at any other Degree.
	 */
	public List<String> steps(int degree) {
		if (degree == 5) {
			return List.of(faneComplete ? "perform_bloodline_rite" : !inBloodline ? "found_bloodline" : "found_fane");
		}
		if (degree != 6) return List.of();
		List<String> steps = new ArrayList<>();
		if (!chamberReturned) steps.add("chamber_return");
		if (!throneBound) {
			steps.add(!inBloodline ? "found_bloodline"
					: lineHasFane ? "bind_throne"
					: leadsLine ? "found_fane" : "line_has_no_fane");
		}
		if (!vigilComplete) steps.add("complete_vigil");
		if (steps.isEmpty()) steps.add("perform_hematic_order_rite");
		return steps;
	}
}
