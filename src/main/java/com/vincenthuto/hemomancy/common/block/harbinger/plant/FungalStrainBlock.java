package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

import javax.annotation.Nullable;

/**
 * A fungal strain planted from its spore (core-loop round two, P7). It stands only on its row's substrate
 * ({@link FungalStrains.Substrate}) and needs no light. Its growth goes through the NeoForge crop hooks, so the one crop
 * subscriber decides it: the strains are tagged {@code needs_blood_to_grow}, so an unfed strain never advances, by its
 * own growth or by bone meal. A ripe strain sheds a faint drift of its school's spores and, broken, returns its spore and
 * sometimes another (its loot table). Not a {@code CropBlock}: a spore in the cloche's culture slot stays an enzyme
 * culture, as the First Culture path needs.
 */
public class FungalStrainBlock extends BushBlock implements BonemealableBlock {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	public static final MapCodec<FungalStrainBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			StringRepresentable.fromEnum(FungalStrains::values).fieldOf("strain").forGetter(FungalStrainBlock::strain),
			propertiesCodec()).apply(instance, FungalStrainBlock::new));
	private static final VoxelShape[] SHAPES = {
			Block.box(4.0D, 0.0D, 4.0D, 12.0D, 4.0D, 12.0D),
			Block.box(3.0D, 0.0D, 3.0D, 13.0D, 7.0D, 13.0D),
			Block.box(2.0D, 0.0D, 2.0D, 14.0D, 10.0D, 14.0D),
			Block.box(1.0D, 0.0D, 1.0D, 15.0D, 13.0D, 15.0D)};

	private final FungalStrains strain;

	public FungalStrainBlock(FungalStrains strain, BlockBehaviour.Properties properties) {
		super(properties);
		this.strain = strain;
		registerDefaultState(stateDefinition.any().setValue(AGE, 0));
	}

	public FungalStrains strain() {
		return strain;
	}

	@Override
	protected MapCodec<? extends BushBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(AGE)];
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(strain.substrate().tag());
	}

	/** Only its own substrate holds it, whatever that block says about plants in general. */
	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return mayPlaceOn(level.getBlockState(below), level, below);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return !FungalStrainRules.ripe(state.getValue(AGE));
	}

	/** One stage on a won growth check, asked through the crop hook so fed ground has its say. */
	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (FungalStrainRules.ripe(age)) return;
		if (CommonHooks.canCropGrow(level, pos, state, FungalStrainRules.ownRollGrows(random.nextInt(FungalStrainRules.GROW_ROLL)))) {
			level.setBlock(pos, state.setValue(AGE, FungalStrainRules.advance(age, 1)), Block.UPDATE_CLIENTS);
			CommonHooks.fireCropGrowPost(level, pos, state);
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return !FungalStrainRules.ripe(state.getValue(AGE));
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		level.setBlock(pos, state.setValue(AGE, FungalStrainRules.advance(state.getValue(AGE), FungalStrainRules.BONEMEAL_STAGES)),
				Block.UPDATE_CLIENTS);
	}

	/** The first ripe strain a Harbinger cuts is the strain-bed first; anyone else just takes the spores. */
	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
		super.playerDestroy(level, player, pos, state, blockEntity, tool);
		if (player instanceof ServerPlayer server && FungalStrainRules.ripe(state.getValue(AGE))
				&& HemoCapabilityAccess.getBloodVolume(server).isActive()) {
			HarbingerAdvancementGranter.grantIfNotDone(server, HarbingerAdvancementGranter.ADV_FIRST_STRAIN_BED);
		}
	}

	/** A ripe strain sheds a few of its school's spores, drifting up off the caps. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!FungalStrainRules.ripe(state.getValue(AGE)) || random.nextInt(6) != 0) return;
		int rgb = strain.moteColor();
		ParticleColor color = new ParticleColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
		level.addParticle(HemoParticleData.sporiticSpore(color), pos.getX() + 0.25D + random.nextDouble() * 0.5D,
				pos.getY() + 0.55D + random.nextDouble() * 0.3D, pos.getZ() + 0.25D + random.nextDouble() * 0.5D,
				(random.nextDouble() - 0.5D) * 0.01D, 0.012D + random.nextDouble() * 0.01D, (random.nextDouble() - 0.5D) * 0.01D);
	}
}
