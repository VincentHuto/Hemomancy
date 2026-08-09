package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.projectile.CombatWeaponCarrierProjectile;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TendencyWeaponHelper;
import com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper;
import com.vincenthuto.hemomancy.common.manipulation.TendencyDamageCarrier;
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

		Entity directEntity = event.getSource().getDirectEntity();
		ItemStack weaponStack = resolveWeaponStack(directEntity, player);
		EnumBloodTendency weaponTendency = directEntity instanceof TendencyDamageCarrier carrier
				? carrier.getDamageTendency() : null;
		boolean carriedTendency = weaponTendency != null;
		if (weaponTendency == null) {
			weaponTendency = TendencyWeaponHelper.getWeaponTendency(weaponStack).orElse(null);
		}
		if (weaponTendency == null) {
			return;
		}

		EnumBloodTendency secondaryTendency = carriedTendency && directEntity instanceof TendencyDamageCarrier carrier
				? carrier.getSecondaryDamageTendency()
				: TendencyWeaponHelper.getWeaponSecondaryTendency(weaponStack).orElse(null);
		SchoolHitHelper.tryTriggerConductiveArc(player, target, weaponTendency, secondaryTendency, event.getAmount());
		float multiplier = TendencyWeaponHelper.getDamageMultiplier(player, target, weaponTendency, secondaryTendency);
		if (multiplier == 1.0f) {
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


