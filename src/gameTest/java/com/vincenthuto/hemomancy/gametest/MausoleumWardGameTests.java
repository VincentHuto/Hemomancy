package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.MausoleumEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The consecrated ward (Track 2): the threshold turns back anyone below the eighth degree, and a refused break strikes
 * the ward. The anchor is settled by hand here as {@code MausoleumStructure.afterPlace} settles a generated one.
 */
@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class MausoleumWardGameTests {
	/** The anchor in the wall at x=4, its slab across x=4, y 1..5, z 1..5; the ward runs on to x=9. */
	private static final BlockPos ANCHOR = new BlockPos(4, 3, 0);

	private MausoleumWardGameTests() {
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void aVisitorBelowTheEighthDegreeIsTurnedBackAtTheThreshold(GameTestHelper helper) {
		WardAnchorBlockEntity anchor = settledAnchor(helper);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(WardAnchors.loaded(helper.getLevel()).contains(anchor),
					"A ward anchor placed in a loaded chunk is indexed for its level");
			try (CapturedPlayer visitor = CapturedPlayer.create(helper)) {
				HemoCapabilityAccess.getInitiatoryDegree(visitor.player).setDegreeNumber(4);
				Vec3 past = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(7, 1, 3)));
				visitor.player.moveTo(past.x, past.y, past.z);
				helper.assertTrue(MausoleumEvents.holdAtThreshold(visitor.player), "A D4 past the line is turned back");
				Vec3 back = visitor.player.position();
				Vec3 expected = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 1, 3))).subtract(0.5, 0, 0);
				helper.assertTrue(back.distanceTo(expected) < 1.0E-6,
						"Set down a block before the line, found " + back + " expected " + expected);
				helper.assertTrue(!anchor.inWard(back) && !visitor.player.getBoundingBox().intersects(anchor.threshold()),
						"Where the visitor is set down is outside the ward and clear of the slab");
				var strikes = visitor.sent(WardStrikePacket.class);
				helper.assertTrue(strikes.size() == 1 && strikes.get(0).kind() == WardStrikePacket.Kind.THRESHOLD,
						"The turned-back visitor sees the threshold flare, got " + strikes);
				helper.assertTrue(!MausoleumEvents.holdAtThreshold(visitor.player), "Before the line, nothing happens");
			}
			clear(helper);
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void theEighthDegreeWalksThroughTheThreshold(GameTestHelper helper) {
		settledAnchor(helper);
		helper.runAfterDelay(2, () -> {
			try (CapturedPlayer apotheos = CapturedPlayer.create(helper)) {
				HemoCapabilityAccess.getInitiatoryDegree(apotheos.player).setDegreeNumber(8);
				Vec3 past = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 3)));
				apotheos.player.moveTo(past.x, past.y, past.z);
				helper.assertTrue(!MausoleumEvents.holdAtThreshold(apotheos.player), "A D8 on the line is not held");
				helper.assertTrue(apotheos.player.position().equals(past), "A D8 stays where they stand");
				helper.assertTrue(apotheos.sent(WardStrikePacket.class).isEmpty(), "Nothing strikes a D8");
			}
			clear(helper);
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 20)
	public static void aRefusedBreakStrikesTheWard(GameTestHelper helper) {
		BlockPos stone = new BlockPos(6, 2, 6);
		helper.setBlock(stone, Blocks.POLISHED_DEEPSLATE);
		ServerLevel level = helper.getLevel();
		BlockPos absolute = helper.absolutePos(stone);
		Runnable unconsecrate = consecrate(helper, new BoundingBox(absolute).inflatedBy(2));
		try (CapturedPlayer visitor = CapturedPlayer.create(helper)) {
			Vec3 eye = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(6, 1, 3)));
			visitor.player.moveTo(eye.x, eye.y, eye.z);
			var event = new BlockEvent.BreakEvent(level, absolute, level.getBlockState(absolute), visitor.player);
			MausoleumEvents.onBreak(event);
			helper.assertTrue(event.isCanceled(), "Consecrated stone does not break");
			var strikes = visitor.sent(WardStrikePacket.class);
			helper.assertTrue(strikes.size() == 1 && strikes.get(0).kind() == WardStrikePacket.Kind.BREAK
					&& strikes.get(0).pos().equals(absolute) && strikes.get(0).entityId() == -1,
					"The refused break strikes the ward on that block, got " + strikes);
		} finally {
			unconsecrate.run();
		}
		helper.assertTrue(!MausoleumEvents.hallowed(level, absolute), "The test ground is ordinary again");
		clear(helper);
		helper.succeed();
	}

	private static WardAnchorBlockEntity settledAnchor(GameTestHelper helper) {
		helper.setBlock(ANCHOR, BlockInit.ward_anchor.get().defaultBlockState());
		WardAnchorBlockEntity anchor = helper.getBlockEntity(ANCHOR);
		CompoundTag template = new CompoundTag();
		template.putString("Saint", "HEMORATH");
		template.putIntArray("ThresholdFrom", new int[] {0, -2, 1});
		template.putIntArray("ThresholdTo", new int[] {0, 2, 5});
		anchor.loadWithComponents(template, helper.getLevel().registryAccess());
		BoundingBox ward = BoundingBox.fromCorners(helper.absolutePos(new BlockPos(4, 0, 0)),
				helper.absolutePos(new BlockPos(9, 6, 6)));
		BoundingBox hallowed = BoundingBox.fromCorners(helper.absolutePos(new BlockPos(0, 0, 0)),
				helper.absolutePos(new BlockPos(9, 6, 6)));
		anchor.settle(Rotation.NONE, List.of(ward), List.of(hallowed));
		return anchor;
	}

	/**
	 * Registers a one-piece Hemorath mausoleum over {@code box}, so {@link MausoleumEvents#hallowed} holds there as in a
	 * generated complex; the returned action puts every touched chunk's starts and references back.
	 */
	private static Runnable consecrate(GameTestHelper helper, BoundingBox box) {
		ServerLevel level = helper.getLevel();
		Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
				.get(Hemomancy.rloc("mausoleum_hemorath"));
		var piece = new PoolElementStructurePiece(level.getStructureManager(), EmptyPoolElement.INSTANCE,
				new BlockPos(box.minX(), box.minY(), box.minZ()), 0, Rotation.NONE, box,
				LiquidSettings.IGNORE_WATERLOGGING);
		ChunkPos origin = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()),
				SectionPos.blockToSectionCoord(box.minZ()));
		List<Runnable> restore = new ArrayList<>();
		var originChunk = level.getChunk(origin.x, origin.z);
		Map<Structure, StructureStart> starts = new HashMap<>(originChunk.getAllStarts());
		restore.add(() -> originChunk.setAllStarts(starts));
		originChunk.setStartForStructure(structure, new StructureStart(structure, origin, 0,
				new PiecesContainer(List.of(piece))));
		ChunkPos.rangeClosed(origin, new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()),
				SectionPos.blockToSectionCoord(box.maxZ()))).forEach(chunk -> {
			var access = level.getChunk(chunk.x, chunk.z);
			var references = new HashMap<>(access.getAllReferences());
			restore.add(() -> access.setAllReferences(references));
			access.addReferenceForStructure(structure, origin.toLong());
		});
		return () -> restore.forEach(Runnable::run);
	}

	private static void clear(GameTestHelper helper) {
		for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)),
				helper.absolutePos(new BlockPos(10, 7, 10)))) {
			if (!helper.getLevel().getBlockState(pos).isAir()) {
				helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
		}
	}
}
