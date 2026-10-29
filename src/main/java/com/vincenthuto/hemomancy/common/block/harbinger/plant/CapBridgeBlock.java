package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.GardensTerrainRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A cap grown out into a bridge under an Apotheos's hand. Each segment grows the next one {@code reach} more times,
 * then withers after a while unless an Apotheos is still standing near it. It has no item and drops nothing.
 */
public class CapBridgeBlock extends Block {
	/** Caps an Apotheos can grow a bridge from. A bridge is not one, so a bridge cannot be extended from its end. */
	public static final TagKey<Block> BRIDGING_CAPS = TagKey.create(Registries.BLOCK, Hemomancy.rloc("bridging_caps"));
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty REACH = IntegerProperty.create("reach", 0, GardensTerrainRules.BRIDGE_REACH);
	public static final BooleanProperty GROWN = BooleanProperty.create("grown");
	private static final VoxelShape SHAPE = Block.box(0, 8, 0, 16, 16, 16);
	private static final ParticleColor WITHERED = new ParticleColor(226, 206, 200);

	public CapBridgeBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(REACH, 0)
				.setValue(GROWN, false));
	}

	/** The first segment of a bridge growing toward {@code direction}, with the rest of the reach still to grow. */
	public BlockState seed(Direction direction) {
		return defaultBlockState().setValue(FACING, direction).setValue(REACH, GardensTerrainRules.BRIDGE_REACH - 1);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, REACH, GROWN);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (!level.isClientSide && !oldState.is(this) && !state.getValue(GROWN)) {
			level.scheduleTick(pos, this, GardensTerrainRules.BRIDGE_GROW_TICKS);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.getValue(GROWN)) {
			int reach = state.getValue(REACH);
			BlockPos next = pos.relative(state.getValue(FACING));
			if (reach > 0 && level.getBlockState(next).isAir()) {
				level.setBlock(next, state.setValue(REACH, reach - 1), Block.UPDATE_ALL);
			}
			level.setBlock(pos, state.setValue(GROWN, true), Block.UPDATE_CLIENTS);
			level.scheduleTick(pos, this, GardensTerrainRules.BRIDGE_LIFETIME_TICKS);
			return;
		}
		if (apotheosNear(level, pos)) {
			level.scheduleTick(pos, this, GardensTerrainRules.BRIDGE_LINGER_TICKS);
			return;
		}
		level.removeBlock(pos, false);
		level.sendParticles(HemoParticleData.sporiticSpore(WITHERED), pos.getX() + 0.5, pos.getY() + 0.75,
				pos.getZ() + 0.5, 6, 0.3, 0.1, 0.3, 0.01);
		level.playSound(null, pos, SoundEvents.FUNGUS_BREAK, SoundSource.BLOCKS, 0.6f, 0.9f);
	}

	private static boolean apotheosNear(ServerLevel level, BlockPos pos) {
		return !level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(GardensTerrainRules.BRIDGE_HOLD_RADIUS),
				player -> player.isAlive() && ApotheosBodyEvents.isApotheos(player)).isEmpty();
	}
}
