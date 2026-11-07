package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The liquid dark in the fight (design revision 2026-11-06, R.3): every pool and strip of dark is an owned
 * {@link BossGroundHazardEntity} of kind {@link VelorumEntity#HAZARD_DARK_POOL}, so it goes with the trial and the client
 * draws it from its synced shape and age. A pool is a {@code Disc} whose radius is its cap (its drawn and tested radius
 * grows from its age by {@link VelorumDarkRules#poolRadius}); a strip is a {@code Lane} whose front runs out behind a
 * shade; the Eclipse's dark is a {@code Region} over the chamber. Once a tick this decides who stands in the dark (in a
 * pool, outside his lantern's light, away from an ally's warmth, not Searing), tracks the Gutter's flood, and holds the
 * pools to 45% of the boss floor by fading the oldest. Fight state only; nothing here outlives the trial.
 */
public final class VelorumDark {
	/** How often the 45% cap is checked; the pools grow between checks. */
	private static final int CAP_EVERY = 10;
	/** A pool's hazard pulse: it applies nothing on its own, the dark is read here. */
	private static final int POOL_PULSE = 20;
	/** At most this many fades in one check of the cap. */
	private static final int MAX_FADES = 8;

	private final List<UUID> pools = new ArrayList<>();
	@Nullable
	private UUID eclipse;
	private final Set<UUID> dark = new HashSet<>();
	private int gutterAge = -1;
	private int sinceGutter = -1;
	private int flood;
	/** The fight's own tick count here, and how long ago the Eclipse was relit (-1 for not lately). */
	private int ticks;
	private int relitAge = -1;
	private float lantern = VelorumDarkRules.lanternRest(1);

	// ── what the moves leave ─────────────────────────────────────────

	/** A pool of liquid dark at {@code centre}, pulled onto the boss floor or the leash ring round it. */
	@Nullable
	public BossGroundHazardEntity pool(VelorumEntity velorum, Vec3 centre, double cap, int life) {
		SaintArena arena = velorum.arena();
		if (arena == null || !(velorum.level() instanceof ServerLevel server)) return null;
		Vec3 at = onFloor(arena, centre);
		BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(server, velorum, VelorumEntity.HAZARD_DARK_POOL,
				new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, cap, BossFootprint.Height.STANDING),
				frame(arena, at, at), life, POOL_PULSE);
		arena.own(hazard, null);
		pools.add(hazard.getUUID());
		VelorumMoves.sound(velorum, at, SoundEvents.SCULK_BLOCK_SPREAD, 0.4F, 0.5F);
		return hazard;
	}

	/** A strip of dark left along a road behind a shade, its front running out at {@code speed} blocks a tick. */
	@Nullable
	public BossGroundHazardEntity strip(VelorumEntity velorum, Vec3 from, Vec3 to, double speed, int life) {
		SaintArena arena = velorum.arena();
		if (arena == null || !(velorum.level() instanceof ServerLevel server)) return null;
		Vec3 a = onFloor(arena, from), b = onFloor(arena, to);
		double length = BossFootprint.Shapes.planar(a, b);
		if (length < 0.5D) return null;
		BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(server, velorum, VelorumEntity.HAZARD_DARK_POOL,
				new BossFootprint.Lane(length, VelorumDarkRules.STRIP_HALF_WIDTH, 0, 0.0D, 0.0D, speed, 0,
						BossFootprint.Height.STANDING), frame(arena, a, b), life, POOL_PULSE);
		arena.own(hazard, null);
		pools.add(hazard.getUUID());
		return hazard;
	}

	/** The Eclipse's dark over the whole chamber for its hold. */
	public void eclipse(VelorumEntity velorum, int life) {
		SaintArena arena = velorum.arena();
		if (arena == null || !(velorum.level() instanceof ServerLevel server)) return;
		Vec3 at = velorum.position();
		BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(server, velorum, VelorumEntity.HAZARD_DARK_POOL,
				new BossFootprint.Region(BossFootprint.Area.CHAMBER, 0.0D, BossFootprint.Height.STANDING),
				frame(arena, at, at), life, POOL_PULSE);
		arena.own(hazard, null);
		eclipse = hazard.getUUID();
	}

	/** The Eclipse's dark lifts (relit or unbroken). */
	public void endEclipse(VelorumEntity velorum, int fade) {
		BossGroundHazardEntity hazard = hazard(velorum, eclipse);
		if (hazard != null) hazard.expire(fade);
		eclipse = null;
	}

	/** Relit, the light tears the dark back to the walls: every pool fades, and the lantern flares. */
	public void tearBack(VelorumEntity velorum, int fade) {
		for (UUID id : pools) {
			BossGroundHazardEntity hazard = hazard(velorum, id);
			if (hazard != null) hazard.expire(fade);
		}
		relitAge = 0;
		VelorumMoves.sound(velorum, velorum.position(), com.vincenthuto.hemomancy.common.init.SoundInit.RITE_SUBJECT_SINK.get(), 0.3F, 0.8F);
	}

	/** Every pool fades now: the ending. */
	public void fadeAll(VelorumEntity velorum, int fade) {
		for (UUID id : pools) {
			BossGroundHazardEntity hazard = hazard(velorum, id);
			if (hazard != null) hazard.expire(fade);
		}
		endEclipse(velorum, fade);
	}

	// ── each tick ────────────────────────────────────────────────────

	public void tick(VelorumEntity velorum, List<Player> challengers) {
		SaintArena arena = velorum.arena();
		ticks++;
		if (relitAge >= 0 && ++relitAge >= VelorumDarkRules.RELIT_FLARE + VelorumDarkRules.RELIT_EASE) relitAge = -1;
		trackGutter(velorum);
		lantern = lanternNow(velorum);
		List<BossGroundHazardEntity> live = live(velorum);
		if (arena != null && ticks % CAP_EVERY == 0) holdCap(velorum, arena, live);
		dark.clear();
		for (Player player : challengers) {
			boolean warmed = velorum.congregation().warms(player);
			boolean searing = SchoolStates.has(player, SchoolState.SEARING);
			boolean inPool = arena != null && inPool(arena, live, player.position(), false);
			boolean lit = VelorumDarkRules.lit(planar(player.position(), velorum.position()), lantern);
			if (VelorumDarkRules.inDark(inPool, lit, warmed, searing, velorum.eclipseHolding())) {
				dark.add(player.getUUID());
				SaintLines.teach(player, "message.hemomancy.velorum.hint.dark");
			}
		}
	}

	public boolean inDark(Player player) {
		return dark.contains(player.getUUID());
	}

	/** Whether a point (a hostile devotee's feet) stands in a live pool outside his light. */
	public boolean darkAt(VelorumEntity velorum, Vec3 point) {
		SaintArena arena = velorum.arena();
		if (velorum.eclipseHolding()) return true;
		if (arena == null || VelorumDarkRules.lit(planar(point, velorum.position()), lantern)) return false;
		return inPool(arena, live(velorum), point, false);
	}

	/** {@code L} as it stands this tick. */
	public float lantern() {
		return lantern;
	}

	/** How much of the boss floor the pools cover on their own curves (the Gutter's flood and the Eclipse aside). */
	public double coverage(VelorumEntity velorum) {
		SaintArena arena = velorum.arena();
		if (arena == null) return 0.0D;
		return coverage(arena, live(velorum));
	}

	public int pools(VelorumEntity velorum) {
		return live(velorum).size();
	}

	public void clear() {
		pools.clear();
		dark.clear();
		eclipse = null;
		gutterAge = -1;
		sinceGutter = -1;
		flood = 0;
		relitAge = -1;
	}

	// ── the lantern and the Gutter's flood ───────────────────────────

	private void trackGutter(VelorumEntity velorum) {
		SaintOpening.Spec open = velorum.openings().current();
		if (open != null && open.id() == VelorumOpeningRules.GUTTER_ID) {
			gutterAge = velorum.openings().age();
			flood = gutterAge;
			sinceGutter = -1;
		} else if (gutterAge >= 0) {
			gutterAge = -1;
			sinceGutter = 0;
		} else if (sinceGutter >= 0) {
			sinceGutter++;
			if (sinceGutter > VelorumDarkRules.FLOOD_RETURN) {
				sinceGutter = -1;
				flood = 0;
			}
		}
	}

	private float lanternNow(VelorumEntity velorum) {
		SaintMove move = velorum.controller().move();
		BossMoveTimeline timeline = velorum.controller().timeline();
		int tick = velorum.controller().moveTick();
		BossMoveTimeline.Stage stage = velorum.controller().stage();
		float night = 0.0F, eclipseOut = 0.0F;
		if (move == VelorumMove.LONG_NIGHT || move == VelorumMove.ECLIPSE) {
			float build = stage == BossMoveTimeline.Stage.WINDUP ? tick / (float) Math.max(1, timeline.windup())
					: stage == BossMoveTimeline.Stage.ACTIVE ? 1.0F : 0.0F;
			if (move == VelorumMove.LONG_NIGHT) night = build;
			else eclipseOut = velorum.eclipseRelit() ? 0.0F : build;
		}
		return VelorumDarkRules.lanternRadius(velorum.phases().phase(), night, gutterAge, sinceGutter, eclipseOut, relitAge);
	}

	// ── the pools ────────────────────────────────────────────────────

	private List<BossGroundHazardEntity> live(VelorumEntity velorum) {
		List<BossGroundHazardEntity> live = new ArrayList<>();
		pools.removeIf(id -> {
			BossGroundHazardEntity hazard = hazard(velorum, id);
			if (hazard == null) return true;
			if (!hazard.fading()) live.add(hazard);
			return false;
		});
		return live;
	}

	/** Whether {@code point} is inside a live pool or strip on the leash box; {@code own} ignores the Gutter's flood. */
	private boolean inPool(SaintArena arena, List<BossGroundHazardEntity> live, Vec3 point, boolean own) {
		if (!onLeash(arena, point.x, point.z)) return false;
		for (BossGroundHazardEntity hazard : live) {
			if (covers(hazard, point.x, point.z, own)) return true;
		}
		return false;
	}

	private boolean covers(BossGroundHazardEntity hazard, double x, double z, boolean own) {
		BossFootprint shape = hazard.shape();
		FootprintFrame frame = hazard.frame();
		Vec3 point = new Vec3(x, frame.floorY(), z);
		if (shape instanceof BossFootprint.Disc disc) {
			double radius = own ? VelorumDarkRules.ownRadius(disc.radius(), hazard.tickCount)
					: VelorumDarkRules.poolRadius(disc.radius(), hazard.tickCount, flood, sinceGutter);
			return planar(frame.origin(), point) <= radius;
		}
		if (shape instanceof BossFootprint.Lane lane) return lane.hits(frame, point, hazard.tickCount);
		return false;
	}

	private double coverage(SaintArena arena, List<BossGroundHazardEntity> live) {
		AABB floor = arena.bossFloor();
		return VelorumDarkRules.coverage(floor.minX, floor.minZ, floor.maxX, floor.maxZ, (x, z) -> {
			for (BossGroundHazardEntity hazard : live) if (covers(hazard, x, z, true)) return true;
			return false;
		});
	}

	/** Past 45% of the boss floor the oldest pool fades first. */
	private void holdCap(VelorumEntity velorum, SaintArena arena, List<BossGroundHazardEntity> live) {
		List<BossGroundHazardEntity> left = new ArrayList<>(live);
		for (int i = 0; i < MAX_FADES && !left.isEmpty(); i++) {
			if (!VelorumDarkRules.overCap(coverage(arena, left))) return;
			left.remove(0).expire(VelorumDarkRules.EXPIRE_TICKS);
		}
	}

	@Nullable
	private static BossGroundHazardEntity hazard(VelorumEntity velorum, @Nullable UUID id) {
		if (id == null || !(velorum.level() instanceof ServerLevel server)) return null;
		Entity entity = server.getEntity(id);
		return entity instanceof BossGroundHazardEntity hazard && !hazard.isRemoved() ? hazard : null;
	}

	private static FootprintFrame frame(SaintArena arena, Vec3 origin, Vec3 aim) {
		return new FootprintFrame(origin, aim, aim, arena.floorY(), List.of(), LongSets.EMPTY_SET, List.of(),
				arena.bossFloor(), arena.chamber());
	}

	/** The leash box: the boss floor and the block round it; the aisles stay clear of the dark. */
	static AABB leash(SaintArena arena) {
		return arena.bossFloor().inflate(VelorumDarkRules.LEASH, 0.0D, VelorumDarkRules.LEASH);
	}

	private static boolean onLeash(SaintArena arena, double x, double z) {
		AABB box = leash(arena);
		return VelorumDarkRules.leashed(x, z, box.minX, box.minZ, box.maxX, box.maxZ);
	}

	static Vec3 onFloor(SaintArena arena, Vec3 point) {
		AABB box = leash(arena);
		double[] at = VelorumDarkRules.onLeash(point.x, point.z, box.minX, box.minZ, box.maxX, box.maxZ);
		return new Vec3(at[0], arena.floorY(), at[1]);
	}

	private static double planar(Vec3 a, Vec3 b) {
		return BossFootprint.Shapes.planar(a, b);
	}
}
