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

/**
 * A harmful spell component that applies the Blood Binding effect to the
 * target, rooting them in place and preventing movement. Duration and magnitude
 * scale with spell attributes.
 */
public class ComponentBloodBinding extends PotionEffectComponent {

	public ComponentBloodBinding(ResourceLocation guiIcon) {
		super(guiIcon, EffectInit.blood_binding, new AttributeValuePair[]{
				new AttributeValuePair(Attribute.DURATION, 60.0F, 20.0F, 300.0F, 20.0F, 5.0F),
				new AttributeValuePair(Attribute.MAGNITUDE, 1.0F, 1.0F, 4.0F, 1.0F, 12.0F)});
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
			float particle_spread = 1.0F;
			float v = 0.2F;
			int particleCount = 16;

			for (int i = 0; i < particleCount; ++i) {
				double angle = Math.random() * Math.PI * 2.0;
				double radius = 0.3 + Math.random() * 0.7;
				Vec3 velocity = new Vec3(
						Math.cos(angle) * 0.05,
						-Math.random() * (double) v,
						Math.sin(angle) * 0.05);
				world.addParticle(
						recipe.colorParticle(new MAParticleType((ParticleType) ParticleInit.DUST.get()), caster),
						impact_position.x + Math.cos(angle) * radius,
						impact_position.y + Math.random() * (double) particle_spread,
						impact_position.z + Math.sin(angle) * radius,
						velocity.x, velocity.y, velocity.z);
			}
		}
	}

	public float initialComplexity() {
		return 25.0F;
	}

	public boolean targetsBlocks() {
		return false;
	}

	public int requiredXPForRote() {
		return 150;
	}

	public List<Affinity> getValidTinkerAffinities() {
		return Arrays.asList(Affinity.ARCANE, Affinity.BLOOD, Affinity.EARTH);
	}
}
