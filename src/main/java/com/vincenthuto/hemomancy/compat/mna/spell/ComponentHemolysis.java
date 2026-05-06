package com.vincenthuto.hemomancy.compat.mna.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.spells.components.PotionEffectComponent;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

/**
 * A harmful spell component that applies the Hemolysis effect (blood
 * destruction / DoT) to the target via the MnA spell system. When combined
 * with MnA's spell modifier system, the DoT behaviour can be tuned (extended,
 * amplified) in ways that Hemomancy's potion system doesn't support. Pair with
 * AoE/zone shapes for area denial.
 */
public class ComponentHemolysis extends PotionEffectComponent {

	public ComponentHemolysis(ResourceLocation guiIcon) {
		super(guiIcon, EffectInit.hemolysis, new AttributeValuePair[]{
				new AttributeValuePair(Attribute.DURATION, 40.0F, 20.0F, 200.0F, 20.0F, 5.0F),
				new AttributeValuePair(Attribute.MAGNITUDE, 1.0F, 1.0F, 4.0F, 1.0F, 12.0F)});
	}

	@Override
	public SoundEvent SoundEffect() {
		return SoundEvents.WITHER_HURT;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.BLOOD;
	}

	@Override
	public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster,
			ISpellDefinition recipe) {
		if (age <= 10) {
			float particle_spread = 0.8F;
			float v = 0.2F;
			int particleCount = 18;

			for (int i = 0; i < particleCount; ++i) {
				// Chaotic, sharp particles to convey destruction
				double angle = Math.random() * Math.PI * 2.0;
				double radius = 0.1 + Math.random() * 0.8;
				Vec3 velocity = new Vec3(
						(Math.random() - 0.5) * 0.15,
						(Math.random() - 0.5) * (double) v,
						(Math.random() - 0.5) * 0.15);
				world.addParticle(
						recipe.colorParticle(new MAParticleType((ParticleType) ParticleInit.DUST.get()), caster),
						impact_position.x + Math.cos(angle) * radius,
						impact_position.y + Math.random() * (double) particle_spread,
						impact_position.z + Math.sin(angle) * radius,
						velocity.x, velocity.y, velocity.z);
			}
		}
	}

	@Override
	public float initialComplexity() {
		return 30.0F;
	}

	@Override
	public boolean targetsBlocks() {
		return false;
	}

	@Override
	public int requiredXPForRote() {
		return 200;
	}

	@Override
	public SpellPartTags getUseTag() {
		return SpellPartTags.HARMFUL;
	}

	@Override
	public List<Affinity> getValidTinkerAffinities() {
		return Arrays.asList(Affinity.ARCANE, Affinity.BLOOD, Affinity.ENDER);
	}
}
