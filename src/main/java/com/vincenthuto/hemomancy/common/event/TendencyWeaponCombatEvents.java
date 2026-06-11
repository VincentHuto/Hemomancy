package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.projectile.CombatWeaponCarrierProjectile;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TendencyWeaponHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Objects;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TendencyWeaponCombatEvents {
	private TendencyWeaponCombatEvents() {
	}

	@SubscribeEvent
	public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
		if (event.getAmount() <= 0.0f) {
			return;
		}

		LivingEntity target = event.getEntity();
		Entity sourceEntity = event.getSource().getEntity();
		if (!(sourceEntity instanceof Player player)) {
			return;
		}

		ItemStack weaponStack = resolveWeaponStack(event.getSource().getDirectEntity(), player);
		if (weaponStack.isEmpty()) {
			return;
		}

		EnumBloodTendency weaponTendency = TendencyWeaponHelper.getWeaponTendency(weaponStack).orElse(null);
		if (weaponTendency == null) {
			return;
		}

		EnumBloodTendency secondaryTendency = TendencyWeaponHelper.getWeaponSecondaryTendency(weaponStack).orElse(null);
		float multiplier = TendencyWeaponHelper.getDamageMultiplier(player, target, weaponTendency, secondaryTendency);
		if (multiplier <= 1.0f) {
			return;
		}

		event.setAmount(event.getAmount() * multiplier);
	}

	private static ItemStack resolveWeaponStack(Entity directEntity, Player player) {
		if (directEntity instanceof CombatWeaponCarrierProjectile combatProjectile) {
			return combatProjectile.getCombatWeaponItem();
		}
		if (directEntity instanceof AbstractArrow arrow) {
			return Objects.requireNonNullElse(arrow.getWeaponItem(), ItemStack.EMPTY);
		}
		if (directEntity == player) {
			return player.getMainHandItem();
		}
		return ItemStack.EMPTY;
	}
}


