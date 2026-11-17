package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HematicSutureNodeBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class HematicSutureNodeBlock extends BaseEntityBlock {
    public static final MapCodec<HematicSutureNodeBlock> CODEC = simpleCodec(HematicSutureNodeBlock::new);
    private static final ParticleColor SEEP = new ParticleColor(150, 14, 26);

    public HematicSutureNodeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HematicSutureNodeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return createTickerHelper(type, BlockEntityInit.hematic_suture_node.get(),
                    HematicSutureNodeBlockEntity::clientTick);
        }
        return createTickerHelper(type, BlockEntityInit.hematic_suture_node.get(),
                HematicSutureNodeBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }
        level.addParticle(HemoParticleData.emberSpark(SEEP, 0.031F, 0.8F, 30),
                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                pos.getY() + 0.55 + random.nextDouble() * 0.25,
                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                0, 0.01, 0);
    }
}
