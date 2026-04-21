package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.effect.SilverWardEffect;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Handles the tangible rewards and passive bonuses that unlock at each
 * Unstained purity/clarity stage, giving players concrete reasons to progress:
 *
 * <h3>Purity Stage Rewards</h3>
 * <ul>
 *   <li><b>TAINTED (25):</b> Verdigris Aura auto-applied while enabled (amplifier 0)</li>
 *   <li><b>CLEANSING (50):</b> Verdigris Aura amplifier 1 + night vision near Pallid Lanterns</li>
 *   <li><b>ABSOLVED (75):</b> Verdigris Aura amplifier 2 + bonus damage to hemomancy mobs</li>
 *   <li><b>PURIFIED (100):</b> Verdigris Aura amplifier 3 (maximum field radius)</li>
 * </ul>
 *
 * <h3>Clarity Stage Rewards</h3>
 * <ul>
 *   <li><b>DISCERNING (25):</b> Silver Ward auto-applied while enabled (amplifier 0)</li>
 *   <li><b>VIGILANT (50):</b> Silver Ward amplifier 1 + glowing on hemomancy mobs</li>
 *   <li><b>RESOLUTE (75):</b> Silver Ward amplifier 2</li>
 *   <li><b>ENLIGHTENED (100):</b> Silver Ward amplifier 3 (full protection)</li>
 * </ul>
 *
 * <h3>Silver Ward Damage Reduction</h3>
 * Reduces incoming damage from hemomancy-tagged sources when Silver Ward is active.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class UnstainedMilestoneHandler {

	/** How often to refresh the passive effects (every 5 seconds). */
	private static final int EFFECT_REFRESH_INTERVAL = 100;
	/** Duration of auto-applied effects (6 seconds — slightly longer than refresh). */
	private static final int AUTO_EFFECT_DURATION = 130;
	/** Extra damage dealt to hemomancy mobs at ABSOLVED+ stage. */
	private static final float ABSOLVED_BONUS_DAMAGE = 2.0f;

	// ════════════════════════════════════════════════════════════
	//  Passive Effect Application (Tick-Based)
	// ════════════════════════════════════════════════════════════

	/**
	 * Periodically applies Silver Ward and Verdigris Aura effects based on
	 * the player's current purity/clarity stage and toggle state, and grants
	 * stage-based advancements whenever a new threshold is crossed.
	 */
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Player player = event.player;
		if (player.level().isClientSide()) return;
		if (!(player instanceof ServerPlayer serverPlayer)) return;
		if (player.tickCount % EFFECT_REFRESH_INTERVAL != 0) return;

		HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
			if (!progress.hasBegunPurification()) return;

			EnumPurityStage purityStage = EnumPurityStage.byPurity(progress.getPurity());
			EnumClarityStage clarityStage = progress.hasClarityUnlocked()
					? EnumClarityStage.byClarity(progress.getClarity())
					: EnumClarityStage.AWAKENED;

			// ── Grant purity stage advancements ──
			checkPurityStageAdvancements(serverPlayer, purityStage);

			// ── Grant clarity stage advancements ──
			if (progress.hasClarityUnlocked()) {
				checkClarityStageAdvancements(serverPlayer, clarityStage);
			}

			// ── Verdigris Aura: unlocked at TAINTED (25+), amplifier scales with purity stage ──
			if (progress.isVerdigrisAuraEnabled() && purityStage.getLevel() >= EnumPurityStage.TAINTED.getLevel()) {
				int auraAmplifier = purityStage.getLevel() - 1; // 0 at TAINTED, 1 at CLEANSING, 2 at ABSOLVED, 3 at PURIFIED
				// Only refresh if no existing effect or existing effect is weaker
				MobEffectInstance existing = serverPlayer.getEffect(EffectInit.verdigris_aura.get());
				if (existing == null || existing.getAmplifier() < auraAmplifier || existing.getDuration() < 40) {
					serverPlayer.addEffect(new MobEffectInstance(
							EffectInit.verdigris_aura.get(), AUTO_EFFECT_DURATION, auraAmplifier, false, false, true));
				}
			}

			// ── Silver Ward: unlocked at DISCERNING clarity (25+), amplifier scales with clarity stage ──
			if (progress.isSilverWardEnabled() && progress.hasClarityUnlocked()
					&& clarityStage.getLevel() >= EnumClarityStage.DISCERNING.getLevel()) {
				int wardAmplifier = clarityStage.getLevel() - 1; // 0 at DISCERNING, 1 at VIGILANT, 2 at RESOLUTE, 3 at ENLIGHTENED
				MobEffectInstance existing = serverPlayer.getEffect(EffectInit.silver_ward.get());
				if (existing == null || existing.getAmplifier() < wardAmplifier || existing.getDuration() < 40) {
					serverPlayer.addEffect(new MobEffectInstance(
							EffectInit.silver_ward.get(), AUTO_EFFECT_DURATION, wardAmplifier, false, false, true));
				}
			}

			// ── Vigilant+ clarity: apply Glowing to nearby hemomancy mobs ──
			if (progress.hasClarityUnlocked() && clarityStage.getLevel() >= EnumClarityStage.VIGILANT.getLevel()) {
				double detectionRadius = 16.0 + clarityStage.getLevel() * 4.0;
				serverPlayer.level().getEntitiesOfClass(LivingEntity.class,
						serverPlayer.getBoundingBox().inflate(detectionRadius),
						e -> e != serverPlayer && e.getType().is(EntityInit.HEMOMANCY_MOB)
				).forEach(mob -> {
					mob.addEffect(new MobEffectInstance(
							net.minecraft.world.effect.MobEffects.GLOWING, AUTO_EFFECT_DURATION, 0, false, false, false));
				});
			}
		});
	}

	// ════════════════════════════════════════════════════════════
	//  Advancement Granting — Purity & Clarity Stage Thresholds
	// ════════════════════════════════════════════════════════════

	/**
	 * Grants purity-stage advancements for any stage the player has already
	 * reached. Safe to call on every tick check because
	 * {@link UnstainedAdvancementGranter#grantIfNotDone} is a no-op once done.
	 */
	private static void checkPurityStageAdvancements(ServerPlayer player, EnumPurityStage stage) {
		if (stage.getLevel() >= EnumPurityStage.TAINTED.getLevel()) {
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_TAINTED);
		}
		if (stage.getLevel() >= EnumPurityStage.CLEANSING.getLevel()) {
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_CLEANSING);
		}
		if (stage.getLevel() >= EnumPurityStage.ABSOLVED.getLevel()) {
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_ABSOLVED);
		}
		if (stage.getLevel() >= EnumPurityStage.PURIFIED.getLevel()) {
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_PURIFIED);
		}
	}

	/**
	 * Grants clarity-stage advancements for any stage the player has reached.
	 * Called only after {@code hasClarityUnlocked()} returns {@code true}.
	 */
	private static void checkClarityStageAdvancements(ServerPlayer player, EnumClarityStage stage) {
		// Always grant clarity_awakened once clarity is unlocked (regardless of stage)
		UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED);
		if (stage.getLevel() >= EnumClarityStage.DISCERNING.getLevel()) {
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_DISCERNING);
		}
		if (stage.getLevel() >= EnumClarityStage.VIGILANT.getLevel()) {
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_VIGILANT);
		}
		if (stage.getLevel() >= EnumClarityStage.RESOLUTE.getLevel()) {
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_RESOLUTE_STAGE);
		}
		if (stage.getLevel() >= EnumClarityStage.ENLIGHTENED.getLevel()) {
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_ENLIGHTENED_SEEKER);
		}
	}

	// ════════════════════════════════════════════════════════════
	//  Damage Modification — Silver Ward Reduction & Absolved Bonus
	// ════════════════════════════════════════════════════════════

	/**
	 * Reduces incoming damage from hemomancy-tagged sources when the player
	 * has Silver Ward active, and applies bonus damage to hemomancy mobs
	 * at ABSOLVED+ purity.
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLivingHurt(LivingHurtEvent event) {
		// ── Silver Ward: reduce incoming blood damage ──
		if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
			MobEffectInstance ward = player.getEffect(EffectInit.silver_ward.get());
			if (ward != null) {
				// Check if the damage source entity is a hemomancy mob
				boolean isBloodDamage = false;
				if (event.getSource().getEntity() instanceof LivingEntity attacker) {
					isBloodDamage = attacker.getType().is(EntityInit.HEMOMANCY_MOB);
				}
				if (isBloodDamage) {
					float reduction = SilverWardEffect.getBloodDamageReduction(ward.getAmplifier());
					event.setAmount(event.getAmount() * (1.0f - reduction));
				}
			}
		}

		// ── Absolved+ bonus: extra damage to hemomancy mobs ──
		if (event.getSource().getEntity() instanceof Player attacker && !attacker.level().isClientSide()) {
			LivingEntity target = event.getEntity();
			if (target.getType().is(EntityInit.HEMOMANCY_MOB)) {
				HemoCapabilityAccess.getUnstainedProgress(attacker).ifPresent(progress -> {
					if (progress.hasBegunPurification()) {
						EnumPurityStage stage = EnumPurityStage.byPurity(progress.getPurity());
						if (stage.getLevel() >= EnumPurityStage.ABSOLVED.getLevel()) {
							event.setAmount(event.getAmount() + ABSOLVED_BONUS_DAMAGE);
						}
					}
				});
			}
		}
	}
}
