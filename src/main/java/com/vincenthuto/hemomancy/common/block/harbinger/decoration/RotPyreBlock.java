package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.RotPyreBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Putriciel's pyre: bones stacked over a ring of stones. Where it is {@link #LIT} its ember bed glows and its block
 * entity's renderer burns a Flammeus fire on it (crimson, pink-white at the heart, black soot at the tips; never green);
 * unlit, only cold char is left. The Putriciel fight toggles LIT. Models from tools/model_export/rot_pyre.py.
 */
public class RotPyreBlock extends Block implements EntityBlock {
	public static final BooleanProperty LIT = BooleanProperty.create("lit");
	/** Where the drawn flame tops out, in block pixels (rot_pyre.py FLAME_TIP). */
	public static final double FLAME_TIP = 13;
	/** The ember bed's surface, where the fire stands, in block pixels (rot_pyre.py BED). */
	public static final double BED = 0.75;
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 11, 15);
	private static final ParticleColor EMBER = new ParticleColor(226, 30, 46);
	private static final ParticleColor EMBER_HOT = new ParticleColor(255, 150, 172);
	private static final ParticleColor SOOT = new ParticleColor(20, 10, 12);

	public RotPyreBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(LIT, true));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RotPyreBlockEntity(pos, state);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		double x = pos.getX() + 0.35 + random.nextDouble() * 0.3;
		double z = pos.getZ() + 0.35 + random.nextDouble() * 0.3;
		double y = pos.getY() + FLAME_TIP / 16.0;
		if (random.nextFloat() < 0.6f) {
			level.addParticle(HemoParticleData.emberSpark(random.nextFloat() < 0.25f ? EMBER_HOT : EMBER, 0.1f, 0.9f, 30),
					x, y - 0.2, z, (random.nextDouble() - 0.5) * 0.01, 0.02 + random.nextDouble() * 0.02,
					(random.nextDouble() - 0.5) * 0.01);
		}
		if (random.nextFloat() < 0.35f) {
			level.addParticle(HemoParticleData.darkGlow(SOOT), x, y + 0.1, z, 0, 0.015, 0);
		}
	}
}
