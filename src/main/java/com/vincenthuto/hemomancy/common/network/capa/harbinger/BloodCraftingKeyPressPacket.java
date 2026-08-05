package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMediumRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.PuppeteerTrialRiteController;
import com.vincenthuto.hemomancy.common.mission.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.tile.functional.CardinalFocusBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public class BloodCraftingKeyPressPacket implements CustomPacketPayload {

	public static final Type<BloodCraftingKeyPressPacket> TYPE = new Type<>(Hemomancy.rloc("blood_crafting_key_press_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodCraftingKeyPressPacket> STREAM_CODEC = StreamCodec.of(BloodCraftingKeyPressPacket::encode, BloodCraftingKeyPressPacket::decode);
	private static final ResourceLocation FOUNDING_FANE_RITE_ID = Hemomancy.rloc("cardinal_rite/founding_fane");
	private static final ResourceLocation APOTHEOS_RITE_ID = Hemomancy.rloc("cardinal_rite/apotheos_rite");
	private static final Direction[] SEARCH_DIRECTIONS = Direction.values();
	public static BloodCraftingKeyPressPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new BloodCraftingKeyPressPacket(ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer));
	}

	public static void encode(final FriendlyByteBuf buffer, final BloodCraftingKeyPressPacket message) {
		buffer.writeByte(0);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, message.heldStack);
	}

	public static void handle(final BloodCraftingKeyPressPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;

			boolean handled = false;

			// === Blood Structure Recipes (instant crafting) ===
			HitResult rayTrace = player.pick(3, 102, false);
			if (rayTrace.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockResult = (BlockHitResult) rayTrace;
				BlockPos hitPos = blockResult.getBlockPos();
				ServerLevel sLevel = (ServerLevel) player.level();

				if (!handled) {
				for (BloodStructureRecipe targetPattern : BloodCraftingPatternSearchRules.sortedByPatternSearchCost(
						BloodStructureRecipe.getAllRecipes(player.level()),
						recipe -> recipe.getPattern().getPatternArray())) {
					if (BloodStructureCraftingHelper.isRiteExclusive(targetPattern)) {
						BlockPattern.BlockPatternMatch forbiddenMatch = findStructurePatternAtHit(targetPattern, sLevel, hitPos);
						if (forbiddenMatch != null) {
							player.displayClientMessage(BloodStructureCraftingHelper.riteExclusiveMessage(), false);
							handled = true;
							break;
						}
						continue;
					}
					if (!targetPattern.isUnstained()) {
						BlockPattern.BlockPatternMatch projectionMatch = findStructurePatternAtHit(targetPattern, sLevel, hitPos);
						if (projectionMatch != null) {
							showProjectionHint(player);
							handled = true;
							break;
						}
						continue;
					}
					if (player.getMainHandItem().getItem() != targetPattern.getHeldItem().getItem()) continue;

					BlockPattern blockPat = targetPattern.getPattern().getBlockPattern();
					BlockPattern.BlockPatternMatch patternHelper = findStructurePatternAtHit(targetPattern, sLevel, hitPos);
					if (patternHelper == null) continue;

					IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(player)
							.orElseThrow(NullPointerException::new);

					// Explicit recipe degree / stage check.
					int requiredDegree = RecipeDegreeGates.getRequiredDegree(targetPattern);
					if (!RecipeDegreeGates.playerMeets(player, targetPattern)) {
						String requiredName = RecipeDegreeGates.requirementLabel(targetPattern);
						player.displayClientMessage(
								Component.literal("This formation requires " + targetPattern.getId().getPath() + " to be held, and demands ")
										.withStyle(ChatFormatting.RED)
										.append(Component.literal(requiredName)
												.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
										.append(Component.literal(targetPattern.isUnstained() ? " purity stage." : ".")
												.withStyle(ChatFormatting.RED)),
								false);
						handled = true;
						break;
					}
					// ── Path alignment gate ──
					net.minecraft.network.chat.Component alignError = checkPathAlignment(player, targetPattern);
					if (alignError != null) {
						player.displayClientMessage(alignError, false);
						handled = true;
						break;
					}
					if (bloodVolume.getBloodVolume() > targetPattern.getBloodCost()) {
						// ── Compute structure bounding box ──
						int patW = blockPat.getWidth();
						int patH = blockPat.getHeight();
						int patD = blockPat.getDepth();
						int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
						int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
						int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
						for (int i = 0; i < patW; ++i) {
							for (int j = 0; j < patH; ++j) {
								for (int k = 0; k < patD; ++k) {
									BlockPos matchPos = patternHelper.getBlock(i, j, k).getPos();
									if (matchPos.getX() < minX) minX = matchPos.getX();
									if (matchPos.getX() > maxX) maxX = matchPos.getX();
									if (matchPos.getY() < minY) minY = matchPos.getY();
									if (matchPos.getY() > maxY) maxY = matchPos.getY();
									if (matchPos.getZ() < minZ) minZ = matchPos.getZ();
									if (matchPos.getZ() > maxZ) maxZ = matchPos.getZ();
								}
							}
						}

						// ── Send blood craft ring animation immediately ──
						BlockPos ringCenter = new BlockPos(
								(minX + maxX) / 2, minY, (minZ + maxZ) / 2);
						float centerY = (minY + maxY) / 2.0f + 0.5f;
						float halfW = (maxX - minX) / 2.0f + 0.5f;
						float halfD = (maxZ - minZ) / 2.0f + 0.5f;
						float startRadius = Math.max(halfW, halfD) + 2.0f;
						int animDuration = 30; // ticks (~1.5 seconds)
						PacketDistributor.sendToAllPlayers(new PacketBloodCraftRing(ringCenter, startRadius,
										centerY, animDuration));

						// ── Drain blood and consume held item now ──
						ItemStack oldStack = player.getMainHandItem().copy();
						player.setItemInHand(InteractionHand.MAIN_HAND,
								new ItemStack(oldStack.getItem(), oldStack.getCount() - 1));
						bloodVolume.drain(targetPattern.getBloodCost());
						PacketHandler.sendToPlayer((ServerPlayer) player, new BloodVolumeServerPacket(bloodVolume));

						// ── Schedule block breaking + result drop after ring collapses ──
						PendingBloodCraftManager.schedule(
								new PendingBloodCraftManager.PendingCraft(
										sLevel, patternHelper,
										patW, patH, patD,
										hitPos, targetPattern.getResult(),
										animDuration, (ServerPlayer) player));

						handled = true;
					} else {
						player.displayClientMessage(
								Component.literal("Not enough blood can be drawn for formation"), true);
						sLevel.playLocalSound(player.blockPosition().getX(),
								player.blockPosition().getY(), player.blockPosition().getZ(),
								SoundEvents.ENDERMAN_SCREAM, null, 1f, 1f, false);
						handled = true;
					}
					break;
				}
				}
			}

			// === Missing held item warning ===
			if (!handled) {
				if (rayTrace.getType() == HitResult.Type.BLOCK) {
					BlockHitResult blockResult = (BlockHitResult) rayTrace;
					BlockPos hitPos = blockResult.getBlockPos();
					ServerLevel sLevel = (ServerLevel) player.level();

					for (BloodStructureRecipe recipe : BloodCraftingPatternSearchRules.sortedByPatternSearchCost(
							BloodStructureRecipe.getAllRecipes(player.level()),
							targetPattern -> targetPattern.getPattern().getPatternArray())) {
						BlockPattern.BlockPatternMatch match = findStructurePatternAtHit(recipe, sLevel, hitPos);
						if (match != null && BloodStructureCraftingHelper.isRiteExclusive(recipe)) {
							player.displayClientMessage(BloodStructureCraftingHelper.riteExclusiveMessage(), false);
							handled = true;
							break;
						}
						if (match != null && !recipe.isUnstained()) {
							showProjectionHint(player);
							handled = true;
							break;
						}
						if (match != null && player.getMainHandItem().getItem() != recipe.getHeldItem().getItem()) {
							// Check progression first, so locked recipes explain the missing degree/stage.
							int requiredDegree = RecipeDegreeGates.getRequiredDegree(recipe);
							if (!RecipeDegreeGates.playerMeets(player, recipe)) {
								String requiredName = RecipeDegreeGates.requirementLabel(recipe);
								player.displayClientMessage(
										Component.literal("This formation requires " + recipe.getId().getPath() + " to be held, and demands ")
												.withStyle(ChatFormatting.RED)
												.append(Component.literal(requiredName)
														.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
												.append(Component.literal(recipe.isUnstained() ? " purity stage." : ".")
														.withStyle(ChatFormatting.RED)),
										false);
							} else {
								// Path alignment check
								net.minecraft.network.chat.Component alignError = checkPathAlignment(player, recipe);
								if (alignError != null) {
									player.displayClientMessage(alignError, false);
								} else {
									player.displayClientMessage(
											Component.literal("This formation requires you to hold: "+ recipe.getId().getPath() + " to be held, and demands ")
													.withStyle(ChatFormatting.RED)
													.append(recipe.getHeldItem().getHoverName().copy()
															.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)),
											false);
								}
							}
							handled = true;
							break;
						}
					}
				}
			}

			// Unstained rites retain their existing key activation. Harbinger
			// rites deliberately require their progression-appropriate item use.
			if (!handled && rayTrace instanceof BlockHitResult blockResult) {
				tryStartCardinalRite(player, blockResult.getBlockPos(),
						CardinalRiteActivationRules.Trigger.BLOOD_CRAFTING_KEY);
			}

		});
	}

	private static void showProjectionHint(Player player) {
		player.displayClientMessage(
				Component.literal("To complete this formation, use Blood Projection with its catalyst in your offhand.")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	public static CardinalRiteActivationRules.ActivationAttempt tryStartCardinalRite(Player player, BlockPos hitPos,
			CardinalRiteActivationRules.Trigger trigger) {
		ServerLevel sLevel = (ServerLevel) player.level();
		ServerPlayer serverPlayer = (ServerPlayer) player;

		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);

		IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);

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
			player.displayClientMessage(Component.literal(layeredResolution.status()
							== CardinalRiteStationMatcher.Status.AMBIGUOUS_FLOOR
							? "The Cardinal Focus answers to more than one floor."
							: "The offerings answer to more than one rite.")
					.withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
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
					: findPatternAtHit(bp, sLevel, hitPos);
			if (match != null) {
				if (!recipe.hasLayeredStation()
						&& trigger != CardinalRiteActivationRules.Trigger.BLOOD_CRAFTING_KEY) {
					CardinalRiteActivationRules.Cell activation =
							CardinalRiteActivationRules.activationCell(recipe.getPattern().getPatternArray());
					if (activation == null) continue;
					BlockPos activationPos = match.getBlock(
							activation.x(), activation.y(), activation.z()).getPos();
					if (!activationPos.equals(hitPos)) {
						String activator = trigger == CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE
								? "The living staff points" : "The sanguine formation draws";
						player.displayClientMessage(
								Component.literal(activator + " toward the rite's central ")
										.withStyle(ChatFormatting.DARK_RED)
										.append(sLevel.getBlockState(activationPos).getBlock().getName()
												.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
										.append(Component.literal(".").withStyle(ChatFormatting.DARK_RED)),
								true);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
				}
				if (savedData.hasActiveRite(player.getUUID())) {
					player.displayClientMessage(
							Component.literal("A rite is already in progress...")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							true);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
				// Explicit recipe degree / stage progression check.
				if (!recipe.isUnstained()) {
					// Harbinger: check Hematic Order degree
					int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
					int requiredDegree = RecipeDegreeGates.getRequiredDegree(recipe);
					if (playerDegree < requiredDegree) {
						player.displayClientMessage(
								Component.literal("This rite requires ")
										.withStyle(ChatFormatting.RED)
										.append(Component.literal(RecipeDegreeGates.degreeLabel(requiredDegree))
												.withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)),
								false);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
					if (recipe.isRankup()) {
						Integer targetDegree = RecipeDegreeGates.getRankupTargetDegree(recipe.getId());
						if (targetDegree != null && playerDegree >= targetDegree) {
							EnumInitiatoryDegree current = EnumInitiatoryDegree.byNumber(playerDegree);
							String currentName = current != null ? current.getTitle() : "Degree " + playerDegree;
							player.displayClientMessage(
									Component.literal("You have already attained ")
											.withStyle(ChatFormatting.DARK_RED)
											.append(Component.literal(currentName)
													.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
											.append(Component.literal(". This rank rite has no further hold on you.")
													.withStyle(ChatFormatting.DARK_RED)),
									false);
							return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
						}
						if (targetDegree != null) {
							HarbingerChapterMilestone unmet =
									HarbingerChapterProgression.unmetChapterForTargetDegree(serverPlayer, targetDegree);
							if (unmet != null) {
								player.displayClientMessage(Component.literal("The rank rite remains sealed. Complete ")
										.withStyle(ChatFormatting.DARK_RED)
										.append(Component.literal(unmet.chapterName())
												.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
										.append(Component.literal(" first.").withStyle(ChatFormatting.DARK_RED)), false);
								return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
							}
						}
					}
				} else {
					// Unstained: check purity/clarity progression level (0â€“8)
					int requiredLevel = RecipeDegreeGates.getRequiredDegree(recipe);
					if (!RecipeDegreeGates.playerMeets(player, recipe)) {
						String stageName = RecipeDegreeGates.requirementLabel(recipe);
						player.displayClientMessage(
								Component.literal("This rite demands ")
										.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
										.append(Component.literal(stageName)
												.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
								.append(Component.literal(" (Unstained requirement)")
												.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
								false);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
				}

				// â”€â”€ Path alignment gate â”€â”€
				boolean playerIsInitiated = HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 1;
				boolean playerIsUnstained = HemoCapabilityAccess.getUnstainedProgress(player)
						.map(u -> u.hasBegunPurification()).orElse(false);
				if (recipe.isUnstained() && playerIsInitiated) {
					player.displayClientMessage(
							Component.literal("Those who have sworn blood to the Hematic Order cannot walk the Unstained path.")
									.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
							false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}
				if (!recipe.isUnstained() && playerIsUnstained) {
					player.displayClientMessage(
							Component.literal("One who has begun the purification cannot invoke the rites of the Hematic Order.")
									.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
							false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}

				// â”€â”€ Apotheos gate: requires completed Qliphoth Communion â”€â”€
				if (APOTHEOS_RITE_ID.equals(recipe.getId())
						&& !HemoCapabilityAccess.getInitiatoryDegree(player)
								.map(d -> d.isQliphothCommunionDone()).orElse(false)) {
					player.displayClientMessage(
							Component.literal("The Eighth Degree remains sealed. Consume all nine Qliphoth husks from a single bloom.")
									.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
							false);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}

				// â”€â”€ Blood cost check â”€â”€
				if (!recipe.hasInteractiveCeremony() && bloodVolume.getBloodVolume() < recipe.getBloodCost()) {
					player.displayClientMessage(
							Component.literal("Not enough blood to begin the " + recipe.getRiteName())
									.withStyle(ChatFormatting.DARK_RED),
							true);
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
							Component.literal("This cardinal station is already carrying a rite.")
									.withStyle(ChatFormatting.DARK_RED),
							true);
					return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
				}

				if (recipe.hasLayeredStation() && !focusMediumMatches(sLevel, centerPos, recipe)) {
					player.displayClientMessage(Component.literal(recipe.hasMedium()
								? "Seat the rite's required medium in the Cardinal Focus."
								: "Remove the unexpected medium from the Cardinal Focus.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
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

				// Founding Fane also requires a Consecrated Bloodwell heart.
				if (FOUNDING_FANE_RITE_ID.equals(recipe.getId())) {
					if (!canStartFoundingFane(serverPlayer, centerPos)) {
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
					if (!canManifestFoundingFaneHeart(sLevel, centerPos)) {
						player.displayClientMessage(
								Component.literal("The rite's heart must remain clear so a Consecrated Bloodwell can be manifested there.")
										.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
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
						: recipe.hasInteractiveCeremony()
						? ActiveCardinalRite.interactive(
								player.getUUID(), centerPos, recipe.getId(), castingDuration,
								recipe.getRiteType().getSize(), ceremonyDegree,
								recipe.getCeremony().abbreviated(),
								recipe.getCeremony().waves().isEmpty()
										? recipe.getCeremony().guaranteedWaves().size()
										: Math.max(recipe.getCeremony().guaranteedWaves().size(),
												recipe.getRequiredDegree() == 5 ? 1
														: recipe.getRequiredDegree() == 6 ? 3
														: recipe.getRequiredDegree() >= 7
																? Math.min(6, Math.max(4,
																		recipe.getCeremony().waves().size()))
																: 0),
								recipe.getCeremony().anchors().size())
						: new ActiveCardinalRite(player.getUUID(), centerPos, recipe.getId(),
								castingDuration, recipe.getRiteType().getSize());
				if (recipe.hasInteractiveCeremony()) {
					rite.setInstabilityDamagePriority(
							com.vincenthuto.hemomancy.common.rite.CardinalRiteInstabilityBoundaryRules
												.damagePriority(recipe.getCeremony().anchors()).stream()
									.mapToInt(Integer::intValue).toArray());
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
				ItemStack plantingStaff = ItemStack.EMPTY;
				if (!recipe.isUnstained() && recipe.hasInteractiveCeremony()
						&& "living_staff".equals(recipe.getCeremony().focusMode())) {
					ItemStack staff = CardinalRiteStaffEscrow.capture(serverPlayer);
					if (staff.isEmpty()) {
						player.displayClientMessage(
								Component.literal("A Living Staff must be planted in the Cardinal Focus.")
										.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
								false);
						return CardinalRiteActivationRules.ActivationAttempt.HANDLED;
					}
					rite.setEscrowedStaff(staff, serverPlayer.registryAccess());
					rite.beginStaffPlanting();
					plantingStaff = staff;
				}
				savedData.startRite(rite);

				if (!plantingStaff.isEmpty()) {
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer,
							new PacketCardinalRiteStaffPlanting(
									serverPlayer.getId(), centerPos, plantingStaff));
				} else {
					sLevel.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE,
							SoundSource.BLOCKS, 1.0F, 1.5F);
				}

				// Notify the player
				player.displayClientMessage(
						Component.literal(recipe.hasInteractiveCeremony()
								? "The " + recipe.getRiteName()
										+ " awaits consecration. Fill the crimson anchors."
								: "The " + recipe.getRiteName() + " begins... (" + castingDuration / 20 + "s)")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
						false);
				player.displayClientMessage(
						Component.literal("Do not leave the ritual circle!")
								.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
						false);
				return CardinalRiteActivationRules.ActivationAttempt.STARTED;
			}
		}
		if (hitState.is(BlockInit.cardinal_focus.get())) {
			player.displayClientMessage(Component.literal(layeredResolution.status()
							== CardinalRiteStationMatcher.Status.NO_FLOOR
							? "The Cardinal Focus cannot find a complete rite floor."
							: "The floor answers, but no structure and lit-brazier signature names a valid rite.")
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
				.map(IBloodVolume::getBloodLine)
				.orElse(Bloodline.NOBLOODLINE);
		if (bloodline == null || !bloodline.isValid()) {
			player.displayClientMessage(
					Component.literal("The Founding Fane requires an established bloodline.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return false;
		}
		if (!bloodline.getLeaderUUID().equals(player.getUUID())) {
			player.displayClientMessage(
					Component.literal("Only the bloodline Progenitor may consecrate a Founding Fane.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
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
			if (com.vincenthuto.hemomancy.common.rite.harbinger.RiteRelocationConfirmationRules.confirmed(
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
					+ com.vincenthuto.hemomancy.common.rite.harbinger.RiteRelocationConfirmationRules.CONFIRMATION_TICKS);
			player.displayClientMessage(
					Component.literal("Your bloodline already has a Founding Fane. Activate this exact formation "
									+ "again within thirty seconds to abandon its old heart and relocate it here.")
							.withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
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

	private static boolean canManifestFoundingFaneHeart(ServerLevel level, BlockPos centerPos) {
		var state = level.getBlockState(centerPos);
		return state.is(BlockInit.consecrated_bloodwell.get()) || state.isAir() || state.canBeReplaced();
	}

	private static BlockPattern.BlockPatternMatch findStructurePatternAtHit(
			BloodStructureRecipe recipe, ServerLevel level, BlockPos hitPos) {
		if (!level.getBlockState(hitPos).is(recipe.getHitBlock())) return null;
		return findPatternContainingHit(recipe.getPattern().getBlockPattern(), level, hitPos);
	}

	private static BlockPattern.BlockPatternMatch findPatternAtHit(
			BlockPattern blockPattern, ServerLevel level, BlockPos hitPos) {
		return findPatternContainingHit(blockPattern, level, hitPos);
	}

	private static BlockPattern.BlockPatternMatch findPatternContainingHit(
			BlockPattern blockPattern, ServerLevel level, BlockPos hitPos) {
		int width = blockPattern.getWidth();
		int height = blockPattern.getHeight();
		int depth = blockPattern.getDepth();
		for (Direction finger : SEARCH_DIRECTIONS) {
			for (Direction thumb : SEARCH_DIRECTIONS) {
				if (thumb == finger || thumb == finger.getOpposite()) {
					continue;
				}
				for (int i = 0; i < width; ++i) {
					for (int j = 0; j < height; ++j) {
						for (int k = 0; k < depth; ++k) {
							BlockPos origin = patternOriginForHit(hitPos, finger, thumb, i, j, k);
							BlockPattern.BlockPatternMatch match = blockPattern.matches(level, origin, finger, thumb);
							if (match != null && match.getBlock(i, j, k).getPos().equals(hitPos)) {
								return match;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static BlockPos patternOriginForHit(BlockPos hitPos, Direction finger, Direction thumb,
			int widthOffset, int heightOffset, int depthOffset) {
		Vec3i fingerNormal = finger.getNormal();
		Vec3i thumbNormal = thumb.getNormal();
		int palmX = fingerNormal.getY() * thumbNormal.getZ() - fingerNormal.getZ() * thumbNormal.getY();
		int palmY = fingerNormal.getZ() * thumbNormal.getX() - fingerNormal.getX() * thumbNormal.getZ();
		int palmZ = fingerNormal.getX() * thumbNormal.getY() - fingerNormal.getY() * thumbNormal.getX();
		return hitPos.offset(
				thumbNormal.getX() * heightOffset - palmX * widthOffset - fingerNormal.getX() * depthOffset,
				thumbNormal.getY() * heightOffset - palmY * widthOffset - fingerNormal.getY() * depthOffset,
				thumbNormal.getZ() * heightOffset - palmZ * widthOffset - fingerNormal.getZ() * depthOffset);
	}

	private static boolean focusMediumMatches(ServerLevel level, BlockPos centerPos, CardinalRiteRecipe recipe) {
		if (!(level.getBlockEntity(centerPos) instanceof CardinalFocusBlockEntity focus)) return false;
		return CardinalRiteMediumRules.matches(recipe.getMedium(), focus.getMediumForMatching());
	}

	private static void clearMatchedPattern(ServerLevel level, BlockPattern.BlockPatternMatch match,
			BlockPattern blockPattern) {
		int width = blockPattern.getWidth();
		int height = blockPattern.getHeight();
		int depth = blockPattern.getDepth();
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				for (int k = 0; k < depth; ++k) {
					BlockPos pos = match.getBlock(i, j, k).getPos();
					level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(level.getBlockState(pos)));
					level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
				}
			}
		}
	}

	private static AABB getMatchBounds(BlockPattern.BlockPatternMatch match, BlockPattern blockPattern) {
		int width = blockPattern.getWidth();
		int height = blockPattern.getHeight();
		int depth = blockPattern.getDepth();
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				for (int k = 0; k < depth; ++k) {
					BlockPos matchPos = match.getBlock(i, j, k).getPos();
					if (matchPos.getX() < minX) minX = matchPos.getX();
					if (matchPos.getX() > maxX) maxX = matchPos.getX();
					if (matchPos.getY() < minY) minY = matchPos.getY();
					if (matchPos.getY() > maxY) maxY = matchPos.getY();
					if (matchPos.getZ() < minZ) minZ = matchPos.getZ();
					if (matchPos.getZ() > maxZ) maxZ = matchPos.getZ();
				}
			}
		}
		return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}

	/**
	 * Checks Harbinger / Unstained path alignment for a blood structure recipe.
	 * Returns an error {@link Component} if the player is on the wrong path, or
	 * {@code null} if the recipe is accessible.
	 */
	private static net.minecraft.network.chat.Component checkPathAlignment(Player player, BloodStructureRecipe recipe) {
		boolean playerIsInitiated = HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 1;
		boolean playerIsUnstained = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(u -> u.hasBegunPurification()).orElse(false);
		if (recipe.isUnstained() && playerIsInitiated) {
			return Component.literal("Those who have sworn blood to the Hematic Order cannot walk the Unstained path.")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
		}
		if (!recipe.isUnstained() && playerIsUnstained) {
			return Component.literal("One who has begun the purification cannot invoke the formations of the Hematic Order.")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
		}
		return null;
	}

	public ItemStack heldStack;

	public BloodCraftingKeyPressPacket(ItemStack stack) {
		this.heldStack = stack;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
