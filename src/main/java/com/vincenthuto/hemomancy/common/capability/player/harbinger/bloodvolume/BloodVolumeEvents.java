package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationDiagnosticsSync;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.effect.MnemonicCandleRules;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.mission.HarbingerArtificerAssignmentHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodFlowDiagnostics;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePool;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMaxBloodDiagnostics;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class BloodVolumeEvents {
	@SubscribeEvent
	public static void playerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide) return;

		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (!volume.isActive()) return;
			if (player instanceof ServerPlayer serverPlayer
					&& HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 1
					&& volume.getBloodVolume() >= 5000.0) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_VESSEL_FILLED);
			}
			if (player instanceof ServerPlayer serverPlayer
					&& HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2) {
				recordEnzymeMastery(serverPlayer);
			}
			if (player instanceof ServerPlayer serverPlayer && player.tickCount % 20 == 0) {
				syncReturnReadyAdvancements(serverPlayer);
			}

			// Resolve all max-blood sources before tick-based blood flow runs.
			if (player instanceof ServerPlayer serverPlayer) {
				MaxBloodLedger.apply(serverPlayer, volume);
			}

			// â”€â”€ Passive blood regen â”€â”€
			if (HemoServerConfig.BLOOD_REGEN_ENABLED.get()) {
				int interval = HemoServerConfig.BLOOD_REGEN_INTERVAL.get();
				if (player.tickCount % interval == 0) {
					double regenRate = HemoServerConfig.BLOOD_REGEN_RATE.get();
					BloodFlowLedger.applyDirectIncome((ServerPlayer) player, volume, "base_regen",
							"Base Regen", Category.BODY, regenRate, interval);
				}
			}

			// â”€â”€ Skill: Sanguine Surge â€” passive blood regen per tick â”€â”€
			double surgeRegen = SkillPointHelper.getSanguineSurgeRegen(player);
			if (surgeRegen > 0) {
				BloodFlowLedger.applyDirectIncome((ServerPlayer) player, volume, "sanguine_surge",
						"Sanguine Surge", Category.SKILL, surgeRegen, 1);
			}

			// â”€â”€ Skill: Last Wind â€” emergency regen when blood is critically low â”€â”€
			double candleRegen = MnemonicCandleRules.bonusBloodRegenPerTick(
					player.hasEffect(EffectInit.mnemonic_candle_aura));
			if (candleRegen > 0) {
				BloodFlowLedger.applyDirectIncome((ServerPlayer) player, volume, "mnemonic_candle_aura",
						"Mnemonic Candle Aura", Category.EFFECT, candleRegen, 1);
			}

			double lastWindRegen = SkillPointHelper.getLastWindRegenPerTick(player);
			if (lastWindRegen > 0) {
				double threshold = volume.getMaxBloodVolume() * SkillPointHelper.getLastWindThreshold();
				if (volume.getBloodVolume() < threshold && volume.getBloodVolume() > 0) {
					BloodFlowLedger.applyDirectIncome((ServerPlayer) player, volume, "last_wind",
							"Last Wind", Category.SKILL, lastWindRegen, 1);
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
						if (volume.isTrickleEnabled()) {
							contributeToBloodlinePool((ServerPlayer) player, volume, globalLine, savedData,
									overworld, volume.getTrickleRate(), minThreshold, poolInterval);
						} else {
							// â”€â”€ Default server-config-driven passive contribution â”€â”€
							double contributionRate = HemoServerConfig.BLOODLINE_POOL_CONTRIBUTION_RATE.get();
							contributeToBloodlinePool((ServerPlayer) player, volume, globalLine, savedData,
									overworld, contributionRate, minThreshold, poolInterval);
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
									double before = volume.getBloodVolume();
									volume.fill(drawn);
									double accepted = Math.max(0.0D, volume.getBloodVolume() - before);
									BloodFlowLedger.recordApplied((ServerPlayer) player, "bloodline_auto_draw",
											"Bloodline Auto-Draw", Category.BLOODLINE, drawAmount, accepted,
											poolInterval, false, accepted + 0.000001D < drawAmount ? "Pool limited" : "");
									savedData.setDirty();
									syncVolume((ServerPlayer) player, volume);
									syncBloodlinePool(overworld, globalLine);
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
								syncBloodlinePool(overworld, savedData.getBloodline(bloodline.getBloodlineUUID()));
							}
						}
					}
				}
			}
		});
	}

	private static void recordEnzymeMastery(ServerPlayer serverPlayer) {
		boolean hasAnyEnzyme = false;
		for (ItemStack stack : serverPlayer.getInventory().items) {
			if (isEnzyme(stack)) {
				hasAnyEnzyme = true;
			}
			if (stack.is(ItemInit.vivacious_enzyme.get())) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_VIVACIOUS);
			} else if (stack.is(ItemInit.fervent_enzyme.get())) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_FERVENT);
			} else if (stack.is(ItemInit.neurotic_enzyme.get())) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_NEUROTIC);
			} else if (stack.is(ItemInit.incandescent_enzyme.get())) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_INCANDESCENT);
			} else if (stack.is(ItemInit.ruinous_enzyme.get())) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_RUINOUS);
			} else if (stack.is(ItemInit.frigid_enzyme.get())) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_FRIGID);
			} else if (stack.is(ItemInit.ferric_enzyme.get())) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_FERRIC);
			} else if (stack.is(ItemInit.umbral_enzyme.get())) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_UMBRAL);
			}
		}
		if (HarbingerAdvancementGranter.getEnzymeMasteryCount(serverPlayer) >= 8) {
			HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
					HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_COMPLETE);
		}
		if (hasAnyEnzyme && HarbingerAdvancementGranter.isFirstSeparationStarted(serverPlayer)) {
			HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
					HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
		}
	}

	private static void syncReturnReadyAdvancements(ServerPlayer serverPlayer) {
		HarbingerArtificerAssignmentHelper.syncReadyToClaimAdvancements(serverPlayer);
		if (HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(serverPlayer)
				&& !HarbingerAdvancementGranter.isVeinMasonRewardClaimed(serverPlayer)) {
			HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
					HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY);
		}
	}

	private static boolean isEnzyme(ItemStack stack) {
		return stack.is(ItemInit.vivacious_enzyme.get())
				|| stack.is(ItemInit.fervent_enzyme.get())
				|| stack.is(ItemInit.neurotic_enzyme.get())
				|| stack.is(ItemInit.incandescent_enzyme.get())
				|| stack.is(ItemInit.ruinous_enzyme.get())
				|| stack.is(ItemInit.frigid_enzyme.get())
				|| stack.is(ItemInit.ferric_enzyme.get())
				|| stack.is(ItemInit.umbral_enzyme.get());
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
					damage *= (float) SkillPointHelper.getIronWillMultiplier(player);
					event.setNewDamage(damage);
				}

				double drainAmount = damage * HemoServerConfig.BLOOD_DRAIN_PER_DAMAGE.get()
						* SkillPointHelper.getHemostasisMultiplier(player);
				volume.drain(drainAmount);
				volume.addDamage(damage);
				syncVolume((ServerPlayer) player, volume);

				// Warn the player when blood is critically low
				if (volume.getBloodVolume() < volume.getMaxBloodVolume() * 0.1 && volume.getBloodVolume() > 0) {
					player.displayClientMessage(
							Component.literal("Your blood runs thin...")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				}

				// ── Sanguine formation drop while mastery is still rough (degree 1–3) ──
				// Blood still crystallizes unpredictably during early Harbinger ascent.
				// 8% chance per qualifying hit (≥3 hearts / 6 HP) until the player reaches degree 4.
				// 6.0f = 3 hearts (Minecraft damage: 1 heart = 2 HP).
				int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
				if (degree >= 1 && degree <= 3 && damage >= 6.0f
						&& player.level().getRandom().nextFloat() < 0.08f) {
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
				gain *= SkillPointHelper.getFeedingFrenzyMultiplier(player);

				fillPlayerThenEquippedGourd(player, volume, gain);
				syncVolume((ServerPlayer) player, volume);
			}
		});
	}

	// â”€â”€â”€â”€â”€ Utility â”€â”€â”€â”€â”€

	private static void fillPlayerThenEquippedGourd(Player player, IBloodVolume playerVolume, double gain) {
		double before = playerVolume.getBloodVolume();
		playerVolume.fill(gain);
		double accepted = Math.max(0, playerVolume.getBloodVolume() - before);
		double overflow = Math.max(0, gain - accepted);
		if (overflow > 0) {
			fillFirstEquippedGourd(player, overflow);
		}
	}

	private static void fillFirstEquippedGourd(Player player, double amount) {
		HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> fillFirstEquippedGourd(equipment, amount));
	}

	private static void fillFirstEquippedGourd(IHarbingerEquipmentItemHandler scars, double amount) {
		for (int slot = 0; slot < scars.getSlots(); slot++) {
			ItemStack stack = scars.getStackInSlot(slot);
			if (stack.getItem() instanceof BloodGourdItem gourd) {
				double siphoned = amount * gourd.getKillSiphonMultiplier();
				HemoCapabilityAccess.getBloodVolume(stack).ifPresent(gourdVolume -> gourdVolume.fill(siphoned));
				return;
			}
		}
	}

	private static void contributeToBloodlinePool(ServerPlayer player, IBloodVolume volume, Bloodline globalLine,
			BloodlineSavedData savedData, ServerLevel overworld, double requestedAmount, double minThreshold,
			int intervalTicks) {
		double protectedBlood = volume.getMaxBloodVolume() * minThreshold;
		double availableToDonate = Math.max(0.0, volume.getBloodVolume() - protectedBlood);
		double poolRoom = Math.max(0.0, globalLine.getMaxBloodVolume() - globalLine.getBloodVolume());
		double actualDonation = Math.min(requestedAmount, Math.min(availableToDonate, poolRoom));

		if (actualDonation <= 0) {
			return;
		}

		BloodFlowLedger.DrainResult result = BloodFlowLedger.applyDrain(player, volume, "bloodline_trickle",
				"Bloodline Trickle", Category.BLOODLINE, actualDonation, intervalTicks, false);
		if (result.actual() <= 0.0D) {
			return;
		}

		globalLine.contributeBlood((float) result.actual());
		savedData.setDirty();
		syncBloodlinePool(overworld, globalLine);
	}

	private static void syncBloodlinePool(ServerLevel overworld, Bloodline globalLine) {
		if (globalLine == null) {
			return;
		}

		PacketSyncBloodlinePool payload = new PacketSyncBloodlinePool(globalLine.getBloodVolume(),
				globalLine.getMaxBloodVolume(), globalLine.getPlayerUUIDS().size());
		for (var memberId : globalLine.getPlayerUUIDS()) {
			ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
			if (member != null) {
				PacketHandler.sendToPlayer(member, payload);
			}
		}
	}

	public static void syncVolume(ServerPlayer player, IBloodVolume volume) {
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
		PacketHandler.sendToPlayer(player, new PacketSyncBloodFlowDiagnostics(BloodFlowLedger.collect(player, volume)));
		PacketHandler.sendToPlayer(player, new PacketSyncMaxBloodDiagnostics(MaxBloodLedger.collect(player, volume)));
		ManipulationDiagnosticsSync.sync(player);
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
			PacketHandler.sendToPlayer(player, new PacketSyncBloodlinePool(
							globalLine.getBloodVolume(),
							globalLine.getMaxBloodVolume(),
							globalLine.getPlayerUUIDS().size()));
		} else {
			volume.setBloodLine(Bloodline.NOBLOODLINE);
		}

		syncVolume(player, volume);

		// Sync skill tree to client
		SkillPointGainEvents.syncSkills(player);
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
