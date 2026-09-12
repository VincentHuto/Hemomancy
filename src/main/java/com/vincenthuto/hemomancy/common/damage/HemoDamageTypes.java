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
	public static final ResourceKey<DamageType> VESPER_SCUTE =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("vesper_scute"));
	public static final ResourceKey<DamageType> LIVING_TORCH_BREATH =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("living_torch_breath"));
	public static final ResourceKey<DamageType> LIVING_FLAIL_FREEZE =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("living_flail_freeze"));
	public static final ResourceKey<DamageType> PALE_INTERCESSION =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("pale_intercession"));

	private HemoDamageTypes() {
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

	public static DamageSource vesperScute(Level level, Entity projectile, Entity boss) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(VESPER_SCUTE), projectile, boss);
	}

	public static DamageSource livingTorchBreath(Level level, Entity caster) {
        SchoolHitContext hit = SchoolDamage.current();
        if (hit == null) hit = SchoolHitContext.direct(Hemomancy.rloc("living_torch_breath"),
                com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.FLAMMEUS, null, caster);
        return attributed(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(LIVING_TORCH_BREATH), caster, caster), hit, caster);
    }

	public static DamageSource livingFlailFreeze(Level level, Entity projectile, Entity owner) {
        SchoolHitContext hit = SchoolDamage.projectileContext(projectile);
        if (hit == null) hit = SchoolHitContext.direct(Hemomancy.rloc("living_flail"),
                com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.CONGEATIO, null, owner);
        return attributed(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(LIVING_FLAIL_FREEZE), projectile, owner), hit, owner);
    }

	public static DamageSource paleIntercession(Level level, Entity manifestation, Entity owner) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(PALE_INTERCESSION), manifestation, owner);
	}
}
