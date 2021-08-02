package com.huto.hemomancy.effect;

import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.particle.factory.SerpentParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.math.Vector3;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BloodBindingEffect extends MobEffect {

	public BloodBindingEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public Component getDisplayName() {
		return new TextComponent("Blood Binding");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);

	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		super.applyEffectTick(entity, amplifier);
		if (entity != null) {
			Vector3 centerVec = Vector3.fromEntityCenter(entity);
			if (entity.getEffect(PotionInit.blood_bindForSetuping.get()) != null) {
				entity.setDeltaMovement(0, 0, 0);
				if (!entity.level.isClientSide) {
					ServerLevel sLevel = (ServerLevel) entity.level;
					sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(50, 50, 50)),
							centerVec.x + Math.sin(entity.tickCount * 0.3) * (0.50 + Math.sin(entity.tickCount) * 0.05),
							centerVec.y + Math.sin(entity.tickCount * 0.1),
							centerVec.z + Math.cos(entity.tickCount * 0.3) * (0.50 + Math.sin(entity.tickCount) * 0.05),
							2, 0f, 0.0f, 0f, 0);

					sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(100, 0, 0)),
							centerVec.x + Math.sin(entity.tickCount * 0.3) * (0.50 + Math.sin(entity.tickCount) * 0.05),
							centerVec.y + Math.sin(entity.tickCount * 0.1),
							centerVec.z + Math.cos(entity.tickCount * 0.3) * (0.50 + Math.sin(entity.tickCount) * 0.05),
							4, 0f, 0.0f, 0f, 0.0005f);
					sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(entity.tickCount * 0.3) * (0.50 + Math.sin(entity.tickCount) * 0.05),
							centerVec.y + Math.sin(entity.tickCount * 0.1),
							centerVec.z + Math.cos(entity.tickCount * 0.3) * (0.50 + Math.sin(entity.tickCount) * 0.05),
							8, 0f, 0.0f, 0f, 0.0015f);
					sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(entity.tickCount * 0.3) * (0.50 + Math.sin(entity.tickCount) * 0.05),
							centerVec.y + Math.sin(entity.tickCount * 0.1),
							centerVec.z + Math.cos(entity.tickCount * 0.3) * (0.50 + Math.sin(entity.tickCount) * 0.05),
							1, 0f, 0.0f, 0f, 0.0035f);
				}
			}
		}

	}

}
