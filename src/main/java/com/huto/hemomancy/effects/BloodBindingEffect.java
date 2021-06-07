package com.huto.hemomancy.effects;

import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.particle.factory.SerpentParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.math.Vector3;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class BloodBindingEffect extends Effect {

	public BloodBindingEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("Blood Binding");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier,
			double health) {
		super.affectEntity(source, indirectSource, entityLivingBaseIn, amplifier, health);

	}

	@Override
	public void performEffect(LivingEntity entity, int amplifier) {
		super.performEffect(entity, amplifier);
		if (entity != null) {
			Vector3 centerVec = Vector3.fromEntityCenter(entity);
			if (entity.getActivePotionEffect(PotionInit.blood_binding.get()) != null) {
				entity.setMotion(0, 0, 0);
				if (!entity.world.isRemote) {
					ServerWorld sWorld = (ServerWorld) entity.world;
					sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(50, 50, 50)),
							centerVec.x + Math.sin(entity.ticksExisted * 0.3)
									* (0.50 + Math.sin(entity.ticksExisted) * 0.05),
							centerVec.y + Math.sin(entity.ticksExisted * 0.1),
							centerVec.z + Math.cos(entity.ticksExisted * 0.3)
									* (0.50 + Math.sin(entity.ticksExisted) * 0.05),
							2, 0f, 0.0f, 0f, 0);

					sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(100, 0, 0)),
							centerVec.x + Math.sin(entity.ticksExisted * 0.3)
									* (0.50 + Math.sin(entity.ticksExisted) * 0.05),
							centerVec.y + Math.sin(entity.ticksExisted * 0.1),
							centerVec.z + Math.cos(entity.ticksExisted * 0.3)
									* (0.50 + Math.sin(entity.ticksExisted) * 0.05),
							4, 0f, 0.0f, 0f, 0.0005f);
					sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(entity.ticksExisted * 0.3)
									* (0.50 + Math.sin(entity.ticksExisted) * 0.05),
							centerVec.y + Math.sin(entity.ticksExisted * 0.1),
							centerVec.z + Math.cos(entity.ticksExisted * 0.3)
									* (0.50 + Math.sin(entity.ticksExisted) * 0.05),
							8, 0f, 0.0f, 0f, 0.0015f);
					sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
							centerVec.x + Math.sin(entity.ticksExisted * 0.3)
									* (0.50 + Math.sin(entity.ticksExisted) * 0.05),
							centerVec.y + Math.sin(entity.ticksExisted * 0.1),
							centerVec.z + Math.cos(entity.ticksExisted * 0.3)
									* (0.50 + Math.sin(entity.ticksExisted) * 0.05),
							1, 0f, 0.0f, 0f, 0.0035f);
				}
			}
		}

	}

}
