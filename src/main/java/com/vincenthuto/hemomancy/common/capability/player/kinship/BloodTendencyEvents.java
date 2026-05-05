package com.vincenthuto.hemomancy.common.capability.player.kinship;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.elderguardian.ElderGuardian;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class BloodTendencyEvents {

	private record TendencyShift(EnumBloodTendency primary, float amount,
			EnumBloodTendency secondary, float secondaryAmount) {
		TendencyShift(EnumBloodTendency tendency, float amount) {
			this(tendency, amount, null, 0f);
		}
	}

	@SubscribeEvent
	public static void onEntityKilled(LivingDeathEvent event) {
		DamageSource source = event.getSource();
		if (source == null || !(source.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.TENDENCY_SHIFT_ON_KILL_ENABLED.get()) return;

		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.isActive()).orElse(false);
		if (!bloodActive) return;

		LivingEntity victim = event.getEntity();
		float base = HemoServerConfig.TENDENCY_SHIFT_AMOUNT.get().floatValue();
		float bossMult = (float) HemoServerConfig.TENDENCY_BOSS_MULTIPLIER.get().doubleValue();

		TendencyShift shift = determineTendencyFromKill(victim, base, bossMult);
		if (shift == null) return;

		HemoCapabilityAccess.getBloodTendency(player).ifPresent(cap -> {
			cap.addTendencyAlignment(shift.primary(), shift.amount());
			if (shift.secondary() != null) {
				cap.addTendencyAlignment(shift.secondary(), shift.secondaryAmount());
			}
			syncTendency((ServerPlayer) player, cap);
		});
	}

	/**
	 * Maps a killed entity to a TendencyShift (tendency + amount). Returns null for
	 * entities that yield no tendency shift (e.g. Snow Golems).
	 *
	 * Check order: specific bosses → general predicates. This ensures INFERNALBLOOD
	 * and ENDERBLOOD are tested before NOBLOOD, so Blazes and Endermen correctly
	 * give FLAMMEUS/TENEBRIS rather than returning null.
	 */
	private static TendencyShift determineTendencyFromKill(LivingEntity entity, float base, float bossMult) {
		// ── Specific bosses (surge amounts, most specific first) ──────────────────
		if (entity instanceof EnderDragon)   return new TendencyShift(EnumBloodTendency.TENEBRIS, base * bossMult);
		// Wither is both death and fire — split the shift evenly
		if (entity instanceof WitherBoss)    return new TendencyShift(EnumBloodTendency.MORTEM, base * bossMult * 0.5f,
				EnumBloodTendency.FLAMMEUS, base * bossMult * 0.5f);
		if (entity instanceof ElderGuardian) return new TendencyShift(EnumBloodTendency.CONGEATIO, base * bossMult);
		if (entity instanceof Warden)        return new TendencyShift(EnumBloodTendency.MORTEM, base * bossMult);

		// ── Specific single mobs (override before general Animal catch) ──────────
		if (entity instanceof Allay)         return new TendencyShift(EnumBloodTendency.ANIMUS, base * 3f);
		if (entity instanceof Bee)           return new TendencyShift(EnumBloodTendency.DUCTILIS, base);

		// ── General predicates (specific before NOBLOOD to fix predicate conflict) ─
		// Fire-aligned (Blaze, Ghast, Hoglin, etc.) — checked before NOBLOOD which also listed Blaze
		if (HemoEntityPredicates.INFERNALBLOOD.test(entity)) return new TendencyShift(EnumBloodTendency.FLAMMEUS, base * 2f);
		// Ender-aligned (EnderMan, Shulker, Phantom, etc.) — checked before NOBLOOD which also listed EnderMan/Shulker
		if (HemoEntityPredicates.ENDERBLOOD.test(entity))    return new TendencyShift(EnumBloodTendency.TENEBRIS, base * 3f);
		// Cold / aquatic (Drowned, Stray, WaterAnimal)
		if (HemoEntityPredicates.COLDBLOODED.test(entity))   return new TendencyShift(EnumBloodTendency.CONGEATIO, base * 2f);
		// Light-aligned (Guardian, GlowSquid, Witch, Allay)
		if (HemoEntityPredicates.LUMINOUS.test(entity))      return new TendencyShift(EnumBloodTendency.LUX, base);
		// Iron/militant constructs (IronGolem, Vindicator, Pillager)
		if (HemoEntityPredicates.IRONCLAD.test(entity))      return new TendencyShift(EnumBloodTendency.FERRIC, base * 2f);
		// All undead (catches Skeleton, WitherSkeleton, Zombie, Spider, Silverfish via isInvertedHealAndHarm or UNDEAD)
		if (entity.isInvertedHealAndHarm() || HemoEntityPredicates.UNDEAD.test(entity))
			return new TendencyShift(EnumBloodTendency.MORTEM, base);
		// Warm-blooded animals and villagers
		if (HemoEntityPredicates.WARMBLOODED.test(entity))   return new TendencyShift(EnumBloodTendency.ANIMUS, base);
		// Slimes/plant-like mobs
		if (HemoEntityPredicates.PLANTBLOOD.test(entity))    return new TendencyShift(EnumBloodTendency.DUCTILIS, base);
		// Explicitly bloodless (SnowGolem, etc.) — after all specific checks
		if (HemoEntityPredicates.NOBLOOD.test(entity))       return null;

		// Fallback for anything else with blood
		return new TendencyShift(EnumBloodTendency.ANIMUS, base);
	}

	/**
	 * Public helper so other systems (e.g. KnownManipulationEvents) can shift
	 * tendency alignment when the player uses a manipulation of a given tendency.
	 */
	public static void shiftTendencyFromManipUse(ServerPlayer player, EnumBloodTendency tendency) {
		if (!HemoServerConfig.TENDENCY_SHIFT_ON_KILL_ENABLED.get()) return;
		float amount = HemoServerConfig.TENDENCY_SHIFT_ON_MANIP_USE.get().floatValue();
		if (amount <= 0) return;

		HemoCapabilityAccess.getBloodTendency(player).ifPresent(cap -> {
			cap.addTendencyAlignment(tendency, amount);
			syncTendency(player, cap);
		});
	}

	// â”€â”€â”€â”€â”€ Sync & Lifecycle â”€â”€â”€â”€â”€

	public static void syncTendency(ServerPlayer player, IBloodTendency tendency) {
		PacketHandler.sendToPlayer(player, new BloodTendencyServerPacket(tendency.getTendency()));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumBloodTendency, Float> bloodTendency = HemoCapabilityAccess.getBloodTendency(player)
				.map(IBloodTendency::getTendency).orElse(Map.of());
		PacketHandler.sendToPlayer(player, new BloodTendencyServerPacket(bloodTendency));
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumBloodTendency, Float> bloodTendency = HemoCapabilityAccess.getBloodTendency(player)
				.map(IBloodTendency::getTendency).orElse(Map.of());
		PacketHandler.sendToPlayer(player, new BloodTendencyServerPacket(bloodTendency));
	}

	@SubscribeEvent
	public static void respawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(player)
						.orElseThrow(IllegalArgumentException::new);
				PacketHandler.sendToPlayer((ServerPlayer) player, new BloodTendencyServerPacket(tendency.getTendency()));
			}
		}
	}

}
