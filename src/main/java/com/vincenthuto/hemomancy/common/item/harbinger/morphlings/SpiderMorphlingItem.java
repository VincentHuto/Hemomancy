package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Spider morphling that passively repairs vascular damage by applying the
 * Arachnid Anastomosis effect while equipped. Maturity level scales the
 * vascular repair rate. Prefers TENEBRIS (darkness/shadow fuels the web)
 * with LUX as secondary (light reveals and mends hidden damage).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Wall Climbing — grant the player spider-like wall climbing
 *   ability (onClimbable override)
 * - Mature (3): Silk Tether — when the player would take fall damage, spawn a
 *   temporary cobweb block at their feet to break the fall
 * - Apex (4): Web Cocoon — when damaged, briefly immobilize and Poison the
 *   attacker with sticky venom-laced webbing
 */
public class SpiderMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Web Cocoon triggers (5 seconds). */
	private static final int WEB_COCOON_COOLDOWN = 100;

	/**
	 * Tag applied to players with an equipped spider morphling at maturity 2+.
	 * Checked by a mixin or event to enable wall climbing.
	 */
	public static final String WALL_CLIMB_TAG = "hemomancy:spider_climb";

	public SpiderMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.TENEBRIS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.LUX;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Arachnid Anastomosis (vascular repair, amplifier = maturity)
		if (!player.hasEffect(EffectInit.arachnid_anastomosis)) {
			player.addEffect(new MobEffectInstance(EffectInit.arachnid_anastomosis,
					100, maturity, false, true, true));
		}

		// Developing (2+): Wall Climbing — enable spider climb via entity tag
		if (maturity >= 2) {
			if (!player.getTags().contains(WALL_CLIMB_TAG)) {
				player.addTag(WALL_CLIMB_TAG);
			}
			// Simulate wall climbing: if the player is against a wall and not on ground,
			// arrest downward velocity (like a spider clinging to a wall) and reset fall
			if (player.horizontalCollision && !player.onGround()) {
				double yVel = player.getDeltaMovement().y;
				// Cling to wall: prevent falling, allow slow upward drift
				player.setDeltaMovement(player.getDeltaMovement().x,
						Math.max(yVel, -0.05), player.getDeltaMovement().z);
				player.fallDistance = 0;
			}
		} else {
			player.removeTag(WALL_CLIMB_TAG);
		}
	}

	@Override
	public boolean onEquippedFall(Player player, ItemStack stack, float distance) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Mature (3+): Silk Tether — spawn a temporary cobweb at feet to break the fall
		if (maturity >= 3) {
			BlockPos feetPos = player.blockPosition();
			EquippedMorphlingEvents.placeTemporaryWeb(player.level(), feetPos);
			return true; // Cancel fall damage
		}
		return false;
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Apex (4): Web Cocoon — immobilize and Poison the attacker
		if (maturity >= 4 && source.getEntity() instanceof LivingEntity attacker) {
			long lastCocoon = getLastAbilityTick(stack, "WebCocoon");
			long now = player.level().getGameTime();
			if (now - lastCocoon >= WEB_COCOON_COOLDOWN) {
				setLastAbilityTick(stack, "WebCocoon", now);

				// Root the attacker (Slowness 127 = effectively frozen) and Poison them
				attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						60, 127, true, true, true));
				attacker.addEffect(new MobEffectInstance(MobEffects.POISON,
						80, 1, true, true, true));
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Wall Climbing (Spider-climb up walls)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Silk Tether (Spawn web to break falls)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Web Cocoon (Root & Poison attacker)", 4, currentMaturity));
		return list;
	}

}
