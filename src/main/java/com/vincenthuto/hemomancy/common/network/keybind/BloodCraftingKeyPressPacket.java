package com.vincenthuto.hemomancy.common.network.keybind;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.PacketBloodCraftRing;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodCraftingKeyPressPacket {
	private static final ResourceLocation BLOOM_OF_QLIPHOTH_RITE_ID = Hemomancy.rloc("cardinal_rite/bloom_of_qliphoth");
	private static final ResourceLocation FOUNDING_SANCTUM_RITE_ID = Hemomancy.rloc("cardinal_rite/founding_sanctum");
	private static final ResourceLocation APOTHEOS_RITE_ID = Hemomancy.rloc("cardinal_rite/apotheos_rite");
	private static final double CATALYST_SEARCH_RADIUS_XZ = 0.65;
	private static final double CATALYST_SEARCH_RADIUS_Y = 1.0;
	private static final double BLOOM_CATALYST_MATCH_INFLATE_XZ = 0.5;
	private static final double BLOOM_CATALYST_MATCH_INFLATE_Y = 1.0;

	// ── Tier degree requirements (must match HarbingerProgressScreen constants) ──
	private static final String[] CRAFTING_TIER_NAMES = { "Basic", "Advanced", "Expert" };
	private static final int[] CRAFTING_TIER_THRESHOLDS = { 100, 200, Integer.MAX_VALUE };
	private static final int[] CRAFTING_TIER_DEGREE_REQ = { 0, 2, 4 };

	/** Returns the minimum initiatory degree required for a blood structure recipe based on its blood cost. */
	private static int getRequiredDegreeForRecipe(BloodStructureRecipe recipe) {
		double cost = recipe.getBloodCost();
		for (int i = 0; i < CRAFTING_TIER_THRESHOLDS.length; i++) {
			if (cost <= CRAFTING_TIER_THRESHOLDS[i]) {
				return CRAFTING_TIER_DEGREE_REQ[i];
			}
		}
		return CRAFTING_TIER_DEGREE_REQ[CRAFTING_TIER_DEGREE_REQ.length - 1];
	}

	/** Returns the tier name for a blood structure recipe based on its blood cost. */
	private static String getTierNameForRecipe(BloodStructureRecipe recipe) {
		double cost = recipe.getBloodCost();
		for (int i = 0; i < CRAFTING_TIER_THRESHOLDS.length; i++) {
			if (cost <= CRAFTING_TIER_THRESHOLDS[i]) {
				return CRAFTING_TIER_NAMES[i];
			}
		}
		return CRAFTING_TIER_NAMES[CRAFTING_TIER_NAMES.length - 1];
	}

	/** Returns the minimum initiatory degree required for a cardinal rite type. */
	private static int getRequiredDegreeForRite(CardinalRiteType type) {
		return switch (type) {
			case MINOR   -> 0;
			case LESSER  -> 1;
			case GREATER -> 3;
			case GRAND   -> 5;
		};
	}

	public static BloodCraftingKeyPressPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new BloodCraftingKeyPressPacket(buffer.readItem());
	}

	public static void encode(final BloodCraftingKeyPressPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeItem(message.heldStack);
	}

	public static List<BloodStructureRecipe> getMatchingRecipes(ItemStack stack, Level level) {
		List<BloodStructureRecipe> matchedRecipes = new ArrayList<>();
		for (BloodStructureRecipe recipe : BloodStructureRecipe.getAllRecipes(level)) {
			if (recipe.getHeldItem().getItem() == stack.getItem()) {
				matchedRecipes.add(recipe);
			}
		}
		return matchedRecipes;
	}

	public static void handle(final BloodCraftingKeyPressPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;

			boolean handled = false;

			// === Blood Structure Recipes (instant crafting) ===
			for (BloodStructureRecipe pattern : BloodStructureRecipe.getAllRecipes(player.level())) {
				if (player.getMainHandItem().getItem() == pattern.getHeldItem().getItem()) {
					IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					List<BloodStructureRecipe> matchedPatterns = getMatchingRecipes(message.heldStack, player.level());
					if (matchedPatterns != null) {
						if (!matchedPatterns.isEmpty()) {
							for (BloodStructureRecipe targetPattern : matchedPatterns) {
								ServerLevel sLevel = (ServerLevel) ctx.get().getSender().level();
								if (player.getMainHandItem().getItem() == targetPattern.getHeldItem().getItem()) {
									// ── Tier degree check ──
									int playerDegree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
									int requiredDegree = getRequiredDegreeForRecipe(targetPattern);
									if (playerDegree < requiredDegree) {
										String tierName = getTierNameForRecipe(targetPattern);
										player.displayClientMessage(
												Component.literal("This recipe requires the ")
														.withStyle(ChatFormatting.RED)
														.append(Component.literal(tierName)
																.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
														.append(Component.literal(" tier (Degree " + requiredDegree + ")")
																.withStyle(ChatFormatting.RED)),
												false);
										handled = true;
										break;
									}
									if (bloodVolume.getBloodVolume() > targetPattern.getBloodCost()) {
										HitResult rayTrace = player.pick(3, 102, false);
										if (rayTrace.getType() == HitResult.Type.BLOCK) {
											BlockHitResult blockResult = (BlockHitResult) rayTrace;
											BlockPos hitPos = blockResult.getBlockPos();
											BlockPattern blockPat = targetPattern.getPattern().getBlockPattern();
											int maxDim = Math.max(Math.max(blockPat.getWidth(), blockPat.getHeight()), blockPat.getDepth());
											BlockPos searchStart = hitPos.offset(-(maxDim - 1), -(maxDim - 1), -(maxDim - 1));
											BlockPattern.BlockPatternMatch patternHelper = blockPat.find(sLevel, searchStart);
											if (patternHelper != null) {
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
												PacketHandler.CHANNELBLOODVOLUME.send(
														PacketDistributor.ALL.noArg(),
														new PacketBloodCraftRing(ringCenter, startRadius,
																centerY, animDuration));

												// ── Drain blood and consume held item now ──
												ItemStack oldStack = player.getMainHandItem().copy();
												player.setItemInHand(InteractionHand.MAIN_HAND,
														new ItemStack(oldStack.getItem(), oldStack.getCount() - 1));
												bloodVolume.drain(targetPattern.getBloodCost());
												PacketHandler.CHANNELBLOODVOLUME.send(
														PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
														new BloodVolumeServerPacket(bloodVolume));

												// ── Schedule block breaking + result drop after ring collapses ──
												PendingBloodCraftManager.schedule(
														new PendingBloodCraftManager.PendingCraft(
																sLevel, patternHelper,
																patW, patH, patD,
																hitPos, targetPattern.getResult(),
																animDuration));

												handled = true;
											}
										}
									} else {
										player.displayClientMessage(
												Component.literal("Not enough blood can be drawn for formation"), true);
										sLevel.playLocalSound(player.blockPosition().getX(),
												player.blockPosition().getY(), player.blockPosition().getZ(),
												SoundEvents.ENDERMAN_SCREAM, null, 1f, 1f, false);
										handled = true;
									}
								}

							}
						}
					}
				}
			}

			// === Missing held item warning ===
			if (!handled) {
				HitResult rayTrace = player.pick(3, 102, false);
				if (rayTrace.getType() == HitResult.Type.BLOCK) {
					BlockHitResult blockResult = (BlockHitResult) rayTrace;
					BlockPos hitPos = blockResult.getBlockPos();
					ServerLevel sLevel = (ServerLevel) player.level();

					for (BloodStructureRecipe recipe : BloodStructureRecipe.getAllRecipes(player.level())) {
						BlockPattern.BlockPatternMatch match = recipe.getPattern().getBlockPattern().find(sLevel, hitPos);
						if (match != null && player.getMainHandItem().getItem() != recipe.getHeldItem().getItem()) {
							// Check degree first — if locked by tier, show tier message instead
							int playerDegree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
							int requiredDegree = getRequiredDegreeForRecipe(recipe);
							if (playerDegree < requiredDegree) {
								String tierName = getTierNameForRecipe(recipe);
								player.displayClientMessage(
										Component.literal("This recipe requires the ")
												.withStyle(ChatFormatting.RED)
												.append(Component.literal(tierName)
														.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
												.append(Component.literal(" tier (Degree " + requiredDegree + ")")
														.withStyle(ChatFormatting.RED)),
										false);
							} else {
								player.displayClientMessage(
										Component.literal("This formation requires you to hold: ")
												.withStyle(ChatFormatting.RED)
												.append(recipe.getHeldItem().getHoverName().copy()
														.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)),
										false);
							}
							handled = true;
							break;
						}
					}
				}
			}

			// === Cardinal Rite Recipes (delayed casting) ===
			if (!handled) {
				tryStartCardinalRite(player, ctx);
			}

		});
		ctx.get().setPacketHandled(true);
	}

	private static void tryStartCardinalRite(Player player, Supplier<NetworkEvent.Context> ctx) {
		ServerLevel sLevel = (ServerLevel) player.level();
		ServerPlayer serverPlayer = (ServerPlayer) player;

		// Check if player already has an active rite
		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);
		if (savedData.hasActiveRite(player.getUUID())) {
			player.displayClientMessage(
					Component.literal("A rite is already in progress...")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					true);
			return;
		}

		IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);

		HitResult rayTrace = player.pick(5, 0, false);
		if (rayTrace.getType() != HitResult.Type.BLOCK) return;

		BlockHitResult blockResult = (BlockHitResult) rayTrace;
		BlockPos hitPos = blockResult.getBlockPos();

		for (CardinalRiteRecipe recipe : CardinalRiteRecipe.getAllRecipes(player.level())) {
			BlockPattern bp = recipe.getPattern().getBlockPattern();
			int maxDim = Math.max(Math.max(bp.getWidth(), bp.getHeight()), bp.getDepth());
			BlockPos searchStart = hitPos.offset(-(maxDim - 1), -(maxDim - 1), -(maxDim - 1));
			BlockPattern.BlockPatternMatch match = bp.find(sLevel, searchStart);
			if (match != null) {
				// ── Tier degree check ──
				int playerDegree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
				int requiredDegree = recipe.getRequiredDegree() >= 0
						? recipe.getRequiredDegree()
						: getRequiredDegreeForRite(recipe.getRiteType());
				if (playerDegree < requiredDegree) {
					String riteTypeName = recipe.getRiteType().getSerializedName().substring(0, 1).toUpperCase()
							+ recipe.getRiteType().getSerializedName().substring(1);
					player.displayClientMessage(
							Component.literal("This rite requires the ")
									.withStyle(ChatFormatting.RED)
									.append(Component.literal(riteTypeName)
											.withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
									.append(Component.literal(" rite tier (Degree " + requiredDegree + ")")
											.withStyle(ChatFormatting.RED)),
							false);
					return;
				}

				// ── Apotheos gate: requires completed Qliphoth Communion ──
				if (APOTHEOS_RITE_ID.equals(recipe.getId())
						&& !player.getPersistentData().getBoolean(QliphothPomeItem.QLIPHOTH_COMMUNION_DONE_KEY)) {
					player.displayClientMessage(
							Component.literal("The Eighth Degree remains sealed. Consume all nine Qliphoth husks from a single bloom.")
									.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
							false);
					return;
				}

				// ── Blood cost check ──
				if (bloodVolume.getBloodVolume() < recipe.getBloodCost()) {
					player.displayClientMessage(
							Component.literal("Not enough blood to begin the " + recipe.getRiteName())
									.withStyle(ChatFormatting.DARK_RED),
							true);
					return;
				}

				// Calculate the center of the matched pattern
				int centerWidth = recipe.getPattern().getBlockPattern().getWidth() / 2;
				int centerHeight = recipe.getPattern().getBlockPattern().getHeight() / 2;
				int centerDepth = recipe.getPattern().getBlockPattern().getDepth() / 2;
				BlockPos centerPos = match.getBlock(centerWidth, centerHeight, centerDepth).getPos();

				// Bloom of the Qliphoth requires a planted Qliphoth Seed catalyst at center
				if (BLOOM_OF_QLIPHOTH_RITE_ID.equals(recipe.getId())) {
					if (!consumeCatalystWithinMatch(sLevel, match, bp, ItemInit.qliphoth_seed.get())) {
						player.displayClientMessage(
								Component.literal("The Bloom of the Qliphoth demands a planted Qliphoth Seed within the rite.")
										.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
								false);
						return;
					}
				}

				// Founding Sanctum requires a Sanguine Quintessence catalyst at center
				if (FOUNDING_SANCTUM_RITE_ID.equals(recipe.getId())) {
					if (!consumeCenterCatalyst(sLevel, centerPos, ItemInit.sanguine_quintessence.get())) {
						player.displayClientMessage(
								Component.literal("The Founding Sanctum demands a Sanguine Quintessence placed at its heart.")
										.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
								false);
						return;
					}
				}

				// Start the rite
				int castingDuration = recipe.getRiteType().getCastingDurationTicks();
				ActiveCardinalRite rite = new ActiveCardinalRite(
						player.getUUID(), centerPos, recipe.getId(),
						castingDuration, recipe.getRiteType().getSize());
				savedData.startRite(rite);

				// Play start sound
				sLevel.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.5f);

				// Notify the player
				int seconds = castingDuration / 20;
				player.displayClientMessage(
						Component.literal("The " + recipe.getRiteName() + " begins... (" + seconds + "s)")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
						false);
				player.displayClientMessage(
						Component.literal("Do not leave the ritual circle!")
								.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
						false);
				return;
			}
		}
	}

	private static boolean consumeCenterCatalyst(ServerLevel level, BlockPos centerPos, Item requiredItem) {
		AABB centerBox = new AABB(centerPos).inflate(CATALYST_SEARCH_RADIUS_XZ, CATALYST_SEARCH_RADIUS_Y, CATALYST_SEARCH_RADIUS_XZ);
		List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, centerBox,
				e -> e.isAlive() && e.getItem().is(requiredItem));
		if (entities.isEmpty()) {
			return false;
		}
		ItemEntity entity = entities.get(0);
		ItemStack stack = entity.getItem();
		stack.shrink(1);
		if (stack.isEmpty()) {
			entity.discard();
		} else {
			entity.setItem(stack);
		}
		return true;
	}

	private static boolean consumeCatalystWithinMatch(ServerLevel level, BlockPattern.BlockPatternMatch match,
			BlockPattern blockPattern, Item requiredItem) {
		AABB matchBounds = getMatchBounds(match, blockPattern).inflate(
				BLOOM_CATALYST_MATCH_INFLATE_XZ, BLOOM_CATALYST_MATCH_INFLATE_Y, BLOOM_CATALYST_MATCH_INFLATE_XZ);
		List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, matchBounds,
				e -> e.isAlive() && e.getItem().is(requiredItem));
		if (entities.isEmpty()) {
			return false;
		}
		ItemEntity entity = entities.get(0);
		ItemStack stack = entity.getItem();
		stack.shrink(1);
		if (stack.isEmpty()) {
			entity.discard();
		} else {
			entity.setItem(stack);
		}
		return true;
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

	public ItemStack heldStack;

	public BloodCraftingKeyPressPacket(ItemStack stack) {
		this.heldStack = stack;
	}
}
