package com.vincenthuto.hemomancy.common.summon;

import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public final class PuppeteerSummonFactory {
	private PuppeteerSummonFactory() {
	}

	public static Optional<Mob> create(PuppeteerSummonDefinition definition, Level level,
									   Player owner, UUID crossbarId, int livingSinewLevel) {
		if (definition == null || level == null || owner == null || crossbarId == null) {
			return Optional.empty();
		}
		Mob mob = switch (definition.name()) {
			case PuppeteerSummonDefinitions.VEINWING_VULTURE -> EntityInit.veinwing_vulture.get().create(level);
			case PuppeteerSummonDefinitions.MARROW_SPITTER -> EntityInit.marrow_spitter.get().create(level);
			case PuppeteerSummonDefinitions.GOREBOUND_HULK -> EntityInit.gorebound_hulk.get().create(level);
			default -> null;
		};
		if (mob instanceof BoundPuppeteerSummon bound) {
			bound.hemomancy$setOwnerUUID(owner.getUUID());
			bound.hemomancy$setCrossbarUUID(crossbarId);
			bound.hemomancy$setSummonName(definition.name());
			BoundSummonBehavior.applyStats(mob, definition, livingSinewLevel);
			mob.setPos(owner.getX(), owner.getY() + 0.15, owner.getZ());
			return Optional.of(mob);
		}
		return Optional.empty();
	}
}
