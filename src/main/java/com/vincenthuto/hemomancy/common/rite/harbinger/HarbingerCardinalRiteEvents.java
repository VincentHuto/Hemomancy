package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.QliphothBloomBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.event.*;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTermination.Outcome;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncQliphothBlooms;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.*;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hutoslib.client.particle.data.EmberParticleData;
import com.vincenthuto.hutoslib.client.particle.data.DarkColorParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
	private static final BloodAccounting.Source BOUNDARY = BloodAccounting.Source.of("rite_boundary", Category.RITE, false, false);

	private static final float CASTER_BOUNDARY_DAMAGE_PER_TICK = 1.0f;
	private static final double CASTER_BOUNDARY_BLOOD_DRAIN_PER_TICK = 25.0;
	private static final float SACRIFICE_DAMAGE_PER_TICK = 0.5f;
	private static final int SACRIFICE_DAMAGE_INTERVAL = 10;
	/** Blood a sacrifice yields per point of health the rite draws from it. */
	private static final float SACRIFICE_ML_PER_HEALTH = 20.0F;
	private static final int DAEMON_UPDATE_INTERVAL = 2;
	/** How long a rite that cannot resolve waits before checking its finale again. */
	private static final int FINALE_HOLD_TICKS = 40;
	private static final int RITE_SYNC_INTERVAL = 10;
	private static final int BROKEN_ANCHOR_OUTER_BLACK = CardinalRiteLightningPalette.OUTER_BLACK;
	private static final int BROKEN_ANCHOR_INNER_PURPLE = CardinalRiteLightningPalette.INNER_PURPLE;
	/** Last anchor state seen per caster, used to announce fills, breaks and repairs. Transient. */
	private static final Map<UUID, AnchorBeatState> ANCHOR_BEAT_STATE = new java.util.HashMap<>();
	/** Cached station structure validity per caster. Transient. */
	private static final Map<UUID, Boolean> STATION_VALIDITY = new java.util.HashMap<>();


	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			PacketHandler.sendToPlayer(player, CardinalRiteSync.activeRiteSnapshot(player.serverLevel(), player));
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			PacketHandler.sendToPlayer(player, CardinalRiteSync.activeRiteSnapshot(player.serverLevel(), player));
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

			CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
			RiteKind kind = RiteKind.of(rite, recipe);
			ServerPlayer caster = sLevel.getServer().getPlayerList().getPlayer(playerUUID);
			RiteKind.Admission admission = kind.admit(sLevel, caster, rite);
			if (admission == RiteKind.Admission.WAIT) continue;
			if (admission == RiteKind.Admission.BREAK) {
				CardinalRiteTermination.end(sLevel, usable(caster), rite, Outcome.BROKEN);
				toRemove.add(playerUUID);
				continue;
			}

			if (caster == null || caster.level() != sLevel) {
				if (rite.getPhase() != CardinalRitePhase.LEGACY) {
					rite.setDisconnectTicks(rite.getDisconnectTicks() + 1);
					if (rite.getDisconnectTicks() > CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS) {
						CardinalRiteTermination.end(sLevel, usable(caster), rite, Outcome.ABANDONED);
						if (caster != null) caster.displayClientMessage(Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.your_unattended_rite_gutters_out_the").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
						toRemove.add(playerUUID);
					}
					savedData.setDirty();
				}
				continue;
			}
			rite.setDisconnectTicks(0);

			BlockPos center = rite.getCenterPos();
			int riteSize = rite.getRiteSize();
			if ((recipe == null && rite.getPhase() != CardinalRitePhase.LEGACY)
					|| rite.getRecipeId().getPath().equals("cardinal_rite/sanguine_initiation")
					|| rite.getRecipeId().getPath().equals("cardinal_rite/votary_rite")) {
				Hemomancy.LOGGER.warn("Retiring active cardinal rite {} at {} because its recipe no longer exists",
						rite.getRecipeId(), center);
				CardinalRiteTermination.end(sLevel, caster, rite, Outcome.RETIRED);
				caster.displayClientMessage(
						Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_rite_dissolves_harmlessly_because_its").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						false);
				toRemove.add(playerUUID);
				savedData.setDirty();
				continue;
			}
			boolean unstained = recipe != null && recipe.isUnstained();
			if (recipe != null && recipe.hasLayeredStation() && kind.capturesOfferingsAsItRuns()
					&& !rite.hasCapturedOfferingItinerary()) {
				CardinalRiteStationMatcher.StationMatch station =
						CardinalRiteStructure.layeredStationMatch(sLevel, rite, recipe);
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
			double footprintRadius = CardinalRiteFootprintResolver.radius(rite, recipe);
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
					CardinalRiteSync.syncActiveRites(sLevel);
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

			if (kind.isWorking()) {
				// Working rites pause while the caster is away or the floor is disturbed.
				if (outsideLeash || !stationStructureValid(sLevel, rite)) continue;
				// A working rite whose own subject is broken or changed lets go and returns its escrow.
				if (gameTime % 20 == 0 && !kind.intact(sLevel, rite)) {
					failRite(sLevel, caster, rite);
					toRemove.add(playerUUID);
					continue;
				}
			} else if (outsideLeash) {
				// === Caster boundary enforcement ===
				// Only the caster is bound; the rite does not advance while they are outside.
				if (unstained) {
					caster.displayClientMessage(Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_stillness_breaks_return_to_the").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
				} else {
					caster.hurt(caster.damageSources().generic(), CASTER_BOUNDARY_DAMAGE_PER_TICK);
					BloodAccounting.pay(caster, CASTER_BOUNDARY_BLOOD_DRAIN_PER_TICK, BOUNDARY);
					caster.displayClientMessage(
							Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_rite_binds_you_return_to").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
							true);
				}
				continue;
			}

			applyCancellationDaemonRecovery(sLevel, rite);

			RiteKind.Course course = kind.runOwnCourse(sLevel, caster, rite, recipe);
			if (course != RiteKind.Course.SHARED) {
				if (course == RiteKind.Course.ENDED) toRemove.add(playerUUID);
				else phaseChanged |= escalate(sLevel, rite);
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
			if (!kind.advance(sLevel, caster, rite, recipe, gameTime)) {
				failRite(sLevel, caster, rite);
				toRemove.add(playerUUID);
				continue;
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
				if (!kind.isWorking()) collapseInteractiveRite(sLevel, caster, rite, recipe);
				CardinalRiteTermination.end(sLevel, caster, rite, Outcome.COLLAPSED);
				toRemove.add(playerUUID);
				continue;
			}

			if (rite.isComplete()) {
				// === Final structure integrity check ===
				if (!CardinalRiteStructure.verify(sLevel, rite)) {
					if (kind.isWorking()) continue;
					failRite(sLevel, caster, rite);
					toRemove.add(playerUUID);
					continue;
				}
				if (CardinalRiteCompletion.complete(sLevel, caster, rite)) {
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
			CardinalRiteSync.syncActiveRites(sLevel);
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
			cached = CardinalRiteStructure.verify(level, rite);
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
		if (RiteKind.of(rite, recipe).holdsBeforeFinale()) {
			Component refusal = CardinalRiteCompletionEffects.refusal(new CardinalRiteCompletionEffects.Context(
					level, caster, rite, recipe, rite.getCenterPos()), CardinalRiteCompletionEffects.Stage.FINALE);
			if (refusal != null && rite.holdBeforeFinale(FINALE_HOLD_TICKS)) {
				caster.displayClientMessage(Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_rite_waits_before_its_finale",
						refusal.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)).withStyle(ChatFormatting.DARK_RED), true);
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

	/** Called by {@code TransientServerStateEvents} when a server starts and stops. */
	public static void clearSessionState() {
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
					Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_rite_has_been_broken_by").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);

			// Sync updated rite list to clients so boundary circle is removed
			CardinalRiteSync.syncActiveRites(level);
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
					HemoParticleData.bloodCell(new ParticleColor(255, 0, 0)),
					entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(),
					3, 0.1, 0.1, 0.1, 0.02);
			sLevel.sendParticles(
					HemoParticleData.serpent(new ParticleColor(200, 0, 0)),
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
				HemoEasing.lerp(sourceX, targetX, absorption),
				HemoEasing.lerp(sourceY, targetY, absorption),
				HemoEasing.lerp(sourceZ, targetZ, absorption));
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
			caster.displayClientMessage(Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.hold_the_absorption_steady_draw_the").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
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
		caster.displayClientMessage(Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_daemon_folds_into_the_living").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
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
				HemoParticleData.bloodCell(new ParticleColor(190, 0, 12)),
				x, y, z, 24, 0.30D, 0.42D, 0.30D, 0.20D);
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
				(gentle ? Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_rite_s_structure_was_disturbed") : Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_rite_structure_has_been_broken")).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
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
		caster.displayClientMessage(Component.translatable("message.hemomancy.harbinger_cardinal_rite_events.the_cardinal_boundary_collapses_into_backlash").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
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
			java.util.List<QliphothBloomData.BloomEntry> clientEntries = new ArrayList<>();
			for (QliphothBloomSavedData.BloomEntry bloom : data.getBlooms()) {
				if (bloom.dimension().equals(dimension)) {
					int pomesDropped = data.getPomesDropped(bloom);
					clientEntries.add(new QliphothBloomData.BloomEntry(
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
