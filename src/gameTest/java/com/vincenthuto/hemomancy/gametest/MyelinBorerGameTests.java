package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Proves the Borer's two moods against real blocks and real goal ticking, which a JVM test cannot do.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MyelinBorerGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private static BlockState fiberX() {
		return BlockInit.nerve_fiber.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
	}

	/** Lays a run of fiber along X at the given relative y/z, optionally leaving one position empty. */
	private static void layRun(GameTestHelper h, int y, int z, int fromX, int toX, int gapX) {
		for (int x = fromX; x <= toX; x++) {
			if (x == gapX) {
				continue;
			}
			h.setBlock(new BlockPos(x, y, z), fiberX());
		}
	}

	private static MyelinBorerEntity spawnBorer(GameTestHelper h, BlockPos relative) {
		return h.spawn(EntityInit.myelin_borer.get(), relative);
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 600)
	public static void calmBorerMendsAGap(GameTestHelper h) {
		BlockPos gap = new BlockPos(3, 2, 3);
		layRun(h, 2, 3, 1, 5, gap.getX());
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(3, 2, 4));
		borer.setAgitation(0);
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			h.assertBlockPresent(BlockInit.nerve_fiber.get(), gap);
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 600)
	public static void agitatedBorerSeversCable(GameTestHelper h) {
		layRun(h, 2, 3, 1, 5, Integer.MIN_VALUE);
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(3, 2, 4));
		borer.provoke();
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			for (int x = 1; x <= 5; x++) {
				BlockPos pos = new BlockPos(x, 2, 3);
				if (h.getBlockState(pos).isAir()) {
					return;
				}
			}
			throw new net.minecraft.gametest.framework.GameTestAssertException("no cable severed yet");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 600)
	public static void borersNeverEatSynapticNodes(GameTestHelper h) {
		BlockPos node = new BlockPos(3, 2, 3);
		h.setBlock(new BlockPos(2, 2, 3), fiberX());
		h.setBlock(node, BlockInit.synaptic_node.get());
		h.setBlock(new BlockPos(4, 2, 3), fiberX());
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(3, 2, 4));
		borer.provoke();
		// This one passes by something NOT happening, so run out the clock then assert.
		java.util.concurrent.atomic.AtomicInteger ticks = new java.util.concurrent.atomic.AtomicInteger();
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			if (ticks.incrementAndGet() < 400) {
				throw new net.minecraft.gametest.framework.GameTestAssertException("waiting for borer goal ticks");
			}
			h.assertBlockPresent(BlockInit.synaptic_node.get(), node);
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 600)
	public static void boringLeavesRepairableCuts(GameTestHelper h) {
		layRun(h, 2, 3, 1, 5, Integer.MIN_VALUE);
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(3, 2, 4));
		borer.provoke();
		java.util.concurrent.atomic.AtomicInteger ticks = new java.util.concurrent.atomic.AtomicInteger();
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			if (ticks.incrementAndGet() < 400) {
				throw new net.minecraft.gametest.framework.GameTestAssertException("waiting for borer goal ticks");
			}
			for (int x = 1; x <= 5; x++) {
				BlockPos pos = new BlockPos(x, 2, 3);
				if (h.getBlockState(pos).isAir()) {
					if (!h.getBlockState(pos.west()).is(BlockInit.nerve_fiber.get())
							|| !h.getBlockState(pos.east()).is(BlockInit.nerve_fiber.get())) {
						throw new net.minecraft.gametest.framework.GameTestAssertException("borer left an unrepairable cut");
					}
				}
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
	public static void naturalSpawnRequiresNearbyCable(GameTestHelper h) {
		BlockPos spawn = new BlockPos(3, 3, 3);
		boolean adrift = SpawnPlacements.checkSpawnRules(EntityInit.myelin_borer.get(), h.getLevel(),
				MobSpawnType.NATURAL, h.absolutePos(spawn), h.getLevel().random);
		h.setBlock(new BlockPos(4, 3, 3), fiberX());
		boolean nearCable = SpawnPlacements.checkSpawnRules(EntityInit.myelin_borer.get(), h.getLevel(),
				MobSpawnType.NATURAL, h.absolutePos(spawn), h.getLevel().random);
		h.setBlock(new BlockPos(3, 2, 3), fiberX());
		boolean perched = SpawnPlacements.checkSpawnRules(EntityInit.myelin_borer.get(), h.getLevel(),
				MobSpawnType.NATURAL, h.absolutePos(spawn), h.getLevel().random);
		if (adrift || nearCable || !perched) {
			throw new net.minecraft.gametest.framework.GameTestAssertException("spawn must be directly on a cable");
		}
		h.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180)
	public static void borerCrawlsAlongCableAndRecoversFromNudge(GameTestHelper h) {
		layRun(h, 2, 3, 1, 7, Integer.MIN_VALUE);
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(2, 3, 3));
		borer.setPos(borer.getX() + 0.65D, borer.getY() + 0.35D, borer.getZ());
		java.util.concurrent.atomic.AtomicInteger ticks = new java.util.concurrent.atomic.AtomicInteger();
		java.util.concurrent.atomic.AtomicBoolean moved = new java.util.concurrent.atomic.AtomicBoolean();
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			if (borer.getX() > h.absolutePos(new BlockPos(3, 3, 3)).getX()) {
				moved.set(true);
			}
			if (ticks.incrementAndGet() < 100) {
				if (borer.getY() < h.absolutePos(new BlockPos(0, 3, 0)).getY() - 0.1D
						|| Math.abs(borer.getZ() - (h.absolutePos(new BlockPos(0, 3, 3)).getZ() + 0.5D)) > 0.6D) {
					throw new net.minecraft.gametest.framework.GameTestAssertException("borer left the cable");
				}
				throw new net.minecraft.gametest.framework.GameTestAssertException("waiting for crawl");
			}
			if (!moved.get() || !borer.onGround() && !borer.isGripping()) {
				throw new net.minecraft.gametest.framework.GameTestAssertException("borer never resumed crawling");
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 100)
	public static void strandedBorerReturnsToLoadedFiber(GameTestHelper h) {
		h.setBlock(new BlockPos(3, 5, 3), fiberX());
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(3, 2, 3));
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			if (borer.getY() < h.absolutePos(new BlockPos(3, 6, 3)).getY() - 0.1D) {
				throw new net.minecraft.gametest.framework.GameTestAssertException("stranded borer has not returned to fiber");
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void borerBlinksAcrossCableGap(GameTestHelper h) {
		h.setBlock(new BlockPos(2, 2, 3), fiberX());
		h.setBlock(new BlockPos(5, 2, 3), fiberX());
		h.setBlock(new BlockPos(2, 3, 3), net.minecraft.world.level.block.Blocks.AIR);
		h.setBlock(new BlockPos(5, 3, 3), net.minecraft.world.level.block.Blocks.AIR);
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(2, 3, 3));
		h.getLevel().tickNonPassenger(borer);
		if (!borer.tryBlinkToward(h.absolutePos(new BlockPos(6, 2, 3)))) {
			BlockPos farCable = h.absolutePos(new BlockPos(5, 2, 3));
			throw new net.minecraft.gametest.framework.GameTestAssertException("no blink began across cable gap: "
					+ "pos=" + borer.position() + " grip=" + borer.isGripping()
					+ "near=" + h.getLevel().getBlockState(borer.blockPosition().below())
					+ "far=" + h.getLevel().getBlockState(farCable)
					+ "landing=" + h.getLevel().getBlockState(farCable.above()));
		}
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			if (borer.blockPosition().distSqr(h.absolutePos(new BlockPos(5, 3, 3))) > 2.0D) {
				throw new net.minecraft.gametest.framework.GameTestAssertException(
						"borer did not reach far cable: " + borer.blockPosition() + " ticks=" + borer.tickCount
								+ " alive=" + borer.isAlive() + " removed=" + borer.isRemoved()
								+ " phase=" + borer.isBlinking());
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 100)
	public static void patrollingBorerBlinksToAnotherRun(GameTestHelper h) {
		h.setBlock(new BlockPos(2, 2, 3), fiberX());
		h.setBlock(new BlockPos(5, 2, 3), fiberX());
		h.setBlock(new BlockPos(2, 3, 3), net.minecraft.world.level.block.Blocks.AIR);
		h.setBlock(new BlockPos(5, 3, 3), net.minecraft.world.level.block.Blocks.AIR);
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(2, 3, 3));
		java.util.concurrent.atomic.AtomicBoolean sawBlink = new java.util.concurrent.atomic.AtomicBoolean();
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			sawBlink.set(sawBlink.get() || borer.isBlinking());
			if (!sawBlink.get() || borer.blockPosition().distSqr(h.absolutePos(new BlockPos(5, 3, 3))) > 2.0D) {
				throw new net.minecraft.gametest.framework.GameTestAssertException("patrolling borer has not blinked to the next run");
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180)
	public static void workingBorerBlinksAcrossBrokenRun(GameTestHelper h) {
		h.setBlock(new BlockPos(2, 2, 3), BlockInit.synaptic_node.get());
		layRun(h, 2, 3, 5, 7, Integer.MIN_VALUE);
		MyelinBorerEntity borer = spawnBorer(h, new BlockPos(2, 3, 3));
		borer.provoke();
		java.util.concurrent.atomic.AtomicBoolean sawBlink = new java.util.concurrent.atomic.AtomicBoolean();
		h.succeedWhen(() -> {
			h.getLevel().tickNonPassenger(borer);
			sawBlink.set(sawBlink.get() || borer.isBlinking());
			if (!sawBlink.get() || borer.blockPosition().distSqr(h.absolutePos(new BlockPos(5, 3, 3))) > 2.0D) {
				throw new net.minecraft.gametest.framework.GameTestAssertException(
						"borer did not blink across run: " + borer.blockPosition() + " phase=" + borer.isBlinking()
								+ " ticks=" + borer.tickCount + " alive=" + borer.isAlive()
								+ " removed=" + borer.isRemoved());
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
	public static void strikingOneBorerAlertsNearbyBorers(GameTestHelper h) {
		MyelinBorerEntity struck = spawnBorer(h, new BlockPos(2, 2, 3));
		MyelinBorerEntity nearby = spawnBorer(h, new BlockPos(4, 2, 3));
		struck.hurt(h.getLevel().damageSources().generic(), 1.0F);
		if (!nearby.isAgitated()) {
			throw new net.minecraft.gametest.framework.GameTestAssertException("nearby borer stayed calm");
		}
		h.succeed();
	}
}
