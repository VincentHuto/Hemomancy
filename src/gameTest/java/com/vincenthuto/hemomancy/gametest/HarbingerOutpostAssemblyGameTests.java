package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HarbingerEscritoireBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.ResonantForgeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.WarpChairBlock;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.structure.HarbingerOutpostStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.Optional;

@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class HarbingerOutpostAssemblyGameTests {
	private HarbingerOutpostAssemblyGameTests() {}

	@GameTest(templateNamespace = "registration_validation", template = "empty")
	public static void fourSeedsKeepAllMandatoryPieces(GameTestHelper helper) {
		var level = helper.getLevel();
		var generator = level.getChunkSource().getGenerator();
		var structure = (HarbingerOutpostStructure) level.registryAccess().registryOrThrow(Registries.STRUCTURE)
				.get(Hemomancy.rloc("harbinger_outpost"));
		BlockPos anchor = helper.absolutePos(new BlockPos(48, 3, 48));
		for (long seed : new long[] {11L, 29L, 42L, 97L}) {
			var context = new Structure.GenerationContext(level.registryAccess(), generator,
					generator.getBiomeSource(), level.getChunkSource().randomState(), level.getStructureManager(),
					seed, new ChunkPos(anchor), level, biome -> true);
			var stub = structure.findGenerationPoint(context);
			helper.assertTrue(stub.isPresent() && stub.orElseThrow().getPiecesBuilder().build().pieces().size() == 11,
					"A flat site or mandatory wing was rejected for seed " + seed);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "registration_validation", template = "empty", timeoutTicks = 1200)
	public static void mandatoryJigsawWingsAssemble(GameTestHelper helper) {
		var level = helper.getLevel();
		var key = ResourceKey.create(Registries.TEMPLATE_POOL,
				Hemomancy.rloc("harbinger_outpost/start_pool"));
		var pool = level.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL).getHolderOrThrow(key);
		BlockPos anchor = helper.absolutePos(new BlockPos(48, 3, 48));
		var generator = level.getChunkSource().getGenerator();
		var context = new Structure.GenerationContext(level.registryAccess(), generator,
				generator.getBiomeSource(), level.getChunkSource().randomState(), level.getStructureManager(),
				level.getSeed(), new ChunkPos(anchor), level, biome -> true);
		var stub = JigsawPlacement.addPieces(context, pool,
				Optional.of(Hemomancy.rloc("harbinger_outpost/anchor")), 2, anchor, false,
				Optional.empty(), 80, PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING,
				LiquidSettings.APPLY_WATERLOGGING);
		helper.assertTrue(stub.isPresent(), "The outpost start jigsaw failed");
		var pieces = stub.orElseThrow().getPiecesBuilder().build();
		helper.assertTrue(pieces.pieces().size() == 11, "A mandatory wall, corner home, or hall was omitted");
		var structure = (HarbingerOutpostStructure) level.registryAccess().registryOrThrow(Registries.STRUCTURE)
				.get(Hemomancy.rloc("harbinger_outpost"));
		BoundingBox bounds = pieces.calculateBoundingBox();
		BoundingBox plazaBox = pieces.pieces().getFirst().getBoundingBox();
		BlockPos plaza = new BlockPos((plazaBox.minX() + plazaBox.maxX()) / 2,
				plazaBox.minY(), (plazaBox.minZ() + plazaBox.maxZ()) / 2);
		for (int cx = bounds.minX() >> 4; cx <= bounds.maxX() >> 4; cx++) {
			for (int cz = bounds.minZ() >> 4; cz <= bounds.maxZ() >> 4; cz++) {
				ChunkPos chunk = new ChunkPos(cx, cz);
				BoundingBox chunkBox = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(),
						chunk.getMinBlockZ(), chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1,
						chunk.getMaxBlockZ());
				for (var piece : pieces.pieces()) {
					if (piece instanceof PoolElementStructurePiece poolPiece) {
						poolPiece.place(level, level.structureManager(), generator, level.getRandom(),
								chunkBox, anchor, false);
					}
				}
				structure.afterPlace(level, level.structureManager(), generator, level.getRandom(),
						chunkBox, chunk, pieces);
			}
		}

		int vicar = 0, escritoires = 0, artificer = 0, alchemist = 0, alembic = 0, cabinet = 0;
		int mnemonist = 0, banners = 0, standingBanners = 0, pyres = 0, basins = 0, vialDisplays = 0;
		int beds = 0, bedX = 0, bedZ = 0, gateX = 0, gateZ = 0, tallRoof = 0;
		int crafting = 0, furnaces = 0, smokers = 0, maps = 0;
		BlockPos forgePos = null, seatPos = null, loomPos = null, alchemistPos = null, gateBannerPos = null;
		BlockPos furnacePos = null, smokerPos = null;
		var ladderPositions = new HashSet<BlockPos>();
		for (BlockPos pos : BlockPos.betweenClosed(anchor.offset(-42, 0, -42), anchor.offset(42, 20, 42))) {
			var state = level.getBlockState(pos);
			if (state.is(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB)) {
				helper.assertTrue(level.getBlockState(pos.below()).isSolid(),
						"A roof slab is floating above its supporting layer at " + pos);
			}
			if (state.is(Blocks.RED_BANNER) || state.is(Blocks.RED_WALL_BANNER)) {
				helper.assertTrue(state.canSurvive(level, pos), "An outpost banner lacks support at " + pos);
			}
			if (state.is(Blocks.LADDER)) ladderPositions.add(pos.immutable());
			if (state.is(BlockInit.warp_chair.get())) { vicar++; seatPos = pos.immutable(); }
			if (state.is(BlockInit.harbinger_escritoire.get())) escritoires++;
			if (state.is(BlockInit.resonant_forge.get())) { artificer++; forgePos = pos.immutable(); }
			if (state.is(BlockInit.vial_centrifuge.get())) { alchemist++; alchemistPos = pos.immutable(); }
			if (state.is(BlockInit.ghastly_alembic.get())) alembic++;
			if (state.is(BlockInit.phlebotomists_cabinet.get())) cabinet++;
			if (state.is(BlockInit.somatic_loom.get())) { mnemonist++; loomPos = pos.immutable(); }
			if (state.is(Blocks.RED_WALL_BANNER)) banners++;
			if (state.is(Blocks.RED_BANNER)) {
				standingBanners++; gateX += pos.getX(); gateZ += pos.getZ(); gateBannerPos = pos.immutable();
			}
			if (state.is(Blocks.RED_BED)) { beds++; bedX += pos.getX(); bedZ += pos.getZ(); }
			if (state.is(Blocks.CRAFTING_TABLE)) crafting++;
			if (state.is(Blocks.FURNACE)) { furnaces++; furnacePos = pos.immutable(); }
			if (state.is(Blocks.SMOKER)) { smokers++; smokerPos = pos.immutable(); }
			if (state.is(Blocks.CARTOGRAPHY_TABLE)) maps++;
			if (state.is(Blocks.BLACKSTONE) && pos.getY() == plaza.getY() + 19) tallRoof++;
			if (state.is(Blocks.BREWING_STAND)
					&& level.getBlockEntity(pos) instanceof BrewingStandBlockEntity stand
					&& stand.getItem(0).is(Items.POTION) && stand.getItem(1).is(Items.POTION)
					&& stand.getItem(2).is(Items.POTION)) vialDisplays++;
			if (state.is(Blocks.CAMPFIRE)) pyres++;
			if (state.is(BlockInit.blood_basin.get())) basins++;
		}
		helper.assertTrue(vicar == 1 && artificer == 1 && alchemist == 1 && alembic == 1
				&& cabinet == 1 && mnemonist == 1,
				"A profession station was missing: seat=" + vicar + " forge=" + artificer
						+ " centrifuge=" + alchemist + " alembic=" + alembic + " cabinet=" + cabinet
						+ " loom=" + mnemonist);
		helper.assertTrue(banners >= 8 && standingBanners >= 2 && pyres >= 1 && basins == 0,
				"The courtyard decoration or safe basin contract was lost");
		helper.assertTrue(vialDisplays == 2, "The Alchemist's vial displays did not load their bottles");
		helper.assertTrue(beds == 8 && crafting == 1 && furnaces == 1 && smokers == 1
				&& maps == 1 && tallRoof > 0,
				"The two-story barracks hall or its shared facilities were omitted");
		for (BlockPos station : new BlockPos[] {furnacePos, smokerPos}) {
			Direction front = level.getBlockState(station).getValue(AbstractFurnaceBlock.FACING);
			helper.assertTrue(front.getStepX() * (plaza.getX() - station.getX())
					+ front.getStepZ() * (plaza.getZ() - station.getZ()) > 0
					&& level.getBlockState(station.relative(front)).isAir(),
					"A barracks furnace or smoker faces the wall instead of the room at " + station);
		}
		int hallDx = bedX / beds - plaza.getX();
		int hallDz = bedZ / beds - plaza.getZ();
		int gateDx = gateX / standingBanners - plaza.getX();
		int gateDz = gateZ / standingBanners - plaza.getZ();
		helper.assertTrue(hallDx * gateDx + hallDz * gateDz < -700
				&& Math.abs(hallDx * gateDz - hallDz * gateDx) < 100,
				"The barracks hall did not generate across the courtyard from the gate");
		BlockPos gateBanner = gateBannerPos;
		BoundingBox gateBounds = pieces.pieces().stream().map(piece -> piece.getBoundingBox())
				.filter(box -> box.isInside(gateBanner)).findFirst().orElseThrow();
		int gateWallBanners = 0;
		int gateCenterX = (gateBounds.minX() + gateBounds.maxX()) / 2;
		int gateCenterZ = (gateBounds.minZ() + gateBounds.maxZ()) / 2;
		for (BlockPos pos : BlockPos.betweenClosed(
				new BlockPos(gateBounds.minX(), gateBounds.minY(), gateBounds.minZ()),
				new BlockPos(gateBounds.maxX(), gateBounds.maxY(), gateBounds.maxZ()))) {
			var state = level.getBlockState(pos);
			if (!state.is(Blocks.RED_WALL_BANNER)) continue;
			gateWallBanners++;
			Direction facing = state.getValue(WallBannerBlock.FACING);
			BlockPos inPassage = pos.relative(facing);
			helper.assertTrue(facing.getStepX() * gateDx + facing.getStepZ() * gateDz == 0
					&& Math.abs(inPassage.getX() - gateCenterX) + Math.abs(inPassage.getZ() - gateCenterZ)
						< Math.abs(pos.getX() - gateCenterX) + Math.abs(pos.getZ() - gateCenterZ)
					&& level.getBlockState(inPassage).isAir(),
					"A gate banner does not face into the passage at " + pos);
		}
		helper.assertTrue(gateWallBanners == 4, "The gate passage lost its paired wall banners");
		for (Direction side : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
			BlockPos alongWall = plaza.relative(side.getClockWise(), 5);
			for (int radius : new int[] {24, 28}) {
				BlockPos wall = alongWall.relative(side, radius);
				helper.assertTrue(level.getBlockState(wall.above(3)).isSolid(),
						"A perimeter wall layer is missing toward " + side + " at radius " + radius);
			}
			for (int radius = 25; radius <= 27; radius++) {
				BlockPos walk = alongWall.relative(side, radius);
				helper.assertTrue(!level.getBlockState(walk.above(2)).isAir()
						&& level.getBlockState(walk.above(3)).isAir(),
						"The patrol walkway is blocked toward " + side + " at " + walk
								+ " floor=" + level.getBlockState(walk.above(2))
								+ " passage=" + level.getBlockState(walk.above(3))
								+ " plaza=" + plaza + " root=" + plazaBox);
			}
		}
		var occupiedCorners = new HashSet<String>();
		for (BlockPos station : new BlockPos[] {seatPos, forgePos, alchemistPos, loomPos}) {
			helper.assertTrue(Math.abs(station.getX() - plaza.getX()) >= 17
					&& Math.abs(station.getZ() - plaza.getZ()) >= 17,
					"A profession home was placed along a wall instead of at a corner");
			occupiedCorners.add((station.getX() < plaza.getX() ? "W" : "E")
					+ (station.getZ() < plaza.getZ() ? "N" : "S"));
			BoundingBox home = pieces.pieces().stream().map(piece -> piece.getBoundingBox())
					.filter(box -> box.isInside(station)).findFirst().orElseThrow();
			int cornerX = station.getX() < plaza.getX() ? home.maxX() : home.minX();
			int cornerZ = station.getZ() < plaza.getZ() ? home.maxZ() : home.minZ();
			int intoRoomX = cornerX == home.maxX() ? -1 : 1;
			int intoRoomZ = cornerZ == home.maxZ() ? -1 : 1;
			BlockPos entry = new BlockPos(cornerX, station.getY(), cornerZ);
			for (int height = 0; height < 3; height++) {
				BlockPos passage = entry.above(height);
				helper.assertTrue(level.getBlockState(passage).isAir()
						&& level.getBlockState(passage.offset(intoRoomX, 0, 0)).isAir()
						&& level.getBlockState(passage.offset(0, 0, intoRoomZ)).isAir()
						&& level.getBlockState(passage.offset(intoRoomX, 0, intoRoomZ)).isAir(),
						"The courtyard-facing corner entrance is blocked at " + passage);
			}
			BlockPos approach = entry.offset(-intoRoomX, 0, -intoRoomZ);
			helper.assertTrue(level.getBlockState(approach).isAir()
					&& level.getBlockState(approach.above()).isAir()
					&& level.getBlockState(approach.below()).isSolid(),
					"The inward corner entrance has no walkable courtyard approach at " + approach);
		}
		helper.assertTrue(occupiedCorners.size() == 4, "Two profession homes occupy the same corner");
		int ladderTops = 0;
		for (BlockPos ladder : ladderPositions) {
			Direction facing = level.getBlockState(ladder).getValue(LadderBlock.FACING);
			BlockPos backing = ladder.relative(facing.getOpposite());
			helper.assertTrue(level.getBlockState(backing).isSolid(),
					"A generated ladder has no backing at " + ladder);
			if (ladderPositions.contains(ladder.above())) continue;
			ladderTops++;
			helper.assertTrue(level.getBlockState(ladder.above()).isAir(),
					"The ladder top has only one block of headroom at " + ladder);
			boolean hasLanding = false;
			for (int drop = 0; drop <= 1; drop++) {
				BlockPos landing = ladder.below(drop).relative(facing);
				hasLanding |= level.getBlockState(landing).isAir()
						&& level.getBlockState(landing.above()).isAir()
						&& level.getBlockState(landing.below()).isSolid();
			}
			helper.assertTrue(hasLanding, "The ladder cannot be exited into a two-block-high landing at " + ladder);
		}
		helper.assertTrue(ladderPositions.size() == 22 && ladderTops == 3,
				"A watchtower or barracks ladder was omitted");
		helper.assertTrue(forgePos != null && ResonantForgeBlock.hasCompleteStructure(level,
				forgePos, level.getBlockState(forgePos)), "Generated forge has incomplete filler blocks");
		helper.assertTrue(seatPos != null && linkedFiller(level, seatPos.above(), seatPos, true),
				"Generated Somnolent Seat has no linked upper half");
		Direction seatFacing = level.getBlockState(seatPos).getValue(WarpChairBlock.FACING);
		var escritoire = level.getBlockState(seatPos.relative(seatFacing));
		helper.assertTrue(escritoires == 1 && escritoire.is(BlockInit.harbinger_escritoire.get())
				&& escritoire.getValue(HarbingerEscritoireBlock.FACING) == seatFacing.getOpposite(),
				"The Vicar's Escritoire must face the sitter directly in front of the Somnolent Seat");
		Direction loomFacing = level.getBlockState(loomPos).getValue(SomaticLoomBlock.FACING);
		helper.assertTrue(linkedFiller(level, loomPos.above(), loomPos, false)
				&& linkedFiller(level, loomPos.relative(loomFacing), loomPos, false)
				&& linkedFiller(level, loomPos.relative(loomFacing.getOpposite()), loomPos, false),
				"Generated Somatic Loom has incomplete filler blocks");
		AABB outpost = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(),
				bounds.maxX() + 1, bounds.maxY() + 1, bounds.maxZ() + 1);
		for (var type : new net.minecraft.world.entity.EntityType<?>[] {
				EntityInit.harbinger_vicar.get(), EntityInit.harbinger_artificer.get(),
				EntityInit.harbinger_alchemist.get(), EntityInit.harbinger_mnemonist.get()}) {
			var residents = level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, outpost,
					entity -> entity.getType() == type);
			helper.assertTrue(residents.size() == 1, "Expected one outpost resident of " + type + ", got " + residents.size());
			helper.assertTrue(residents.getFirst() instanceof net.minecraft.world.entity.Mob mob
					&& mob.isPersistenceRequired(), "Outpost resident can despawn: " + type);
			helper.assertTrue(HarbingerRecruitmentRules.findOutpostKey(residents.getFirst()) != null,
					"Outpost resident lost recruitment identity");
		}
		helper.succeed();
	}

	private static boolean linkedFiller(net.minecraft.world.level.Level level, BlockPos fillerPos,
			BlockPos controller, boolean seat) {
		return level.getBlockState(fillerPos).is(seat ? BlockInit.warp_chair_filler.get() : BlockInit.filler_block.get())
				&& level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler
				&& controller.equals(filler.getMainBlockPos());
	}
}
