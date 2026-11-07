package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPresence;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import net.minecraft.world.entity.player.Player;

/**
 * The drain (design section 5) on the kit's {@link SaintPresence}: the kit runs it over the challengers in range and in
 * sight, every 5 ticks, through {@code canHarmFor}, and pauses it for the rise, the card and the ending. A blood-active
 * player pays through {@link BloodAccounting#payUpkeep} with the named source, and what they paid fills the meter
 * (x1.25 from an Apotheos); an empty or inactive one is hurt instead, worth 25 to the meter a health point. The Last
 * Thirst widens it to the whole chamber at a flat 2 a tick.
 */
public final class HemorathPresence implements SaintPresence {
	public static final String LESSON_DRAIN = "message.hemomancy.hemorath.lesson_drain";

	private final HemorathEntity saint;

	public HemorathPresence(HemorathEntity saint) {
		this.saint = saint;
	}

	@Override
	public double radius(SaintTrialEntity body) {
		return HemorathPresenceRules.radius(saint.thirsting());
	}

	@Override
	public double verticalReach(SaintTrialEntity body) {
		return HemorathPresenceRules.VERTICAL_REACH;
	}

	@Override
	public int cadence(SaintTrialEntity body) {
		return HemorathPresenceRules.CADENCE;
	}

	@Override
	public float falloff(SaintTrialEntity body, double distance) {
		return HemorathPresenceRules.falloff(distance, saint.thirsting());
	}

	@Override
	public void pulse(SaintTrialEntity body, Player player, float strength) {
		if (strength <= 0.0F) return;
		boolean thirst = saint.thirsting();
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume.isActive() && volume.getBloodVolume() > 0.0D) {
			BloodAccounting.Payment payment = BloodAccounting.payUpkeep(player,
					HemorathPresenceRules.drain(strength, thirst), HemorathSources.PRESENCE,
					HemorathPresenceRules.CADENCE, false);
			if (payment.paid() <= 0.0D) return;
			saint.feed(HemorathPresenceRules.intake((float) payment.paid(), ApotheosBodyEvents.isApotheos(player)), player,
					false);
			saint.bloodFell(player);
			HemorathEffects.drainMotes(saint, player);
			SaintLines.teach(player, LESSON_DRAIN);
			return;
		}
		if (!HemorathPresenceRules.hurtsEmpty(strength, thirst) || !HemorathPresenceRules.emptyTickDue(saint.tickCount)) return;
		if (SaintMoves.hurt(saint, player, HemorathPresenceRules.EMPTY_DAMAGE, null)) {
			saint.feed(HemorathPresenceRules.METER_PER_HEALTH, player, false);
			saint.bloodFell(player);
			HemorathEffects.drainMotes(saint, player);
		}
	}
}
