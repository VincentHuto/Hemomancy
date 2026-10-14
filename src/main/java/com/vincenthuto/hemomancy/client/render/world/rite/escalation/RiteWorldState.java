package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Each Harbinger rite's hold on the world around it, eased from its synced stage. A rite seen for the first time
 * snaps to its stage, so arriving mid-rite shows the world as it already is; later steps ease in behind a surge.
 * When a rite stops being synced, its ghost decides how the world lets go.
 */
public final class RiteWorldState {
	static final float EASE = 0.08F;
	static final float SURGE_DECAY = 0.88F;

	/** One rite's world, as the layers read it. */
	public record World(BlockPos center, UUID owner, float level, float previousLevel, float surge, int stage,
			int total, CardinalRiteInstability strain, float footprint, String fogProfile, boolean lightning,
			CardinalRitePhase phase, RiteFloor floor, RiteWorldMath.Ending ending, float endingAge) {
		/** The eased level between ticks. */
		public float level(float partialTick) {
			return previousLevel + (level - previousLevel) * RiteWorldMath.clamp01(partialTick);
		}

		public boolean ended() {
			return ending != RiteWorldMath.Ending.NONE;
		}

		public boolean still() {
			return phase == CardinalRitePhase.STILL_INTERVAL;
		}

		public boolean culminating() {
			return phase == CardinalRitePhase.CULMINATION;
		}
	}

	/** How a rite's world bends a brazier's fire. */
	public record Flame(float height, float leanX, float leanZ, boolean sanguine) {
		public static final Flame CALM = new Flame(1.0F, 0.0F, 0.0F, false);
	}

	private static final class Tracked {
		private ActiveRiteClientData.RiteEntry entry;
		private float target;
		private float level;
		private float previous;
		private float surge;
		private int stage;
		private RiteWorldMath.Ending ending = RiteWorldMath.Ending.NONE;
		private float endingAge;
		private RiteFloor floor = RiteFloor.NONE;
		private boolean seen;
	}

	private static final Map<BlockPos, Tracked> TRACKED = new HashMap<>();
	private static List<World> worlds = List.of();

	private RiteWorldState() {
	}

	/** Advances one client tick from the synced rites, the ghosts of ended ones, and the floors under them. */
	public static void tick(List<ActiveRiteClientData.RiteEntry> active,
			List<CardinalRiteEventClientState.Ghost> ghosts, Function<BlockPos, RiteFloor> floors) {
		for (Tracked tracked : TRACKED.values()) tracked.seen = false;
		for (ActiveRiteClientData.RiteEntry entry : active) {
			if (entry.isUnstained() || entry.isLegacy() || entry.getEscalationTotal() <= 0) continue;
			float target = RiteWorldMath.level(entry.getEscalation(), entry.getEscalationTotal());
			Tracked tracked = TRACKED.get(entry.getCenter());
			if (tracked == null || tracked.ending != RiteWorldMath.Ending.NONE) {
				// First sight, or a new rite on a Focus whose last rite is still letting go: the world is already as
				// far as the rite has pushed it.
				tracked = new Tracked();
				tracked.level = target;
				tracked.previous = target;
				tracked.stage = entry.getEscalation();
				TRACKED.put(entry.getCenter(), tracked);
			} else if (entry.getEscalation() > tracked.stage) {
				tracked.surge = 1.0F;
				tracked.stage = entry.getEscalation();
			}
			tracked.entry = entry;
			tracked.target = target;
			tracked.seen = true;
		}
		List<World> next = new ArrayList<>(TRACKED.size());
		Iterator<Map.Entry<BlockPos, Tracked>> iterator = TRACKED.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<BlockPos, Tracked> mapped = iterator.next();
			Tracked tracked = mapped.getValue();
			if (!tracked.seen) {
				RiteWorldMath.Ending ending = endingOf(ghostAt(ghosts, mapped.getKey()));
				if (tracked.ending == RiteWorldMath.Ending.NONE) {
					tracked.ending = ending;
					tracked.endingAge = 0.0F;
				} else {
					// The beat that names an ending can arrive just after the snapshot that dropped the rite.
					if (tracked.ending == RiteWorldMath.Ending.VANISHED) tracked.ending = ending;
					tracked.endingAge++;
				}
				if (tracked.endingAge > RiteWorldMath.endingTicks(tracked.ending)) {
					iterator.remove();
					continue;
				}
			}
			tracked.previous = tracked.level;
			if (tracked.ending == RiteWorldMath.Ending.NONE) tracked.level += (tracked.target - tracked.level) * EASE;
			tracked.surge *= SURGE_DECAY;
			RiteFloor floor = floors.apply(mapped.getKey());
			tracked.floor = floor == null ? RiteFloor.NONE : floor;
			next.add(world(mapped.getKey(), tracked));
		}
		worlds = List.copyOf(next);
	}

	public static List<World> worlds() {
		return worlds;
	}

	public static World at(BlockPos center) {
		for (World world : worlds) {
			if (world.center().equals(center)) return world;
		}
		return null;
	}

	/** A ring tore open at this Focus: the world surges without stepping. Unknown Foci are ignored. */
	public static void ringOpened(BlockPos center, float surge) {
		Tracked tracked = TRACKED.get(center);
		if (tracked != null) tracked.surge = Math.max(tracked.surge, surge);
	}

	public static void clear() {
		TRACKED.clear();
		worlds = List.of();
	}

	/**
	 * The floor shader's {@code RiteBlood}: the trench's fill, its overflow, the creep's radius from the Focus
	 * centre and the creep's strength. A collapse tears the creep back to the floor; a completion sinks it.
	 */
	public static float[] bloodUniform(World world, float partialTick) {
		float[] blood = new float[4];
		if (world == null) return blood;
		float level = world.level(partialTick);
		float ground = RiteWorldMath.groundEnding(world.ending(), world.endingAge());
		blood[0] = RiteWorldMath.trenchFill(level) * ground;
		blood[1] = RiteWorldMath.overflow(level, world.total()) * ground;
		if (!world.floor().formed()) return blood;
		float radius = RiteWorldMath.creepRadius(world.floor().edge(), world.footprint(), level, world.total(),
				world.strain());
		if (radius <= 0.0F) return blood;
		float edge = world.floor().edge();
		float tearing = world.ending() == RiteWorldMath.Ending.COLLAPSED ? ground : 1.0F;
		blood[2] = edge + (radius - edge) * tearing;
		// A collapse tears the creep back and away together, so no fringe outlasts the tear.
		blood[3] = ground;
		return blood;
	}

	/** Braziers within a rite's footprint lean toward its Focus, burn taller and redder, and gutter under strain. */
	public static Flame flameAt(BlockPos pos, double time) {
		for (World world : worlds) {
			double dx = world.center().getX() - pos.getX();
			double dz = world.center().getZ() - pos.getZ();
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (distance > world.footprint() + 2.0D || Math.abs(world.center().getY() - pos.getY()) > 4) continue;
			float level = world.level(1.0F) * RiteWorldMath.groundEnding(world.ending(), world.endingAge());
			if (level <= 0.0F) return Flame.CALM;
			float beat = CardinalRiteMath.heartbeat((float) time);
			float flicker = world.strain() == CardinalRiteInstability.STRAINED
					? 0.85F + 0.15F * (float) Math.sin(time * 0.9D + (pos.asLong() & 1023L)) : 1.0F;
			float height = RiteWorldMath.flameHeight(level, world.strain(), beat, flicker);
			double lean = Math.tan(Math.toRadians(RiteWorldMath.flameLeanDegrees(level)));
			float leanX = distance < 0.5D ? 0.0F : (float) (dx / distance * lean);
			float leanZ = distance < 0.5D ? 0.0F : (float) (dz / distance * lean);
			return new Flame(height, leanX, leanZ, RiteWorldMath.flameSanguine(level));
		}
		return Flame.CALM;
	}

	private static World world(BlockPos center, Tracked tracked) {
		ActiveRiteClientData.RiteEntry entry = tracked.entry;
		return new World(center, entry.getOwner(), tracked.level, tracked.previous, tracked.surge, tracked.stage,
				entry.getEscalationTotal(), CardinalRiteCeremonyRules.instabilityBand(entry.getInstability()),
				entry.getFootprintRadius(), entry.getFogProfile(), entry.hasFogLightning(), entry.ritePhase(),
				tracked.floor, tracked.ending, tracked.endingAge);
	}

	private static CardinalRiteEventClientState.Ghost ghostAt(List<CardinalRiteEventClientState.Ghost> ghosts,
			BlockPos center) {
		for (CardinalRiteEventClientState.Ghost ghost : ghosts) {
			if (ghost.center().equals(center)) return ghost;
		}
		return null;
	}

	private static RiteWorldMath.Ending endingOf(CardinalRiteEventClientState.Ghost ghost) {
		if (ghost == null) return RiteWorldMath.Ending.VANISHED;
		return switch (ghost.kind()) {
			case COMPLETED -> RiteWorldMath.Ending.COMPLETED;
			case COLLAPSED, BROKEN -> RiteWorldMath.Ending.COLLAPSED;
			case CANCELLED -> RiteWorldMath.Ending.CANCELLED;
			default -> RiteWorldMath.Ending.VANISHED;
		};
	}
}
