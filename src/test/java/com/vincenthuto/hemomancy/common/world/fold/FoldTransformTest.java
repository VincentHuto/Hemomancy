package com.vincenthuto.hemomancy.common.world.fold;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FoldTransformTest {
	private FoldTransformTest() {
	}

	public static void main(String[] args) throws IOException {
		compressesOnlyForwardMovementForEveryHorizontalFacing();
		roundTripsWorldAndFoldCoordinates();
		classifiesViewerSideAroundTheSingleAperturePlane();
		usesSinglePlaneVisualDepthForBothViewerSides();
		detectsEntryWhenMovementCrossesTheSinglePlane();
		detectsEntryWhenGroundStepStartsOnTheSinglePlane();
		detectsMeaningfulDiagonalEntryFromBothFaces();
		ignoresGlancingSideMovementAcrossThePlane();
		ignoresTinyDepthCrossingsAndPlaneSliding();
		rejectsCrossingsWithoutPlayerCenterClearance();
		compressesOnlyPostCrossingMovementWhenEntering();
		detectsActiveExitCrossingsFromBothEnds();
		compressesOnlyPreExitMovementWhenLeaving();
		traversesOnTheEnteredSideOfTheSinglePlane();
		managerTickDoesNotPassivelyActivateInactivePlayers();
		managerMovementCooldownSuppressesImmediateReentry();
		usesApertureStencilOnlyForOutsideViews();
		clipsFrontInteriorSegmentsThatCrossTheCamera();
		doesNotClipBackInteriorSegmentsInFrontOfTheBackView();
		placesOppositeExitCamerasBeyondTheRealFold();
		clampsToThePrototypeHallwayBounds();
		reportsFrontAndBackExitThresholds();
		generatesSyntheticCorridorCollisionForBothEntrySides();
		collisionOverrideLifecycleStaysActiveUntilMoveTail();
		managerStateIsPartitionedByLogicalSide();
	}

	private static void compressesOnlyForwardMovementForEveryHorizontalFacing() {
		assertCompressed(FoldFacing.SOUTH, v(0.25D, 0.5D, 1.0D), v(0.25D, 0.5D, 0.0625D));
		assertCompressed(FoldFacing.NORTH, v(0.25D, 0.5D, -1.0D), v(0.25D, 0.5D, -0.0625D));
		assertCompressed(FoldFacing.EAST, v(1.0D, 0.5D, 0.25D), v(0.0625D, 0.5D, 0.25D));
		assertCompressed(FoldFacing.WEST, v(-1.0D, 0.5D, 0.25D), v(-0.0625D, 0.5D, 0.25D));
	}

	private static void roundTripsWorldAndFoldCoordinates() {
		FoldTransform transform = FoldTransform.of(prototype(FoldFacing.EAST));
		FoldVector world = v(11.5D, 65.25D, 11.25D);
		FoldTransform.FoldCoordinates fold = transform.toFold(world);

		assertEquals(0.75D, fold.lateral(), "lateral coordinate");
		assertEquals(1.25D, fold.vertical(), "vertical coordinate");
		assertEquals(1.0D, fold.realDepth(), "real depth coordinate");
		assertEquals(16.0D, fold.virtualDepth(), "virtual depth coordinate");
		assertVec(world, transform.toWorld(fold.lateral(), fold.vertical(), fold.realDepth()), "round trip");
	}

	private static void classifiesViewerSideAroundTheSingleAperturePlane() {
		FoldTransform transform = FoldTransform.of(prototype(FoldFacing.SOUTH));

		assertEquals(FoldViewerSide.FRONT, transform.viewerSide(transform.toWorld(0.0D, 1.0D, -0.25D)),
				"front side viewer");
		assertEquals(FoldViewerSide.BACK, transform.viewerSide(transform.toWorld(0.0D, 1.0D, 0.25D)),
				"back side viewer");
	}

	private static void usesSinglePlaneVisualDepthForBothViewerSides() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldTransform transform = FoldTransform.of(prototype(facing));
			assertEquals(1, FoldViewerSide.FRONT.visualDepthSign(), "front visual depth sign");
			assertEquals(-1, FoldViewerSide.BACK.visualDepthSign(), "back visual depth sign");
			assertVec(transform.toWorld(0.0D, 1.0D, 4.0D),
					transform.toVisualWorld(FoldViewerSide.FRONT, 0.0D, 1.0D, 4.0D),
					"front visual depth for " + facing);
			assertVec(transform.toWorld(0.0D, 1.0D, -4.0D),
					transform.toVisualWorld(FoldViewerSide.BACK, 0.0D, 1.0D, 4.0D),
					"back visual depth for " + facing);
			assertVec(transform.toWorld(0.0D, 1.0D, 0.0D),
					transform.toVisualWorld(FoldViewerSide.BACK, 0.0D, 1.0D, 0.0D),
					"back face stays on the single aperture plane for " + facing);
		}
	}

	private static void detectsEntryWhenMovementCrossesTheSinglePlane() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);

			assertEquals(FoldViewerSide.FRONT, FoldActivationBounds.entrySideForCrossing(spec,
					transform.toWorld(0.0D, 1.0D, -0.08D),
					transform.toWorld(0.0D, 1.0D, 0.08D)).orElseThrow(),
					"front crossing entry side for " + facing);
			assertEquals(FoldViewerSide.BACK, FoldActivationBounds.entrySideForCrossing(spec,
					transform.toWorld(0.0D, 1.0D, 0.08D),
					transform.toWorld(0.0D, 1.0D, -0.08D)).orElseThrow(),
					"back crossing entry side for " + facing);
			assertEquals(false, FoldActivationBounds.entrySideForCrossing(spec,
					transform.toWorld(1.5D, 1.0D, -0.08D),
					transform.toWorld(1.5D, 1.0D, 0.08D)).isPresent(),
					"crossing outside aperture for " + facing);
			assertEquals(false, FoldActivationBounds.entrySideForCrossing(spec,
					transform.toWorld(0.0D, 1.0D, -0.12D),
					transform.toWorld(0.0D, 1.0D, -0.06D)).isPresent(),
					"same-side movement for " + facing);
		}
	}

	private static void detectsEntryWhenGroundStepStartsOnTheSinglePlane() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);

			assertEquals(FoldViewerSide.FRONT, FoldActivationBounds.entrySideForCrossing(spec,
					transform.toWorld(0.0D, 0.0D, 0.0D),
					transform.toWorld(0.0D, 0.0D, 0.08D)).orElseThrow(),
					"front entry from exact plane for " + facing);
			assertEquals(FoldViewerSide.BACK, FoldActivationBounds.entrySideForCrossing(spec,
					transform.toWorld(0.0D, 0.0D, 0.0D),
					transform.toWorld(0.0D, 0.0D, -0.08D)).orElseThrow(),
					"back entry from exact plane for " + facing);
		}
	}

	private static void detectsMeaningfulDiagonalEntryFromBothFaces() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);

			assertEquals(FoldViewerSide.FRONT, FoldActivationBounds.entrySideForCrossing(spec,
					transform.toWorld(0.0D, 1.0D, -0.08D),
					transform.toWorld(0.2D, 1.0D, 0.01D)).orElseThrow(),
					"front diagonal entry for " + facing);
			assertEquals(FoldViewerSide.BACK, FoldActivationBounds.entrySideForCrossing(spec,
					transform.toWorld(0.0D, 1.0D, 0.08D),
					transform.toWorld(-0.2D, 1.0D, -0.01D)).orElseThrow(),
					"back diagonal entry for " + facing);
		}
	}

	private static void ignoresGlancingSideMovementAcrossThePlane() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);

			assertEquals(false, FoldActivationBounds.entryCrossing(spec,
					transform.toWorld(-0.12D, 1.0D, -0.01D),
					transform.toWorld(0.12D, 1.0D, 0.01D)).isPresent(),
					"side scrape does not enter for " + facing);
			assertEquals(false, FoldActivationBounds.entryCrossing(spec,
					transform.toWorld(-0.35D, 1.0D, -0.02D),
					transform.toWorld(0.35D, 1.0D, 0.02D)).isPresent(),
					"near-parallel side scrape does not enter for " + facing);
		}
	}

	private static void ignoresTinyDepthCrossingsAndPlaneSliding() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);

			assertEquals(false, FoldActivationBounds.entryCrossing(spec,
					transform.toWorld(0.0D, 1.0D, -0.004D),
					transform.toWorld(0.0D, 1.0D, 0.004D)).isPresent(),
					"tiny depth crossing does not enter for " + facing);
			assertEquals(false, FoldActivationBounds.entryCrossing(spec,
					transform.toWorld(-0.5D, 1.0D, 0.0D),
					transform.toWorld(0.5D, 1.0D, 0.0D)).isPresent(),
					"sliding along the plane does not enter for " + facing);
		}
	}

	private static void rejectsCrossingsWithoutPlayerCenterClearance() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);

			assertEquals(false, FoldActivationBounds.entryCrossing(spec,
					transform.toWorld(0.85D, 1.0D, -0.08D),
					transform.toWorld(0.85D, 1.0D, 0.08D)).isPresent(),
					"edge crossing leaves player-center clearance for " + facing);
		}
	}

	private static void compressesOnlyPostCrossingMovementWhenEntering() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);
			FoldVector frontStart = transform.toWorld(0.0D, 1.0D, -0.08D);
			FoldVector frontEnd = transform.toWorld(0.0D, 1.0D, 0.08D);
			FoldActivationBounds.EntryCrossing frontCrossing = FoldActivationBounds.entryCrossing(spec,
					frontStart, frontEnd).orElseThrow();
			FoldVector frontMove = delta(frontStart, frontEnd);
			FoldVector frontCompressed = transform.compressMovementAfterEntry(frontMove, frontCrossing.fraction());
			assertEquals(0.5D, frontCrossing.fraction(), "front crossing fraction for " + facing);
			assertEquals(0.005D, transform.toFold(frontStart.add(frontCompressed)).realDepth(),
					"front entry moves just inside for " + facing);

			FoldVector backStart = transform.toWorld(0.0D, 1.0D, 0.08D);
			FoldVector backEnd = transform.toWorld(0.0D, 1.0D, -0.08D);
			FoldActivationBounds.EntryCrossing backCrossing = FoldActivationBounds.entryCrossing(spec,
					backStart, backEnd).orElseThrow();
			FoldVector backMove = delta(backStart, backEnd);
			FoldVector backCompressed = transform.compressMovementAfterEntry(backMove, backCrossing.fraction());
			assertEquals(0.5D, backCrossing.fraction(), "back crossing fraction for " + facing);
			assertEquals(-0.005D, transform.toFold(backStart.add(backCompressed)).realDepth(),
					"back entry moves just inside for " + facing);
		}
	}

	private static void detectsActiveExitCrossingsFromBothEnds() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);

			assertExitFraction(spec, transform, FoldViewerSide.FRONT, 1.99D, 1.0D,
					"front entered back exit for " + facing);
			assertExitFraction(spec, transform, FoldViewerSide.FRONT, 0.01D, -1.0D,
					"front entered same-face exit for " + facing);
			assertExitFraction(spec, transform, FoldViewerSide.BACK, 1.99D, -1.0D,
					"back entered back exit for " + facing);
			assertExitFraction(spec, transform, FoldViewerSide.BACK, 0.01D, 1.0D,
					"back entered same-face exit for " + facing);
		}
	}

	private static void compressesOnlyPreExitMovementWhenLeaving() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);
			FoldVector requestedMovement = depthMovement(transform, 1.0D);
			FoldVector start = FoldActivationBounds.toWorldAtProgress(spec, FoldViewerSide.FRONT,
					0.0D, 1.0D, 1.99D);
			FoldVector compressed = transform.compressMovement(requestedMovement);
			FoldActivationBounds.ExitCrossing crossing = FoldActivationBounds.exitCrossing(spec,
					FoldViewerSide.FRONT, start, compressed).orElseThrow();
			FoldVector exitMovement = transform.compressMovementUntilExit(requestedMovement, crossing.fraction());
			double exitProgress = FoldActivationBounds.realDepthProgress(spec, FoldViewerSide.FRONT,
					start.add(exitMovement));

			assertEquals(0.16D, crossing.fraction(), "exit fraction for " + facing);
			assertEquals(2.84D, exitProgress, "post-exit movement resumes normal speed for " + facing);
		}
	}

	private static void traversesOnTheEnteredSideOfTheSinglePlane() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			FoldTransform transform = FoldTransform.of(spec);

			assertEquals(true, FoldActivationBounds.containsTraversalPoint(spec, FoldViewerSide.FRONT,
					transform.toWorld(0.0D, 1.0D, 1.0D)), "front-side traversal for " + facing);
			assertEquals(false, FoldActivationBounds.containsTraversalPoint(spec, FoldViewerSide.FRONT,
					transform.toWorld(0.0D, 1.0D, -1.0D)), "front side rejects reverse volume for " + facing);
			assertEquals(true, FoldActivationBounds.containsTraversalPoint(spec, FoldViewerSide.BACK,
					transform.toWorld(0.0D, 1.0D, -1.0D)), "back-side mirrored traversal for " + facing);
			assertEquals(false, FoldActivationBounds.containsTraversalPoint(spec, FoldViewerSide.BACK,
					transform.toWorld(0.0D, 1.0D, 1.0D)), "back side rejects hidden positive volume for " + facing);
			assertEquals(1.0D, FoldActivationBounds.realDepthProgress(spec, FoldViewerSide.BACK,
					transform.toWorld(0.0D, 1.0D, -1.0D)), "back-side mirrored progress for " + facing);
		}
	}

	private static void managerTickDoesNotPassivelyActivateInactivePlayers() throws IOException {
		String manager = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/world/fold/FoldedHallwayManager.java"));
		assertNotContains("inactive tick should not scan for containing folds",
				manager, "findContainingFold(player)");
		assertNotContains("inactive tick should not activate from a paper-thin entry point",
				manager, "containsEntryPoint");
	}

	private static void managerMovementCooldownSuppressesImmediateReentry() throws IOException {
		String manager = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/world/fold/FoldedHallwayManager.java"));
		assertBefore("movement cooldown should run before entry compression",
				manager, "state.cooldownTicks() > 0", "return compressEntryMovement(player, movement);");
	}

	private static void usesApertureStencilOnlyForOutsideViews() {
		assertEquals(true, FoldRenderWindow.needsApertureStencil(false), "outside portal view needs stencil");
		assertEquals(false, FoldRenderWindow.needsApertureStencil(true), "inside first-person view bypasses stencil");
	}

	private static void clipsFrontInteriorSegmentsThatCrossTheCamera() {
		FoldRenderWindow.DepthSpan clipped = FoldRenderWindow.clipForInsideCamera(
				FoldViewerSide.FRONT, 1.0D, 0.5D, 1.5D);
		assertEquals(1.08D, clipped.start(), "front clipped start");
		assertEquals(1.5D, clipped.end(), "front clipped end");

		FoldRenderWindow.DepthSpan hidden = FoldRenderWindow.clipForInsideCamera(
				FoldViewerSide.FRONT, 1.0D, -0.5D, 1.02D);
		assertEquals(false, hidden.visible(), "front fully clipped segment");
	}

	private static void doesNotClipBackInteriorSegmentsInFrontOfTheBackView() {
		FoldRenderWindow.DepthSpan unclipped = FoldRenderWindow.clipForInsideCamera(
				FoldViewerSide.BACK, 1.0D, 0.0D, 1.0D);
		assertEquals(0.0D, unclipped.start(), "back unclipped start");
		assertEquals(1.0D, unclipped.end(), "back unclipped end");
	}

	private static void placesOppositeExitCamerasBeyondTheRealFold() {
		FoldTransform transform = FoldTransform.of(prototype(FoldFacing.SOUTH));

		assertVec(v(10.75D, 65.0D, 12.75D),
				transform.oppositeExitCamera(FoldViewerSide.FRONT, 0.25D, 1.0D, 0.25D),
				"front viewer sees through the back exit");
		assertVec(v(10.75D, 65.0D, 10.25D),
				transform.oppositeExitCamera(FoldViewerSide.BACK, 0.25D, 1.0D, 0.25D),
				"back viewer sees through the front exit");
	}

	private static void clampsToThePrototypeHallwayBounds() {
		FoldTransform transform = FoldTransform.of(prototype(FoldFacing.SOUTH));
		FoldVector clamped = transform.clampWorldPosition(v(13.0D, 68.0D, 15.0D));
		FoldTransform.FoldCoordinates fold = transform.toFold(clamped);

		assertEquals(1.0D, fold.lateral(), "clamped lateral");
		assertEquals(3.0D, fold.vertical(), "clamped vertical");
		assertEquals(2.0D, fold.realDepth(), "clamped real depth");
		assertEquals(32.0D, fold.virtualDepth(), "clamped virtual depth");
	}

	private static void reportsFrontAndBackExitThresholds() {
		FoldTransform transform = FoldTransform.of(prototype(FoldFacing.WEST));

		assertEquals(FoldTransform.ExitSide.FRONT, transform.exitSide(transform.toWorld(0.0D, 1.0D, -0.01D)),
				"front exit");
		assertEquals(FoldTransform.ExitSide.NONE, transform.exitSide(transform.toWorld(0.0D, 1.0D, 1.0D)),
				"inside fold");
		assertEquals(FoldTransform.ExitSide.BACK, transform.exitSide(transform.toWorld(0.0D, 1.0D, 2.01D)),
				"back exit");
	}

	private static void generatesSyntheticCorridorCollisionForBothEntrySides() {
		for (FoldFacing facing : FoldFacing.values()) {
			FoldedHallwaySpec spec = prototype(facing);
			assertCorridorCollisionSet(spec, FoldViewerSide.FRONT, "front entry " + facing);
			assertCorridorCollisionSet(spec, FoldViewerSide.BACK, "back entry " + facing);
		}
	}

	private static void collisionOverrideLifecycleStaysActiveUntilMoveTail() throws IOException {
		String manager = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/world/fold/FoldedHallwayManager.java"));
		String mixin = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinEntity.java"));

		assertContains("manager tracks pending exit after collision",
				manager, "PENDING_EXIT_AFTER_MOVE.add(key(player))");
		assertContains("manager exposes finish move hook",
				manager, "public static void finishMove(Player player)");
		assertContains("finish move transitions pending exit to cooldown",
				manager, "FoldTravelerState.inactive(EXIT_COOLDOWN_TICKS)");
		assertContains("entity mixin finishes folded movement at move tail",
				mixin, "FoldedHallwayManager.finishMove(player)");
		assertBefore("pending exit should be recorded before returning exit movement",
				manager, "PENDING_EXIT_AFTER_MOVE.add(key(player))",
				"return FoldMinecraftAdapter.toVec3(transform.compressMovementUntilExit");
	}

	private static void managerStateIsPartitionedByLogicalSide() throws IOException {
		String manager = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/world/fold/FoldedHallwayManager.java"));

		assertContains("integrated server must not share client/server traveler slots",
				manager, "record TravelerKey(UUID playerId, boolean clientSide)");
		assertContains("traveler key should include the logical side from the player's level",
				manager, "new TravelerKey(player.getUUID(), player.level().isClientSide)");
		assertNotContains("traveler map should not be keyed only by UUID",
				manager, "Map<UUID, FoldTravelerState>");
		assertNotContains("pending exit set should not be keyed only by UUID",
				manager, "Set<UUID> PENDING_EXIT_AFTER_MOVE");
	}

	private static FoldedHallwaySpec prototype(FoldFacing facing) {
		return FoldedHallwaySpec.prototype("minecraft:overworld", 10, 64, 10, facing);
	}

	private static void assertCompressed(FoldFacing facing, FoldVector movement, FoldVector expected) {
		FoldTransform transform = FoldTransform.of(prototype(facing));
		assertEquals(0.0625D, transform.compressionRatio(), "compression ratio for " + facing);
		assertVec(expected, transform.compressMovement(movement), "compressed movement for " + facing);
	}

	private static FoldVector v(double x, double y, double z) {
		return new FoldVector(x, y, z);
	}

	private static FoldVector delta(FoldVector start, FoldVector end) {
		return new FoldVector(end.x - start.x, end.y - start.y, end.z - start.z);
	}

	private static FoldVector depthMovement(FoldTransform transform, double signedDepthDelta) {
		return delta(transform.toWorld(0.0D, 0.0D, 0.0D),
				transform.toWorld(0.0D, 0.0D, signedDepthDelta));
	}

	private static void assertExitFraction(FoldedHallwaySpec spec, FoldTransform transform,
			FoldViewerSide entrySide, double startProgress, double signedDepthMovement, String label) {
		FoldVector start = FoldActivationBounds.toWorldAtProgress(spec, entrySide,
				0.0D, 1.0D, startProgress);
		FoldVector compressed = transform.compressMovement(depthMovement(transform, signedDepthMovement));
		FoldActivationBounds.ExitCrossing crossing = FoldActivationBounds.exitCrossing(spec, entrySide,
				start, compressed).orElseThrow();
		assertEquals(0.16D, crossing.fraction(), label);
	}

	private static void assertCorridorCollisionSet(FoldedHallwaySpec spec, FoldViewerSide entrySide, String label) {
		List<FoldedHallwayCollision.CollisionBox> boxes = FoldedHallwayCollision.boxesFor(spec, entrySide);
		assertEquals(4, boxes.size(), label + " shape count");

		boolean foundFloor = false;
		boolean foundCeiling = false;
		boolean foundLeftWall = false;
		boolean foundRightWall = false;
		for (FoldedHallwayCollision.CollisionBox collisionBox : boxes) {
			FoldBox box = foldBox(spec, entrySide, collisionBox);
			assertEquals(0.0D, box.minProgress, label + " min progress");
			assertEquals(spec.realDepth(), box.maxProgress, label + " max progress");

			foundFloor |= close(box.maxVertical, 0.0D)
					&& close(box.minLateral, -spec.apertureHalfWidth())
					&& close(box.maxLateral, spec.apertureHalfWidth());
			foundCeiling |= close(box.minVertical, spec.apertureHeight())
					&& close(box.minLateral, -spec.apertureHalfWidth())
					&& close(box.maxLateral, spec.apertureHalfWidth());
			foundLeftWall |= close(box.maxLateral, -spec.apertureHalfWidth())
					&& close(box.minVertical, 0.0D)
					&& close(box.maxVertical, spec.apertureHeight());
			foundRightWall |= close(box.minLateral, spec.apertureHalfWidth())
					&& close(box.minVertical, 0.0D)
					&& close(box.maxVertical, spec.apertureHeight());
		}

		assertEquals(true, foundFloor, label + " floor");
		assertEquals(true, foundCeiling, label + " ceiling");
		assertEquals(true, foundLeftWall, label + " left wall");
		assertEquals(true, foundRightWall, label + " right wall");
	}

	private static FoldBox foldBox(FoldedHallwaySpec spec, FoldViewerSide entrySide,
			FoldedHallwayCollision.CollisionBox box) {
		FoldTransform transform = FoldTransform.of(spec);
		double minLateral = Double.POSITIVE_INFINITY;
		double maxLateral = Double.NEGATIVE_INFINITY;
		double minVertical = Double.POSITIVE_INFINITY;
		double maxVertical = Double.NEGATIVE_INFINITY;
		double minProgress = Double.POSITIVE_INFINITY;
		double maxProgress = Double.NEGATIVE_INFINITY;
		for (double x : new double[] { box.minX(), box.maxX() }) {
			for (double y : new double[] { box.minY(), box.maxY() }) {
				for (double z : new double[] { box.minZ(), box.maxZ() }) {
					FoldVector point = v(x, y, z);
					FoldTransform.FoldCoordinates coordinates = transform.toFold(point);
					double progress = FoldActivationBounds.realDepthProgress(spec, entrySide, point);
					minLateral = Math.min(minLateral, coordinates.lateral());
					maxLateral = Math.max(maxLateral, coordinates.lateral());
					minVertical = Math.min(minVertical, coordinates.vertical());
					maxVertical = Math.max(maxVertical, coordinates.vertical());
					minProgress = Math.min(minProgress, progress);
					maxProgress = Math.max(maxProgress, progress);
				}
			}
		}
		return new FoldBox(minLateral, maxLateral, minVertical, maxVertical, minProgress, maxProgress);
	}

	private static boolean close(double actual, double expected) {
		return Math.abs(actual - expected) <= 0.0001D;
	}

	private static void assertVec(FoldVector expected, FoldVector actual, String label) {
		assertEquals(expected.x, actual.x, label + " x");
		assertEquals(expected.y, actual.y, label + " y");
		assertEquals(expected.z, actual.z, label + " z");
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertEquals(Object expected, Object actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found unexpected text " + unexpected);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0) {
			throw new AssertionError(label + ": missing " + first);
		}
		if (secondIndex < 0) {
			throw new AssertionError(label + ": missing " + second);
		}
		if (firstIndex > secondIndex) {
			throw new AssertionError(label + ": expected " + first + " before " + second);
		}
	}

	private record FoldBox(double minLateral, double maxLateral, double minVertical, double maxVertical,
			double minProgress, double maxProgress) {
	}
}
