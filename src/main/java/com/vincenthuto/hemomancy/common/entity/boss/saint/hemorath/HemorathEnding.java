package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

/**
 * The ending's server beats (design section 11) on the kit's saved ending window ({@link HemorathEndingRules#SPEC}):
 * the hit-stop's crack, the burst out of his ribs with every patch, the pour that floods every gutter in fronts running
 * out from where he stands, the fold, and the road home to the tomb. The kit runs {@code endTrial} once at tick 125,
 * restores every gutter and both flank basins, and discards him at 130. A reload resumes the ending; a flood the reload
 * lost is laid again.
 */
public final class HemorathEnding {
	private boolean flooded;

	public void tick(HemorathEntity saint, ServerLevel level, int tick) {
		Vec3 chest = saint.chest();
		if (HemorathEndingRules.starts(tick, HemorathEndingRules.Beat.HIT_STOP)) {
			HemorathEffects.sound(saint, SoundEvents.BONE_BLOCK_BREAK, 1.5F, 0.4F);
			saint.endFightState();
		}
		if (HemorathEndingRules.starts(tick, HemorathEndingRules.Beat.BURST)) {
			HemorathEffects.burst(level, chest, HemorathVisualRules.EXSANGUINATE, 3.5F);
			HemorathEffects.burst(level, chest, HemorathVisualRules.RIB_JETS, (float) HemorathMove.RIB_LENGTH * 0.5F);
			HemorathEffects.sound(saint, SoundInit.RITE_BACKLASH, 2.0F, 0.5F);
			int patches = saint.patchCount();
			saint.tearPatches(patches, true);
		}
		boolean pouring = tick >= HemorathEndingRules.BURST_END && tick < HemorathEndingRules.HOME_END;
		if (pouring && !flooded) {
			flooded = true;
			saint.floodAll();
			HemorathEffects.sound(saint, SoundEvents.BUCKET_EMPTY, 1.5F, 0.5F);
		}
		if (HemorathEndingRules.starts(tick, HemorathEndingRules.Beat.FOLD)) {
			HemorathEffects.sound(saint, SoundInit.RITE_COLLAPSE, 1.2F, 0.6F);
		}
		if (HemorathEndingRules.starts(tick, HemorathEndingRules.Beat.HOME)) {
			Vec3 home = saint.tombPoint();
			HemorathEffects.flow(level, saint.position().add(0.0D, 0.2D, 0.0D), home, HemorathVisualRules.ROAD_HOME,
					HemorathEndingRules.HOME_END - HemorathEndingRules.FOLD_END, 0.5D);
		}
		if (tick == HemorathEndingRules.END_TRIAL - 1) HemorathEffects.sound(saint, SoundInit.RITE_SEAL, 1.0F, 0.7F);
	}
}
