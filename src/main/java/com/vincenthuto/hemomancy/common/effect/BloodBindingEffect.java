package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.client.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BloodBindingEffect extends MobEffect {

	public BloodBindingEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		super.applyEffectTick(entity, amplifier);
		if (entity != null) {
			if (entity.getEffect(EffectInit.blood_binding) != null) {
				entity.setDeltaMovement(0, 0, 0);
				Level level = entity.level();
				Vector3 centerVec = Vector3.fromEntityCenter(entity);
				if (!level.isClientSide && level instanceof ServerLevel sr) {
					double time = level.getGameTime();
					double orbitX = Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05);
					double orbitY = Math.sin(time * 0.1) * 0.55;
					double orbitZ = Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05);
					sr.sendParticles(SerpentParticleFactory.createData(new ParticleColor(50, 50, 50)),
							centerVec.x + orbitX, centerVec.y + orbitY, centerVec.z + orbitZ,
							1, 0, 0, 0, 0);
					sr.sendParticles(SerpentParticleFactory.createData(new ParticleColor(100, 0, 0)),
							centerVec.x + orbitX, centerVec.y + orbitY, centerVec.z + orbitZ,
							1, 0, 0, 0, 0);
					sr.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + orbitX, centerVec.y + orbitY, centerVec.z + orbitZ,
							1, 0, 0, 0, 0);
					sr.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + orbitX, centerVec.y + orbitY, centerVec.z + orbitZ,
							1, 0, 0, 0, 0);
				}
			}
		}
	
		return true;
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.blood_binding");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

}
