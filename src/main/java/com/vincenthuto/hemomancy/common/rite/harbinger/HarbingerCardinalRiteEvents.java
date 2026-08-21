package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.QliphothBloomBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineDisbandHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.AncestralCommunionDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.event.BloodInfusionManager;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.event.SanguineFormationProjectionHandler;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRites;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncQliphothBlooms;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMediumRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteChecklist;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilProgress;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRoleResolver;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedRitePreflight;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.tile.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.client.particle.factory.DarkGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.data.EmberParticleData;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
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
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private static final int PARTICLE_SPAWN_INTERVAL = CardinalRitePillarTiming.SPAWN_INTERVAL_TICKS;
	private static final int RITE_SYNC_INTERVAL = 10;
	private static final int BROKEN_ANCHOR_OUTER_BLACK = 0xE806020A;
	private static final int BROKEN_ANCHOR_INNER_PURPLE = 0xFF5A167D;

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;

		// Tick pending blood structure crafts (delayed block breaking)
		PendingBloodCraftManager.tick();
		BloodStructureFeedManager.tick(sLevel);
		BloodInfusionManager.tick(sLevel);
		SanguineFormationProjectionHandler.tick(sLevel);

		// Periodically sync Qliphoth Bloom data to clients for tree rendering
		// (must be outside the active-rite block since blooms persist after rites end)
		// Only sync from the overworld to prevent other dimension ticks from overwriting
		// the client bloom list with an empty set.
		if (sLevel == sLevel.getServer().overworld() && sLevel.getGameTime() % 200 == 0) {
			syncQliphothBlooms(sLevel.getServer());
		}

		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);
		Map<UUID, ActiveCardinalRite> activeRites = savedData.getActiveRites();

		if (activeRites.isEmpty()) return;

		List<UUID> toRemove = new ArrayList<>();
		boolean phaseChanged = false;

		for (Map.Entry<UUID, ActiveCardinalRite> entry : activeRites.entrySet()) {
			UUID playerUUID = entry.getKey();
			ActiveCardinalRite rite = entry.getValue();

			ServerPlayer caster = sLevel.getServer().getPlayerList().getPlayer(playerUUID);

			if (caster == null || !caster.level().equals(sLevel)) {
				if (rite.getPhase() != CardinalRitePhase.LEGACY) {
					rite.setDisconnectTicks(rite.getDisconnectTicks() + 1);
					if (rite.getDisconnectTicks() > CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS) {
						CardinalRiteOrdealEngine.clearThreats(sLevel, rite);
						rite.markCollapsed();
					}
					savedData.setDirty();
				}
				continue;
			}
			rite.setDisconnectTicks(0);

			BlockPos center = rite.getCenterPos();
			int riteSize = rite.getRiteSize();
			CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
			if (recipe == null && rite.getPhase() != CardinalRitePhase.LEGACY) {
				Hemomancy.LOGGER.warn("Retiring active cardinal rite {} at {} because its recipe no longer exists",
						rite.getRecipeId(), center);
				CardinalRiteOrdealEngine.clearThreats(sLevel, rite);
				CardinalRiteStaffEscrow.restore(caster, rite);
				caster.displayClientMessage(
						Component.literal("The rite dissolves harmlessly because its pattern is no longer known.")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						false);
				toRemove.add(playerUUID);
				savedData.setDirty();
				continue;
			}
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
					sLevel.playSound(null, center, SoundEvents.RAVAGER_STEP,
							SoundSource.BLOCKS, 0.9F, 0.62F);
					sLevel.playSound(null, center, SoundEvents.ROOTED_DIRT_HIT,
							SoundSource.BLOCKS, 1.0F, 0.55F);
					sLevel.playSound(null, center, SoundEvents.BEACON_ACTIVATE,
							SoundSource.BLOCKS, 0.8F, 1.25F);
					List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>();
					for (ActiveCardinalRite active : activeRites.values()) {
						entries.add(toClientEntry(sLevel, active));
					}
					PacketDistributor.sendToAllPlayers(new PacketSyncActiveRites(entries));
				}
				if (!rite.isStaffImpactReached()) continue;
			}

			// === Caster boundary enforcement ===
			// Only the caster takes damage and blood drain for leaving the rite bounds
			if (!casterBounds.contains(caster.position())) {
				rite.interruptCancellation();
				caster.hurt(caster.damageSources().generic(), CASTER_BOUNDARY_DAMAGE_PER_TICK);
				HemoCapabilityAccess.getBloodVolume(caster).ifPresent(volume -> {
					volume.drain(CASTER_BOUNDARY_BLOOD_DRAIN_PER_TICK);
					BloodVolumeEvents.syncVolume(caster, volume);
				});
				caster.displayClientMessage(
						Component.literal("The rite binds you! Return to the circle!")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
						true);
				// Don't tick the rite forward while the caster is outside
				continue;
			}

			if (rite.tickCancellation(sLevel.getGameTime())) {
				tickRiteCancellation(sLevel, caster, rite);
				savedData.setDirty();
				if (rite.isCancellationComplete()) {
					completeRiteCancellation(sLevel, caster, rite);
					toRemove.add(playerUUID);
				}
				continue;
			}

			applyCancellationDaemonRecovery(sLevel, rite);

			// === Unwilling sacrifice processing ===
			// Non-caster living entities within bounds take damage and feed the ritual
			if (rite.getPhase() == CardinalRitePhase.LEGACY
					&& CardinalRiteThreatRules.allowsPassiveSacrifice(
							rite.getRecipeId().getPath())
					&& sLevel.getGameTime() % SACRIFICE_DAMAGE_INTERVAL == 0) {
				processSacrifices(sLevel, rite, caster, ritualBounds);
			}

			// === Spawn helix particles ===
			if (sLevel.getGameTime() % PARTICLE_SPAWN_INTERVAL == 0) {
				spawnHelixParticles(sLevel, caster, rite);
			}


			CardinalRitePhase phaseBeforeTick = rite.getPhase();
			if (rite.getPhase() == CardinalRitePhase.LEGACY || recipe == null
					|| !recipe.hasInteractiveCeremony()) {
				rite.tick();
			} else {
				if (sLevel.getGameTime() % 20 == 0 && !verifyRiteStructure(sLevel, rite)) {
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
			phaseChanged |= rite.getPhase() != phaseBeforeTick;
			rite.advanceCancellationRecovery();
			savedData.setDirty();

			if (rite.getPhase() == CardinalRitePhase.COLLAPSED) {
				collapseInteractiveRite(sLevel, caster, rite, recipe);
				toRemove.add(playerUUID);
				continue;
			}

			if (rite.isComplete()) {
				// === Final structure integrity check ===
				if (!verifyRiteStructure(sLevel, rite)) {
					failRite(sLevel, caster, rite);
					toRemove.add(playerUUID);
					continue;
				}
				if (completeRite(sLevel, caster, rite)) {
					spawnHumanityDispersal(sLevel, caster);
				}
				toRemove.add(playerUUID);
			}
		}

		for (UUID uuid : toRemove) {
			ActiveCardinalRite removedRite = activeRites.get(uuid);
			if (removedRite != null) {
				CardinalRiteAllyService.returnNpcAlliesToFane(sLevel, removedRite);
				discardHumanitySprites(sLevel, uuid, removedRite.getCenterPos());
			}
			savedData.removeRite(uuid);
		}

		// Sync active rites to clients for boundary circle rendering
		if (sLevel.getGameTime() % RITE_SYNC_INTERVAL == 0 || phaseChanged || !toRemove.isEmpty()) {
			List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>();
			for (ActiveCardinalRite rite : activeRites.values()) {
				entries.add(toClientEntry(sLevel, rite));
			}
			PacketDistributor.sendToAllPlayers(new PacketSyncActiveRites(entries));
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (player.level().isClientSide) return;

		ServerLevel sLevel = (ServerLevel) player.level();
		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);

		if (savedData.hasActiveRite(player.getUUID())) {
			ActiveCardinalRite broken = savedData.getRite(player.getUUID());
			if (broken != null) {
				CardinalRiteOrdealEngine.clearThreats(sLevel, broken);
				CardinalRiteAllyService.returnNpcAlliesToFane(sLevel, broken);
				discardHumanitySprites(sLevel, player.getUUID(), broken.getCenterPos());
				CardinalRiteStaffEscrow.restore(player, broken);
			}
			savedData.removeRite(player.getUUID());
			player.displayClientMessage(
					Component.literal("The rite has been broken by your death...")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);

			// Sync updated rite list to clients so boundary circle is removed
			List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>();
			for (ActiveCardinalRite rite : savedData.getActiveRites().values()) {
				entries.add(toClientEntry(sLevel, rite));
			}
			PacketDistributor.sendToAllPlayers(new PacketSyncActiveRites(entries));
		}
	}

	/**
	 * Non-caster living entities inside the rite bounds are treated as unwilling
	 * sacrifices. They take damage and their life force feeds into the ritual,
	 * reducing remaining casting time. Bloodless entities (skeletons, golems, etc.)
	 * are not valid sacrifices.
	 */
	private static void processSacrifices(ServerLevel sLevel, ActiveCardinalRite rite, ServerPlayer caster,
			AABB bounds) {
		List<LivingEntity> entities = sLevel.getEntitiesOfClass(LivingEntity.class, bounds,
				entity -> entity != caster && entity.isAlive()
						&& CardinalRiteThreatRules.isEligiblePassiveSacrifice(
								entity.getPersistentData().getBoolean(CardinalRiteThreatRules.RITE_BOUND_TAG),
								entity instanceof Player,
								caster.isAlliedTo(entity),
								entity instanceof net.minecraft.world.entity.TamableAnimal tame && tame.isTame(),
								entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
										|| entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss));

		boolean fedThisTick = false;
		for (LivingEntity entity : entities) {
			// Bloodless entities cannot feed the ritual
			if (HemoEntityPredicates.NOBLOOD.test(entity)) {
				continue;
			}

			entity.hurt(caster.damageSources().playerAttack(caster), SACRIFICE_DAMAGE_PER_TICK);

			// The first valid sacrifice each interval grants a bonus tick to speed the rite
			if (!fedThisTick && rite.getRemainingTicks() > 1) {
				rite.tick();
				fedThisTick = true;
			}

			// Draw blood particles from sacrifice toward the rite center
			BlockPos center = rite.getCenterPos();
			sLevel.sendParticles(
					BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
					entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(),
					3, 0.1, 0.1, 0.1, 0.02);
			sLevel.sendParticles(
					SerpentParticleFactory.createData(new ParticleColor(200, 0, 0)),
					center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
					1, 0.1, 0.2, 0.1, 0);
		}
	}

	private static void spawnHelixParticles(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		BlockPos center = rite.getCenterPos();
		int elapsed = rite.getTotalTicks() - rite.getRemainingTicks();
		double progress = rite.getProgress();

		// Helix rises higher as the ritual progresses
		double maxHeight = 3.0 + rite.getRiteSize() * 0.5;
		double currentMaxHeight = maxHeight * progress;
		double time = elapsed * 0.15;

		// Spawn particles along two helical strands
		int particleCount = 8;
		for (int i = 0; i < particleCount; i++) {
			double heightFraction = (double) i / particleCount;
			double h = currentMaxHeight * heightFraction;

			for (int strand = 0; strand < 2; strand++) {
				double angle = time + h * 2.0 + strand * Math.PI;
				double radius = 0.5;
				double x = center.getX() + 0.5 + Math.cos(angle) * radius;
				double z = center.getZ() + 0.5 + Math.sin(angle) * radius;
				double y = center.getY() + 1.0 + h;

				sLevel.sendParticles(
						BloodCellParticleFactory.createData(new ParticleColor(200, 0, 0)),
						x, y, z, 1, 0.02, 0.02, 0.02, 0);
			}
		}

		if (currentMaxHeight >= 0.75D
				&& rite.getPhase() != CardinalRitePhase.OFFERING_PROCESSION
				&& rite.getPhase() != CardinalRitePhase.CULMINATION) {
			updateHumanitySprite(sLevel, caster, rite,
					CardinalRiteFinaleTiming.preProcessionHeight(currentMaxHeight), progress);
		}
	}

	private static void updateHumanitySprite(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, double height, double riteProgress) {
		if (rite.isCancellationRecovering()) return;

		BlockPos center = rite.getCenterPos();
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
			sprite = EntityInit.humanity_sprite.get().create(level);
			if (sprite == null) return;
			sprite.initialize(new Vec3(sourceX, sourceY, sourceZ),
					HumanitySpriteEntity.MIN_SCALE);
			sprite.bindToRite(rite.getPlayerUUID());
			level.addFreshEntity(sprite);
		}
		double emergence = CardinalRiteDaemonEmergence.progress(sprite.tickCount);
		if (emergence < 1.0D) {
			emitDaemonEmergence(level, center, sprite.tickCount);
		}
		sprite.setPos(
				lerp(absorption, sourceX, targetX),
				lerp(absorption, sourceY, targetY),
				lerp(absorption, sourceZ, targetZ));
		sprite.setSpriteScale((float) (height
				/ CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT * contraction * emergence));
		sprite.faceDirection(targetX - sourceX, targetZ - sourceZ);
	}

	private static void emitDaemonEmergence(ServerLevel level, BlockPos center, int elapsedTicks) {
		for (int pointIndex = 0; pointIndex < CardinalRiteDaemonEmergence.SPIRAL_POINTS; pointIndex++) {
			CardinalRiteDaemonEmergence.SpiralPoint black = CardinalRiteDaemonEmergence.spiralPoint(
					center.getX() + 0.5D, center.getZ() + 0.5D, elapsedTicks, pointIndex, 0);
			CardinalRiteDaemonEmergence.SpiralPoint white = CardinalRiteDaemonEmergence.spiralPoint(
					center.getX() + 0.5D, center.getZ() + 0.5D, elapsedTicks, pointIndex, 1);
			level.sendParticles(DarkGlowParticleFactory.createData(ParticleColor.BLACK),
					black.x(), center.getY() + black.y(), black.z(), 1,
					0.01D, 0.01D, 0.01D, 0.0D);
			level.sendParticles(new EmberParticleData(new ParticleColor(240, 240, 240),
					0.8F, 0.035F, 24),
					white.x(), center.getY() + white.y(), white.z(), 1,
					0.01D, 0.01D, 0.01D, 0.0D);
		}
	}

	private static void discardHumanitySprites(ServerLevel level, UUID owner, BlockPos center) {
		for (HumanitySpriteEntity sprite : level.getEntitiesOfClass(
				HumanitySpriteEntity.class, new AABB(center).inflate(128.0D),
				entity -> entity.isBoundToRite(owner))) {
			sprite.discard();
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
		daemon.setFlying(true);

		if (rite.getCancellationTicks() == 1) {
			caster.displayClientMessage(Component.literal(
					"Hold the absorption steady. Draw the rite back into your hand.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		}
		if (rite.getCancellationTicks() % 10 == 0) {
			level.playSound(null, center, SoundEvents.SCULK_SHRIEKER_SHRIEK,
					SoundSource.BLOCKS, 0.25F,
					1.45F + rite.getCancellationTicks()
							/ (float) CardinalRiteCancellationRules.TOTAL_TICKS * 0.35F);
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
		daemon.setFlying(true);
	}

	private static void completeRiteCancellation(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite) {
		CardinalRiteOrdealEngine.clearThreats(level, rite);
		CardinalRiteStaffEscrow.restore(caster, rite);
		level.playSound(null, rite.getCenterPos(), SoundEvents.BEACON_DEACTIVATE,
				SoundSource.BLOCKS, 1.0F, 0.65F);
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
				DarkGlowParticleFactory.createData(new ParticleColor(3, 0, 2)),
				x, y, z, 48, 0.28D, 0.42D, 0.28D, 0.16D);
		level.sendParticles(
				new EmberParticleData(new ParticleColor(225, 8, 18), 0.9F, 0.055F, 52),
				x, y, z, 32, 0.25D, 0.38D, 0.25D, 0.22D);
		level.sendParticles(
				new EmberParticleData(new ParticleColor(235, 230, 225), 0.62F, 0.035F, 38),
				x, y, z, 20, 0.32D, 0.45D, 0.32D, 0.26D);
		level.sendParticles(
				BloodCellParticleFactory.createData(new ParticleColor(190, 0, 12)),
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
			boolean valid = (rite.getOfferingVisitIndex() > 0 || rite.isPuppeteerTrialManifested()
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
				// Null or air expected means this was a space (wildcard) â€” skip it
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
	 * Fails a cardinal rite because the structure was tampered with.
	 * Deals damage to the caster, blasts them back, plays a loud noise, and sends a message.
	 */
	private static void failRite(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		BlockPos center = rite.getCenterPos();
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());

		CardinalRiteOrdealEngine.clearThreats(sLevel, rite);
		spawnBrokenAnchorDispersal(sLevel, rite, recipe);

		// Deal damage to the caster
		caster.hurt(caster.damageSources().magic(), 10.0f);

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
		double knockbackStrength = 2.5;
		caster.setDeltaMovement(
				(dx / dist) * knockbackStrength,
				0.5,
				(dz / dist) * knockbackStrength);
		caster.hurtMarked = true;

		// Play loud, ominous failure sounds
		sLevel.playSound(null, center, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.0f, 0.5f);
		sLevel.playSound(null, center, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 1.5f, 0.7f);

		// Notify the caster
		caster.displayClientMessage(
				Component.literal("The rite structure has been broken! The ritual backlashes!")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		CardinalRiteStaffEscrow.restore(caster, rite);
	}

	private static void spawnBrokenAnchorDispersal(ServerLevel sLevel, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (recipe == null || recipe.getCeremony() == null) return;

		Vec3 center = Vec3.atCenterOf(rite.getCenterPos());
		int anchorIndex = 0;
		for (var anchor : recipe.getCeremony().anchors()) {
			Vec3 origin = CardinalRiteTargetGeometry.anchorAimPoint(
					rite.getCenterPos(), anchor.offset()).add(0.0D, 0.08D, 0.0D);
			sLevel.sendParticles(DarkGlowParticleFactory.createData(ParticleColor.BLACK),
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

	private static ActiveRiteClientData.RiteEntry toClientEntry(ServerLevel level, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		boolean unstained = recipe != null && recipe.isUnstained();
		int totalRings = rite.getPhase() == CardinalRitePhase.LEGACY
				? Math.max(1, (rite.getRiteSize() - 1) / 2) : Math.max(1, rite.getDegree());
		int upfront = rite.getAnchorBloodMl().length * CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
		int sharedBlood = -1;
		ServerPlayer caster = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
		if (caster != null) {
			sharedBlood = HemoCapabilityAccess.getBloodVolume(caster)
					.map(volume -> volume.getBloodLine())
					.filter(Bloodline::isValid)
					.map(line -> (int) line.getBloodVolume())
					.orElse(-1);
		}
		String cue = "";
		if (rite.getPhase() == CardinalRitePhase.ORDEAL
				&& rite.getCurrentWave() < rite.getWaveDeck().size()) {
			cue = rite.getWaveDeck().get(rite.getCurrentWave());
		}
		var boundarySegments = recipe == null || recipe.getCeremony() == null
				? java.util.List.<CardinalRiteBoundaryProgress.Segment>of()
				: CardinalRiteBoundaryProgress.completedSegments(
						recipe.getCeremony().anchors(), rite.getAnchorBloodMl()).stream()
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
		return new ActiveRiteClientData.RiteEntry(
				rite.getCenterPos(), rite.getRiteSize(), rite.getProgress(stillIntervalTicks),
				rite.getRecipeId(), unstained,
				rite.getPhase().name(), rite.getInstability(), rite.getCurrentWave(), rite.getTotalWaves(),
				rite.completedRings(), totalRings, rite.getCommittedBloodMl(), upfront,
				rite.getCarriedIchorMl(), rite.getAllyRoles().size(), sharedBlood, cue,
				footprintRadius, checklist,
				boundarySegments, sigilSegments, sanguineBlobs,
				rite.hasEscrowedStaff() && rite.isStaffImpactReached(), rite.getPlayerUUID(),
				rite.getCancellationTicks(), rite.getStaffPlantingTicks(),
				fogProfile, fogLightning, boundaryDome, rite.getPhaseTicks());
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
			for (int blood : rite.getAnchorBloodMl()) {
				if (blood < CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML) missing++;
			}
			return java.util.List.of(
					"Boundary rings: " + rite.completedRings() + "/" + Math.max(1, rite.getDegree()),
					"Anchors remaining: " + missing,
					"Project blood into the glowing boundary anchors");
		}
		if (rite.getPhase() == CardinalRitePhase.INSCRIPTION) {
			java.util.List<CardinalRiteInteractionHandler.SigilPlacement> supports =
					recipe == null ? java.util.List.of() : CardinalRiteInteractionHandler.supportSigils(recipe);
			int complete = 0;
			for (CardinalRiteInteractionHandler.SigilPlacement placement : supports) {
				IchorianSigilDefinition sigil = IchorianSigilRegistry.get(placement.id());
				if (sigil != null && (rite.isSigilAwakened(placement.progressKey())
						|| rite.isSigilComplete(placement.progressKey(), sigil.nodes().size()))) complete++;
			}
			boolean mediumReady = recipe == null
					|| CardinalRiteInteractionHandler.sealMediumReady(level, rite, recipe);
			return CardinalRiteChecklist.inscription(
					supports.size(), complete, rite.getAllyRoles().size(), mediumReady);
		}
		if (rite.getPhase() == CardinalRitePhase.ORDEAL) {
			String wave = rite.getCurrentWave() < rite.getWaveDeck().size()
					? rite.getWaveDeck().get(rite.getCurrentWave()) : "";
			long dry = java.util.Arrays.stream(rite.getAnchorBloodMl())
					.filter(blood -> blood < CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML).count();
			return java.util.List.of(
					"Wave " + Math.min(rite.getCurrentWave() + 1, rite.getTotalWaves())
							+ "/" + rite.getTotalWaves(),
					CardinalRiteChecklist.ordealObjective(wave),
					"Damaged anchors: " + rite.getBrokenInstabilityAnchors().size(),
					"Dry anchors: " + dry);
		}
		if (rite.getPhase() == CardinalRitePhase.STILL_INTERVAL) {
			int duration = recipe == null || recipe.getCeremony() == null ? 0
					: recipe.getCeremony().stillIntervalTicks();
			int seconds = Math.max(0, duration - rite.getPhaseTicks() + 19) / 20;
			return java.util.List.of(
					"Still interval: " + seconds + "s",
					"Repair damaged anchors: " + rite.getBrokenInstabilityAnchors().size(),
					"Restore every dry cardinal station");
		}
		if (rite.getPhase() == CardinalRitePhase.OFFERING_PROCESSION) {
			int total = rite.getOfferingItinerary().size();
			if (rite.isReturningFromOfferings()) {
				return java.util.List.of(
						"Offerings absorbed: " + total + "/" + total,
						"The daemon is returning to the planted staff");
			}
			return java.util.List.of(
					"Offering " + Math.min(rite.getOfferingVisitIndex() + 1, total) + "/" + total,
					"The daemon is consuming the burning offerings");
		}
		if (rite.getPhase() == CardinalRitePhase.CULMINATION) {
			return java.util.List.of(
					rite.getPhaseTicks() <= CardinalRiteFinaleTiming.GROWTH_TICKS
							? "The daemon grows as the offerings fade"
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
				boolean instabilityDamaged = rite.isInstabilityDamagedAnchor(i);
				if (!instabilityDamaged && CardinalRiteAnchorVisualRules.boundaryVisual(
						blood[i], CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML)
						!= CardinalRiteAnchorVisualRules.Visual.SANGUINE_BLOB) continue;
				BlockPos offset = recipe.getCeremony().anchors().get(i).offset();
				double x = rite.getCenterPos().getX() + offset.getX() + 0.5D;
				double y = CardinalRiteAnchorVisualRules.ritePlaneY(rite.getCenterPos().getY()) + 0.14D;
				double z = rite.getCenterPos().getZ() + offset.getZ() + 0.5D;
				float integrity = rite.instabilityAnchorIntegrity(i);
				int color = boundaryAnchorColor(integrity);
				float radius = instabilityDamaged ? 0.19F
						: CardinalRiteAnchorVisualRules.formingBoundaryRadius(
								blood[i], CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML);
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

	private static void collapseInteractiveRite(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		CardinalRiteOrdealEngine.clearThreats(level, rite);
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
		HemoCapabilityAccess.getVascularSystem(caster).ifPresent(vascular -> {
			EnumVeinSections[] sections = EnumVeinSections.values();
			for (int i = 0; i < sectionHits; i++) {
				vascular.setVascularSectionHealth(
						sections[caster.getRandom().nextInt(sections.length)], -sectionDamage);
			}
			VascularSystemEvents.syncVascular(caster, vascular);
		});
		level.playSound(null, rite.getCenterPos(), SoundEvents.GENERIC_EXPLODE.value(),
				SoundSource.BLOCKS, 1.5F, 0.55F);
		caster.displayClientMessage(Component.literal("The cardinal boundary collapses into backlash.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
		CardinalRiteStaffEscrow.restore(caster, rite);
	}

	private static final String BLOODLINE_FOUNDING_RITE = "cardinal_rite/bloodline_founding";
	private static final String BLOODLINE_RECALL_RITE = "cardinal_rite/bloodline_recall";
	private static final String SANGUINE_INITIATION_RITE = "cardinal_rite/sanguine_initiation";

	// â”€â”€ New utility rite paths â”€â”€
	private static final String SANGUINE_ATTUNEMENT_RITE = "cardinal_rite/sanguine_attunement";
	private static final String CRIMSON_BEACON_RITE = "cardinal_rite/crimson_beacon";
	private static final String VASCULAR_MENDING_RITE = "cardinal_rite/vascular_mending";
	private static final String HEMATIC_FORTIFICATION_RITE = "cardinal_rite/hematic_fortification";
	private static final String HUNGERING_EARTH_RITE = "cardinal_rite/hungering_earth";
	private static final String SCARLET_SUMMONS_RITE = "cardinal_rite/scarlet_summons";
	private static final String SANGUINE_DOMINION_RITE = "cardinal_rite/sanguine_dominion";
	private static final String ETERNAL_COVENANT_RITE = "cardinal_rite/eternal_covenant";
	private static final String ANCESTRAL_COMMUNION_RITE = "cardinal_rite/ancestral_communion";
	private static final String CHAMBER_OF_WILL_RITE = "cardinal_rite/chamber_of_will";
	private static final String EXSANGUINATION_RITE = "cardinal_rite/exsanguination";
	private static final String HEMATIC_UNBINDING_RITE = "cardinal_rite/hematic_unbinding";
	private static final String PALLID_SHADOW_RITE = "cardinal_rite/pallid_shadow";
	private static final String BLOOM_OF_QLIPHOTH_RITE = "cardinal_rite/bloom_of_qliphoth";
	private static final String SANGUINE_ECLIPSE_RITE = "cardinal_rite/sanguine_eclipse";
	private static final String FOUNDING_FANE_RITE = "cardinal_rite/founding_fane";
	private static final String SANGUINE_FERVOR_RITE = "cardinal_rite/sanguine_fervor";
	private static final String COVENANT_VIGIL_RITE = "cardinal_rite/covenant_vigil";
	private static final String ILLUMINATUS_RITE = "cardinal_rite/illuminatus_rite";

	// â”€â”€ Gourd upgrade rite paths â”€â”€
	private static final String PALLID_VESSEL_RITE = "cardinal_rite/pallid_vessel_rite";
	private static final String CRIMSON_VESSEL_RITE = "cardinal_rite/crimson_vessel_rite";
	private static final String ASHEN_VESSEL_RITE = "cardinal_rite/ashen_vessel_rite";
	private static final String HORN_OF_CULMINATION_RITE = "cardinal_rite/horn_of_culmination_rite";


	/** Radius (in blocks) for Hungering Earth terrain corruption. */
	private static final int HUNGERING_EARTH_RADIUS = 16;
	/** Chunk radius for Sanguine Dominion blood domain. */
	private static final int DOMINION_CHUNK_RADIUS = 3;
	/** Chunk radius for Qliphoth Bloom effect zone. */
	private static final int QLIPHOTH_BLOOM_CHUNK_RADIUS = 3;
	/** Chunk radius for the Sanguine Fervor spawn-boost zone. */
	private static final int SANGUINE_FERVOR_CHUNK_RADIUS = 3;
	/** Chunk radius for the Crimson Lodge buff zone. */
	private static final int CRIMSON_LODGE_CHUNK_RADIUS = 5;
	/** Duration in ticks for the Sanguine Fervor spawn-boost effect (5 minutes). */
	private static final long SANGUINE_FERVOR_DURATION_TICKS = 6000L;
	/** Blood cost per member for Scarlet Summons (from bloodline pool). */
	private static final float SUMMONS_COST_PER_MEMBER = 200f;

	private static final java.util.Map<String, Integer> DEGREE_RITE_PATHS = new java.util.HashMap<>();
	private static final String APOTHEOS_RITE_PATH = "cardinal_rite/apotheos_rite";

	static {
		DEGREE_RITE_PATHS.put("cardinal_rite/sanguine_initiation", 1); // Neophyte of the Crimson Veil
		DEGREE_RITE_PATHS.put("cardinal_rite/votary_rite", 2);          // Votary of the Hematic Covenant
		DEGREE_RITE_PATHS.put("cardinal_rite/initiate_rite", 3);        // Initiate of the Incarnadine Fane
		DEGREE_RITE_PATHS.put("cardinal_rite/sanguine_brotherhood", 4); // Adept of the Sanguine Brotherhood
		DEGREE_RITE_PATHS.put("cardinal_rite/illuminatus_rite", 5);     // Illuminatus of the Crimson Lodge
		DEGREE_RITE_PATHS.put("cardinal_rite/sanctified_rite", 6);      // Sanctified of the Bloodline Covenant
		DEGREE_RITE_PATHS.put("cardinal_rite/archon_rite", 7);          // Archon of the Hematic Order
		DEGREE_RITE_PATHS.put(APOTHEOS_RITE_PATH, 8);                   // Apotheos of the Hematic Order
	}

	private static boolean completeRite(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
		if (recipe == null) return false;

		if (!RecipeDegreeGates.playerMeets(caster, recipe)) {
			String requirement = RecipeDegreeGates.requirementLabel(recipe);
			caster.displayClientMessage(
					Component.literal("The rite falls silent. It requires ")
							.withStyle(recipe.isUnstained() ? ChatFormatting.GRAY : ChatFormatting.DARK_RED)
							.append(Component.literal(requirement)
									.withStyle(recipe.isUnstained() ? ChatFormatting.AQUA : ChatFormatting.GOLD, ChatFormatting.BOLD)),
					false);
			return false;
		}

		UnstainedRitePreflight.Result unstainedPreflight = UnstainedCardinalRiteEvents.preflight(
				caster, rite.getRecipeId().getPath());
		if (recipe.isUnstained() && !unstainedPreflight.success()) {
			UnstainedCardinalRiteEvents.announceFailure(caster, unstainedPreflight.failure());
			CardinalRiteStaffEscrow.restore(caster, rite);
			return false;
		}

		if (isApotheosRite(recipe.getId()) && !hasQliphothCommunion(caster)) {
			caster.displayClientMessage(
					Component.literal("The Eighth Degree remains sealed. Consume all nine Qliphoth husks from a single bloom.")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
					false);
			return false;
		}

		if (isApotheosRite(recipe.getId()) && HemoCapabilityAccess.getInitiatoryDegree(caster)
				.map(degree -> degree.getArchonPath() != EnumArchonPath.APOTHEOS_PENDING)
				.orElse(true)) {
			caster.displayClientMessage(Component.literal(
					"Apotheosis has not been chosen. Witness the first projection and answer the truth it leaves behind.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
			return false;
		}

		// Qliphoth Pome Corruption: at 9 pomes Apotheosis is the only remaining Cardinal Rite.
		// Silent refusal is performed directly at the owned bloom with the Living Arsenal.
		int pomesConsumed = HemoCapabilityAccess.getInitiatoryDegree(caster)
				.map(d -> d.getTotalPomesConsumed())
				.orElse(0);
		if (pomesConsumed >= 9 && !isApotheosRite(recipe.getId())) {
			caster.displayClientMessage(
					Component.literal("The void has claimed your will \u2014 only one path remains, and one way back.")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
					false);
			return false;
		}

		// Interactive Harbinger ceremonies already paid their base cost node by
		// node. Only legacy/Unstained countdown rites retain the completion drain.
		if (rite.getPhase() == CardinalRitePhase.LEGACY) {
			HemoCapabilityAccess.getBloodVolume(caster).ifPresent(volume -> {
				volume.drain(recipe.getBloodCost());
				BloodVolumeEvents.syncVolume(caster, volume);
			});
		}

		// Floors are reusable. A recipe may explicitly consume only its matched upper structure.
		BlockPos center = rite.getCenterPos();
		CardinalRiteStationMatcher.StationMatch layeredMatch = recipe.hasLayeredStation()
				? layeredStructureMatch(sLevel, rite, recipe)
				: null;
		if (recipe.isPuppeteerTrial() && !completePuppeteerTrial(sLevel, caster, rite, recipe, layeredMatch)) {
			return false;
		}
		if (!consumeRiteMedium(sLevel, caster, rite, recipe)) return false;
		String ritePath = rite.getRecipeId().getPath();
		if (recipe.isUnstained()
				&& !UnstainedCardinalRiteEvents.completeRite(sLevel, caster, center, ritePath)) {
			return false;
		}

		// Spawn result item
		if (!recipe.getResult().isEmpty()) {
			ItemStack resultStack = recipe.getResult().copy();

			// === Gourd upgrade rites: consume the prerequisite gourd from player ===
			if (PALLID_VESSEL_RITE.equals(ritePath)) {
				if (!consumeGourdPrerequisite(caster, ItemInit.dried_gourd.get())) {
					caster.displayClientMessage(
							Component.literal("You carry no dried gourd to consecrate. The rite yields nothing.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
					resultStack = ItemStack.EMPTY;
				} else {
					caster.displayClientMessage(
							Component.literal("The dried gourd awakens, its pallid shell now a vessel for living blood.")
									.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
							false);
				}
			}

			if (CRIMSON_VESSEL_RITE.equals(ritePath)) {
				if (!consumeGourdPrerequisite(caster, ItemInit.blood_gourd_white.get())) {
					caster.displayClientMessage(
							Component.literal("You carry no pallid vessel to steep. The rite yields nothing.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
					resultStack = ItemStack.EMPTY;
				} else {
					caster.displayClientMessage(
							Component.literal("The pallid vessel flushes crimson â€” reborn in the deepest scarlet.")
									.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
							false);
				}
			}

			if (ASHEN_VESSEL_RITE.equals(ritePath)) {
				if (!consumeGourdPrerequisite(caster, ItemInit.blood_gourd_red.get())) {
					caster.displayClientMessage(
							Component.literal("You carry no crimson vessel to temper. The rite yields nothing.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
					resultStack = ItemStack.EMPTY;
				} else {
					caster.displayClientMessage(
							Component.literal("Through fire and ash the vessel is reborn â€” blackened, hardened, and hungry.")
									.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
							false);
				}
			}

			if (HORN_OF_CULMINATION_RITE.equals(ritePath)) {
				if (!consumeGourdPrerequisite(caster, ItemInit.blood_gourd_black.get())) {
					caster.displayClientMessage(
							Component.literal("You carry no ashen vessel to transcend. The rite yields nothing.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
					resultStack = ItemStack.EMPTY;
				} else {
					caster.displayClientMessage(
							Component.literal("The final vessel transcends flesh and gourd alike â€” the Curved Horn is born.")
									.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
							false);
				}
			}

			// Bloodline founding rite: pre-sign the ledger with the caster's new bloodline
			if (BLOODLINE_FOUNDING_RITE.equals(ritePath) && resultStack.getItem() instanceof UnsignedLedgerItem) {
				presignBloodlineLedger(sLevel, caster, resultStack);
				HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_BLOOD_IS_BOUND);
			}

			// Bloodline recall rite: re-issue a ledger from the caster's existing bloodline
			if (BLOODLINE_RECALL_RITE.equals(ritePath) && resultStack.getItem() instanceof UnsignedLedgerItem) {
				if (!recallBloodlineLedger(sLevel, caster, resultStack)) {
					// Caster has no bloodline â€” the rite still completes but the ledger stays unsigned
					caster.displayClientMessage(
							Component.literal("The blood remembers nothing... You have no bloodline to recall.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
				}
			}

			// Exsanguination rite: verify a named sacrifice was killed during the rite
			if (EXSANGUINATION_RITE.equals(ritePath)) {
				// The sacrifice processing in the tick loop already damages entities.
				// The quintessence result item is always produced â€” the rite IS the sacrifice.
				caster.displayClientMessage(
						Component.literal("The lifeblood crystallizes... Sanguine Quintessence is born.")
								.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
						false);
			}

			if (!resultStack.isEmpty()) {
				sLevel.addFreshEntity(new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5,
						resultStack));
			}
		}

		// === Utility rite effects (no result item needed) ===

		// Rite of Sanguine Attunement: reset all blood tendency scores to zero
		if (SANGUINE_ATTUNEMENT_RITE.equals(ritePath)) {
			completeSanguineAttunement(caster);
		}

		// Rite of the Crimson Beacon: register a death waypoint at the rite center
		if (CRIMSON_BEACON_RITE.equals(ritePath)) {
			completeCrimsonBeacon(sLevel, caster, center);
		}

		// Rite of Vascular Mending: fully heal all 7 vein sections
		if (VASCULAR_MENDING_RITE.equals(ritePath)) {
			completeVascularMending(caster);
		}

		if (HEMATIC_FORTIFICATION_RITE.equals(ritePath)) {
			completeHematicFortification(caster);
		}

		// Rite of the Hungering Earth: corrupt terrain in a radius
		if (HUNGERING_EARTH_RITE.equals(ritePath)) {
			completeHungeringEarth(sLevel, caster, center);
		}

		// Rite of the Scarlet Summons: teleport all bloodline members
		if (SCARLET_SUMMONS_RITE.equals(ritePath)) {
			completeScarletSummons(sLevel, caster, center);
		}

		// Rite of Sanguine Dominion: establish a blood domain
		if (SANGUINE_DOMINION_RITE.equals(ritePath)) {
			completeSanguineDominion(sLevel, caster, center);
		}

		// Rite of the Eternal Covenant: permanently increase max blood volume
		if (ETERNAL_COVENANT_RITE.equals(ritePath)) {
			completeEternalCovenant(caster);
		}

		// Rite of Ancestral Communion: open a lore dialogue
		if (ANCESTRAL_COMMUNION_RITE.equals(ritePath)) {
			completeAncestralCommunion(sLevel, caster);
		}

		// Rite of Hematic Unbinding: dissolve the caster's bloodline
		if (HEMATIC_UNBINDING_RITE.equals(ritePath)) {
			completeHematicUnbinding(sLevel, caster);
		}

		// Rite of the Pallid Shadow: strip Unstained progress from a nearby player
		if (PALLID_SHADOW_RITE.equals(ritePath)) {
			completePallidShadow(sLevel, caster, center, rite.getRiteSize());
		}

		// Bloom of the Qliphoth: summon a persistent bloom tree that buffs nearby players
		if (BLOOM_OF_QLIPHOTH_RITE.equals(ritePath)) {
			completeBloomOfQliphoth(sLevel, caster, center);
		}

		// Rite of Sanguine Fervor: boost mob spawn rates in a 3-chunk radius for 5 minutes
		if (SANGUINE_FERVOR_RITE.equals(ritePath)) {
			completeSanguineFervor(sLevel, caster, center);
		}

		// Rite of the Covenant Vigil: everyone who held an assigned station
		// through the ordeal shares the same temporary protection.
		if (COVENANT_VIGIL_RITE.equals(ritePath)) {
			completeCovenantVigil(sLevel, caster, rite);
		}

		// Rite of the Crimson Lodge: degree advancement only; territorial consecration belongs to Founding Fane
		if (ILLUMINATUS_RITE.equals(ritePath)) {
			HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_CRIMSON_LODGE_CONSECRATED);
		}

		// Rite of the Sanguine Eclipse: manually invoke a Blood Moon
		if (SANGUINE_ECLIPSE_RITE.equals(ritePath)) {
			completeSanguineEclipse(sLevel, caster);
		}

		// Rite of the Founding Fane: consecrate the surrounding area as a Harbinger Fane
		if (FOUNDING_FANE_RITE.equals(ritePath)) {
			completeFoundingFane(sLevel, caster, center);
		}

		// Play completion sound
		sLevel.playSound(null, center, SoundEvents.ENDERMAN_SCREAM, SoundSource.BLOCKS, 1.0f, 0.5f);
		sLevel.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);

		// Notify the caster
		caster.displayClientMessage(
				Component.literal("The " + recipe.getRiteName() + " is complete!")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				false);

		// Award rite completion milestone (first rite + tiered)
		SkillPointGainEvents.onRiteCompleted(caster);
		LiberKnowledgeHelper.unlockForRite(caster, ritePath);

		Integer targetDegree = DEGREE_RITE_PATHS.get(ritePath);
		if (targetDegree != null) {
			HemoCapabilityAccess.getInitiatoryDegree(caster).ifPresent(degree -> {
				int currentDegree = degree.getDegreeNumber();
				if (currentDegree < targetDegree) {
					degree.setDegreeNumber(targetDegree);
					if (targetDegree == 8) degree.setArchonPath(EnumArchonPath.APOTHEOS);
					InitiatoryDegreeEvents.syncDegree(caster, degree);

					// Award degree milestone skill points
					SkillPointGainEvents.onDegreeReached(caster, targetDegree);

					// Grant Harbinger degree advancement(s) for the new rank
					HarbingerAdvancementGranter.grantDegree(caster, targetDegree);
					LiberKnowledgeHelper.unlockForDegree(caster, targetDegree);
					KnownManipulationGrantHelper.grantDegreeOneUtilities(caster);

					// Mutual exclusion: reset Unstained progress (Harbingers and Unstained are opposed)
					boolean unstainedWasReset = PathMutualExclusionHelper.resetUnstainedProgress(caster);
					if (unstainedWasReset) {
						caster.displayClientMessage(
								Component.literal("Your purification has been undone by the blood rite.")
										.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
								false);
					}

					EnumInitiatoryDegree newDegree = degree.getDegree();
					if (newDegree != null) {
						caster.displayClientMessage(
								Component.literal("You have attained the ")
										.withStyle(ChatFormatting.DARK_RED)
										.append(Component.translatable(newDegree.getLangKey())
												.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
										.append(Component.literal("!")
												.withStyle(ChatFormatting.DARK_RED)),
								false);
					}
					triggerSpineProgressionWhisper(sLevel, caster, targetDegree);
				}
			});
		}

		// Sanguine Initiation: give the caster a Sanguine Conduit so they can monitor their progress
		if (SANGUINE_INITIATION_RITE.equals(ritePath)) {
			HemoCapabilityAccess.getBloodVolume(caster).ifPresent(volume -> {
				volume.setActive(true);
				BloodVolumeEvents.syncVolume(caster, volume);
			});
			replaceLinkedTempleDisplay(sLevel, center);
			HarbingerAdvancementGranter.grantIfNotDone(caster,
					Hemomancy.rloc("hemomancy/the_first_awakening"));
			ItemStack conduit = new ItemStack(ItemInit.sanguine_conduit.get());
			if (!caster.getInventory().add(conduit)) {
				sLevel.addFreshEntity(new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, conduit));
			}
			ItemStack waybill = new ItemStack(ItemInit.covenant_waybill.get());
			if (!caster.getInventory().add(waybill)) {
				sLevel.addFreshEntity(new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, waybill));
			}
			caster.displayClientMessage(
					Component.translatable("hemomancy.rite.sanguine_initiation.conduit_granted")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}

		if ("cardinal_rite/initiate_rite".equals(ritePath)) {
			ItemStack blob = new ItemStack(ItemInit.sanguine_blob.get());
			if (!caster.getInventory().add(blob)) {
				sLevel.addFreshEntity(new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, blob));
			}
			caster.displayClientMessage(
					Component.translatable("hemomancy.rite.initiate_rite.blob_granted")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}

		if (CHAMBER_OF_WILL_RITE.equals(ritePath)) {
			ChamberVisitService.beginRiteVisit(caster);
		}
		if (rite.commitCompletion()) {
			consumeMatchedStructure(sLevel, recipe, center, layeredMatch);
		}
		CardinalRiteStaffEscrow.restore(caster, rite);
		return true;
	}

	private static void replaceLinkedTempleDisplay(ServerLevel level, BlockPos focusPos) {
		if (!(level.getBlockEntity(focusPos) instanceof CardinalFocusBlockEntity focus)) return;
		BlockPos displayPos = focus.getTempleDisplay();
		if (displayPos != null && level.getBlockState(displayPos).is(BlockInit.mortal_display.get())) {
			level.setBlockAndUpdate(displayPos,
					BlockInit.placed_blood_stained_stone.get().defaultBlockState());
		}
	}

	private static boolean consumeRiteMedium(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (!recipe.hasLayeredStation()) return true;
		if (level.getBlockEntity(rite.getCenterPos()) instanceof CardinalFocusBlockEntity focus
				&& CardinalRiteMediumRules.finish(focus, recipe.getMedium(),
						recipe.shouldConsumeMediumOnSuccess())) {
			return true;
		}
		CardinalRiteOrdealEngine.clearThreats(level, rite);
		CardinalRiteStaffEscrow.restore(caster, rite);
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

	// Gourd Upgrade Helpers

	/**
	 * Searches the player's main hand, off hand, and inventory for an item matching
	 * the given prerequisite. If found, one stack entry is consumed and true is returned.
	 * Prefers the main hand, then off hand, then the first matching inventory slot.
	 */
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
					level.levelEvent(2001, worldPos, Block.getId(state));
				}
			}
		}
	}

	private static boolean consumeGourdPrerequisite(ServerPlayer caster, net.minecraft.world.item.Item prerequisite) {
		// Check main hand first
		ItemStack mainHand = caster.getMainHandItem();
		if (mainHand.getItem() == prerequisite) {
			mainHand.shrink(1);
			return true;
		}

		// Check off hand
		ItemStack offHand = caster.getOffhandItem();
		if (offHand.getItem() == prerequisite) {
			offHand.shrink(1);
			return true;
		}

		// Search entire inventory
		for (int i = 0; i < caster.getInventory().getContainerSize(); i++) {
			ItemStack stack = caster.getInventory().getItem(i);
			if (stack.getItem() == prerequisite) {
				stack.shrink(1);
				return true;
			}
		}

		return false;
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	// Utility Rite Completion Handlers
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	/**
	 * Rite of Sanguine Attunement (Degree 2, Minor):
	 * Resets all blood tendency alignment axes to zero.
	 */
	private static void completeSanguineAttunement(ServerPlayer caster) {
		HemoCapabilityAccess.getBloodTendency(caster).ifPresent(tendency -> {
			for (EnumBloodTendency bt : EnumBloodTendency.values()) {
				tendency.setTendencyAlignment(bt, 0f);
			}
			BloodTendencyEvents.syncTendency(caster, tendency);
		});
		caster.displayClientMessage(
				Component.literal("Your blood tendencies have been purged. You are a blank slate once more.")
						.withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of the Crimson Beacon (Degree 3, Lesser):
	 * Registers a death waypoint at the rite center. On fatal damage, the
	 * player's body will be teleported here before death (one-time use).
	 */
	private static void completeCrimsonBeacon(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		CrimsonBeaconSavedData data = CrimsonBeaconSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();
		CrimsonBeaconSavedData.BeaconEntry previous = data.getBeacon(caster.getUUID());
		data.setBeacon(caster.getUUID(), center, dimension);

		caster.displayClientMessage(
				Component.literal(previous == null
						? "A Crimson Beacon is anchored here. Should you fall, your body will return."
						: "Your Crimson Beacon tears free of its former anchor and settles here.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of Vascular Mending (Degree 3, Lesser):
	 * Fully restores all vein sections to maximum health (100).
	 */
	private static void completeVascularMending(ServerPlayer caster) {
		HemoCapabilityAccess.getVascularSystem(caster).ifPresent(vascular -> {
			for (EnumVeinSections section : EnumVeinSections.values()) {
				java.util.Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
				sys.put(section, 100f);
				vascular.setVascularSystem(sys);
			}
			VascularSystemEvents.syncVascular(caster, vascular);
		});
		caster.displayClientMessage(
				Component.literal("Purified blood surges through your veins. All vascular damage has been mended.")
						.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of the Hungering Earth (Degree 3, Lesser):
	 * Corrupts natural terrain in a radius around the rite center, converting:
	 * <ul>
	 *   <li>Stone â†’ Venous Stone</li>
	 *   <li>Cobblestone â†’ Venous Stone</li>
	 *   <li>Deepslate â†’ Infested Venous Stone</li>
	 *   <li>Dirt/Grass â†’ Befouling Ash Trail (block below becomes venous stone)</li>
	 *   <li>Sand/Gravel â†’ Polished Venous Stone</li>
	 * </ul>
	 */
	private static void completeHungeringEarth(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		int radius = HUNGERING_EARTH_RADIUS;
		int converted = 0;

		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				// Circular check
				if (x * x + z * z > radius * radius) continue;

				for (int y = -radius / 2; y <= radius / 2; y++) {
					BlockPos pos = center.offset(x, y, z);
					BlockState state = sLevel.getBlockState(pos);
					Block block = state.getBlock();

					BlockState replacement = getHungeringEarthReplacement(block);
					if (replacement != null) {
						sLevel.setBlock(pos, replacement, 2);
						converted++;

						// Spawn occasional particles for visual feedback
						if (converted % 10 == 0) {
							sLevel.levelEvent(2001, pos, Block.getId(state));
						}
					}
				}
			}
		}

		caster.displayClientMessage(
				Component.literal("The earth hungers... " + converted + " blocks have been corrupted.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Returns the blood-corrupted replacement for a natural block, or null if
	 * the block should not be converted.
	 */
	private static BlockState getHungeringEarthReplacement(Block block) {
		if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.ANDESITE
				|| block == Blocks.DIORITE || block == Blocks.GRANITE) {
			return BlockInit.venous_stone.get().defaultBlockState();
		}
		if (block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) {
			return BlockInit.infested_venous_stone.get().defaultBlockState();
		}
		if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL
				|| block == Blocks.MYCELIUM || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
			return BlockInit.befouling_ash_trail.get().defaultBlockState();
		}
		if (block == Blocks.SAND || block == Blocks.RED_SAND || block == Blocks.GRAVEL) {
			return BlockInit.polished_venous_stone.get().defaultBlockState();
		}
		return null;
	}

	/**
	 * Rite of the Scarlet Summons (Degree 5, Greater):
	 * Teleports all online bloodline members to the rite center. Draws blood
	 * from the shared bloodline pool proportional to the number of members.
	 */
	private static void completeScarletSummons(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData bloodlineData = BloodlineSavedData.get(overworld);
		Bloodline bloodline = bloodlineData.getBloodlineForPlayer(caster.getUUID());

		if (bloodline == null || !bloodline.isValid()) {
			caster.displayClientMessage(
					Component.literal("You have no bloodline to summon.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Only the bloodline leader may perform the summons
		if (!bloodline.getLeaderUUID().equals(caster.getUUID())) {
			caster.displayClientMessage(
					Component.literal("Only the bloodline leader may perform the Scarlet Summons.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		List<ServerPlayer> onlineMembers = new ArrayList<>();
		for (UUID memberUUID : bloodline.getPlayerUUIDS()) {
			if (memberUUID.equals(caster.getUUID())) continue; // Skip the caster
			ServerPlayer member = sLevel.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				onlineMembers.add(member);
			}
		}

		if (onlineMembers.isEmpty()) {
			caster.displayClientMessage(
					Component.literal("No bloodline members are online to summon.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Draw blood from the shared pool
		float totalCost = onlineMembers.size() * SUMMONS_COST_PER_MEMBER;
		float drawn = bloodlineData.drawBlood(bloodline.getBloodlineUUID(), totalCost);
		if (drawn < totalCost * 0.5f) {
			caster.displayClientMessage(
					Component.literal("The bloodline pool lacks sufficient blood for the summons.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Teleport all online members
		int teleported = 0;
		for (ServerPlayer member : onlineMembers) {
			// Handle cross-dimension teleport
			if (!member.level().equals(sLevel)) {
				member.teleportTo(sLevel, center.getX() + 0.5, center.getY() + 1.5,
						center.getZ() + 0.5, member.getYRot(), member.getXRot());
			} else {
				member.teleportTo(center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5);
			}
			member.displayClientMessage(
					Component.literal("The blood calls! You have been summoned by " + caster.getName().getString() + ".")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
			sLevel.playSound(null, member.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 1.0f, 0.7f);
			teleported++;
		}

		caster.displayClientMessage(
				Component.literal("The Scarlet Summons draws " + teleported + " blood-kin to your side.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of Sanguine Dominion (Degree 6, Greater):
	 * Establishes a persistent Blood Domain centered on the rite location.
	 * Within the domain: enemies take slow bleed damage, and the caster's
	 * manipulations cost less blood.
	 */
	private static void completeSanguineDominion(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		SanguineDominionSavedData data = SanguineDominionSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		SanguineDominionSavedData.DominionEntry entry = new SanguineDominionSavedData.DominionEntry(
				caster.getUUID(), center, dimension, DOMINION_CHUNK_RADIUS, sLevel.getGameTime());
		SanguineDominionSavedData.DominionEntry previous = data.replaceDominionForOwner(entry);

		int blockRadius = DOMINION_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal((previous == null
						? "A Blood Domain has been established! "
						: "Your Blood Domain abandons its former bounds and reforms here. ")
						+ blockRadius + " blocks in every direction now bow to your crimson will.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_SANGUINE_DOMAIN);
	}

	/**
	 * Rite of the Crimson Lodge (Degree 5, Greater):
	 * Establishes a persistent Crimson Lodge centered on the rite location.
	 * Within the lodge: players gain Strength I and enhanced blood
	 * regeneration. The structure blocks are preserved (breakBlocksOnCreation
	 * is false in the recipe). Recruited NPC Harbingers may be summoned
	 * anywhere within the lodge radius via the ancestral ledger.
	 */
	private static void completeCrimsonLodge(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		CrimsonLodgeSavedData data = CrimsonLodgeSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		// Check for overlap with existing lodges
		CrimsonLodgeSavedData.LodgeEntry overlapping = data.getOverlappingLodge(
				center, dimension, CRIMSON_LODGE_CHUNK_RADIUS);
		if (overlapping != null) {
			caster.displayClientMessage(
					Component.literal("A Crimson Lodge already exists within " + CRIMSON_LODGE_CHUNK_RADIUS
							+ " chunks of here. Only one lodge may exist per region.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		CrimsonLodgeSavedData.LodgeEntry entry = new CrimsonLodgeSavedData.LodgeEntry(
				caster.getUUID(), center, dimension, CRIMSON_LODGE_CHUNK_RADIUS, sLevel.getGameTime(), center);
		data.addLodge(entry);

		int blockRadius = CRIMSON_LODGE_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal("The Crimson Lodge has been consecrated! Strength and blood virility flow within "
						+ blockRadius + " blocks.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_CRIMSON_LODGE_CONSECRATED);
	}

	/**
	 * Rite of the Eternal Covenant (Degree 6, Greater):
	 * Permanently increases the caster's maximum blood volume. Can only be
	 * performed once per player.
	 */
	private static void completeEternalCovenant(ServerPlayer caster) {
		CompoundTag persistentData = caster.getPersistentData();
		if (persistentData.getBoolean(MaxBloodLedger.ETERNAL_COVENANT_TAG)) {
			caster.displayClientMessage(
					Component.literal("The covenant is already sealed. Its boon has already been granted.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		persistentData.putBoolean(MaxBloodLedger.ETERNAL_COVENANT_TAG, true);
		HemoCapabilityAccess.getBloodVolume(caster).ifPresent(volume -> MaxBloodLedger.apply(caster, volume));

		caster.displayClientMessage(
				Component.literal("The Eternal Covenant is sealed! Your maximum blood volume has been permanently increased by "
						+ (int) MaxBloodLedger.ETERNAL_COVENANT_BONUS + ".")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				false);
		HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_ETERNAL_COVENANT_SEALED);
	}

	/**
	 * Rite of Ancestral Communion (Degree 7, Grand):
	 * Opens a dialogue with the fungal consciousness, granting unique lore.
	 * Each invocation uses a different dialogue variant from a pool.
	 */
	private static void completeAncestralCommunion(ServerLevel sLevel, ServerPlayer caster) {
		int variant = HemoCapabilityAccess.getInitiatoryDegree(caster)
				.map(degree -> {
					int next = Math.floorMod(degree.getAncestralCommunions(),
							AncestralCommunionDialogueTrees.VARIANT_COUNT);
					degree.setAncestralCommunions(degree.getAncestralCommunions() + 1);
					InitiatoryDegreeEvents.syncDegree(caster, degree);
					return next;
				})
				.orElse(0);
		DialogueTree tree = AncestralCommunionDialogueTrees.forVariant(variant);

		PacketHandler.sendToPlayer(caster, new OpenDialoguePacket(tree));

		caster.displayClientMessage(
				Component.literal("The ancient blood stirs... a voice rises from the depths.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
				false);
		HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_VOICES_IN_THE_VEIN);
	}

	/**
	 * Rite of Hematic Unbinding (Cross-tier, Lesser):
	 * Destroys the caster's bloodline, freeing all members. Any shared blood
	 * in the pool is returned proportionally to the remaining members.
	 */
	private static void completeHematicUnbinding(ServerLevel sLevel, ServerPlayer caster) {
		final String pendingLineKey = "hemomancy:pending_unbinding_bloodline";
		final String pendingUntilKey = "hemomancy:pending_unbinding_until";
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData bloodlineData = BloodlineSavedData.get(overworld);
		Bloodline bloodline = bloodlineData.getBloodlineForPlayer(caster.getUUID());

		if (bloodline == null || !bloodline.isValid()) {
			caster.getPersistentData().remove(pendingLineKey);
			caster.getPersistentData().remove(pendingUntilKey);
			caster.displayClientMessage(
					Component.literal("You have no bloodline to unbind.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Only the leader can dissolve the bloodline
		if (!bloodline.getLeaderUUID().equals(caster.getUUID())) {
			caster.displayClientMessage(
					Component.literal("Only the bloodline leader may perform the Hematic Unbinding.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		CompoundTag playerData = caster.getPersistentData();
		UUID warnedLine = playerData.hasUUID(pendingLineKey)
				? playerData.getUUID(pendingLineKey) : null;
		long warningUntil = playerData.getLong(pendingUntilKey);
		if (HematicUnbindingRules.decision(bloodline.getBloodlineUUID(), warnedLine,
				warningUntil, sLevel.getGameTime()) == HematicUnbindingRules.Decision.WARN) {
			playerData.putUUID(pendingLineKey, bloodline.getBloodlineUUID());
			playerData.putLong(pendingUntilKey,
					sLevel.getGameTime() + HematicUnbindingRules.CONFIRMATION_TICKS);
			caster.displayClientMessage(
					Component.literal("The covenant loosens but holds. Performing Hematic Unbinding again "
									+ "within ten minutes will permanently dissolve " + bloodline.getName()
									+ ", remove its fanes, and free every member.")
							.withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
					false);
			return;
		}
		playerData.remove(pendingLineKey);
		playerData.remove(pendingUntilKey);

		String bloodlineName = bloodline.getName();
		float poolBlood = bloodline.getBloodVolume();
		int memberCount = bloodline.getPlayerUUIDS().size();
		float bloodPerMember = memberCount > 0 ? poolBlood / memberCount : 0;

		// Return blood to all online members and clear their bloodline reference
		for (UUID memberUUID : new ArrayList<>(bloodline.getPlayerUUIDS())) {
			ServerPlayer member = sLevel.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				// Return their share of the pool blood
				if (bloodPerMember > 0) {
					HemoCapabilityAccess.getBloodVolume(member).ifPresent(volume -> {
						volume.fill(bloodPerMember);
						volume.setBloodLine(Bloodline.NOBLOODLINE);
						BloodVolumeEvents.syncVolume(member, volume);
					});
				} else {
					HemoCapabilityAccess.getBloodVolume(member).ifPresent(volume -> {
						volume.setBloodLine(Bloodline.NOBLOODLINE);
						BloodVolumeEvents.syncVolume(member, volume);
					});
				}

				member.displayClientMessage(
						Component.literal("The bloodline " + bloodlineName + " has been dissolved. You are unbound.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
			}
		}

		// Remove the bloodline and any fanes owned by its members from world data.
		BloodlineDisbandHelper.removeOwnedFanes(sLevel.getServer(), bloodline);
		bloodlineData.disbandBloodline(bloodline.getBloodlineUUID());

		caster.displayClientMessage(
				Component.literal("The " + bloodlineName + " is dissolved. What was bound by blood is unbound.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Rite of the Pallid Shadow (Cross-tier, Grand):
	 * Targets the nearest non-caster player within the rite circle and strips
	 * their Unstained purification progress. A direct hematic assault against
	 * followers of Our Lady of Still Waters.
	 */
	private static void completePallidShadow(ServerLevel sLevel, ServerPlayer caster,
			BlockPos center, int riteSize) {
		int halfSize = (Math.max(1, riteSize) - 1) / 2;
		AABB bounds = new AABB(center).inflate(halfSize + 1);

		List<ServerPlayer> nearbyPlayers = sLevel.getEntitiesOfClass(ServerPlayer.class, bounds,
				target -> target.isAlive() && !target.getUUID().equals(caster.getUUID())
						&& PallidShadowRules.canTarget(
								sLevel.getServer().isPvpAllowed(),
								target.isCreative(), target.isSpectator(),
								caster.isAlliedTo(target),
								HemoCapabilityAccess.getUnstainedProgress(target)
										.map(progress -> progress.hasBegunPurification())
										.orElse(false)));

		if (nearbyPlayers.isEmpty()) {
			caster.displayClientMessage(
					Component.literal("No target stands within the circle. The shadow dissipates.")
							.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Target the closest player
		ServerPlayer target = nearbyPlayers.get(0);
		double closestDist = target.distanceToSqr(center.getX(), center.getY(), center.getZ());
		for (ServerPlayer p : nearbyPlayers) {
			double dist = p.distanceToSqr(center.getX(), center.getY(), center.getZ());
			if (dist < closestDist) {
				target = p;
				closestDist = dist;
			}
		}

		final ServerPlayer victim = target;
		HemoCapabilityAccess.getUnstainedProgress(victim).ifPresent(unstained -> {
			boolean hadProgress = PathMutualExclusionHelper.resetUnstainedProgress(victim, unstained);

			if (hadProgress) {
				victim.displayClientMessage(
						Component.literal("A shadow of crimson corruption washes over you... Your purification has been destroyed!")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
						false);
				caster.displayClientMessage(
						Component.literal("The Pallid Shadow consumes " + victim.getName().getString()
								+ "'s purity. Their purification is undone.")
								.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
						false);
			} else {
				caster.displayClientMessage(
						Component.literal(victim.getName().getString()
								+ " had no purification to destroy. The shadow finds nothing.")
								.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
						false);
			}
		});

		// Visual/sound feedback
		sLevel.playSound(null, center, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.7f, 1.5f);
	}

	/**
	 * Bloom of the Qliphoth (Degree 7, Grand â€” Archon-tier summoning rite):
	 * Summons a persistent Qliphoth Bloom at the rite center. Within a 3-chunk
	 * radius, all blood manipulations cost 25% less blood and players receive
	 * passive health regeneration and enhanced blood regeneration.
	 * <p>
	 * Places a 1Ã—1Ã—8 multi-block (QliphothBloomBlock + 7 fillers) at the
	 * ritual center and registers the bloom in world SavedData.
	 * The tree produces exactly nine pomes over its lifecycle â€” one for each
	 * husk of the Qliphoth â€” then ceases dropping fruit until re-summoned.
	 */
	private static void completeBloomOfQliphoth(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		// Check if a bloom already exists within the radius â€” only one bloom per 3-chunk area
		QliphothBloomSavedData.BloomEntry overlapping = data.getOverlappingBloom(
				center.above(2), dimension, QLIPHOTH_BLOOM_CHUNK_RADIUS);
		if (overlapping != null) {
			caster.displayClientMessage(
					Component.literal("A Qliphoth Bloom already exists within " + QLIPHOTH_BLOOM_CHUNK_RADIUS
							+ " chunks of here. Only one bloom may exist per " + QLIPHOTH_BLOOM_CHUNK_RADIUS
							+ "-chunk radius.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Verify there is room for the 1Ã—1Ã—8 column
		Block bloomBlock = BlockInit.qliphoth_bloom.get();
		IMultiBlock multiBlock =
				(IMultiBlock) bloomBlock;
		if (!multiBlock.canPlaceMultiBlock(sLevel, center.above(2))) {
			caster.displayClientMessage(
					Component.literal("The Qliphoth needs more room to bloom here.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Place the multi-block
		sLevel.setBlockAndUpdate(center.above(2), bloomBlock.defaultBlockState());
		net.minecraft.world.level.block.entity.BlockEntity be = sLevel.getBlockEntity(center.above(2));
		if (be instanceof com.vincenthuto.hemomancy.common.tile.functional.QliphothBloomBlockEntity bloomBE) {
			bloomBE.setOwnerUUID(caster.getUUID());
			bloomBE.setChunkRadius(QLIPHOTH_BLOOM_CHUNK_RADIUS);
		}
		// Place filler blocks above
		multiBlock.placeFillers(sLevel, center.above(2), bloomBlock.defaultBlockState());

		// Register in SavedData
		QliphothBloomSavedData.BloomEntry entry = new QliphothBloomSavedData.BloomEntry(
				caster.getUUID(), center.above(2),dimension, QLIPHOTH_BLOOM_CHUNK_RADIUS, sLevel.getGameTime());
		data.addBloom(entry);

		// Sync to all nearby clients so the tree renders immediately
		syncQliphothBlooms(sLevel.getServer());

		int blockRadius = QLIPHOTH_BLOOM_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal("The Qliphoth blooms! A dark tree takes root, empowering blood within "
						+ blockRadius + " blocks.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);

		// Fire the post-bloom Fungal Whisper so the Entity acknowledges the fruiting
		if (FungalWhisperDialogueTrees.shouldOfferMemoWhisper(caster, MemoDefinitions.QLIPHOTH_COMMUNION)) {
			PacketHandler.sendToPlayer(caster, new OpenDialoguePacket(FungalWhisperDialogueTrees.postBloom()));
		}
	}

	private static void completeHematicFortification(ServerPlayer caster) {
		HemoCapabilityAccess.getInitiatoryDegree(caster).ifPresent(degree -> {
			degree.setHematicFortification(true);
			com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.onFortification(caster);
			InitiatoryDegreeEvents.syncDegree(caster, degree);
		});
		caster.displayClientMessage(
				Component.literal("Your vascular lattice hardens. Future strain is reduced by fifteen percent.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of the Covenant Vigil (Grand): rewards the caster and every assigned
	 * ally who is still present at completion. The common effects make the
	 * covenant mechanically shared instead of treating the helper as a key used
	 * only to unlock the ceremony.
	 */
	private static void completeCovenantVigil(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		grantCovenantVigilReward(caster);
		HarbingerAdvancementGranter.grantIfNotDone(caster,
				HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED);
		HarbingerChapterProgression.tryCompleteLivingCovenant(caster);
		for (UUID allyId : rite.getAllyRoles().keySet()) {
			if (!CardinalRiteAllyService.isAvailable(sLevel, rite, allyId)) continue;
			Entity entity = sLevel.getEntity(allyId);
			if (entity instanceof LivingEntity ally && ally.isAlive()) {
				grantCovenantVigilReward(ally);
			}
		}
		caster.displayClientMessage(
				Component.literal("The vigil closes around every survivor. The covenant guards and restores you.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	private static void grantCovenantVigilReward(LivingEntity participant) {
		participant.addEffect(new MobEffectInstance(
				MobEffects.DAMAGE_RESISTANCE,
				CovenantVigilRules.REWARD_DURATION_TICKS,
				CovenantVigilRules.RESISTANCE_AMPLIFIER,
				false, true, true));
		participant.addEffect(new MobEffectInstance(
				MobEffects.REGENERATION,
				CovenantVigilRules.REWARD_DURATION_TICKS,
				CovenantVigilRules.REGENERATION_AMPLIFIER,
				false, true, true));
		if (participant instanceof ServerPlayer player) {
			player.displayClientMessage(
					Component.literal("Your shared vigil lingers: resistance and regeneration for ten minutes.")
							.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
					false);
		}
	}

	/**
	 * Rite of Sanguine Fervor (Lesser):
	 * Registers a time-limited spawn-boost zone centred on the rite. For
	 * {@link #SANGUINE_FERVOR_DURATION_TICKS} ticks (5 minutes) natural mob
	 * spawns within a {@link #SANGUINE_FERVOR_CHUNK_RADIUS}-chunk radius are
	 * force-allowed regardless of the global mob cap, greatly increasing local
	 * mob density and making the area ideal for farming.
	 */
	private static void completeSanguineFervor(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		SanguineFervorSavedData data = SanguineFervorSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + SANGUINE_FERVOR_DURATION_TICKS;

		SanguineFervorSavedData.FervorEntry entry = new SanguineFervorSavedData.FervorEntry(
				caster.getUUID(), center, dimension, SANGUINE_FERVOR_CHUNK_RADIUS, expiryTick);
		data.addEntry(entry);

		int blockRadius = SANGUINE_FERVOR_CHUNK_RADIUS * 16;
		long durationMinutes = SANGUINE_FERVOR_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The blood heats the earth! Mobs will swarm within "
						+ blockRadius + " blocks for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

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
					int pomesDropped = data.getPomesDropped(bloom.center());
					clientEntries.add(new com.vincenthuto.hemomancy.client.data.QliphothBloomClientData.BloomEntry(
							bloom.center(), bloom.chunkRadius(), pomesDropped,
							data.getState(bloom.center()).ordinal()));
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

	/**
	 * Pre-signs a bloodline ledger with the caster's newly founded bloodline.
	 * Creates the bloodline, registers it in world data, sets the caster's capability,
	 * and writes the signed state onto the ledger item so it can be redistributed.
	 */
	private static void presignBloodlineLedger(ServerLevel sLevel, ServerPlayer caster, ItemStack ledgerStack) {
		String bloodLineName = caster.getName().getString() + "'s Blood Line";
		UUID bloodLineUUID = new UUID(caster.getUUID().getMostSignificantBits(), sLevel.getGameTime());
		ArrayList<UUID> uuids = new ArrayList<>();
		Bloodline playerLine = new Bloodline(bloodLineName, caster.getUUID(), bloodLineUUID, uuids);

		// Register bloodline in world-level saved data
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		savedData.registerBloodline(playerLine);

		// Set the caster's bloodline capability
		HemoCapabilityAccess.getBloodVolume(caster).ifPresent(volume -> {
			volume.setBloodLine(playerLine);
			BloodVolumeEvents.syncVolume(caster, volume);
		});
		HemoCapabilityAccess.getInitiatoryDegree(caster).ifPresent(degree -> {
			degree.setHasFoundedBloodline(true);
			InitiatoryDegreeEvents.syncDegree(caster, degree);
		});

		// Write signed state and bloodline data onto the ledger
		CompoundTag compound = ledgerStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		compound.putBoolean(UnsignedLedgerItem.TAG_STATE, true);
		compound.put(UnsignedLedgerItem.TAG_BLOODLINE, playerLine.serialize());
		ledgerStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));

		caster.displayClientMessage(
				Component.literal("You have founded: " + playerLine.getName())
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Recalls a lost bloodline ledger by looking up the caster's existing bloodline
	 * from world data and writing it onto the result item. This is a penitent rite â€”
	 * the covenant does not forget, but it demands a price for carelessness.
	 * Returns false if the caster has no bloodline to recall.
	 */
	private static boolean recallBloodlineLedger(ServerLevel sLevel, ServerPlayer caster, ItemStack ledgerStack) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		Bloodline existingLine = savedData.getBloodlineForPlayer(caster.getUUID());

		if (existingLine == null || !existingLine.isValid()) {
			return false;
		}

		// Write the existing bloodline data onto the new ledger as a signed copy
		CompoundTag compound = ledgerStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		compound.putBoolean(UnsignedLedgerItem.TAG_STATE, true);
		compound.put(UnsignedLedgerItem.TAG_BLOODLINE, existingLine.serialize());
		ledgerStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));

		caster.displayClientMessage(
				Component.literal("The covenant remembers. Your ledger for " + existingLine.getName()
						+ " has been restored.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		return true;
	}


	private static boolean isApotheosRite(ResourceLocation recipeId) {
		return recipeId != null && APOTHEOS_RITE_PATH.equals(recipeId.getPath());
	}

	private static void triggerSpineProgressionWhisper(ServerLevel level, ServerPlayer player, int targetDegree) {
		if (targetDegree < 5 || targetDegree > 8) {
			return;
		}

		if (targetDegree == 8) return;

		PacketHandler.sendToPlayer(player, new OpenDialoguePacket(FungalWhisperDialogueTrees.spineGrowth(targetDegree)));
	}

	private static void popFungalSpineFromBack(ServerLevel level, ServerPlayer player) {
		ItemStack spine = new ItemStack(ItemInit.fungal_spine.get());
		Vec3 look = player.getLookAngle();
		double x = player.getX() - look.x * 0.55;
		double y = player.getY() + 1.15;
		double z = player.getZ() - look.z * 0.55;

		level.playSound(null, player.blockPosition(), SoundEvents.SLIME_BLOCK_BREAK, SoundSource.PLAYERS, 1.8f, 0.55f);
		level.playSound(null, player.blockPosition(), SoundEvents.HONEY_BLOCK_BREAK, SoundSource.PLAYERS, 1.5f, 0.65f);
		level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.7f, 0.8f);

		ItemEntity drop = new ItemEntity(level, x, y, z, spine);
		drop.setDeltaMovement(-look.x * 0.18, 0.18, -look.z * 0.18);
		drop.setPickUpDelay(10);
		level.addFreshEntity(drop);
	}

	private static boolean hasQliphothCommunion(ServerPlayer player) {
		return HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(degree -> degree.isQliphothCommunionDone())
				.orElse(false);
	}


	private static void completeSanguineEclipse(ServerLevel sLevel, ServerPlayer caster) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodMoonSavedData bloodMoonData = BloodMoonSavedData.get(overworld);
		if (bloodMoonData.isActive()) {
			caster.displayClientMessage(
					Component.literal("A Blood Moon already reigns over this world...")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}
		long gameTime = overworld.getGameTime();
		bloodMoonData.start(gameTime + 11900L);
		for (ServerPlayer p : overworld.getPlayers(ServerPlayer::isAlive)) {
			p.sendSystemMessage(Component.translatable("hemomancy.blood_moon.start")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
		}
		PacketDistributor.sendToAllPlayers(new PacketSyncBloodMoon(true));
		caster.displayClientMessage(
				Component.literal("The ritual tears the veil â€” the Blood Moon rises!")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}


	private static void completeFoundingFane(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		BlockPos heartPos = center.above(3);
		BlockState heartState = sLevel.getBlockState(heartPos);
		if (!heartState.is(BlockInit.consecrated_bloodwell.get())
				&& !heartState.isAir()
				&& !heartState.canBeReplaced()) {
			caster.displayClientMessage(
					Component.literal("The rite's heart was obstructed before the Consecrated Bloodwell could be manifested.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(caster)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		if (bloodline == null || !bloodline.isValid()) {
			caster.displayClientMessage(
					Component.literal("The Founding Fane requires an established bloodline.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}
		if (!bloodline.getLeaderUUID().equals(caster.getUUID())) {
			caster.displayClientMessage(
					Component.literal("Only the bloodline Progenitor may consecrate a Founding Fane.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}
		UUID faneOwner = bloodline.getLeaderUUID();
		FoundingFaneSavedData faneData = FoundingFaneSavedData.get(sLevel);
		boolean isReconsecrating = false;
		for (ServerLevel oldLevel : sLevel.getServer().getAllLevels()) {
			FoundingFaneSavedData oldData = FoundingFaneSavedData.get(oldLevel);
			if (!oldData.hasFane(faneOwner)) continue;
			isReconsecrating = true;
			for (BlockPos stakePos : oldData.removeStakesAndGet(faneOwner)) {
				if (oldLevel.getBlockState(stakePos).is(BlockInit.hematic_stake.get())) {
					oldLevel.removeBlock(stakePos, false);
				}
			}
		}
		if (!sLevel.getBlockState(heartPos).is(BlockInit.consecrated_bloodwell.get())
				&& !sLevel.setBlock(heartPos, BlockInit.consecrated_bloodwell.get().defaultBlockState(), 3)) {
			caster.displayClientMessage(
					Component.literal("The rite falters before the Consecrated Bloodwell can take form.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}
		faneData.consecrateHeart(faneOwner, heartPos);
		if (isReconsecrating) {
			caster.displayClientMessage(
					Component.literal("Your Founding Fane has been moved to this location.")
							.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
					false);
		} else {
			for (ServerPlayer p : sLevel.getPlayers(ServerPlayer::isAlive)) {
				p.sendSystemMessage(Component.literal(caster.getDisplayName().getString()
						+ " has consecrated a Founding Fane.")
						.withStyle(ChatFormatting.DARK_RED));
			}
			caster.displayClientMessage(
					Component.literal("This ground is now consecrated. All Harbingers within will feel its power.")
							.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
					false);
			HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_FOUNDING_FANE_ESTABLISHED);
			HarbingerAdvancementGranter.grantIfNotDone(caster,
					HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE);
		}
		sLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
				heartPos.getX() + 0.5, heartPos.getY() + 1.0, heartPos.getZ() + 0.5,
				300, FoundingFaneSavedData.FANE_RADIUS * 0.3, 3.0, FoundingFaneSavedData.FANE_RADIUS * 0.3, 0.01);
	}

}
