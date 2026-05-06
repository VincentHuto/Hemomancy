package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.item.MorphicNectarMutationRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.PrimalMorphlingRules;
import com.vincenthuto.hemomancy.common.recipe.MorphicNectarRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

/**
 * Handles morphic nectar item transformation. When an ItemEntity is submerged
 * in morphic_nectar fluid and a matching {@link MorphicNectarRecipe} exists,
 * a per-entity counter increments each tick. On reaching {@code transform_time}
 * the item is consumed and a new ItemEntity with the result spawns in its place.
 *
 * Progress is tracked via {@link net.minecraft.world.entity.Entity#getPersistentData()}
 * so it survives level saves and entity reload.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class MorphicNectarEvents {

	private static final String TICKS_KEY = "hemomancy:morphic_nectar_ticks";
	private static final String PRIMAL_TICKS_KEY = "hemomancy:primal_nectar_ticks";
	private static final int PRIMAL_TRANSFORM_TIME = 200;

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

		Level level = itemEntity.level();
		if (level.isClientSide()) return;

		BlockPos pos = itemEntity.blockPosition();
		FluidState fluid = level.getFluidState(pos);

		if (!fluid.is(FluidInit.MORPHIC_NECTAR.get())
				&& !fluid.is(FluidInit.MORPHIC_NECTAR_FLOWING.get())) {
			// Reset counter when item leaves the fluid
			CompoundTag data = itemEntity.getPersistentData();
			if (data.contains(TICKS_KEY)) {
				data.remove(TICKS_KEY);
			}
			return;
		}

		ItemStack stack = itemEntity.getItem();
		if (stack.getItem() instanceof MorphlingItem) {
			if (tryPrimalizeMorphling(itemEntity, stack, pos)) {
				return;
			}
		}

		List<MorphicNectarRecipe> recipes = MorphicNectarRecipe.getAllRecipes(level);
		MorphicNectarRecipe match = null;
		for (MorphicNectarRecipe r : recipes) {
			if (r.matchesInput(stack)) {
				match = r;
				break;
			}
		}

		if (match == null) return;

		CompoundTag data = itemEntity.getPersistentData();
		int ticks = data.getInt(TICKS_KEY) + 1;
		data.putInt(TICKS_KEY, ticks);

		// Ambient particle pulse every 10 ticks
		if (level instanceof ServerLevel serverLevel && ticks % 10 == 0) {
			serverLevel.sendParticles(ParticleTypes.WITCH,
					itemEntity.getX(), itemEntity.getY() + 0.3, itemEntity.getZ(),
					3, 0.15, 0.1, 0.15, 0.01);
		}

		if (ticks < match.getTransformTime()) return;

		// --- Transform ---
		ItemStack result = match.getResultItemRaw().copy();
		result.setCount(match.getResultItemRaw().getCount() * stack.getCount());
		MorphicNectarMutationRules.markMutated(result);

		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
					itemEntity.getX(), itemEntity.getY() + 0.5, itemEntity.getZ(),
					20, 0.3, 0.3, 0.3, 0.1);
			serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.BLOCKS, 1.0f, 0.8f + level.random.nextFloat() * 0.4f);
		}

		ItemEntity resultEntity = new ItemEntity(level,
				itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
		resultEntity.setDefaultPickUpDelay();
		level.addFreshEntity(resultEntity);
		itemEntity.discard();
	}

	private static boolean tryPrimalizeMorphling(ItemEntity itemEntity, ItemStack stack, BlockPos pos) {
		if (!(itemEntity.level() instanceof ServerLevel serverLevel)) return false;

		ServerPlayer player = findEligiblePrimalizer(serverLevel, itemEntity, stack);
		if (player == null) return true;

		CompoundTag data = itemEntity.getPersistentData();
		int ticks = data.getInt(PRIMAL_TICKS_KEY) + 1;
		data.putInt(PRIMAL_TICKS_KEY, ticks);

		if (ticks % 10 == 0) {
			serverLevel.sendParticles(ParticleTypes.MYCELIUM,
					itemEntity.getX(), itemEntity.getY() + 0.3, itemEntity.getZ(),
					5, 0.2, 0.12, 0.2, 0.02);
		}
		if (ticks < PRIMAL_TRANSFORM_TIME) return true;

		ItemStack result = stack.copy();
		MorphlingItem.setPrimalized(result);
		MorphicNectarMutationRules.markMutated(result);
		serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
				itemEntity.getX(), itemEntity.getY() + 0.5, itemEntity.getZ(),
				28, 0.35, 0.35, 0.35, 0.12);
		serverLevel.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM,
				SoundSource.BLOCKS, 1.0f, 0.8f + serverLevel.random.nextFloat() * 0.25f);

		ItemEntity resultEntity = new ItemEntity(serverLevel,
				itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
		resultEntity.setDefaultPickUpDelay();
		serverLevel.addFreshEntity(resultEntity);
		itemEntity.discard();
		return true;
	}

	private static ServerPlayer findEligiblePrimalizer(ServerLevel level, ItemEntity itemEntity, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		boolean primalized = MorphlingItem.isPrimal(stack);
		AABB area = itemEntity.getBoundingBox().inflate(8.0);
		List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area, ServerPlayer::isAlive);
		for (ServerPlayer player : players) {
			int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
			if (PrimalMorphlingRules.canPrimalize(maturity, primalized, degree)) {
				return player;
			}
		}
		return null;
	}
}
