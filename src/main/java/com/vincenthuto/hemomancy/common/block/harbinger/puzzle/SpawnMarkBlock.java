package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Downfall wing's seal: a cracked plate in the floor that a wave of the fall rises from. {@link #WAVE} is the wave
 * it answers, cut into it as notches; while that wave is due the Chapel focus sets {@link #DUE} and blood seeps up
 * through the cracks. {@link #STYLE} is the venous seal or Putriciel's ash heap (the seal sunk in grey-black ash
 * with charred bone bits; due, its cracks seep crimson and it throws embers and soot). Walked over, never stood on.
 * Unbreakable. Models from tools/model_export/spawn_mark.py.
 */
public class SpawnMarkBlock extends Block {
	public static final IntegerProperty WAVE = IntegerProperty.create("wave", 0, 2);
	public static final BooleanProperty DUE = BooleanProperty.create("due");
	public static final EnumProperty<Style> STYLE = EnumProperty.create("style", Style.class);
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 2, 15);
	private static final ParticleColor SEEP = new ParticleColor(150, 16, 30);
	private static final ParticleColor CLOT = new ParticleColor(70, 6, 14);
	private static final ParticleColor EMBER = new ParticleColor(226, 30, 46);
	private static final ParticleColor SOOT = new ParticleColor(20, 10, 12);

	/** The seal's dressing: the venous plate, or sunk in Putriciel's ash. */
	public enum Style implements StringRepresentable {
		VENOUS, ASH;

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}

	public SpawnMarkBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(WAVE, 0).setValue(DUE, false)
				.setValue(STYLE, Style.VENOUS));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WAVE, DUE, STYLE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(DUE)) return;
		if (state.getValue(STYLE) == Style.ASH) {
			double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
			double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
			level.addParticle(HemoParticleData.emberSpark(EMBER, 0.07f, 0.85f, 22), x, pos.getY() + 0.1, z,
					(random.nextDouble() - 0.5) * 0.006, 0.012 + random.nextDouble() * 0.012,
					(random.nextDouble() - 0.5) * 0.006);
			if (random.nextFloat() < 0.5f) {
				level.addParticle(HemoParticleData.darkGlow(SOOT), x, pos.getY() + 0.2, z, 0, 0.012, 0);
			}
			return;
		}
		for (int i = 0; i < 2; i++) {
			double x = pos.getX() + 0.15 + random.nextDouble() * 0.7;
			double z = pos.getZ() + 0.15 + random.nextDouble() * 0.7;
			level.addParticle(HemoParticleData.glow(random.nextFloat() < 0.3f ? CLOT : SEEP), x, pos.getY() + 0.1, z,
					0, 0.01 + random.nextDouble() * 0.015, 0);
		}
	}
}
