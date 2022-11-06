package com.vincenthuto.hemomancy.effect;

import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hemomancy.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.network.chat.Component;
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
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		super.applyEffectTick(entity, amplifier);
		if (entity != null) {
			Vector3 centerVec = Vector3.fromEntityCenter(entity);
			if (entity.getEffect(PotionInit.blood_binding.get()) != null) {
				entity.setDeltaMovement(0, 0, 0);
				Level level = entity.level;
				if (level.isClientSide) {
					double time = level.getGameTime();
					level.addParticle(SerpentParticleFactory.createData(new ParticleColor(50, 50, 50)),
							centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
							centerVec.y + Math.sin(time * 0.1) * 0.55f,
							centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
					level.addParticle(SerpentParticleFactory.createData(new ParticleColor(100, 0, 0)),
							centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
							centerVec.y + Math.sin(time * 0.1) * 0.55f,
							centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
					level.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
							centerVec.y + Math.sin(time * 0.1) * 0.55f,
							centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
					level.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
							centerVec.y + Math.sin(time * 0.1) * 0.55f,
							centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
				}
//				if (!level.isClientSide) {
//					if (level instanceof ServerLevel sr) {
//						double time = level.getGameTime();
//						sr.sendParticles(SerpentParticleFactory.createData(new ParticleColor(50, 50, 50)),
//								centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
//								centerVec.y + Math.sin(time * 0.1) * 0.55,
//								centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 1, 0, 0, 0.0, 0);
//
//						sr.sendParticles(SerpentParticleFactory.createData(new ParticleColor(100, 0, 0)),
//								centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
//								centerVec.y + Math.sin(time * 0.1) * 0.55,
//								centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 1, 0, 0, 0.0, 0);
//
//						sr.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
//								centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
//								centerVec.y + Math.sin(time * 0.1) * 0.55,
//								centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 1, 0, 0, 0.0, 0);
//
//						sr.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
//								centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
//								centerVec.y + Math.sin(time * 0.1) * 0.55,
//								centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 1, 0, 0, 0.0, 0);
//					}
//				}
			}
		}

	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);

	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Blood Binding");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}

}
