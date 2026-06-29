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
	public static final ResourceKey<DamageType> PHANTASMAL_ECHO =
			ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("phantasmal_echo"));

	private HemoDamageTypes() {
	}

	public static DamageSource phantasmalEcho(Level level, @Nullable Entity directEntity,
			@Nullable Entity causingEntity) {
		return new DamageSource(level.registryAccess()
				.registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(PHANTASMAL_ECHO), directEntity, causingEntity);
	}
}
