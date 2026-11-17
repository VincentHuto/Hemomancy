package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class HemoDamageTypes {
    public static final ResourceKey<DamageType> SEARING = ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("searing"));
    public static final ResourceKey<DamageType> DECAY_RELEASE = ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("decay_release"));
    /** Kinbud's Stung: its own type so deaths read right; not Poison (Brined Court balance rule 6). */
    public static final ResourceKey<DamageType> STUNG = ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("stung"));
    /** A vial or syringe drawing a sample (core-loop Track 5): sampling costs the creature health. Skips the hurt cooldown. */
    public static final ResourceKey<DamageType> BLOOD_DRAW = ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("blood_draw"));

    /**
     * Absorption drawing blood from a creature (core-loop Track 12, CL-24): it names the one drawing, so the creature
     * turns on them and a kill is theirs, but a kill by draw pays no kill bounty ({@link #paysKillBounty}).
     */
    public static final ResourceKey<DamageType> BLOOD_ABSORPTION = ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("blood_absorption"));

    public static DamageSource bloodAbsorption(Level level, Entity drawer) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(BLOOD_ABSORPTION), drawer, drawer);
    }

    /**
     * Whether a kill by {@code source} pays the killer's bounty (kill blood and the Temperament kill shift). A kill by
     * draw does not: the draw already paid its blood and Temper, so the creature would be counted twice.
     */
    public static boolean paysKillBounty(DamageSource source) {
        return source == null || !source.is(BLOOD_ABSORPTION);
    }

    public static DamageSource attributed(DamageSource source, SchoolHitContext context, @Nullable Entity owner) {
        return SchoolDamage.attributed(source, context, owner);
    }

    public static DamageSource stateDamage(Level level, ResourceKey<DamageType> type,
            SchoolHitContext context, @Nullable Entity owner) {
        return attributed(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(type), owner, owner), context, owner);
    }
	public static final ResourceKey<DamageType> PHANTASMAL_ECHO =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("phantasmal_echo"));
	public static final ResourceKey<DamageType> VESPER_IMPALE =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("vesper_impale"));
	public static final ResourceKey<DamageType> VESPER_REND =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("vesper_rend"));
	public static final ResourceKey<DamageType> VESPER_SCUTE =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("vesper_scute"));
	public static final ResourceKey<DamageType> LIVING_TORCH_BREATH =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("living_torch_breath"));
	public static final ResourceKey<DamageType> LIVING_FLAIL_FREEZE =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("living_flail_freeze"));
	/** The Mycophant's claim on a held or siphoned player: never scaled by difficulty, so the nonlethal clamp holds. */
	public static final ResourceKey<DamageType> MYCOPHANT_CLAIM =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("mycophant_claim"));
	/** A broken fusion's bonus wound: bypasses the hurt cooldown so it never swallows the blows around it. */
	public static final ResourceKey<DamageType> MYCOPHANT_REFUSAL =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("mycophant_refusal"));
	public static final ResourceKey<DamageType> PALE_INTERCESSION =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("pale_intercession"));

	/** A Gardens gill snapping shut on a stranger. */
	public static final ResourceKey<DamageType> GILL_BITE =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("gill_bite"));

	/** A Saint's consecrated ground rejecting an Apotheos body. */
	public static final ResourceKey<DamageType> HALLOWED_BURN =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("hallowed_burn"));

	private HemoDamageTypes() {
	}

	public static DamageSource hallowedBurn(Level level) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(HALLOWED_BURN));
	}

	public static DamageSource gillBite(Level level) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(GILL_BITE));
	}

	public static DamageSource phantasmalEcho(Level level, @Nullable Entity directEntity,
			@Nullable Entity causingEntity) {
		return new DamageSource(level.registryAccess()
				.registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(PHANTASMAL_ECHO), directEntity, causingEntity);
	}

	public static DamageSource vesperImpale(Level level, Entity boss) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(VESPER_IMPALE), boss, boss);
	}

	/** The burrowing mount's claws raking up through the floor; ignores armor. */
	public static DamageSource vesperRend(Level level, Entity boss) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(VESPER_REND), boss, boss);
	}

	public static DamageSource vesperScute(Level level, Entity projectile, Entity boss) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(VESPER_SCUTE), projectile, boss);
	}

	public static DamageSource livingTorchBreath(Level level, Entity caster) {
        SchoolHitContext hit = SchoolDamage.current();
        if (hit == null) hit = SchoolHitContext.direct(Hemomancy.rloc("living_torch_breath"),
                com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper.FLAMMEUS, null, caster);
        return attributed(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(LIVING_TORCH_BREATH), caster, caster), hit, caster);
    }

	public static DamageSource livingFlailFreeze(Level level, Entity projectile, Entity owner) {
        SchoolHitContext hit = SchoolDamage.projectileContext(projectile);
        if (hit == null) hit = SchoolHitContext.direct(Hemomancy.rloc("living_flail"),
                com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper.CONGEATIO, null, owner);
        return attributed(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(LIVING_FLAIL_FREEZE), projectile, owner), hit, owner);
    }

	public static DamageSource mycophantClaim(Level level, Entity boss) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(MYCOPHANT_CLAIM), boss, boss);
	}

	public static DamageSource mycophantRefusal(Level level, Entity player) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(MYCOPHANT_REFUSAL), player, player);
	}

	public static DamageSource paleIntercession(Level level, Entity manifestation, Entity owner) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(PALE_INTERCESSION), manifestation, owner);
	}
}
