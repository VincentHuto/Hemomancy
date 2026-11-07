package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMeter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * The absorbed-blood meter's rule on the kit's {@link SaintMeter}: a Saint-owned maximum of 3000 scaled per
 * challenger, a leak read live from his state each tick (the kit applies it as negative drift), no floor
 * (resolution D-1), and the four thresholds whose crossings change his body and teach the fight. The numbers are
 * {@link HemorathMeterRules}; the Saint reports the leak it took to its wells and patches.
 */
public final class HemorathMeter implements SaintMeter.Rule {
	private final HemorathEntity saint;

	public HemorathMeter(HemorathEntity saint) {
		this.saint = saint;
	}

	@Override
	public float max(int challengers) {
		return HemorathMeterRules.max(challengers);
	}

	@Override
	public float drift(@Nullable SaintTrialEntity body, float value, float max) {
		if (body != saint || saint.level().isClientSide) return 0.0F;
		float leak = Math.min(value, saint.leakNow(value, max));
		saint.leaked(leak);
		return -leak;
	}

	@Override
	public float[] thresholds() {
		return HemorathMeterRules.thresholds();
	}

	@Override
	public void crossed(@Nullable SaintTrialEntity body, @Nullable Player player, int threshold, boolean rising) {
		if (body == saint) saint.thresholdCrossed(threshold, rising);
	}
}
