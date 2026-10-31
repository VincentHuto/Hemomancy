package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.structure.BloodTempleStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.regex.Pattern;

/**
 * The rebuilt Blood Temple assembles where natural generation would put it: its heart, Cardinal Focus and Hermit land
 * in one chunk (the anchor contract BloodTempleStructure.afterPlace relies on), the Hermit is linked to both, its
 * braziers burn, and it uses none of the Nether's blocks.
 */
@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class BloodTempleAssemblyGameTests {
	private static final Pattern NETHER_WORDS =
			Pattern.compile("nether|blackstone|basalt|shroomlight|netherrack|crimson_|warped_|soul_");

	private BloodTempleAssemblyGameTests() {}

	/** A temple's start never lands within four chunks of an outpost's (its structure set's exclusion zone). */
	@GameTest(templateNamespace = "registration_validation", template = "empty")
	public static void templesKeepClearOfOutposts(GameTestHelper helper) {
		var level = helper.getLevel();
		var sets = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
		var temples = sets.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE_SET,
				Hemomancy.rloc("blood_temple")));
		var outposts = sets.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE_SET,
				Hemomancy.rloc("harbinger_outpost")));
		var state = level.getChunkSource().getGeneratorState();
		int checked = 0;
		for (int x = -400; x <= 400; x++) {
			for (int z = -400; z <= 400; z++) {
				if (!temples.value().placement().isStructureChunk(state, x, z)) continue;
				checked++;
				helper.assertTrue(!state.hasStructureChunkInRange(outposts, x, z, 4),
						"A Blood Temple may start within four chunks of an outpost at chunk " + x + ", " + z);
			}
		}
		helper.assertTrue(checked > 50, "Too few temple starts to judge: " + checked);
		helper.succeed();
	}

	@GameTest(templateNamespace = "registration_validation", template = "empty", timeoutTicks = 600,
			batch = "blood_temple_assembly")
	public static void templeLinksItsHermitInOneChunk(GameTestHelper helper) {
		var level = helper.getLevel();
		var generator = level.getChunkSource().getGenerator();
		var structure = (BloodTempleStructure) level.registryAccess().registryOrThrow(Registries.STRUCTURE)
				.get(Hemomancy.rloc("blood_temple"));
		BlockPos surface = helper.absolutePos(new BlockPos(32, 1, 32));
		ChunkPos origin = new ChunkPos(surface);
		var context = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(),
				level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(), origin, level,
				biome -> true);
		BlockPos middle = new BlockPos(origin.getMiddleBlockX(), surface.getY(), origin.getMiddleBlockZ());
		var stub = structure.assembleOnSurface(context, middle);
		helper.assertTrue(stub.isPresent(), "The Blood Temple's anchor did not assemble");
		var pieces = stub.orElseThrow().getPiecesBuilder().build();
		BoundingBox bounds = pieces.calculateBoundingBox();
		for (int cx = bounds.minX() >> 4; cx <= bounds.maxX() >> 4; cx++) {
			for (int cz = bounds.minZ() >> 4; cz <= bounds.maxZ() >> 4; cz++) {
				ChunkPos chunk = new ChunkPos(cx, cz);
				BoundingBox chunkBox = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(),
						chunk.getMinBlockZ(), chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
				for (var piece : pieces.pieces()) {
					if (piece instanceof PoolElementStructurePiece poolPiece) {
						poolPiece.place(level, level.structureManager(), generator, level.getRandom(), chunkBox, middle, false);
					}
				}
				structure.afterPlace(level, level.structureManager(), generator, level.getRandom(), chunkBox, chunk,
						pieces);
			}
		}

		BlockPos display = null, focus = null;
		int braziers = 0, lit = 0;
		for (BlockPos pos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(),
				bounds.maxY(), bounds.maxZ())) {
			var state = level.getBlockState(pos);
			if (state.is(BlockInit.mortal_display.get())) display = pos.immutable();
			if (state.is(BlockInit.cardinal_focus.get())) focus = pos.immutable();
			if (state.is(BlockInit.iron_brazier.get())) {
				braziers++;
				if (BrazierBlock.isLit(state)) lit++;
			}
			helper.assertTrue(!state.is(Blocks.LIGHT), "The Hermit's marker was left in the temple at " + pos);
			String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
			helper.assertTrue(!NETHER_WORDS.matcher(path).find(), "The temple still uses " + path + " at " + pos);
		}
		helper.assertTrue(display != null && focus != null, "The temple lost its heart or its Cardinal Focus");
		helper.assertTrue(braziers == 2 && lit == 2, "The temple's braziers did not burn: " + lit + "/" + braziers);
		AABB box = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX() + 1, bounds.maxY() + 1,
				bounds.maxZ() + 1);
		var hermits = level.getEntitiesOfClass(Entity.class, box, e -> e.getType() == EntityInit.harbinger_hermit.get());
		helper.assertTrue(hermits.size() == 1, "Expected one Hermit, got " + hermits.size());
		Entity hermit = hermits.getFirst();
		helper.assertTrue(hermit instanceof Mob mob && mob.isPersistenceRequired(), "The Hermit can despawn");
		helper.assertTrue(new ChunkPos(display).equals(new ChunkPos(focus))
						&& new ChunkPos(hermit.blockPosition()).equals(new ChunkPos(focus)),
				"The heart, the Focus and the Hermit did not land in one chunk: " + display + " " + focus + " "
						+ hermit.blockPosition());
		helper.assertTrue(level.getBlockEntity(display) instanceof MortalDisplayBlockEntity heart
				&& hermit.getUUID().equals(heart.getLinkedHermit()), "The heart is not linked to the Hermit");
		helper.assertTrue(level.getBlockEntity(focus) instanceof CardinalFocusBlockEntity linked
				&& display.equals(linked.getTempleDisplay()) && hermit.getUUID().equals(linked.getTempleHermit()),
				"The Cardinal Focus is not linked to the heart and the Hermit");

		// The temple reaches past the test's own area; clear it so later batches find empty ground.
		level.getEntitiesOfClass(Entity.class, box.inflate(4), e -> !(e instanceof Player)).forEach(Entity::discard);
		int floor = helper.absolutePos(BlockPos.ZERO).getY();
		for (BlockPos pos : BlockPos.betweenClosed(bounds.minX() - 1, floor, bounds.minZ() - 1, bounds.maxX() + 1,
				bounds.maxY() + 1, bounds.maxZ() + 1)) {
			if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16);
		}
		helper.succeed();
	}
}
