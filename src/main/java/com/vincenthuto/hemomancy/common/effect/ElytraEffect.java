package com.vincenthuto.hemomancy.common.effect;

import org.jetbrains.annotations.NotNull;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ElytraEffect extends MobEffect {
	public ElytraEffect() {
		super(MobEffectCategory.BENEFICIAL, 0);
	}

	public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
		return true;
	}

	@Override
	public void applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
		if (livingEntity instanceof Player player) {
			Hemomancy.instance.proxy.setFlightEnabled(player, true);
			if (!player.isCreative() && !player.isSpectator()) {

				Hemomancy.instance.proxy.setFlySpeed(player, amplifier*5/100f);
			} else {
				Hemomancy.instance.proxy.setFlySpeed(player, 0.05F);
			}

			if (player.isFallFlying()) {

				Vec3 look = player.getLookAngle();
				Vec3 pos;
				float maxLength;
				double lookScale;
				Vec3 scaled_look;
				if (!player.isShiftKeyDown()) {
					pos = player.getDeltaMovement();
					maxLength = 1.75F;
					lookScale = 0.06D;
					scaled_look = look.scale(lookScale);
					pos = pos.add(scaled_look);
					if (pos.length() > (double) maxLength) {
						pos = pos.scale((double) maxLength / pos.length());
					}
					player.setDeltaMovement(pos);
				} else {
					// magic.getCastingResource().consume(player,
					// CommonConfig.getElytraManaCostPerTick() / 2.0f);

					pos = player.getDeltaMovement();
					maxLength = 0.1F;
					lookScale = -0.01D;
					scaled_look = look.scale(lookScale);
					pos = pos.add(scaled_look);
					if (pos.length() < (double) maxLength) {
						pos = pos.scale((double) maxLength / pos.length());
					}
					player.setDeltaMovement(pos);
				}

				if (player.level().isClientSide) {
					pos = player.position().add(look.scale(3.0D));
					for (int i = 0; i < 5; ++i) {
						player.level().addParticle(ParticleTypes.MYCELIUM, pos.x - 0.5D + Math.random(),
								pos.y - 0.5D + Math.random(), pos.z - 0.5D + Math.random(), -look.x, -look.y, -look.z);
					}
				}

			} else {
				player.stopFallFlying();
			}
		}
	}

	@Override
	public void removeAttributeModifiers(@NotNull LivingEntity living, @NotNull AttributeMap attributemods,
			int p_111187_3_) {
		super.removeAttributeModifiers(living, attributemods, p_111187_3_);
		if (living instanceof ServerPlayer) {
			Hemomancy.instance.proxy.setFlightEnabled((ServerPlayer) living, false);
		} else if (living instanceof Player && living.level().isClientSide) {
			Hemomancy.instance.proxy.setFlySpeed((Player) living, 0.05F);
		}
	}
}