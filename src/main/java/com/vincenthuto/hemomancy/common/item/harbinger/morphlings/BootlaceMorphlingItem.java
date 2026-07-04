package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Bootlace strain for wall movement, fall arrest, and sneaking web nests.
 */
public class BootlaceMorphlingItem extends MorphlingItem {

	private static final int WEB_NEST_COOLDOWN = 100;
	public static final String WALL_CLIMB_TAG = "hemomancy:spider_climb";

	public BootlaceMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	protected String binomialKey() {
		return "morphling.hemomancy.bootlace.binomial";
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
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "WebOfRedThread",
				250.0, 160, 120, 0)) return;
		LivingEntity target = MorphlingItem.findLookTarget(playerIn, 22.0);
		if (target != null) {
			Vec3 pull = playerIn.position().subtract(target.position()).normalize().scale(1.35);
			target.push(pull.x, 0.35, pull.z);
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
					100, 127, true, true, true));
			target.addEffect(new MobEffectInstance(MobEffects.POISON,
					100, 1, true, true, true));
		} else {
			Vec3 leap = playerIn.getLookAngle().normalize().scale(1.25);
			playerIn.push(leap.x, Math.max(0.25, leap.y + 0.25), leap.z);
			playerIn.fallDistance = 0;
			playerIn.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
					80, 0, true, false, true));
		}
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		int amplifier = MorphlingItem.passiveAmplifier(player, stack, maturity);

		if (!player.hasEffect(EffectInit.arachnid_anastomosis)) {
			player.addEffect(new MobEffectInstance(EffectInit.arachnid_anastomosis,
					100, amplifier, false, true, true));
		}

		if (maturity >= 2) {
			if (!player.getTags().contains(WALL_CLIMB_TAG)) {
				player.addTag(WALL_CLIMB_TAG);
			}
			if (player.horizontalCollision && !player.onGround()) {
				double yVel = player.getDeltaMovement().y;
				player.setDeltaMovement(player.getDeltaMovement().x,
						Math.max(yVel, -0.05), player.getDeltaMovement().z);
				player.fallDistance = 0;
			}
		} else {
			player.removeTag(WALL_CLIMB_TAG);
		}

		runProximityWebNest(player, stack, maturity);
	}

	@Override
	public boolean onEquippedFall(Player player, ItemStack stack, float distance) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		if (maturity >= 3) {
			EquippedMorphlingEvents.placeTemporaryWeb(player.level(), player.blockPosition());
			return true;
		}
		return false;
	}

	private void runProximityWebNest(Player player, ItemStack stack, int maturity) {
		if (maturity < 4 || player.level().isClientSide || !player.isShiftKeyDown()) {
			return;
		}
		long now = player.level().getGameTime();
		long lastNest = getLastAbilityTick(stack, "WebNest");
		if (now - lastNest < WEB_NEST_COOLDOWN) {
			return;
		}
		AABB area = player.getBoundingBox().inflate(4.0);
		boolean placedAny = false;
		for (Monster target : player.level().getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
			BlockPos webPos = target.blockPosition();
			EquippedMorphlingEvents.placeTemporaryWeb(player.level(), webPos);
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
					60, 2, true, true, true));
			placedAny = true;
		}
		if (placedAny) {
			setLastAbilityTick(stack, "WebNest", now);
			MorphlingItem.addHusbandryProgress(stack, 1);
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Wall Climbing (Spider-climb up walls)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Silk Tether (Spawn web to break falls)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Web Nest (Sneaking traps nearby hostiles)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Web of Red Thread (Staff active pulls, roots, or tethers movement)", 5, currentMaturity));
		return list;
	}
}
