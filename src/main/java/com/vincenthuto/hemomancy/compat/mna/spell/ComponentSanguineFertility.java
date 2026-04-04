package com.vincenthuto.hemomancy.compat.mna.spell;

import java.util.Arrays;
import java.util.List;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX.Spell.Buff;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.spells.components.PotionEffectComponent;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentSanguineFertility extends PotionEffectComponent {

	public ComponentSanguineFertility(ResourceLocation guiIcon) {
		super(guiIcon, EffectInit.sanguine_fertility, new AttributeValuePair[]{
				new AttributeValuePair(Attribute.DURATION, 120.0F, 30.0F, 600.0F, 30.0F, 4.0F),
				new AttributeValuePair(Attribute.MAGNITUDE, 1.0F, 1.0F, 3.0F, 1.0F, 10.0F)});
	}

	public SoundEvent SoundEffect() {
		return Buff.EARTH;
	}

	public Affinity getAffinity() {
		return Affinity.BLOOD;
	}

	public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster,
			ISpellDefinition recipe) {
		if (age <= 10) {
			float particle_spread = 1.5F;
			float v = 0.3F;
			int particleCount = 12;

			for (int i = 0; i < particleCount; ++i) {
				Vec3 velocity = new Vec3(0.0, Math.random() * (double) v, 0.0);
				world.addParticle(
						recipe.colorParticle(new MAParticleType((ParticleType) ParticleInit.DUST.get()), caster),
						impact_position.x + (double) (-particle_spread)
								+ Math.random() * (double) particle_spread * 2.0,
						impact_position.y + (double) (-particle_spread)
								+ Math.random() * (double) particle_spread * 2.0,
						impact_position.z + (double) (-particle_spread)
								+ Math.random() * (double) particle_spread * 2.0,
						velocity.x, velocity.y, velocity.z);
			}
		}
	}

	public float initialComplexity() {
		return 15.0F;
	}

	public boolean targetsBlocks() {
		return false;
	}

	public int requiredXPForRote() {
		return 100;
	}

	public List<Affinity> getValidTinkerAffinities() {
		return Arrays.asList(Affinity.ARCANE, Affinity.EARTH, Affinity.WATER);
	}
}
