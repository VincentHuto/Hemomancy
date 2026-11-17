package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineVigilBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The Sanguine Vigil — a Grand-tier Blood Structure that acts as a fane
 * ward-post. It stores up to {@link SanguineVigilBlockEntity#MAX_BLOOD} units
 * of blood and, while stocked, continuously suppresses hostile mobs within
 * {@link SanguineVigilBlockEntity#WARD_RADIUS} blocks by applying the
 * {@code blood_binding} effect.
 *
 * <p>Interactions:</p>
 * <ul>
 *   <li><b>Right-click</b> — deposit {@link #DEPOSIT_PER_CLICK} blood from
 *       the player into the Vigil to keep it stocked.</li>
 *   <li><b>Sneak + right-click</b> — display current blood level and ward
 *       status.</li>
 * </ul>
 *
 * <p>The Vigil is a 1×2×1 multi-block (filler one block above the base). {@link #LIT} is set while it
 * holds blood: the model's eye opens and its gauge runs red (tools/model_export/sanguine_vigil.py).</p>
 */
public class SanguineVigilBlock extends BaseEntityBlock implements IMultiBlock {
    private static final BloodAccounting.Source DEPOSIT = BloodAccounting.Source.of("sanguine_vigil", Category.STATION, false);

    public static final MapCodec<SanguineVigilBlock> CODEC = simpleCodec(SanguineVigilBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** The ward is stocked and watching. */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    // From tools/model_export/sanguine_vigil.py: plinth, socket and shaft below, shaft and yoke above.
    private static final VoxelShape LOWER = Shapes.or(Block.box(2, 0, 2, 14, 3, 14),
            Block.box(4.5, 3, 4.5, 11.5, 5, 11.5), Block.box(5.5, 5, 5.5, 10.5, 16, 10.5));
    private static final VoxelShape UPPER_NS = Shapes.or(Block.box(5.5, 0, 5.5, 10.5, 4, 10.5),
            Block.box(3, 4, 6, 13, 16, 10));
    private static final VoxelShape UPPER_EW = Shapes.or(Block.box(5.5, 0, 5.5, 10.5, 4, 10.5),
            Block.box(6, 4, 3, 10, 16, 13));

    /** Filler block placed one above the base. */
    private static final BlockPos[] FILLER_OFFSETS = { new BlockPos(0, 1, 0) };

    /** Blood deposited per click. */
    public static final double DEPOSIT_PER_CLICK = 500.0;

    public SanguineVigilBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(LIT, false));
    }

    public SanguineVigilBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops()
                .strength(3.0f, 8.0f)
                .sound(SoundType.BONE_BLOCK)
                .lightLevel(s -> s.getValue(LIT) ? 6 : 0)
                .noOcclusion());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ── IMultiBlock ────────────────────────────────────────────────────────────

    @Override
    public BlockPos[] getFillerOffsets() {
        return FILLER_OFFSETS;
    }

    @Override
    public VoxelShape fillerShape(BlockState controller, BlockPos offset) {
        return controller.getValue(FACING).getAxis() == Direction.Axis.X ? UPPER_EW : UPPER_NS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return LOWER;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    // ── BlockState (facing) ────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            placeFillers(level, pos, state);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
            boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                if (level.getBlockEntity(pos) instanceof SanguineVigilBlockEntity vigil) vigil.dropCatalyst(level, pos);
                removeFillers(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    // ── BlockEntity wiring ─────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SanguineVigilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, BlockEntityInit.sanguine_vigil.get(),
                        SanguineVigilBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Interaction ────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult result) {
        return handleInteraction(level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        // A Temper catalyst goes into the Vigil's slot: it tends the kept creatures near it (core-loop Track 5).
        if (com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCatalysts.of(stack) != null) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof SanguineVigilBlockEntity vigil) {
                int moved = vigil.insertCatalyst(stack);
                player.displayClientMessage(Component.translatable(moved > 0
                        ? "block.hemomancy.sanguine_vigil.catalyst_in" : "block.hemomancy.sanguine_vigil.catalyst_full",
                        vigil.catalyst().getHoverName(), vigil.catalyst().getCount()).withStyle(ChatFormatting.GRAY), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        handleInteraction(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    private InteractionResult handleInteraction(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof SanguineVigilBlockEntity vigil))
            return InteractionResult.SUCCESS;

        IBloodVolume playerBlood = HemoCapabilityAccess.getBloodVolume(player);
        if (!playerBlood.isActive()) {
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.sanguine_vigil.inactive").withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }

        var vigilBloodOpt = HemoCapabilityAccess.getBloodVolume(vigil);
        if (vigilBloodOpt.isEmpty()) return InteractionResult.SUCCESS;
        IBloodVolume storage = vigilBloodOpt.get();

        if (player.isShiftKeyDown()) {
            // Status display
            boolean active = storage.getBloodVolume() > 0;
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.sanguine_vigil.status",
                    (int) storage.getBloodVolume(),
                    (int) storage.getMaxBloodVolume(),
                    active
                            ? Component.translatable("block.hemomancy.sanguine_vigil.status.active")
                            : Component.translatable("block.hemomancy.sanguine_vigil.status.dormant"))
                    .withStyle(active ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
            player.sendSystemMessage(vigil.catalyst().isEmpty()
                    ? Component.translatable("block.hemomancy.sanguine_vigil.no_catalyst").withStyle(ChatFormatting.GRAY)
                    : Component.translatable(vigil.tending() ? "block.hemomancy.sanguine_vigil.tending"
                            : "block.hemomancy.sanguine_vigil.not_tending", vigil.catalyst().getHoverName(),
                            vigil.catalyst().getCount()).withStyle(ChatFormatting.GRAY));
        } else {
            // Deposit blood into vigil
            double toDeposit = Math.min(DEPOSIT_PER_CLICK,
                    Math.min(playerBlood.getBloodVolume(),
                            storage.getMaxBloodVolume() - storage.getBloodVolume()));
            if (toDeposit > 0) {
                BloodAccounting.pay(serverPlayer, toDeposit, DEPOSIT);
                storage.addBloodVolume(toDeposit);
                vigil.sendUpdates();
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.sanguine_vigil.fuelled",
                        (int) toDeposit,
                        (int) storage.getBloodVolume(),
                        (int) storage.getMaxBloodVolume())
                        .withStyle(ChatFormatting.DARK_RED));
            } else {
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.sanguine_vigil.full_or_empty",
                        (int) storage.getBloodVolume(),
                        (int) storage.getMaxBloodVolume())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
