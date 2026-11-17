package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The one pick for both verbs, used by the server chain and by the hand's stream on the client. It looks along the
 * player's current look ray for the kinds a Sculpting {@code wants} and returns the nearest one. Blocks stop the ray;
 * a creature or a heap of items in front of the block is nearer and wins. Without {@link VerbTargetKind#FLUID} a
 * fluid surface reads as its block, exactly as plain Projection has always aimed.
 */
public final class VerbTargeting {
	/** How far past an item entity's own box the aim still finds it: items are small. */
	static final double ITEM_AIM_MARGIN = 0.35D;
	/** Items lying this close to the one aimed at come with it. */
	static final double ITEM_HEAP_RADIUS = 1.25D;
	static final int ITEM_HEAP_LIMIT = 16;
	private static final double EPSILON = 1.0E-4D;

	private VerbTargeting() {
	}

	public static Optional<VerbTarget> pick(Level level, Entity viewer, double reach, Set<VerbTargetKind> wants) {
		return pick(level, viewer, viewer.getEyePosition(), viewer.getLookAngle(), reach, wants);
	}

	public static Optional<VerbTarget> pick(Level level, Entity viewer, Vec3 eye, Vec3 look, double reach,
			Set<VerbTargetKind> wants) {
		if (wants.isEmpty() || reach <= 0.0D) return Optional.empty();
		Vec3 end = SanguineProjectionTargeting.rayEnd(eye, look, reach);
		boolean wantsFluid = wants.contains(VerbTargetKind.FLUID);
		BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
				wantsFluid ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY, viewer));
		boolean blocked = blockHit.getType() == HitResult.Type.BLOCK;
		double occluder = blocked ? eye.distanceTo(blockHit.getLocation()) : eye.distanceTo(end);
		Vec3 reachEnd = blocked ? blockHit.getLocation() : end;

		List<Hit<VerbTarget>> hits = new ArrayList<>();
		if (blocked) {
			hits.add(new Hit<>(VerbTargetKind.BLOCK, occluder,
					new VerbTarget.Block(blockHit, level.getBlockState(blockHit.getBlockPos()))));
		}
		if (wantsFluid) {
			BlockHitResult fluidHit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
					ClipContext.Fluid.ANY, viewer));
			if (fluidHit.getType() == HitResult.Type.BLOCK) {
				BlockPos pos = fluidHit.getBlockPos();
				FluidState fluid = level.getFluidState(pos);
				if (!fluid.isEmpty()) {
					hits.add(new Hit<>(VerbTargetKind.FLUID, eye.distanceTo(fluidHit.getLocation()),
							new VerbTarget.Fluid(pos, fluid, fluidHit.getLocation())));
				}
			}
		}
		if (wants.contains(VerbTargetKind.CREATURE)) {
			EntityHitResult creature = ProjectileUtil.getEntityHitResult(level, viewer, eye, reachEnd,
					viewer.getBoundingBox().expandTowards(reachEnd.subtract(eye)).inflate(1.0D),
					entity -> isAimableCreature(viewer, entity));
			if (creature != null && creature.getEntity() instanceof LivingEntity living) {
				Vec3 at = creature.getLocation();
				hits.add(new Hit<>(VerbTargetKind.CREATURE, eye.distanceTo(at), new VerbTarget.Creature(living, at)));
			}
		}
		if (wants.contains(VerbTargetKind.ITEMS)) {
			itemsAlong(level, eye, reachEnd).ifPresent(hits::add);
		}
		Optional<VerbTarget> chosen = nearestWanted(hits, wants, occluder);
		if (chosen.isPresent()) return chosen;
		return !blocked && wants.contains(VerbTargetKind.AIR) ? Optional.of(new VerbTarget.Air(end)) : Optional.empty();
	}

	/**
	 * The nearest hit of a wanted kind that is not behind the occluding block ({@code occluder} is its distance).
	 * On a tie the thing standing in front of a surface wins: creature, then items, then fluid, then block.
	 */
	public static <T> Optional<T> nearestWanted(List<Hit<T>> hits, Set<VerbTargetKind> wants, double occluder) {
		return hits.stream()
				.filter(hit -> wants.contains(hit.kind()) && hit.distance() <= occluder + EPSILON)
				.min(Comparator.<Hit<T>>comparingDouble(Hit::distance).thenComparingInt(hit -> tiePriority(hit.kind())))
				.map(Hit::value);
	}

	private static int tiePriority(VerbTargetKind kind) {
		return switch (kind) {
			case CREATURE -> 0;
			case ITEMS -> 1;
			case FLUID -> 2;
			case BLOCK -> 3;
			case AIR -> 4;
		};
	}

	/** One thing the ray met, at {@code distance} from the eye. */
	public record Hit<T>(VerbTargetKind kind, double distance, T value) {
	}

	static boolean isAimableCreature(Entity viewer, Entity entity) {
		return entity instanceof LivingEntity living && entity != viewer && living.isAlive() && !entity.isSpectator()
				&& entity.isPickable() && !viewer.isPassengerOfSameVehicle(entity);
	}

	private static Optional<Hit<VerbTarget>> itemsAlong(Level level, Vec3 eye, Vec3 reachEnd) {
		AABB swept = new AABB(eye, reachEnd).inflate(ITEM_AIM_MARGIN + 0.5D);
		ItemEntity aimed = null;
		double nearest = Double.MAX_VALUE;
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, swept, ItemEntity::isAlive)) {
			Optional<Vec3> crossing = item.getBoundingBox().inflate(ITEM_AIM_MARGIN).clip(eye, reachEnd);
			if (crossing.isEmpty()) continue;
			double distance = eye.distanceTo(crossing.get());
			if (distance < nearest) {
				nearest = distance;
				aimed = item;
			}
		}
		if (aimed == null) return Optional.empty();
		Vec3 focus = aimed.position();
		ItemEntity first = aimed;
		List<ItemEntity> heap = new ArrayList<>(level.getEntitiesOfClass(ItemEntity.class,
				new AABB(focus, focus).inflate(ITEM_HEAP_RADIUS),
				item -> item.isAlive() && item.position().distanceTo(focus) <= ITEM_HEAP_RADIUS));
		heap.remove(first);
		heap.sort(Comparator.comparingDouble(item -> item.position().distanceToSqr(focus)));
		heap.addFirst(first);
		if (heap.size() > ITEM_HEAP_LIMIT) heap = heap.subList(0, ITEM_HEAP_LIMIT);
		return Optional.of(new Hit<>(VerbTargetKind.ITEMS, nearest, new VerbTarget.Items(heap, focus)));
	}
}
