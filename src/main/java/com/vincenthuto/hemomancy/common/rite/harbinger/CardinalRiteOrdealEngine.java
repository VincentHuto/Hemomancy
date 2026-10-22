package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierMount;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.entity.mob.animal.BloodlickerEntity;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.entity.utility.UnsettledIchorEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteDaemonFinale;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.CardinalRiteImpactPacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteWaveRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tick controller for interactive ceremony phases and reshuffled ordeal waves.
 */
public final class CardinalRiteOrdealEngine {
	private static final int WAVE_TIMEOUT_TICKS = CardinalRiteWaveRules.DEADLINE_TICKS;
	/** Counterfeit anchors raised by a false-omens wave, relative to the rite centre. */
	static final BlockPos[] FALSE_OMEN_OFFSETS = {
			new BlockPos(2, 1, 2), new BlockPos(-2, 1, -2), new BlockPos(2, 1, -2)
	};
	/** Blood the covenant presses out of every anchor each second until the Seal is traced. */
	private static final int COVENANT_PRESSURE_ML = 4;

	private CardinalRiteOrdealEngine() {
	}

	public static void tick(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if ((rite.getPhase() == CardinalRitePhase.OFFERING_PROCESSION
				|| rite.getPhase() == CardinalRitePhase.CULMINATION)
				&& !level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(rite.getCenterPos()))) return;
		rite.tick();
			switch (rite.getPhase()) {
			case CONSECRATION -> tickConsecration(level, caster, rite, recipe);
			case INSCRIPTION -> {
				drawSupportSockets(level, rite, recipe);
				drawAllyStations(level, rite);
			}
			case STATION_PROJECTION -> rite.tryFinishStationProjection();
			case ORDEAL -> tickOrdeal(level, caster, rite, recipe);
			case STILL_INTERVAL -> tickStillInterval(caster, rite, recipe);
			case OFFERING_PROCESSION -> tickOfferingProcession(level, caster, rite);
			case CULMINATION -> tickCulmination(level, caster, rite, recipe);
			default -> {
			}
		}
	}

	/**
	 * The daemon hovers over the planted staff while the floor draws each burning
	 * offering into it in turn. Draws are staggered by procession time; each one
	 * is taken from its brazier when it starts (a tampered brazier collapses the
	 * rite) and swells the daemon when it arrives.
	 */
	private static void tickOfferingProcession(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite) {
		if (rite.isWorkingCraft()) {
			tickCraftProcession(level, caster, rite);
			return;
		}
		int total = rite.getOfferingItinerary().size();
		int arrived = CardinalRiteFinaleTiming.offeringsArrived(rite.getPhaseTicks(), total);
		float scale = CardinalRiteFinaleTiming.daemonScale(arrived);
		HumanitySpriteEntity daemon = findOrCreateDaemon(level, rite, scale);
		if (daemon == null) {
			rite.markCollapsed();
			return;
		}
		Vec3 hover = centerPosition(rite, scale);
		daemon.setPos(daemon.position().lerp(hover, 0.25D));
		daemon.setSpriteScale(scale);

		int due = CardinalRiteFinaleTiming.offeringsDue(rite.getPhaseTicks(), total);
		while (rite.getOfferingVisitIndex() < due) {
			int index = rite.getOfferingVisitIndex();
			ActiveCardinalRite.RiteOffering offering = rite.getCurrentOffering();
			if (offering == null) break;
			ItemStack drawn = offering.stack() == null ? ItemStack.EMPTY : offering.stack().copy();
			if (!StationUpgradeRites.isRite(rite.getRecipeId()) && !rite.isWorkingCraft()) {
				// Station upgrades and Working crafts escrowed their offerings at activation; others are taken now.
				if (!(level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier)) {
					rite.markCollapsed();
					return;
				}
				ItemStack present = brazier.getOfferingForMatching();
				if (present.isEmpty() || offering.stack() == null
						|| !ItemStack.isSameItemSameComponents(present, offering.stack())) {
					rite.markCollapsed();
					return;
				}
				drawn = brazier.consumeForRite(false);
				if (drawn.isEmpty()) {
					rite.markCollapsed();
					return;
				}
			}
			rite.markOfferingDrawn();
			// A station upgrade's escrowed offering leaves its bowl as the vessel draws it.
			RiteOfferingEscrow.releaseShown(level, offering.pos());
			Vec3 brazierTop = BrazierMount.bowl(level.getBlockState(offering.pos()), offering.pos(), 0.75D);
			CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.OFFERING_DRAWN, index, "",
					java.util.List.of(brazierTop, hover), drawn);
			CardinalRiteBeats.sound(level, offering.pos(), SoundInit.RITE_OFFERING_DRAWN,
					0.9F, 0.9F + level.random.nextFloat() * 0.2F);
		}

		if (rite.isReturningFromOfferings()
				&& CardinalRiteFinaleTiming.processionFinished(rite.getPhaseTicks(), total)
				&& rite.finishOfferingProcession()) {
			caster.displayClientMessage(Component.literal(
					"The daemon swells with the offerings.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		}
	}

	/**
	 * A Working craft's daemon does not wait over the staff: it swoops to each lit
	 * brazier in turn and drinks the escrowed offering there, swelling with each,
	 * then returns over the staff.
	 */
	private static void tickCraftProcession(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite) {
		List<ActiveCardinalRite.RiteOffering> itinerary = rite.getOfferingItinerary();
		int total = itinerary.size();
		int ticks = rite.getPhaseTicks();
		int absorbed = WorkingCraftDaemonPath.drawsDue(ticks, total);
		float scale = CardinalRiteFinaleTiming.daemonScale(absorbed);
		HumanitySpriteEntity daemon = findOrCreateDaemon(level, rite, scale);
		if (daemon == null) {
			rite.markCollapsed();
			return;
		}
		Vec3 hover = centerPosition(rite, scale);
		// Over a standing bowl, out from a sconce, below and beside a hanging one: never inside a wall or ceiling.
		Vec3 focus = Vec3.atCenterOf(rite.getCenterPos());
		List<Vec3> stops = itinerary.stream()
				.map(offering -> BrazierMount.daemonStop(level.getBlockState(offering.pos()), offering.pos(), focus))
				.toList();
		Vec3 at = WorkingCraftDaemonPath.processionPosition(ticks, hover, stops);
		Vec3 ahead = WorkingCraftDaemonPath.processionPosition(ticks + 1, hover, stops);
		daemon.setPos(at);
		if (ahead.distanceToSqr(at) > 1.0E-4D) daemon.faceDirection(ahead.x - at.x, ahead.z - at.z);
		daemon.setSpriteScale(scale);

		while (rite.getOfferingVisitIndex() < absorbed) {
			int index = rite.getOfferingVisitIndex();
			ActiveCardinalRite.RiteOffering offering = rite.getCurrentOffering();
			if (offering == null) break;
			// The offering was escrowed at activation and still shows in its bowl: the daemon breaks it there.
			ItemStack broken = offering.stack() == null ? ItemStack.EMPTY : offering.stack().copy();
			rite.markOfferingDrawn();
			RiteOfferingEscrow.releaseShown(level, offering.pos());
			Vec3 brazierTop = BrazierMount.bowl(level.getBlockState(offering.pos()), offering.pos(), 0.75D);
			CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.OFFERING_DISSOLVED, index, "",
					List.of(brazierTop, at), broken);
			CardinalRiteBeats.sound(level, offering.pos(), SoundInit.RITE_OFFERING_DRAWN,
					0.9F, 0.75F + level.random.nextFloat() * 0.15F);
		}

		if (rite.isReturningFromOfferings()
				&& WorkingCraftDaemonPath.processionFinished(ticks, total)
				&& rite.finishOfferingProcession()) {
			caster.displayClientMessage(Component.literal(
					"The daemon has drunk the offerings. It turns toward the structure.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		}
	}

	/**
	 * A Working craft's culmination: the daemon dives into the structure and pours
	 * itself in, and the structure darkens and throbs with it until the floor takes it.
	 */
	private static void tickCraftCulmination(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite) {
		float scale = CardinalRiteFinaleTiming.daemonScale(rite.getOfferingItinerary().size());
		HumanitySpriteEntity daemon = findOrCreateDaemon(level, rite, scale);
		if (daemon == null) {
			rite.markCollapsed();
			return;
		}
		int ticks = rite.getPhaseTicks();
		Vec3 hover = centerPosition(rite, scale);
		Vec3 heart = WorkingCraftRites.subjectHeart(rite);
		Vec3 at = WorkingCraftDaemonPath.divePosition(ticks, hover, heart);
		daemon.setPos(at);
		daemon.faceDirection(heart.x - hover.x, heart.z - hover.z);
		daemon.setSpriteScale(WorkingCraftDaemonPath.diveScale(ticks, scale, HumanitySpriteEntity.MIN_SCALE));
		BlockPos heartPos = BlockPos.containing(heart);
		if (ticks == WorkingCraftDaemonPath.DIVE_TICKS) {
			CardinalRiteBeats.sound(level, heartPos, SoundInit.RITE_SUBJECT_RISE, 1.0F, 0.5F);
			caster.displayClientMessage(Component.literal("The daemon pours itself into the structure.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		} else if (ticks > WorkingCraftDaemonPath.DIVE_TICKS && (ticks - WorkingCraftDaemonPath.DIVE_TICKS) % 16 == 0) {
			// The possessed structure beats like a heart until the floor takes it.
			CardinalRiteBeats.sound(level, heartPos, SoundInit.RITE_AMBIENT_HEARTBEAT, 0.9F, 0.7F);
		}
		if (ticks >= CardinalRiteFinaleTiming.TOTAL_TICKS) {
			rite.markComplete();
		}
	}

	private static void tickCulmination(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (rite.isWorkingCraft()) {
			tickCraftCulmination(level, caster, rite);
			return;
		}
		CardinalRiteDaemonFinale finale = recipe == null ? CardinalRiteDaemonFinale.MERGE : recipe.getDaemonFinale();
		if (finale != CardinalRiteDaemonFinale.MERGE) {
			tickWorldCulmination(level, caster, rite, recipe, finale);
			return;
		}
		// It keeps the size its offerings left it: it gathers over the staff while they fade, then merges.
		int total = rite.getOfferingItinerary().size();
		float scale = CardinalRiteFinaleTiming.daemonScale(total);
		HumanitySpriteEntity daemon = findOrCreateDaemon(level, rite, scale);
		if (daemon == null) {
			rite.markCollapsed();
			return;
		}
		double merge = smoothstep(CardinalRiteFinaleTiming.mergeProgress(rite.getPhaseTicks()));
		Vec3 center = centerPosition(rite, scale);
		Vec3 player = new Vec3(caster.getX(),
				caster.getY() + caster.getBbHeight() * 0.55D, caster.getZ());
		daemon.setPos(center.lerp(player, merge));
		daemon.setSpriteScale((float) lerp(merge, scale, HumanitySpriteEntity.MIN_SCALE));
		daemon.faceDirection(player.x - center.x, player.z - center.z);
		if (CardinalRiteFinaleTiming.isImpactTick(rite.getPhaseTicks())) {
			CardinalRiteImpactPacket impact = new CardinalRiteImpactPacket(8, 0.52F, level.random.nextInt());
			PacketHandler.sendToPlayer(caster, impact);
			for (UUID ally : rite.getAllyRoles().keySet()) {
				if (level.getPlayerByUUID(ally) instanceof ServerPlayer witness
						&& witness.distanceToSqr(caster) < 48.0D * 48.0D) {
					PacketHandler.sendToPlayer(witness, new CardinalRiteImpactPacket(6, 0.32F, level.random.nextInt()));
				}
			}
		}
		if (rite.getPhaseTicks() >= CardinalRiteFinaleTiming.TOTAL_TICKS) {
			rite.markComplete();
		}
	}

	/**
	 * A world rite sends its daemon out into the world. After gathering over the
	 * staff it either shoots up and strikes the sky, which flashes red, or climbs,
	 * rears and slams into the floor, cracking the ground open in blazing red and
	 * black. The strike lands on the impact tick, as the rite takes effect.
	 */
	private static void tickWorldCulmination(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe, CardinalRiteDaemonFinale finale) {
		float scale = CardinalRiteFinaleTiming.daemonScale(rite.getOfferingItinerary().size());
		HumanitySpriteEntity daemon = findOrCreateDaemon(level, rite, scale);
		if (daemon == null) {
			rite.markCollapsed();
			return;
		}
		int ticks = rite.getPhaseTicks();
		Vec3 hover = centerPosition(rite, scale);
		Vec3 ground = Vec3.atBottomCenterOf(rite.getCenterPos().above());
		boolean sky = finale == CardinalRiteDaemonFinale.SKY;
		Vec3 at = sky ? WorldRiteFinalePath.skyPosition(ticks, hover, level.getMaxBuildHeight() - 2.0D)
				: WorldRiteFinalePath.groundPosition(ticks, hover, ground);
		daemon.setPos(at);
		daemon.setSpriteScale(scale);
		if (ticks == CardinalRiteFinaleTiming.GROWTH_TICKS + 1) {
			// The daemon tears loose from the staff.
			CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_ESCALATE, 1.2F, sky ? 1.2F : 0.7F);
		}
		if (CardinalRiteFinaleTiming.isImpactTick(ticks)) {
			int seed = level.random.nextInt();
			if (sky) {
				CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.SKY_STRUCK, seed, "", List.of(at), ItemStack.EMPTY);
				CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_CULMINATION, 3.0F, 0.75F);
				shakeNearby(level, rite.getCenterPos(), 64.0D, 10, 0.55F, seed);
			} else {
				float reach = Math.max(4.0F, HarbingerCardinalRiteEvents.ritualFootprintRadius(rite, recipe) + 1.5F);
				CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.GROUND_SLAMMED, seed, Float.toString(reach),
						List.of(ground), ItemStack.EMPTY);
				CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_SUBJECT_THUD, 2.0F, 0.6F);
				CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_CULMINATION, 2.0F, 0.6F);
				shakeNearby(level, rite.getCenterPos(), 32.0D, 14, 0.3F, seed);
			}
			daemon.setSpriteScale(HumanitySpriteEntity.MIN_SCALE);
		}
		if (ticks >= CardinalRiteFinaleTiming.TOTAL_TICKS) {
			rite.markComplete();
		}
	}

	/** Every player near the rite feels the strike: a shudder and, for the sky, a red flash. */
	private static void shakeNearby(ServerLevel level, BlockPos center, double range, int duration, float flash, int seed) {
		for (ServerPlayer witness : level.players()) {
			if (witness.distanceToSqr(Vec3.atCenterOf(center)) < range * range) {
				PacketHandler.sendToPlayer(witness, new CardinalRiteImpactPacket(duration, flash, seed));
			}
		}
	}

	private static HumanitySpriteEntity findOrCreateDaemon(ServerLevel level,
			ActiveCardinalRite rite, float scale) {
		HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(
				level, rite.getPlayerUUID(), rite.getCenterPos());
		if (daemon != null) return daemon;
		if (!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(rite.getCenterPos()))) return null;
		daemon = EntityInit.humanity_sprite.get().create(level);
		if (daemon == null) return null;
		daemon.initialize(centerPosition(rite, scale), scale);
		daemon.bindToRite(rite.getPlayerUUID());
		return level.addFreshEntity(daemon) ? daemon : null;
	}

	private static Vec3 centerPosition(ActiveCardinalRite rite, float scale) {
		return Vec3.atCenterOf(rite.getCenterPos()).add(0.0D,
				0.5D + CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT * scale * 0.5D, 0.0D);
	}

	private static double smoothstep(double value) {
		return value * value * (3.0D - 2.0D * value);
	}

	private static double lerp(double amount, double start, double end) {
		return start + (end - start) * amount;
	}

	private static void tickConsecration(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (!StationUpgradeRites.isRite(rite.getRecipeId())) rite.incrementIdleTicks();
		if (rite.getIdleTicks() >= CardinalRiteCeremonyRules.CONSECRATION_TIMEOUT_TICKS) {
			rite.markCollapsed();
			caster.displayClientMessage(Component.literal("The untouched consecration gutters out.")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
	}

	private static void tickOrdeal(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (rite.getPhaseTicks() == 1) beginWave(level, caster, rite);
		if (rite.getPhaseTicks() == 1 + CardinalRiteThreatRules.WAVE_TELEGRAPH_TICKS) spawnWave(level, caster, rite);
		tickAllies(level, rite);
		pruneThreats(level, rite);
		ResourceLocation sigilId = CardinalRiteInteractionHandler.sigilForWave(rite);
		boolean traced = false;
		if ("false_omens".equals(currentWave(rite))) drawFalseOmens(level, rite);
		if (sigilId != null) {
			String wave = currentWave(rite);
			CardinalRiteInteractionHandler.SigilPlacement placement =
					CardinalRiteInteractionHandler.resolveWaveSigilPlacement(
							sigilId, wave.startsWith("discover_"), rite.getCurrentWave(),
							CardinalRiteInteractionHandler.supportSigils(recipe), recipe);
			String progressKey = placement.progressKey();
			drawSigil(level, rite, sigilId,
					new BlockPos(placement.x(), placement.y(), placement.z()), progressKey);
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(sigilId);
			traced = sigil != null && rite.getSigilProgress().getOrDefault(progressKey, 0) >= sigil.nodes().size();
			if ("covenant_pressure".equals(wave) && !traced && rite.getPhaseTicks() % 20 == 0) {
				applyCovenantPressure(rite);
			}
			if (traced && rite.areAnchorsConsecrated()) {
				clearWave(level, rite);
				return;
			}
		} else {
			applyThreatPressure(level, caster, rite, recipe);
			if (rite.getPhaseTicks() > CardinalRiteThreatRules.WAVE_TELEGRAPH_TICKS + 40
					&& rite.getRiteThreats().isEmpty()
					&& rite.areAnchorsConsecrated()) {
				clearWave(level, rite);
				return;
			}
		}
		if (rite.getPhaseTicks() >= WAVE_TIMEOUT_TICKS
				&& CardinalRiteWaveRules.missedRequiredObjective(currentWave(rite), traced, !rite.getRiteThreats().isEmpty())) {
			clearThreats(level, rite);
			rite.markCollapsed();
			caster.displayClientMessage(Component.literal(sigilId != null
					? "The demanded sigil went unanswered. The rite fails; prepare it again."
					: "The puppeteers outlasted the calling. The rite fails; prepare it again.")
					.withStyle(ChatFormatting.DARK_RED), false);
			return;
		}
		if (rite.getPhaseTicks() == WAVE_TIMEOUT_TICKS) {
			if (!attendantCatchesMiss(level, rite)) {
				rite.addInstability(12 + CardinalRiteCeremonyRules.formIndex(recipe.getRiteType()) * 3);
				CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_ANCHOR_BREAK, 1.0F, 0.8F);
				caster.displayClientMessage(Component.literal("The ordeal outlasts you and the boundary strains.")
						.withStyle(ChatFormatting.DARK_RED), true);
			}
			clearThreats(level, rite);
			if (rite.areAnchorsConsecrated()) clearWave(level, rite);
			else caster.displayClientMessage(Component.literal(
					"The ordeal cannot advance while the cardinal stations run dry.")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
		if (rite.getPhaseTicks() % 20 == 0) {
			rite.applyAnchorDeficitPressure();
			applyStrain(level, caster, rite);
		}
	}

	private static void clearWave(ServerLevel level, ActiveCardinalRite rite) {
		rite.completeWave();
		CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.WAVE_CLEARED);
		CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_WAVE_CLEAR, 1.0F, 1.0F);
	}

	/**
	 * Instability bites before it breaks the rite. Once strained, the outermost
	 * protected anchor bleeds a little each second; once rupturing, rite-bound
	 * threats also feed on the instability and grow stronger and faster.
	 */
	private static void applyStrain(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite) {
		CardinalRiteInstability band = rite.getInstabilityBand();
		if (band == CardinalRiteInstability.STABLE) return;
		rite.drainOutermostProtectedAnchor(band == CardinalRiteInstability.RUPTURING ? 2 : 1);
		if (band != CardinalRiteInstability.RUPTURING) return;
		for (UUID id : rite.getRiteThreats()) {
			if (level.getEntity(id) instanceof Mob mob) {
				mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, false, true, true));
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, false, true, true));
			}
		}
		if (rite.getPhaseTicks() % 100 == 0) {
			caster.displayClientMessage(Component.literal("The boundary ruptures. Repair the broken anchors.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
		}
	}

	/** The covenant presses blood out of every anchor until the demanded Seal is traced. */
	private static void applyCovenantPressure(ActiveCardinalRite rite) {
		for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.drainAnchor(i, COVENANT_PRESSURE_ML);
	}

	private static void tickStillInterval(ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (rite.getPhaseTicks() == 1 && !rite.isHeldBeforeFinale()) {
			caster.displayClientMessage(Component.literal("A still interval: repair, reinforce, and breathe.")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
		}
		drawSupportSockets(caster.serverLevel(), rite, recipe);
		if (rite.isHeldBeforeFinale()) {
			if (rite.tickFinaleHold()) {
				if (!rite.isHeldBeforeFinale() && rite.areAnchorsConsecrated()) rite.finishStillInterval();
				return;
			}
		}
		int duration = recipe.getCeremony().stillIntervalTicks();
		if (rite.getPhaseTicks() >= duration) {
			if (rite.areAnchorsConsecrated()) {
				rite.finishStillInterval();
			} else {
				if (rite.getPhaseTicks() == Math.max(1, duration)) {
					caster.displayClientMessage(Component.literal(
							"The rite waits: restore every cardinal station.")
							.withStyle(ChatFormatting.DARK_RED), false);
				}
				if (rite.getPhaseTicks() % 20 == 0) {
					rite.applyStillIntervalPressure(duration);
				}
			}
		}
	}

	private static void beginWave(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite) {
		String wave = currentWave(rite);
		String cue = switch (wave) {
			case "bloodlicker_siphon" -> "Bloodlickers crawl for the anchors.";
			case "fargone_dive" -> "A Fargone stoops toward your living blood.";
			case "rogue_will" -> "A rogue will claws at the boundary's intent.";
			case "false_omens" -> "Counterfeit anchors bloom. Find the puppeteer.";
			case "covenant_pressure" -> "The covenant presses on every anchor. Trace the Seal to bear it.";
			default -> wave.startsWith("discover_")
					? "An unknown Ichorian shape wakes. Read it before it reads you."
					: "A response sigil is demanded.";
		};
		caster.displayClientMessage(Component.literal(cue).withStyle(ChatFormatting.DARK_RED), false);
		caster.displayClientMessage(Component.literal(CardinalRiteWaveRules.deadlineHint(wave)
				+ ". Restore every anchor before advancing.").withStyle(ChatFormatting.GRAY), false);
		List<Vec3> spawns = spawnsThreats(wave) ? threatSpawnPoints(rite, wave) : List.of();
		CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.WAVE_START, rite.getCurrentWave(), wave,
				spawns, ItemStack.EMPTY);
		CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_WAVE_START, 1.2F, 1.0F);
	}

	private static boolean spawnsThreats(String wave) {
		return !wave.startsWith("discover_") && !"response_sigil".equals(wave) && !"covenant_pressure".equals(wave);
	}

	/** Threats step out of the fog where the wave's warning struck. */
	private static void spawnWave(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite) {
		String wave = currentWave(rite);
		if (spawnsThreats(wave)) spawnThreats(level, caster, rite, wave);
	}

	private static List<Vec3> threatSpawnPoints(ActiveCardinalRite rite, String wave) {
		int count = Math.max(1, 1 + rite.getDegree() / 3);
		List<Vec3> points = new ArrayList<>(count);
		double radius = rite.getRiteSize() / 2.0D + 4.0D;
		for (int i = 0; i < count; i++) {
			double angle = Math.PI * 2.0D * i / count;
			points.add(new Vec3(rite.getCenterPos().getX() + 0.5D + Math.cos(angle) * radius,
					rite.getCenterPos().getY() + ("fargone_dive".equals(wave) ? 5.0D : 1.0D),
					rite.getCenterPos().getZ() + 0.5D + Math.sin(angle) * radius));
		}
		return points;
	}

	private static void spawnThreats(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			String wave) {
		for (Vec3 point : threatSpawnPoints(rite, wave)) {
			Entity entity = switch (wave) {
				case "fargone_dive" -> EntityInit.fargone.get().create(level);
				case "rogue_will" -> EntityInit.will.get().create(level);
				case "false_omens" -> EntityInit.blood_drunk_puppeteer.get().create(level);
				default -> EntityInit.bloodlicker.get().create(level);
			};
			if (entity == null) continue;
			float yaw = (float) Math.toDegrees(Math.atan2(point.z - rite.getCenterPos().getZ() - 0.5D,
					point.x - rite.getCenterPos().getX() - 0.5D));
			entity.moveTo(point.x, point.y, point.z, yaw, 0.0F);
			entity.getPersistentData().putBoolean(CardinalRiteThreatRules.RITE_BOUND_TAG, true);
			if (entity instanceof Monster monster) monster.setTarget(caster);
			if (entity instanceof Mob mob) mob.getNavigation().moveTo(caster, 1.15D);
			if (level.addFreshEntity(entity)) rite.addRiteThreat(entity.getUUID());
		}
	}

	private static void applyThreatPressure(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (rite.getPhaseTicks() % 20 != 0) return;
		String wave = currentWave(rite);
		int active = rite.getRiteThreats().size();
		if (active == 0) return;
		switch (wave) {
			case "bloodlicker_siphon" -> {
				for (UUID threatId : rite.getRiteThreats()) {
					Entity threat = level.getEntity(threatId);
					if (!(threat instanceof Mob mob)) continue;
					int anchor = nearestAnchor(recipe, rite, threat.blockPosition());
					BlockPos anchorOffset = recipe.getCeremony().anchors().get(anchor).offset();
					BlockPos anchorPos = CardinalRiteAnchorVisualRules.riteSurface(rite.getCenterPos())
							.offset(anchorOffset.getX(), 0, anchorOffset.getZ());
					mob.getNavigation().moveTo(anchorPos.getX() + 0.5D, anchorPos.getY(), anchorPos.getZ() + 0.5D, 1.2D);
					if (!CardinalRiteThreatRules.canSiphonAnchor(threat.distanceToSqr(
							anchorPos.getX() + 0.5D, anchorPos.getY(), anchorPos.getZ() + 0.5D))) continue;
					int stolen = rite.drainAnchor(anchor, 5);
					if (threat instanceof BloodlickerEntity bloodlicker) {
						bloodlicker.addSiphonedBlood(stolen);
					}
					int restored = rite.drawReservoirBlood(stolen);
					if (restored > 0) rite.fillAnchor(anchor, restored);
					int escaped = stolen - restored;
					if (escaped > 0) {
						rite.addInstability(Math.max(1, escaped / 5));
						spawnUnsettledIchor(level, rite, recipe, anchor, escaped);
					}
				}
			}
			case "fargone_dive" -> {
				var volume = HemoCapabilityAccess.getBloodVolume(caster);
				int touching = (int) rite.getRiteThreats().stream()
						.map(level::getEntity).filter(java.util.Objects::nonNull)
						.filter(entity -> CardinalRiteThreatRules.canDrainCaster(entity.distanceToSqr(caster)))
						.count();
				int drain = Math.min(touching * 5, (int) volume.getBloodVolume());
				if (drain > 0) {
					volume.drain(drain);
					BloodVolumeEvents.syncVolume(caster, volume);
				}
			}
			case "rogue_will" -> rite.addInstability(active);
			case "false_omens" -> {
				if (rite.getPhaseTicks() % 60 == 0) rite.addInstability(active * 2);
			}
			default -> {
			}
		}
		if (isComplete(rite, "bastion")) {
			for (UUID id : rite.getRiteThreats()) {
				Entity threat = level.getEntity(id);
				if (threat != null) threat.hurt(level.damageSources().magic(),
						CardinalRiteThreatRules.BASTION_DAMAGE_PER_PULSE);
			}
		}
		if (CardinalRiteSupportSigilRules.bindsThreats(isComplete(rite, "cage"))) {
			for (UUID id : rite.getRiteThreats()) {
				Entity threat = level.getEntity(id);
				if (threat instanceof Mob mob) {
					mob.addEffect(new MobEffectInstance(
							MobEffects.MOVEMENT_SLOWDOWN,
							CardinalRiteSupportSigilRules.CAGE_EFFECT_TICKS,
							CardinalRiteSupportSigilRules.CAGE_SLOWNESS_AMPLIFIER,
							false, true, true));
				}
			}
		}
		if (isComplete(rite, "hematic_lattice")) rite.balanceAnchors();
		applyAnchorDecay(rite, recipe);
	}

	private static void spawnUnsettledIchor(ServerLevel level, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe, int anchor, int bloodMl) {
		UnsettledIchorEntity ichor = EntityInit.unsettled_ichor.get().create(level);
		if (ichor == null) return;
		BlockPos offset = anchor < recipe.getCeremony().anchors().size()
				? recipe.getCeremony().anchors().get(anchor).offset() : BlockPos.ZERO;
		BlockPos position = CardinalRiteAnchorVisualRules.riteSurface(rite.getCenterPos())
				.offset(offset.getX(), 0, offset.getZ());
		ichor.initialize(rite.getPlayerUUID(), bloodMl);
		ichor.moveTo(position.getX() + 0.5D, position.getY() + 0.4D, position.getZ() + 0.5D);
		ichor.setDeltaMovement((level.random.nextDouble() - 0.5D) * 0.12D, 0.16D,
				(level.random.nextDouble() - 0.5D) * 0.12D);
		level.addFreshEntity(ichor);
	}

	private static void applyAnchorDecay(ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		double decay = switch (recipe.getCeremony().atmosphere().fog()) {
			case "dense" -> 0.25D;
			case "storm" -> 0.5D;
			default -> 0.0D;
		};
		if (decay <= 0.0D) return;
		int period = Math.max(1, (int) Math.round(20.0D / decay));
		if (rite.getPhaseTicks() % period == 0) {
			rite.drainOutermostProtectedAnchor(1);
		}
	}

	private static boolean attendantCatchesMiss(ServerLevel level, ActiveCardinalRite rite) {
		return CardinalRiteAllyService.tryCorrectMiss(level, rite);
	}

	private static void tickAllies(ServerLevel level, ActiveCardinalRite rite) {
		if (rite.getPhaseTicks() % 20 == 0) {
			int anchorOrdinal = 0;
			for (var entry : rite.getAllyRoles().entrySet()) {
				if (entry.getValue() != CardinalRiteAllyRole.ANCHOR
						|| !CardinalRiteAllyService.isAvailable(level, rite, entry.getKey())) continue;
				int[] anchors = rite.getAnchorBloodMl();
				int ringCount = Math.max(1, (anchors.length + 3) / 4);
				int ring = Math.floorMod(anchorOrdinal++, ringCount);
				for (int i = ring * 4; i < Math.min(anchors.length, ring * 4 + 4); i++) {
					if (rite.bloodNeededForAnchor(i) <= 0) continue;
					int drawn = CardinalRiteAllyService.spend(level, rite, entry.getKey(), Math.min(10, rite.bloodNeededForAnchor(i)));
					if (drawn > 0) rite.fillAnchor(i, drawn);
					break;
				}
			}
		}
		if (rite.getPhaseTicks() % 100 == 0) {
			Mob target = rite.getRiteThreats().stream().map(level::getEntity)
					.filter(Mob.class::isInstance).map(Mob.class::cast)
					.filter(Mob::isAlive).findFirst().orElse(null);
			if (target == null) return;
			for (var entry : rite.getAllyRoles().entrySet()) {
				if (entry.getValue() != CardinalRiteAllyRole.WARDEN
						|| !CardinalRiteAllyService.isAvailable(level, rite, entry.getKey())
						|| !CardinalRiteAllyService.trySpend(level, rite, entry.getKey(), 25)) continue;
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
			}
		}
	}

	private static void drawAllyStations(ServerLevel level, ActiveCardinalRite rite) {
		if (rite.getDegree() < 5 || level.getGameTime() % 4 != 0) return;
		for (var marker : CardinalRiteAllyService.markers(CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId())).entrySet()) {
			BlockPos pos = rite.getCenterPos().offset(marker.getValue());
			ParticleColor color = switch (marker.getKey()) {
				case ANCHOR -> new ParticleColor(220, 40, 40);
				case ATTENDANT -> new ParticleColor(210, 160, 255);
				case WARDEN -> new ParticleColor(70, 150, 255);
			};
			drawInteractionMarker(level, color,
					pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D,
					3, 0.15D, 0.03D, 0.15D);
		}
	}

	private static void pruneThreats(ServerLevel level, ActiveCardinalRite rite) {
		for (UUID id : new ArrayList<>(rite.getRiteThreats())) {
			Entity entity = level.getEntity(id);
			if (entity == null || !entity.isAlive()) rite.removeRiteThreat(id);
		}
	}

	public static void clearThreats(ServerLevel level, ActiveCardinalRite rite) {
		for (UUID id : rite.getRiteThreats()) {
			Entity entity = level.getEntity(id);
			// A trial puppet leaves on its strings like any other; other threats vanish as before.
			if (entity instanceof net.minecraft.world.entity.Mob mob
					&& mob instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon)
				com.vincenthuto.hemomancy.common.summon.PuppetStrings.unspool(mob);
			else if (entity != null) entity.discard();
		}
		rite.clearRiteThreats();
	}

	private static void drawSupportSockets(ServerLevel level, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (level.getGameTime() % 4 != 0) return;
		for (CardinalRiteInteractionHandler.SigilPlacement placement
				: CardinalRiteInteractionHandler.supportSigils(recipe)) {
			drawSigil(level, rite, placement.id(),
					new BlockPos(placement.x(), placement.y(), placement.z()),
					placement.progressKey());
		}
	}

	private static int nearestAnchor(CardinalRiteRecipe recipe, ActiveCardinalRite rite, BlockPos from) {
		int nearest = 0;
		double nearestDistance = Double.MAX_VALUE;
		for (int i = 0; i < recipe.getCeremony().anchors().size(); i++) {
			BlockPos offset = recipe.getCeremony().anchors().get(i).offset();
			BlockPos pos = CardinalRiteAnchorVisualRules.riteSurface(rite.getCenterPos())
					.offset(offset.getX(), 0, offset.getZ());
			double distance = pos.distSqr(from);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = i;
			}
		}
		return nearest;
	}

	private static boolean isComplete(ActiveCardinalRite rite, String path) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath("hemomancy", path);
		IchorianSigilDefinition definition = IchorianSigilRegistry.get(id);
		return definition != null && rite.isSigilComplete(id.toString(), definition.nodes().size());
	}

	private static void drawSigil(ServerLevel level, ActiveCardinalRite rite, ResourceLocation id,
			BlockPos offset, String progressKey) {
		if (level.getGameTime() % 3 != 0) return;
		IchorianSigilDefinition sigil = IchorianSigilRegistry.get(id);
		if (sigil == null) return;
		int completed = rite.getSigilProgress().getOrDefault(progressKey, 0);
		if (completed >= sigil.nodes().size()) return;
		for (int i = completed; i < sigil.nodes().size(); i++) {
			var node = sigil.nodes().get(i);
			BlockPos base = rite.getCenterPos().offset(0, offset.getY(), 0);
			BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level, base,
					offset.getX() + (int) Math.round(node.x()),
					offset.getZ() + (int) Math.round(node.z()));
			Vec3 aimPoint = CardinalRiteTargetGeometry.sigilAimPoint(
					rite.getCenterPos(), surface, offset.getX(), offset.getZ(), node.x(), node.z());
			drawInteractionMarker(level, color(sigil.color()),
					aimPoint.x, aimPoint.y, aimPoint.z,
					i == completed ? 2 : 1, 0.03D, 0.02D, 0.03D);
		}
	}

	private static void drawFalseOmens(ServerLevel level, ActiveCardinalRite rite) {
		if (level.getGameTime() % 3 != 0) return;
		boolean revealed = CardinalRiteSupportSigilRules.revealsFalseOmens(
				isComplete(rite, "mnemonic"), isComplete(rite, "lens"));
		ParticleColor fakeColor = revealed ? new ParticleColor(90, 210, 255)
				: new ParticleColor(185, 40, 220);
		for (BlockPos offset : FALSE_OMEN_OFFSETS) {
			BlockPos fake = rite.getCenterPos().offset(offset);
			drawInteractionMarker(level, fakeColor,
					fake.getX() + 0.5D, fake.getY() + 0.12D, fake.getZ() + 0.5D,
					revealed ? 1 : 3, 0.18D, 0.02D, 0.18D);
		}
	}

	private static void drawInteractionMarker(ServerLevel level, ParticleColor color,
			double x, double y, double z, int bloodCellCount,
			double spreadX, double spreadY, double spreadZ) {
		for (CardinalRiteInteractionMarker.Layer layer : CardinalRiteInteractionMarker.layers()) {
			switch (layer) {
				case BLOOD_CELL -> level.sendParticles(HemoParticleData.bloodCell(color),
						x, y, z, bloodCellCount, spreadX, spreadY, spreadZ, 0.0D);
				case GLOW -> level.sendParticles(HemoParticleData.glow(color),
						x, y + 0.04D, z, 1, 0.02D, 0.01D, 0.02D, 0.0D);
			}
		}
	}

	private static ParticleColor color(int rgb) {
		return new ParticleColor((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255);
	}

	private static String currentWave(ActiveCardinalRite rite) {
		return rite.getCurrentWave() < rite.getWaveDeck().size()
				? rite.getWaveDeck().get(rite.getCurrentWave())
				: "bloodlicker_siphon";
	}
}
