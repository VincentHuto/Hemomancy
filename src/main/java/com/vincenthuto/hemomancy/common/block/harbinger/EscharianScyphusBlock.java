package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public final class EscharianScyphusBlock extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, 5);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final EnumMap<Direction, VoxelShape[]> SHAPES = shapes();

    public EscharianScyphusBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(COUNT, 1)
                .setValue(WATERLOGGED, false));
    }

    private static EnumMap<Direction, VoxelShape[]> shapes() {
        var shapes = new EnumMap<Direction, VoxelShape[]>(Direction.class);
        List<List<double[]>> elements = modelElements();
        for (Direction face : Direction.values()) {
            VoxelShape[] counts = new VoxelShape[6];
            for (int count = 1; count <= 5; count++) {
                VoxelShape shape = Shapes.empty();
                for (double[] b : elements.get(count)) {
                    double[] a = orient(face, b[0], b[1], b[2]);
                    double[] c = orient(face, b[3], b[4], b[5]);
                    shape = Shapes.or(shape, box(Math.min(a[0],c[0]), Math.min(a[1],c[1]), Math.min(a[2],c[2]),
                            Math.max(a[0],c[0]), Math.max(a[1],c[1]), Math.max(a[2],c[2])));
                }
                counts[count] = shape.optimize();
            }
            shapes.put(face, counts);
        }
        return shapes;
    }

    private static List<List<double[]>> modelElements() {
        List<List<double[]>> result = new ArrayList<>();
        for (int count = 0; count <= 5; count++) result.add(new ArrayList<>());
        smallCup(result.get(1), 6.5, 3.5);
        smallCup(result.get(2), 1.5, 0.5); smallCup(result.get(2), 7.5, 8.5);
        smallCup(result.get(3), 6.5, 0.5); smallCup(result.get(3), 0.5, 8); smallCup(result.get(3), 8.5, 8.5);
        smallCup(result.get(4), 8.5, 0.5); smallCup(result.get(4), 0.5, 8.5);
        smallCup(result.get(4), 0.5, 0.5); smallCup(result.get(4), 8.5, 8.5);
        largeCup(result.get(5));
        return result;
    }

    private static void smallCup(List<double[]> boxes, double x, double z) {
        boxes.add(new double[]{x+1,0,z+1,x+6,1,z+6});
        boxes.add(new double[]{x+1,.375,z+6,x+6,1.375,z+6.5});
        boxes.add(new double[]{x+.5,.625,z+6.5,x+6.5,1.625,z+7});
        boxes.add(new double[]{x+1,.375,z+.5,x+6,1.375,z+1});
        boxes.add(new double[]{x+.5,.625,z,x+6.5,1.625,z+.5});
        boxes.add(new double[]{x+6,.375,z+.5,x+6.5,1.375,z+6.5});
        boxes.add(new double[]{x+6.5,.625,z+.5,x+7,1.625,z+6.5});
        boxes.add(new double[]{x+.5,.375,z+.5,x+1,1.375,z+6.5});
        boxes.add(new double[]{x,.625,z+.5,x+.5,1.625,z+6.5});
    }

    private static void largeCup(List<double[]> boxes) {
        boxes.add(new double[]{3,0,3,13,2,13});
        boxes.add(new double[]{2,1.25,14,14,3.25,15}); boxes.add(new double[]{3,.75,13,13,2.75,14});
        boxes.add(new double[]{2,1.25,1,14,3.25,2}); boxes.add(new double[]{3,.75,2,13,2.75,3});
        boxes.add(new double[]{14,1.25,2,15,3.25,14}); boxes.add(new double[]{13,.75,2,14,2.75,14});
        boxes.add(new double[]{1,1.25,2,2,3.25,14}); boxes.add(new double[]{2,.75,2,3,2.75,14});
    }

    private static double[] orient(Direction face, double x, double y, double z) {
        return switch (face) {
            case UP -> new double[]{x,y,z};
            case DOWN -> new double[]{x,16-y,16-z};
            case NORTH -> new double[]{x,z,16-y};
            case SOUTH -> new double[]{16-x,z,y};
            case EAST -> new double[]{y,z,x};
            case WEST -> new double[]{16-y,z,16-x};
        };
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, COUNT, WATERLOGGED);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState current = context.getLevel().getBlockState(context.getClickedPos());
        if (current.is(this)) return current.setValue(COUNT, Math.min(5, current.getValue(COUNT)+1));
        var state = defaultBlockState().setValue(FACING, context.getClickedFace())
                .setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }
    @Override public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return (!context.isSecondaryUseActive() && context.getItemInHand().is(asItem()) && state.getValue(COUNT)<5)
                || super.canBeReplaced(state, context);
    }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction face = state.getValue(FACING);
        BlockPos support = pos.relative(face.getOpposite());
        var backing = level.getBlockState(support);
        return !backing.is(BlockInit.escharian_overgrowth_rim.get()) && backing.isFaceSturdy(level, support, face);
    }
    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
        // Air requests normal destruction: Minecraft drops the item and restores the state's water.
        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }
    @Override public FluidState getFluidState(BlockState state) { return WaterloggedBlockSupport.fluidState(state); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING))[state.getValue(COUNT)];
    }
    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
