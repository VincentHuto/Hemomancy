package com.vincenthuto.hemomancy.compat.mna.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

/**
 * A spell component that drains the target's (or caster's) Hemomancy blood
 * volume and converts it into mana for the caster. The inverse of
 * {@link ComponentManaToBlood}.
 * <p>
 * When used with FRIENDLY tag (self-cast or allied player target), drains
 * the target's blood and gives mana to the caster. When paired with harmful
 * shapes (projectile at enemy), drains enemy blood and gives mana to caster.
 * <p>
 * The MAGNITUDE attribute controls how much blood to drain (50–500).
 * Conversion ratio: configurable via server config (blood_to_mana_ratio).
 */
public class ComponentBloodToMana extends SpellEffect {

	public ComponentBloodToMana(ResourceLocation guiIcon) {
		super(guiIcon, new AttributeValuePair[]{
				new AttributeValuePair(Attribute.MAGNITUDE, 50.0F, 50.0F, 500.0F, 25.0F, 5.0F)});
	}

	@Override
	public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target,
			IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {

		if (!source.isPlayerCaster()) {
			return ComponentApplicationResult.FAIL;
		}

		Player caster = source.getPlayer();

		// Resolve the blood source — if targeting a living entity (player or mob), drain them
		LivingEntity bloodSource;
		if (target.isLivingEntity() && target.getLivingEntity() != null) {
			bloodSource = target.getLivingEntity();
		} else {
			// Self-cast if no entity target
			bloodSource = caster;
		}

		// Get blood capability on the blood source
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(bloodSource).orElse(null);
		if (blood == null || !blood.isActive()) {
			return ComponentApplicationResult.FAIL;
		}

		// Get mana capability on the caster
		IPlayerMagic magic = caster.getCapability(PlayerMagicProvider.MAGIC).orElse(null);
		if (magic == null) {
			return ComponentApplicationResult.FAIL;
		}

		float bloodToDrain = modificationData.getValue(Attribute.MAGNITUDE);

		// Don't drain if the source would overstrain
		if (blood.wouldOverstrain(bloodToDrain)) {
			return ComponentApplicationResult.FAIL;
		}

		// Don't waste blood if mana is already full
		float currentMana = magic.getCastingResource().getAmount();
		float maxMana = magic.getCastingResource().getMaxAmount();
		if (currentMana >= maxMana) {
			return ComponentApplicationResult.FAIL;
		}

		// Drain blood, grant mana
		blood.drain(bloodToDrain);
		float manaGain = bloodToDrain * com.vincenthuto.hemomancy.config.HemoMnAConfig.BLOOD_TO_MANA_RATIO.get().floatValue();

		// Cap so we don't exceed the player's max mana
		float actualGain = Math.min(manaGain, maxMana - currentMana);
		magic.getCastingResource().restore(actualGain);

		return ComponentApplicationResult.SUCCESS;
	}

	@Override
	public SoundEvent SoundEffect() {
		return SoundEvents.BREWING_STAND_BREW;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.BLOOD;
	}

	@Override
	public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster,
			ISpellDefinition recipe) {
		if (age <= 10) {
			float particle_spread = 1.0F;
			float v = 0.3F;
			int particleCount = 16;

			for (int i = 0; i < particleCount; ++i) {
				// Converging particles — blood being drawn inward
				double angle = Math.random() * Math.PI * 2.0;
				double radius = 0.5 + Math.random() * 0.8;
				Vec3 velocity = new Vec3(
						-Math.cos(angle) * 0.08,
						Math.random() * (double) v * 0.5,
						-Math.sin(angle) * 0.08);
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
		return 25.0F;
	}

	@Override
	public boolean targetsBlocks() {
		return false;
	}

	@Override
	public int requiredXPForRote() {
		return 175;
	}

	@Override
	public SpellPartTags getUseTag() {
		return SpellPartTags.FRIENDLY;
	}

	@Override
	public List<Affinity> getValidTinkerAffinities() {
		return Arrays.asList(Affinity.ARCANE, Affinity.BLOOD, Affinity.ENDER);
	}
}
