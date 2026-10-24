package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
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

	private CardinalRiteActivation() {
	}

	public static CardinalRiteActivationRules.ActivationAttempt tryStart(Player player, BlockPos hitPos,
			CardinalRiteActivationRules.Trigger trigger) {
		ServerLevel sLevel = (ServerLevel) player.level();
		ServerPlayer serverPlayer = (ServerPlayer) player;

		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);

		IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(player)
				;

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
			BlockPattern bp = recipe.hasLayeredStation()
					? stationMatch.floor().pattern().getBlockPattern()
					: recipe.getPattern().getBlockPattern();
			BlockPattern.BlockPatternMatch match = recipe.hasLayeredStation()
					? stationMatch.floorMatch()
					: PatternOrigins.findContaining(bp, sLevel, hitPos);
			if (match != null) {
				if (!recipe.hasLayeredStation()
						&& trigger != CardinalRiteActivationRules.Trigger.BLOOD_CRAFTING_KEY) {
					CardinalRiteActivationRules.Cell activation =
							CardinalRiteActivationRules.activationCell(recipe.getPattern().getPatternArray());
					if (activation == null) continue;
					BlockPos activationPos = match.getBlock(
							activation.x(), activation.y(), activation.z()).getPos();
					if (!activationPos.equals(hitPos)) {
						Component activator = Component.translatable(
								trigger == CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE
										? "message.hemomancy.cardinal_rite_activation.the_living_staff_points"
										: "message.hemomancy.cardinal_rite_activation.the_sanguine_formation_draws");
						player.displayClientMessage(
								HemoText.translatable("message.hemomancy.cardinal_rite_activation.toward_the_rite_s_central",
										activator,
										sLevel.getBlockState(activationPos).getBlock().getName()
												.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
										Component.literal(".").withStyle(ChatFormatting.DARK_RED)).withStyle(ChatFormatting.DARK_RED),
								true);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
				}
				if (CardinalRiteSavedData.hasActiveRiteAnywhere(sLevel.getServer(), player.getUUID())) {
					player.displayClientMessage(
							Component.translatable("message.hemomancy.cardinal_rite_activation.a_rite_is_already_in_progress").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							true);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
				// Explicit recipe degree / stage progression check.
				if (!recipe.isUnstained()) {
					if (RecipeDegreeGates.getRequiredDegree(recipe) > 0 && !bloodVolume.isActive()) {
						player.displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.your_blood_must_be_active_to").withStyle(ChatFormatting.DARK_RED), false);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
					// Harbinger: check Hematic Order degree
					int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
					int requiredDegree = RecipeDegreeGates.getRequiredDegree(recipe);
					if (playerDegree < requiredDegree) {
						player.displayClientMessage(
								Component.translatable("message.hemomancy.cardinal_rite_activation.this_rite_requires",
										RecipeDegreeGates.degreeLabel(requiredDegree)
												.withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)).withStyle(ChatFormatting.RED),
								false);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
					if (recipe.isRankup()) {
						Integer targetDegree = RecipeDegreeGates.getRankupTargetDegree(recipe);
						if (targetDegree != null && playerDegree >= targetDegree) {
							EnumInitiatoryDegree current = EnumInitiatoryDegree.byNumber(playerDegree);
							Component currentName = current != null ? current.displayName()
									: Component.translatable("message.hemomancy.recipe_degree_gates.degree", playerDegree);
							player.displayClientMessage(
									Component.translatable("message.hemomancy.cardinal_rite_activation.you_have_already_attained",
											currentName.copy()
													.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
											Component.translatable("message.hemomancy.cardinal_rite_activation.this_rank_rite_has_no_further").withStyle(ChatFormatting.DARK_RED)).withStyle(ChatFormatting.DARK_RED),
									false);
							return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
						}
						if (targetDegree != null) {
							HarbingerChapterMilestone unmet =
									HarbingerChapterProgression.unmetChapterForTargetDegree(serverPlayer, targetDegree);
							if (unmet != null) {
								player.displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.the_rank_rite_remains_sealed_complete",
										unmet.chapterName().copy()
												.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
										Component.translatable("message.hemomancy.cardinal_rite_activation.first").withStyle(ChatFormatting.DARK_RED)).withStyle(ChatFormatting.DARK_RED), false);
								return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
							}
						}
					}
				} else {
					// Unstained: check purity/clarity progression level
					int requiredLevel = RecipeDegreeGates.getRequiredDegree(recipe);
					if (!RecipeDegreeGates.playerMeets(player, recipe)
							&& !UnstainedAccessRules.bypassesUnstainedLevelGate(recipe.getId().getPath(), HemoCapabilityAccess.getUnstainedProgress(player))) {
						var stageName = RecipeDegreeGates.requirementLabel(recipe);
						player.displayClientMessage(
								Component.translatable("message.hemomancy.cardinal_rite_activation.this_rite_demands",
										stageName
												.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
										Component.translatable("message.hemomancy.cardinal_rite_activation.unstained_requirement").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
								false);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
				}

				if (recipe.isUnstained()) {
					var preflight = UnstainedCardinalRiteEvents.preflight(serverPlayer, recipe.getId().getPath());
					if (!preflight.success()) {
						UnstainedCardinalRiteEvents.announceFailure(serverPlayer, preflight.failure());
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
				}

				//  Path alignment gate 
				boolean harbingerProgressBlocked = UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player));
				if (!recipe.isUnstained() && harbingerProgressBlocked) {
					player.displayClientMessage(
							Component.translatable("message.hemomancy.cardinal_rite_activation.purification_bars_access_to_the_hematic").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
							false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}

				//  Apotheos gate: requires completed Qliphoth Communion 
				if (APOTHEOS_RITE_ID.equals(recipe.getId())
						&& !HemoCapabilityAccess.getInitiatoryDegree(player).isQliphothCommunionDone()) {
					player.displayClientMessage(
							Component.translatable("message.hemomancy.cardinal_rite_activation.the_eighth_degree_remains_sealed_consume").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
							false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}

				//  Blood cost check 
				if (!recipe.hasInteractiveCeremony() && bloodVolume.getBloodVolume() < recipe.getBloodCost()) {
					player.displayClientMessage(
							Component.translatable("message.hemomancy.cardinal_rite_activation.not_enough_blood_to_begin_the",
									recipe.displayName()).withStyle(ChatFormatting.DARK_RED),
							true);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}

				boolean pathPermitsRite = HarbingerPathPermissions.canPerformRite(HemoCapabilityAccess.getInitiatoryDegree(player).getTotalPomesConsumed(),
								HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath(), APOTHEOS_RITE_ID.equals(recipe.getId()));
				if (!recipe.isUnstained() && !pathPermitsRite) {
					player.displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.this_rite_cannot_answer_your_current"), false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}

				// Calculate the center of the matched pattern
				BlockPos centerPos;
				if (recipe.hasLayeredStation()) {
					centerPos = hitPos;
				} else {
					int centerWidth = recipe.getPattern().getBlockPattern().getWidth() / 2;
					int centerHeight = recipe.getPattern().getBlockPattern().getHeight() / 2;
					int centerDepth = recipe.getPattern().getBlockPattern().getDepth() / 2;
					centerPos = match.getBlock(centerWidth, centerHeight, centerDepth).getPos();
				}
				if (savedData.hasRiteAt(centerPos)) {
					player.displayClientMessage(
							Component.translatable("message.hemomancy.cardinal_rite_activation.this_cardinal_station_is_already_carrying").withStyle(ChatFormatting.DARK_RED),
							true);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}

				if (recipe.hasLayeredStation() && !focusMediumMatches(sLevel, centerPos, recipe)) {
					player.displayClientMessage((recipe.hasMedium() ? Component.translatable("message.hemomancy.cardinal_rite_activation.seat_the_rite_s_required_medium") : Component.translatable("message.hemomancy.cardinal_rite_activation.remove_the_unexpected_medium_from_the")).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
				if (recipe.isPuppeteerTrial()) {
					ItemStack seated = sLevel.getBlockEntity(centerPos) instanceof CardinalFocusBlockEntity focus
							? focus.getMediumForMatching() : ItemStack.EMPTY;
					if (!PuppeteerTrialRiteController.canBegin(serverPlayer, seated,
							recipe.getPuppeteerTrial().summonName(), recipe.getBloodCost(), true)) {
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
				}

				// Founding Fane relocation needs an explicit second activation.
				if (FOUNDING_FANE_RITE_ID.equals(recipe.getId()) && !canStartFoundingFane(serverPlayer, centerPos)) {
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
				// A rite whose result cannot take hold here is refused before anything is escrowed.
				if (!recipe.isUnstained()) {
					Component refusal = CardinalRiteCompletionEffects.refusal(
							new CardinalRiteCompletionEffects.Context(
									sLevel, serverPlayer, null, recipe, centerPos),
							CardinalRiteCompletionEffects.Stage.ACTIVATION);
					if (refusal != null) {
						player.displayClientMessage(refusal.copy()
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
				}

				// Start the rite
				int castingDuration = recipe.hasInteractiveCeremony()
						? recipe.getCeremony().targetDurationTicks()
						: recipe.getRiteType().getCastingDurationTicks();
				int ceremonyDegree = Math.max(1, recipe.getRequiredDegree());
				ActiveCardinalRite rite = recipe.isPuppeteerTrial()
						? ActiveCardinalRite.puppeteerTrial(player.getUUID(), centerPos, recipe.getId(),
								recipe.getRiteType().getSize())
						: recipe.isWorkingCraft()
						? ActiveCardinalRite.workingCraft(player.getUUID(), centerPos, recipe.getId(),
								recipe.getRiteType().getSize(), recipe.getSubjectFeedMl())
						: recipe.hasInteractiveCeremony()
						? ActiveCardinalRite.interactive(
								player.getUUID(), centerPos, recipe.getId(), castingDuration,
								recipe.getRiteType().getSize(), ceremonyDegree,
								recipe.getCeremony().abbreviated(),
								StationUpgradeRites.isRite(recipe.getId()) ? 0
								: com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules.waveCount(
										recipe.getCeremony(), recipe.getRequiredDegree()),
								recipe.getCeremony().anchors().size())
						: new ActiveCardinalRite(player.getUUID(), centerPos, recipe.getId(),
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
				if (com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.isRecipe(recipe.getId())
                        && !com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.prepare(sLevel, serverPlayer, rite, stationMatch))
                    return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
                ItemStack plantingStaff = ItemStack.EMPTY;
				if (!recipe.isUnstained() && recipe.hasInteractiveCeremony()
						&& "living_staff".equals(recipe.getCeremony().focusMode())) {
					ItemStack staff = CardinalRiteStaffEscrow.capture(serverPlayer);
					if (staff.isEmpty()) {
                        com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.cleanup(sLevel, rite);
						player.displayClientMessage(
								Component.translatable("message.hemomancy.cardinal_rite_activation.a_living_staff_must_be_planted").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
								false);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
					rite.setEscrowedStaff(staff, serverPlayer.registryAccess());
					rite.beginStaffPlanting();
					// The planting click must not also open a blood channel with the hand that held the staff.
					serverPlayer.stopUsingItem();
					plantingStaff = staff;
				}
				var upgradeTier = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog.forRite(rite.getRecipeId());
				if (upgradeTier.isPresent() && !HemoCapabilityAccess.stationUpgrades(serverPlayer).eligible(serverPlayer, upgradeTier.get())) {
					CardinalRiteStaffEscrow.restore(serverPlayer, rite);
					player.displayClientMessage(Component.translatable("hemomancy.station_upgrade.requirements_unmet")
							.withStyle(ChatFormatting.RED), false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
				if (recipe.isWorkingCraft() && !WorkingCraftRites.prepare(
						sLevel, rite, stationMatch == null ? null : stationMatch.subject())) {
					CardinalRiteStaffEscrow.restore(serverPlayer, rite);
					player.displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.the_working_floor_cannot_take_hold").withStyle(ChatFormatting.DARK_RED), false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
				if (!StationUpgradeRites.prepare(sLevel, rite)) {
					CardinalRiteStaffEscrow.restore(serverPlayer, rite);
					player.displayClientMessage(Component.translatable("message.hemomancy.cardinal_rite_activation.seat_an_idle_station_of_the").withStyle(ChatFormatting.DARK_RED), false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
				savedData.startRite(rite);
                CardinalRiteAllyService.maintainNpcStations(sLevel, rite);

				if (!plantingStaff.isEmpty()) {
					CardinalRiteStaffEscrow.armProjectionHand(serverPlayer);
					PacketHandler.sendTracking(serverPlayer,
							new PacketCardinalRiteStaffPlanting(
									serverPlayer.getId(), centerPos, plantingStaff));
				} else {
					sLevel.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE,
							SoundSource.BLOCKS, 1.0F, 1.5F);
				}

				// Notify the player
				player.displayClientMessage(
						Component.translatable(recipe.isWorkingCraft()
								? "message.hemomancy.cardinal_rite_activation.rite_begins_working"
								: recipe.hasInteractiveCeremony()
                                && !com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.is(rite)
								? "message.hemomancy.cardinal_rite_activation.rite_awaits_consecration"
								: "message.hemomancy.cardinal_rite_activation.rite_begins_timed",
								recipe.displayName(), castingDuration / 20).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
						false);
				player.displayClientMessage(
						Component.translatable("message.hemomancy.cardinal_rite_activation.stay_inside_the_ritual_circle").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
						false);
				return CardinalRiteActivationRules.ActivationAttempt.STARTED;
			}
		}
		if (hitState.is(BlockInit.cardinal_focus.get())) {
			player.displayClientMessage(Component.translatable(switch (layeredResolution.status()) {
						case NO_FLOOR -> "message.hemomancy.cardinal_rite_activation.the_cardinal_focus_cannot_find_a";
						case NO_SUBJECT -> "message.hemomancy.cardinal_rite_activation.the_working_floor_cannot_find_its";
						default -> "message.hemomancy.cardinal_rite_activation.the_floor_answers_but_no_structure";
					})
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
		}
		return CardinalRiteActivationRules.ActivationAttempt.NOT_HANDLED;
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
