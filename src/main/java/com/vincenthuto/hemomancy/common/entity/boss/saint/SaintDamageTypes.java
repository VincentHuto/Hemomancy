package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * The Saints' damage types ({@code data/hemomancy/damage_type/saint_*.json}, message ids {@code hemomancy.<id>}, so
 * deaths read {@code death.attack.hemomancy.<id>}), all in {@code #hemomancy:saint_damage}. None is fire: Putriciel's
 * cleansing fire is its own type so its deaths read right.
 */
public final class SaintDamageTypes {
	public static final ResourceKey<DamageType> HEMORATH = key("saint_hemorath");
	public static final ResourceKey<DamageType> SERAPHAE = key("saint_seraphae");
	public static final ResourceKey<DamageType> PUTRICIEL = key("saint_putriciel");
	public static final ResourceKey<DamageType> PUTRICIEL_CLEANSING = key("saint_putriciel_cleansing");
	public static final ResourceKey<DamageType> VELORUM = key("saint_velorum");
	public static final TagKey<DamageType> SAINT_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("saint_damage"));

	private SaintDamageTypes() {
	}

	/** The Saint's own type. */
	public static ResourceKey<DamageType> of(EnumSaintType saint) {
		return switch (saint) {
			case HEMORATH -> HEMORATH;
			case SERAPHAE -> SERAPHAE;
			case PUTRICIEL -> PUTRICIEL;
			case VELORUM -> VELORUM;
		};
	}

	private static ResourceKey<DamageType> key(String id) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc(id));
	}
}
