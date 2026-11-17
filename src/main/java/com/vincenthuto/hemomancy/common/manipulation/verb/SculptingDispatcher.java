package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.VerbRefusalPacket;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusProfile;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusRules;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Runs the selected manipulation's Sculpting of a verb. Called from the verb chains after the early claims (founder
 * initiation, Saints, Wills, Cardinal Rites, and on Absorption the block endpoints) and before the plain verb's own
 * endpoints: a Sculpting that claims its target acts ahead of machines and reservoirs (CL-4); one that does not claim
 * falls through, so the plain verb behaves exactly as it does with nothing selected.
 *
 * <p>This class never names a manipulation; what a manipulation makes of a verb is attached in
 * {@code ManipulationInit} ({@code SculptingGuardrailTest}).
 */
public final class SculptingDispatcher {
	private SculptingDispatcher() {
	}

	/** The selected manipulation (the registry entry) and its Sculpting of a verb. */
	public record Selected(BloodManipulation manipulation, VerbSculpting sculpting) {
	}

	/** What the Sculpting would take now, as both sides see it. */
	public record Claim(Selected selected, VerbTarget target) {
	}

	public enum Outcome {
		/** No Sculpting of this verb is selected: the plain verb runs. */
		NONE,
		/** A Sculpting is selected but does not take what the player aims at: the plain verb runs. */
		PASSED,
		/** The Sculpting took the target this tick (paid, waiting for its next pulse, or refused by a gate). */
		CLAIMED
	}

	public record Result(Outcome outcome, double bloodPaid) {
		static final Result NONE = new Result(Outcome.NONE, 0.0D);
		static final Result PASSED = new Result(Outcome.PASSED, 0.0D);

		public boolean claimed() {
			return outcome == Outcome.CLAIMED;
		}
	}

	/**
	 * The equipped, selected manipulation's Sculpting of {@code verb}, read from the registry entry (saved copies carry
	 * no Sculpting): its main one, or its first narrow one when it has no main one. This is the Sculpting the HUD names by
	 * default and whose movement the channel follows. Safe on both sides.
	 */
	public static Optional<Selected> selected(Player player, Verb verb) {
		return selectedManipulation(player).flatMap(registered -> registered.getSculpting(verb)
				.or(() -> registered.getSculptings(verb).stream().findFirst())
				.map(sculpting -> new Selected(registered, sculpting)));
	}

	/**
	 * Every Sculpting of {@code verb} the selected manipulation has, in the order they claim: its narrow Sculptings first,
	 * then its main one ({@code addProjectionSculpting} before {@code setProjectionSculpting}). Safe on both sides.
	 */
	public static List<Selected> sculptings(Player player, Verb verb) {
		return selectedManipulation(player)
				.map(registered -> registered.getSculptings(verb).stream().map(sculpting -> new Selected(registered, sculpting)).toList())
				.orElse(List.of());
	}

	/**
	 * The equipped, selected manipulation's registry entry, when it is one. For another player on this client it is
	 * what their server last sent ({@code VerbPresence}), since their memories are not synced here.
	 */
	static Optional<BloodManipulation> selectedManipulation(Player player) {
		if (player.level().isClientSide) {
			Optional<String> sent = com.vincenthuto.hemomancy.common.util.ClientBridge.get().onlookerSelection(player);
			if (sent.isPresent()) {
				return sent.get().isEmpty() ? Optional.empty() : Optional.ofNullable(ManipulationInit.getByName(sent.get()));
			}
		}
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		if (known == null || known.getManipList().isEmpty()) return Optional.empty();
		MemorySlotRef ref = known.getSelectedMemoryRef();
		BloodManipulation chosen = known.getSelectedManip();
		if (ref == null || ref.kind() != MemoryEntryKind.MANIPULATION || chosen == null || chosen == BloodManipulation.BLANK) {
			return Optional.empty();
		}
		BloodManipulation registered = ManipulationInit.getByName(chosen.getName());
		if (registered == null || !known.isManipEquipped(registered)) return Optional.empty();
		return Optional.of(registered);
	}

	/**
	 * Whether {@code manipulation} sculpts {@code verb} at all: for its memory's tooltip and teaching, never to run one.
	 * Safe on both sides.
	 */
	public static boolean sculpts(BloodManipulation manipulation, Verb verb) {
		if (manipulation == null) return false;
		BloodManipulation registered = ManipulationInit.getByName(manipulation.getName());
		return registered != null && !registered.getSculptings(verb).isEmpty();
	}

	/** The selected Sculpting's reach for {@code verb}. */
	public static double reach(Player player, Verb verb, VerbSculpting sculpting) {
		return VerbReach.scaled(VerbReach.of(player, verb), sculpting.reachScale(player));
	}

	/**
	 * What the selected manipulation would claim if the player channelled {@code verb} now: the first of its Sculptings,
	 * in claim order, that takes what the player aims at. Safe on both sides.
	 */
	public static Optional<Claim> currentClaim(Player player, Verb verb) {
		return firstClaim(player, verb, sculptings(player, verb));
	}

	private static Optional<Claim> firstClaim(Player player, Verb verb, List<Selected> candidates) {
		for (Selected candidate : candidates) {
			Optional<Claim> claim = claimFor(player, verb, candidate);
			if (claim.isPresent()) return claim;
		}
		return Optional.empty();
	}

	private static Optional<Claim> claimFor(Player player, Verb verb, Selected selected) {
		return VerbTargeting
				.pick(player.level(), player, reach(player, verb, selected.sculpting()), selected.sculpting().wants())
				.filter(target -> selected.sculpting().claims(player, target))
				.map(target -> new Claim(selected, target));
	}

	/**
	 * One tick of {@code verb} on the server: a held channel already open keeps it (a strand swept over water stays
	 * a strand), otherwise the first Sculpting in claim order that takes the aim claims it; then the session is kept
	 * and a pulse runs when one is due.
	 */
	public static Result trySculpt(ServerPlayer player, Verb verb) {
		List<Selected> candidates = sculptings(player, verb);
		if (candidates.isEmpty()) {
			SculptingSessions.end(player, false);
			return Result.NONE;
		}
		for (Selected candidate : candidates) {
			Optional<Result> held = continueHeld(player, verb, candidate);
			if (held.isPresent()) return held.get();
		}
		Optional<Claim> claim = firstClaim(player, verb, candidates);
		if (claim.isEmpty()) {
			SculptingSessions.end(player, false);
			return Result.PASSED;
		}
		Selected selected = claim.get().selected();
		VerbSculpting sculpting = selected.sculpting();
		SculptingSessions.Session session = SculptingSessions.track(player, selected, verb, claim.get().target());
		long now = player.level().getGameTime();
		if (!SculptingRules.pulseDue(SculptingSessions.lastPulse(player), now, sculpting.cadence())) {
			return new Result(Outcome.CLAIMED, 0.0D);
		}
		OptionalDouble paid = pulse(player, verb, selected, session, claim.get().target(), now);
		if (paid.isEmpty()) {
			// A refused pulse waits out a cadence too, so its refusal line is not repeated every tick, and nothing is
			// being worked meanwhile, so the cast presentation stops until a pulse is paid again.
			SculptingSessions.refused(player, session);
			showRefusal(player, sculpting);
			return new Result(Outcome.CLAIMED, 0.0D);
		}
		if (sculpting.holdsChannel() && session.opened) sculpting.hold(player, claim.get().target());
		return new Result(Outcome.CLAIMED, paid.getAsDouble());
	}

	/**
	 * A held channel (a strand): once its Sculpting has begun it keeps the channel wherever the aim goes, pulsing on
	 * its cadence and working every tick, until the use stops or a pulse is refused. Empty when no held session runs.
	 */
	private static Optional<Result> continueHeld(ServerPlayer player, Verb verb, Selected selected) {
		VerbSculpting sculpting = selected.sculpting();
		if (!sculpting.holdsChannel()) return Optional.empty();
		long now = player.level().getGameTime();
		SculptingSessions.Session session = SculptingSessions.held(player, selected, verb, now);
		if (session == null) return Optional.empty();
		double reach = reach(player, verb, sculpting);
		VerbTarget aimed = VerbTargeting.pick(player.level(), player, reach, sculpting.wants())
				.orElseGet(() -> new VerbTarget.Air(player.getEyePosition().add(player.getLookAngle().scale(reach))));
		double paid = 0.0D;
		if (SculptingRules.pulseDue(SculptingSessions.lastPulse(player), now, sculpting.cadence())) {
			OptionalDouble pulse = pulse(player, verb, selected, session, aimed, now);
			if (pulse.isEmpty()) {
				// Out of blood or refused by a gate: a held channel lets go rather than hang on unpaid.
				SculptingSessions.end(player, false);
				showRefusal(player, sculpting);
				return Optional.of(new Result(Outcome.CLAIMED, 0.0D));
			}
			paid = pulse.getAsDouble();
		}
		sculpting.hold(player, aimed);
		return Optional.of(new Result(Outcome.CLAIMED, paid));
	}

	/** How long past its cadence a refusal's HUD mark lasts, so a refusal repeated each cadence reads as steady. */
	public static final int REFUSAL_MARK_GRACE_TICKS = 4;

	/** Tells the owner's verb HUD the claim was refused; the mark lasts until the next refusal is due, and a little more. */
	private static void showRefusal(ServerPlayer player, VerbSculpting sculpting) {
		PacketHandler.sendToPlayer(player, new VerbRefusalPacket(Math.max(1, sculpting.cadence()) + REFUSAL_MARK_GRACE_TICKS));
	}

	/** Pays and runs one pulse of the session's Sculpting; empty when a gate refused it. */
	private static OptionalDouble pulse(ServerPlayer player, Verb verb, Selected selected, SculptingSessions.Session session,
			VerbTarget target, long now) {
		VerbSculpting sculpting = selected.sculpting();
		int cadence = Math.max(1, sculpting.cadence());
		int credited = SculptingRules.creditedUses(session.bankedTicks, cadence);
		SculptingPulse pulse = new SculptingPulse(verb, 0.0D, workScale(VerbReach.staffFocus(player), verb), session.pulses);
		OptionalDouble paid = selected.manipulation().tryPerformVerbPulse(player, sculpting, target,
				pulse, credited, () -> SculptingSessions.opened(player, session, selected, target));
		SculptingSessions.recordPulse(player, now);
		if (paid.isPresent()) {
			session.pulses++;
			session.bankedTicks += cadence;
		}
		return paid;
	}

	/**
	 * How much more work the tool does per pulse than a bare hand: the Living Staff's transfer rates over the bare
	 * hand's (its rate skills raise the work, never the price). 1 for a bare hand.
	 */
	public static double workScale(LivingStaffFocusProfile staffFocus, Verb verb) {
		if (staffFocus == null) return 1.0D;
		return verb == Verb.ABSORB
				? LivingStaffFocusRules.blockAbsorptionPerTick(staffFocus) / LivingStaffFocusRules.bareBlockAbsorptionPerTick()
				: LivingStaffFocusRules.bloodTileProjectionRate(true, staffFocus)
						/ LivingStaffFocusRules.bloodTileProjectionRate(false, LivingStaffFocusProfile.NONE);
	}
}
