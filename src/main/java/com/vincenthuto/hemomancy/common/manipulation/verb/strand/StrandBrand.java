package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

/**
 * A brand a strand set on a creature (core-loop Track 12): who set it, in which school, and the game time it lapses.
 * It lives on the creature as the {@code hemomancy:strand_brand} attachment, saved with it, and is taken off once it
 * lapses, so an unbranded creature saves nothing.
 */
public record StrandBrand(UUID caster, EnumBloodTemper school, long until) {
	public static final StrandBrand NONE = new StrandBrand(new UUID(0L, 0L), EnumBloodTemper.ANIMUS, 0L);
	public static final Codec<StrandBrand> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("caster").forGetter(StrandBrand::caster),
			Codec.STRING.xmap(EnumBloodTemper::valueOf, EnumBloodTemper::name).fieldOf("school").forGetter(StrandBrand::school),
			Codec.LONG.fieldOf("until").forGetter(StrandBrand::until)).apply(instance, StrandBrand::new));

	/** Whether this brand names {@code caster} and still holds at {@code now}. */
	public boolean names(UUID caster, long now) {
		return this.caster.equals(caster) && BrandRules.holds(until, now);
	}
}
