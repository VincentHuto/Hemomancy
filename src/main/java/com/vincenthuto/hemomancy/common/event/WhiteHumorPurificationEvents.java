package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.unstained.plant.LetheanPoppyBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.recipe.WhiteHumorPurificationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles item purification in white humor pools. Nearby Unstained absorber
 * blocks accelerate the process and slowly tarnish as they take on taint.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class WhiteHumorPurificationEvents {

	private static final String TICKS_KEY = "hemomancy:white_humor_purification_ticks";
	private static final String RECIPE_KEY = "hemomancy:white_humor_purification_recipe";
	private static final int ABSORBER_RADIUS = 4;
	private static final int SOURCE_SEARCH_RADIUS = 2;
	private static final int MAX_ABSORBER_BONUS = 4;
	private static final int ABSORBER_DEGRADE_INTERVAL = 80;

	private static final Map<Block, Block> CANDLE_TARNISH = createCandleTarnishMap();
	private static final Map<Block, Block> COPPER_TARNISH = createCopperTarnishMap();

	@SubscribeEvent
	public static void onEntityTickPre(EntityTickEvent.Pre event) {
		if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

		Level level = itemEntity.level();
		if (level.isClientSide()) return;

		if (isWhiteHumor(level.getFluidState(itemEntity.blockPosition()))
				&& findMatch(level, itemEntity.getItem()) != null) {
			itemEntity.setExtendedLifetime();
		}
	}

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

		Level level = itemEntity.level();
		if (level.isClientSide()) return;

		BlockPos pos = itemEntity.blockPosition();
		FluidState fluid = level.getFluidState(pos);
		if (!isWhiteHumor(fluid)) {
			resetProgress(itemEntity);
			return;
		}

		ItemStack stack = itemEntity.getItem();
		WhiteHumorPurificationRecipe match = findMatch(level, stack);
		if (match == null) {
			resetProgress(itemEntity);
			return;
		}

		CompoundTag data = itemEntity.getPersistentData();
		String recipeId = match.getId().toString();
		if (data.contains(RECIPE_KEY) && !recipeId.equals(data.getString(RECIPE_KEY))) {
			data.putInt(TICKS_KEY, 0);
		}
		data.putString(RECIPE_KEY, recipeId);

		AbsorberScan scan = scanAbsorbers(level, pos);
		int progress = 1 + Math.min(scan.bonus(), MAX_ABSORBER_BONUS);
		int ticks = data.getInt(TICKS_KEY) + progress;
		data.putInt(TICKS_KEY, ticks);

		if (level instanceof ServerLevel serverLevel && itemEntity.tickCount % 10 == 0) {
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					itemEntity.getX(), itemEntity.getY() + 0.25, itemEntity.getZ(),
					2 + Math.min(scan.bonus(), MAX_ABSORBER_BONUS), 0.12, 0.08, 0.12, 0.01);
		}

		if (itemEntity.tickCount % ABSORBER_DEGRADE_INTERVAL == 0) {
			degradeOneAbsorber(level, scan.positions());
		}

		if (ticks < match.getTransformTime()) return;

		if (!(level instanceof ServerLevel serverLevel)) return;

		BlockPos sourcePos = findChargedSource(serverLevel, pos);
		if (sourcePos == null) {
			data.putInt(TICKS_KEY, match.getTransformTime());
			return;
		}

		WhiteHumorPoolSavedData poolData = WhiteHumorPoolSavedData.get(serverLevel);
		int sourceCharges = poolData.getRemainingCharges(sourcePos);
		int transformedCount = Math.min(stack.getCount(), sourceCharges);
		if (transformedCount <= 0) {
			data.putInt(TICKS_KEY, match.getTransformTime());
			return;
		}
		poolData.consumeCharges(sourcePos, transformedCount);
		if (transformedCount >= sourceCharges) {
			level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			poolData.clearSource(sourcePos);
		}

		ItemStack result = match.getResultItemRaw().copy();
		result.setCount(match.getResultItemRaw().getCount() * transformedCount);

		serverLevel.sendParticles(ParticleTypes.POOF,
				itemEntity.getX(), itemEntity.getY() + 0.45, itemEntity.getZ(),
				16, 0.25, 0.2, 0.25, 0.02);
		serverLevel.sendParticles(ParticleTypes.END_ROD,
				itemEntity.getX(), itemEntity.getY() + 0.55, itemEntity.getZ(),
				12, 0.25, 0.25, 0.25, 0.05);
		serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
				SoundSource.BLOCKS, 0.9f, 1.4f + level.random.nextFloat() * 0.25f);

		ItemEntity resultEntity = new ItemEntity(level,
				itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
		resultEntity.setDefaultPickUpDelay();
		level.addFreshEntity(resultEntity);
		if (transformedCount >= stack.getCount()) {
			itemEntity.discard();
		} else {
			stack.shrink(transformedCount);
			resetProgress(itemEntity);
		}
	}

	private static boolean isWhiteHumor(FluidState fluid) {
		return fluid.is(FluidInit.WHITE_HUMOR.get())
				|| fluid.is(FluidInit.WHITE_HUMOR_FLOWING.get());
	}

	private static WhiteHumorPurificationRecipe findMatch(Level level, ItemStack stack) {
		for (WhiteHumorPurificationRecipe recipe : WhiteHumorPurificationRecipe.getAllRecipes(level)) {
			if (recipe.matchesInput(stack)) {
				return recipe;
			}
		}
		return null;
	}

	private static BlockPos findChargedSource(ServerLevel level, BlockPos center) {
		WhiteHumorPoolSavedData poolData = WhiteHumorPoolSavedData.get(level);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;

		for (int x = -SOURCE_SEARCH_RADIUS; x <= SOURCE_SEARCH_RADIUS; x++) {
			for (int y = -SOURCE_SEARCH_RADIUS; y <= SOURCE_SEARCH_RADIUS; y++) {
				for (int z = -SOURCE_SEARCH_RADIUS; z <= SOURCE_SEARCH_RADIUS; z++) {
					cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
					FluidState fluid = level.getFluidState(cursor);
					if (!isWhiteHumor(fluid) || !fluid.isSource()) continue;
					if (poolData.getRemainingCharges(cursor) <= 0) continue;

					double distance = cursor.distSqr(center);
					if (distance < bestDistance) {
						bestDistance = distance;
						best = cursor.immutable();
					}
				}
			}
		}
		return best;
	}

	private static void resetProgress(ItemEntity itemEntity) {
		CompoundTag data = itemEntity.getPersistentData();
		if (data.contains(TICKS_KEY)) {
			data.remove(TICKS_KEY);
		}
		if (data.contains(RECIPE_KEY)) {
			data.remove(RECIPE_KEY);
		}
	}

	private static AbsorberScan scanAbsorbers(Level level, BlockPos center) {
		int bonus = 0;
		List<BlockPos> positions = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = -ABSORBER_RADIUS; x <= ABSORBER_RADIUS; x++) {
			for (int y = -ABSORBER_RADIUS; y <= ABSORBER_RADIUS; y++) {
				for (int z = -ABSORBER_RADIUS; z <= ABSORBER_RADIUS; z++) {
					cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
					BlockState state = level.getBlockState(cursor);
					int value = absorberValue(state);
					if (value <= 0) continue;
					bonus += value;
					positions.add(cursor.immutable());
				}
			}
		}
		return new AbsorberScan(bonus, positions);
	}

	private static int absorberValue(BlockState state) {
		if (state.is(BlockInit.lethean_poppy.get())
				&& state.getValue(LetheanPoppyBlock.STATE) == LetheanPoppyBlock.BLOOMED) {
			return 2;
		}
		if (CANDLE_TARNISH.containsKey(state.getBlock())) {
			return 1;
		}
		if (COPPER_TARNISH.containsKey(state.getBlock())) {
			return 1;
		}
		return 0;
	}

	private static void degradeOneAbsorber(Level level, List<BlockPos> positions) {
		if (positions.isEmpty()) return;
		BlockPos pos = positions.get(level.random.nextInt(positions.size()));
		BlockState state = level.getBlockState(pos);
		BlockState tarnished = tarnishedState(state);
		if (tarnished == null) return;

		level.setBlock(pos, tarnished, Block.UPDATE_ALL);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SCRAPE,
					pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					6, 0.25, 0.25, 0.25, 0.01);
		}
		level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 0.45f, 1.55f);
	}

	private static BlockState tarnishedState(BlockState state) {
		if (state.is(BlockInit.lethean_poppy.get())
				&& state.getValue(LetheanPoppyBlock.STATE) == LetheanPoppyBlock.BLOOMED) {
			return state.setValue(LetheanPoppyBlock.STATE, LetheanPoppyBlock.DORMANT);
		}

		Block candleTarget = CANDLE_TARNISH.get(state.getBlock());
		if (candleTarget != null) {
			return copyMatchingProperties(state, candleTarget.defaultBlockState());
		}

		Block copperTarget = COPPER_TARNISH.get(state.getBlock());
		if (copperTarget != null) {
			return copyMatchingProperties(state, copperTarget.defaultBlockState());
		}

		return null;
	}

	private static Map<Block, Block> createCandleTarnishMap() {
		Map<Block, Block> map = new HashMap<>();
		map.put(Blocks.WHITE_CANDLE, Blocks.LIGHT_GRAY_CANDLE);
		map.put(Blocks.LIGHT_GRAY_CANDLE, Blocks.GRAY_CANDLE);
		map.put(Blocks.GRAY_CANDLE, Blocks.BLACK_CANDLE);
		return map;
	}

	private static Map<Block, Block> createCopperTarnishMap() {
		Map<Block, Block> map = new HashMap<>();
		map.put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER);
		map.put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER);
		map.put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER);
		map.put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER);
		map.put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER);
		map.put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER);
		map.put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS);
		map.put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS);
		map.put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS);
		map.put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB);
		map.put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB);
		map.put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB);
		return map;
	}

	private static BlockState copyMatchingProperties(BlockState source, BlockState target) {
		for (Property<?> prop : source.getBlock().getStateDefinition().getProperties()) {
			if (target.hasProperty(prop)) {
				target = copyProperty(source, target, prop);
			}
		}
		if (target.hasProperty(BlockStateProperties.LIT)) {
			target = target.setValue(BlockStateProperties.LIT, false);
		}
		return target;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> BlockState copyProperty(
			BlockState source, BlockState target, Property<T> prop) {
		return target.setValue(prop, source.getValue(prop));
	}

	private record AbsorberScan(int bonus, List<BlockPos> positions) {}
}
