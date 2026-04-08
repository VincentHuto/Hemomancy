package com.vincenthuto.hemomancy.common.network.keybind;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.PacketBloodCraftRing;
import com.vincenthuto.hemomancy.common.crafting.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodCraftingKeyPressPacket {

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
									if (bloodVolume.getBloodVolume() > targetPattern.getBloodCost()) {
										HitResult rayTrace = player.pick(3, 102, false);
										if (rayTrace.getType() == HitResult.Type.BLOCK) {
											BlockHitResult blockResult = (BlockHitResult) rayTrace;
											BlockPos hitPos = blockResult.getBlockPos();
											Block hitBlock = sLevel.getBlockState(hitPos).getBlock();
											if (hitBlock == targetPattern.getHitBlock()) {
												BlockPattern.BlockPatternMatch patternHelper = targetPattern
														.getPattern().getBlockPattern().find(sLevel, hitPos);
												if (patternHelper != null) {
													// ── Compute structure bounding box ──
													int patW = targetPattern.getPattern().getBlockPattern().getWidth();
													int patH = targetPattern.getPattern().getBlockPattern().getHeight();
													int patD = targetPattern.getPattern().getBlockPattern().getDepth();
													int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
													int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
													int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
													for (int i = 0; i < patW; ++i) {
														for (int j = 0; j < patH; ++j) {
															for (int k = 0; k < patD; ++k) {
																BlockPos bp = patternHelper.getBlock(i, j, k).getPos();
																if (bp.getX() < minX) minX = bp.getX();
																if (bp.getX() > maxX) maxX = bp.getX();
																if (bp.getY() < minY) minY = bp.getY();
																if (bp.getY() > maxY) maxY = bp.getY();
																if (bp.getZ() < minZ) minZ = bp.getZ();
																if (bp.getZ() > maxZ) maxZ = bp.getZ();
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
			if (bloodVolume.getBloodVolume() < recipe.getBloodCost()) {
				continue;
			}

			BlockPattern.BlockPatternMatch match = recipe.getPattern().getBlockPattern().find(sLevel, hitPos);
			if (match != null) {
				// Calculate the center of the matched pattern
				int centerWidth = recipe.getPattern().getBlockPattern().getWidth() / 2;
				int centerHeight = recipe.getPattern().getBlockPattern().getHeight() / 2;
				int centerDepth = recipe.getPattern().getBlockPattern().getDepth() / 2;
				BlockPos centerPos = match.getBlock(centerWidth, centerHeight, centerDepth).getPos();

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

	public ItemStack heldStack;

	public BloodCraftingKeyPressPacket(ItemStack stack) {
		this.heldStack = stack;
	}
}