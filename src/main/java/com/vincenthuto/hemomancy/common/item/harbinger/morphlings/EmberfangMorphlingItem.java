package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Emberfang strain that grants increased reflexes by applying the
 * Serpentine Guile effect while equipped. Maturity level scales the speed
 * bonuses (capped at amplifier 2 to prevent extreme stacking).
 * Prefers FLAMMEUS, with DUCTILIS as its secondary affinity.
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Venom Strike applies one attributed Searing stream.
 * - Mature (3): Constrict's repeated strikes sustain that heat.
 * - Apex (4): Ambush Predator extends Searing and retains its earned damage burst.
 */
public class EmberfangMorphlingItem extends MorphlingItem {

	/** Number of hits within the window to trigger Constrict. */
	private static final int CONSTRICT_THRESHOLD = 3;
	/** Time window in ticks for Constrict hits to count (5 seconds). */
	private static final int CONSTRICT_WINDOW = 100;
	/** Minimum sneak ticks required for Ambush Predator (3 seconds). */
	private static final long AMBUSH_SNEAK_TICKS = 60;
	/** Cooldown for Ambush Predator in ticks (8 seconds). */
	private static final int AMBUSH_COOLDOWN = 160;
	private static final int SOVEREIGN_VENOM_WINDOW = 400;
	private static final ResourceLocation HOTHEADED_SPEED_ID = Hemomancy.rloc("hotheaded_movement_speed");
	private static final ResourceLocation HOTHEADED_DAMAGE_ID = Hemomancy.rloc("hotheaded_attack_damage");

	public EmberfangMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	protected String binomialKey() {
		return "morphling.hemomancy.emberfang.binomial";
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.FLAMMEUS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.DUCTILIS;
	}

	@Override
	public boolean tryUse(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
        if (com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(playerIn)) return false;
        if (playerIn instanceof net.minecraft.server.level.ServerPlayer server
                && com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager.isMarionetteChannel(server))
            com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.stop(server, false);
        try (var schoolAbility = MorphlingCombat.scope(this, playerIn, itemStack, null)) {

		LivingEntity target = MorphlingItem.findLookTarget(playerIn, 24.0);
		if (target == null) {
			playerIn.displayClientMessage(Component.literal("No blood-warm target answers the venom."), true);
			return false;
		}
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "SovereignVenom",
				420.0, 700, 220, 0)) return false;
		CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putString("SovereignVenomTarget", target.getStringUUID());
		tag.putLong("SovereignVenomUntil", worldIn.getGameTime() + SOVEREIGN_VENOM_WINDOW);
		tag.putInt("SovereignVenomHits", 0);
		itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, true, false, true));
		return true;

        }
    }

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
        try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {

		int maturity = MorphlingItem.getMaturityLevel(stack);

		// The named passive remains visible; Hotheaded owns its contextual attributes.
		int amplifier = Math.min(MorphlingItem.passiveAmplifier(player, stack, maturity), 2);
		MorphlingItem.applyPassiveEffect(player, stack, EffectInit.morphling_emberfang,
				EffectInit.serpentine_guile, amplifier);

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
    }

	public static void applyHotheadedTick(Player player, ItemStack stack) {
		double benefit = MorphlingItem.getMaturityLevel(stack) >= 2
				? EmberfangHeatRules.benefit(environmentLevel(player)) : 0.0D;
		updateModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), HOTHEADED_SPEED_ID, benefit);
		updateModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), HOTHEADED_DAMAGE_ID, benefit);
	}

	public static float scaleExhaustion(Player player, float amount) {
		return equippedStack(player)
				.filter(stack -> MorphlingItem.getMaturityLevel(stack) >= 2)
				.map(stack -> amount * EmberfangHeatRules.exhaustionMultiplier(environmentLevel(player)))
				.orElse(amount);
	}

	public static float adjustIncomingDamage(Player player, ItemStack stack, float damage) {
		if (MorphlingItem.getMaturityLevel(stack) < 2) return damage;
		return damage * EmberfangHeatRules.incomingDamageMultiplier(environmentLevel(player));
	}

	public static void clearHeatModifiers(Player player) {
		updateModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), HOTHEADED_SPEED_ID, 0.0D);
		updateModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), HOTHEADED_DAMAGE_ID, 0.0D);
	}

	private static int environmentLevel(Player player) {
		float temperature = player.level().getBiome(player.blockPosition()).value().getBaseTemperature();
		return EmberfangHeatRules.environmentLevel(temperature, player.level().dimensionType().ultraWarm(),
				player.isOnFire(), player.isInLava());
	}

	private static java.util.Optional<ItemStack> equippedStack(Player player) {
		return HemoCapabilityAccess.getEquippedMorphling(player)
				.filter(cap -> cap.hasMorphling()
						&& cap.getEquippedMorphling().getItem() instanceof EmberfangMorphlingItem)
				.map(cap -> cap.getEquippedMorphling());
	}

	private static void updateModifier(AttributeInstance attribute, ResourceLocation id, double amount) {
		if (attribute == null) return;
		AttributeModifier existing = attribute.getModifier(id);
		if (existing != null && Math.abs(existing.amount() - amount) > 0.000001D) {
			attribute.removeModifier(id);
			existing = null;
		}
		if (amount > 0.0D && existing == null) {
			attribute.addTransientModifier(new AttributeModifier(id, amount,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		} else if (amount <= 0.0D && existing != null) {
			attribute.removeModifier(id);
		}
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
        try (var schoolAbility = MorphlingCombat.scope(this, player, stack, player.damageSources().magic())) {

		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Venom Strike — apply Searing on a confirmed hit
		if (maturity >= 2) {
			int venomDuration = 60 + (maturity - 2) * 40; // 3s at Developing, 5s Mature, 7s Apex
			MorphlingCombat.afflict(this, player, target, venomDuration);
		}

		// Mature (3+): Constrict — repeated hits on same target build stacks,
		// at 3 stacks the existing heat is refreshed
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
				MorphlingCombat.afflict(this, player, target, 60 + (maturity - 3) * 40);
				// Reset stacks
				tag.putInt("ConstrHits", 0);
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}

		// Apex (4): Ambush Predator — first hit from stealth extends heat and deals its earned burst
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

					// An ambush extends the same burn rather than adding another stream.
					MorphlingCombat.afflict(this, player, target, 200);
					// Bonus magic damage burst.
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
					MorphlingCombat.afflict(this, player, target, 160);
				} else if (hits == 2) {
					MorphlingCombat.afflict(this, player, target, 120);
				} else {
					target.hurt(player.damageSources().magic(), Math.min(18.0f, target.getMaxHealth() * 0.18f));
					MorphlingCombat.afflict(this, player, target, 140);
					tag.remove("SovereignVenomTarget");
					tag.remove("SovereignVenomUntil");
					tag.remove("SovereignVenomHits");
				}
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			}
		}

        }
    }

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Hotheaded + Venom Strike (Heat drives speed, damage, hunger, and risk)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Constrict (Repeated strikes sustain Searing)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Ambush Predator (Sneak 3s for lethal first strike)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Sovereign Venom (Mark one target for heat and an earned burst)", 5, currentMaturity));
		return list;
	}

}
