package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class BodyIdiomRules {
	public static final float BASE_IRON_HEART_HEALTH = 10.0F;
	public static final float IRON_HEART_HEALTH_PER_CAST = 2.0F;
	public static final float HEALTH_PER_HEART = 2.0F;
	public static final int IRON_HEART_DURATION_TICKS = 12_000;
	public static final int IRON_HEART_CHARGE_TICKS = 40;
	public static final float BLACKHEARTED_CONVERSION = 0.65F;
	public static final float BLACKHEARTED_HEALING_FRACTION = 0.5F;
	public static final float NECROTIC_SATURATION_CAP = 12.0F;
	public static final float NECROTIC_RUPTURE_DAMAGE = 6.0F;
	public static final int BLACKHEARTED_COOLDOWN_TICKS = 1_400;

	private BodyIdiomRules() {
	}

	public static float maxIronHeartHealth(int bonusHearts) {
		return BASE_IRON_HEART_HEALTH + Math.max(0, bonusHearts) * HEALTH_PER_HEART;
	}

	public static float maxIronHeartHealth(Player player) {
		int[] bonusHearts = {0};
		HemoCapabilityAccess.getScarState(player).ifPresent(scars -> scars.forEachActiveCerebralScar(
				scar -> bonusHearts[0] += scar.getIronHeartCapacityBonus()));
		return maxIronHeartHealth(bonusHearts[0]);
	}

	public static int ironHeartSlots(float maxIronHeartHealth) {
		return Math.max(0, Mth.ceil(maxIronHeartHealth / HEALTH_PER_HEART));
	}

	public static int ironHeartY(int y, int heart, boolean regenerating, int guiTicks, float maxHealth) {
		int offsetHeart = regenerating ? guiTicks % Mth.ceil(maxHealth + 5.0F) : -1;
		return heart == offsetHeart ? y - 2 : y;
	}

	public static boolean ironHeartPulse(long gameTime) {
		return gameTime % 20L < 4L;
	}

	public static IronHeartFormation ironHeartFormation(float storedFill, float shownFill) {
		float fill = Mth.clamp(shownFill, 0.0F, 1.0F);
		if (storedFill > 0.0F) return new IronHeartFormation(1.0F, fill);
		return new IronHeartFormation(Mth.clamp(fill * 2.0F, 0.0F, 1.0F),
				Mth.clamp(fill * 2.0F - 1.0F, 0.0F, 1.0F));
	}

	public static int removedIronHeartSlots(float previousHealth, float currentHealth) {
		return Math.max(0, ironHeartSlots(previousHealth) - ironHeartSlots(currentHealth));
	}

	public static int ironHeartCrackFrame(long startTick, long gameTime) {
		long elapsed = gameTime - startTick;
		return elapsed < 0L || elapsed >= 12L ? -1 : (int) (elapsed / 4L);
	}

	public static float addIronHeartHealth(float current, float maxIronHeartHealth) {
		return Mth.clamp(current + IRON_HEART_HEALTH_PER_CAST, 0.0F, Math.max(0.0F, maxIronHeartHealth));
	}

	public static IronHeartAbsorption absorbWithIronHearts(float ironHeartHealth, float incomingDamage,
			float maxIronHeartHealth) {
		float resource = Mth.clamp(ironHeartHealth, 0.0F, Math.max(0.0F, maxIronHeartHealth));
		float damage = Math.max(0.0F, incomingDamage);
		float absorbed = Math.min(resource, damage);
		return new IronHeartAbsorption(resource - absorbed, damage - absorbed);
	}

	public static BlackheartedResult metabolizeWither(float incomingDamage, float currentSaturation,
			boolean refractory) {
		float damage = Math.max(0.0F, incomingDamage);
		float saturation = Mth.clamp(currentSaturation, 0.0F, NECROTIC_SATURATION_CAP);
		if (refractory || damage == 0.0F) {
			return new BlackheartedResult(damage, 0.0F, saturation, false);
		}
		float prevented = Math.min(damage * BLACKHEARTED_CONVERSION,
				NECROTIC_SATURATION_CAP - saturation);
		float filled = saturation + prevented;
		boolean ruptured = filled >= NECROTIC_SATURATION_CAP;
		return new BlackheartedResult(damage - prevented,
				prevented * BLACKHEARTED_HEALING_FRACTION,
				ruptured ? 0.0F : filled, ruptured);
	}

	public record IronHeartAbsorption(float ironHeartHealth, float remainingDamage) {
	}

	public record IronHeartFormation(float emptyAlpha, float fill) {
	}

	public record BlackheartedResult(float remainingDamage, float healing, float saturation, boolean ruptured) {
	}
}
