package com.vincenthuto.hemomancy.compat.mna.block;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import com.vincenthuto.hemomancy.compat.mna.tile.BrokenManaTrapazahedronBlockEntity;
import com.vincenthuto.hemomancy.compat.mna.tile.MnAPluginBlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BrokenManaTrapazahedronBlock extends WaterloggableBlock implements ITranslucentBlock, EntityBlock {
    protected static final VoxelShape SHAPE_NORMAL = Block.box(0.0, 0.0, 0.0, 16.0, 25.5, 16.0);
    protected static final VoxelShape SHAPE_HANGING = Block.box(0.0, -9.5, 0.0, 16.0, 16.0, 16.0);
    public static final BooleanProperty HANGING = BooleanProperty.create("hanging");

    public BrokenManaTrapazahedronBlock() {
        super(Properties.of().strength(2.0F).noOcclusion().sound(SoundType.GLASS).dynamicShape().isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState(this.defaultBlockState().setValue(HANGING, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean hanging = false;
        if (context.getClickedFace() == Direction.DOWN) {
            hanging = true;
        }

        return super.getStateForPlacement(context).setValue(HANGING, hanging);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING);
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (worldIn.isClientSide && rand.nextBoolean()) {
            Vec3 origin = this.getRandomPointAroundBlock(pos, stateIn, true);
            Vec3 dest = this.getRandomPointAroundBlock(pos, stateIn, false);
            worldIn.addParticle(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get()), origin.x, origin.y, origin.z, dest.x, dest.y, dest.z);
        }
    }

    private Vec3 getRandomPointAroundBlock(BlockPos pos, BlockState state, boolean touch) {
        return new Vec3(
                touch ? pos.getX() + 0.15 + Math.random() * 0.7 : pos.getX() + Math.random(),
                pos.getY() + (state.getValue(HANGING) ? 0 : 1) - 0.5 + Math.random(),
                touch ? pos.getZ() + 0.15 + Math.random() * 0.7 : pos.getZ() + Math.random()
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrokenManaTrapazahedronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == MnAPluginBlockEntityInit.broken_mana_trapazahedron.get() && !level.isClientSide()
                ? (lvl, pos, state1, be) -> BrokenManaTrapazahedronBlockEntity.ServerTick(lvl, pos, state1, (BrokenManaTrapazahedronBlockEntity)be)
                : null;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return 10;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? SHAPE_HANGING : SHAPE_NORMAL;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
