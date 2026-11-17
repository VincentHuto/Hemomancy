package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.tile.ProjectedBloodSink;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.OptionalDouble;
import java.util.Set;

/**
 * A strand: a held Projection Sculpting with a combat form (core-loop Track 12). It opens when the first tick's target
 * is open air, a creature or a plain block (a plant only on a block), and once begun it keeps the channel until the
 * use stops or the blood runs out ({@link #holdsChannel}). A block that takes blood (a machine, a reservoir, a rite
 * endpoint, a cauldron) is never claimed, so projecting into a station with a strand selected still fills it. Rites,
 * Saints and Wills claim ahead of it every tick. All the work is in {@link StrandService}.
 */
public final class StrandSculpting implements VerbSculpting {
	/** Use ticks at a channel's start in which the client keeps the strand's pace before its first cue arrives. */
	public static final int CLIENT_OPEN_GRACE_TICKS = 6;
	private final StrandSpec spec;

	public StrandSculpting(StrandSpec spec) {
		this.spec = spec;
	}

	public StrandSpec spec() {
		return spec;
	}

	public StrandForm form() {
		return spec.form();
	}

	@Override
	public Set<VerbTargetKind> wants() {
		return spec.form().wants();
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return switch (target) {
			case VerbTarget.Air air -> spec.form() != StrandForm.PLANT;
			case VerbTarget.Creature creature -> spec.form() != StrandForm.PLANT;
			case VerbTarget.Block block -> !takesBlood(player.level(), block);
			default -> false;
		};
	}

	/**
	 * Whether projected blood has somewhere to go at this struck face: {@link #takesBlood(Level, BlockPos, BlockState)},
	 * or the ground network's own marks (an unlit ash trail, a vein node, a gouge run). Decidable on both sides.
	 */
	public static boolean takesBlood(Level level, VerbTarget.Block block) {
		return takesBlood(level, block.pos(), block.state()) || GroundNetwork.takesProjection(level, block.hit());
	}

	/**
	 * Whether projected blood has somewhere to go in this block: a blood endpoint, a reservoir, a projected-blood
	 * sink, or a cauldron that becomes a Condenser. Decidable on both sides.
	 */
	public static boolean takesBlood(Level level, BlockPos pos, BlockState state) {
		if (state.is(Blocks.CAULDRON) || level.getBlockEntity(pos) instanceof ProjectedBloodSink) return true;
		return BlockBloodInteractions.isBloodInteractionBlock(level, pos);
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		StrandService.begin(player, this, target);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		StrandService.pulse(player, this, target, pulse);
	}

	@Override
	public void hold(ServerPlayer player, VerbTarget aimed) {
		StrandService.hold(player, this, aimed);
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		StrandService.end(player, released);
	}

	@Override
	public boolean holdsChannel() {
		return true;
	}

	@Override
	public boolean takesAccent() {
		return true;
	}

	/** The snap (CL-22). */
	@Override
	public boolean accent(ServerPlayer player) {
		return StrandService.snap(player);
	}

	/** The strand is its own presentation: no outpouring round the caster on top of it. */
	@Override
	public boolean presentsCast() {
		return false;
	}

	@Override
	public int cadence() {
		return StrandSpec.CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return spec.bloodPerPulse();
	}

	/**
	 * Half again with the twin out of the off hand (Second Strand): the open strand's, else what opening one would be.
	 * A lash pays for the ticks since its last pulse by what it was doing in them: its profile's charge blood while the
	 * orb gathered, the spec's while it was thrown or live, and nothing while it faded ({@link #lashPrice}).
	 */
	@Override
	public double bloodPerPulse(Player player) {
		return StrandSkills.pulseCost(base(player), StrandService.twinFor(player));
	}

	private double base(Player player) {
		LashProfile lash = spec.lash();
		if (lash == null) return spec.bloodPerPulse();
		StrandSession session = StrandService.current(player);
		if (session == null) return lash.chargeBloodPerPulse();
		return lashPrice(lash.chargeBloodPerPulse(), spec.bloodPerPulse(), session.chargingTicks, session.outTicks, session.fadingTicks);
	}

	/**
	 * A lash's pulse price: the two rates weighed by the ticks spent at each since the last pulse, fading ticks counting
	 * for nothing. A whip rested just before its pulse still pays for the ticks it was out. Before any tick is counted
	 * (the pulse that opens the lash) it is the charge rate.
	 */
	public static double lashPrice(double chargeBlood, double outBlood, int chargingTicks, int outTicks, int fadingTicks) {
		int total = chargingTicks + outTicks + fadingTicks;
		if (total <= 0) return chargeBlood;
		return (chargeBlood * chargingTicks + outBlood * outTicks) / total;
	}

	@Override
	public double reachScale() {
		return spec.form().reachScale;
	}

	/** Long Strand reaches the pick further, as it lengthens the strand. */
	@Override
	public double reachScale(Player player) {
		return spec.form().reachScale * StrandSkills.lengthScale(SkillPointHelper.getLongStrandLevel(player));
	}

	/**
	 * The strand's pace (CQ-10), raised by Free Hand: only while a strand is actually out, so Projection into a station with a strand memory
	 * selected moves at the plain rule. On the client the pace also holds for the first
	 * {@link #CLIENT_OPEN_GRACE_TICKS} use ticks while this Sculpting claims the aim, so it does not hitch while the
	 * server's first strand cue is on its way.
	 */
	@Override
	public OptionalDouble movement(Player player) {
		if (StrandService.isOut(player) || opening(player))
			return OptionalDouble.of(StrandSkills.pace(spec.form().movement, SkillPointHelper.getFreeHandLevel(player)));
		return OptionalDouble.empty();
	}

	private boolean opening(Player player) {
		return player.level().isClientSide && player.getTicksUsingItem() <= CLIENT_OPEN_GRACE_TICKS
				&& SculptingDispatcher.currentClaim(player, Verb.PROJECT).map(claim -> claim.selected().sculpting() == this).orElse(false);
	}
}
