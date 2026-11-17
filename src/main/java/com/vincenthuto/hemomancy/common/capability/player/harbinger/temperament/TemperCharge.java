package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Temper charge every Sculpting pulse and every R cast pays (core loop Track 4). One rule: pay from the pool; when it
 * is short, dissolve one catalyst into it; when there is none, the effect runs dry (more blood, less strength). A
 * charge is worked out before the blood is priced, so the cost ledger can show the dry line, and settled only once
 * the blood is paid: a refused cast spends no Temper and no catalyst.
 *
 * Declared exemptions, which never reach here: conjurations and Staff weapon forms ({@link BloodManipulation#chargesTemper}),
 * Drudge actions, entity casts, the Crawling Choir's echo, Still Arts, Thelemic memories and weapon-form melee.
 */
public final class TemperCharge {
	/** At most one dry or catalyst notice on the action bar in this many ticks. */
	public static final int NOTICE_GAP_TICKS = 40;
	/** How often a continuous channel or a sustained passive pulses (ManipulationChannelManager, the avatar upkeep). */
	public static final int CHANNEL_PULSE_TICKS = 20;

	private TemperCharge() {
	}

	/** A charge worked out and not yet settled. */
	public record Charge(EnumBloodTemper school, TemperRules.Kind kind, TemperRules.Plan plan,
			@Nullable TemperCatalysts.Carried catalyst, float cap) {
		public boolean dry() {
			return plan.dry();
		}

		public TemperRules.Outcome outcome() {
			return plan.outcome();
		}

		/** The blood multiplier this charge puts on the effect. */
		public double costMultiplier() {
			return TemperRules.costMultiplier(kind, plan);
		}

		/** The strength the effect works at. */
		public float strength() {
			return TemperRules.strength(kind, plan);
		}
	}

	/**
	 * The charge of an R cast of {@code manipulation}: its rank's cost times {@code scale} (the charge fraction of a
	 * CHARGED cast) for a discrete cast, or one channel pulse for an uncredited pulse (a continuous channel, a
	 * sustained passive), burned by the price band of its cost a second. Null when the manipulation charges no Temper.
	 */
	@Nullable
	public static Charge forCast(Player player, BloodManipulation manipulation, float scale, boolean pulse) {
		if (manipulation == null || !manipulation.chargesTemper()) return null;
		float cost = pulse ? TemperRules.pulseCost(CHANNEL_PULSE_TICKS, manipulation.getCost())
				: TemperRules.castCost(manipulation.getRank()) * Math.clamp(scale, 0.0F, 1.0F);
		return plan(player, manipulation.getTend(), TemperRules.Kind.CAST, cost);
	}

	/**
	 * The charge of one Sculpting pulse at {@code cadence} costing {@code bloodPerPulse} before the ledger, burned by
	 * that price's band (LP-14). Null when the manipulation charges no Temper.
	 */
	@Nullable
	public static Charge forPulse(Player player, BloodManipulation manipulation, int cadence, double bloodPerPulse) {
		if (manipulation == null || !manipulation.chargesTemper()) return null;
		return plan(player, manipulation.getTend(), TemperRules.Kind.SCULPTING,
				TemperRules.pulseCost(cadence, SculptingRules.bloodPerSecond(bloodPerPulse, cadence)));
	}

	/** Works out a charge of {@code baseCost} in {@code school} for {@code player}, scars and catalysts included. */
	public static Charge plan(Player player, EnumBloodTemper school, TemperRules.Kind kind, float baseCost) {
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		float cap = cap(player, school);
		float cost = TemperRules.scarred(baseCost, activeScarTier(player, school));
		float held = temper.get(school);
		TemperCatalysts.Carried carried = held >= cost || cost <= 0.0F ? null
				: TemperCatalysts.find(player, school, held, cost, cap);
		TemperRules.Plan plan = TemperRules.plan(held, cost, carried == null ? 0.0F : carried.catalyst().worth(), cap);
		return new Charge(school, kind, plan, plan.outcome() == TemperRules.Outcome.CATALYST ? carried : null, cap);
	}

	/**
	 * Settles a charge once the effect's blood is paid: the catalyst dissolves, the pool burns, a dry school starts
	 * showing on the HUD, and the action bar says what happened (at most once every {@link #NOTICE_GAP_TICKS}).
	 */
	public static void commit(ServerPlayer player, @Nullable Charge charge) {
		if (charge == null) return;
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		TemperRules.Plan plan = charge.plan();
		ItemStack dissolved = ItemStack.EMPTY;
		if (plan.outcome() == TemperRules.Outcome.CATALYST) {
			TemperCatalysts.Carried carried = charge.catalyst();
			if (carried == null) {
				plan = new TemperRules.Plan(TemperRules.Outcome.DRY, 0.0F, 0.0F);
			} else {
				dissolved = new ItemStack(carried.item());
				if (!TemperCatalysts.dissolve(player, carried)) plan = new TemperRules.Plan(TemperRules.Outcome.DRY, 0.0F, 0.0F);
				else HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_CARRIED);
			}
		}
		float before = temper.get(charge.school());
		temper.set(charge.school(), TemperRules.settle(before, plan, charge.cap()), Math.max(charge.cap(), before));
		if (plan.dry()) temper.markSeen(charge.school());
		if (plan.outcome() == TemperRules.Outcome.TEMPERED) return;
		if (!temper.takeNotice(player.level().getGameTime(), NOTICE_GAP_TICKS)) return;
		Component school = BloodManipulation.temperName(charge.school());
		Component line = plan.dry()
				? Component.translatable("message.hemomancy.temper." + (charge.kind() == TemperRules.Kind.SCULPTING
						? "dry_sculpting" : "dry_cast"), school).withStyle(ChatFormatting.GOLD)
				: Component.translatable("message.hemomancy.temper.catalyst", dissolved.getHoverName(), school)
						.withStyle(ChatFormatting.GRAY);
		player.displayClientMessage(line, true);
	}

	/** How much of {@code school} the player can carry, by their effective alignment (scars included). */
	public static float cap(Player player, EnumBloodTemper school) {
		return TemperRules.cap(HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(school));
	}

	/** The highest tier among the player's active cerebral scars of {@code school}, or 0. */
	public static int activeScarTier(Player player, EnumBloodTemper school) {
		int[] tier = {0};
		HemoCapabilityAccess.getScarState(player).forEachActiveCerebralScar(scar -> {
			if (scar.getAssignedTemper() == school) tier[0] = Math.max(tier[0], scar.getTier());
		});
		return tier[0];
	}

	/**
	 * Temper from drawing {@code ml} out of a living creature: an even share per school of its blood profile. A
	 * bloodless or unprofiled creature gives none. Returns the total added.
	 */
	public static float fillFromCreature(ServerPlayer player, LivingEntity creature, double ml) {
		List<EnumBloodTemper> schools = BloodProfileData.profile(creature.getType(), false).tempers();
		float share = TemperRules.fromCreature(ml, schools.size());
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		float added = 0.0F;
		if (share > 0.0F) {
			for (EnumBloodTemper school : schools) added += temper.add(school, share, cap(player, school));
		}
		// A tainted parent's offspring carries a weak second school (core-loop Track 5): half a share of it.
		EnumBloodTemper inherited = com.vincenthuto.hemomancy.common.entity.menagerie.AnimalTaint.inheritedSchool(creature);
		if (inherited != null && !schools.contains(inherited)) {
			float weak = TemperRules.fromCreature(ml, Math.max(1, schools.size())) * 0.5F;
			if (weak > 0.0F) added += temper.add(inherited, weak, cap(player, inherited));
		}
		return added;
	}

	/**
	 * Temper from a meal (core-loop Track 10): the food table's school and amount, held to the player's cap. Returns
	 * what was added.
	 */
	public static float fillFromFood(ServerPlayer player, @Nullable EnumBloodTemper school, float amount) {
		if (school == null || !(amount > 0.0F)) return 0.0F;
		return HemoCapabilityAccess.getBloodTemper(player).add(school, amount, cap(player, school));
	}

	/**
	 * Mortem Temper drawn from the dead by Carrion Communion's Sculpting (core-loop round two P4): flesh and bone hold no
	 * living blood, only this. Held to the player's cap. Returns what was added.
	 */
	public static float fillFromCarrion(ServerPlayer player, float amount) {
		if (!(amount > 0.0F)) return 0.0F;
		return HemoCapabilityAccess.getBloodTemper(player).add(EnumBloodTemper.MORTEM, amount, cap(player, EnumBloodTemper.MORTEM));
	}

	/**
	 * Temper from a Will taken whole by Absorption (core-loop Track 11): an enzyme's worth per tier in the Will's
	 * school, held to the player's cap. Returns what was added.
	 */
	public static float fillFromWill(ServerPlayer player, EnumBloodTemper school, int tier) {
		float amount = TemperRules.fromWill(tier);
		if (school == null || !(amount > 0.0F)) return 0.0F;
		return HemoCapabilityAccess.getBloodTemper(player).add(school, amount, cap(player, school));
	}

	/**
	 * Temper from one pull on a kept creature (core-loop Track 5): {@code total}, already cut by Vigor and acclimation,
	 * shared evenly over the creature's {@code schools}. A creature with no schools gives none. Returns the total added.
	 */
	public static float fillFromKept(ServerPlayer player, List<EnumBloodTemper> schools, double total) {
		if (schools.isEmpty() || !(total > 0.0D)) return 0.0F;
		float share = (float) (total / schools.size());
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		float added = 0.0F;
		for (EnumBloodTemper school : schools) added += temper.add(school, share, cap(player, school));
		return added;
	}
}
