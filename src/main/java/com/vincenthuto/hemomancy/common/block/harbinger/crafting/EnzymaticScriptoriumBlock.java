package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnzymaticScriptoriumBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty STAGE =
            net.minecraft.world.level.block.state.properties.IntegerProperty.create("stage", 0, 2);

    public EnzymaticScriptoriumBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(STAGE, 0));
    }

    public static int tier(BlockState state) { return 3 + state.getValue(STAGE) * 2; }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, STAGE);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        for (BlockPos offset : net.minecraft.world.level.block.EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) != 0 || com.vincenthuto.hemomancy.common.enchanting.ScriptoriumPower
                    .contribution(level, pos, offset) <= 0) continue;
            double targetX = pos.getX() + 0.5D;
            double targetY = pos.getY() + 1.0D;
            double targetZ = pos.getZ() + 0.5D;
            double dx = offset.getX() + random.nextDouble() - 0.5D;
            double dy = offset.getY() - 0.5D + random.nextDouble() * 0.5D;
            double dz = offset.getZ() + random.nextDouble() - 0.5D;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    targetX, targetY, targetZ, dx, dy, dz);
            level.addParticle(new com.vincenthuto.hemomancy.client.particle.data.WillAbsorptionGlowParticleData(
                    0.35F, 0.015F, 0.025F, false), targetX, targetY, targetZ, dx, dy, dz);
        }
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnzymaticScriptoriumBlockEntity(pos, state);
    }

    private InteractionResult open(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof EnzymaticScriptoriumBlockEntity station) {
            serverPlayer.openMenu(station, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return open(level, pos, player);
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        open(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !(next.getBlock() instanceof EnzymaticScriptoriumBlock)) {
            if (level.getBlockEntity(pos) instanceof EnzymaticScriptoriumBlockEntity station) {
                net.minecraft.world.Containers.dropContents(level, pos, station);
            }
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
