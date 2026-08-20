package com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.ConserveStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarNoeticRoutingRules;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.VascularSystemServerPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.ArrayList;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class VascularSystemEvents {	/**
	 * When the player takes damage, degrade a vascular section based on damage type.
	 * <ul>
	 *   <li>Fall damage â†’ legs</li>
	 *   <li>Projectile (arrow, trident) â†’ body/chest</li>
	 *   <li>Explosion â†’ random section</li>
	 *   <li>Melee/generic â†’ random section weighted toward body/arms</li>
	 * </ul>
	 */
	@SubscribeEvent
	public static void onPlayerDamaged(LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.VASCULAR_DEGRADATION_ON_DAMAGE_ENABLED.get()) return;

		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.isActive()).orElse(false);
		if (!bloodActive) return;

		HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			boolean fortified = HemoCapabilityAccess.getInitiatoryDegree(player)
					.map(degree -> degree.hasHematicFortification()).orElse(false);
			float strain = HematicFortificationRules.adjustedStrain(
					(float) (event.getNewDamage() * HemoServerConfig.VASCULAR_DAMAGE_PER_HIT.get().doubleValue()),
					fortified);
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
	private static EnumVeinSections determineSectionFromDamage(LivingDamageEvent.Pre event, Player player) {
		var source = event.getSource();

		// Fall damage hits the legs
		if (source.is(DamageTypes.FALL)) {
			return EnumVeinSections.LEGS;
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

		// Melee/generic â€” weighted random favoring body and arms
		EnumVeinSections[] meleePool = {
				EnumVeinSections.BODY, EnumVeinSections.BODY,
				EnumVeinSections.ARMS, EnumVeinSections.ARMS,
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
			boolean fortified = HemoCapabilityAccess.getInitiatoryDegree(player)
					.map(degree -> degree.hasHematicFortification()).orElse(false);
			float strain = HematicFortificationRules.adjustedStrain(
					HemoServerConfig.VASCULAR_MANIP_STRAIN.get().floatValue(), fortified);
			vascular.setVascularSectionHealth(section, -strain);

			if (vascular.getHealthBySection(section) < 0) {
				Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
				sys.put(section, 0f);
				vascular.setVascularSystem(sys);
			}

			syncVascular(player, vascular);
		});
	}

	public static void applyManipStrain(ServerPlayer player, BloodManipulation manipulation) {
		ArrayList<ScarDefinition> active = new ArrayList<>();
		HemoCapabilityAccess.getScarState(player).ifPresent(scars -> scars.forEachActiveCerebralScar(active::add));
		int tier = ScarNoeticRoutingRules.qualifies(manipulation.getName())
				? ScarNoeticRoutingRules.bestMatchingTier(manipulation.getTend(), active) : 0;
		if (HemoServerConfig.VASCULAR_DEGRADATION_ON_MANIP_ENABLED.get()) HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			boolean fortified = HemoCapabilityAccess.getInitiatoryDegree(player)
					.map(degree -> degree.hasHematicFortification()).orElse(false);
			float strain = HematicFortificationRules.adjustedStrain(
					HemoServerConfig.VASCULAR_MANIP_STRAIN.get().floatValue(), fortified);
			vascular.setVascularSectionHealth(manipulation.getSection(),
					-ScarNoeticRoutingRules.adjustedStrain(strain, tier));
			if (vascular.getHealthBySection(manipulation.getSection()) < 0) {
				Map<EnumVeinSections, Float> system = vascular.getVascularSystem();
				system.put(manipulation.getSection(), 0F);
				vascular.setVascularSystem(system);
			}
			syncVascular(player, vascular);
		});
		if (tier > 0) VeinMasonAssignments.onMatchingNoeticCast(player);
	}

	/**
	 * Passive vascular healing and debuff application per tick.
	 * <ul>
	 *   <li>All sections slowly heal back toward 100 when the player is well-fed.</li>
	 *   <li>Sections in CLOTTED or DEAD state apply debuffs:</li>
	 *   <li>  HEAD (DEAD) â†’ Blindness, (CLOTTED) â†’ Nausea</li>
	 *   <li>  HEART (DEAD) â†’ Wither, (CLOTTED) â†’ Weakness</li>
	 *   <li>  BODY (DEAD) â†’ Hunger, (CLOTTED) â†’ Mining Fatigue</li>
	 *   <li>  LEGS (DEAD) â†’ Slowness II, (CLOTTED) â†’ Slowness I</li>
	 *   <li>  ARMS (DEAD) â†’ Mining Fatigue II, (CLOTTED) â†’ Weakness</li>
	 * </ul>
	 */
	@SubscribeEvent
	public static void playerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
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
					float healRate = (float) (HemoServerConfig.VASCULAR_HEAL_RATE.get().floatValue()
							* ConserveStateHelper.vascularHealMultiplier(player));

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

			// Debuffs from damaged sections â€” check every 2 seconds
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
					case LEGS -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false, true));
					case ARMS -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 1, false, false, true));
				}
			} else if (flow == EnumBloodFlow.ClOTTED) {
				switch (section) {
					case HEAD -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false, true));
					case HEART -> player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
					case BODY -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false, true));
					case LEGS -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, true));
					case ARMS -> player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
				}
			}
		}
	}

	// â”€â”€â”€â”€â”€ Sync & Lifecycle â”€â”€â”€â”€â”€

	public static void syncVascular(ServerPlayer player, IVascularSystem vascular) {
		PacketHandler.sendToPlayer(player, new VascularSystemServerPacket(vascular.getVascularSystem()));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumVeinSections, Float> bloodFlow = HemoCapabilityAccess.getPlayerVascularSystem(player);
		PacketHandler.sendToPlayer(player, new VascularSystemServerPacket(bloodFlow));
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumVeinSections, Float> bloodFlow = HemoCapabilityAccess.getPlayerVascularSystem(player);
		PacketHandler.sendToPlayer(player, new VascularSystemServerPacket(bloodFlow));
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
