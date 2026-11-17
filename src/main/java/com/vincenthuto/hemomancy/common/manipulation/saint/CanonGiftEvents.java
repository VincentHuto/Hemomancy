package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** The Saint gifts' ticks and their ends on death and dimension change (D-1 spec 2.8). */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CanonGiftEvents {
	private CanonGiftEvents() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
		UnclosingEyeManip.tick(player);
		BloomOfRotManip.tick(player);
		EndlessHourManip.tick(player);
		CanonGiftState.TitheRelease release = CanonGiftState.releases().get(player.getUUID());
		if (release == null) return;
		ServerLevel level = player.serverLevel();
		if (level.getGameTime() - release.born() >= CrimsonTitheRules.CLOSE_END) CanonGiftState.releases().remove(player.getUUID());
		else CrimsonTitheManip.floodTick(level, release, player.getUUID());
	}

	/** Growth falls by one every 40 ticks once out of the beam, and goes with its body. */
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		MinecraftServer server = event.getServer();
		Iterator<Map.Entry<UUID, CanonGiftState.Growth>> entries = CanonGiftState.growth().entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<UUID, CanonGiftState.Growth> entry = entries.next();
			CanonGiftState.Growth growth = entry.getValue();
			ServerLevel level = growth.dimension == null ? null : server.getLevel(growth.dimension);
			Entity body = level == null ? null : level.getEntity(entry.getKey());
			if (!(body instanceof LivingEntity living) || !living.isAlive()) {
				entries.remove();
				continue;
			}
			long now = level.getGameTime();
			if (now - growth.lastBeam < UnclosingEyeRules.DECAY_TICKS || now - growth.lastDecay < UnclosingEyeRules.DECAY_TICKS)
				continue;
			growth.lastDecay = now;
			growth.growth--;
			if (growth.growth <= 0) {
				entries.remove();
				ManipulationVisuals.attached(living, ManipulationVisuals.Form.EYE_GROWTH, 0, 0, 0);
			} else {
				ManipulationVisuals.attached(living, ManipulationVisuals.Form.EYE_GROWTH, growth.growth / 8.0D, 60,
						Math.max(1, UnclosingEyeRules.stage(growth.growth)));
			}
		}
		Iterator<Map.Entry<UUID, CanonGiftState.Colony>> colonies = CanonGiftState.colonies().entrySet().iterator();
		while (colonies.hasNext()) {
			Map.Entry<UUID, CanonGiftState.Colony> entry = colonies.next();
			CanonGiftState.Colony colony = entry.getValue();
			ServerLevel level = colony.dimension == null ? null : server.getLevel(colony.dimension);
			Entity body = level == null ? null : level.getEntity(entry.getKey());
			if (!(body instanceof LivingEntity living) || !living.isAlive()
					|| BloomOfRotManip.decay(living, colony, level.getGameTime()))
				colonies.remove();
		}
		Iterator<Map.Entry<UUID, CanonGiftState.Stillness>> stillness = CanonGiftState.stillness().entrySet().iterator();
		while (stillness.hasNext()) {
			Map.Entry<UUID, CanonGiftState.Stillness> entry = stillness.next();
			CanonGiftState.Stillness still = entry.getValue();
			ServerLevel level = still.dimension == null ? null : server.getLevel(still.dimension);
			Entity body = level == null ? null : level.getEntity(entry.getKey());
			if (!(body instanceof LivingEntity living) || !living.isAlive()
					|| EndlessHourManip.drain(living, still, level.getGameTime()))
				stillness.remove();
		}
	}

	/** A landed hit on a living, unlit Bloom field stirs its Kindling; a hit on a planted caster counts toward the Bow. */
	@SubscribeEvent
	public static void onDamaged(LivingDamageEvent.Post event) {
		if (event.getEntity().level().isClientSide) return;
		BloomOfRotManip.stir(event.getEntity(), event.getSource(), event.getNewDamage());
		if (event.getEntity() instanceof ServerPlayer player) EndlessHourManip.wounded(player, event.getNewDamage());
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) CanonGiftState.end(player.getUUID());
	}

	@SubscribeEvent
	public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) CanonGiftState.end(player.getUUID());
	}

	/**
	 * The gifts' prices and guards on the body, in the defenders' stage of {@code DamageStages}: a Brimming Tithe and
	 * the Eye's Lapse each make the caster take more damage; the Endless Hour guards its planted caster, shields them in
	 * their self-set and softens them in the Bow.
	 */
	public static void applyBodyStates(LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		float scale = 1.0F;
		if (CrimsonTitheManip.brimming(player)) scale += (float) CrimsonTitheRules.BRIMMING_HURT;
		if (UnclosingEyeManip.inLapse(player)) scale += (float) UnclosingEyeRules.LAPSE_HURT;
		scale *= (float) EndlessHourManip.taken(player);
		if (scale != 1.0F) event.setNewDamage(event.getNewDamage() * scale);
	}
}
