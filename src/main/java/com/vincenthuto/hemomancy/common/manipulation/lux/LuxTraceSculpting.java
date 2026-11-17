package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.block.harbinger.ground.LuxTraceBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hematic Beacon as a Sculpting of Projection (core-loop Track 6): held on a surface, light spreads from the struck face
 * across it in thin branching lines, a few cells a pulse, each a {@link LuxTraceBlock} that gives real light and dims
 * out over four minutes. It is how a Harbinger lights a cave without torches, for blood and Lux Temper. The lines keep
 * to the block grid and to the one surface they started on; they fork now and then and never fill an area.
 *
 * <p>The growing pattern is a session map keyed by player UUID, cleared through {@code TransientServerStateEvents}.
 */
public final class LuxTraceSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 6.0D;
	public static final int CADENCE = 10;
	public static final double REACH_SCALE = 1.5D;
	/** Cells one pulse adds by hand; the Staff's work scale adds more. */
	public static final int CELLS_PER_PULSE = 3;
	/** The most cells one held pattern spreads to. */
	public static final int MAX_CELLS = 48;
	private static final float FORK_CHANCE = 0.3F;
	private static final float TURN_CHANCE = 0.45F;
	private static final ParticleColor LUX = new ParticleColor(255, 232, 168);
	private static final Map<UUID, Pattern> PATTERNS = new HashMap<>();

	/** One growing tip: where it stands and which way it is heading. */
	private record Tip(BlockPos cell, Direction heading) {
	}

	private static final class Pattern {
		final BlockPos origin;
		final Direction face;
		final List<Tip> tips = new ArrayList<>();
		int cells;

		Pattern(BlockPos origin, Direction face) {
			this.origin = origin;
			this.face = face;
		}
	}

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return false;
		Direction side = block.hit().getDirection();
		return lightable(player.level(), block.pos().relative(side), side.getOpposite());
	}

	/** A cell where a trace may lie on {@code face}: open air or a trace already, on a solid face. */
	static boolean lightable(Level level, BlockPos cell, Direction face) {
		BlockState there = level.getBlockState(cell);
		if (!there.isAir() && !(there.getBlock() instanceof LuxTraceBlock)) return false;
		BlockPos host = cell.relative(face);
		return MultifaceBlock.canAttachTo(level, face, host, level.getBlockState(host));
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return;
		Direction side = block.hit().getDirection();
		BlockPos origin = block.pos().relative(side).immutable();
		Pattern pattern = new Pattern(origin, side.getOpposite());
		for (Direction heading : Direction.values()) {
			if (heading.getAxis() != pattern.face.getAxis() && player.getRandom().nextFloat() < 0.6F) {
				pattern.tips.add(new Tip(origin, heading));
			}
		}
		if (pattern.tips.isEmpty()) pattern.tips.add(new Tip(origin, inPlane(pattern.face, player.getRandom())));
		PATTERNS.put(player.getUUID(), pattern);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(player.level() instanceof ServerLevel level)) return;
		Pattern pattern = PATTERNS.get(player.getUUID());
		if (pattern == null) return;
		RandomSource random = player.getRandom();
		int budget = Math.max(1, Mth.ceil(CELLS_PER_PULSE * pulse.workScale()));
		if (pulse.index() == 0 && light(level, player, pattern.origin, pattern.face)) {
			pattern.cells++;
			budget--;
		}
		int guard = 0;
		while (budget > 0 && pattern.cells < MAX_CELLS && !pattern.tips.isEmpty() && guard++ < 24) {
			int pick = random.nextInt(pattern.tips.size());
			Tip tip = pattern.tips.get(pick);
			Direction heading = random.nextFloat() < TURN_CHANCE ? inPlane(pattern.face, random) : tip.heading();
			BlockPos next = tip.cell().relative(heading);
			if (!open(level, next, pattern.face) && heading != tip.heading()) {
				// A turn into a wall or back onto the line keeps going straight instead of ending the line.
				heading = tip.heading();
				next = tip.cell().relative(heading);
			}
			if (!open(level, next, pattern.face)) {
				pattern.tips.remove(pick);
				continue;
			}
			if (!light(level, player, next, pattern.face)) {
				pattern.tips.remove(pick);
				continue;
			}
			pattern.cells++;
			budget--;
			pattern.tips.set(pick, new Tip(next.immutable(), heading));
			if (random.nextFloat() < FORK_CHANCE) pattern.tips.add(new Tip(next.immutable(), inPlane(pattern.face, random)));
		}
		Vec3 at = Vec3.atCenterOf(pattern.origin).add(Vec3.atLowerCornerOf(pattern.face.getNormal()).scale(0.45D));
		level.sendParticles(HemoParticleData.glow(LUX), at.x, at.y, at.z, 4, 0.2D, 0.2D, 0.2D, 0.01D);
		level.playSound(null, pattern.origin, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.4F, 1.6F);
	}

	/** A cell a growing line can step into: lightable and not lit yet. */
	private static boolean open(Level level, BlockPos cell, Direction face) {
		return lightable(level, cell, face) && !(level.getBlockState(cell).getBlock() instanceof LuxTraceBlock);
	}

	/** Lays one trace face, as the player's edit. */
	private static boolean light(ServerLevel level, ServerPlayer player, BlockPos cell, Direction face) {
		if (!ManipulationCombatHelper.mayAlter(player, cell.relative(face))) return false;
		BlockState there = level.getBlockState(cell);
		if (there.getBlock() instanceof LuxTraceBlock && MultifaceBlock.hasFace(there, face)) return false;
		BlockState trace = (there.getBlock() instanceof LuxTraceBlock ? there : BlockInit.lux_trace.get().defaultBlockState())
				.setValue(MultifaceBlock.getFaceProperty(face), true).setValue(LuxTraceBlock.FADE, 0);
		return there.isAir() ? PlayerBlockEdits.place(player, cell, trace) : PlayerBlockEdits.replace(player, cell, trace);
	}

	private static Direction inPlane(Direction face, RandomSource random) {
		List<Direction> headings = new ArrayList<>(4);
		for (Direction heading : Direction.values()) if (heading.getAxis() != face.getAxis()) headings.add(heading);
		return headings.get(random.nextInt(headings.size()));
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		PATTERNS.remove(player.getUUID());
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	@Override
	public double reachScale() {
		return REACH_SCALE;
	}

	/** Cells the player's held pattern has lit so far; for tests. */
	public static int patternSize(Player player) {
		Pattern pattern = PATTERNS.get(player.getUUID());
		return pattern == null ? 0 : pattern.cells;
	}

	public static void clearSessionState() {
		PATTERNS.clear();
	}

	public static void logout(Player player) {
		PATTERNS.remove(player.getUUID());
	}
}
