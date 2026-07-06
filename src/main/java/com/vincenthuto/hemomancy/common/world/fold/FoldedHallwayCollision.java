package com.vincenthuto.hemomancy.common.world.fold;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FoldedHallwayCollision {
	private static final double FLOOR_THICKNESS = 0.25D;
	private static final double CEILING_THICKNESS = 0.25D;
	private static final double WALL_THICKNESS = 0.25D;

	private FoldedHallwayCollision() {
	}

	public static boolean overridesCollisions(Entity entity) {
		return entity instanceof Player player && FoldedHallwayManager.getState(player).isPresent();
	}

	public static Iterable<VoxelShape> blockCollisions(Player player, AABB queryBox) {
		Optional<FoldTravelerState> state = FoldedHallwayManager.getState(player);
		if (state.isEmpty() || state.get().entrySide() == null) {
			return List.of();
		}
		return shapesFor(state.get().activeFold(), state.get().entrySide(), queryBox);
	}

	public static List<VoxelShape> shapesFor(FoldedHallwaySpec spec, FoldViewerSide entrySide, AABB queryBox) {
		CollisionBox query = CollisionBox.from(queryBox);
		List<VoxelShape> shapes = new ArrayList<>();
		for (CollisionBox box : boxesFor(spec, entrySide)) {
			if (box.intersects(query)) {
				shapes.add(Shapes.create(box.toAabb()));
			}
		}
		return shapes;
	}

	public static List<CollisionBox> boxesFor(FoldedHallwaySpec spec, FoldViewerSide entrySide) {
		double halfWidth = spec.apertureHalfWidth();
		double height = spec.apertureHeight();
		double depth = spec.realDepth();
		return List.of(
				boxFor(spec, entrySide, -halfWidth, -FLOOR_THICKNESS, 0.0D, halfWidth, 0.0D, depth),
				boxFor(spec, entrySide, -halfWidth, height, 0.0D, halfWidth, height + CEILING_THICKNESS, depth),
				boxFor(spec, entrySide, -halfWidth - WALL_THICKNESS, 0.0D, 0.0D, -halfWidth, height, depth),
				boxFor(spec, entrySide, halfWidth, 0.0D, 0.0D, halfWidth + WALL_THICKNESS, height, depth));
	}

	private static CollisionBox boxFor(FoldedHallwaySpec spec, FoldViewerSide entrySide, double minLateral,
			double minVertical, double minProgress, double maxLateral, double maxVertical, double maxProgress) {
		FoldVector first = FoldActivationBounds.toWorldAtProgress(spec, entrySide,
				minLateral, minVertical, minProgress);
		FoldVector second = FoldActivationBounds.toWorldAtProgress(spec, entrySide,
				maxLateral, maxVertical, maxProgress);
		return CollisionBox.fromCorners(first, second);
	}

	public record CollisionBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		private static CollisionBox from(AABB box) {
			return new CollisionBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		}

		private static CollisionBox fromCorners(FoldVector first, FoldVector second) {
			return new CollisionBox(
					Math.min(first.x, second.x),
					Math.min(first.y, second.y),
					Math.min(first.z, second.z),
					Math.max(first.x, second.x),
					Math.max(first.y, second.y),
					Math.max(first.z, second.z));
		}

		private boolean intersects(CollisionBox other) {
			return this.maxX >= other.minX && this.minX <= other.maxX
					&& this.maxY >= other.minY && this.minY <= other.maxY
					&& this.maxZ >= other.minZ && this.minZ <= other.maxZ;
		}

		private AABB toAabb() {
			return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
		}
	}
}
