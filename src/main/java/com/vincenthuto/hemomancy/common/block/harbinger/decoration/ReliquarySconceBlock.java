package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * The mausoleums' wall light: a hematic-iron sconce holding a lacquered cup of bone oil. {@link #FACING} is the way
 * it faces out of its wall; {@link #FLAME} is the kind of flame it burns. Model from
 * tools/model_export/reliquary_sconce.py.
 */
public class ReliquarySconceBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<Flame> FLAME = EnumProperty.create("flame", Flame.class);
	/** The flame's centre in block pixels for the north-facing model (reliquary_sconce.py FLAME). */
	private static final Vec3 FLAME_CENTRE = new Vec3(8, 10.55, 8);
	/** The smoulder's ember bed in block pixels (reliquary_sconce.py EMBER_BED). */
	private static final double EMBER_BED = 9.6;
	private static final Map<Direction, VoxelShape> SHAPES = ModelShapes.horizontal(
			ModelShapes.of(new double[] {5, 2, 6, 11, 16, 16}));
	private static final ParticleColor OIL = new ParticleColor(240, 206, 150);
	private static final ParticleColor CRIMSON = new ParticleColor(214, 34, 40);
	private static final ParticleColor CRIMSON_CORE = new ParticleColor(255, 112, 64);
	/** Flammeus embers and their soot, for Putriciel's smoulder. */
	private static final ParticleColor EMBER = new ParticleColor(226, 30, 46);
	private static final ParticleColor EMBER_HOT = new ParticleColor(255, 150, 172);
	private static final ParticleColor SOOT = new ParticleColor(20, 10, 12);

	/**
	 * The flame a sconce burns: the pale bone-oil standard, Hemorath's deep crimson, or Putriciel's smoulder (no tongue,
	 * a low bed of Flammeus embers under char).
	 */
	public enum Flame implements StringRepresentable {
		PALE("pale", 14),
		CRIMSON("crimson", 12),
		SMOULDER("smoulder", 7);

		private final String name;
		private final int light;

		Flame(String name, int light) {
			this.name = name;
			this.light = light;
		}

		public int light() {
			return light;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public ReliquarySconceBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FLAME, Flame.PALE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FLAME);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextFloat() > 0.3f) return;
		Vec3 flame = flame(state.getValue(FACING));
		double x = pos.getX() + flame.x, y = pos.getY() + flame.y, z = pos.getZ() + flame.z;
		if (state.getValue(FLAME) == Flame.SMOULDER) {
			double bed = pos.getY() + EMBER_BED / 16.0;
			level.addParticle(HemoParticleData.emberSpark(random.nextFloat() < 0.2f ? EMBER_HOT : EMBER, 0.06f, 0.85f,
					20 + random.nextInt(10)), x + (random.nextDouble() - 0.5) * 0.12, bed + 0.03,
					z + (random.nextDouble() - 0.5) * 0.12, (random.nextDouble() - 0.5) * 0.004,
					0.008 + random.nextDouble() * 0.008, (random.nextDouble() - 0.5) * 0.004);
			if (random.nextFloat() < 0.5f) {
				level.addParticle(HemoParticleData.darkGlow(SOOT), x, bed + 0.12, z, 0, 0.01, 0);
			}
			return;
		}
		if (state.getValue(FLAME) == Flame.PALE) {
			level.addParticle(HemoParticleData.glow(OIL), x, y, z, 0, 0.008, 0);
			return;
		}
		level.addParticle(HemoParticleData.glow(CRIMSON), x, y, z, 0, 0.008, 0);
		if (random.nextFloat() < 0.25f) {
			level.addParticle(HemoParticleData.emberSpark(CRIMSON_CORE, 0.08f, 0.9f, 24), x, y + 0.08, z,
					(random.nextDouble() - 0.5) * 0.006, 0.012 + random.nextDouble() * 0.01,
					(random.nextDouble() - 0.5) * 0.006);
		}
	}

	/** The flame's centre in the cell (0..1), turned like the blockstate turns the model. */
	static Vec3 flame(Direction facing) {
		double x = FLAME_CENTRE.x / 16.0, z = FLAME_CENTRE.z / 16.0;
		for (int turn = 0; turn < (facing.get2DDataValue() + 2) % 4; turn++) {
			double next = 1 - z;
			z = x;
			x = next;
		}
		return new Vec3(x, FLAME_CENTRE.y / 16.0, z);
	}
}
