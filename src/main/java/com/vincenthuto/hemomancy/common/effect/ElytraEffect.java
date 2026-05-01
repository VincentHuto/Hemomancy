package com.vincenthuto.hemomancy.common.effect;

import org.jetbrains.annotations.NotNull;

import com.vincenthuto.hemomancy.common.init.AttributeInit;

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

	public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
		return true;
	}

	private static void setFlightEnabled(Player player, boolean enabled) {
		if (player.getAbilities().mayfly == enabled) return;
		if (enabled) {
			player.getAbilities().mayfly = true;
		} else {
			boolean creative = player.isCreative() || player.isSpectator();
			player.getAbilities().mayfly = creative;
			if (!creative) {
				player.getAbilities().flying = false;
			} else {
				player.getAbilities().setFlyingSpeed(0.05F);
			}
		}
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.onUpdateAbilities();
		}
	}

	private static void setFlySpeed(Player player, float speed) {
		player.getAbilities().setFlyingSpeed(speed);
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.onUpdateAbilities();
		}
	}

	@Override
	public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
		if (livingEntity instanceof Player player) {
			// Enable flight ability for hovering when not on ground
			if (!player.onGround() && !player.isInWater()) {
				setFlightEnabled(player, true);
			} else {
				setFlightEnabled(player, false);
			}

			// Apply elytra gliding physics only when actively fall-flying
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

			}
		}
	
		return true;
	}

	@Override
	public void removeAttributeModifiers(@NotNull AttributeMap attributeMap) {
		super.removeAttributeModifiers(attributeMap);
	}

	@Override
	public void onMobRemoved(LivingEntity living, int amplifier, net.minecraft.world.entity.Entity.RemovalReason reason) {
		super.onMobRemoved(living, amplifier, reason);
		if (living instanceof ServerPlayer serverPlayer) {
			setFlightEnabled(serverPlayer, false);
		} else if (living instanceof Player player && living.level().isClientSide) {
			setFlySpeed(player, 0.05F);
		}
	}
}