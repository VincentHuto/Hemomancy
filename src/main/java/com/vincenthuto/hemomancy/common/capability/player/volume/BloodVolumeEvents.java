package com.vincenthuto.hemomancy.common.capability.player.volume;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class BloodVolumeEvents {
	@SubscribeEvent
	public static void playerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide) return;

		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (!volume.isActive()) return;

			// â”€â”€ Skill: Capacity â€” add flat bonus to max blood â”€â”€
			double baseMax = 5000.0;
			double capacityBonus = SkillPointHelper.getCapacityBonus();
			double desiredMax = baseMax + capacityBonus;
			if (Math.abs(volume.getMaxBloodVolume() - desiredMax) > 0.01) {
				volume.setMaxBloodVolume(desiredMax);
			}

			// â”€â”€ Passive blood regen â”€â”€
			if (HemoServerConfig.BLOOD_REGEN_ENABLED.get()) {
				int interval = HemoServerConfig.BLOOD_REGEN_INTERVAL.get();
				if (player.tickCount % interval == 0 && !volume.isFull()) {
					double regenRate = HemoServerConfig.BLOOD_REGEN_RATE.get();
					volume.fill(regenRate);
					syncVolume((ServerPlayer) player, volume);
				}
			}

			// â”€â”€ Skill: Sanguine Surge â€” passive blood regen per tick â”€â”€
			double surgeRegen = SkillPointHelper.getSanguineSurgeRegen();
			if (surgeRegen > 0 && !volume.isFull()) {
				volume.fill(surgeRegen);
				syncVolume((ServerPlayer) player, volume);
			}

			// â”€â”€ Skill: Last Wind â€” emergency regen when blood is critically low â”€â”€
			double lastWindRegen = SkillPointHelper.getLastWindRegenPerTick();
			if (lastWindRegen > 0) {
				double threshold = volume.getMaxBloodVolume() * SkillPointHelper.getLastWindThreshold();
				if (volume.getBloodVolume() < threshold && volume.getBloodVolume() > 0) {
					volume.fill(lastWindRegen);
					syncVolume((ServerPlayer) player, volume);
				}
			}

			// â”€â”€ Bloodline: Shared Blood Pool Contribution â”€â”€
			Bloodline bloodline = volume.getBloodLine();
			if (bloodline.isValid() && HemoServerConfig.BLOODLINE_POOL_ENABLED.get()) {
				int poolInterval = HemoServerConfig.BLOODLINE_POOL_CONTRIBUTION_INTERVAL.get();
				double minThreshold = HemoServerConfig.BLOODLINE_POOL_MIN_BLOOD_THRESHOLD.get();

				if (player.tickCount % poolInterval == 0) {
					ServerLevel overworld = ((ServerLevel) player.level()).getServer().overworld();
					BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
					Bloodline globalLine = savedData.getBloodline(bloodline.getBloodlineUUID());

					if (globalLine != null) {
						// â”€â”€ Per-player trickle donation â”€â”€
						// Only trickle if enabled AND passive regen rate >= trickle rate
						// (so the player never loses net blood from trickling)
						if (volume.isTrickleEnabled()) {
							double trickleRate = volume.getTrickleRate();
							double regenRate = HemoServerConfig.BLOOD_REGEN_ENABLED.get()
									? HemoServerConfig.BLOOD_REGEN_RATE.get() : 0;
							if (regenRate >= trickleRate
									&& volume.getBloodVolume() > volume.getMaxBloodVolume() * minThreshold) {
								if (volume.drain(trickleRate)) {
									globalLine.contributeBlood((float) trickleRate);
									savedData.setDirty();
									syncVolume((ServerPlayer) player, volume);
								}
							}
						} else {
							// â”€â”€ Default server-config-driven passive contribution â”€â”€
							double contributionRate = HemoServerConfig.BLOODLINE_POOL_CONTRIBUTION_RATE.get();
							if (volume.getBloodVolume() > volume.getMaxBloodVolume() * minThreshold) {
								if (volume.drain(contributionRate)) {
									globalLine.contributeBlood((float) contributionRate);
									savedData.setDirty();
									syncVolume((ServerPlayer) player, volume);
								}
							}
						}

						// â”€â”€ Per-player auto-draw from pool â”€â”€
						// When the player's blood drops below their configured threshold,
						// automatically draw from the shared pool to top them up
						if (volume.isAutoDrawEnabled()) {
							double threshold = volume.getAutoDrawThreshold();
							double targetBlood = volume.getMaxBloodVolume() * threshold;
							if (volume.getBloodVolume() < targetBlood) {
								double deficit = targetBlood - volume.getBloodVolume();
								double maxDraw = HemoServerConfig.BLOODLINE_AUTO_DRAW_MAX_RATE.get();
								double drawAmount = Math.min(deficit, maxDraw);
								float drawn = globalLine.drawBlood((float) drawAmount);
								if (drawn > 0) {
									volume.fill(drawn);
									savedData.setDirty();
									syncVolume((ServerPlayer) player, volume);
								}
							}
						}
					}
				}
			}

			// â”€â”€ Bloodline: Nearby Member Healing â”€â”€
			if (bloodline.isValid() && HemoServerConfig.BLOODLINE_HEAL_ENABLED.get()) {
				int healInterval = HemoServerConfig.BLOODLINE_HEAL_INTERVAL.get();
				if (player.tickCount % healInterval == 0) {
					float healthThreshold = (float) (player.getMaxHealth()
							* HemoServerConfig.BLOODLINE_HEAL_HEALTH_THRESHOLD.get());
					if (player.getHealth() < healthThreshold && player.getHealth() > 0) {
						double healRange = HemoServerConfig.BLOODLINE_HEAL_RANGE.get();
						boolean hasNearbyMember = false;
						for (Player other : player.level().players()) {
							if (!other.getUUID().equals(player.getUUID())
									&& bloodline.hasMember(other.getUUID())
									&& other.distanceTo(player) <= healRange) {
								hasNearbyMember = true;
								break;
							}
						}
						if (hasNearbyMember) {
							ServerLevel overworld = ((ServerLevel) player.level()).getServer().overworld();
							BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
							float healAmount = HemoServerConfig.BLOODLINE_HEAL_AMOUNT.get().floatValue();
							float drawn = savedData.drawBlood(bloodline.getBloodlineUUID(), healAmount);
							if (drawn > 0) {
								player.heal(drawn);
							}
						}
					}
				}
			}
		});
	}

	/**
	 * When the player takes damage, drain blood proportional to the damage dealt.
	 * Wounds cause blood loss â€” this is the core cost of being reckless in combat.
	 */
	@SubscribeEvent
	public static void onPlayerDamaged(LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.BLOOD_DRAIN_ON_DAMAGE_ENABLED.get()) return;

		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (volume.isActive()) {
				// â”€â”€ Skill: Iron Will â€” reduce incoming damage when blood is critically low â”€â”€
				double ironWillThreshold = volume.getMaxBloodVolume() * SkillPointHelper.getIronWillThreshold();
				float damage = event.getNewDamage();
				if (volume.getBloodVolume() < ironWillThreshold && volume.getBloodVolume() > 0) {
					damage *= (float) SkillPointHelper.getIronWillMultiplier();
					event.setNewDamage(damage);
				}

				double drainAmount = damage * HemoServerConfig.BLOOD_DRAIN_PER_DAMAGE.get()
						* SkillPointHelper.getHemostasisMultiplier();
				volume.drain(drainAmount);
				volume.addDamage(damage);
				syncVolume((ServerPlayer) player, volume);

				// Warn the player when blood is critically low
				if (volume.getBloodVolume() < volume.getMaxBloodVolume() * 0.1 && volume.getBloodVolume() > 0) {
					player.displayClientMessage(
							Component.literal("Your blood runs thin...")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				}

				// ── One-time first sanguine formation drop ──
				// When the player first takes a meaningful hit after blood becomes active,
				// a sanguine formation crystallises from the wound and falls at their feet.
				// 6.0f = 3 hearts (Minecraft damage: 1 heart = 2 HP).
				if (!volume.isFirstFormationDropped() && damage >= 6.0f) {
					volume.setFirstFormationDropped(true);
					syncVolume((ServerPlayer) player, volume);
					ItemStack formation = new ItemStack(ItemInit.sanguine_formation.get());
					ItemEntity drop = new ItemEntity(player.level(),
							player.getX(), player.getY(), player.getZ(), formation);
					drop.setDefaultPickUpDelay();
					player.level().addFreshEntity(drop);
					player.displayClientMessage(
							Component.literal("Something crystalline falls from the wound.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
				}
			}
		});
	}

	/**
	 * When the player kills a living entity, gain blood from the slain creature.
	 * Bloodless entities (skeletons, golems, etc.) yield nothing.
	 * Bosses yield significantly more.
	 */
	@SubscribeEvent
	public static void onEntityKilledByPlayer(LivingDeathEvent event) {
		DamageSource source = event.getSource();
		if (source == null || !(source.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.BLOOD_GAIN_ON_KILL_ENABLED.get()) return;

		LivingEntity victim = event.getEntity();

		// Bloodless entities yield no blood
		if (HemoEntityPredicates.NOBLOOD.test(victim)) return;

		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (volume.isActive()) {
				double baseGain = HemoServerConfig.BLOOD_GAIN_PER_KILL.get();

				// Scale with victim max health â€” bigger creatures have more blood
				double healthScale = Math.max(1.0, victim.getMaxHealth() / 20.0);
				double gain = baseGain * healthScale;

				// Boss multiplier
				if (victim instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
						|| victim instanceof net.minecraft.world.entity.boss.wither.WitherBoss) {
					gain *= HemoServerConfig.BLOOD_GAIN_BOSS_MULTIPLIER.get();
				}

				// Skill: Feeding Frenzy â€” bonus blood from kills
				gain *= SkillPointHelper.getFeedingFrenzyMultiplier();

				volume.fill(gain);
				syncVolume((ServerPlayer) player, volume);
			}
		});
	}

	// â”€â”€â”€â”€â”€ Utility â”€â”€â”€â”€â”€

	public static void syncVolume(ServerPlayer player, IBloodVolume volume) {
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IBloodVolume volume = HemoCapabilityAccess.requireBloodVolume(player);
		syncVolume(player, volume);
		player.displayClientMessage(
				Component.literal(
						"Welcome! Current Blood Volume: " + ChatFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IBloodVolume volume = HemoCapabilityAccess.requireBloodVolume(player);

		// Re-associate player with their global bloodline from saved data
		ServerLevel overworld = player.server.overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		Bloodline globalLine = savedData.getBloodlineForPlayer(player.getUUID());
		if (globalLine != null) {
			volume.setBloodLine(globalLine);

			// Sync shared pool data to client
			PacketHandler.sendToPlayer(player, new com.vincenthuto.hemomancy.common.network.capa.PacketSyncBloodlinePool(
							globalLine.getBloodVolume(),
							globalLine.getMaxBloodVolume(),
							globalLine.getPlayerUUIDS().size()));
		} else {
			volume.setBloodLine(Bloodline.NOBLOODLINE);
		}

		syncVolume(player, volume);

		// Sync skill tree to client
		com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(player, new com.vincenthuto.hemomancy.common.network.capa.PacketSyncSkills(
						com.vincenthuto.hemomancy.common.init.SkillPointInit.serializeAll()));

		player.displayClientMessage(
				Component.literal("Welcome! Blood Active? " + ChatFormatting.LIGHT_PURPLE + volume.isActive()), false);
		player.displayClientMessage(
				Component.literal(
						"Welcome! Current Blood Volume: " + ChatFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
		Bloodline currentLine = volume.getBloodLine();
		player.displayClientMessage(
				Component.literal("Welcome! Current Bloodline: " + ChatFormatting.GOLD + currentLine.getName()),
				false);
		if (currentLine.isValid()) {
			player.displayClientMessage(
					Component.literal("Bloodline Pool: " + ChatFormatting.DARK_RED
							+ String.format("%.1f", currentLine.getBloodVolume()) + "/"
							+ String.format("%.0f", currentLine.getMaxBloodVolume()) + "ml"),
					false);
		}
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		Player playernew = event.getEntity();
		if (!playernew.level().isClientSide) {
			IBloodVolume bloodVolumeNew = HemoCapabilityAccess.requireBloodVolume(playernew);
			syncVolume((ServerPlayer) playernew, bloodVolumeNew);
		}
	}

}
