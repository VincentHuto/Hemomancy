package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

/**
 * One Saint meter being drawn. {@code x} is the bar's left edge and {@code y} the first row under it (the bar's origin
 * plus 14); a style draws {@link BossBarFrame#WIDTH} across and its own {@code height()} down. The Saint values come from
 * the body's entity data (the last seen, when the body is out of range), the own values, flags and countdown from the
 * challenger's {@code SaintMeterPacket}. {@code reveal} runs 0 to 1 as the meter first appears; {@code time} is client
 * ticks plus the partial tick. {@code canvas} is null outside a draw (the muffle query).
 */
public record SaintMeterContext(@Nullable GuiCanvas canvas, int x, int y, int screenWidth, int screenHeight, float time,
		float partialTick, @Nullable SaintTrialEntity saint, LocalPlayer viewer, float saintValue, float saintMax,
		float ownValue, float ownMax, float ownSecondary, float ownSecondaryMax, int flags, int stateTicks, int phase,
		int openingId, float openingTime, int openingLength, float reveal) {
	public float right() {
		return x + BossBarFrame.WIDTH;
	}

	public float centerX() {
		return x + BossBarFrame.WIDTH * 0.5F;
	}

	public float saintFraction() {
		return saintMax <= 0.0F ? 0.0F : Mth.clamp(saintValue / saintMax, 0.0F, 1.0F);
	}

	public float ownFraction() {
		return ownMax <= 0.0F ? 0.0F : Mth.clamp(ownValue / ownMax, 0.0F, 1.0F);
	}

	public float secondaryFraction() {
		return ownSecondaryMax <= 0.0F ? 0.0F : Mth.clamp(ownSecondary / ownSecondaryMax, 0.0F, 1.0F);
	}

	/** How far through the open opening, 0 to 1; 0 when closed. */
	public float openingFraction() {
		return openingId == 0 || openingLength <= 0 ? 0.0F : Mth.clamp(openingTime / openingLength, 0.0F, 1.0F);
	}

	public boolean flag(int bit) {
		return (flags & bit) != 0;
	}
}
