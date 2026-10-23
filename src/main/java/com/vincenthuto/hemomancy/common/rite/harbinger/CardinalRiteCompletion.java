package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMediumRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedRitePreflight;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

import java.util.UUID;

/**
 * Resolves a finished Cardinal Rite. Each {@link RiteKind} completes in its own way; the ceremony, countdown and
 * Puppeteer kinds share {@link #resolve}: final gates, medium and structure consumption, the result item, rewards,
 * rank advancement and the clean-rite refund. Effects live in {@link CardinalRiteCompletionEffects}. The caller ends
 * the rite through {@link CardinalRiteTermination}.
 */
public final class CardinalRiteCompletion {
	private static final BloodAccounting.Source LEGACY_COMPLETION = BloodAccounting.Source.of("legacy_rite_completion", Category.RITE, false, false);

	/** A kind's own claim on the reward, checked against the matched structure before the medium is spent. */
	@FunctionalInterface
	interface Claim {
		Claim NONE = (level, caster, rite, recipe, layeredMatch) -> true;

		boolean claim(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe,
				CardinalRiteStationMatcher.StationMatch layeredMatch);
	}

	private CardinalRiteCompletion() {
	}

	/**
	 * Completes the rite the way its kind does.
	 *
	 * @return false when a final check refused the rite; nothing was granted
	 */
	public static boolean complete(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null) return false;
		return RiteKind.of(rite, recipe).complete(level, caster, rite, recipe);
	}

	/**
	 * The last checks before a rite may resolve: an Unstained rite's degree and preflight, or a Harbinger rite's
	 * finale refusal. A refused caster is told why.
	 */
	static boolean passesFinalGates(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (recipe.isUnstained()) {
			if (!RecipeDegreeGates.playerMeets(caster, recipe)) {
				caster.displayClientMessage(
						Component.literal("The rite falls silent. It requires ")
								.withStyle(ChatFormatting.GRAY)
								.append(Component.literal(RecipeDegreeGates.requirementLabel(recipe))
										.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)),
						false);
				return false;
			}
			UnstainedRitePreflight.Result unstainedPreflight = UnstainedCardinalRiteEvents.preflight(caster,
					rite.getRecipeId().getPath());
			if (!unstainedPreflight.success()) {
				UnstainedCardinalRiteEvents.announceFailure(caster, unstainedPreflight.failure());
				return false;
			}
			return true;
		}
		Component refusal = CardinalRiteCompletionEffects.refusal(
				new CardinalRiteCompletionEffects.Context(level, caster, rite, recipe, rite.getCenterPos()),
				CardinalRiteCompletionEffects.Stage.FINALE);
		if (refusal != null) {
			caster.displayClientMessage(Component.literal("The rite falls silent. ")
					.withStyle(ChatFormatting.DARK_RED)
					.append(refusal.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)), false);
			return false;
		}
		return true;
	}

	/** The shared resolution of a ceremony, countdown or trial; {@code claim} is the kind's own reward check. */
	static boolean resolve(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite, CardinalRiteRecipe recipe,
			Claim claim) {
		BlockPos center = rite.getCenterPos();
		String ritePath = rite.getRecipeId().getPath();
		CardinalRiteCompletionEffects.Context context =
				new CardinalRiteCompletionEffects.Context(sLevel, caster, rite, recipe, center);
		if (!passesFinalGates(sLevel, caster, rite, recipe)) return false;

		// Interactive Harbinger ceremonies already paid their base cost node by
		// node. Only legacy/Unstained countdown rites retain the completion drain.
		if (rite.getPhase() == CardinalRitePhase.LEGACY) {
			BloodAccounting.pay(caster, recipe.getBloodCost(), LEGACY_COMPLETION);
		}

		// Floors are reusable. A recipe may explicitly consume only its matched upper structure.
		CardinalRiteStationMatcher.StationMatch layeredMatch = recipe.hasLayeredStation()
				? CardinalRiteStructure.layeredStructureMatch(sLevel, rite, recipe)
				: null;
		if (!claim.claim(sLevel, caster, rite, recipe, layeredMatch)) return false;
		if (!consumeRiteMedium(sLevel, caster, rite, recipe)) return false;
		if (recipe.isUnstained()
				&& !UnstainedCardinalRiteEvents.completeRite(sLevel, caster, center, ritePath)) {
			return false;
		}

		// Spawn result item
		if (!recipe.getResult().isEmpty()) {
			ItemStack resultStack = CardinalRiteCompletionEffects.shapeResult(context, recipe.getResult().copy());
			if (!resultStack.isEmpty()) {
				ItemEntity resultDrop = new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5,
						resultStack);
				if (!sLevel.addFreshEntity(resultDrop)) caster.drop(resultStack, false);
			}
		}

		if (!recipe.isUnstained() && !CardinalRiteCompletionEffects.apply(context)) return false;

		if (recipe.isUnstained()) {
			sLevel.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
		} else {
			CardinalRiteBeats.sound(sLevel, center, SoundInit.RITE_COMPLETE, 1.0F, 1.0F);
			CardinalRiteBeats.sound(sLevel, center, SoundInit.RITE_CONSECRATE, 1.0F, 0.9F);
		}

		caster.displayClientMessage(
				Component.literal("The ").append(recipe.displayName()).append(" is complete!")
						.withStyle(recipe.isUnstained() ? ChatFormatting.AQUA : ChatFormatting.GOLD,
								ChatFormatting.BOLD),
				false);

		// Award rite completion milestone (first rite + tiered)
		SkillPointGainEvents.onRiteCompleted(caster);
		LiberKnowledgeHelper.unlockForRite(caster, ritePath);

		Integer targetDegree = RecipeDegreeGates.getRankupTargetDegree(recipe);
		if (targetDegree != null && !recipe.isUnstained()
				&& com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression.advance(
						caster, targetDegree)) {
			CardinalRiteCompletionEffects.triggerSpineProgressionWhisper(sLevel, caster, targetDegree);
		}

		if (!recipe.isUnstained()) refundCleanRite(caster, rite);
		if (rite.commitCompletion()) {
			consumeMatchedStructure(sLevel, recipe, center, layeredMatch);
		}
		return true;
	}

	/** A Puppeteer trial pays out only for its own beaten puppet and the crossbar it was bound to. */
	static boolean claimPuppeteerTrial(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe, CardinalRiteStationMatcher.StationMatch layeredMatch) {
		if (!rite.puppeteerTrial().isDefeated() || layeredMatch == null
				|| !(level.getBlockEntity(rite.getCenterPos()) instanceof CardinalFocusBlockEntity focus)) {
			return false;
		}
		ItemStack seated = focus.getMediumForMatching();
		UUID crossbarId = MarionetteCrossbarItem.getCrossbarId(seated);
		if (!rite.getPlayerUUID().equals(MarionetteCrossbarItem.getBoundOwner(seated))
				|| !java.util.Objects.equals(rite.puppeteerTrial().crossbarId(), crossbarId)) {
			caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.crossbar_replaced")
					.withStyle(ChatFormatting.DARK_RED), false);
			return false;
		}
		var definition = PuppeteerSummonDefinitions.byName(rite.puppeteerTrial().summonName()).orElse(null);
		if (definition == null) return false;
		if (KnownSummonEvents.grantSummon(caster, definition)) {
			caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.complete",
					Component.translatable(definition.translationKey())).withStyle(ChatFormatting.RED), false);
		}
		return true;
	}

	/**
	 * An awakened Reservoir banks blood for its rite. If the ceremony also held
	 * together cleanly (instability stayed low and no anchor was left broken), the
	 * Reservoir returns half of the anchors' blood. Without the Reservoir, rites
	 * keep their contract: no completion debit and no refund.
	 */
	private static void refundCleanRite(ServerPlayer caster, ActiveCardinalRite rite) {
		int anchors = rite.getAnchorBloodMl().length;
		if (anchors == 0 || rite.getInstability() >= CardinalRiteCeremonyRules.CLEAN_RITE_INSTABILITY
				|| !rite.getBrokenInstabilityAnchors().isEmpty()
				|| !rite.isSigilAwakened(Hemomancy.rloc("reservoir").toString())) return;
		int refund = Math.round(rite.fullRequirementMl() * CardinalRiteCeremonyRules.CLEAN_RITE_REFUND);
		if (refund <= 0) return;
		var volume = HemoCapabilityAccess.getBloodVolume(caster);
		volume.fill(refund);
		BloodVolumeEvents.syncVolume(caster, volume);
		caster.displayClientMessage(Component.literal("The rite closes cleanly. The Reservoir returns "
				+ refund + " mL of anchor blood to you.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
	}

	private static boolean consumeRiteMedium(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (!recipe.hasLayeredStation()) return true;
		if (level.getBlockEntity(rite.getCenterPos()) instanceof CardinalFocusBlockEntity focus
				&& CardinalRiteMediumRules.finish(focus, recipe.getMedium(),
						recipe.shouldConsumeMediumOnSuccess())) {
			return true;
		}
		String failure = recipe.hasMedium()
				? "The rite reaches culmination, but its required medium is no longer seated in the Cardinal Focus."
				: "The rite reaches culmination, but an unexpected medium occupies the Cardinal Focus.";
		caster.displayClientMessage(Component.literal(failure)
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
		return false;
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
				: CardinalRiteStructure.findPatternNearCenter(pattern.getBlockPattern(), level, center);
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
					if (!state.isAir()) level.sendParticles(HemoParticleData.bloodCell(new ParticleColor(150, 6, 16)),
							worldPos.getX() + 0.5D, worldPos.getY() + 0.5D, worldPos.getZ() + 0.5D,
							6, 0.3D, 0.3D, 0.3D, 0.03D);
				}
			}
		}
	}
}
