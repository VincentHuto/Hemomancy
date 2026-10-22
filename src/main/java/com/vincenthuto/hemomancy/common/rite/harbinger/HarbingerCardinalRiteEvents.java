package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.particle.data.BloodCellData;
import com.vincenthuto.hemomancy.client.particle.data.SerpentParticleData;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.QliphothBloomBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.AncestralCommunionDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.event.*;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTermination.Outcome;
import com.vincenthuto.hemomancy.common.succession.SuccessionRites;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRites;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncQliphothBlooms;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.*;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedRitePreflight;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
import com.vincenthuto.hutoslib.client.particle.data.EmberParticleData;
import com.vincenthuto.hutoslib.client.particle.data.DarkColorParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side event handler for managing active cardinal rite casting.
 * Handles tick processing, particle spawning, boundary enforcement,
 * unwilling sacrifice processing, and player death during active rites.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class HarbingerCardinalRiteEvents {

	private static final float CASTER_BOUNDARY_DAMAGE_PER_TICK = 1.0f;
	private static final double CASTER_BOUNDARY_BLOOD_DRAIN_PER_TICK = 25.0;
	private static final float SACRIFICE_DAMAGE_PER_TICK = 0.5f;
	private static final int SACRIFICE_DAMAGE_INTERVAL = 10;
	/** Blood a sacrifice yields per point of health the rite draws from it. */
	private static final float SACRIFICE_ML_PER_HEALTH = 20.0F;
	private static final int DAEMON_UPDATE_INTERVAL = 2;
	/** A full anchor's orb sits half sunk in its sore, just above the floor block's top. */
	private static final double ANCHOR_ORB_Y = 1.06D;
	/** How long a rite that cannot resolve waits before checking its finale again. */
	private static final int FINALE_HOLD_TICKS = 40;
	private static final int RITE_SYNC_INTERVAL = 10;
	private static final int BROKEN_ANCHOR_OUTER_BLACK = CardinalRiteLightningPalette.OUTER_BLACK;
	private static final int BROKEN_ANCHOR_INNER_PURPLE = CardinalRiteLightningPalette.INNER_PURPLE;
	/** Last anchor state seen per caster, used to announce fills, breaks and repairs. Transient. */
	private static final Map<UUID, AnchorBeatState> ANCHOR_BEAT_STATE = new java.util.HashMap<>();
	/** Cached station structure validity per caster. Transient. */
	private static final Map<UUID, Boolean> STATION_VALIDITY = new java.util.HashMap<>();

	private static PacketSyncActiveRites activeRiteSnapshot(ServerLevel level, ServerPlayer viewer) {
		List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>();
		for (ActiveCardinalRite rite : CardinalRiteSavedData.get(level).getActiveRites().values()) {
			entries.add(toClientEntry(level, rite, mayReadPool(rite, viewer)));
		}
		return new PacketSyncActiveRites(entries);
	}

	/**
	 * Every viewer sees every rite, but only the caster and its allies see the
	 * bloodline pool the rite can draw on.
	 */
	private static void syncActiveRites(ServerLevel level) {
		List<ActiveCardinalRite> rites = new ArrayList<>(CardinalRiteSavedData.get(level).getActiveRites().values());
		List<ActiveRiteClientData.RiteEntry> shared = new ArrayList<>(rites.size());
		for (ActiveCardinalRite rite : rites) shared.add(toClientEntry(level, rite, false));
		ActiveRiteClientData.RiteEntry[] revealed = new ActiveRiteClientData.RiteEntry[rites.size()];
		PacketSyncActiveRites sharedPacket = new PacketSyncActiveRites(shared);
		for (ServerPlayer viewer : level.players()) {
			List<ActiveRiteClientData.RiteEntry> entries = null;
			for (int i = 0; i < rites.size(); i++) {
				if (!mayReadPool(rites.get(i), viewer)) continue;
				if (revealed[i] == null) revealed[i] = toClientEntry(level, rites.get(i), true);
				if (entries == null) entries = new ArrayList<>(shared);
				entries.set(i, revealed[i]);
			}
			PacketHandler.sendToPlayer(viewer, entries == null ? sharedPacket : new PacketSyncActiveRites(entries));
		}
	}

	private static boolean mayReadPool(ActiveCardinalRite rite, ServerPlayer viewer) {
		return viewer != null && (rite.getPlayerUUID().equals(viewer.getUUID())
				|| rite.getAllyRoles().containsKey(viewer.getUUID()));
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			PacketHandler.sendToPlayer(player, activeRiteSnapshot(player.serverLevel(), player));
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			PacketHandler.sendToPlayer(player, activeRiteSnapshot(player.serverLevel(), player));
		}
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;

		// Tick pending blood structure crafts (delayed block breaking)
		PendingBloodCraftManager.tick();
		BloodStructureFeedManager.tick(sLevel);
		BloodInfusionManager.tick(sLevel);

		// Periodically sync Qliphoth Bloom data to clients for tree rendering
		// (must be outside the active-rite block since blooms persist after rites end)
		// Only sync from the overworld to prevent other dimension ticks from overwriting
		// the client bloom list with an empty set.
		if (sLevel == sLevel.getServer().overworld() && sLevel.getGameTime() % 200 == 0) {
			syncQliphothBlooms(sLevel.getServer());
		}

		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);
		if (sLevel.getGameTime() % 20 == 0) {
			for (ServerPlayer player : sLevel.players()) {
				CardinalRiteSavedData.get(sLevel.getServer().overworld()).deliverRecovery(player);
				HemoCapabilityAccess.stationUpgrades(player).deliver(player);
			}
		}
		Map<UUID, ActiveCardinalRite> activeRites = savedData.getActiveRites();

		if (activeRites.isEmpty()) return;

		List<UUID> toRemove = new ArrayList<>();
		boolean phaseChanged = false;
		long gameTime = sLevel.getGameTime();

		// Iterate a snapshot: lethal backlash can end a rite (through the death event) mid-loop.
		for (Map.Entry<UUID, ActiveCardinalRite> entry : List.copyOf(activeRites.entrySet())) {
			UUID playerUUID = entry.getKey();
			ActiveCardinalRite rite = entry.getValue();
			if (rite.isEnded()) continue;

			if (SuccessionRites.is(rite) && !SuccessionRites.chunksReady(sLevel, rite)) continue;

			ServerPlayer caster = sLevel.getServer().getPlayerList().getPlayer(playerUUID);
			boolean stationRite = StationUpgradeRites.isRite(rite.getRecipeId());
			if (stationRite) {
				if (!sLevel.hasChunkAt(rite.getCenterPos()) || !sLevel.hasChunkAt(StationUpgradeRites.seat(rite))) continue;
				if (!StationUpgradeRites.focusPresent(sLevel, rite)
						|| !StationUpgradeRites.subjectPresent(sLevel, rite)) {
					CardinalRiteTermination.end(sLevel, usable(caster), rite, Outcome.BROKEN);
					toRemove.add(playerUUID);
					continue;
				}
				// A station rite waits for its caster instead of collapsing.
				if (caster == null || caster.level() != sLevel || !caster.isAlive()) continue;
			}
			boolean craftRite = WorkingCraftRites.isRite(rite);
			if (craftRite) {
				if (!sLevel.hasChunkAt(rite.getCenterPos())) continue;
				if (!StationUpgradeRites.focusPresent(sLevel, rite)) {
					CardinalRiteTermination.end(sLevel, usable(caster), rite, Outcome.BROKEN);
					toRemove.add(playerUUID);
					continue;
				}
				// Like a station upgrade, a Working craft waits for its caster.
				if (caster == null || caster.level() != sLevel || !caster.isAlive()) continue;
			}

			if (caster == null || caster.level() != sLevel) {
				if (rite.getPhase() != CardinalRitePhase.LEGACY) {
					rite.setDisconnectTicks(rite.getDisconnectTicks() + 1);
					if (rite.getDisconnectTicks() > CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS) {
						CardinalRiteTermination.end(sLevel, usable(caster), rite, Outcome.ABANDONED);
						if (caster != null) caster.displayClientMessage(Component.literal(
								"Your unattended rite gutters out. The planted staff awaits your return.")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
						toRemove.add(playerUUID);
					}
					savedData.setDirty();
				}
				continue;
			}
			rite.setDisconnectTicks(0);

			BlockPos center = rite.getCenterPos();
			int riteSize = rite.getRiteSize();
			CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
			if ((recipe == null && rite.getPhase() != CardinalRitePhase.LEGACY)
                    || rite.getRecipeId().getPath().equals("cardinal_rite/sanguine_initiation")
                    || rite.getRecipeId().getPath().equals("cardinal_rite/votary_rite")) {
				Hemomancy.LOGGER.warn("Retiring active cardinal rite {} at {} because its recipe no longer exists",
						rite.getRecipeId(), center);
				CardinalRiteTermination.end(sLevel, caster, rite, Outcome.RETIRED);
				caster.displayClientMessage(
						Component.literal("The rite dissolves harmlessly because its pattern is no longer known.")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						false);
				toRemove.add(playerUUID);
				savedData.setDirty();
				continue;
			}
			boolean unstained = recipe != null && recipe.isUnstained();
			if (recipe != null && recipe.hasLayeredStation() && !recipe.isPuppeteerTrial()
					&& !rite.hasCapturedOfferingItinerary()) {
				CardinalRiteStationMatcher.StationMatch station =
						layeredStationMatch(sLevel, rite, recipe);
				if (station != null) {
					rite.captureOfferingItinerary(station.braziers().stream()
							.map(offering -> new ActiveCardinalRite.RiteOffering(
									offering.pos(), offering.stack(), offering.consumeOnSuccess()))
							.toList());
				}
			}
			if (recipe != null && recipe.getCeremony() != null) {
				rite.setInstabilityDamagePriority(
						com.vincenthuto.hemomancy.common.rite.CardinalRiteInstabilityBoundaryRules
								.damagePriority(recipe.getCeremony().anchors()).stream()
								.mapToInt(Integer::intValue).toArray());
			}
			CardinalRiteAllyService.maintainNpcStations(sLevel, rite);
			double footprintRadius = ritualFootprintRadius(rite, recipe);
			AABB ritualBounds = new AABB(center).inflate(
					Math.max(CardinalRiteBoundaryLeashRules.ritualRadius(riteSize), footprintRadius));
			AABB casterBounds = new AABB(center).inflate(
					Math.max(CardinalRiteBoundaryLeashRules.casterLeashRadius(riteSize), footprintRadius));

			if (rite.isStaffPlanting()) {
				Vec3 motion = caster.getDeltaMovement();
				caster.setDeltaMovement(0.0D, motion.y, 0.0D);
				caster.hurtMarked = true;
				boolean impact = rite.tickStaffPlanting();
				savedData.setDirty();
				if (impact) {
					CardinalRiteBeats.sound(sLevel, center, SoundInit.RITE_STAFF_IMPACT, 1.0F, 1.0F);
					sLevel.playSound(null, center, SoundEvents.ROOTED_DIRT_HIT, SoundSource.BLOCKS, 1.0F, 0.55F);
					CardinalRiteBeats.sound(sLevel, center, SoundInit.RITE_CONSECRATE, 0.8F, 1.0F);
					CardinalRiteBeats.send(sLevel, rite, CardinalRiteEventKind.STAFF_IMPACT);
					syncActiveRites(sLevel);
				}
				if (!rite.isStaffImpactReached()) continue;
			}

			// The recall channel is checked before every pause so a caster can always draw a stalled rite back.
			boolean outsideLeash = !casterBounds.contains(caster.position());
			if (!outsideLeash && rite.tickCancellation(gameTime)) {
				tickRiteCancellation(sLevel, caster, rite);
				savedData.setDirty();
				if (rite.isCancellationComplete()) {
					completeRiteCancellation(sLevel, caster, rite);
					toRemove.add(playerUUID);
				}
				continue;
			}
			if (outsideLeash) rite.interruptCancellation();

			if (stationRite || craftRite) {
				// Working rites pause while the caster is away or the floor is disturbed.
				if (outsideLeash || !stationStructureValid(sLevel, rite)) continue;
				// A craft whose structure is broken or changed lets go and returns its escrow.
				if (craftRite && gameTime % 20 == 0 && !WorkingCraftRites.subjectIntact(sLevel, rite)) {
					failRite(sLevel, caster, rite);
					toRemove.add(playerUUID);
					continue;
				}
			} else if (outsideLeash) {
				// === Caster boundary enforcement ===
				// Only the caster is bound; the rite does not advance while they are outside.
				if (unstained) {
					caster.displayClientMessage(Component.literal("The stillness breaks. Return to the circle.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
				} else {
					caster.hurt(caster.damageSources().generic(), CASTER_BOUNDARY_DAMAGE_PER_TICK);
					var volume = HemoCapabilityAccess.getBloodVolume(caster);
					volume.drain(CASTER_BOUNDARY_BLOOD_DRAIN_PER_TICK);
					BloodVolumeEvents.syncVolume(caster, volume);
					caster.displayClientMessage(
							Component.literal("The rite binds you! Return to the circle!")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
							true);
				}
				continue;
			}

			applyCancellationDaemonRecovery(sLevel, rite);

            if (SuccessionRites.is(rite)) {
                if (!SuccessionRites.tick(sLevel, caster, rite, recipe)) {
					if (rite.isComplete()) {
						CardinalRiteTermination.end(sLevel, caster, rite, Outcome.COMPLETED,
								CardinalRiteCulmination.BINDING.id());
					} else {
						CardinalRiteTermination.end(sLevel, caster, rite, Outcome.COLLAPSED);
					}
                    toRemove.add(playerUUID);
                } else {
                    phaseChanged |= escalate(sLevel, rite);
                }
                savedData.setDirty();
                continue;
            }

			// === Sacrifice ===
			// Exsanguination drains a named creature inside the boundary into its anchors.
			if (CardinalRiteThreatRules.allowsPassiveSacrifice(rite.getRecipeId().getPath())
					&& CardinalRiteThreatRules.acceptsSacrifice(rite.getPhase())
					&& gameTime % SACRIFICE_DAMAGE_INTERVAL == 0) {
				processSacrifices(sLevel, rite, caster, ritualBounds);
			}

			// The daemon grows above the planted staff while the rite matures.
			if (!unstained && gameTime % DAEMON_UPDATE_INTERVAL == 0) {
				updateDaemon(sLevel, caster, rite);
			}

			CardinalRitePhase phaseBeforeTick = rite.getPhase();
			if (rite.getPhase() == CardinalRitePhase.LEGACY || recipe == null
					|| !recipe.hasInteractiveCeremony()) {
				rite.tick();
			} else {
				if (gameTime % 20 == 0 && !verifyRiteStructure(sLevel, rite)) {
					failRite(sLevel, caster, rite);
					toRemove.add(playerUUID);
					continue;
				}
				if (recipe.isPuppeteerTrial() && rite.getPhase() == CardinalRitePhase.PUPPET_TRIAL) {
					PuppeteerTrialRiteController.tick(sLevel, caster, rite, recipe);
				} else {
					CardinalRiteOrdealEngine.tick(sLevel, caster, rite, recipe);
				}
			}
			if (rite.getPhase() != phaseBeforeTick && recipe != null && !unstained) {
				onPhaseEntered(sLevel, caster, rite, recipe, phaseBeforeTick);
			}
			if (recipe != null && !unstained) emitAnchorBeats(sLevel, rite, recipe);
			if (recipe != null && !unstained) phaseChanged |= announceWokenRings(sLevel, rite, recipe);
			if (recipe != null && !unstained && recipe.hasInteractiveCeremony()) {
				phaseChanged |= escalate(sLevel, rite);
			}
			phaseChanged |= rite.getPhase() != phaseBeforeTick;
			rite.advanceCancellationRecovery();
			savedData.setDirty();

			if (rite.getPhase() == CardinalRitePhase.COLLAPSED) {
				if (!stationRite && !craftRite) collapseInteractiveRite(sLevel, caster, rite, recipe);
				CardinalRiteTermination.end(sLevel, caster, rite, Outcome.COLLAPSED);
				toRemove.add(playerUUID);
				continue;
			}

			if (rite.isComplete()) {
				// === Final structure integrity check ===
				if (!verifyRiteStructure(sLevel, rite)) {
					if (stationRite || craftRite) continue;
					failRite(sLevel, caster, rite);
					toRemove.add(playerUUID);
					continue;
				}
				if (completeRite(sLevel, caster, rite)) {
					if (!unstained) spawnHumanityDispersal(sLevel, caster);
					CardinalRiteTermination.end(sLevel, caster, rite, Outcome.COMPLETED,
							culminationStyle(rite, recipe));
				} else {
					CardinalRiteTermination.end(sLevel, caster, rite, Outcome.REFUSED);
				}
				toRemove.add(playerUUID);
			}
		}

		for (UUID uuid : toRemove) {
			savedData.removeRite(uuid);
			ANCHOR_BEAT_STATE.remove(uuid);
			STATION_VALIDITY.remove(uuid);
		}

		// Sync active rites to clients for boundary circle rendering
		if (gameTime % RITE_SYNC_INTERVAL == 0 || phaseChanged || !toRemove.isEmpty()) {
			syncActiveRites(sLevel);
		}
	}

	/** Raises the rite's world stage when a milestone lands, and lets nearby clients feel the step. */
	private static boolean escalate(ServerLevel level, ActiveCardinalRite rite) {
		if (!CardinalRiteEscalation.update(rite)) return false;
		CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.ESCALATION, rite.getEscalationStage(), "",
				List.of(), ItemStack.EMPTY);
		CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_ESCALATE, 1.2F, 1.0F);
		return true;
	}

	/** A caster who can receive items back, or {@code null} when they must go to the recovery queue. */
	private static ServerPlayer usable(ServerPlayer caster) {
		// An observational Chamber visit carries a temporary inventory that is discarded on return.
		return caster != null && caster.isAlive() && !ChamberVisitService.isObservational(caster) ? caster : null;
	}

	/** Station structure checks are expensive; re-verify once a second and pause on the cached answer. */
	private static boolean stationStructureValid(ServerLevel level, ActiveCardinalRite rite) {
		Boolean cached = STATION_VALIDITY.get(rite.getPlayerUUID());
		if (cached == null || level.getGameTime() % 20 == 0) {
			cached = verifyRiteStructure(level, rite);
			STATION_VALIDITY.put(rite.getPlayerUUID(), cached);
		}
		return cached;
	}

	/** Beats, sounds and the finale gate when a ceremony moves into a new phase. */
	private static void onPhaseEntered(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe, CardinalRitePhase previous) {
		CardinalRitePhase phase = rite.getPhase();
		boolean finale = phase == CardinalRitePhase.OFFERING_PROCESSION || phase == CardinalRitePhase.CULMINATION;
		boolean wasFinale = previous == CardinalRitePhase.OFFERING_PROCESSION
				|| previous == CardinalRitePhase.CULMINATION;
		if (finale && !wasFinale) enterFinale(level, caster, rite, recipe);
	}

	/**
	 * Called whenever a ceremony moves into its finale, from the tick or from the
	 * seal itself. A rite that cannot resolve is held before any offering is drawn;
	 * otherwise culmination is announced.
	 */
	public static void enterFinale(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (!StationUpgradeRites.isRite(rite.getRecipeId()) && !recipe.isPuppeteerTrial()) {
			Component refusal = CardinalRiteCompletionEffects.refusal(new CardinalRiteCompletionEffects.Context(
					level, caster, rite, recipe, rite.getCenterPos()), CardinalRiteCompletionEffects.Stage.FINALE);
			if (refusal != null && rite.holdBeforeFinale(FINALE_HOLD_TICKS)) {
				caster.displayClientMessage(Component.literal("The rite waits before its finale. ")
						.withStyle(ChatFormatting.DARK_RED)
						.append(refusal.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)), true);
				return;
			}
		}
		if (rite.getPhase() == CardinalRitePhase.CULMINATION) announceCulmination(level, rite, recipe);
	}

	/** The culmination beat and its swell, for every path into culmination. */
	public static void announceCulmination(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.CULMINATION, culminationStyle(rite, recipe));
		CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_CULMINATION, 1.0F, 1.0F);
	}

	/**
	 * Each ring that woke since the last tick tears open: its beat for the floor, sores and world, a sting heard
	 * rite-wide, and living blood bursting from each of its sores. Deeper rings land harder.
	 */
	private static boolean announceWokenRings(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		boolean announced = false;
		for (int rank = rite.nextRingToAnnounce(); rank >= 0; rank = rite.nextRingToAnnounce()) {
			announced = true;
			int rings = rite.totalAnchorRings();
			float depth = com.vincenthuto.hemomancy.common.rite.RingOpeningMath.depth(rank, rings);
			java.util.List<Vec3> points = new java.util.ArrayList<>();
			if (recipe.getCeremony() != null) {
				var anchors = recipe.getCeremony().anchors();
				for (int i = 0; i < anchors.size(); i++) {
					if (rite.anchorRank(i) == rank) {
						points.add(CardinalRiteTargetGeometry.anchorAimPoint(rite.getCenterPos(), anchors.get(i).offset()));
					}
				}
			}
			CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.RING_OPENED, rank, Integer.toString(rings),
					points, ItemStack.EMPTY);
			CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_RING_OPEN,
					com.vincenthuto.hemomancy.common.rite.RingOpeningMath.stingVolume(depth),
					com.vincenthuto.hemomancy.common.rite.RingOpeningMath.stingPitch(depth));
			var cell = com.vincenthuto.hemomancy.common.particle.HemoParticleData.bloodCell(new ParticleColor(150, 6, 18));
			int count = com.vincenthuto.hemomancy.common.rite.RingOpeningMath.burstCells(depth);
			for (Vec3 point : points) {
				level.sendParticles(cell, point.x, point.y, point.z, count, 0.35D, 0.6D, 0.35D, 0.08D);
			}
		}
		// Synced the same tick, so the sores start their tear with the beat rather than up to a sync late.
		return announced;
	}

	/**
	 * Announces anchors that filled, broke or were repaired since the last tick,
	 * whatever filled them: the caster, an Anchor ally, or the Reservoir.
	 */
	private static void emitAnchorBeats(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (recipe.getCeremony() == null) return;
		int[] blood = rite.getAnchorBloodMl();
		java.util.BitSet full = new java.util.BitSet(blood.length);
		for (int i = 0; i < blood.length; i++) {
			if (rite.isAnchorOpen(i) && blood[i] >= rite.anchorRequirementMl(i)) full.set(i);
		}
		java.util.Set<Integer> broken = rite.getBrokenInstabilityAnchors();
		AnchorBeatState previous = ANCHOR_BEAT_STATE.put(rite.getPlayerUUID(), new AnchorBeatState(full, broken));
		if (previous == null) return;
		for (int i = full.nextSetBit(0); i >= 0; i = full.nextSetBit(i + 1)) {
			if (previous.full().get(i)) continue;
			CardinalRiteBeats.anchor(level, rite, recipe, CardinalRiteEventKind.ANCHOR_FILLED, i);
		}
		for (int index : broken) {
			if (previous.broken().contains(index)) continue;
			CardinalRiteBeats.anchor(level, rite, recipe, CardinalRiteEventKind.ANCHOR_BROKEN, index);
			CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_ANCHOR_BREAK, 0.9F, 1.0F);
		}
		for (int index : previous.broken()) {
			if (broken.contains(index)) continue;
			CardinalRiteBeats.anchor(level, rite, recipe, CardinalRiteEventKind.ANCHOR_REPAIRED, index);
		}
		if (rite.getPhase() == CardinalRitePhase.INSCRIPTION
				&& com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRules.justConsecrated(previous.full().cardinality(), full.cardinality(),
						rite.openAnchorCount())) {
			CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.CONSECRATED);
			CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_CONSECRATE, 1.0F, 0.8F);
		}
	}

	private record AnchorBeatState(java.util.BitSet full, java.util.Set<Integer> broken) {
	}

	static String culminationStyle(ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (recipe == null) return CardinalRiteCulmination.FORGING.id();
		String floorStyle = null;
		if (rite.getMatchedFloorId() != null) {
			floorStyle = CardinalRiteFloorRegistry.get(rite.getMatchedFloorId())
					.map(com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition::style)
					.orElse(null);
		}
		return CardinalRiteCulmination.resolve(
				recipe.getCeremony() == null ? "" : recipe.getCeremony().culmination(),
				recipe.isRankup(), recipe.getPurpose(), !recipe.getResult().isEmpty(), floorStyle).id();
	}

	@SubscribeEvent
	public static void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
		ANCHOR_BEAT_STATE.clear();
		STATION_VALIDITY.clear();
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (player.level().isClientSide) return;

		// Saved rites live per level; the caster may have died away from their rite.
		for (ServerLevel level : player.server.getAllLevels()) {
			CardinalRiteSavedData savedData = CardinalRiteSavedData.get(level);
			ActiveCardinalRite broken = savedData.getRite(player.getUUID());
			if (broken == null) continue;
			// The staff returns to the inventory the player is about to drop, matching the old contract.
			CardinalRiteTermination.end(level, player, broken, Outcome.DIED);
			savedData.removeRite(player.getUUID());
			ANCHOR_BEAT_STATE.remove(player.getUUID());
			STATION_VALIDITY.remove(player.getUUID());
			player.displayClientMessage(
					Component.literal("The rite has been broken by your death...")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);

			// Sync updated rite list to clients so boundary circle is removed
			syncActiveRites(level);
		}
	}

	/**
	 * A named, bloodied creature inside the boundary is drained into the rite.
	 * Its blood feeds the emptiest anchor first, then is counted toward the
	 * sacrifice the rite demands before its altar can be sealed. Legacy
	 * countdown rites instead advance a tick per pulse.
	 */
	private static void processSacrifices(ServerLevel sLevel, ActiveCardinalRite rite, ServerPlayer caster,
			AABB bounds) {
		List<LivingEntity> entities = sLevel.getEntitiesOfClass(LivingEntity.class, bounds,
				entity -> entity != caster && entity.isAlive()
						&& (rite.getPhase() == CardinalRitePhase.LEGACY || entity.hasCustomName())
						&& CardinalRiteThreatRules.isEligiblePassiveSacrifice(
								entity.getPersistentData().getBoolean(CardinalRiteThreatRules.RITE_BOUND_TAG),
								entity instanceof Player,
								caster.isAlliedTo(entity),
								entity instanceof net.minecraft.world.entity.TamableAnimal tame && tame.isTame(),
								entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
										|| entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss));

		boolean fedThisTick = false;
		BlockPos center = rite.getCenterPos();
		for (LivingEntity entity : entities) {
			// Bloodless entities cannot feed the ritual
			if (HemoEntityPredicates.NOBLOOD.test(entity)) {
				continue;
			}

			float before = entity.getHealth();
			entity.hurt(caster.damageSources().playerAttack(caster), SACRIFICE_DAMAGE_PER_TICK);
			int drawnMl = Math.round(Math.max(0.0F, before - entity.getHealth()) * SACRIFICE_ML_PER_HEALTH);

			if (rite.getPhase() == CardinalRitePhase.LEGACY) {
				// The first valid sacrifice each interval grants a bonus tick to speed the rite
				if (!fedThisTick && rite.getRemainingTicks() > 1) {
					rite.tick();
					fedThisTick = true;
				}
			} else if (drawnMl > 0) {
				int remaining = drawnMl;
				int[] blood = rite.getAnchorBloodMl();
				for (int pass = 0; pass < blood.length && remaining > 0; pass++) {
					int emptiest = -1;
					for (int i = 0; i < blood.length; i++) {
						if (rite.bloodNeededForAnchor(i) <= 0) continue;
						if (emptiest < 0 || blood[i] < blood[emptiest]) emptiest = i;
					}
					if (emptiest < 0) break;
					int accepted = Math.min(remaining, rite.bloodNeededForAnchor(emptiest));
					if (!rite.fillAnchor(emptiest, accepted)) break;
					remaining -= accepted;
					blood = rite.getAnchorBloodMl();
				}
				rite.enterInscription();
				rite.addSacrificeMl(drawnMl);
			}

			// Draw blood particles from sacrifice toward the rite center
			sLevel.sendParticles(
					new BloodCellData(ParticleInit.blood_cell.get(), new ParticleColor(255, 0, 0)),
					entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(),
					3, 0.1, 0.1, 0.1, 0.02);
			sLevel.sendParticles(
					new SerpentParticleData(ParticleInit.serpent.get(), new ParticleColor(200, 0, 0)),
					center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
					1, 0.1, 0.2, 0.1, 0);
		}
	}

	/**
	 * Keeps the daemon over the planted staff once the rite has drawn enough to call it. It forms at its one size and
	 * keeps it; the blood helix around it is drawn client-side from the synced progress.
	 */
	private static void updateDaemon(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		double progress = rite.getProgress();
		double currentMaxHeight = (3.0D + rite.getRiteSize() * 0.5D) * progress;
		if (currentMaxHeight >= 0.75D
				&& rite.getPhase() != CardinalRitePhase.OFFERING_PROCESSION
				&& rite.getPhase() != CardinalRitePhase.CULMINATION) {
			updateHumanitySprite(sLevel, caster, rite, progress);
		}
	}

	private static void updateHumanitySprite(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, double riteProgress) {
		if (rite.isCancellationRecovering()) return;

		BlockPos center = rite.getCenterPos();
		double height = CardinalRiteFinaleTiming.PROCESSION_HEIGHT;
		double sourceX = center.getX() + 0.5D;
		double sourceY = CardinalRiteDaemonEmergence.daemonY(
				center.getY(), center.getY() + 1.0D + height * 0.5D);
		double sourceZ = center.getZ() + 0.5D;
		double targetX = caster.getX();
		double targetY = caster.getY() + caster.getBbHeight() * 0.55D;
		double targetZ = caster.getZ();
		double absorption = CardinalRiteHumanityGeometry.absorptionProgress(riteProgress);
		double contraction = CardinalRiteHumanityGeometry.contractionScale(riteProgress);
		HumanitySpriteEntity sprite = HumanitySpriteEntity.findBoundToRite(
				level, rite.getPlayerUUID(), center);
		if (sprite == null) {
			// Rite data may tick before the chunk's saved entities become searchable on restart.
			if (!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(center))) return;
			sprite = EntityInit.humanity_sprite.get().create(level);
			if (sprite == null) return;
			sprite.initialize(new Vec3(sourceX, sourceY, sourceZ),
					(float) (CardinalRiteFinaleTiming.daemonScale(0) * contraction));
			sprite.bindToRite(rite.getPlayerUUID());
			level.addFreshEntity(sprite);
		}
		// It forms out of the staff at its full size: the smoke rises into its shape rather than inflating.
		if (CardinalRiteDaemonEmergence.progress(sprite.tickCount) < 1.0D) {
			emitDaemonEmergence(level, center, sprite.tickCount);
		}
		sprite.setPos(
				lerp(absorption, sourceX, targetX),
				lerp(absorption, sourceY, targetY),
				lerp(absorption, sourceZ, targetZ));
		sprite.setSpriteScale((float) (CardinalRiteFinaleTiming.daemonScale(0) * contraction));
		sprite.faceDirection(targetX - sourceX, targetZ - sourceZ);
	}

	private static void emitDaemonEmergence(ServerLevel level, BlockPos center, int elapsedTicks) {
		for (int pointIndex = 0; pointIndex < CardinalRiteDaemonEmergence.SPIRAL_POINTS; pointIndex++) {
			CardinalRiteDaemonEmergence.SpiralPoint black = CardinalRiteDaemonEmergence.spiralPoint(
					center.getX() + 0.5D, center.getZ() + 0.5D, elapsedTicks, pointIndex, 0);
			CardinalRiteDaemonEmergence.SpiralPoint white = CardinalRiteDaemonEmergence.spiralPoint(
					center.getX() + 0.5D, center.getZ() + 0.5D, elapsedTicks, pointIndex, 1);
			level.sendParticles(new DarkColorParticleData(HLParticleInit.dark_glow.get(), ParticleColor.BLACK),
					black.x(), center.getY() + black.y(), black.z(), 1,
					0.01D, 0.01D, 0.01D, 0.0D);
			level.sendParticles(new EmberParticleData(new ParticleColor(240, 240, 240),
					0.8F, 0.035F, 24),
					white.x(), center.getY() + white.y(), white.z(), 1,
					0.01D, 0.01D, 0.01D, 0.0D);
		}
	}

	private static void tickRiteCancellation(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite) {
		BlockPos center = rite.getCenterPos();
		Vec3 staff = new Vec3(center.getX() + 0.5D, center.getY() + 0.95D,
				center.getZ() + 0.5D);
		HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(
				level, rite.getPlayerUUID(), center);
		if (!CardinalRiteCancellationRules.canAnimateDaemon(daemon != null)) return;
		rite.captureCancellationDaemonStart(daemon.position(), daemon.getSpriteScale());
		daemon.setPos(CardinalRiteCancellationGeometry.daemonPosition(
				rite.getCancellationDaemonStartPos(), staff, rite.getCancellationTicks()));
		daemon.setSpriteScale(CardinalRiteCancellationGeometry.daemonScale(
				rite.getCancellationDaemonStartScale(), rite.getCancellationTicks()));
		daemon.faceDirection(caster.getX() - staff.x, caster.getZ() - staff.z);

		if (rite.getCancellationTicks() == 1) {
			caster.displayClientMessage(Component.literal(
					"Hold the absorption steady. Draw the rite back into your hand.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		}
		if (rite.getCancellationTicks() % 10 == 0) {
			CardinalRiteBeats.sound(level, center, SoundInit.RITE_CANCEL_DRAW, 1.0F,
					0.95F + rite.getCancellationTicks()
							/ (float) CardinalRiteCancellationRules.TOTAL_TICKS * 0.25F);
		}
	}

	private static void applyCancellationDaemonRecovery(ServerLevel level,
			ActiveCardinalRite rite) {
		if (!rite.isCancellationRecovering()) return;

		HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(
				level, rite.getPlayerUUID(), rite.getCenterPos());
		if (daemon == null) {
			rite.resetCancellation();
			return;
		}

		int remainingTicks = rite.getCancellationRecoveryTicks();
		daemon.setPos(CardinalRiteCancellationGeometry.recoveryPosition(
				daemon.position(), rite.getCancellationRecoveryTargetPos(), remainingTicks));
		daemon.setSpriteScale(CardinalRiteCancellationGeometry.recoveryScale(
				daemon.getSpriteScale(), rite.getCancellationRecoveryTargetScale(), remainingTicks));
	}

	private static void completeRiteCancellation(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite) {
		CardinalRiteTermination.end(level, caster, rite, Outcome.CANCELLED);
		CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_CANCEL, 1.0F, 1.0F);
		level.playSound(null, caster.blockPosition(), SoundEvents.BOTTLE_FILL,
				SoundSource.PLAYERS, 0.8F, 0.75F);
		caster.displayClientMessage(Component.literal(
				"The daemon folds into the Living Staff, and the rite returns to your blood.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
	}

	private static void spawnHumanityDispersal(ServerLevel level, ServerPlayer caster) {
		double x = caster.getX();
		double y = caster.getY() + caster.getBbHeight() * 0.55D;
		double z = caster.getZ();
		level.sendParticles(
				new DarkColorParticleData(HLParticleInit.dark_glow.get(), new ParticleColor(3, 0, 2)),
				x, y, z, 48, 0.28D, 0.42D, 0.28D, 0.16D);
		level.sendParticles(
				new EmberParticleData(new ParticleColor(225, 8, 18), 0.9F, 0.055F, 52),
				x, y, z, 32, 0.25D, 0.38D, 0.25D, 0.22D);
		level.sendParticles(
				new EmberParticleData(new ParticleColor(235, 230, 225), 0.62F, 0.035F, 38),
				x, y, z, 20, 0.32D, 0.45D, 0.32D, 0.26D);
		level.sendParticles(
				new BloodCellData(ParticleInit.blood_cell.get(), new ParticleColor(190, 0, 12)),
				x, y, z, 24, 0.30D, 0.42D, 0.30D, 0.20D);
	}

	private static double lerp(double amount, double start, double end) {
		return start + (end - start) * amount;
	}

	/**
	 * Searches for a block pattern match near a center position.
	 * <p>
	 * Vanilla {@link BlockPattern#find} scans a cube of only
	 * {@code maxDim Ã— maxDim Ã— maxDim} starting positions, which is too
	 * small when the stored center sits in the middle of the structure.
	 * Depending on which rotation matches, the pattern's
	 * {@code frontTopLeft} anchor can be up to {@code (maxDim - 1)} blocks
	 * from center in <b>any</b> direction. We therefore scan a larger cube
	 * of radius {@code (maxDim - 1)} around center to guarantee coverage.
	 */
	private static BlockPattern.BlockPatternMatch findPatternNearCenter(
			BlockPattern blockPattern, ServerLevel sLevel, BlockPos center) {
		int maxDim = Math.max(Math.max(
				blockPattern.getWidth(), blockPattern.getHeight()), blockPattern.getDepth());
		int radius = maxDim - 1;
		for (BlockPos candidate : BlockPos.betweenClosed(
				center.offset(-radius, -radius, -radius),
				center.offset(radius, radius, radius))) {
			for (Direction finger : Direction.values()) {
				for (Direction thumb : Direction.values()) {
					if (thumb == finger || thumb == finger.getOpposite()) continue;
					BlockPattern.BlockPatternMatch match = blockPattern.matches(sLevel, candidate, finger, thumb);
					if (match != null) return match;
				}
			}
		}
		return null;
	}

	/**
	 * Re-validates that the multiblock structure for a cardinal rite is still intact.
	 * Returns true if the pattern still matches at the rite's center position.
	 */
	private static boolean verifyRiteStructure(ServerLevel sLevel, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
		if (recipe == null) {
			Hemomancy.LOGGER.warn("Rite verification failed: recipe {} not found", rite.getRecipeId());
			return false;
		}

		BlockPos center = rite.getCenterPos();
		if (recipe.hasLayeredStation()) {
			boolean valid = (StationUpgradeRites.isRite(rite.getRecipeId()) || rite.isWorkingCraft()
					|| rite.getOfferingVisitIndex() > 0 || rite.isPuppeteerTrialManifested()
					? layeredStructureMatch(sLevel, rite, recipe)
					: layeredStationMatch(sLevel, rite, recipe)) != null;
			if (!valid) {
				Hemomancy.LOGGER.warn("Layered Cardinal Rite station no longer matches {} at {}",
						rite.getRecipeId(), center);
			}
			return valid;
		}
		BlockPattern blockPattern = recipe.getPattern().getBlockPattern();
		BlockPattern.BlockPatternMatch match = findPatternNearCenter(blockPattern, sLevel, center);
		if (match == null) {
			Hemomancy.LOGGER.warn("Rite verification failed for {} at center {}. Dumping expected vs actual:",
					rite.getRecipeId(), center);
			var blockPairs = recipe.getPattern().getBlockPosBlockList();
			int width = blockPattern.getWidth();
			int height = blockPattern.getHeight();
			int depth = blockPattern.getDepth();
			Hemomancy.LOGGER.warn("  Pattern size: {}w x {}h x {}d, blockPairs: {}",
					width, height, depth, blockPairs.size());

			// Compute the origin of the structure relative to the stored center.
			// getBlockPosBlockList uses (charIndex, invertedRow, aisleIndex) as (X,Y,Z).
			// The center was derived from pattern index (width/2, height/2, depth/2).
			int halfW = width / 2;
			int halfH = height / 2;
			int halfD = depth / 2;

			int mismatches = 0;
			for (var pair : blockPairs) {
				Block expected = pair.getBlock();
				// Null or air expected means this was a space (wildcard) skip it
				if (expected == null || expected == Blocks.AIR) continue;
				BlockPos relPos = pair.getPos();
				BlockPos worldPos = center.offset(
						relPos.getX() - halfW,
						relPos.getY() - halfH,
						relPos.getZ() - halfD
				);
				Block actualBlock = sLevel.getBlockState(worldPos).getBlock();
				boolean mismatch = actualBlock != expected;
				if (mismatch) mismatches++;
				Hemomancy.LOGGER.warn("  {} Expected [{}] at rel {} -> world {} | Found [{}]{}",
						mismatch ? "XX" : "OK",
						expected,
						relPos, worldPos,
						actualBlock,
						mismatch ? " << MISMATCH" : "");
			}
			Hemomancy.LOGGER.warn("  Total mismatches: {} / {} non-wildcard positions", mismatches, blockPairs.size());
		}
		return match != null;
	}

	/**
	 * Fails a cardinal rite because its floor, braziers or structure were broken.
	 * Backlash follows the recipe's failure profile: a safe-retry rite only pushes
	 * the caster back, harsher profiles also wound them.
	 */
	private static void failRite(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		BlockPos center = rite.getCenterPos();
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
		String failureProfile = recipe == null || recipe.getCeremony() == null
				? null : recipe.getCeremony().failureProfile();
		boolean gentle = "safe_retry".equals(failureProfile);

		spawnBrokenAnchorDispersal(sLevel, rite, recipe);

		float damage = failureProfile == null ? 10.0F : CardinalRiteCeremonyRules.collapseDamage(failureProfile);
		if (damage > 0.0F) caster.hurt(caster.damageSources().magic(), damage);

		// Blast the caster back from the rite center
		double dx = caster.getX() - (center.getX() + 0.5);
		double dz = caster.getZ() - (center.getZ() + 0.5);
		double dist = Math.sqrt(dx * dx + dz * dz);
		if (dist < 0.1) {
			// If standing right on center, pick a random direction
			dx = caster.getRandom().nextDouble() - 0.5;
			dz = caster.getRandom().nextDouble() - 0.5;
			dist = Math.sqrt(dx * dx + dz * dz);
		}
		double knockbackStrength = gentle ? 0.9 : 2.5;
		caster.setDeltaMovement(
				(dx / dist) * knockbackStrength,
				gentle ? 0.25 : 0.5,
				(dz / dist) * knockbackStrength);
		caster.hurtMarked = true;

		CardinalRiteBeats.sound(sLevel, center, SoundInit.RITE_COLLAPSE, gentle ? 1.2F : 2.0F, 1.0F);
		if (!gentle) CardinalRiteBeats.sound(sLevel, center, SoundInit.RITE_BACKLASH, 1.5F, 1.0F);

		caster.displayClientMessage(
				Component.literal(gentle
						? "The rite's structure was disturbed. The boundary lets go of you."
						: "The rite structure has been broken! The ritual backlashes!")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		CardinalRiteTermination.end(sLevel, caster, rite, Outcome.BROKEN);
	}

	private static void spawnBrokenAnchorDispersal(ServerLevel sLevel, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (recipe == null || recipe.getCeremony() == null) return;

		Vec3 center = Vec3.atCenterOf(rite.getCenterPos());
		int anchorIndex = 0;
		for (var anchor : recipe.getCeremony().anchors()) {
			Vec3 origin = CardinalRiteTargetGeometry.anchorAimPoint(
					rite.getCenterPos(), anchor.offset()).add(0.0D, 0.08D, 0.0D);
			sLevel.sendParticles(new DarkColorParticleData(HLParticleInit.dark_glow.get(), ParticleColor.BLACK),
					origin.x, origin.y, origin.z, 24, 0.22D, 0.18D, 0.22D, 0.12D);
			sLevel.sendParticles(new EmberParticleData(ParticleColor.PURPLE, 0.92F, 0.065F, 24),
					origin.x, origin.y, origin.z, 18, 0.20D, 0.16D, 0.20D, 0.18D);

			Vec3 outward = origin.subtract(center.x, origin.y, center.z);
			if (outward.lengthSqr() < 1.0E-6D) outward = new Vec3(0.0D, 0.0D, 1.0D);
			outward = outward.normalize();
			Vec3 sideways = new Vec3(-outward.z, 0.0D, outward.x);
			for (int bolt = 0; bolt < 5; bolt++) {
				double reach = 0.45D + sLevel.random.nextDouble() * 0.55D;
				double lateral = (sLevel.random.nextDouble() - 0.5D) * 0.75D;
				double rise = (sLevel.random.nextDouble() - 0.15D) * 0.65D;
				Vec3 end = origin.add(outward.scale(reach))
						.add(sideways.scale(lateral))
						.add(0.0D, rise, 0.0D);
				long seed = sLevel.random.nextLong() ^ ((long) anchorIndex << 32) ^ bolt;
				LightningTesterSpawner.spawn(sLevel, origin, end,
						new LightningTestConfig(
								LightningTestConfig.Backend.BOLT,
								BROKEN_ANCHOR_OUTER_BLACK,
								BROKEN_ANCHOR_OUTER_BLACK,
								BROKEN_ANCHOR_INNER_PURPLE,
								8.0F, 0.0F, 0.0F, 0.0F,
								64.0F, 2.6F, 7, 5, 0.08F, 0.028F,
								true, seed, false, 24));
			}
			anchorIndex++;
		}
	}

	private static ActiveRiteClientData.RiteEntry toClientEntry(ServerLevel level, ActiveCardinalRite rite,
			boolean revealPool) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		boolean unstained = recipe != null && recipe.isUnstained();
		int totalRings = rite.getPhase() == CardinalRitePhase.LEGACY
				? Math.max(1, (rite.getRiteSize() - 1) / 2) : rite.totalAnchorRings();
		int upfront = rite.openRequirementMl();
		int sharedBlood = -1;
		ServerPlayer caster = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
		if (caster != null && revealPool) {
			Bloodline line = HemoCapabilityAccess.getBloodVolume(caster).getBloodLine();
			sharedBlood = line.isValid() ? (int) line.getBloodVolume() : -1;
		}
		String cue = "";
		if (rite.getPhase() == CardinalRitePhase.ORDEAL
				&& rite.getCurrentWave() < rite.getWaveDeck().size()) {
			cue = rite.getWaveDeck().get(rite.getCurrentWave());
		}
		var boundarySegments = recipe == null || recipe.getCeremony() == null
				? java.util.List.<CardinalRiteBoundaryProgress.Segment>of()
				: CardinalRiteBoundaryProgress.completedSegments(
						recipe.getCeremony().anchors(), rite.getAnchorBloodMl(), rite.anchorRequirements()).stream()
						.map(segment -> segment.withIntegrity(
								rite.instabilityAnchorIntegrity(segment.startAnchorIndex())))
						.toList();
		var sigilSegments = visibleSigilSegments(level, rite);
		var sanguineBlobs = visibleSanguineBlobs(level, rite, recipe);
		float footprintRadius = ritualFootprintRadius(rite, recipe);
		java.util.List<String> checklist = buildChecklist(level, rite, recipe);
		int stillIntervalTicks = recipe == null || recipe.getCeremony() == null ? 0
				: recipe.getCeremony().stillIntervalTicks();
		var atmosphere = recipe == null || recipe.getCeremony() == null
				? null : recipe.getCeremony().atmosphere();
		String fogProfile = atmosphere == null ? (unstained ? "none" : "storm") : atmosphere.fog();
		boolean fogLightning = atmosphere == null ? !unstained : atmosphere.lightning();
		boolean boundaryDome = atmosphere == null ? !unstained : atmosphere.dome();
        boolean succession = com.vincenthuto.hemomancy.common.succession.SuccessionRites.is(rite);
        int successionTicks = rite.succession().getInt("Ticks");
        if (succession) {
            cue = "hemomancy.succession.phase." + Math.min(5, successionTicks / 200);
            checklist = java.util.List.of(Component.translatable(cue).getString());
            upfront = 0;
        }
		return new ActiveRiteClientData.RiteEntry(
				rite.getCenterPos(), rite.getRiteSize(), succession ? successionTicks / 1200.0 : rite.getProgress(stillIntervalTicks),
				rite.getRecipeId(), unstained,
				succession ? "SUCCESSION" : rite.getPhase().name(), rite.getInstability(), rite.getCurrentWave(), rite.getTotalWaves(),
				rite.completedRings(), totalRings, rite.committedAnchorBloodMl(), upfront,
				rite.getCarriedIchorMl(), rite.getAllyRoles().size(), sharedBlood, cue,
				footprintRadius, checklist,
				boundarySegments, sigilSegments, sanguineBlobs,
				rite.hasEscrowedStaff() && rite.isStaffImpactReached(), rite.getPlayerUUID(),
				rite.getCancellationTicks(), rite.getStaffPlantingTicks(),
				fogProfile, fogLightning, boundaryDome, rite.getPhaseTicks()).withAnchorStates(anchorStates(rite))
				.withEscalation(unstained ? 0 : rite.getEscalationStage(), unstained ? 0 : rite.getEscalationTotal())
				.withGuidance(unstained || succession ? List.of() : CardinalRiteProblems.of(problemFacts(level, rite, recipe)),
						unstained ? List.of() : CardinalRiteInteractionHandler.sleepingRequiredSockets(level, rite, recipe))
				.withRings(rite.getOpenRings(), unstained ? new int[0] : rite.anchorRequirements())
				.withSubject(rite.isWorkingCraft() ? WorkingCraftRites.subjectPositions(rite) : List.of(),
						rite.isWorkingCraft() ? WorkingCraftRites.subjectStates(level, rite).stream()
								.mapToInt(net.minecraft.world.level.block.Block::getId).toArray() : new int[0],
						rite.getPhase() == CardinalRitePhase.SUBJECT_FEEDING ? rite.subjectFill()
								: rite.isWorkingCraft() ? 1.0F : 0.0F);
	}

	private static String sacrificeLine(ActiveCardinalRite rite) {
		return "Sacrifice: " + Math.min(rite.getSacrificeMl(), CardinalRiteThreatRules.SACRIFICE_REQUIRED_ML)
				+ "/" + CardinalRiteThreatRules.SACRIFICE_REQUIRED_ML + " mL from a named creature";
	}

	/** Each anchor's blood, flagged when instability has broken it, for the floor's sores and jets. */
	private static int[] anchorStates(ActiveCardinalRite rite) {
		int[] blood = rite.getAnchorBloodMl();
		int[] states = new int[blood.length];
		int[] required = rite.anchorRequirements();
		for (int i = 0; i < blood.length; i++) {
			states[i] = Math.clamp(blood[i], 0, required[i])
					+ (rite.isInstabilityDamagedAnchor(i) ? ActiveRiteClientData.RiteEntry.ANCHOR_DAMAGED : 0);
		}
		return states;
	}

	public static float ritualFootprintRadius(ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition floor = null;
		if (rite.getMatchedFloorId() != null) {
			var matchedFloor = CardinalRiteFloorRegistry.get(rite.getMatchedFloorId());
			if (matchedFloor.isPresent()) floor = matchedFloor.get();
		}
		return CardinalRiteFootprintResolver.radius(recipe, floor,
				(float) CardinalRiteBoundaryLeashRules.ritualRadius(rite.getRiteSize()));
	}

    private static String nextAnchorRepair(ActiveCardinalRite rite) {
        int[] blood = rite.getAnchorBloodMl();
        for (int index = 0; index < blood.length; index++) {
            int repair = rite.instabilityRepairBloodNeeded(index);
            if (repair > 0 || rite.bloodNeededForAnchor(index) > 0) {
                return "Anchor " + (index + 1) + ": repair " + repair + "ml, blood "
                        + Math.clamp(blood[index], 0, rite.anchorRequirementMl(index))
                        + "/" + rite.anchorRequirementMl(index) + "ml";
            }
        }
        return "All anchors restored";
    }

	private static CardinalRiteProblems.Facts problemFacts(ServerLevel level, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		List<String> sleeping = new ArrayList<>();
		int requiredAllies = 0;
		int availableAllies = 0;
		boolean mediumReady = true;
		if (rite.getPhase() == CardinalRitePhase.INSCRIPTION && recipe != null && recipe.getCeremony() != null) {
			for (var socket : recipe.getCeremony().supportSockets()) {
				if (socket.required() && !rite.isSigilAwakened(Hemomancy.rloc(socket.suggestedSigil()).toString())) {
					sleeping.add(CardinalRiteProblems.readable(socket.suggestedSigil()));
				}
			}
			requiredAllies = recipe.getCeremony().requiredHelpers();
			availableAllies = (int) rite.getAllyRoles().keySet().stream()
					.filter(ally -> CardinalRiteAllyService.isAvailable(level, rite, ally)).count();
			mediumReady = CardinalRiteStationMatcher.mediumMatches(level, rite.getCenterPos(), recipe);
		}
		int owed = CardinalRiteThreatRules.allowsPassiveSacrifice(rite.getRecipeId().getPath())
				? Math.max(0, CardinalRiteThreatRules.SACRIFICE_REQUIRED_ML - rite.getSacrificeMl()) : 0;
		return new CardinalRiteProblems.Facts(rite.getPhase(), sleeping, requiredAllies, availableAllies, mediumReady,
				rite.getBrokenInstabilityAnchors().size(), owed,
				rite.getPhase() == CardinalRitePhase.STILL_INTERVAL && rite.isHeldBeforeFinale(),
				rite.hungryLaterRingAnchors());
	}

	private static java.util.List<String> buildChecklist(ServerLevel level, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (rite.getCancellationTicks() > 0) {
			int remaining = CardinalRiteCancellationRules.TOTAL_TICKS - rite.getCancellationTicks();
			return java.util.List.of(
					"Absorbing the rite: " + Math.max(0, remaining + 19) / 20 + "s",
					"Keep Blood Absorption trained on the planted staff");
		}
		if (rite.getPhase() == CardinalRitePhase.CONSECRATION) {
			int missing = 0;
			for (int i = 0; i < rite.getAnchorBloodMl().length; i++) {
				if (rite.bloodNeededForAnchor(i) > 0) missing++;
			}
			if (CardinalRiteThreatRules.allowsPassiveSacrifice(rite.getRecipeId().getPath())) {
				return java.util.List.of(
						"Boundary rings: " + rite.completedRings() + "/" + rite.totalAnchorRings(),
						"Anchors remaining: " + missing,
						sacrificeLine(rite),
						"Bleed a named creature inside the circle, or project blood into the anchors");
			}
			return java.util.List.of(
					"Boundary rings: " + rite.completedRings() + "/" + rite.totalAnchorRings(),
					"Anchors remaining: " + missing,
                    nextAnchorRepair(rite),
					"Project blood into the glowing boundary anchors");
		}
		if (rite.getPhase() == CardinalRitePhase.INSCRIPTION) {
            int required = 0, optional = 0, requiredComplete = 0, optionalComplete = 0;
            if (recipe != null && recipe.getCeremony() != null) {
                for (var socket : recipe.getCeremony().supportSockets()) {
                    boolean awakened = rite.isSigilAwakened(Hemomancy.rloc(socket.suggestedSigil()).toString());
                    if (socket.required()) {
                        required++;
                        if (awakened) requiredComplete++;
                    } else {
                        optional++;
                        if (awakened) optionalComplete++;
                    }
                }
            }
            boolean mediumReady = recipe == null
                    || CardinalRiteStationMatcher.mediumMatches(level, rite.getCenterPos(), recipe);
            int availableAllies = (int) rite.getAllyRoles().keySet().stream()
                    .filter(ally -> CardinalRiteAllyService.isAvailable(level, rite, ally)).count();
            int requiredAllies = recipe == null || recipe.getCeremony() == null ? 0 : recipe.getCeremony().requiredHelpers();
            List<String> lines = new ArrayList<>(CardinalRiteChecklist.inscription(optional, optionalComplete,
                    required, requiredComplete, availableAllies, requiredAllies, mediumReady));
            if (CardinalRiteThreatRules.allowsPassiveSacrifice(rite.getRecipeId().getPath())) {
                lines.add(0, sacrificeLine(rite));
            }
            return List.copyOf(lines);
		}
		if (rite.getPhase() == CardinalRitePhase.SUBJECT_FEEDING) {
			int required = rite.craft().getInt("FeedRequired");
			return java.util.List.of(
					"Structure fed: " + (required - rite.subjectFeedNeeded()) + "/" + required + " mL",
					"Project blood into the structure beside the Focus",
					"The Working floor climbs it as it fills");
		}
		if (rite.getPhase() == CardinalRitePhase.STATION_PROJECTION) {
			int stage = rite.upgrade().getInt("Stage");
			int cost = StationUpgradeRites.bloodPerCircuit(rite.getRecipeId());
			return java.util.List.of("Station circuits: " + stage + "/" + StationUpgradeRites.circuits(rite.getRecipeId()),
					"Current circuit: " + (cost - rite.upgradeBloodNeeded()) + "/" + cost + " mL",
					"Project blood into the next station circuit");
		}
		if (rite.getPhase() == CardinalRitePhase.ORDEAL) {
			String wave = rite.getCurrentWave() < rite.getWaveDeck().size()
					? rite.getWaveDeck().get(rite.getCurrentWave()) : "";
			long dry = java.util.stream.IntStream.range(0, rite.getAnchorBloodMl().length)
					.filter(i -> rite.bloodNeededForAnchor(i) > 0).count();
			return java.util.List.of(
					"Wave " + Math.min(rite.getCurrentWave() + 1, rite.getTotalWaves())
							+ "/" + rite.getTotalWaves(),
					CardinalRiteChecklist.ordealObjective(wave),
					"Damaged anchors: " + rite.getBrokenInstabilityAnchors().size(),
					"Dry anchors: " + dry, nextAnchorRepair(rite));
		}
		if (rite.getPhase() == CardinalRitePhase.STILL_INTERVAL && rite.isHeldBeforeFinale()) {
			return java.util.List.of(
					"The rite waits before its finale",
					"Resolve what it lacks, or recall it with Blood Absorption");
		}
		if (rite.getPhase() == CardinalRitePhase.STILL_INTERVAL) {
			int duration = recipe == null || recipe.getCeremony() == null ? 0
					: recipe.getCeremony().stillIntervalTicks();
			int seconds = Math.max(0, duration - rite.getPhaseTicks() + 19) / 20;
			return java.util.List.of(
					"Still interval: " + seconds + "s",
					"Repair damaged anchors: " + rite.getBrokenInstabilityAnchors().size(),
					nextAnchorRepair(rite),
                    "Reservoir: " + rite.getReservoirBloodMl() + "ml (replaces siphoned blood only)");
		}
		if (rite.getPhase() == CardinalRitePhase.OFFERING_PROCESSION) {
			int total = rite.getOfferingItinerary().size();
			return java.util.List.of(
					"Offerings drawn: " + Math.min(rite.getOfferingVisitIndex(), total) + "/" + total,
					"The floor draws each burning offering into the daemon");
		}
		if (rite.getPhase() == CardinalRitePhase.CULMINATION) {
			return java.util.List.of(
					rite.getPhaseTicks() <= CardinalRiteFinaleTiming.GROWTH_TICKS
							? "The daemon gathers as the offerings fade"
							: "The daemon is returning to your blood",
					"No further input is required");
		}
		return java.util.List.of();
	}

	private static java.util.List<ActiveRiteClientData.SanguineBlob> visibleSanguineBlobs(
			ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		java.util.List<ActiveRiteClientData.SanguineBlob> result = new java.util.ArrayList<>();
		if (recipe != null && recipe.getCeremony() != null) {
			int[] blood = rite.getAnchorBloodMl();
			for (int i = 0; i < recipe.getCeremony().anchors().size() && i < blood.length; i++) {
				if (!rite.isAnchorOpen(i)) continue;
				boolean instabilityDamaged = rite.isInstabilityDamagedAnchor(i);
				// While filling, the floor's sore shows the blood rising; the orb seats itself once the anchor is full.
				if (!instabilityDamaged && blood[i] < rite.anchorRequirementMl(i)) continue;
				BlockPos offset = recipe.getCeremony().anchors().get(i).offset();
				double x = rite.getCenterPos().getX() + offset.getX() + 0.5D;
				// Seated in the sore's brimming pool rather than floating over the floor.
				double y = rite.getCenterPos().getY() + ANCHOR_ORB_Y;
				double z = rite.getCenterPos().getZ() + offset.getZ() + 0.5D;
				float integrity = rite.instabilityAnchorIntegrity(i);
				int color = boundaryAnchorColor(integrity);
				float radius = instabilityDamaged ? 0.19F
						: CardinalRiteAnchorVisualRules.formingBoundaryRadius(
								blood[i], rite.anchorRequirementMl(i));
				result.add(new ActiveRiteClientData.SanguineBlob(
						x, y, z, radius, color,
						blobSeed(x, y, z, i), integrity,
						ActiveRiteClientData.NodeKind.BOUNDARY_ANCHOR,
						IchorianSigilAnatomy.Role.JOINT));
			}
		}
		for (CardinalRiteInteractionHandler.SigilPlacement placement
				: CardinalRiteInteractionHandler.activeSigils(level, rite)) {
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(placement.id());
			if (sigil == null) continue;
			int completed = rite.getSigilProgress().getOrDefault(placement.progressKey(), 0);
			BlockPos base = rite.getCenterPos().offset(0, placement.y(), 0);
			for (int i = 0; i < sigil.nodes().size(); i++) {
				if (CardinalRiteAnchorVisualRules.sigilVisual(i, completed)
						!= CardinalRiteAnchorVisualRules.Visual.SANGUINE_BLOB) continue;
				IchorianSigilDefinition.Node node = sigil.nodes().get(i);
				BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level, base,
						placement.x() + (int) Math.round(node.x()),
						placement.z() + (int) Math.round(node.z()));
				double x = rite.getCenterPos().getX() + 0.5D + placement.x() + node.x();
				double y = surface.getY() + 0.17D;
				double z = rite.getCenterPos().getZ() + 0.5D + placement.z() + node.z();
				result.add(new ActiveRiteClientData.SanguineBlob(
						x, y, z, 0.16F, CardinalRiteAnchorVisualRules.sigilColor(sigil.color()),
						blobSeed(x, y, z, i), 1.0F,
						ActiveRiteClientData.NodeKind.SIGIL_NODE,
						IchorianSigilRoleResolver.forSource(sigil, i)));
			}
			int nodeBlood = rite.getSigilProgress().getOrDefault(
					"blood:" + placement.progressKey(), 0);
			if (completed < sigil.nodes().size() && nodeBlood > 0) {
				IchorianSigilDefinition.Node forming = sigil.nodes().get(completed);
				BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level, base,
						placement.x() + (int) Math.round(forming.x()),
						placement.z() + (int) Math.round(forming.z()));
				double x = rite.getCenterPos().getX() + 0.5D + placement.x() + forming.x();
				double y = surface.getY() + 0.17D;
				double z = rite.getCenterPos().getZ() + 0.5D + placement.z() + forming.z();
				result.add(new ActiveRiteClientData.SanguineBlob(
						x, y, z, CardinalRiteSigilRules.formingNodeRadius(nodeBlood),
						CardinalRiteAnchorVisualRules.sigilColor(sigil.color()),
						blobSeed(x, y, z, completed), 1.0F,
						ActiveRiteClientData.NodeKind.SIGIL_NODE,
						IchorianSigilRoleResolver.forSource(sigil, completed)));
			}
		}
		return java.util.List.copyOf(result);
	}

	private static int boundaryAnchorColor(float integrity) {
		float clamped = Math.max(0.0F, Math.min(1.0F, integrity));
		int red = Math.round(((CardinalRiteAnchorVisualRules.BOUNDARY_COLOR >> 16) & 255) * clamped);
		int green = Math.round(((CardinalRiteAnchorVisualRules.BOUNDARY_COLOR >> 8) & 255) * clamped);
		int blue = Math.max(5, Math.round((CardinalRiteAnchorVisualRules.BOUNDARY_COLOR & 255) * clamped));
		return (red << 16) | (green << 8) | blue;
	}

	private static long blobSeed(double x, double y, double z, int index) {
		long seed = Double.doubleToLongBits(x);
		seed = seed * 31L + Double.doubleToLongBits(y);
		seed = seed * 31L + Double.doubleToLongBits(z);
		return seed * 31L + index;
	}

	private static java.util.List<ActiveRiteClientData.SigilSegment> visibleSigilSegments(
			ServerLevel level, ActiveCardinalRite rite) {
		java.util.List<ActiveRiteClientData.SigilSegment> result = new java.util.ArrayList<>();
		for (CardinalRiteInteractionHandler.SigilPlacement placement
				: CardinalRiteInteractionHandler.activeSigils(level, rite)) {
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(placement.id());
			if (sigil == null) continue;
			int completed = rite.getSigilProgress().getOrDefault(placement.progressKey(), 0);
			for (CardinalRiteSigilProgress.Connection connection
					: CardinalRiteSigilProgress.completedConnections(sigil, completed)) {
				result.add(toClientSigilSegment(level, rite, placement, connection, sigil.color()));
			}
		}
		return java.util.List.copyOf(result);
	}

	private static ActiveRiteClientData.SigilSegment toClientSigilSegment(
			ServerLevel level, ActiveCardinalRite rite,
			CardinalRiteInteractionHandler.SigilPlacement placement,
			CardinalRiteSigilProgress.Connection connection, int color) {
		IchorianSigilDefinition.Node start = connection.start();
		IchorianSigilDefinition.Node end = connection.end();
		BlockPos base = rite.getCenterPos().offset(0, placement.y(), 0);
		BlockPos startSurface = CardinalRiteSigilRules.surfaceAirPosition(level, base,
				placement.x() + (int) Math.round(start.x()),
				placement.z() + (int) Math.round(start.z()));
		BlockPos endSurface = CardinalRiteSigilRules.surfaceAirPosition(level, base,
				placement.x() + (int) Math.round(end.x()),
				placement.z() + (int) Math.round(end.z()));
		return new ActiveRiteClientData.SigilSegment(
				rite.getCenterPos().getX() + 0.5D + placement.x() + start.x(),
				startSurface.getY() + 0.10D,
				rite.getCenterPos().getZ() + 0.5D + placement.z() + start.z(),
				rite.getCenterPos().getX() + 0.5D + placement.x() + end.x(),
				endSurface.getY() + 0.10D,
				rite.getCenterPos().getZ() + 0.5D + placement.z() + end.z(),
				color);
	}

	/** Backlash for a collapsed ceremony; {@link CardinalRiteTermination} releases what it held. */
	private static void collapseInteractiveRite(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		int legacyForm = recipe == null ? Math.max(0, (rite.getRiteSize() - 3) / 2)
				: CardinalRiteCeremonyRules.formIndex(recipe.getRiteType());
		String failureProfile = recipe == null || recipe.getCeremony() == null
				? null
				: recipe.getCeremony().failureProfile();
		float collapseDamage = failureProfile == null
				? CardinalRiteCeremonyRules.collapseDamage(legacyForm)
				: CardinalRiteCeremonyRules.collapseDamage(failureProfile);
		if (collapseDamage > 0.0F) {
			caster.hurt(caster.damageSources().magic(), collapseDamage);
		}
		if (recipe != null && recipe.getCeremony() != null) {
			int remaining = CardinalRiteCeremonyRules.fragileBlocksOnCollapse(failureProfile);
			for (BlockPos offset : recipe.getCeremony().fragileOffsets()) {
				if (remaining <= 0) break;
				BlockPos pos = rite.getCenterPos().offset(offset);
				BlockState state = level.getBlockState(pos);
				float hardness = state.getDestroySpeed(level, pos);
				if (state.isAir() || level.getBlockEntity(pos) != null || hardness < 0.0F || hardness > 5.0F) {
					continue;
				}
				if (level.destroyBlock(pos, true, caster)) remaining--;
			}
		}
		int sectionHits = failureProfile == null
				? (legacyForm >= 3 ? 2 : legacyForm >= 2 ? 1 : 0)
				: ("collapse".equals(failureProfile) ? 2 : "fragile_damage".equals(failureProfile) ? 1 : 0);
		float sectionDamage = failureProfile == null
				? (legacyForm >= 3 ? 15.0F : 10.0F)
				: ("collapse".equals(failureProfile) ? 15.0F : 10.0F);
		var vascular = HemoCapabilityAccess.getVascularSystem(caster);
		EnumVeinSections[] sections = EnumVeinSections.values();
		for (int i = 0; i < sectionHits; i++) {
			vascular.setVascularSectionHealth(
					sections[caster.getRandom().nextInt(sections.length)], -sectionDamage);
		}
		VascularSystemEvents.syncVascular(caster, vascular);
		CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_COLLAPSE, 1.5F, 1.0F);
		CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_BACKLASH, 1.0F, 1.1F);
		spawnBrokenAnchorDispersal(level, rite, recipe);
		caster.displayClientMessage(Component.literal("The cardinal boundary collapses into backlash.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
	}

	/**
	 * Resolves a finished rite. Effects live in {@link CardinalRiteCompletionEffects};
	 * this applies the shared steps around them: final gates, medium and structure
	 * consumption, the result item, rewards, rank advancement and the clean-rite
	 * refund. The caller ends the rite through {@link CardinalRiteTermination}.
	 *
	 * @return false when a final check refused the rite; nothing was granted
	 */
	private static boolean completeRite(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
		if (recipe == null) return false;
		if (StationUpgradeRites.isRite(rite.getRecipeId())) {
			boolean completed = StationUpgradeRites.complete(sLevel, rite);
			if (completed) CardinalRiteSavedData.get(sLevel.getServer().overworld()).deliverRecovery(caster);
			return completed;
		}
		BlockPos center = rite.getCenterPos();
		String ritePath = rite.getRecipeId().getPath();
		CardinalRiteCompletionEffects.Context context =
				new CardinalRiteCompletionEffects.Context(sLevel, caster, rite, recipe, center);

		if (recipe.isUnstained()) {
			if (!RecipeDegreeGates.playerMeets(caster, recipe)) {
				caster.displayClientMessage(
						Component.literal("The rite falls silent. It requires ")
								.withStyle(ChatFormatting.GRAY)
								.append(Component.literal(RecipeDegreeGates.requirementLabel(recipe))
										.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)),
						false);
				return false;
			}
			UnstainedRitePreflight.Result unstainedPreflight = UnstainedCardinalRiteEvents.preflight(caster, ritePath);
			if (!unstainedPreflight.success()) {
				UnstainedCardinalRiteEvents.announceFailure(caster, unstainedPreflight.failure());
				return false;
			}
		} else {
			Component refusal = CardinalRiteCompletionEffects.refusal(context,
					CardinalRiteCompletionEffects.Stage.FINALE);
			if (refusal != null) {
				caster.displayClientMessage(Component.literal("The rite falls silent. ")
						.withStyle(ChatFormatting.DARK_RED)
						.append(refusal.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)), false);
				return false;
			}
			if (WorkingCraftRites.isRite(rite)) return WorkingCraftRites.complete(sLevel, caster, rite, recipe);
		}

		// Interactive Harbinger ceremonies already paid their base cost node by
		// node. Only legacy/Unstained countdown rites retain the completion drain.
		if (rite.getPhase() == CardinalRitePhase.LEGACY) {
			var volume = HemoCapabilityAccess.getBloodVolume(caster);
			volume.drain(recipe.getBloodCost());
			BloodVolumeEvents.syncVolume(caster, volume);
		}

		// Floors are reusable. A recipe may explicitly consume only its matched upper structure.
		CardinalRiteStationMatcher.StationMatch layeredMatch = recipe.hasLayeredStation()
				? layeredStructureMatch(sLevel, rite, recipe)
				: null;
		if (recipe.isPuppeteerTrial() && !completePuppeteerTrial(sLevel, caster, rite, recipe, layeredMatch)) {
			return false;
		}
		if (!consumeRiteMedium(sLevel, caster, rite, recipe)) return false;
		if (recipe.isUnstained()
				&& !UnstainedCardinalRiteEvents.completeRite(sLevel, caster, center, ritePath)) {
			return false;
		}

		// Spawn result item
		if (!recipe.getResult().isEmpty()) {
			ItemStack resultStack = CardinalRiteCompletionEffects.shapeResult(context, recipe.getResult().copy());
			if (!resultStack.isEmpty()) {
				ItemEntity resultDrop = new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5,
						resultStack);
				if (!sLevel.addFreshEntity(resultDrop)) caster.drop(resultStack, false);
			}
		}

		if (!recipe.isUnstained() && !CardinalRiteCompletionEffects.apply(context)) return false;

		if (recipe.isUnstained()) {
			sLevel.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
		} else {
			CardinalRiteBeats.sound(sLevel, center, SoundInit.RITE_COMPLETE, 1.0F, 1.0F);
			CardinalRiteBeats.sound(sLevel, center, SoundInit.RITE_CONSECRATE, 1.0F, 0.9F);
		}

		caster.displayClientMessage(
				Component.literal("The ").append(recipe.displayName()).append(" is complete!")
						.withStyle(recipe.isUnstained() ? ChatFormatting.AQUA : ChatFormatting.GOLD,
								ChatFormatting.BOLD),
				false);

		// Award rite completion milestone (first rite + tiered)
		SkillPointGainEvents.onRiteCompleted(caster);
		LiberKnowledgeHelper.unlockForRite(caster, ritePath);

		Integer targetDegree = RecipeDegreeGates.getRankupTargetDegree(recipe);
		if (targetDegree != null && !recipe.isUnstained()
				&& com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression.advance(
						caster, targetDegree)) {
			CardinalRiteCompletionEffects.triggerSpineProgressionWhisper(sLevel, caster, targetDegree);
		}

		if (!recipe.isUnstained()) refundCleanRite(caster, rite);
		if (rite.commitCompletion()) {
			consumeMatchedStructure(sLevel, recipe, center, layeredMatch);
		}
		return true;
	}

	/**
	 * An awakened Reservoir banks blood for its rite. If the ceremony also held
	 * together cleanly (instability stayed low and no anchor was left broken), the
	 * Reservoir returns half of the anchors' blood. Without the Reservoir, rites
	 * keep their contract: no completion debit and no refund.
	 */
	private static void refundCleanRite(ServerPlayer caster, ActiveCardinalRite rite) {
		int anchors = rite.getAnchorBloodMl().length;
		if (anchors == 0 || rite.getInstability() >= CardinalRiteCeremonyRules.CLEAN_RITE_INSTABILITY
				|| !rite.getBrokenInstabilityAnchors().isEmpty()
				|| !rite.isSigilAwakened(Hemomancy.rloc("reservoir").toString())) return;
		int refund = Math.round(rite.fullRequirementMl() * CardinalRiteCeremonyRules.CLEAN_RITE_REFUND);
		if (refund <= 0) return;
		var volume = HemoCapabilityAccess.getBloodVolume(caster);
		volume.fill(refund);
		BloodVolumeEvents.syncVolume(caster, volume);
		caster.displayClientMessage(Component.literal("The rite closes cleanly. The Reservoir returns "
				+ refund + " mL of anchor blood to you.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
	}

	private static boolean consumeRiteMedium(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (!recipe.hasLayeredStation()) return true;
		if (level.getBlockEntity(rite.getCenterPos()) instanceof CardinalFocusBlockEntity focus
				&& CardinalRiteMediumRules.finish(focus, recipe.getMedium(),
						recipe.shouldConsumeMediumOnSuccess())) {
			return true;
		}
		String failure = recipe.hasMedium()
				? "The rite reaches culmination, but its required medium is no longer seated in the Cardinal Focus."
				: "The rite reaches culmination, but an unexpected medium occupies the Cardinal Focus.";
		caster.displayClientMessage(Component.literal(failure)
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
		return false;
	}

	private static boolean completePuppeteerTrial(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, CardinalRiteRecipe recipe,
			CardinalRiteStationMatcher.StationMatch layeredMatch) {
		if (!rite.isPuppeteerTrialDefeated() || layeredMatch == null
				|| !(level.getBlockEntity(rite.getCenterPos()) instanceof CardinalFocusBlockEntity focus)) {
			return false;
		}
		ItemStack seated = focus.getMediumForMatching();
		UUID crossbarId = MarionetteCrossbarItem.getCrossbarId(seated);
		if (!rite.getPlayerUUID().equals(MarionetteCrossbarItem.getBoundOwner(seated))
				|| !java.util.Objects.equals(rite.getPuppeteerTrialCrossbarId(), crossbarId)) {
			caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.crossbar_replaced")
					.withStyle(ChatFormatting.DARK_RED), false);
			return false;
		}
		var definition = PuppeteerSummonDefinitions.byName(rite.getPuppeteerTrialSummonName()).orElse(null);
		if (definition == null) return false;
		if (KnownSummonEvents.grantSummon(caster, definition)) {
			caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.complete",
					Component.translatable(definition.translationKey())).withStyle(ChatFormatting.RED), false);
		}
		return true;
	}

	private static CardinalRiteStationMatcher.StationMatch layeredStationMatch(
			ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (rite.getMatchedFloorId() != null) {
			return CardinalRiteStationMatcher.findCaptured(level, rite.getCenterPos(), recipe,
					rite.getMatchedFloorId(), rite.getFloorForwards(), rite.getFloorUp()).orElse(null);
		}
		return CardinalRiteStationMatcher.find(level, rite.getCenterPos(), recipe).orElse(null);
	}

	private static CardinalRiteStationMatcher.StationMatch layeredStructureMatch(
			ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (rite.getMatchedFloorId() == null) return layeredStationMatch(level, rite, recipe);
		return CardinalRiteStationMatcher.findCapturedStructure(level, rite.getCenterPos(), recipe,
				rite.getMatchedFloorId(), rite.getFloorForwards(), rite.getFloorUp()).orElse(null);
	}

	private static void consumeMatchedStructure(ServerLevel level, CardinalRiteRecipe recipe, BlockPos center,
			CardinalRiteStationMatcher.StationMatch layeredMatch) {
		MultiblockPattern pattern = recipe.hasLayeredStation()
				? recipe.getRequiredStructure()
				: recipe.getPattern();
		boolean consume = recipe.hasLayeredStation()
				? recipe.shouldConsumeRequiredStructure()
				: recipe.shouldBreakBlocksOnCreation();
		if (!consume || pattern == null) return;
		BlockPattern.BlockPatternMatch match = recipe.hasLayeredStation()
				? layeredMatch == null ? null : layeredMatch.structureMatch()
				: findPatternNearCenter(pattern.getBlockPattern(), level, center);
		if (match == null) return;
		String[][] patternArray = pattern.getPatternArray();
		java.util.Map<String, Block> symbols = pattern.getSymbolList();
		BlockPattern blockPattern = pattern.getBlockPattern();
		for (int z = 0; z < blockPattern.getDepth(); z++) {
			for (int y = 0; y < blockPattern.getHeight(); y++) {
				String row = patternArray[z][y];
				for (int x = 0; x < blockPattern.getWidth(); x++) {
					if (x >= row.length() || row.charAt(x) == ' ') continue;
					Block expected = symbols.get(String.valueOf(row.charAt(x)));
					if (expected == null || expected == Blocks.AIR) continue;
					BlockPos worldPos = match.getBlock(x, y, z).getPos();
					BlockState state = level.getBlockState(worldPos);
					level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), 2);
					if (!state.isAir()) level.sendParticles(HemoParticleData.bloodCell(new ParticleColor(150, 6, 16)),
							worldPos.getX() + 0.5D, worldPos.getY() + 0.5D, worldPos.getZ() + 0.5D,
							6, 0.3D, 0.3D, 0.3D, 0.03D);
				}
			}
		}
	}

	//
	// Utility Rite Completion Handlers
	//

	/**
	 * Syncs all Qliphoth Bloom locations to each connected client for rendering.
	 * Each player receives only the blooms in their current dimension, including
	 * the current pomes-dropped count so the client can render the correct growth stage.
	 */
	public static void syncQliphothBlooms(net.minecraft.server.MinecraftServer server) {
		QliphothBloomSavedData data = QliphothBloomSavedData.get(server.overworld());
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			String dimension = player.level().dimension().location().toString();
			java.util.List<com.vincenthuto.hemomancy.client.data.QliphothBloomClientData.BloomEntry> clientEntries = new ArrayList<>();
			for (QliphothBloomSavedData.BloomEntry bloom : data.getBlooms()) {
				if (bloom.dimension().equals(dimension)) {
					int pomesDropped = data.getPomesDropped(bloom);
					clientEntries.add(new com.vincenthuto.hemomancy.client.data.QliphothBloomClientData.BloomEntry(
							bloom.center(), bloom.chunkRadius(), pomesDropped,
							data.getState(bloom).ordinal()));
				}
			}
			PacketSyncQliphothBlooms packet =
					new PacketSyncQliphothBlooms(clientEntries);
			PacketHandler.sendToPlayer(player, packet);
		}
	}

	/**
	 * Public helper called by {@link QliphothBloomBlock#onRemove}
	 * when the bloom block is broken (directly or via filler destruction).
	 * Removes the SavedData entry and syncs clients.
	 */
	public static void removeBloomAt(net.minecraft.world.level.Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel sLevel)) return;
		ServerLevel overworld = sLevel.getServer().overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		data.removeBloomInChunk(pos, dimension);
		syncQliphothBlooms(sLevel.getServer());
	}


	/** Gametest and activation entry point: whether a Bloom may take root above this Focus. */
	public static boolean canPlaceQliphothBloom(ServerLevel level, ServerPlayer caster, BlockPos center) {
		Component refusal = CardinalRiteCompletionEffects.bloomRefusal(level, center);
		if (refusal != null) caster.displayClientMessage(refusal.copy()
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		return refusal == null;
	}



}
