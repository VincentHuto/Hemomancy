package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRites;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.rite.sigil.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * What clients see of the active Cardinal Rites in a level: each rite's boundary, anchors, sigils, checklist and
 * guidance, sent every half second and on every change by {@link HarbingerCardinalRiteEvents}. Only the caster and
 * its allies see the bloodline pool a rite can draw on.
 */
final class CardinalRiteSync {
	/** A full anchor's orb sits half sunk in its sore, just above the floor block's top. */
	private static final double ANCHOR_ORB_Y = 1.06D;

	private CardinalRiteSync() {
	}

	static PacketSyncActiveRites activeRiteSnapshot(ServerLevel level, ServerPlayer viewer) {
		List<ActiveRiteData.RiteEntry> entries = new ArrayList<>();
		for (ActiveCardinalRite rite : CardinalRiteSavedData.get(level).getActiveRites().values()) {
			entries.add(toClientEntry(level, rite, mayReadPool(rite, viewer)));
		}
		return new PacketSyncActiveRites(entries);
	}

	/**
	 * Every viewer sees every rite, but only the caster and its allies see the
	 * bloodline pool the rite can draw on.
	 */
	static void syncActiveRites(ServerLevel level) {
		List<ActiveCardinalRite> rites = new ArrayList<>(CardinalRiteSavedData.get(level).getActiveRites().values());
		List<ActiveRiteData.RiteEntry> shared = new ArrayList<>(rites.size());
		for (ActiveCardinalRite rite : rites) shared.add(toClientEntry(level, rite, false));
		ActiveRiteData.RiteEntry[] revealed = new ActiveRiteData.RiteEntry[rites.size()];
		PacketSyncActiveRites sharedPacket = new PacketSyncActiveRites(shared);
		for (ServerPlayer viewer : level.players()) {
			List<ActiveRiteData.RiteEntry> entries = null;
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

	private static ActiveRiteData.RiteEntry toClientEntry(ServerLevel level, ActiveCardinalRite rite,
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
		float footprintRadius = CardinalRiteFootprintResolver.radius(rite, recipe);
		java.util.List<Component> checklist = buildChecklist(level, rite, recipe);
		int stillIntervalTicks = recipe == null || recipe.getCeremony() == null ? 0
				: recipe.getCeremony().stillIntervalTicks();
		var atmosphere = recipe == null || recipe.getCeremony() == null
				? null : recipe.getCeremony().atmosphere();
		String fogProfile = atmosphere == null ? (unstained ? "none" : "storm") : atmosphere.fog();
		boolean fogLightning = atmosphere == null ? !unstained : atmosphere.lightning();
		boolean boundaryDome = atmosphere == null ? !unstained : atmosphere.dome();
        boolean succession = com.vincenthuto.hemomancy.common.succession.SuccessionRites.is(rite);
        int successionTicks = rite.succession().ticks();
        if (succession) {
            cue = "hemomancy.succession.phase." + Math.min(5, successionTicks / 200);
            checklist = java.util.List.of(Component.translatable(cue));
            upfront = 0;
        }
		return new ActiveRiteData.RiteEntry(
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

	private static Component sacrificeLine(ActiveCardinalRite rite) {
		return Component.translatable("message.hemomancy.cardinal_rite_sync.sacrifice_ml_from_a_named_creature",
				Math.min(rite.getSacrificeMl(), CardinalRiteThreatRules.SACRIFICE_REQUIRED_ML),
				CardinalRiteThreatRules.SACRIFICE_REQUIRED_ML);
	}

	/** Each anchor's blood, flagged when instability has broken it, for the floor's sores and jets. */
	private static int[] anchorStates(ActiveCardinalRite rite) {
		int[] blood = rite.getAnchorBloodMl();
		int[] states = new int[blood.length];
		int[] required = rite.anchorRequirements();
		for (int i = 0; i < blood.length; i++) {
			states[i] = Math.clamp(blood[i], 0, required[i])
					+ (rite.isInstabilityDamagedAnchor(i) ? ActiveRiteData.RiteEntry.ANCHOR_DAMAGED : 0);
		}
		return states;
	}


    private static Component nextAnchorRepair(ActiveCardinalRite rite) {
        int[] blood = rite.getAnchorBloodMl();
        for (int index = 0; index < blood.length; index++) {
            int repair = rite.instabilityRepairBloodNeeded(index);
            if (repair > 0 || rite.bloodNeededForAnchor(index) > 0) {
                return Component.translatable("message.hemomancy.cardinal_rite_sync.anchor_repair_blood", index + 1,
                        repair, Math.clamp(blood[index], 0, rite.anchorRequirementMl(index)),
                        rite.anchorRequirementMl(index));
            }
        }
        return Component.translatable("message.hemomancy.cardinal_rite_sync.all_anchors_restored");
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

	private static java.util.List<Component> buildChecklist(ServerLevel level, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (rite.getCancellationTicks() > 0) {
			int remaining = CardinalRiteCancellationRules.TOTAL_TICKS - rite.getCancellationTicks();
			return java.util.List.of(
					Component.translatable("message.hemomancy.cardinal_rite_sync.absorbing_the_rite",
							Math.max(0, remaining + 19) / 20),
					Component.translatable("message.hemomancy.cardinal_rite_sync.keep_blood_absorption_trained_on_the_planted_staff"));
		}
		if (rite.getPhase() == CardinalRitePhase.CONSECRATION) {
			int missing = 0;
			for (int i = 0; i < rite.getAnchorBloodMl().length; i++) {
				if (rite.bloodNeededForAnchor(i) > 0) missing++;
			}
			if (CardinalRiteThreatRules.allowsPassiveSacrifice(rite.getRecipeId().getPath())) {
				return java.util.List.of(
						Component.translatable("message.hemomancy.cardinal_rite_sync.boundary_rings",
								rite.completedRings(), rite.totalAnchorRings()),
						Component.translatable("message.hemomancy.cardinal_rite_sync.anchors_remaining", missing),
						sacrificeLine(rite),
						Component.translatable("message.hemomancy.cardinal_rite_sync.bleed_a_named_creature_inside_the_circle"));
			}
			return java.util.List.of(
					Component.translatable("message.hemomancy.cardinal_rite_sync.boundary_rings",
							rite.completedRings(), rite.totalAnchorRings()),
					Component.translatable("message.hemomancy.cardinal_rite_sync.anchors_remaining", missing),
                    nextAnchorRepair(rite),
					Component.translatable("message.hemomancy.cardinal_rite_sync.project_blood_into_the_glowing_boundary_anchors"));
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
            List<Component> lines = new ArrayList<>(CardinalRiteChecklist.inscription(optional, optionalComplete,
                    required, requiredComplete, availableAllies, requiredAllies, mediumReady));
            if (CardinalRiteThreatRules.allowsPassiveSacrifice(rite.getRecipeId().getPath())) {
                lines.add(0, sacrificeLine(rite));
            }
            return List.copyOf(lines);
		}
		if (rite.getPhase() == CardinalRitePhase.SUBJECT_FEEDING) {
			int required = rite.craft().feedRequired();
			return java.util.List.of(
					Component.translatable("message.hemomancy.cardinal_rite_sync.structure_fed",
							required - rite.subjectFeedNeeded(), required),
					Component.translatable("message.hemomancy.cardinal_rite_sync.project_blood_into_the_structure_beside_the_focus"),
					Component.translatable("message.hemomancy.cardinal_rite_sync.the_working_floor_climbs_it_as_it_fills"));
		}
		if (rite.getPhase() == CardinalRitePhase.STATION_PROJECTION) {
			int stage = rite.upgrade().stage();
			int cost = StationUpgradeRites.bloodPerCircuit(rite.getRecipeId());
			return java.util.List.of(Component.translatable("message.hemomancy.cardinal_rite_sync.station_circuits",
							stage, StationUpgradeRites.circuits(rite.getRecipeId())),
					Component.translatable("message.hemomancy.cardinal_rite_sync.current_circuit",
							cost - rite.upgradeBloodNeeded(), cost),
					Component.translatable("message.hemomancy.cardinal_rite_sync.project_blood_into_the_next_station_circuit"));
		}
		if (rite.getPhase() == CardinalRitePhase.ORDEAL) {
			String wave = rite.getCurrentWave() < rite.getWaveDeck().size()
					? rite.getWaveDeck().get(rite.getCurrentWave()) : "";
			long dry = java.util.stream.IntStream.range(0, rite.getAnchorBloodMl().length)
					.filter(i -> rite.bloodNeededForAnchor(i) > 0).count();
			return java.util.List.of(
					Component.translatable("message.hemomancy.cardinal_rite_sync.wave",
							Math.min(rite.getCurrentWave() + 1, rite.getTotalWaves()), rite.getTotalWaves()),
					CardinalRiteChecklist.ordealObjective(wave),
					Component.translatable("message.hemomancy.cardinal_rite_sync.damaged_anchors",
							rite.getBrokenInstabilityAnchors().size()),
					Component.translatable("message.hemomancy.cardinal_rite_sync.dry_anchors", dry), nextAnchorRepair(rite));
		}
		if (rite.getPhase() == CardinalRitePhase.STILL_INTERVAL && rite.isHeldBeforeFinale()) {
			return java.util.List.of(
					Component.translatable("message.hemomancy.cardinal_rite_sync.the_rite_waits_before_its_finale"),
					Component.translatable("message.hemomancy.cardinal_rite_sync.resolve_what_it_lacks_or_recall_it"));
		}
		if (rite.getPhase() == CardinalRitePhase.STILL_INTERVAL) {
			int duration = recipe == null || recipe.getCeremony() == null ? 0
					: recipe.getCeremony().stillIntervalTicks();
			int seconds = Math.max(0, duration - rite.getPhaseTicks() + 19) / 20;
			return java.util.List.of(
					Component.translatable("message.hemomancy.cardinal_rite_sync.still_interval", seconds),
					Component.translatable("message.hemomancy.cardinal_rite_sync.repair_damaged_anchors",
							rite.getBrokenInstabilityAnchors().size()),
					nextAnchorRepair(rite),
                    Component.translatable("message.hemomancy.cardinal_rite_sync.reservoir_replaces_siphoned_blood_only",
                            rite.getReservoirBloodMl()));
		}
		if (rite.getPhase() == CardinalRitePhase.OFFERING_PROCESSION) {
			int total = rite.getOfferingItinerary().size();
			return java.util.List.of(
					Component.translatable("message.hemomancy.cardinal_rite_sync.offerings_drawn",
							Math.min(rite.getOfferingVisitIndex(), total), total),
					Component.translatable("message.hemomancy.cardinal_rite_sync.the_floor_draws_each_burning_offering"));
		}
		if (rite.getPhase() == CardinalRitePhase.CULMINATION) {
			return java.util.List.of(
					Component.translatable(rite.getPhaseTicks() <= CardinalRiteFinaleTiming.GROWTH_TICKS
							? "message.hemomancy.cardinal_rite_sync.the_daemon_gathers_as_the_offerings_fade"
							: "message.hemomancy.cardinal_rite_sync.the_daemon_is_returning_to_your_blood"),
					Component.translatable("message.hemomancy.cardinal_rite_sync.no_further_input_is_required"));
		}
		return java.util.List.of();
	}

	private static java.util.List<ActiveRiteData.SanguineBlob> visibleSanguineBlobs(
			ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		java.util.List<ActiveRiteData.SanguineBlob> result = new java.util.ArrayList<>();
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
				result.add(new ActiveRiteData.SanguineBlob(
						x, y, z, radius, color,
						blobSeed(x, y, z, i), integrity,
						ActiveRiteData.NodeKind.BOUNDARY_ANCHOR,
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
				result.add(new ActiveRiteData.SanguineBlob(
						x, y, z, 0.16F, CardinalRiteAnchorVisualRules.sigilColor(sigil.color()),
						blobSeed(x, y, z, i), 1.0F,
						ActiveRiteData.NodeKind.SIGIL_NODE,
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
				result.add(new ActiveRiteData.SanguineBlob(
						x, y, z, CardinalRiteSigilRules.formingNodeRadius(nodeBlood),
						CardinalRiteAnchorVisualRules.sigilColor(sigil.color()),
						blobSeed(x, y, z, completed), 1.0F,
						ActiveRiteData.NodeKind.SIGIL_NODE,
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

	private static java.util.List<ActiveRiteData.SigilSegment> visibleSigilSegments(
			ServerLevel level, ActiveCardinalRite rite) {
		java.util.List<ActiveRiteData.SigilSegment> result = new java.util.ArrayList<>();
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

	private static ActiveRiteData.SigilSegment toClientSigilSegment(
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
		return new ActiveRiteData.SigilSegment(
				rite.getCenterPos().getX() + 0.5D + placement.x() + start.x(),
				startSurface.getY() + 0.10D,
				rite.getCenterPos().getZ() + 0.5D + placement.z() + start.z(),
				rite.getCenterPos().getX() + 0.5D + placement.x() + end.x(),
				endSurface.getY() + 0.10D,
				rite.getCenterPos().getZ() + 0.5D + placement.z() + end.z(),
				color);
	}
}
