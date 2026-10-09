package com.vincenthuto.hemomancy.common.summon;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** The one place a summon definition is matched to the entity type that embodies it. */
public final class PuppeteerSummonBodies {
	private static final Map<String, Supplier<? extends EntityType<? extends Mob>>> BODIES = Map.of(
			PuppeteerSummonDefinitions.VEINWING_VULTURE, EntityInit.veinwing_vulture,
			PuppeteerSummonDefinitions.MARROW_SPITTER, EntityInit.marrow_spitter,
			PuppeteerSummonDefinitions.GOREBOUND_HULK, EntityInit.gorebound_hulk,
			PuppeteerSummonDefinitions.SCARLET_MUMMER, EntityInit.scarlet_mummer,
			PuppeteerSummonDefinitions.SANGUINE_HOUND, EntityInit.sanguine_hound,
			PuppeteerSummonDefinitions.CINDER_BELLOWS, EntityInit.cinder_bellows,
			PuppeteerSummonDefinitions.RINGMASTER_PATTERN, EntityInit.ringmaster_pattern,
			PuppeteerSummonDefinitions.MNEMONIST_PUPPET, EntityInit.mnemonist_puppet);

	private PuppeteerSummonBodies() {
	}

	public static Optional<Mob> create(String summonName, Level level) {
		Supplier<? extends EntityType<? extends Mob>> type = summonName == null ? null : BODIES.get(summonName);
		return type == null || level == null ? Optional.empty() : Optional.ofNullable(type.get().create(level));
	}

	public static boolean hasBody(String summonName) {
		return summonName != null && BODIES.containsKey(summonName);
	}
}
