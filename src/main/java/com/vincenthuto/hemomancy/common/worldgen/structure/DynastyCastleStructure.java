package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Custom jigsaw-style structure that assembles the dynasty castle from generated {@code .nbt}
 * pieces. Registration and CODEC shape follow {@code MausoleumStructure}; piece assembly follows
 * {@code VigilPlacement} (real {@link PoolElementStructurePiece}s at computed offsets/rotations,
 * with no jigsaw connector blocks in the templates). Variety comes from {@link DynastyCastleLayouts}.
 */
public class DynastyCastleStructure extends Structure {
	public static final MapCodec<DynastyCastleStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					settingsCodec(instance),
					HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
					Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
							.forGetter(s -> s.projectStartToHeightmap))
			.apply(instance, DynastyCastleStructure::new));

	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;

	public DynastyCastleStructure(StructureSettings settings, HeightProvider startHeight,
			Optional<Heightmap.Types> projectStartToHeightmap) {
		super(settings);
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		RandomSource random = context.random();
		ChunkPos chunkPos = context.chunkPos();
		int x = chunkPos.getMiddleBlockX();
		int z = chunkPos.getMiddleBlockZ();
		WorldGenerationContext worldGenContext =
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());
		// Sit the castle on the terrain surface. start_height acts as a vertical offset that is added
		// to the projected heightmap height (matching vanilla JigsawPlacement projection semantics).
		int offset = this.startHeight.sample(random, worldGenContext);
		int startY = this.projectStartToHeightmap
				.map(heightmap -> offset + context.chunkGenerator().getFirstFreeHeight(
						x, z, heightmap, context.heightAccessor(), context.randomState()))
				.orElse(offset);
		BlockPos origin = new BlockPos(x, startY, z);
		StructureTemplateManager templates = context.structureTemplateManager();
		DynastyCastleLayouts.Layout layout = DynastyCastleLayouts.pick(random);

		List<PoolElementStructurePiece> pieces = new ArrayList<>();
		// Courtyard clears the whole envelope of terrain; it must be placed first so the keep,
		// towers, and walls overwrite their own columns afterwards.
		pieces.add(piece(templates, "courtyard", origin, Rotation.NONE));
		pieces.add(piece(templates, layout.keep(), origin, Rotation.NONE));
		for (DynastyCastleLayouts.Slot slot : layout.slots()) {
			String choice = slot.candidates().get(random.nextInt(slot.candidates().size()));
			if ("empty".equals(choice)) continue;
			pieces.add(piece(templates, choice, origin.offset(slot.offset()), slot.rotation()));
		}

		return Optional.of(new GenerationStub(origin, builder -> pieces.forEach(builder::addPiece)));
	}

	/**
	 * Places a piece centered on {@code center}. Pieces are authored with their min corner at the
	 * template origin, so to center them we shift the placement position back by half the footprint
	 * (swapping X/Z for quarter-turn rotations). Y is left at {@code center.y} because pieces put
	 * their floor at local y = 0.
	 */
	private static PoolElementStructurePiece piece(StructureTemplateManager templates, String name,
			BlockPos center, Rotation rotation) {
		StructurePoolElement element = StructurePoolElement
				.single("hemomancy:dynasty_castle/" + name)
				.apply(StructureTemplatePool.Projection.RIGID);
		Vec3i size = element.getSize(templates, rotation);
		BlockPos position = center.offset(-size.getX() / 2, 0, -size.getZ() / 2);
		BoundingBox bounds = element.getBoundingBox(templates, position, rotation);
		return new PoolElementStructurePiece(templates, element, position, 0, rotation, bounds,
				LiquidSettings.APPLY_WATERLOGGING);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.dynasty_castle.get();
	}
}
