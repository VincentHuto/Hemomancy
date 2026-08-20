package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingArsenalInventoryGuard;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingWeaponGraftRecipeUnlockEvents {
	private LivingWeaponGraftRecipeUnlockEvents() {
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !isLivingArsenalKill(player)) {
			return;
		}
		LivingEntity victim = event.getEntity();
		ArtificerAssignments.onLivingArsenalKill(player);
		if (bloodFraction(player) >= 0.75D) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.BLADE);
		}
		if (victim.hasEffect(MobEffects.GLOWING)) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.SPEAR);
		}
		if (isClawsAmbush(player)) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.CLAWS);
		}
		if (victim.isOnFire()) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.TORCH);
		}
		if (isFlailControlled(victim)) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.FLAIL);
		}
	}

	public static void onAxeAlignedManipulation(LivingEntity caster) {
		if (caster instanceof ServerPlayer player) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.AXE);
		}
	}

	public static void onSpearAlignedManipulation(LivingEntity caster) {
		if (caster instanceof ServerPlayer player) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.SPEAR);
		}
	}

	public static void onTorchAlignedManipulation(LivingEntity caster) {
		if (caster instanceof ServerPlayer player) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.TORCH);
		}
	}

	public static void onFlailAlignedManipulation(LivingEntity caster) {
		if (caster instanceof ServerPlayer player) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.FLAIL);
		}
	}

	public static void onConductiveArcTriggered(LivingEntity attacker) {
		if (attacker instanceof ServerPlayer player) {
			LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, LivingWeaponForm.CROSSBOW);
		}
	}

	private static double bloodFraction(ServerPlayer player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive() || volume.getMaxBloodVolume() <= 0.0D) {
			return 0.0D;
		}
		return volume.getBloodVolume() / volume.getMaxBloodVolume();
	}

	private static boolean isLivingArsenalKill(ServerPlayer player) {
		return LivingArsenalInventoryGuard.isLivingArsenalItem(player.getMainHandItem())
				|| LivingArsenalInventoryGuard.isLivingArsenalItem(player.getOffhandItem());
	}

	private static boolean isClawsAmbush(ServerPlayer player) {
		return player.hasEffect(MobEffects.INVISIBILITY)
				|| player.level().getMaxLocalRawBrightness(player.blockPosition()) <= 7;
	}

	private static boolean isFlailControlled(LivingEntity victim) {
		return victim.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
				|| victim.getTicksFrozen() > 0;
	}
}
