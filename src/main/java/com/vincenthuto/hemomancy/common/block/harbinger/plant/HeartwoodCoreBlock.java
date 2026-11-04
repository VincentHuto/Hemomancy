package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartwoodCoreBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import javax.annotation.Nullable;

/**
 * The one core in a Heartwood tree. It glows brighter as blood is projected into it or the tree bleeds
 * what touches it; {@link #FED} is its {@link HeartwoodCoreRules#stage stage}, and a core at the last stage drops
 * Ravenous Heartwood instead of a log. Its item places an unfed core with no tree of its own, which bites only what
 * touches the core itself and players near it. Mining one never returns the item.
 */
public class HeartwoodCoreBlock extends BaseEntityBlock implements BlockBloodEndpoint {
	public static final MapCodec<HeartwoodCoreBlock> CODEC = simpleCodec(HeartwoodCoreBlock::new);
	public static final IntegerProperty FED = IntegerProperty.create("fed", 0, HeartwoodCoreRules.RAVENOUS_STAGE);
	private static final ParticleColor[] MOTE = {
			new ParticleColor(70, 14, 40), new ParticleColor(110, 18, 48), new ParticleColor(160, 22, 44),
			new ParticleColor(205, 26, 38), new ParticleColor(235, 12, 24) };

	public HeartwoodCoreBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FED, 0));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FED);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HeartwoodCoreBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : HemoTickers.create(type, BlockEntityInit.heartwood_core.get(),
				HeartwoodCoreBlockEntity::serverTick);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		int stage = state.getValue(FED);
		if (random.nextInt(HeartwoodCoreRules.RAVENOUS_STAGE + 1) > stage) return;
		level.addParticle(HemoParticleData.glow(MOTE[stage]),
				pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
				0.0D, 0.01D + stage * 0.004D, 0.0D);
	}

	@Override
	public boolean canDrawAbsorptionParticles(Level level, BlockPos pos, BlockState state) {
		return false;
	}

	@Override
	public double absorbBloodFromBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player, double maxAmount) {
		return 0.0D;
	}

	@Override
	public double projectBloodIntoBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player, double maxAmount) {
		return level.getBlockEntity(pos) instanceof HeartwoodCoreBlockEntity core ? core.receiveProjectedBlood(player, maxAmount) : 0.0D;
	}
}
