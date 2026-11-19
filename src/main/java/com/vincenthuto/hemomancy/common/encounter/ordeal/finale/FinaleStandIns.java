package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusArena;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

/**
 * The real troupe's stand-ins on his stage (2026-11-18 fix pass): the finale cast standing on the real ring is raised on
 * the copy at its mapped place, frozen (no AI, untouchable, tagged as one of the finale's copies), so the hidden crossing
 * shows nobody missing. The director hauls them off on their strings as his opening ends and sweeps them with the stage;
 * one no finale owns removes itself.
 */
public final class FinaleStandIns {
	private FinaleStandIns() {
	}

	/** Raises a stand-in in {@code stageLevel} for each finale troupe member of the ring at {@code origin}. */
	public static List<CircusPerformerEntity> raise(ServerLevel siteLevel, ServerLevel stageLevel, BlockPos origin,
			BlockPos stage, Rotation rotation) {
		List<CircusPerformerEntity> raised = new ArrayList<>();
		for (CircusPerformerEntity real : CircusArena.of(origin).troupe(siteLevel)) {
			if (!real.isAlive() || real.getTags().contains(RingmasterFinaleDirector.COPY_TAG)) continue;
			Entity made = real.getType().create(stageLevel);
			if (!(made instanceof CircusPerformerEntity standIn)) continue;
			Vec3 at = FinaleStage.toCopy(real.position(), origin, stage, rotation);
			standIn.moveTo(at.x, at.y, at.z, FinaleStage.yawToCopy(real.getYRot()), real.getXRot());
			standIn.setYHeadRot(standIn.getYRot());
			standIn.copyLook(real);
			standIn.setNoAi(true);
			standIn.setInvulnerable(true);
			standIn.addTag(RingmasterFinaleDirector.COPY_TAG);
			if (stageLevel.addFreshEntity(standIn)) raised.add(standIn);
		}
		return raised;
	}
}
