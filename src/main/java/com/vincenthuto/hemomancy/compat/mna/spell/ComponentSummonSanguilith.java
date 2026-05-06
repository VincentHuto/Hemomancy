package com.vincenthuto.hemomancy.compat.mna.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
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
import com.mna.tools.SummonUtils;
import com.vincenthuto.hemomancy.compat.mna.entity.MnAPluginEntityInit;
import com.vincenthuto.hemomancy.compat.mna.entity.SanguilithEntity;
import com.vincenthuto.hemomancy.compat.mna.faction.HarbingerEventHandler;
import com.vincenthuto.hemomancy.config.HemoMnAConfig;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

/**
 * A spell component that summons a Sanguilith entity at the target location.
 * The Sanguilith is a blood-themed summoned monster that uses MnA's summon/target
 * utilities. Making it a proper spell component integrates it into MnA's summon
 * ecosystem alongside Animated Constructs and other faction summons.
 * <p>
 * Players can customise it with MnA modifiers (longer duration, more damage, etc.).
 * Requires Harbinger faction membership.
 */
public class ComponentSummonSanguilith extends SpellEffect {

	public ComponentSummonSanguilith(ResourceLocation guiIcon) {
		super(guiIcon, new AttributeValuePair[]{
				new AttributeValuePair(Attribute.DURATION, 200.0F, 100.0F, 600.0F, 50.0F, 2.0F),
				new AttributeValuePair(Attribute.MAGNITUDE, 1.0F, 1.0F, 3.0F, 1.0F, 8.0F)});
	}

	@Override
	public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target,
			IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {

		// Prevent duplicate summons in the same cast
		if (source.hasCasterReference() && context.hasEntityBeenAffected(this, source.getCaster())) {
			return ComponentApplicationResult.FAIL;
		}
		if (source.hasCasterReference()) {
			context.addAffectedEntity(this, source.getCaster());
		}

		// Find a clear spot above the target
		int offsetCount;
		for (offsetCount = 0; !context.getServerLevel().isEmptyBlock(target.getBlock().above(offsetCount))
				&& offsetCount < 5; ++offsetCount) {
		}
		if (offsetCount >= 5) {
			return ComponentApplicationResult.FAIL;
		}

		int durationTicks = (int) (modificationData.getValue(Attribute.DURATION) * 20.0F);
		float magnitude = modificationData.getValue(Attribute.MAGNITUDE);

		SanguilithEntity sanguilith = MnAPluginEntityInit.sanguilith.get().create(context.getServerLevel());
		if (sanguilith == null) {
			return ComponentApplicationResult.FAIL;
		}

		DifficultyInstance difficulty = context.getServerLevel().getCurrentDifficultyAt(target.getBlock());
		sanguilith.finalizeSpawn(context.getServerLevel(), difficulty, MobSpawnType.TRIGGERED,
				(SpawnGroupData) null, (CompoundTag) null);
		sanguilith.setPos(
				target.getPosition().x,
				target.getPosition().y + offsetCount,
				target.getPosition().z);
		sanguilith.invulnerableTime = 60;
		sanguilith.setPersistenceRequired();

		// Scale damage based on magnitude
		// Base health/damage scaling with magnitude
		if (magnitude > 1.0F) {
			float healthBonus = (float) ((magnitude - 1.0F) * HemoMnAConfig.SANGUILITH_HEALTH_PER_MAGNITUDE.get());
			float currentMax = sanguilith.getMaxHealth();
			sanguilith.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
					.setBaseValue(currentMax + healthBonus);
			sanguilith.setHealth(sanguilith.getMaxHealth());
		}

		if (source.hasCasterReference()) {
			sanguilith.setSummoner(source.getCaster(), durationTicks);
			SummonUtils.clampTrackedEntities(source.getCaster());
			SummonUtils.setSummon(sanguilith, source.getCaster(), true, durationTicks);
			SummonUtils.addTrackedEntity(source.getCaster(), sanguilith);
		}

		context.getServerLevel().addFreshEntity(sanguilith);

		return ComponentApplicationResult.SUCCESS;
	}

	@Override
	public IFaction getFactionRequirement() {
		return HarbingerEventHandler.HARBINGERS_FACTION;
	}

	@Override
	public boolean canBeChanneled() {
		return false;
	}

	@Override
	public SoundEvent SoundEffect() {
		return SoundEvents.WITHER_SPAWN;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.BLOOD;
	}

	@Override
	public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster,
			ISpellDefinition recipe) {
		if (age <= 15) {
			float particle_spread = 2.0F;
			int particleCount = 20;

			for (int i = 0; i < particleCount; ++i) {
				double angle = Math.random() * Math.PI * 2.0;
				double radius = 0.5 + Math.random() * 1.5;
				Vec3 velocity = new Vec3(
						-Math.cos(angle) * 0.1,
						Math.random() * 0.3,
						-Math.sin(angle) * 0.1);
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
		return 40.0F;
	}

	@Override
	public boolean targetsBlocks() {
		return true;
	}

	@Override
	public int requiredXPForRote() {
		return 300;
	}

	@Override
	public SpellPartTags getUseTag() {
		return SpellPartTags.FRIENDLY;
	}

	@Override
	public List<Affinity> getValidTinkerAffinities() {
		return Arrays.asList(Affinity.BLOOD, Affinity.ENDER);
	}
}
