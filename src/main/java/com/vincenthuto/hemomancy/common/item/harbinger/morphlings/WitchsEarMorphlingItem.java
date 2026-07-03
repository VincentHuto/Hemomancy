package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Witch's Ear strain for local signal reading, falling control, and dark-state
 * pursuit.
 */
public class WitchsEarMorphlingItem extends MorphlingItem {

	public WitchsEarMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	protected String binomialKey() {
		return "morphling.hemomancy.witchs_ear.binomial";
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.DUCTILIS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.TENEBRIS;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "Echothesis",
				260.0, 500, 160, 0)) return;
		AABB area = playerIn.getBoundingBox().inflate(36.0);
		for (LivingEntity entity : worldIn.getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity != playerIn && entity.isAlive())) {
			entity.addEffect(new MobEffectInstance(MobEffects.GLOWING,
					220, 0, true, false, true));
		}
		int lightLevel = worldIn.getMaxLocalRawBrightness(playerIn.blockPosition());
		if (lightLevel < 5) {
			playerIn.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
					180, 1, true, false, true));
			playerIn.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
					180, 1, true, false, true));
			for (Monster mob : worldIn.getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
				mob.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
						100, 0, true, true, true));
			}
		}
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		int amplifier = MorphlingItem.passiveAmplifier(player, stack, maturity);

		if (!player.hasEffect(EffectInit.echoic_perception)) {
			player.addEffect(new MobEffectInstance(EffectInit.echoic_perception,
					100, amplifier, false, true, true));
		}

		if (maturity >= 3 && !player.onGround() && player.isShiftKeyDown()) {
			if (!player.hasEffect(MobEffects.SLOW_FALLING)) {
				player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
						20, 0, true, false, true));
			}
		}

		if (maturity >= 4 && !player.level().isClientSide) {
			int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
			if (lightLevel < 4) {
				if (!player.hasEffect(MobEffects.DAMAGE_BOOST)) {
					player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
							40, 1, true, false, true));
				}
				if (!player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
					player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
							40, 0, true, false, true));
				}
			}
		}
	}

	@Override
	public boolean onEquippedFall(Player player, ItemStack stack, float distance) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		return maturity >= 3 && distance * 0.25f <= 3.0f;
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Echoic Perception (Reveal nearby signals)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Membrane Glide (Slow falling and reduced fall damage)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Night-State Pursuit (Strength in darkness)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Echothesis (Staff active reveals blood signatures and empowers night raids)", 5, currentMaturity));
		return list;
	}
}
