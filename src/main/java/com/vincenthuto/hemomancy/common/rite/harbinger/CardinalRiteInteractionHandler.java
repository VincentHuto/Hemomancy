package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.utility.AwakenedIchorianSigilEntity;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteFootprintRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstabilityBoundaryRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hutoslib.client.particle.data.EmberParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntUnaryOperator;

/**
 * Routes blood tools and the Neophyte's bare-hand bloodletting into virtual
 * rite nodes before ordinary projection endpoints.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CardinalRiteInteractionHandler {
	private static final double HANDLED_WITHOUT_BLOOD = Double.MIN_NORMAL;
	private static final int FALSE_STROKE_OUTER_BLACK = 0xE806020A;
	private static final int FALSE_STROKE_INNER_PURPLE = 0xFF5A167D;

	private CardinalRiteInteractionHandler() {
	}

	public static CardinalRiteProjectionResult tryProject(Level level, LivingEntity source, double projectionRate) {
		if (!(level instanceof ServerLevel serverLevel) || !(source instanceof ServerPlayer player)) {
			return CardinalRiteProjectionResult.unhandled();
		}
		HitResult trace = SanguineProjectionTargeting.pick(serverLevel, player,
				SanguineProjectionTargeting.PROJECTION_REACH, true);
		BlockPos physicalTarget = trace instanceof BlockHitResult blockHit ? blockHit.getBlockPos() : null;

		CardinalRiteSavedData data = CardinalRiteSavedData.get(serverLevel);
		for (ActiveCardinalRite rite : data.getActiveRites().values()) {
			if (!mayParticipate(player, rite)) continue;
			if (tryProjectSeal(serverLevel, player, rite)) {
				data.setDirty();
				return CardinalRiteProjectionResult.handled(0.0D);
			}
			BlockPos target = virtualProjectionTarget(serverLevel, player, rite);
			if (target == null) target = physicalTarget;
			if (target == null) continue;
			double falseOmen = tryProjectFalseOmen(serverLevel, player, rite, target, projectionRate);
			if (falseOmen > 0.0D) {
				data.setDirty();
				return CardinalRiteProjectionResult.handled(falseOmen);
			}
			double sigilHandled = tryProjectSigil(serverLevel, player, rite, target, projectionRate);
			if (sigilHandled > 0.0D) {
				data.setDirty();
				return CardinalRiteProjectionResult.handled(sigilHandled);
			}
			int anchor = anchorAt(serverLevel, rite, target);
			if (anchor < 0) continue;
			int repairNeeded = rite.instabilityRepairBloodNeeded(anchor);
			if (repairNeeded > 0) {
				int requested = Math.min(repairNeeded, Math.max(1, (int) Math.floor(projectionRate)));
				int committed = spendBlood(player, rite, requested);
				if (committed <= 0) return CardinalRiteProjectionResult.handled(0.0D);
				rite.offerInstabilityRepair(anchor, committed);
				data.setDirty();
				serverLevel.playSound(null, target, SoundEvents.RESPAWN_ANCHOR_CHARGE,
						SoundSource.PLAYERS, 0.4F, 0.6F + anchor * 0.012F);
				return CardinalRiteProjectionResult.handled(committed);
			}
			int needed = rite.bloodNeededForAnchor(anchor);
			if (needed <= 0) return CardinalRiteProjectionResult.handled(0.0D);

			int requested = Math.min(needed, Math.max(1, (int) Math.floor(projectionRate)));
			int committed = spendBlood(player, rite, requested);
			if (committed <= 0) return CardinalRiteProjectionResult.handled(0.0D);
			rite.fillAnchor(anchor, committed);
			if (rite.areAnchorsConsecrated()) {
				rite.enterInscription();
				player.displayClientMessage(Component.literal(
						"The boundary lives. Prepare support, then project into the daemon.")
						.withStyle(ChatFormatting.DARK_RED), false);
			}
			data.setDirty();
			serverLevel.playSound(null, target, SoundEvents.BUCKET_FILL,
					SoundSource.PLAYERS, 0.35F, 0.65F + anchor * 0.015F);
			return CardinalRiteProjectionResult.handled(committed);
		}
		return CardinalRiteProjectionResult.unhandled();
	}

	private static boolean tryProjectSeal(ServerLevel level, ServerPlayer player,
			ActiveCardinalRite rite) {
		if (rite.getPhase() != CardinalRitePhase.INSCRIPTION) return false;
		HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(
				level, rite.getPlayerUUID(), rite.getCenterPos());
		if (daemon == null || CardinalRiteVirtualTargeting.closestTarget(
				player.getEyePosition(), player.getLookAngle(),
				CardinalRiteVirtualTargeting.PROJECTION_RANGE,
				CardinalRiteVirtualTargeting.TARGET_RADIUS,
				List.of(daemon.position())) < 0) return false;
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null || !sealMediumReady(level, rite, recipe)) return true;
		if (!CardinalRiteAllyService.hasRequiredHelpers(level, rite)) {
			player.displayClientMessage(Component.literal("The rite cannot be sealed until its required bloodline helpers take their stations.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
			return true;
		}
		for (var socket : recipe.getCeremony().supportSockets()) {
			if (socket.required()
					&& !rite.isSigilAwakened(Hemomancy.rloc(socket.suggestedSigil()).toString())) {
				player.displayClientMessage(Component.literal(
								"The required " + socket.suggestedSigil() + " support sigil has not awakened.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
				return true;
			}
		}
		prepareWaveDeck(rite, recipe);
		rite.sealAltar(recipe.getCeremony().stillIntervalTicks() > 0);
		level.playSound(null, rite.getCenterPos(), SoundEvents.BEACON_POWER_SELECT,
				SoundSource.BLOCKS, 1.0F, 0.8F);
		player.displayClientMessage(Component.literal(rite.getTotalWaves() > 0
						? "The altar is sealed. Endure the ordeal."
						: rite.getPhase() == CardinalRitePhase.STILL_INTERVAL
								? "The altar is sealed. Restore the rite before culmination."
								: "The altar is sealed. The rite begins its culmination.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
		return true;
	}

	private static BlockPos virtualProjectionTarget(ServerLevel level, ServerPlayer player,
			ActiveCardinalRite rite) {
		List<ProjectionTarget> targets = activeProjectionTargets(level, rite);
		List<Vec3> aimPoints = targets.stream().map(ProjectionTarget::aimPoint).toList();
		int selected = CardinalRiteVirtualTargeting.closestTarget(
				player.getEyePosition(), player.getLookAngle(),
				CardinalRiteVirtualTargeting.PROJECTION_RANGE,
				CardinalRiteVirtualTargeting.TARGET_RADIUS, aimPoints);
		return selected < 0 ? null : targets.get(selected).interactionPos();
	}

	private static List<ProjectionTarget> activeProjectionTargets(ServerLevel level, ActiveCardinalRite rite) {
		List<ProjectionTarget> targets = new ArrayList<>();
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe != null && recipe.getCeremony() != null) {
			for (int index = 0; index < recipe.getCeremony().anchors().size(); index++) {
				if (rite.bloodNeededForAnchor(index) <= 0
						&& rite.instabilityRepairBloodNeeded(index) <= 0) continue;
				var anchor = recipe.getCeremony().anchors().get(index);
				BlockPos offset = anchor.offset();
				targets.add(new ProjectionTarget(
						CardinalRiteAnchorVisualRules.riteSurface(rite.getCenterPos())
								.offset(offset.getX(), 0, offset.getZ()),
						CardinalRiteTargetGeometry.anchorAimPoint(rite.getCenterPos(), offset)));
			}
		}
		for (SigilPlacement placement : activeSigils(level, rite)) {
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(placement.id());
			if (sigil == null) continue;
			BlockPos base = rite.getCenterPos().offset(0, placement.y(), 0);
			int completed = Math.min(sigil.nodes().size(),
					rite.getSigilProgress().getOrDefault(placement.progressKey(), 0));
			for (int index : CardinalRiteSigilRules.raycastNodeIndices(
					completed, sigil.nodes().size())) {
				IchorianSigilDefinition.Node node = sigil.nodes().get(index);
				BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level, base,
						placement.x() + (int) Math.round(node.x()),
						placement.z() + (int) Math.round(node.z()));
				targets.add(new ProjectionTarget(surface,
						CardinalRiteTargetGeometry.sigilAimPoint(
								rite.getCenterPos(), surface, placement.x(), placement.z(),
								node.x(), node.z())));
			}
		}
		if (rite.getPhase() == CardinalRitePhase.ORDEAL
				&& rite.getCurrentWave() < rite.getWaveDeck().size()
				&& "false_omens".equals(rite.getWaveDeck().get(rite.getCurrentWave()))) {
			for (BlockPos offset : List.of(
					new BlockPos(2, 1, 2),
					new BlockPos(-2, 1, -2),
					new BlockPos(2, 1, -2))) {
				BlockPos target = rite.getCenterPos().offset(offset);
				targets.add(new ProjectionTarget(target,
						new Vec3(target.getX() + 0.5D, target.getY() + 0.12D, target.getZ() + 0.5D)));
			}
		}
		return targets;
	}

	private static double tryProjectSigil(ServerLevel level, ServerPlayer player, ActiveCardinalRite rite,
			BlockPos target, double projectionRate) {
		SigilOffer offer = tryOfferSigilBlood(level, player, rite, target, projectionRate,
				requested -> spendBlood(player, rite, requested));
		if (!offer.handled()) return 0.0D;
		return offer.spent() > 0 ? offer.spent() : HANDLED_WITHOUT_BLOOD;
	}

	private static SigilOffer tryOfferSigilBlood(ServerLevel level, ServerPlayer player, ActiveCardinalRite rite,
			BlockPos target, double projectionRate, IntUnaryOperator payment) {
		List<SigilPlacement> placements = activeSigils(level, rite);
		for (SigilPlacement placement : placements) {
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(placement.id());
			if (sigil == null || sigil.nodes().isEmpty()) continue;
			String progressKey = placement.progressKey();
			int completed = Math.min(sigil.nodes().size(), rite.getSigilProgress().getOrDefault(progressKey, 0));
			if (completed >= sigil.nodes().size()) {
				if (nodeAt(level, rite, placement, sigil, target) >= 0) {
					return SigilOffer.handled(0);
				}
				continue;
			}
			int touched = nodeAt(level, rite, placement, sigil, target);
			if (touched < 0) continue;
			CardinalRiteSigilRules.StrokeDisposition disposition =
					CardinalRiteSigilRules.strokeDisposition(touched, completed);
			if (disposition == CardinalRiteSigilRules.StrokeDisposition.COMPLETED) {
				return SigilOffer.handled(0);
			}
			String falseStrokeKey = "false_stroke:" + progressKey + ":" + touched;
			if (disposition == CardinalRiteSigilRules.StrokeDisposition.FALSE
					&& rite.getSigilProgress().containsKey(falseStrokeKey)
					&& !CardinalRiteSigilRules.falseStrokePenaltyReady(rite.getPhaseTicks(),
							rite.getSigilProgress().get(falseStrokeKey))) {
				return SigilOffer.handled(0);
			}
			int requestedBlood = disposition == CardinalRiteSigilRules.StrokeDisposition.FALSE
					? CardinalRiteSigilRules.falseStrokeBloodRequest(projectionRate)
					: Math.max(1, Math.min(50, (int) Math.floor(projectionRate)));
			int spend = payment.applyAsInt(requestedBlood);
			if (spend <= 0) return SigilOffer.handled(0);
			if (disposition == CardinalRiteSigilRules.StrokeDisposition.FALSE) {
				rite.setSigilProgress(falseStrokeKey, rite.getPhaseTicks());
				boolean caught = CardinalRiteAllyService.tryCorrectMiss(level, rite);
				if (!caught) rite.addInstability(CardinalRiteSigilRules.FALSE_STROKE_INSTABILITY);
				spawnFalseStrokeBolts(level, rite, placement, sigil, touched);
				player.displayClientMessage(Component.literal(caught
								? "The Attendant catches the false stroke before it destabilizes the rite."
								: "The false stroke takes a trace offering.")
						.withStyle(ChatFormatting.DARK_PURPLE), true);
				return SigilOffer.handled(spend);
			}
			String bloodKey = "blood:" + progressKey;
			int nodeBlood = rite.getSigilProgress().getOrDefault(bloodKey, 0) + spend;
			spawnCorrectNodeFeedFeedback(level, rite, placement, sigil, touched,
					Math.min(50, nodeBlood));
			if (nodeBlood < 50) {
				rite.setSigilProgress(bloodKey, nodeBlood);
				return SigilOffer.handled(spend);
			}
			rite.setSigilProgress(bloodKey, 0);
			int newCompleted = completed + 1;
			rite.setSigilProgress(progressKey, newCompleted);
			rite.storeReservoirBlood(CardinalRiteSigilRules.nodeCompletionStorageMl(
					sigil.capacityMl(), sigil.nodes().size()), sigil.capacityMl());
			boolean learned = HemoCapabilityAccess.requireIchorianKnowledge(player)
					.recordNode(placement.id(), completed, sigil.nodes().size());
			com.vincenthuto.hemomancy.common.capability.player.harbinger.rite.IchorianKnowledgeEvents.sync(player);
			if (newCompleted >= sigil.nodes().size()) {
				rite.stabilize(sigil.stability());
				if (rite.awakenSigil(progressKey)) {
					spawnAwakenedSigil(level, rite, placement, sigil);
				}
				player.displayClientMessage(Component.literal(learned
								? "The shape resolves: " + sigil.name() + " — " + sigil.purpose()
								: sigil.name() + " answers the rite.")
						.withStyle(ChatFormatting.GOLD), false);
			}
			return SigilOffer.handled(spend);
		}
		return SigilOffer.unhandled();
	}

	private static void spawnCorrectNodeFeedFeedback(ServerLevel level, ActiveCardinalRite rite,
			SigilPlacement placement, IchorianSigilDefinition sigil,
			int nodeIndex, int storedBloodMl) {
		if (nodeIndex < 0 || nodeIndex >= sigil.nodes().size()) return;
		IchorianSigilDefinition.Node node = sigil.nodes().get(nodeIndex);
		BlockPos base = rite.getCenterPos().offset(0, placement.y(), 0);
		BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level, base,
				placement.x() + (int) Math.round(node.x()),
				placement.z() + (int) Math.round(node.z()));
		Vec3 origin = CardinalRiteTargetGeometry.sigilAimPoint(
				rite.getCenterPos(), surface, placement.x(), placement.z(), node.x(), node.z())
				.add(0.0D, 0.07D, 0.0D);
		int color = CardinalRiteAnchorVisualRules.sigilColor(sigil.color());
		ParticleColor particleColor = new ParticleColor(
				(color >> 16) & 255, (color >> 8) & 255, color & 255);
		level.sendParticles(new EmberParticleData(
						particleColor, 0.78F,
						CardinalRiteSigilRules.formingNodeRadius(storedBloodMl), 10),
				origin.x, origin.y, origin.z,
				1, 0.006D, 0.003D, 0.006D, 0.0D);
	}

	private static void spawnFalseStrokeBolts(ServerLevel level, ActiveCardinalRite rite,
			SigilPlacement placement, IchorianSigilDefinition sigil, int nodeIndex) {
		if (nodeIndex < 0 || nodeIndex >= sigil.nodes().size()) return;
		IchorianSigilDefinition.Node node = sigil.nodes().get(nodeIndex);
		BlockPos base = rite.getCenterPos().offset(0, placement.y(), 0);
		BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level, base,
				placement.x() + (int) Math.round(node.x()),
				placement.z() + (int) Math.round(node.z()));
		Vec3 origin = CardinalRiteTargetGeometry.sigilAimPoint(
				rite.getCenterPos(), surface, placement.x(), placement.z(), node.x(), node.z())
				.add(0.0D, 0.06D, 0.0D);
		Vec3 outward = new Vec3(
				origin.x - (rite.getCenterPos().getX() + 0.5D),
				0.0D,
				origin.z - (rite.getCenterPos().getZ() + 0.5D));
		if (outward.lengthSqr() < 1.0E-6D) outward = new Vec3(0.0D, 0.0D, 1.0D);
		outward = outward.normalize();
		Vec3 sideways = new Vec3(-outward.z, 0.0D, outward.x);
		for (int bolt = 0; bolt < 4; bolt++) {
			double reach = 0.28D + level.random.nextDouble() * 0.28D;
			double lateral = (level.random.nextDouble() - 0.5D) * 0.34D;
			double rise = 0.10D + level.random.nextDouble() * 0.28D;
			Vec3 end = origin.add(outward.scale(reach))
					.add(sideways.scale(lateral))
					.add(0.0D, rise, 0.0D);
			long seed = level.random.nextLong() ^ ((long) nodeIndex << 32) ^ bolt;
			LightningTesterSpawner.spawn(level, origin, end,
					new LightningTestConfig(
							LightningTestConfig.Backend.BOLT,
							FALSE_STROKE_OUTER_BLACK,
							FALSE_STROKE_OUTER_BLACK,
							FALSE_STROKE_INNER_PURPLE,
							6.0F, 0.0F, 0.0F, 0.0F,
							48.0F, 2.2F, 5, 4, 0.06F, 0.022F,
							true, seed, false, 20));
		}
	}

	private static void spawnAwakenedSigil(ServerLevel level, ActiveCardinalRite rite,
			SigilPlacement placement, IchorianSigilDefinition sigil) {
		AwakenedIchorianSigilEntity entity = EntityInit.awakened_ichorian_sigil.get().create(level);
		if (entity == null) return;
		BlockPos base = rite.getCenterPos().offset(0, placement.y(), 0);
		BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(
				level, base, placement.x(), placement.z());
		double originX = rite.getCenterPos().getX() + placement.x() + 0.5D;
		double originY = surface.getY() + 0.10D;
		double originZ = rite.getCenterPos().getZ() + placement.z() + 0.5D;
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		float footprintRadius = HarbingerCardinalRiteEvents.ritualFootprintRadius(rite, recipe);
		float orbitRadius = CardinalRiteFootprintRules.awakenedSigilOrbitRadius(footprintRadius);
		float startingAngle = (float) Math.toRadians(Math.floorMod(placement.progressKey().hashCode(), 360));
		entity.initialize(rite.getPlayerUUID(), sigil.id(), rite.getCenterPos(),
				originX, originY, originZ, orbitRadius, startingAngle);
		level.addFreshEntity(entity);
		level.playSound(null, surface, SoundEvents.BEACON_ACTIVATE,
				SoundSource.PLAYERS, 0.8F, 1.35F);
	}

	private static boolean tryBloodletSigil(ServerLevel level, ServerPlayer player,
			ActiveCardinalRite rite, BlockPos target) {
		if (!CardinalRiteBloodlettingRules.canTraceSigil(rite.getDegree(), rite.getPhase())
				|| !hasActionableSigilNodeAt(level, rite, target)) {
			return false;
		}
		if (!CardinalRiteBloodlettingRules.canOffer(player.getHealth())) {
			player.displayClientMessage(Component.literal("The rite will not take your final heart.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return true;
		}
		SigilOffer offer = tryOfferSigilBlood(level, player, rite, target,
				CardinalRiteBloodlettingRules.offeringMl(), requested -> {
					player.setHealth(CardinalRiteBloodlettingRules.healthAfterStroke(player.getHealth()));
					return requested;
				});
		if (offer.spent() > 0) {
			level.playSound(null, target, SoundEvents.PLAYER_HURT,
					SoundSource.PLAYERS, 0.6F, 0.9F);
		}
		return true;
	}

	private record SigilOffer(boolean handled, int spent) {
		private static SigilOffer unhandled() {
			return new SigilOffer(false, 0);
		}

		private static SigilOffer handled(int spent) {
			return new SigilOffer(true, Math.max(0, spent));
		}
	}

	private static boolean hasActionableSigilNodeAt(ServerLevel level, ActiveCardinalRite rite, BlockPos target) {
		for (SigilPlacement placement : activeSigils(level, rite)) {
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(placement.id());
			if (sigil == null || sigil.nodes().isEmpty()) {
				continue;
			}
			int touched = nodeAt(level, rite, placement, sigil, target);
			int completed = rite.getSigilProgress().getOrDefault(placement.progressKey(), 0);
			if (CardinalRiteSigilRules.isActionableNode(
					touched, completed, sigil.nodes().size())) {
				return true;
			}
		}
		return false;
	}

	private static int spendBlood(ServerPlayer player, ActiveCardinalRite rite, int requested) {
		int ichor = rite.consumeCarriedIchor(requested);
		int personalRequest = requested - ichor;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		int personal = volume == null ? 0
				: Math.min(personalRequest, (int) Math.floor(volume.getBloodVolume()));
		if (personal > 0) {
			volume.drain(personal);
			BloodVolumeEvents.syncVolume(player, volume);
		}
		int shared = CardinalRiteAllyService.spend(player.serverLevel(), rite, player.getUUID(),
				personalRequest - personal);
		return ichor + personal + shared;
	}

	private static double tryProjectFalseOmen(ServerLevel level, ServerPlayer player, ActiveCardinalRite rite,
			BlockPos target, double projectionRate) {
		if (rite.getPhase() != CardinalRitePhase.ORDEAL
				|| rite.getCurrentWave() >= rite.getWaveDeck().size()
				|| !"false_omens".equals(rite.getWaveDeck().get(rite.getCurrentWave()))) return 0.0D;
		BlockPos[] falseAnchors = {
				rite.getCenterPos().offset(2, 1, 2),
				rite.getCenterPos().offset(-2, 1, -2),
				rite.getCenterPos().offset(2, 1, -2)
		};
		for (BlockPos falseAnchor : falseAnchors) {
			if (!target.closerThan(falseAnchor, 1.4D) && !target.closerThan(falseAnchor.below(), 1.4D)) continue;
			int spent = spendBlood(player, rite, Math.max(1, Math.min(50, (int) projectionRate)));
			if (spent > 0) {
				rite.addInstability(6);
				player.displayClientMessage(Component.literal("The counterfeit anchor drinks the projection.")
						.withStyle(ChatFormatting.DARK_PURPLE), true);
			}
			return spent;
		}
		return 0.0D;
	}

	static List<SigilPlacement> activeSigils(ServerLevel level, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null || recipe.getCeremony() == null) return List.of();
		List<SigilPlacement> placements = new ArrayList<>();
		List<SigilPlacement> supportPlacements = supportSigils(recipe);
		if (rite.getPhase() == CardinalRitePhase.INSCRIPTION
				|| rite.getPhase() == CardinalRitePhase.ORDEAL
				|| rite.getPhase() == CardinalRitePhase.STILL_INTERVAL) {
			placements.addAll(supportPlacements);
		}
		if (rite.getPhase() == CardinalRitePhase.ORDEAL) {
			ResourceLocation response = sigilForWave(rite);
			if (response != null) {
				String wave = rite.getCurrentWave() < rite.getWaveDeck().size()
						? rite.getWaveDeck().get(rite.getCurrentWave()) : "";
				SigilPlacement wavePlacement = resolveWaveSigilPlacement(
						response, wave.startsWith("discover_"), rite.getCurrentWave(),
						supportPlacements, recipe);
				if (!placements.contains(wavePlacement)) placements.add(wavePlacement);
			}
		}
		placements.removeIf(placement -> rite.isSigilAwakened(placement.progressKey()));
		return placements;
	}

	static SigilPlacement resolveWaveSigilPlacement(ResourceLocation id, boolean discovery, int wave,
			List<SigilPlacement> supportPlacements, CardinalRiteRecipe recipe) {
		String progressKey = discovery ? id.toString() : responseProgressKey(wave, id);
		if (discovery) {
			int matchingIndex = CardinalRiteSigilPlacementRules.matchingSigilIndex(
					id, supportPlacements.stream().map(SigilPlacement::id).toList());
			if (matchingIndex >= 0) return supportPlacements.get(matchingIndex);
		}
		IchorianSigilDefinition sigil = IchorianSigilRegistry.get(id);
		if (sigil == null || recipe == null || recipe.getCeremony() == null) {
			return new SigilPlacement(id, 0, 0, 0, progressKey);
		}
		BlockPos resolved = resolveResponseSigilPlacement(
				recipe.getCeremony().anchors(), supportPlacements, sigil);
		return new SigilPlacement(id, resolved.getX(), 0, resolved.getZ(), progressKey);
	}

	static List<BlockPos> resolvedResponseSigilPoints(
			List<CardinalRiteCeremonyDefinition.Anchor> anchors,
			List<SigilPlacement> supportPlacements, IchorianSigilDefinition sigil) {
		if (sigil == null) return List.of();
		return List.copyOf(CardinalRiteSigilPlacementRules.resolvedFootprint(
				BlockPos.ZERO, sigil.nodes(), occupiedSigilTargets(anchors, supportPlacements)));
	}

	private static BlockPos resolveResponseSigilPlacement(
			List<CardinalRiteCeremonyDefinition.Anchor> anchors,
			List<SigilPlacement> supportPlacements, IchorianSigilDefinition sigil) {
		return CardinalRiteSigilPlacementRules.resolveNearestPlacement(
				BlockPos.ZERO, sigil.nodes(), occupiedSigilTargets(anchors, supportPlacements));
	}

	private static Set<BlockPos> occupiedSigilTargets(
			List<CardinalRiteCeremonyDefinition.Anchor> anchors,
			List<SigilPlacement> supportPlacements) {
		Set<BlockPos> occupied = new HashSet<>();
		for (var anchor : anchors) {
			occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
		}
		for (SigilPlacement placement : supportPlacements) {
			IchorianSigilDefinition support = IchorianSigilRegistry.get(placement.id());
			if (support == null) continue;
			occupied.addAll(CardinalRiteSigilPlacementRules.footprint(
					new BlockPos(placement.x(), 0, placement.z()), support.nodes()));
		}
		return occupied;
	}

	static List<SigilPlacement> supportSigils(CardinalRiteRecipe recipe) {
		if (recipe == null || recipe.getCeremony() == null) return List.of();
		Set<BlockPos> occupied = new HashSet<>();
		for (var anchor : recipe.getCeremony().anchors()) {
			occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
		}

		List<SigilPlacement> placements = new ArrayList<>();
		for (var socket : recipe.getCeremony().supportSockets()) {
			ResourceLocation id = Hemomancy.rloc(socket.suggestedSigil());
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(id);
			BlockPos requested = new BlockPos(socket.x(), 0, socket.z());
			BlockPos resolved = sigil == null ? requested
					: CardinalRiteSigilPlacementRules.resolveSupportPlacement(
							requested, sigil.nodes(), occupied);
			if (sigil != null) {
				occupied.addAll(CardinalRiteSigilPlacementRules.footprint(resolved, sigil.nodes()));
			}
			placements.add(new SigilPlacement(
					id, resolved.getX(), socket.y(), resolved.getZ(), id.toString()));
		}
		return placements;
	}

	public static ResourceLocation sigilForWave(ActiveCardinalRite rite) {
		if (rite.getCurrentWave() >= rite.getWaveDeck().size()) return null;
		String wave = rite.getWaveDeck().get(rite.getCurrentWave());
		if (wave.startsWith("discover_")) return Hemomancy.rloc(wave.substring("discover_".length()));
		if ("response_sigil".equals(wave)) {
			String[] ids = { "suture", "shunt", "seal", "cage", "lens" };
			return Hemomancy.rloc(ids[Math.floorMod(rite.getCurrentWave() + rite.getDegree(), ids.length)]);
		}
		return null;
	}

	private static int nodeAt(ServerLevel level, ActiveCardinalRite rite, SigilPlacement placement,
			IchorianSigilDefinition sigil, BlockPos target) {
		List<BlockPos> positions = new ArrayList<>(sigil.nodes().size());
		for (IchorianSigilDefinition.Node node : sigil.nodes()) {
			BlockPos base = rite.getCenterPos().offset(0, placement.y(), 0);
			positions.add(CardinalRiteSigilRules.surfaceAirPosition(
					level, base,
					placement.x() + (int) Math.round(node.x()),
					placement.z() + (int) Math.round(node.z())));
		}
		return CardinalRiteSigilRules.closestNodeIndex(positions, target, 1.4D);
	}

	public static String responseProgressKey(int wave, ResourceLocation id) {
		return CardinalRiteSigilRules.responseProgressKey(wave, id);
	}

	public static String currentWaveSigilProgressKey(ActiveCardinalRite rite, ResourceLocation id) {
		if (rite.getCurrentWave() < rite.getWaveDeck().size()
				&& rite.getWaveDeck().get(rite.getCurrentWave()).startsWith("discover_")) return id.toString();
		return responseProgressKey(rite.getCurrentWave(), id);
	}

	record SigilPlacement(ResourceLocation id, int x, int y, int z, String progressKey) {
	}

	private record ProjectionTarget(BlockPos interactionPos, Vec3 aimPoint) {
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getLevel() instanceof ServerLevel level)
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| !event.getItemStack().isEmpty()) {
			return;
		}
		CardinalRiteSavedData data = CardinalRiteSavedData.get(level);
		for (ActiveCardinalRite candidate : data.getActiveRites().values()) {
			if (CardinalRiteAllyService.tryClaimPlayerRole(level, player, candidate, event.getPos())) {
				data.setDirty();
				consume(event);
				return;
			}
		}
		ActiveCardinalRite rite = data.getRite(player.getUUID());
		if (rite == null) return;

		if (CardinalRiteBloodlettingRules.canRepairBoundaryDirectly(rite.getDegree())) {
			int repairAnchor = anchorAt(level, rite, event.getPos());
			if (repairAnchor >= 0 && rite.instabilityRepairBloodNeeded(repairAnchor) > 0) {
				if (player.getHealth() <= 2.0F) {
					player.displayClientMessage(Component.literal("The rite will not take your final heart.")
							.withStyle(ChatFormatting.DARK_RED), true);
					consume(event);
					return;
				}
				player.setHealth(Math.max(2.0F, player.getHealth() - 2.0F));
				rite.offerInstabilityRepair(repairAnchor,
						CardinalRiteInstabilityBoundaryRules.REPAIR_BLOOD_ML);
				data.setDirty();
				level.playSound(null, event.getPos(), SoundEvents.RESPAWN_ANCHOR_CHARGE,
						SoundSource.PLAYERS, 0.7F, 0.72F);
				player.displayClientMessage(Component.literal("Your circulation sutures the damaged boundary.")
						.withStyle(ChatFormatting.DARK_RED), true);
				consume(event);
				return;
			}
		}

		if (rite.getPhase() == CardinalRitePhase.CONSECRATION && rite.getDegree() == 1) {
			int anchor = anchorAt(level, rite, event.getPos());
			if (anchor < 0 || rite.bloodNeededForAnchor(anchor) <= 0) return;
			if (player.getHealth() <= 2.0F) {
				player.displayClientMessage(Component.literal("The rite will not take your final heart.")
						.withStyle(ChatFormatting.DARK_RED), true);
				consume(event);
				return;
			}
			player.setHealth(Math.max(2.0F, player.getHealth() - 2.0F));
			rite.fillAnchor(anchor, 50);
			if (rite.areAnchorsConsecrated()) {
				rite.enterInscription();
				player.displayClientMessage(Component.literal(
						"Your first circulation closes. Project into the daemon.")
						.withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
			}
			data.setDirty();
			level.playSound(null, event.getPos(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.6F, 0.75F);
			consume(event);
			return;
		}

		if (tryBloodletSigil(level, player, rite, event.getPos())) {
			data.setDirty();
			consume(event);
			return;
		}

	}

	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (!(event.getLevel() instanceof ServerLevel level)
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| !event.getItemStack().isEmpty()) return;
		CardinalRiteSavedData data = CardinalRiteSavedData.get(level);
		ActiveCardinalRite rite = data.getRite(player.getUUID());
		if (rite != null && CardinalRiteAllyService.tryAssignNpc(level, player, rite, event.getTarget())) {
			data.setDirty();
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	private static boolean mayParticipate(ServerPlayer player, ActiveCardinalRite rite) {
		return rite.getPlayerUUID().equals(player.getUUID()) || rite.getAllyRoles().containsKey(player.getUUID());
	}

	private static int anchorAt(ServerLevel level, ActiveCardinalRite rite, BlockPos target) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null || recipe.getCeremony() == null) return -1;
		return CardinalRiteTargetGeometry.nearestAnchorIndex(
				CardinalRiteAnchorVisualRules.riteSurface(rite.getCenterPos()),
				recipe.getCeremony().anchors().stream()
						.map(com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition.Anchor::offset)
						.toList(),
				target, 1.6D);
	}

	private static void prepareWaveDeck(ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		rite.getWaveDeck().clear();
		rite.getWaveDeck().addAll(recipe.getCeremony().guaranteedWaves());
		List<String> shuffled = new ArrayList<>(recipe.getCeremony().waves());
		Collections.shuffle(shuffled);
		for (String wave : shuffled) {
			if (rite.getWaveDeck().size() >= rite.getTotalWaves()) break;
			rite.getWaveDeck().add(wave);
		}
		while (rite.getWaveDeck().size() < rite.getTotalWaves()) {
			rite.getWaveDeck().add("bloodlicker_siphon");
		}
	}

	static boolean sealMediumReady(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		boolean ready = CardinalRiteStationMatcher.mediumMatches(level, rite.getCenterPos(), recipe);
		if (!ready) {
			ServerPlayer caster = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
			if (caster != null) caster.displayClientMessage(
					Component.literal(recipe.hasMedium()
							? "The rite's medium must remain seated in the Cardinal Focus."
							: "The Cardinal Focus must be empty before this altar can be sealed.")
							.withStyle(ChatFormatting.DARK_RED), false);
		}
		return ready;
	}

	private static void consume(PlayerInteractEvent.RightClickBlock event) {
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}
}
