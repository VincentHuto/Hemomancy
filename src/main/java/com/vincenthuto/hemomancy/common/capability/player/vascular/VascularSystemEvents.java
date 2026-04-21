package com.vincenthuto.hemomancy.common.capability.player.vascular;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.VascularSystemServerPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class VascularSystemEvents {

	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("vascularsystem"), new VascularSystemProvider());
		}
	}

	/**
	 * When the player takes damage, degrade a vascular section based on damage type.
	 * <ul>
	 *   <li>Fall damage → legs</li>
	 *   <li>Projectile (arrow, trident) → body/chest</li>
	 *   <li>Explosion → random section</li>
	 *   <li>Melee/generic → random section weighted toward body/arms</li>
	 * </ul>
	 */
	@SubscribeEvent
	public static void onPlayerDamaged(LivingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.VASCULAR_DEGRADATION_ON_DAMAGE_ENABLED.get()) return;

		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.isActive()).orElse(false);
		if (!bloodActive) return;

		HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			float strain = (float) (event.getAmount() * HemoServerConfig.VASCULAR_DAMAGE_PER_HIT.get().doubleValue());
			EnumVeinSections section = determineSectionFromDamage(event, player);

			// Negative value = damage to the section
			vascular.setVascularSectionHealth(section, -strain);

			// Clamp to 0 minimum
			if (vascular.getHealthBySection(section) < 0) {
				Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
				sys.put(section, 0f);
				vascular.setVascularSystem(sys);
			}

			syncVascular((ServerPlayer) player, vascular);
		});
	}

	/**
	 * Determine which vein section is affected based on the damage source type.
	 */
	private static EnumVeinSections determineSectionFromDamage(LivingDamageEvent event, Player player) {
		var source = event.getSource();

		// Fall damage hits the legs
		if (source.is(DamageTypes.FALL)) {
			return player.level().random.nextBoolean() ? EnumVeinSections.LEFTLEG : EnumVeinSections.RIGHTLEG;
		}

		// Projectiles hit the body
		if (source.is(DamageTypes.ARROW) || source.is(DamageTypes.TRIDENT)) {
			return EnumVeinSections.BODY;
		}

		// Explosions hit a random section
		if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
			return getRandomSection(player);
		}

		// Drowning / suffocation hits the head
		if (source.is(DamageTypes.DROWN) || source.is(DamageTypes.IN_WALL)) {
			return EnumVeinSections.HEAD;
		}

		// Fire / lava spreads across the body
		if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.IN_FIRE)) {
			return EnumVeinSections.BODY;
		}

		// Melee/generic — weighted random favoring body and arms
		EnumVeinSections[] meleePool = {
				EnumVeinSections.BODY, EnumVeinSections.BODY,
				EnumVeinSections.LEFTARM, EnumVeinSections.RIGHTARM,
				EnumVeinSections.HEAD
		};
		return meleePool[player.level().random.nextInt(meleePool.length)];
	}

	private static EnumVeinSections getRandomSection(Player player) {
		EnumVeinSections[] all = EnumVeinSections.values();
		return all[player.level().random.nextInt(all.length)];
	}

	/**
	 * Public helper for other systems (e.g. KnownManipulationEvents) to apply
	 * vascular strain when the player uses a blood manipulation.
	 */
	public static void applyManipStrain(ServerPlayer player, EnumVeinSections section) {
		if (!HemoServerConfig.VASCULAR_DEGRADATION_ON_MANIP_ENABLED.get()) return;

		HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			float strain = HemoServerConfig.VASCULAR_MANIP_STRAIN.get().floatValue();
			vascular.setVascularSectionHealth(section, -strain);

			if (vascular.getHealthBySection(section) < 0) {
				Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
				sys.put(section, 0f);
				vascular.setVascularSystem(sys);
			}

			syncVascular(player, vascular);
		});
	}

	/**
	 * Passive vascular healing and debuff application per tick.
	 * <ul>
	 *   <li>All sections slowly heal back toward 100 when the player is well-fed.</li>
	 *   <li>Sections in CLOTTED or DEAD state apply debuffs:</li>
	 *   <li>  HEAD (DEAD) → Blindness, (CLOTTED) → Nausea</li>
	 *   <li>  HEART (DEAD) → Wither, (CLOTTED) → Weakness</li>
	 *   <li>  BODY (DEAD) → Hunger, (CLOTTED) → Mining Fatigue</li>
	 *   <li>  LEG sections (DEAD) → Slowness II, (CLOTTED) → Slowness I</li>
	 *   <li>  ARM sections (DEAD) → Mining Fatigue II, (CLOTTED) → Weakness</li>
	 * </ul>
	 */
	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Player player = event.player;
		if (player.level().isClientSide) return;

		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.isActive()).orElse(false);
		if (!bloodActive) return;

		HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			boolean changed = false;

			// Passive healing
			if (HemoServerConfig.VASCULAR_PASSIVE_HEAL_ENABLED.get()) {
				int healInterval = HemoServerConfig.VASCULAR_HEAL_INTERVAL.get();
				if (player.tickCount % healInterval == 0) {
					float healRate = HemoServerConfig.VASCULAR_HEAL_RATE.get().floatValue();

					// Only heal if the player has enough food / saturation
					boolean wellFed = player.getFoodData().getFoodLevel() > 6;

					for (EnumVeinSections section : EnumVeinSections.values()) {
						float health = vascular.getHealthBySection(section);
						if (health < 100f && wellFed) {
							float newHealth = Math.min(100f, health + healRate);
							Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
							sys.put(section, newHealth);
							vascular.setVascularSystem(sys);
							changed = true;
						}
					}
				}
			}

			// Debuffs from damaged sections — check every 2 seconds
			if (HemoServerConfig.VASCULAR_DEBUFFS_ENABLED.get() && player.tickCount % 40 == 0) {
				applyVascularDebuffs(player, vascular);
			}

			if (changed) {
				syncVascular((ServerPlayer) player, vascular);
			}
		});
	}

	/**
	 * Apply debuffs based on the flow state of each vein section.
	 */
	private static void applyVascularDebuffs(Player player, IVascularSystem vascular) {
		for (EnumVeinSections section : EnumVeinSections.values()) {
			EnumBloodFlow flow = vascular.getBloodFlowBySection(section);

			if (flow == EnumBloodFlow.DEAD) {
				switch (section) {
					case HEAD -> player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, true));
					case HEART -> player.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, false, true));
					case BODY -> player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 1, false, false, true));
					case LEFTLEG, RIGHTLEG -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false, true));
					case LEFTARM, RIGHTARM -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 1, false, false, true));
				}
			} else if (flow == EnumBloodFlow.ClOTTED) {
				switch (section) {
					case HEAD -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false, true));
					case HEART -> player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
					case BODY -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false, true));
					case LEFTLEG, RIGHTLEG -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, true));
					case LEFTARM, RIGHTARM -> player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
				}
			}
		}
	}

	// ───── Sync & Lifecycle ─────

	public static void syncVascular(ServerPlayer player, IVascularSystem vascular) {
		PacketHandler.sendToPlayer(player, new VascularSystemServerPacket(vascular.getVascularSystem()));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumVeinSections, Float> BloodFlow = VascularSystemProvider.getPlayerVascularSystem(player);
		PacketHandler.sendToPlayer(player, new VascularSystemServerPacket(BloodFlow));
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		Player peorig = event.getOriginal();
		Player playernew = event.getEntity();
		if (event.isWasDeath()) {
			peorig.reviveCaps();
			IVascularSystem bloodVolumeNew = HemoCapabilityAccess.getVascularSystem(playernew)
					.orElseThrow(IllegalStateException::new);
			bloodVolumeNew.setVascularSystem(HemoCapabilityAccess.getVascularSystem(peorig)
					.orElseThrow(IllegalArgumentException::new).getVascularSystem());
			peorig.invalidateCaps();
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumVeinSections, Float> BloodFlow = VascularSystemProvider.getPlayerVascularSystem(player);
		PacketHandler.sendToPlayer(player, new VascularSystemServerPacket(BloodFlow));
	}

	@SubscribeEvent
	public static void respawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			if (!player.getCommandSenderWorld().isClientSide) {
				IVascularSystem section = HemoCapabilityAccess.getVascularSystem(player)
						.orElseThrow(IllegalArgumentException::new);
				PacketHandler.sendToPlayer((ServerPlayer) player, new VascularSystemServerPacket(section.getVascularSystem()));
			}
		}
	}

}