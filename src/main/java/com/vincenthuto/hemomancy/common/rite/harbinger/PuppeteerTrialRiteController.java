package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureOfferingBurst;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonFactory;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.tile.functional.CardinalFocusBlockEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Owns manifestation and combat tracking for Cardinal Rite puppeteer ordeals. */
public final class PuppeteerTrialRiteController {
	private PuppeteerTrialRiteController() {
	}

	public static PuppeteerTrialRiteRules.MediumStatus mediumStatus(ServerPlayer caster, ItemStack crossbar) {
		UUID owner = MarionetteCrossbarItem.getBoundOwner(crossbar);
		UUID crossbarId = MarionetteCrossbarItem.getCrossbarId(crossbar);
		if (!(crossbar.getItem() instanceof MarionetteCrossbarItem) || crossbarId == null) {
			owner = null;
		}
		boolean activeBodies = crossbarId != null
				&& !MarionetteCrossbarItem.activeSummonsForCrossbar(caster, crossbarId, null).isEmpty();
		return PuppeteerTrialRiteRules.mediumStatus(owner, caster.getUUID(), activeBodies);
	}

	public static boolean canBegin(ServerPlayer caster, ItemStack crossbar, String summonName, double bloodCost,
			boolean notify) {
		PuppeteerTrialRiteRules.MediumStatus status = mediumStatus(caster, crossbar);
		if (status != PuppeteerTrialRiteRules.MediumStatus.READY) {
			if (notify) caster.displayClientMessage(Component.translatable(switch (status) {
				case UNATTUNED -> "hemomancy.summon.crossbar.unattuned";
				case FOREIGN -> "hemomancy.summon.crossbar.foreign";
				case ACTIVE_BODIES -> "hemomancy.summon.trial.crossbar_active";
				case READY -> "hemomancy.summon.trial.failed";
			}), true);
			return false;
		}
		boolean known = HemoCapabilityAccess.getKnownSummons(caster)
				.map(value -> value.getKnownSummonNames().contains(summonName)).orElse(false);
		if (known) {
			if (notify) caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.already_known"), true);
			return false;
		}
		boolean enoughBlood = HemoCapabilityAccess.getBloodVolume(caster)
				.map(value -> value.getBloodVolume() >= bloodCost).orElse(false);
		if (!enoughBlood && notify) {
			caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.no_blood"), true);
		}
		return enoughBlood;
	}

	public static void tick(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (!rite.isPuppeteerTrialManifested()) {
			manifest(level, caster, rite, recipe);
			return;
		}
		Entity tracked = rite.getPuppeteerTrialEntityId() == null
				? null : level.getEntity(rite.getPuppeteerTrialEntityId());
		if (!(tracked instanceof Mob puppet) || !puppet.isAlive()) {
			if (!rite.isPuppeteerTrialDefeated()
					&& PuppeteerTrialRiteRules.missingEntityExpired(rite.incrementPuppeteerTrialMissingTicks())) {
				rite.markCollapsed();
			}
			return;
		}
		rite.resetPuppeteerTrialMissingTicks();
		rite.updatePuppeteerTrialHealth(puppet.getHealth(), puppet.getMaxHealth());
		if (puppet.getTarget() != caster) puppet.setTarget(caster);
		double radius = CardinalRiteBoundaryLeashRules.ritualRadius(rite.getRiteSize());
		if (puppet.distanceToSqr(rite.getCenterPos().getX() + 0.5, puppet.getY(),
				rite.getCenterPos().getZ() + 0.5) > radius * radius) {
			puppet.teleportTo(rite.getCenterPos().getX() + 0.5, rite.getCenterPos().getY() + 1.0,
					rite.getCenterPos().getZ() + 0.5);
		}
	}

	private static void manifest(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		CardinalRiteStationMatcher.StationMatch station = CardinalRiteStationMatcher.findCaptured(level,
				rite.getCenterPos(), recipe, rite.getMatchedFloorId(), rite.getFloorForwards(), rite.getFloorUp())
				.orElse(null);
		ItemStack crossbar = level.getBlockEntity(rite.getCenterPos()) instanceof CardinalFocusBlockEntity focus
				? focus.getMediumForMatching() : ItemStack.EMPTY;
		String summonName = recipe.getPuppeteerTrial().summonName();
		if (station == null || !canBegin(caster, crossbar, summonName, recipe.getBloodCost(), true)) {
			rite.markCollapsed();
			return;
		}
		UUID crossbarId = MarionetteCrossbarItem.getCrossbarId(crossbar);
		var definition = PuppeteerSummonDefinitions.byName(summonName).orElse(null);
		var created = PuppeteerSummonFactory.createTrial(definition, level, caster, rite.getCenterPos());
		if (created.isEmpty() || !level.addFreshEntity(created.get())) {
			created.ifPresent(Entity::discard);
			caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.failed"), false);
			rite.markCollapsed();
			return;
		}
		List<PacketBloodStructureOfferingBurst.OfferingBurst> offeringBursts = new ArrayList<>();
		for (CardinalRiteStationMatcher.BrazierMatch offering : station.braziers()) {
			if (offering.consumeOnSuccess()
					&& level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier) {
				ItemStack consumed = brazier.consumeOffering();
				if (!consumed.isEmpty()) {
					offeringBursts.add(new PacketBloodStructureOfferingBurst.OfferingBurst(offering.pos(), consumed));
				}
			}
			level.sendParticles(ParticleTypes.CRIMSON_SPORE,
					offering.pos().getX() + 0.5, offering.pos().getY() + 1.0, offering.pos().getZ() + 0.5,
					12, 0.08, 0.2, 0.08, 0.02);
		}
		if (!offeringBursts.isEmpty()) {
			PacketDistributor.sendToPlayersNear(level, null,
					rite.getCenterPos().getX() + 0.5D, rite.getCenterPos().getY() + 0.5D,
					rite.getCenterPos().getZ() + 0.5D, 64.0D,
					new PacketBloodStructureOfferingBurst(offeringBursts, rite.getCenterPos(), 30));
		}
		HemoCapabilityAccess.getBloodVolume(caster).ifPresent(volume -> {
			volume.drain(recipe.getBloodCost());
			BloodVolumeEvents.syncVolume(caster, volume);
		});
		Mob puppet = created.get();
		rite.beginPuppeteerTrial(summonName, puppet.getUUID(), crossbarId);
		rite.addRiteThreat(puppet.getUUID());
		caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.started",
				puppet.getDisplayName()), false);
	}
}
