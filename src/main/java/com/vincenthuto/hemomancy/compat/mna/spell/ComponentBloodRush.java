package com.vincenthuto.hemomancy.compat.mna.spell;

import java.util.Arrays;
import java.util.List;

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

/**
 * A friendly spell component that applies the Blood Rush effect (+move speed,
 * +attack speed) to the target via the MnA spell system. Composable with MnA
 * shapes — cast on allies at range with projectile shape, create a sigil/scar_pattern
 * that buffs anyone who steps on it, or combine with zone shape to create a
 * "blood rally point" area buff.
 */
public class ComponentBloodRush extends PotionEffectComponent {

	public ComponentBloodRush(ResourceLocation guiIcon) {
		super(guiIcon, EffectInit.blood_rush, new AttributeValuePair[]{
				new AttributeValuePair(Attribute.DURATION, 100.0F, 40.0F, 600.0F, 20.0F, 3.0F),
				new AttributeValuePair(Attribute.MAGNITUDE, 1.0F, 1.0F, 3.0F, 1.0F, 10.0F)});
	}

	@Override
	public SoundEvent SoundEffect() {
		return SoundEvents.PLAYER_LEVELUP;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.BLOOD;
	}

	@Override
	public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster,
			ISpellDefinition recipe) {
		if (age <= 10) {
			float particle_spread = 1.2F;
			float v = 0.4F;
			int particleCount = 16;

			for (int i = 0; i < particleCount; ++i) {
				// Upward-rushing particles to convey speed/energy
				double angle = Math.random() * Math.PI * 2.0;
				double radius = 0.3 + Math.random() * 0.6;
				Vec3 velocity = new Vec3(
						Math.cos(angle) * 0.04,
						Math.random() * (double) v,
						Math.sin(angle) * 0.04);
				world.addParticle(
						recipe.colorParticle(new MAParticleType((ParticleType) ParticleInit.DUST.get()), caster),
						impact_position.x + Math.cos(angle) * radius,
						impact_position.y + Math.random() * 0.5,
						impact_position.z + Math.sin(angle) * radius,
						velocity.x, velocity.y, velocity.z);
			}
		}
	}

	@Override
	public float initialComplexity() {
		return 15.0F;
	}

	@Override
	public boolean targetsBlocks() {
		return false;
	}

	@Override
	public int requiredXPForRote() {
		return 100;
	}

	@Override
	public SpellPartTags getUseTag() {
		return SpellPartTags.FRIENDLY;
	}

	@Override
	public List<Affinity> getValidTinkerAffinities() {
		return Arrays.asList(Affinity.ARCANE, Affinity.BLOOD, Affinity.FIRE);
	}
}
