package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.recipe.RenderingRecipe;
import com.vincenthuto.hemomancy.common.world.RenderingPoolRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Rendering pools break down what an Apotheos throws into them. An item thrown by anyone else, dropped on death or
 * with no thrower floats untouched: the pool never takes what was not offered.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class RenderingPoolEvents {
	private static final String TICKS_KEY = "hemomancy:rendering_ticks";
	/** How many items were offered when the stack entered the bile; a stack merged in later is not the offerer's. */
	private static final String OFFERED_KEY = "hemomancy:rendering_offered";
	private static final ParticleColor PALE_SPORE = new ParticleColor(214, 196, 150);
	private static final ParticleColor CRIMSON_SPORE = new ParticleColor(150, 40, 58);

	private RenderingPoolEvents() {}

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof ItemEntity item) || !(item.level() instanceof ServerLevel level)) return;
		CompoundTag data = item.getPersistentData();
		if (!inBile(level.getFluidState(item.blockPosition()))) {
			data.remove(TICKS_KEY);
			data.remove(OFFERED_KEY);
			return;
		}
		boolean apotheos = item.getOwner() instanceof Player thrower && ApotheosBodyEvents.isApotheos(thrower);
		render(level, item, apotheos);
	}

	/** One tick of an item in bile; {@code offeredByApotheos} is whether its thrower is an Apotheos. */
	public static void render(ServerLevel level, ItemEntity item, boolean offeredByApotheos) {
		if (!RenderingPoolRules.mayRender(offeredByApotheos)) return;
		CompoundTag data = item.getPersistentData();
		ItemStack stack = item.getItem();
		RenderingRecipe match = null;
		for (RenderingRecipe recipe : RenderingRecipe.getAllRecipes(level)) {
			if (recipe.matchesInput(stack)) {
				match = recipe;
				break;
			}
		}
		if (match == null) return;

		if (!data.contains(OFFERED_KEY)) data.putInt(OFFERED_KEY, stack.getCount());
		int offered = Math.min(stack.getCount(), data.getInt(OFFERED_KEY));
		int ticks = data.getInt(TICKS_KEY) + 1;
		data.putInt(TICKS_KEY, ticks);
		if (ticks % RenderingPoolRules.PULSE_TICKS == 0) {
			level.sendParticles(HemoParticleData.sporiticSpore(PALE_SPORE), item.getX(), item.getY() + 0.3, item.getZ(),
					3, 0.15, 0.1, 0.15, 0.01);
		}
		if (ticks < match.getRenderTime()) return;

		for (RenderingRecipe.Result result : match.getResults()) {
			int count = RenderingPoolRules.yield(result.stack().getCount(), offered, result.chance(),
					level.random);
			while (count > 0) {
				ItemStack out = result.stack().copyWithCount(Math.min(count, result.stack().getMaxStackSize()));
				count -= out.getCount();
				ItemEntity spawned = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), out);
				spawned.setDefaultPickUpDelay();
				level.addFreshEntity(spawned);
			}
		}
		level.sendParticles(HemoParticleData.sporiticSpore(CRIMSON_SPORE), item.getX(), item.getY() + 0.4, item.getZ(),
				16, 0.3, 0.25, 0.3, 0.03);
		level.playSound(null, item.blockPosition(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 0.9f,
				0.6f + level.random.nextFloat() * 0.2f);
		int surplus = stack.getCount() - offered;
		if (surplus > 0) {
			ItemEntity left = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), stack.copyWithCount(surplus));
			left.setDefaultPickUpDelay();
			level.addFreshEntity(left);
		}
		item.discard();
	}

	private static boolean inBile(FluidState fluid) {
		return fluid.is(FluidInit.RENDERING_BILE.get()) || fluid.is(FluidInit.RENDERING_BILE_FLOWING.get());
	}
}
