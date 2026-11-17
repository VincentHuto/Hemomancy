package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind;
import com.vincenthuto.hemomancy.common.manipulation.verb.HeldSkills;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

/**
 * Ferric Spikes under held Projection: a run of blades the caster steers along the ground. A cluster of iron breaks
 * the ground at their feet and travels toward wherever they look, like a fin through water, turning as the aim turns.
 * What it passes under is impaled and takes Lodestone. It needs ground to run in: it climbs a block and drops two, and
 * stops at a gap, at a wall or at the end of its leash. The attack button makes it erupt where it is, as a clump of
 * standing blades (the R cast's own) that blocks the way for a few seconds, and the run is spent.
 *
 * <p>This is not the drawn wall: Ferric Rampart lays one along a swept path and it stands. The run is one moving thing.
 */
public final class FerricRunSculpting implements VerbSculpting {
	/** One caster's run: where it is in the ground, whether it is stuck, and when it last bit each body. */
	private static final class Run {
		Vec3 at;
		boolean stuck;
		int ticks;
		final Map<Integer, Long> bitten = new HashMap<>();

		Run(Vec3 at) {
			this.at = at;
		}
	}

	private static final Map<UUID, Run> RUNS = new HashMap<>();
	/** Player to the game time a new run may start after an eruption. */
	private static final Map<UUID, Long> SPENT = new HashMap<>();

	@Override
	public Set<VerbTargetKind> wants() {
		return EnumSet.of(VerbTargetKind.CREATURE, VerbTargetKind.BLOCK, VerbTargetKind.AIR);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return !(target instanceof VerbTarget.Block block) || !StrandSculpting.takesBlood(player.level(), block);
	}

	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		Long back = SPENT.get(player.getUUID());
		if (back != null && player.level().getGameTime() < back) return false;
		// A run needs ground to start in: none under a caster in the air.
		return RUNS.containsKey(player.getUUID()) || start(player) != null;
	}

	@Override
	public double bloodPerPulse() {
		return FerricRunRules.BLOOD_PER_PULSE;
	}

	/** It draws itself: the hand pours no ordinary stream toward the aim while this holds the channel. */
	@Override
	public boolean drawsOwnStream() {
		return true;
	}

	@Override
	public boolean holdsChannel() {
		return true;
	}

	@Override
	public boolean takesAccent() {
		return true;
	}

	@Override
	public OptionalDouble movement(Player player) {
		return HeldSkills.pace(player, FerricRunRules.PACE);
	}

	@Override
	public double reachScale(Player player) {
		return HeldSkills.reach(player);
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		Vec3 start = start(player);
		if (start == null) return;
		RUNS.put(player.getUUID(), new Run(start));
		player.level().playSound(null, BlockPos.containing(start), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.4F, 1.5F);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
	}

	@Override
	public void hold(ServerPlayer player, VerbTarget aimed) {
		Run run = RUNS.get(player.getUUID());
		if (run == null || !(player.level() instanceof ServerLevel level)) return;
		run.ticks++;
		travel(player, level, run, steer(player));
		bite(player, level, run);
		// The blades break the ground afresh every other tick: the run reads as one moving ridge of iron.
		if (run.ticks % 2 == 0) {
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.STRAND_ACCENT, run.at, run.at.add(0.0D, 1.0D, 0.0D), 0.7D, 12,
					StrandCue.encodeAccent(StrandCue.Accent.ERUPTION, EnumBloodTemper.FERRIC, null));
		}
		if (run.ticks % 6 == 0 && !run.stuck) {
			level.playSound(null, BlockPos.containing(run.at), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.35F, 1.6F);
		}
	}

	/**
	 * Where the run is steered to: the spot on the world the caster is looking at, out to the run's leash. Creatures are
	 * looked through, so an ally or a victim standing in the line of sight never stops the run short of the ground
	 * behind it.
	 */
	private static Vec3 steer(Player player) {
		Vec3 eye = player.getEyePosition();
		Vec3 far = eye.add(player.getLookAngle().scale(FerricRunRules.LEASH));
		return player.level().clip(new net.minecraft.world.level.ClipContext(eye, far, net.minecraft.world.level.ClipContext.Block.COLLIDER,
				net.minecraft.world.level.ClipContext.Fluid.NONE, player)).getLocation();
	}

	/** One stride toward the aim, following the ground; the run sticks where the ground will not carry it. */
	private static void travel(ServerPlayer player, ServerLevel level, Run run, Vec3 aim) {
		double[] next = FerricRunRules.stride(run.at.x, run.at.z, aim.x, aim.z);
		if (next[0] == run.at.x && next[1] == run.at.z) {
			run.stuck = false;
			return;
		}
		Double ground = ground(level, next[0], run.at.y, next[1]);
		Vec3 to = ground == null ? null : new Vec3(next[0], ground, next[1]);
		run.stuck = to == null || !FerricRunRules.leashed(to.distanceTo(player.position()));
		if (!run.stuck) run.at = to;
	}

	private static void bite(ServerPlayer player, ServerLevel level, Run run) {
		BloodManipulation spikes = manipulation(player);
		if (spikes == null) return;
		long now = level.getGameTime();
		AABB reach = new AABB(run.at, run.at).inflate(FerricRunRules.BITE_RADIUS + 0.5D, FerricRunRules.BITE_HEIGHT, FerricRunRules.BITE_RADIUS + 0.5D);
		float damage = (float) (FerricRunRules.BITE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
		try (var cast = SchoolDamage.cast(spikes, player, 1)) {
			for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class, reach, body -> ManipulationCombatHelper.canHarm(player, body))) {
				double dx = body.getX() - run.at.x, dz = body.getZ() - run.at.z;
				if (Math.sqrt(dx * dx + dz * dz) > FerricRunRules.BITE_RADIUS + body.getBbWidth() * 0.5D) continue;
				Long last = run.bitten.get(body.getId());
				if (last != null && now - last < FerricRunRules.BITE_LOCKOUT) continue;
				if (ManipulationCombatHelper.hurt(spikes, player, body, level, damage, FerricRunRules.LODESTONE_TICKS, 1)) {
					run.bitten.put(body.getId(), now);
				}
			}
		}
	}

	@Override
	public boolean accent(ServerPlayer player) {
		Run run = RUNS.get(player.getUUID());
		BloodManipulation spikes = manipulation(player);
		if (run == null || spikes == null || !(player.level() instanceof ServerLevel level)) return false;
		// The blades stand across the way the caster faces, as the R cast's do.
		Direction facing = player.getDirection();
		Vec3 across = new Vec3(facing.getClockWise().getStepX(), 0.0D, facing.getClockWise().getStepZ());
		try (var cast = SchoolDamage.cast(spikes, player, 1)) {
			for (int i = 0; i < FerricRunRules.ERUPTION_BLADES; i++) {
				Vec3 beside = run.at.add(across.scale(FerricRunRules.bladeOffset(i)));
				Double ground = ground(level, beside.x, run.at.y, beside.z);
				if (ground == null) continue;
				EntityIronSpike blade = new EntityIronSpike(EntityInit.iron_spike.get(), level, player);
				blade.moveTo(beside.x, ground, beside.z, facing.toYRot(), 0.0F);
				blade.configure(player, Kind.SPIKE, facing, HeldSkills.accentState(player, FerricRunRules.ERUPTION_TICKS));
				level.addFreshEntity(blade);
			}
		}
		level.playSound(null, BlockPos.containing(run.at), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.65F, 0.72F);
		SPENT.put(player.getUUID(), level.getGameTime() + FerricRunRules.SPENT_TICKS);
		SculptingSessions.end(player, false);
		return true;
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		RUNS.remove(player.getUUID());
	}

	/** Where a run starts: the ground a block ahead of the caster, or under them, or nowhere. */
	@Nullable
	private static Vec3 start(Player player) {
		Vec3 ahead = player.position().add(player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize());
		Double ground = ground(player.level(), ahead.x, player.getY(), ahead.z);
		if (ground != null) return new Vec3(ahead.x, ground, ahead.z);
		ground = ground(player.level(), player.getX(), player.getY(), player.getZ());
		return ground == null ? null : new Vec3(player.getX(), ground, player.getZ());
	}

	/**
	 * The height of the ground the run can stand in at ({@code x}, {@code z}), looking from a block above {@code from}
	 * down as far as it may drop: a sturdy top with open space over it. Null where there is none in that span.
	 */
	@Nullable
	private static Double ground(Level level, double x, double from, double z) {
		int base = (int) Math.floor(from + 1.0E-4D);
		for (int rise = FerricRunRules.MAX_CLIMB; rise >= -FerricRunRules.MAX_DROP; rise--) {
			BlockPos open = BlockPos.containing(x, base + rise, z);
			BlockPos floor = open.below();
			if (level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
					&& level.getBlockState(open).getCollisionShape(level, open).isEmpty()) {
				return (double) open.getY();
			}
		}
		return null;
	}

	/** Where {@code player}'s run is, or null with none out. */
	@Nullable
	public static Vec3 at(Player player) {
		Run run = RUNS.get(player.getUUID());
		return run == null ? null : run.at;
	}

	private static BloodManipulation manipulation(Player player) {
		SculptingSessions.Session session = SculptingSessions.current(player);
		return session == null ? null : ManipulationInit.getByName(session.manipulation());
	}

	public static void clearSessionState() {
		RUNS.clear();
		SPENT.clear();
	}

	public static void logout(Player player) {
		RUNS.remove(player.getUUID());
		SPENT.remove(player.getUUID());
	}
}
