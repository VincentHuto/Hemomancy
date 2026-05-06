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
 * A spell component that converts the caster's MnA mana into Hemomancy blood
 * volume on the target. The MAGNITUDE attribute controls how much mana is
 * consumed per cast, and the blood received is scaled by a conversion ratio.
 */
public class ComponentManaToBlood extends SpellEffect {

	/** How many blood points are gained per 1 mana consumed. */
	private static final float BLOOD_PER_MANA = 5.0F;

	public ComponentManaToBlood(ResourceLocation guiIcon) {
		super(guiIcon, new AttributeValuePair[]{
				new AttributeValuePair(Attribute.MAGNITUDE, 50.0F, 10.0F, 200.0F, 10.0F, 5.0F)});
	}

	@Override
	public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target,
			IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {

		if (!source.isPlayerCaster()) {
			return ComponentApplicationResult.FAIL;
		}

		Player caster = source.getPlayer();

		// Resolve the target — if aimed at a player, fill their blood; otherwise self-cast
		LivingEntity recipient;
		if (target.isLivingEntity() && target.getLivingEntity() instanceof Player) {
			recipient = target.getLivingEntity();
		} else {
			recipient = caster;
		}

		// Get mana capability
		IPlayerMagic magic = caster.getCapability(PlayerMagicProvider.MAGIC).orElse(null);
		if (magic == null) {
			return ComponentApplicationResult.FAIL;
		}

		// Get blood capability on the recipient
		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(recipient).orElse(null);
		if (blood == null || !blood.isActive()) {
			return ComponentApplicationResult.FAIL;
		}

		float manaCost = modificationData.getValue(Attribute.MAGNITUDE);

		// Make sure the caster has enough mana
		if (!magic.getCastingResource().hasEnough(caster, manaCost)) {
			return ComponentApplicationResult.FAIL;
		}

		// Don't waste mana if blood is already full
		if (blood.isFull()) {
			return ComponentApplicationResult.FAIL;
		}

		// Consume mana, grant blood
		magic.getCastingResource().consume(caster, manaCost);
		double bloodGain = manaCost * BLOOD_PER_MANA;
		blood.fill(bloodGain);

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
			float v = 0.35F;
			int particleCount = 14;

			for (int i = 0; i < particleCount; ++i) {
				Vec3 velocity = new Vec3(
						(Math.random() - 0.5) * 0.1,
						Math.random() * (double) v,
						(Math.random() - 0.5) * 0.1);
				world.addParticle(
						recipe.colorParticle(new MAParticleType((ParticleType) ParticleInit.DUST.get()), caster),
						impact_position.x + (double) (-particle_spread)
								+ Math.random() * (double) particle_spread * 2.0,
						impact_position.y + Math.random() * (double) particle_spread,
						impact_position.z + (double) (-particle_spread)
								+ Math.random() * (double) particle_spread * 2.0,
						velocity.x, velocity.y, velocity.z);
			}
		}
	}

	@Override
	public float initialComplexity() {
		return 20.0F;
	}

	@Override
	public boolean targetsBlocks() {
		return false;
	}

	@Override
	public int requiredXPForRote() {
		return 150;
	}

	@Override
	public SpellPartTags getUseTag() {
		return SpellPartTags.FRIENDLY;
	}

	@Override
	public List<Affinity> getValidTinkerAffinities() {
		return Arrays.asList(Affinity.ARCANE, Affinity.BLOOD, Affinity.WATER);
	}
}
