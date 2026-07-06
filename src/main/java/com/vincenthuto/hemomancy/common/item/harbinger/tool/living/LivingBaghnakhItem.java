package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.Vec3;

public class LivingBaghnakhItem extends LivingToolItem {

	public LivingBaghnakhItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -1.8f, EnumBloodTendency.TENEBRIS, tier, builderIn);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean hit = super.hurtEnemy(stack, target, attacker);
		if (!hit || LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)) {
			return hit;
		}
		if (attacker.level() instanceof ServerLevel serverLevel) {
			Vec3 forward = attacker.getLookAngle().normalize();
			Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.56D, 0.0D);
			PacketHandler.sendClawSlash(center, forward, new ParticleColor(70, 0, 125),
					true, 0.82F, 48.0D, serverLevel);
		}
		return hit;
	}

}
