package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hemomancy.common.world.GardensNetworkRules;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;


public class PuffballFungusBlock extends FlowerBlock {
	private static final ParticleColor ABSORBED = new ParticleColor(226, 206, 200);
	private static final ParticleColor BURST = new ParticleColor(150, 40, 58);

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	/** Stub of stem under the broad cap. */
	private static final VoxelShape CAP_SHAPE = ModelShapes.of(new double[] {5, 0, 5, 11, 1, 11},
			new double[] {2, 1, 2, 14, 10, 14});

	public PuffballFungusBlock(Holder<MobEffect> effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return CAP_SHAPE;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		BlockPos blockpos = pPos.below();
		if (pState.getBlock() == this) // Forge: This function is called during world gen and placement, before this
										// block is set, so if we are not 'here' then assume it's the pre-check.
			{
				boolean supportsPlant = pLevel.getBlockState(blockpos).canSustainPlant(pLevel, blockpos, Direction.UP,
						this.defaultBlockState()) == TriState.TRUE;
				return supportsPlant
					|| pLevel.getBlockState(blockpos).getBlock() == BlockInit.erythrocytic_mycelium.get();
			}
		return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
	}

	/** In the Gardens dimension a puffball answers a body: an Apotheos draws its spores in, anyone else is choked by them. */
	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (level.isClientSide || !(entity instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
				|| player.isCreative()) return;
		var outcome = GardensNetworkRules.puffball(ApotheosBodyEvents.isApotheos(player),
				FungalGardensPlaces.inDimension(level));
		if (outcome == GardensNetworkRules.PuffballOutcome.NONE) return;
		ServerLevel server = (ServerLevel) level;
		if (outcome == GardensNetworkRules.PuffballOutcome.ABSORB) {
			HemoCapabilityAccess.getSporeLoad(player).add(3f);
			server.sendParticles(HemoParticleData.sporiticSpore(ABSORBED), pos.getX() + 0.5, pos.getY() + 0.3,
					pos.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.02);
		} else {
			player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
			server.sendParticles(HemoParticleData.sporiticSpore(BURST), pos.getX() + 0.5, pos.getY() + 0.4,
					pos.getZ() + 0.5, 40, 0.6, 0.5, 0.6, 0.04);
		}
		level.removeBlock(pos, false);
	}
}
