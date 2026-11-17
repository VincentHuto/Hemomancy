package com.vincenthuto.hemomancy.common.manipulation.verb;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

/**
 * What a selected manipulation makes of a verb: a Sculpting changes what Projection or Absorption does to the thing the
 * player aims at. It is attached at registration ({@code setProjectionSculpting} / {@code setAbsorptionSculpting} in
 * {@code ManipulationInit}) and run only by {@link SculptingDispatcher}, after rites, Saints and Wills have had their
 * claim and before the plain verb's own endpoints.
 *
 * <p>{@link #claims} must be decidable from what both sides see (block state, fluid, entity type, item stacks), so the
 * hand's stream, the channel HUD and the movement rule agree with the server. Claim narrowly: a Sculpting that claims
 * too much steals rite and machine input.
 */
public interface VerbSculpting {
	/** The default pulse: the hurt cooldown, so a pulse that wounds always lands. */
	int DEFAULT_CADENCE = 10;

	/** The target kinds the pick should look for on this Sculpting's behalf. */
	Set<VerbTargetKind> wants();

	/** Whether this Sculpting takes {@code target}; if not, the plain verb's chain goes on as if nothing were selected. */
	boolean claims(Player player, VerbTarget target);

	/** One paid pulse. The blood is already paid when this runs; {@code pulse} says how much and how hard to work. */
	void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse);

	/**
	 * Server only, after the cast gates and before any blood or Temper is priced: whether this pulse may go ahead on
	 * {@code target}. A Sculpting that refuses here (a ward, a rite space, ground the player may not alter) still holds
	 * the claim, so the plain verb does not run, but nothing is paid. It should say why to the player itself.
	 */
	default boolean admits(ServerPlayer player, VerbTarget target) {
		return true;
	}

	/**
	 * A session opens on a new target: called once its first pulse is paid, just before that pulse runs. A claim that
	 * is never paid (a gate refused it) never begins.
	 */
	default void begin(ServerPlayer player, VerbTarget target) {
	}

	/** The session ends: the use stopped ({@code released}), the target changed, or the claim lapsed. */
	default void end(ServerPlayer player, boolean released) {
	}

	/** Ticks between pulses. */
	default int cadence() {
		return DEFAULT_CADENCE;
	}

	/** The blood one pulse costs before the manipulation cost ledger's multipliers. */
	double bloodPerPulse();

	/**
	 * The blood one pulse costs {@code player} before the cost ledger: {@link #bloodPerPulse()} unless the player's own
	 * state changes it (a strand's twin from the off hand). The price, the HUD and the tests read this one. Safe on both
	 * sides.
	 */
	default double bloodPerPulse(Player player) {
		return bloodPerPulse();
	}

	/**
	 * The blood one pulse on {@code target} costs before the cost ledger, what the pulse is charged:
	 * {@link #bloodPerPulse(Player)} unless the price depends on what is struck (a Hewing cut priced by hardness).
	 */
	default double bloodPerPulse(ServerPlayer player, VerbTarget target) {
		return bloodPerPulse(player);
	}

	/** Whether the player is held still while it works, by the same rules (and skills) as Absorption. */
	default boolean roots() {
		return false;
	}

	/** Scales the verb's reach (a beam reaches further than a touch); held to {@link VerbReach#scaled}'s bounds. */
	default double reachScale() {
		return 1.0D;
	}

	/** {@link #reachScale()} for {@code player}, when their skills change it (Long Strand). Safe on both sides. */
	default double reachScale(Player player) {
		return reachScale();
	}

	/**
	 * Whether this Sculpting, once its first pulse is paid, keeps the channel wherever the aim goes until the use stops
	 * or a pulse is refused (a strand, core-loop Track 12). An ordinary Sculpting claims tick by tick and ends its work
	 * when the target changes; a held one claims once, at {@link #begin}, so sweeping the aim across a basin does not
	 * hand the channel to the basin. Rites, Saints and Wills still claim first every tick.
	 */
	default boolean holdsChannel() {
		return false;
	}

	/**
	 * Each tick a held channel stays open ({@link #holdsChannel}), after any pulse due that tick, with what the
	 * player aims at now (open air at the Sculpting's reach when the pick finds nothing).
	 */
	default void hold(ServerPlayer player, VerbTarget aimed) {
	}

	/**
	 * Whether the attack button means something to this Sculpting while it holds the channel. Read on the client, which
	 * then sends the press; the server decides in {@link #accent}.
	 */
	default boolean takesAccent() {
		return false;
	}

	/**
	 * Server only: the attack button went down while this Sculpting held the channel. A strand snaps; a dome of dark ends
	 * in its flash; an ice hand closes. Returns whether anything was done; a refusal pays nothing.
	 */
	default boolean accent(ServerPlayer player) {
		return false;
	}

	/**
	 * Whether a working session plays the manipulation's sustained cast presentation (its outpouring round the caster).
	 * A strand draws itself and its arm instead, so it declines: the outpouring would bury the strand in first person.
	 */
	default boolean presentsCast() {
		return true;
	}

	/**
	 * Whether the Sculpting draws what runs between the hand and its target itself (the Tendon Line's cord), so the cell
	 * hand pours no ordinary blood-cell stream while it claims the aim. Read on the client only.
	 */
	default boolean drawsOwnStream() {
		return false;
	}

	/**
	 * How fast the channelling player may move while this Sculpting is selected, from 0 to 1; empty to follow
	 * {@link #roots}. A value here replaces vanilla's use-item slowdown rather than adding to it. Safe on both sides.
	 */
	default java.util.OptionalDouble movement(Player player) {
		return java.util.OptionalDouble.empty();
	}
}
