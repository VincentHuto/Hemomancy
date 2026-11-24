package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleRules;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketCardinalRiteStaffPlanting;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMediumRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.structure.BloodCraftingPatternBlockRules;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hemomancy.common.util.PatternOrigins;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

import java.util.List;
import java.util.UUID;

/**
 * Starts a Cardinal Rite from the block a player activated: finds the rite its floor, structure and braziers name,
 * checks every gate, builds the rite of its {@link RiteKind}, takes what it escrows and plants the Living Staff.
 * Reached from the Living Staff, the Sanguine Formation and (for Unstained rites) the blood crafting key.
 */
public final class CardinalRiteActivation {
	private static final ResourceLocation FOUNDING_FANE_RITE_ID = Hemomancy.rloc("cardinal_rite/founding_fane");
	private static final ResourceLocation APOTHEOS_RITE_ID = Hemomancy.rloc("cardinal_rite/apotheos_rite");
	private static final ResourceLocation HEMATIC_UNBINDING_RITE_ID = Hemomancy.rloc("cardinal_rite/hematic_unbinding");
	private static final ResourceLocation ANCESTRAL_COMMUNION_RITE_ID = Hemomancy.rloc("cardinal_rite/ancestral_communion");
	private static final ResourceLocation SPORITIC_THURIBLE_RITE_ID = Hemomancy.rloc("cardinal_rite/working/sporitic_thurible");

	private CardinalRiteActivation() {
	}

	/** One check before a rite may start, in {@link #GATES} order; a gate that refuses tells the player why. */
	private interface Gate {
		boolean admits(Attempt attempt);
	}

	/** A rite whose station the player activated, and the block its rite will centre on. */
	private record Attempt(ServerLevel level, ServerPlayer player, CardinalRiteRecipe recipe, BlockPos center) {
		IBloodVolume volume() {
			return HemoCapabilityAccess.getBloodVolume(player);
		}
	}

	/**
	 * Every check a matched rite passes before anything is escrowed: first the caster (one rite at a time, blood,
	 * Degree, path and the rites that ask more of who casts them), then the station (free, seated, able to take the
	 * rite's result, and the rites that ask to be confirmed).
	 */
	private static final List<Gate> GATES = List.of(
			CardinalRiteActivation::noRiteElsewhere,
			CardinalRiteActivation::bloodIsActive,
			CardinalRiteActivation::degreeIsMet,
			CardinalRiteActivation::rankRiteIsOpen,
			CardinalRiteActivation::unstainedLevelIsMet,
			CardinalRiteActivation::unstainedPreflightPasses,
			CardinalRiteActivation::notBarredByPurification,
			CardinalRiteActivation::apotheosIsUnsealed,
			CardinalRiteActivation::hiveStillAnswers,
			CardinalRiteActivation::thuribleIsAnApotheosRelic,
			CardinalRiteActivation::bloodCoversATimedRite,
			CardinalRiteActivation::pathPermitsTheRite,
			CardinalRiteActivation::stationIsFree,
			CardinalRiteActivation::mediumIsSeated,
			CardinalRiteActivation::seedAnswersTheCaster,
			CardinalRiteActivation::puppetMayBegin,
			CardinalRiteActivation::faneRelocationIsConfirmed,
			CardinalRiteActivation::resultCanTakeHold,
			CardinalRiteActivation::unbindingIsConfirmed);

	public static CardinalRiteActivationRules.ActivationAttempt tryStart(Player player, BlockPos hitPos,
			CardinalRiteActivationRules.Trigger trigger) {
		ServerLevel sLevel = (ServerLevel) player.level();
		ServerPlayer serverPlayer = (ServerPlayer) player;
		var hitState = sLevel.getBlockState(hitPos);
		List<CardinalRiteRecipe> allRites = CardinalRiteRecipe.getAllRecipes(player.level());
		CardinalRiteStationMatcher.Resolution layeredResolution = CardinalRiteStationMatcher.resolve(
				sLevel, hitPos, allRites.stream().filter(recipe ->
						CardinalRiteActivationRules.mayInitiate(
								trigger, recipe.isUnstained(), RecipeDegreeGates.getRequiredDegree(recipe),
								focusMode(recipe))
								&& RecipeDegreeGates.playerMayAttempt(player, recipe))
						.toList());
		if (layeredResolution.status() == CardinalRiteStationMatcher.Status.AMBIGUOUS_FLOOR
				|| layeredResolution.status() == CardinalRiteStationMatcher.Status.AMBIGUOUS_RITE) {
			player.displayClientMessage((layeredResolution.status()
							== CardinalRiteStationMatcher.Status.AMBIGUOUS_FLOOR ? Component.translatable("message.hemomancy.cardinal_rite_activation.the_cardinal_focus_answers_to_more") : Component.translatable("message.hemomancy.cardinal_rite_activation.the_offerings_answer_to_more_than")).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
			return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
		}
		CardinalRiteStationMatcher.ResolvedRecipe layeredResolved =
				layeredResolution.status() == CardinalRiteStationMatcher.Status.MATCHED
						? layeredResolution.matches().get(0) : null;

		for (CardinalRiteRecipe recipe : allRites) {
			if (!CardinalRiteActivationRules.mayInitiate(
					trigger, recipe.isUnstained(), RecipeDegreeGates.getRequiredDegree(recipe),
					focusMode(recipe))) {
				continue;
			}
			if (recipe.hasLayeredStation()
					&& (layeredResolved == null || layeredResolved.recipe() != recipe)) continue;
			CardinalRiteStationMatcher.StationMatch stationMatch =
					recipe.hasLayeredStation() ? layeredResolved.station() : null;
			if (recipe.hasLayeredStation() && stationMatch == null) continue;
			if (!recipe.hasLayeredStation()
					&& !BloodCraftingPatternBlockRules.patternMayContainBlock(recipe.getPattern(), hitState)) continue;
			BlockPattern.BlockPatternMatch match = recipe.hasLayeredStation()
					? stationMatch.floorMatch()
					: PatternOrigins.findContaining(recipe.getPattern().getBlockPattern(), sLevel, hitPos);
			if (match == null) continue;
			if (!recipe.hasLayeredStation() && trigger != CardinalRiteActivationRules.Trigger.BLOOD_CRAFTING_KEY) {
				CardinalRiteActivationRules.Cell activation =
						CardinalRiteActivationRules.activationCell(recipe.getPattern().getPatternArray());
				if (activation == null) continue;
				BlockPos activationPos = match.getBlock(activation.x(), activation.y(), activation.z()).getPos();
				if (!activationPos.equals(hitPos)) {
					pointToActivationCell(player, sLevel, trigger, activationPos);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
			}
			Attempt attempt = new Attempt(sLevel, serverPlayer, recipe, centerOf(recipe, hitPos, match));
			for (Gate gate : GATES) {
				if (!gate.admits(attempt)) return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
			}
			return start(attempt, stationMatch);
		}
		if (hitState.is(BlockInit.cardinal_focus.get())) {
			explainUnansweredFloor(sLevel, hitPos, player, trigger, allRites, layeredResolution.status());
			return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
		}
		return CardinalRiteActivationRules.ActivationAttempt.NOT_HANDLED;
	}

	private static void pointToActivationCell(Player player, ServerLevel level,
			CardinalRiteActivationRules.Trigger trigger, BlockPos activationPos) {
		Component activator = Component.translatable(
				trigger == CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE
						? "message.hemomancy.cardinal_rite_activation.the_living_staff_points"
						: "message.hemomancy.cardinal_rite_activation.the_sanguine_formation_draws");
		player.displayClientMessage(
				HemoText.translatable("message.hemomancy.cardinal_rite_activation.toward_the_rite_s_central",
						activator,
						level.getBlockState(activationPos).getBlock().getName()
								.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
						Component.literal(".").withStyle(ChatFormatting.DARK_RED)).withStyle(ChatFormatting.DARK_RED),
				true);
	}

	/** A layered rite centres on its Focus; an older patterned rite on the middle of its pattern. */
	private static BlockPos centerOf(CardinalRiteRecipe recipe, BlockPos hitPos, BlockPattern.BlockPatternMatch match) {
		if (recipe.hasLayeredStation()) return hitPos;
		BlockPattern pattern = recipe.getPattern().getBlockPattern();
		return match.getBlock(pattern.getWidth() / 2, pattern.getHeight() / 2, pattern.getDepth() / 2).getPos();
	}

	// ---- Gates, in GATES order ----

	private static boolean noRiteElsewhere(Attempt a) {
		if (!CardinalRiteSavedData.hasActiveRiteAnywhere(a.level().getServer(), a.player().getUUID())) return true;
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.a_rite_is_already_in_progress").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				true);
		return false;
	}

	private static boolean bloodIsActive(Attempt a) {
		if (a.recipe().isUnstained() || RecipeDegreeGates.getRequiredDegree(a.recipe()) <= 0 || a.volume().isActive()) {
			return true;
		}
		a.player().displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.your_blood_must_be_active_to").withStyle(ChatFormatting.DARK_RED), false);
		return false;
	}

	private static boolean degreeIsMet(Attempt a) {
		if (a.recipe().isUnstained()) return true;
		int requiredDegree = RecipeDegreeGates.getRequiredDegree(a.recipe());
		if (HemoCapabilityAccess.getPlayerDegreeNumber(a.player()) >= requiredDegree) return true;
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.this_rite_requires",
						RecipeDegreeGates.degreeLabel(requiredDegree)
								.withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)).withStyle(ChatFormatting.RED),
				false);
		return false;
	}

	/** A rank rite is open only below its Degree and once its chapter is complete. */
	private static boolean rankRiteIsOpen(Attempt a) {
		if (a.recipe().isUnstained() || !a.recipe().isRankup()) return true;
		Integer targetDegree = RecipeDegreeGates.getRankupTargetDegree(a.recipe());
		if (targetDegree == null) return true;
		int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(a.player());
		if (playerDegree >= targetDegree) {
			sayAlreadyAttained(a.player(), playerDegree);
			return false;
		}
		HarbingerChapterMilestone unmet = HarbingerChapterProgression.unmetChapterForTargetDegree(a.player(), targetDegree);
		if (unmet == null) return true;
		a.player().displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.the_rank_rite_remains_sealed_complete",
				unmet.chapterName().copy()
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				Component.translatable("message.hemomancy.cardinal_rite_activation.first").withStyle(ChatFormatting.DARK_RED)).withStyle(ChatFormatting.DARK_RED), false);
		return false;
	}

	private static boolean unstainedLevelIsMet(Attempt a) {
		if (!a.recipe().isUnstained() || RecipeDegreeGates.playerMeets(a.player(), a.recipe())
				|| UnstainedAccessRules.bypassesUnstainedLevelGate(a.recipe().getId().getPath(),
						HemoCapabilityAccess.getUnstainedProgress(a.player()))) {
			return true;
		}
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.this_rite_demands",
						RecipeDegreeGates.requirementLabel(a.recipe())
								.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
						Component.translatable("message.hemomancy.cardinal_rite_activation.unstained_requirement").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
				false);
		return false;
	}

	private static boolean unstainedPreflightPasses(Attempt a) {
		if (!a.recipe().isUnstained()) return true;
		var preflight = UnstainedCardinalRiteEvents.preflight(a.player(), a.recipe().getId().getPath());
		if (preflight.success()) return true;
		UnstainedCardinalRiteEvents.announceFailure(a.player(), preflight.failure());
		return false;
	}

	private static boolean notBarredByPurification(Attempt a) {
		if (a.recipe().isUnstained()
				|| !UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(a.player()))) {
			return true;
		}
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.purification_bars_access_to_the_hematic").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
				false);
		return false;
	}

	/** The Rite of Apotheos waits on a completed Qliphoth Communion. */
	private static boolean apotheosIsUnsealed(Attempt a) {
		if (!APOTHEOS_RITE_ID.equals(a.recipe().getId())
				|| HemoCapabilityAccess.getInitiatoryDegree(a.player()).isQliphothCommunionDone()) {
			return true;
		}
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.the_eighth_degree_remains_sealed_consume").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
				false);
		return false;
	}

	/** Ancestral Communion speaks with the hive, which no longer answers a Silentium. */
	private static boolean hiveStillAnswers(Attempt a) {
		if (!ANCESTRAL_COMMUNION_RITE_ID.equals(a.recipe().getId())
				|| HarbingerPathPermissions.hearsTheHive(HemoCapabilityAccess.getPlayerDegreeNumber(a.player()),
						HemoCapabilityAccess.getInitiatoryDegree(a.player()).getArchonPath())) {
			return true;
		}
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.ancestral_communion_silentium").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
				false);
		return false;
	}

	/** The Sporitic Thurible is an Apotheos relic (NQ-10-3). */
	private static boolean thuribleIsAnApotheosRelic(Attempt a) {
		if (!SPORITIC_THURIBLE_RITE_ID.equals(a.recipe().getId())
				|| SporiticThuribleRules.mayBurn(HemoCapabilityAccess.getPlayerDegreeNumber(a.player()),
						HemoCapabilityAccess.getInitiatoryDegree(a.player()).getArchonPath())) {
			return true;
		}
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.sporitic_thurible_apotheos").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
				false);
		return false;
	}

	/** A timed rite with no ceremony is paid in one draw, so the caster must hold it before it starts. */
	private static boolean bloodCoversATimedRite(Attempt a) {
		if (a.recipe().hasInteractiveCeremony() || a.volume().getBloodVolume() >= a.recipe().getBloodCost()) return true;
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.not_enough_blood_to_begin_the",
						a.recipe().displayName()).withStyle(ChatFormatting.DARK_RED),
				true);
		return false;
	}

	private static boolean pathPermitsTheRite(Attempt a) {
		if (a.recipe().isUnstained()) return true;
		var degree = HemoCapabilityAccess.getInitiatoryDegree(a.player());
		if (HarbingerPathPermissions.canPerformRite(degree.getTotalPomesConsumed(), degree.getArchonPath(),
				APOTHEOS_RITE_ID.equals(a.recipe().getId()))) {
			return true;
		}
		a.player().displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.this_rite_cannot_answer_your_current"), false);
		return false;
	}

	private static boolean stationIsFree(Attempt a) {
		if (!CardinalRiteSavedData.get(a.level()).hasRiteAt(a.center())) return true;
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.this_cardinal_station_is_already_carrying").withStyle(ChatFormatting.DARK_RED),
				true);
		return false;
	}

	private static boolean mediumIsSeated(Attempt a) {
		if (!a.recipe().hasLayeredStation() || focusMediumMatches(a.level(), a.center(), a.recipe())) return true;
		a.player().displayClientMessage((a.recipe().hasMedium() ? Component.translatable("message.hemomancy.cardinal_rite_activation.seat_the_rite_s_required_medium") : Component.translatable("message.hemomancy.cardinal_rite_activation.remove_the_unexpected_medium_from_the")).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		return false;
	}

	/** A bound Qliphoth Seed answers only to the Archon who took it. */
	private static boolean seedAnswersTheCaster(Attempt a) {
		if (!(a.level().getBlockEntity(a.center()) instanceof CardinalFocusBlockEntity seatedFocus)) return true;
		Component notTheirs = com.vincenthuto.hemomancy.common.item.harbinger.SeedBinding.refusal(
				seatedFocus.getMediumForMatching(), a.player());
		if (notTheirs == null) return true;
		a.player().displayClientMessage(notTheirs.copy().withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	/** A Puppeteer trial's own checks; it says why it refuses. */
	private static boolean puppetMayBegin(Attempt a) {
		if (!a.recipe().isPuppeteerTrial()) return true;
		ItemStack seated = a.level().getBlockEntity(a.center()) instanceof CardinalFocusBlockEntity focus
				? focus.getMediumForMatching() : ItemStack.EMPTY;
		return PuppeteerTrialRiteController.canBegin(a.player(), seated,
				a.recipe().getPuppeteerTrial().summonName(), a.recipe().getBloodCost(), true);
	}

	/** Founding Fane relocation needs an explicit second activation. */
	private static boolean faneRelocationIsConfirmed(Attempt a) {
		return !FOUNDING_FANE_RITE_ID.equals(a.recipe().getId()) || canStartFoundingFane(a.player(), a.center());
	}

	/** A rite whose result cannot take hold here is refused before anything is escrowed. */
	private static boolean resultCanTakeHold(Attempt a) {
		if (a.recipe().isUnstained()) return true;
		Component refusal = CardinalRiteCompletionEffects.refusal(
				new CardinalRiteCompletionEffects.Context(a.level(), a.player(), null, a.recipe(), a.center()),
				CardinalRiteCompletionEffects.Stage.ACTIVATION);
		if (refusal == null) return true;
		a.player().displayClientMessage(refusal.copy()
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		return false;
	}

	/** The first Hematic Unbinding only warns, before any offering is taken. */
	private static boolean unbindingIsConfirmed(Attempt a) {
		return !HEMATIC_UNBINDING_RITE_ID.equals(a.recipe().getId()) || canStartHematicUnbinding(a.player());
	}

	// ---- Start ----

	/** Builds the rite, takes what it escrows, plants the staff and tells the caster it has begun. */
	private static CardinalRiteActivationRules.ActivationAttempt start(Attempt a,
			CardinalRiteStationMatcher.StationMatch stationMatch) {
		CardinalRiteRecipe recipe = a.recipe();
		int castingDuration = recipe.hasInteractiveCeremony()
				? recipe.getCeremony().targetDurationTicks()
				: recipe.getRiteType().getCastingDurationTicks();
		ActiveCardinalRite rite = build(a, stationMatch, castingDuration);
		ItemStack plantingStaff = escrow(a, rite, stationMatch);
		if (plantingStaff == null) return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
		CardinalRiteSavedData.get(a.level()).startRite(rite);
		CardinalRiteAllyService.maintainNpcStations(a.level(), rite);

		if (!plantingStaff.isEmpty()) {
			CardinalRiteStaffEscrow.armProjectionHand(a.player());
			PacketHandler.sendTracking(a.player(),
					new PacketCardinalRiteStaffPlanting(a.player().getId(), a.center(), plantingStaff));
		} else {
			a.level().playSound(null, a.center(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.5F);
		}
		a.player().displayClientMessage(
				Component.translatable(recipe.isWorkingCraft()
						? "message.hemomancy.cardinal_rite_activation.rite_begins_working"
						: recipe.hasInteractiveCeremony()
								&& !com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.is(rite)
						? "message.hemomancy.cardinal_rite_activation.rite_awaits_consecration"
						: "message.hemomancy.cardinal_rite_activation.rite_begins_timed",
						recipe.displayName(), castingDuration / 20).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		a.player().displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.stay_inside_the_ritual_circle").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
				false);
		return CardinalRiteActivationRules.ActivationAttempt.STARTED;
	}

	/** The rite of the recipe's kind, its rings and instability order set, its floor and offerings recorded. */
	private static ActiveCardinalRite build(Attempt a, CardinalRiteStationMatcher.StationMatch stationMatch,
			int castingDuration) {
		CardinalRiteRecipe recipe = a.recipe();
		UUID caster = a.player().getUUID();
		int ceremonyDegree = Math.max(1, recipe.getRequiredDegree());
		ActiveCardinalRite rite = recipe.isPuppeteerTrial()
				? ActiveCardinalRite.puppeteerTrial(caster, a.center(), recipe.getId(),
						recipe.getRiteType().getSize())
				: recipe.isWorkingCraft()
				? ActiveCardinalRite.workingCraft(caster, a.center(), recipe.getId(),
						recipe.getRiteType().getSize(), recipe.getSubjectFeedMl())
				: recipe.hasInteractiveCeremony()
				? ActiveCardinalRite.interactive(
						caster, a.center(), recipe.getId(), castingDuration,
						recipe.getRiteType().getSize(), ceremonyDegree,
						recipe.getCeremony().abbreviated(),
						StationUpgradeRites.isRite(recipe.getId()) ? 0
						: com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules.waveCount(
								recipe.getCeremony(), recipe.getRequiredDegree()),
						recipe.getCeremony().anchors().size())
				: new ActiveCardinalRite(caster, a.center(), recipe.getId(),
						castingDuration, recipe.getRiteType().getSize());
		if (recipe.hasInteractiveCeremony()) {
			rite.setInstabilityDamagePriority(
					com.vincenthuto.hemomancy.common.rite.CardinalRiteInstabilityBoundaryRules
							.damagePriority(recipe.getCeremony().anchors()).stream()
							.mapToInt(Integer::intValue).toArray());
			if (!recipe.isPuppeteerTrial() && !recipe.isUnstained() && !recipe.isWorkingCraft()) {
				// Ring 0 opens now; each later ring wakes at a later phase of the rite.
				rite.configureRings(com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRules.ranksOf(
						recipe.getCeremony().anchors()), recipe.getCeremony().stillIntervalTicks() > 0);
			}
		}
		if (stationMatch != null) {
			rite.setMatchedFloor(stationMatch.floor().id(),
					stationMatch.floorMatch().getForwards(), stationMatch.floorMatch().getUp());
			if (!recipe.isPuppeteerTrial()) {
				rite.captureOfferingItinerary(stationMatch.braziers().stream()
						.map(offering -> new ActiveCardinalRite.RiteOffering(
								offering.pos(), offering.stack(), offering.consumeOnSuccess()))
						.toList());
			}
		}
		return rite;
	}

	/**
	 * Takes what the rite holds while it runs: a Succession's samples, the planted Living Staff, a craft's or upgrade's
	 * offerings. Anything already taken goes back when a later step refuses.
	 *
	 * @return the planted staff, {@link ItemStack#EMPTY} for a rite that plants none, or null when a step refused
	 */
	private static ItemStack escrow(Attempt a, ActiveCardinalRite rite,
			CardinalRiteStationMatcher.StationMatch stationMatch) {
		CardinalRiteRecipe recipe = a.recipe();
		ServerPlayer player = a.player();
		if (com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.isRecipe(recipe.getId())
				&& !com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.prepare(a.level(), player, rite, stationMatch)) {
			return null;
		}
		ItemStack plantingStaff = ItemStack.EMPTY;
		if (!recipe.isUnstained() && recipe.hasInteractiveCeremony()
				&& "living_staff".equals(recipe.getCeremony().focusMode())) {
			ItemStack staff = CardinalRiteStaffEscrow.capture(player);
			if (staff.isEmpty()) {
				com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.cleanup(a.level(), rite);
				player.displayClientMessage(
						Component.translatable("message.hemomancy.cardinal_rite_activation.a_living_staff_must_be_planted").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						false);
				return null;
			}
			rite.setEscrowedStaff(staff, player.registryAccess());
			rite.beginStaffPlanting();
			// The planting click must not also open a blood channel with the hand that held the staff.
			player.stopUsingItem();
			plantingStaff = staff;
		}
		var upgradeTier = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.forRite(rite.getRecipeId());
		if (upgradeTier.isPresent() && !HemoCapabilityAccess.stationUpgrades(player).eligible(player, upgradeTier.get())) {
			CardinalRiteStaffEscrow.restore(player, rite);
			player.displayClientMessage(Component.translatable("hemomancy.station_upgrade.requirements_unmet")
					.withStyle(ChatFormatting.RED), false);
			return null;
		}
		if (recipe.isWorkingCraft() && !WorkingCraftRites.prepare(
				a.level(), rite, stationMatch == null ? null : stationMatch.subject())) {
			CardinalRiteStaffEscrow.restore(player, rite);
			player.displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.the_working_floor_cannot_take_hold").withStyle(ChatFormatting.DARK_RED), false);
			return null;
		}
		if (!StationUpgradeRites.prepare(a.level(), rite)) {
			CardinalRiteStaffEscrow.restore(player, rite);
			player.displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.seat_an_idle_station_of_the").withStyle(ChatFormatting.DARK_RED), false);
			return null;
		}
		return plantingStaff;
	}

	/** A Focus that matched no rite: says the Degree a higher rite needs, a passed rank, or what the floor lacks. */
	private static void explainUnansweredFloor(ServerLevel level, BlockPos hitPos, Player player,
			CardinalRiteActivationRules.Trigger trigger, List<CardinalRiteRecipe> allRites,
			CardinalRiteStationMatcher.Status status) {
		if (status == CardinalRiteStationMatcher.Status.NO_RITE) {
			Integer degreeNeeded = degreeOfAHigherRite(level, hitPos, player, trigger, allRites);
			if (degreeNeeded != null) {
				player.displayClientMessage(
						Component.translatable("message.hemomancy.cardinal_rite_activation.this_rite_requires",
								RecipeDegreeGates.degreeLabel(degreeNeeded)
										.withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)).withStyle(ChatFormatting.RED),
						false);
				return;
			}
			if (answersAFinishedRankRite(level, hitPos, player, trigger, allRites)) {
				sayAlreadyAttained(player, HemoCapabilityAccess.getPlayerDegreeNumber(player));
				return;
			}
		}
		player.displayClientMessage(Component.translatable(switch (status) {
					case NO_FLOOR -> "message.hemomancy.cardinal_rite_activation.the_cardinal_focus_cannot_find_a";
					case NO_SUBJECT -> "message.hemomancy.cardinal_rite_activation.the_working_floor_cannot_find_its";
					default -> "message.hemomancy.cardinal_rite_activation.the_floor_answers_but_no_structure";
				})
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
	}

	/**
	 * The lowest Degree of the Harbinger rites this floor answers to that the player is still too low for, or null. The
	 * first resolution leaves those rites out, so without this the player hears only that no rite matches.
	 */
	private static Integer degreeOfAHigherRite(ServerLevel level, BlockPos focus, Player player,
			CardinalRiteActivationRules.Trigger trigger, List<CardinalRiteRecipe> allRites) {
		int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		CardinalRiteStationMatcher.Resolution higher = CardinalRiteStationMatcher.resolve(level, focus,
				allRites.stream().filter(recipe -> !recipe.isUnstained()
						&& CardinalRiteActivationRules.mayInitiate(trigger, false,
								RecipeDegreeGates.getRequiredDegree(recipe), focusMode(recipe))
						&& RecipeDegreeGates.getRequiredDegree(recipe) > playerDegree).toList());
		return higher.matches().stream().map(match -> RecipeDegreeGates.getRequiredDegree(match.recipe()))
				.min(Integer::compare).orElse(null);
	}

	/**
	 * Whether this floor answers to a Harbinger rank rite the player has already passed. The first resolution leaves
	 * a closed rank rite out, so without this the player hears only that no rite matches (PT-32).
	 */
	private static boolean answersAFinishedRankRite(ServerLevel level, BlockPos focus, Player player,
			CardinalRiteActivationRules.Trigger trigger, List<CardinalRiteRecipe> allRites) {
		int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		CardinalRiteStationMatcher.Resolution finished = CardinalRiteStationMatcher.resolve(level, focus,
				allRites.stream().filter(recipe -> !recipe.isUnstained() && recipe.isRankup()
						&& CardinalRiteActivationRules.mayInitiate(trigger, false,
								RecipeDegreeGates.getRequiredDegree(recipe), focusMode(recipe))
						&& RecipeDegreeGates.getRequiredDegree(recipe) <= playerDegree
						&& RecipeDegreeGates.getRankupTargetDegree(recipe) != null
						&& playerDegree >= RecipeDegreeGates.getRankupTargetDegree(recipe)).toList());
		return !finished.matches().isEmpty();
	}

	private static void sayAlreadyAttained(Player player, int playerDegree) {
		EnumInitiatoryDegree current = EnumInitiatoryDegree.byNumber(playerDegree);
		Component currentName = current != null ? current.displayName()
				: Component.translatable("message.hemomancy.recipe_degree_gates.degree", playerDegree);
		player.displayClientMessage(
				Component.translatable("message.hemomancy.cardinal_rite_activation.you_have_already_attained",
						currentName.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
						Component.translatable("message.hemomancy.cardinal_rite_activation.this_rank_rite_has_no_further")
								.withStyle(ChatFormatting.DARK_RED)).withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static String focusMode(CardinalRiteRecipe recipe) {
		return recipe.getCeremony() == null ? "" : recipe.getCeremony().focusMode();
	}

	private static boolean canStartFoundingFane(ServerPlayer player, BlockPos requestedCenter) {
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.getBloodLine();
		if (bloodline == null || !bloodline.isValid()) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.cardinal_rite_activation.the_founding_fane_requires_an_established").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return false;
		}
		if (!bloodline.getLeaderUUID().equals(player.getUUID())) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.cardinal_rite_activation.only_the_bloodline_progenitor_may_consecrate").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return false;
		}
		if (hasActiveFane(player, bloodline.getLeaderUUID())) {
			final String pendingPosKey = "hemomancy:pending_fane_relocation_pos";
			final String pendingDimensionKey = "hemomancy:pending_fane_relocation_dimension";
			final String pendingUntilKey = "hemomancy:pending_fane_relocation_until";
			var data = player.getPersistentData();
			BlockPos warned = data.contains(pendingPosKey)
					? BlockPos.of(data.getLong(pendingPosKey)) : null;
			String dimension = player.level().dimension().location().toString();
			if (RiteRelocationConfirmationRules.confirmed(
					requestedCenter, dimension, player.level().getGameTime(),
					warned, data.getString(pendingDimensionKey), data.getLong(pendingUntilKey))) {
				data.remove(pendingPosKey);
				data.remove(pendingDimensionKey);
				data.remove(pendingUntilKey);
				return true;
			}
			data.putLong(pendingPosKey, requestedCenter.asLong());
			data.putString(pendingDimensionKey, dimension);
			data.putLong(pendingUntilKey, player.level().getGameTime()
					+ RiteRelocationConfirmationRules.CONFIRMATION_TICKS);
			player.displayClientMessage(
					Component.translatable("message.hemomancy.cardinal_rite_activation.your_bloodline_already_has_a_founding").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
					false);
			return false;
		}
		return true;
	}

	private static boolean canStartHematicUnbinding(ServerPlayer player) {
		Bloodline bloodline = BloodlineSavedData.get(player.server.overworld()).getBloodlineForPlayer(player.getUUID());
		if (bloodline == null || !bloodline.isValid()) return true;
		if (HematicUnbindingRules.activate(player.getUUID(), bloodline.getBloodlineUUID(),
				player.level().getGameTime()) == HematicUnbindingRules.Decision.CONFIRM) {
			return true;
		}
		player.displayClientMessage(
				HemoText.translatable("message.hemomancy.cardinal_rite_completion_effects.the_covenant_loosens_but_holds_performing",
						bloodline.displayName()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
				false);
		return false;
	}

	private static boolean hasActiveFane(ServerPlayer player, UUID owner) {
		for (ServerLevel level : player.server.getAllLevels()) {
			if (FoundingFaneSavedData.get(level).hasFane(owner)) {
				return true;
			}
		}
		return false;
	}

	private static boolean focusMediumMatches(ServerLevel level, BlockPos centerPos, CardinalRiteRecipe recipe) {
		if (!(level.getBlockEntity(centerPos) instanceof CardinalFocusBlockEntity focus)) return false;
		return CardinalRiteMediumRules.matches(recipe.getMedium(), focus.getMediumForMatching());
	}

}
