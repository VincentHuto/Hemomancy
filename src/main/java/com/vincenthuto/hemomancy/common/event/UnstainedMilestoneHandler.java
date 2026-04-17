package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
import com.vincenthuto.hemomancy.common.effect.SilverWardEffect;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles the tangible rewards and passive bonuses that unlock at each
 * Unstained purity/clarity stage, giving players concrete reasons to progress:
 *
 * <h3>Purity Stage Rewards</h3>
 * <ul>
 *   <li><b>TAINTED (25):</b> Silver Ward auto-applied while enabled (amplifier 0)</li>
 *   <li><b>CLEANSING (50):</b> Silver Ward amplifier 1 + night vision near Pallid Lanterns</li>
 *   <li><b>ABSOLVED (75):</b> Silver Ward amplifier 2 + bonus damage to hemomancy mobs</li>
 *   <li><b>PURIFIED (100):</b> Silver Ward amplifier 3 (full protection)</li>
 * </ul>
 *
 * <h3>Clarity Stage Rewards</h3>
 * <ul>
 *   <li><b>DISCERNING (25):</b> Verdigris Aura auto-applied while enabled (amplifier 0)</li>
 *   <li><b>VIGILANT (50):</b> Verdigris Aura amplifier 1 + glowing on hemomancy mobs</li>
 *   <li><b>RESOLUTE (75):</b> Verdigris Aura amplifier 2</li>
 *   <li><b>ENLIGHTENED (100):</b> Verdigris Aura amplifier 3 (maximum field radius)</li>
 * </ul>
 *
 * <h3>Silver Ward Damage Reduction</h3>
 * Reduces incoming damage from hemomancy-tagged sources when Silver Ward is active.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
	 * the player's current purity/clarity stage and toggle state.
	 */
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Player player = event.player;
		if (player.level().isClientSide()) return;
		if (!(player instanceof ServerPlayer serverPlayer)) return;
		if (player.tickCount % EFFECT_REFRESH_INTERVAL != 0) return;

		serverPlayer.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress -> {
			if (!progress.hasBegunPurification()) return;

			EnumPurityStage purityStage = EnumPurityStage.byPurity(progress.getPurity());
			EnumClarityStage clarityStage = progress.hasClarityUnlocked()
					? EnumClarityStage.byClarity(progress.getClarity())
					: EnumClarityStage.AWAKENED;

			// ── Silver Ward: unlocked at TAINTED (25+), amplifier scales with stage ──
			if (progress.isSilverWardEnabled() && purityStage.getLevel() >= EnumPurityStage.TAINTED.getLevel()) {
				int wardAmplifier = purityStage.getLevel() - 1; // 0 at TAINTED, 1 at CLEANSING, 2 at ABSOLVED, 3 at PURIFIED
				// Only refresh if no existing effect or existing effect is weaker
				MobEffectInstance existing = serverPlayer.getEffect(EffectInit.silver_ward.get());
				if (existing == null || existing.getAmplifier() < wardAmplifier || existing.getDuration() < 40) {
					serverPlayer.addEffect(new MobEffectInstance(
							EffectInit.silver_ward.get(), AUTO_EFFECT_DURATION, wardAmplifier, false, false, true));
				}
			}

			// ── Verdigris Aura: unlocked at DISCERNING clarity (25+) ──
			if (progress.isVerdigrisAuraEnabled() && progress.hasClarityUnlocked()
					&& clarityStage.getLevel() >= EnumClarityStage.DISCERNING.getLevel()) {
				int auraAmplifier = clarityStage.getLevel() - 1; // 0 at DISCERNING, 1 at VIGILANT, 2 at RESOLUTE, 3 at ENLIGHTENED
				MobEffectInstance existing = serverPlayer.getEffect(EffectInit.verdigris_aura.get());
				if (existing == null || existing.getAmplifier() < auraAmplifier || existing.getDuration() < 40) {
					serverPlayer.addEffect(new MobEffectInstance(
							EffectInit.verdigris_aura.get(), AUTO_EFFECT_DURATION, auraAmplifier, false, false, true));
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
				attacker.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress -> {
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
