package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/**
 * The one way a player pays blood. Every payment names its {@link Source}, settles through {@link BloodPaymentRules},
 * shows in the blood-flow diagnostics and ends in {@link BloodVolumeEvents#syncVolume} when blood left the volume.
 * The flags each source passes reproduce what that payment did before it came here; changing one is a balance change.
 */
public final class BloodAccounting {
	private BloodAccounting() {
	}

	/**
	 * Who is charging, for the diagnostics, and how. {@code countsAsSpend} adds a satisfied payment to the Hemorath
	 * blood debt; {@code reserveProtected} keeps Sanguine Reserve's floor out of reach.
	 */
	public record Source(String id, String label, Category category, boolean countsAsSpend, boolean reserveProtected) {
		/** A source labelled from its id: {@code living_axe} shows as "Living Axe". */
		public static Source of(String id, Category category, boolean countsAsSpend, boolean reserveProtected) {
			return new Source(id, label(id), category, countsAsSpend, reserveProtected);
		}

		private static String label(String id) {
			StringBuilder label = new StringBuilder();
			for (String word : id.split("_")) {
				if (word.isEmpty()) continue;
				if (!label.isEmpty()) label.append(' ');
				label.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
			}
			return label.toString();
		}
	}

	/** {@code satisfied} is what {@link IBloodVolume#drain} returned for a one-off payment. */
	public record Payment(double requested, double paid, boolean satisfied) {
		static final Payment NOTHING_OWED = new Payment(0.0D, 0.0D, true);
		static final Payment REFUSED = new Payment(0.0D, 0.0D, false);
	}

	/**
	 * Pays {@code amount} at once. Asking for more than the player holds empties the volume and is not satisfied;
	 * a dormant volume still pays, so callers that require an active volume check it first.
	 */
	public static Payment pay(Player player, double amount, Source source) {
		if (player == null) return Payment.REFUSED;
		if (amount <= 0.0D) return Payment.NOTHING_OWED;
		return settle(player, amount, source, 1, BloodPaymentRules.Terms.ONE_OFF);
	}

	/**
	 * Pays a recurring cost every {@code intervalTicks}, shown per tick in the diagnostics. A dormant volume pays
	 * nothing, and {@code requireFullAmount} refuses a payment that cannot be made in full.
	 */
	public static Payment payUpkeep(Player player, double amount, Source source, int intervalTicks,
			boolean requireFullAmount) {
		if (player == null || amount <= 0.0D) return Payment.REFUSED;
		return settle(player, amount, source, intervalTicks, new BloodPaymentRules.Terms(true, requireFullAmount));
	}

	private static Payment settle(Player player, double amount, Source source, int intervalTicks,
			BloodPaymentRules.Terms terms) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		boolean reserveEnabled = source.reserveProtected()
				&& SkillPointHelper.isTechniqueEnabled(player, SkillPointInit.skill_sanguine_reserve);
		BloodPaymentRules.Outcome outcome = BloodPaymentRules.settle(volume, amount, source.reserveProtected(),
				reserveEnabled, source.countsAsSpend(), terms);
		if (player instanceof ServerPlayer serverPlayer) {
			int interval = Math.max(1, intervalTicks);
			BloodFlowLedger.recordApplied(serverPlayer, source.id(), source.label(), source.category(),
					-amount, -outcome.paid(), interval, false, outcome.condition());
			if (outcome.paid() > 0.0D) {
				BloodVolumeEvents.syncVolume(serverPlayer, volume);
			}
		}
		return new Payment(outcome.requested(), outcome.paid(), outcome.satisfied());
	}
}
