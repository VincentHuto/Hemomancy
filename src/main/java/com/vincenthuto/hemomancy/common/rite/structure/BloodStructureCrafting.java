package com.vincenthuto.hemomancy.common.rite.structure;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodCraftRing;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hemomancy.common.util.PatternOrigins;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Blood Structure crafting with the blood crafting key. An Unstained formation the player looks at, holding its
 * catalyst, is paid for and scheduled to collapse into its result; a Harbinger formation only answers Blood
 * Projection, so the key points the player there. A formation whose catalyst is missing says what it needs.
 */
public final class BloodStructureCrafting {
	private static final BloodAccounting.Source STRUCTURE_CRAFT = BloodAccounting.Source.of("blood_structure_craft", Category.RITE, false, false);

	private BloodStructureCrafting() {
	}

	/**
	 * Crafts or explains the formation the player's key press is aimed at.
	 *
	 * @return true when a formation answered (crafted, refused or hinted); false lets the press try a rite
	 */
	public static boolean tryCraft(Player player, HitResult rayTrace) {
		boolean handled = false;

		// === Blood Structure Recipes (instant crafting) ===
		if (rayTrace.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockResult = (BlockHitResult) rayTrace;
			BlockPos hitPos = blockResult.getBlockPos();
			ServerLevel sLevel = (ServerLevel) player.level();

			if (!handled) {
			for (BloodStructureRecipe targetPattern : BloodCraftingPatternSearchRules.sortedByPatternSearchCost(
					BloodStructureRecipe.getAllRecipes(player.level()),
					recipe -> recipe.getPattern().getPatternArray())) {
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
						;

				// Explicit recipe degree / stage check.
				int requiredDegree = RecipeDegreeGates.getRequiredDegree(targetPattern);
				if (!RecipeDegreeGates.playerMeets(player, targetPattern)
						&& !UnstainedAccessRules.bypassesUnstainedLevelGate(targetPattern.getId().getPath(), HemoCapabilityAccess.getUnstainedProgress(player))) {
					var requiredName = RecipeDegreeGates.requirementLabel(targetPattern);
					player.displayClientMessage(
							HemoText.translatable("message.hemomancy.blood_structure_crafting.this_formation_requires_to_be_held",
									targetPattern.getId().getPath(),
									requiredName
											.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
									(targetPattern.isUnstained() ? Component.translatable("message.hemomancy.blood_structure_crafting.purity_stage") : Component.literal(".")).withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.RED),
							false);
					handled = true;
					break;
				}
				// ── Path alignment gate ──
				net.minecraft.network.chat.Component alignError = BloodStructureCraftingHelper.checkPathAlignment(player, targetPattern);
				if (alignError != null) {
					player.displayClientMessage(alignError, false);
					handled = true;
					break;
				}
				if (bloodVolume.getBloodVolume() >= targetPattern.getBloodCost()) {
					// ── Compute structure bounding box ──
					int patW = blockPat.getWidth();
					int patH = blockPat.getHeight();
					int patD = blockPat.getDepth();
					AABB bounds = PatternOrigins.bounds(patternHelper, blockPat);
					int minX = (int) bounds.minX, maxX = (int) bounds.maxX - 1;
					int minY = (int) bounds.minY, maxY = (int) bounds.maxY - 1;
					int minZ = (int) bounds.minZ, maxZ = (int) bounds.maxZ - 1;

					// ── Send blood craft ring animation immediately ──
					BlockPos ringCenter = new BlockPos(
							(minX + maxX) / 2, minY, (minZ + maxZ) / 2);
					float centerY = (minY + maxY) / 2.0f + 0.5f;
					float halfW = (maxX - minX) / 2.0f + 0.5f;
					float halfD = (maxZ - minZ) / 2.0f + 0.5f;
					float startRadius = Math.max(halfW, halfD) + 2.0f;
					int animDuration = 30; // ticks (~1.5 seconds)
					PacketHandler.sendToAll(new PacketBloodCraftRing(ringCenter, startRadius,
									centerY, animDuration));

					// ── Drain blood and consume held item now ──
					ItemStack oldStack = player.getMainHandItem().copy();
					player.setItemInHand(InteractionHand.MAIN_HAND,
							new ItemStack(oldStack.getItem(), oldStack.getCount() - 1));
					BloodAccounting.pay(player, targetPattern.getBloodCost(), STRUCTURE_CRAFT);

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
							Component.translatable("message.hemomancy.blood_structure_crafting.not_enough_blood_can_be_drawn"), true);
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
					if (match != null && !recipe.isUnstained()) {
						showProjectionHint(player);
						handled = true;
						break;
					}
					if (match != null && player.getMainHandItem().getItem() != recipe.getHeldItem().getItem()) {
						// Check progression first, so locked recipes explain the missing degree/stage.
						int requiredDegree = RecipeDegreeGates.getRequiredDegree(recipe);
						if (!RecipeDegreeGates.playerMeets(player, recipe)
								&& !UnstainedAccessRules.bypassesUnstainedLevelGate(recipe.getId().getPath(), HemoCapabilityAccess.getUnstainedProgress(player))) {
							var requiredName = RecipeDegreeGates.requirementLabel(recipe);
							player.displayClientMessage(
									HemoText.translatable("message.hemomancy.blood_structure_crafting.this_formation_requires_to_be_held",
											recipe.getId().getPath(),
											requiredName
													.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
											(recipe.isUnstained() ? Component.translatable("message.hemomancy.blood_structure_crafting.purity_stage") : Component.literal(".")).withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.RED),
									false);
						} else {
							// Path alignment check
							net.minecraft.network.chat.Component alignError = BloodStructureCraftingHelper.checkPathAlignment(player, recipe);
							if (alignError != null) {
								player.displayClientMessage(alignError, false);
							} else {
								player.displayClientMessage(
										HemoText.translatable("message.hemomancy.blood_structure_crafting.this_formation_requires_you_to_hold",
												recipe.getId().getPath(),
												recipe.getHeldItem().getHoverName().copy()
														.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).withStyle(ChatFormatting.RED),
										false);
							}
						}
						handled = true;
						break;
					}
				}
			}
		}
		return handled;
	}

	private static void showProjectionHint(Player player) {
		player.displayClientMessage(
				Component.translatable("message.hemomancy.blood_structure_crafting.to_complete_this_formation_use_blood").withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static BlockPattern.BlockPatternMatch findStructurePatternAtHit(
			BloodStructureRecipe recipe, ServerLevel level, BlockPos hitPos) {
		if (!level.getBlockState(hitPos).is(recipe.getHitBlock())) return null;
		return PatternOrigins.findContaining(recipe.getPattern().getBlockPattern(), level, hitPos);
	}

}
