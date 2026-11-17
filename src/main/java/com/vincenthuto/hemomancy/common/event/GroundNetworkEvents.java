package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.world.ground.DarkFields;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * The world's side of the ground network (core-loop Track 6): a creature with blood that dies, or bleeds hard, now and
 * then leaves a stain where it stood (sparse, and they age out). A crop beside a lit cold run does not grow: that is
 * answered by the one crop subscriber, {@link BloodFedGrowthEvents}. Each server level also ticks its fields of dark
 * ({@link DarkFields}, core-loop round two P8).
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class GroundNetworkEvents {
	private GroundNetworkEvents() {
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		LivingEntity body = event.getEntity();
		if (!(body.level() instanceof ServerLevel level) || HemoEntityPredicates.NOBLOOD.test(body)) return;
		if (level.random.nextFloat() < GroundRunRules.KILL_STAIN_CHANCE) GroundNetwork.leaveStain(level, body.blockPosition());
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel level) DarkFields.tick(level);
	}

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof LivingEntity body) || body.tickCount % GroundRunRules.BLEED_CHECK_TICKS != 0
				|| !(body.level() instanceof ServerLevel level) || body instanceof Player player && player.isSpectator()) return;
		MobEffectInstance bleeding = body.getEffect(EffectInit.blood_loss);
		if (bleeding == null || bleeding.getAmplifier() < 1 || !body.onGround() || HemoEntityPredicates.NOBLOOD.test(body)) return;
		if (level.random.nextFloat() < GroundRunRules.BLEED_STAIN_CHANCE) GroundNetwork.leaveStain(level, body.blockPosition());
	}
}
