package com.vincenthuto.hemomancy.common.summon;

import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
			case PuppeteerSummonDefinitions.MNEMONIST_PUPPET -> EntityInit.mnemonist_puppet.get().create(level);
			case PuppeteerSummonDefinitions.RINGMASTER_PATTERN -> EntityInit.mnemonist_puppet.get().create(level);
			case PuppeteerSummonDefinitions.SCARLET_MUMMER -> EntityInit.scarlet_mummer.get().create(level);
			case PuppeteerSummonDefinitions.SANGUINE_HOUND -> EntityInit.sanguine_hound.get().create(level);
			default -> null;
		};
		if (mob instanceof BoundPuppeteerSummon bound) {
			bound.hemomancy$setOwnerUUID(owner.getUUID());
			mob.setPersistenceRequired();
			bound.hemomancy$setCrossbarUUID(crossbarId);
			bound.hemomancy$setSummonName(definition.name());
			BoundSummonBehavior.applyStats(mob, definition, livingSinewLevel);
			if (owner instanceof ServerPlayer serverOwner) {
				BoundSummonBehavior.bindOwnerSession(mob, serverOwner);
			}
			mob.setPos(owner.getX(), owner.getY() + 0.15, owner.getZ());
			return Optional.of(mob);
		}
		return Optional.empty();
	}

	public static Optional<Mob> createTrial(PuppeteerSummonDefinition definition, ServerLevel level,
			ServerPlayer caster, BlockPos pos) {
		if (definition == null || level == null || caster == null || pos == null) {
			return Optional.empty();
		}
		Mob mob = switch (definition.name()) {
			case PuppeteerSummonDefinitions.VEINWING_VULTURE -> EntityInit.veinwing_vulture.get().create(level);
			case PuppeteerSummonDefinitions.MARROW_SPITTER -> EntityInit.marrow_spitter.get().create(level);
			case PuppeteerSummonDefinitions.GOREBOUND_HULK -> EntityInit.gorebound_hulk.get().create(level);
			case PuppeteerSummonDefinitions.MNEMONIST_PUPPET -> EntityInit.mnemonist_puppet.get().create(level);
			case PuppeteerSummonDefinitions.RINGMASTER_PATTERN -> EntityInit.mnemonist_puppet.get().create(level);
			case PuppeteerSummonDefinitions.SCARLET_MUMMER -> EntityInit.scarlet_mummer.get().create(level);
			case PuppeteerSummonDefinitions.SANGUINE_HOUND -> EntityInit.sanguine_hound.get().create(level);
			default -> null;
		};
		if (mob instanceof BoundPuppeteerSummon bound) {
			bound.hemomancy$setTrialSummon(true);
			bound.hemomancy$setTrialCasterUUID(caster.getUUID());
			bound.hemomancy$setSummonName(definition.name());
			BoundSummonBehavior.applyTrialStats(mob, definition);
			mob.setCustomName(Component.translatable("entity.hemomancy.unbound_summon",
					Component.translatable(definition.translationKey())));
			mob.setCustomNameVisible(true);
			mob.setPos(pos.getX() + 0.5, pos.getY() + 0.15, pos.getZ() + 0.5);
			return Optional.of(mob);
		}
		return Optional.empty();
	}
}
