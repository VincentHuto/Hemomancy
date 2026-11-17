package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.entity.summon.EntityIronWall;
import com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.SweepTrail;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

/**
 * Ferric Rampart as a Sculpting of Projection (core-loop round two P4): the iron wall is drawn along the path the player
 * sweeps. Each pulse raises one block-wide segment of blood-iron, three tall, on the floor under the aim, its long side
 * along the sweep, and joins it to the last segment across up to three cells. Each segment is a rampart in every way
 * (it stops hostile shots and lets the caster's through) and stands as long as the cast wall does, less when the pulse
 * ran dry. A player keeps at
 * most {@link #MAX_SEGMENTS} standing; beyond that the stream is refused, unpaid, until some fall. Nothing is raised
 * where a body stands. The R cast (a five-wide wall across the aim) is unchanged.
 */
public final class FerricRampartSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 20.0D;
	public static final int CADENCE = 10;
	public static final double REACH_SCALE = 1.5D;
	/** A sweep is joined up across at most this many cells. */
	public static final int JOIN = 3;
	/** Segments one player may have standing at once. */
	public static final int MAX_SEGMENTS = 12;
	/** How far from the caster their standing segments are counted. */
	private static final double COUNT_RANGE = 64.0D;

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	/**
	 * A face with room in front of it (the ice wall's rule); never a block that takes blood. Where no segment can stand
	 * (no sturdy floor in reach, something in the way) the pulse raises nothing there.
	 */
	@Override
	public boolean claims(Player player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block) || StrandSculpting.takesBlood(player.level(), block)) return false;
		var front = player.level().getBlockState(block.pos().relative(block.hit().getDirection()));
		return front.isAir() || front.canBeReplaced();
	}

	/** The column a segment stands in for this aim: the cell just in front of the struck face. */
	static BlockPos column(VerbTarget.Block block) {
		return BlockPos.containing(block.point().add(Vec3.atLowerCornerOf(block.hit().getDirection().getNormal()).scale(0.02D)));
	}

	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block) || !ManipulationCombatHelper.mayAlter(player, column(block))) {
			player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		if (standing(player) < MAX_SEGMENTS) return true;
		player.displayClientMessage(Component.translatable("message.hemomancy.ferric_rampart.too_many", MAX_SEGMENTS)
				.withStyle(ChatFormatting.GOLD), true);
		return false;
	}

	/** The player's segments standing near them. */
	public static int standing(Player player) {
		return player.level().getEntitiesOfClass(FerricConstructEntity.class, player.getBoundingBox().inflate(COUNT_RANGE),
				entity -> entity.isAlive() && entity.constructKind() == Kind.SEGMENT
						&& entity.ownerId().map(player.getUUID()::equals).orElse(false)).size();
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		BlockPos cell = column(block);
		BlockPos last = SweepTrail.last(player, this).orElse(null);
		Direction facing = facing(player, last, cell);
		List<BlockPos> cells = SweepTrail.sweep(player, this, cell, JOIN);
		int room = MAX_SEGMENTS - standing(player);
		int duration = SculptingRules.standingTicks(FerricBarrierManip.RAMPART_DURATION_TICKS, pulse.workScale());
		int raised = 0;
		for (BlockPos at : cells) {
			if (raised >= room) break;
			Vec3 center = FerricPlacement.segment(player, at, facing);
			if (center == null || player.getBoundingBox().intersects(FerricConstructShapes.bounds(Kind.SEGMENT, facing, center))) continue;
			EntityIronWall segment = new EntityIronWall(EntityInit.ferric_rampart.get(), level, player);
			segment.moveTo(center.x, center.y, center.z, facing.toYRot(), 0.0F);
			segment.configure(player, Kind.SEGMENT, facing, duration);
			if (level.addFreshEntity(segment)) raised++;
		}
		if (raised > 0) level.playSound(null, cell, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4F, 0.9F);
	}

	/**
	 * Which way a segment faces: across the sweep, so its long side lies along it. The first segment of a sweep has no
	 * direction yet and faces the player, as the cast wall does.
	 */
	static Direction facing(Player player, BlockPos last, BlockPos cell) {
		if (last != null) {
			int dx = cell.getX() - last.getX();
			int dz = cell.getZ() - last.getZ();
			if (dx != 0 || dz != 0) {
				Direction along = Math.abs(dx) >= Math.abs(dz)
						? (dx > 0 ? Direction.EAST : Direction.WEST)
						: (dz > 0 ? Direction.SOUTH : Direction.NORTH);
				return along.getClockWise();
			}
		}
		return Direction.fromYRot(player.getYRot());
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		if (released) SweepTrail.forget(player);
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
}
