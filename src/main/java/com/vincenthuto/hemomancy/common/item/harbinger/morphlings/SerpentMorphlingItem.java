package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Serpent morphling that grants increased reflexes by applying the
 * Serpentine Guile effect while equipped. Maturity level scales the speed
 * bonuses (capped at amplifier 2 to prevent extreme stacking).
 * Prefers DUCTILIS (flexibility/neurotic energy fuels reflexes) with
 * FLAMMEUS as secondary (fervent heat drives quickness).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Venom Strike — melee attacks apply scaling Poison to
 *   targets (serpent injects venom through the player's strikes)
 * - Mature (3): Constrict — repeated strikes on the same target build
 *   constriction stacks; at 3 stacks the target is rooted and crushed
 *   with burst Wither damage (serpent coils and squeezes prey)
 * - Apex (4): Ambush Predator — the first melee attack after sneaking for
 *   3+ seconds deals triple venom damage and inflicts Darkness, rewarding
 *   patient, calculated strikes (apex serpent ambush from stealth)
 */
public class SerpentMorphlingItem extends MorphlingItem {

	/** Number of hits within the window to trigger Constrict. */
	private static final int CONSTRICT_THRESHOLD = 3;
	/** Time window in ticks for Constrict hits to count (5 seconds). */
	private static final int CONSTRICT_WINDOW = 100;
	/** Minimum sneak ticks required for Ambush Predator (3 seconds). */
	private static final long AMBUSH_SNEAK_TICKS = 60;
	/** Cooldown for Ambush Predator in ticks (8 seconds). */
	private static final int AMBUSH_COOLDOWN = 160;
	private static final int SOVEREIGN_VENOM_WINDOW = 400;

	public SerpentMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.DUCTILIS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.FLAMMEUS;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		LivingEntity target = MorphlingItem.findLookTarget(playerIn, 24.0);
		if (target == null) {
			playerIn.displayClientMessage(Component.literal("No blood-warm target answers the venom."), true);
			return;
		}
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "SovereignVenom",
				420.0, 700, 220, 0)) return;
		CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putString("SovereignVenomTarget", target.getStringUUID());
		tag.putLong("SovereignVenomUntil", worldIn.getGameTime() + SOVEREIGN_VENOM_WINDOW);
		tag.putInt("SovereignVenomHits", 0);
		itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, true, false, true));
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Serpentine Guile (move/attack speed, amplifier capped at 2)
		int amplifier = Math.min(maturity, 2);
		if (!player.hasEffect(EffectInit.serpentine_guile)) {
			player.addEffect(new MobEffectInstance(EffectInit.serpentine_guile,
					100, maturity, false, true, true));
		}

		// Track sneak start time for Ambush Predator (Apex 4)
		if (maturity >= 4 && !player.level().isClientSide) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (player.isShiftKeyDown()) {
				if (!tag.contains("SneakStart")) {
					tag.putLong("SneakStart", player.level().getGameTime());
				}
			} else {
				tag.remove("SneakStart");
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Venom Strike — apply Poison on melee hit
		if (maturity >= 2) {
			int venomDuration = 60 + (maturity - 2) * 40; // 3s at Developing, 5s Mature, 7s Apex
			int venomAmplifier = (maturity >= 4) ? 1 : 0; // Poison II at Apex
			target.addEffect(new MobEffectInstance(MobEffects.POISON,
					venomDuration, venomAmplifier, true, true, true));
		}

		// Mature (3+): Constrict — repeated hits on same target build stacks,
		// at 3 stacks the target is rooted and takes burst Wither damage
		if (maturity >= 3 && !player.level().isClientSide) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			String targetId = target.getStringUUID();
			long now = player.level().getGameTime();

			String lastTargetId = tag.getString("ConstrTarget");
			long lastHitTime = tag.getLong("ConstrLastHit");
			int hits = tag.getInt("ConstrHits");

			if (targetId.equals(lastTargetId) && (now - lastHitTime) <= CONSTRICT_WINDOW) {
				hits++;
			} else {
				hits = 1;
			}

			tag.putString("ConstrTarget", targetId);
			tag.putLong("ConstrLastHit", now);
			tag.putInt("ConstrHits", hits);

			if (hits >= CONSTRICT_THRESHOLD) {
				// Root the target (Slowness 127 = effectively frozen)
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						40, 127, true, true, true)); // 2 seconds
				// Burst Wither damage (constriction crush)
				int witherDuration = 60 + (maturity - 3) * 40; // 3s at Mature, 5s at Apex
				target.addEffect(new MobEffectInstance(MobEffects.WITHER,
						witherDuration, 1, true, true, true)); // Wither II
				// Reset stacks
				tag.putInt("ConstrHits", 0);
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}

		// Apex (4): Ambush Predator — first hit from stealth deals triple venom + Darkness
		if (maturity >= 4 && !player.level().isClientSide) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (tag.contains("SneakStart")) {
				long sneakStart = tag.getLong("SneakStart");
				long now = player.level().getGameTime();
				long lastAmbush = getLastAbilityTick(stack, "AmbushPredator");

				if ((now - sneakStart) >= AMBUSH_SNEAK_TICKS
						&& (now - lastAmbush) >= AMBUSH_COOLDOWN) {
					setLastAbilityTick(stack, "AmbushPredator", now);
					tag.remove("SneakStart");
					stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

					// Triple venom: apply Poison II with extended duration
					target.addEffect(new MobEffectInstance(MobEffects.POISON,
							200, 2, true, true, true)); // Poison III for 10s
					// Darkness — serpent strikes from shadow
					target.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
							80, 0, true, true, true)); // 4 seconds
					// Bonus magic damage burst (serpent fang strike)
					target.hurt(player.damageSources().magic(), 6.0f);
				}
			}
		}

		if (MorphlingItem.isPrimal(stack) && !player.level().isClientSide) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (target.getStringUUID().equals(tag.getString("SovereignVenomTarget"))
					&& tag.getLong("SovereignVenomUntil") > player.level().getGameTime()) {
				int hits = tag.getInt("SovereignVenomHits") + 1;
				tag.putInt("SovereignVenomHits", hits);
				if (hits == 1) {
					target.addEffect(new MobEffectInstance(MobEffects.POISON,
							160, 2, true, true, true));
				} else if (hits == 2) {
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
							80, 127, true, true, true));
					target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
							120, 1, true, true, true));
				} else {
					target.hurt(player.damageSources().magic(), Math.min(18.0f, target.getMaxHealth() * 0.18f));
					target.addEffect(new MobEffectInstance(MobEffects.WITHER,
							140, 1, true, true, true));
					tag.remove("SovereignVenomTarget");
					tag.remove("SovereignVenomUntil");
					tag.remove("SovereignVenomHits");
				}
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Venom Strike (Poison on melee hit)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Constrict (3 hits roots & crushes target)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Ambush Predator (Sneak 3s for lethal first strike)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Sovereign Venom (Staff active marks one target for escalating venom)", 5, currentMaturity));
		return list;
	}

}
