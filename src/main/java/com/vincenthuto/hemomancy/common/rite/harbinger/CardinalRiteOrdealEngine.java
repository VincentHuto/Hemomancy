package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.entity.mob.animal.BloodlickerEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.entity.utility.UnsettledIchorEntity;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.CardinalRiteImpactPacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Tick controller for interactive ceremony phases and reshuffled ordeal waves.
 */
public final class CardinalRiteOrdealEngine {
	private static final String RITE_CASTER = "HemomancyRiteCaster";
	private static final int WAVE_TIMEOUT_TICKS = 360;

	private CardinalRiteOrdealEngine() {
	}

	public static void tick(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		rite.tick();
		switch (rite.getPhase()) {
			case CONSECRATION -> tickConsecration(level, caster, rite, recipe);
			case INSCRIPTION -> {
				drawSupportSockets(level, rite, recipe);
				drawAllyStations(level, rite);
			}
			case ORDEAL -> tickOrdeal(level, caster, rite, recipe);
			case STILL_INTERVAL -> tickStillInterval(caster, rite, recipe);
			case OFFERING_PROCESSION -> tickOfferingProcession(level, caster, rite);
			case CULMINATION -> tickCulmination(level, caster, rite);
			default -> {
			}
		}
	}

	private static void tickOfferingProcession(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite) {
		HumanitySpriteEntity daemon = findOrCreateDaemon(level, rite,
				CardinalRiteFinaleTiming.PROCESSION_SCALE);
		if (daemon == null) {
			rite.markCollapsed();
			return;
		}
		daemon.setSpriteScale(CardinalRiteFinaleTiming.PROCESSION_SCALE);
		if (rite.tickOfferingDwell()) return;

		ActiveCardinalRite.RiteOffering offering = rite.getCurrentOffering();
		Vec3 target = rite.isReturningFromOfferings() || offering == null
				? centerPosition(rite, CardinalRiteFinaleTiming.PROCESSION_SCALE)
				: Vec3.atCenterOf(offering.pos()).add(0.0D, 1.15D, 0.0D);
		if (!moveDaemonToward(daemon, target)) return;

		if (rite.isReturningFromOfferings() || offering == null) {
			rite.finishOfferingProcession();
			caster.displayClientMessage(Component.literal(
					"The daemon returns to the staff and swells with the offerings.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return;
		}
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
		ItemStack consumed = brazier.consumeOffering();
		if (consumed.isEmpty()) {
			rite.markCollapsed();
			return;
		}
		emitOfferingAbsorption(level, daemon, offering.pos(), consumed);
		rite.absorbCurrentOffering();
		level.playSound(null, offering.pos(), SoundEvents.FIRECHARGE_USE,
				SoundSource.BLOCKS, 0.9F, 0.65F + level.random.nextFloat() * 0.2F);
	}

	private static void tickCulmination(ServerLevel level, ServerPlayer caster,
			ActiveCardinalRite rite) {
		double growth = CardinalRiteFinaleTiming.growthProgress(rite.getPhaseTicks());
		float matureScale = matureDaemonScale(rite);
		float scale = (float) lerp(growth, CardinalRiteFinaleTiming.PROCESSION_SCALE, matureScale);
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
		daemon.setFlying(merge > 0.0D);
		if (CardinalRiteFinaleTiming.isImpactTick(rite.getPhaseTicks())) {
			PacketHandler.sendToPlayer(caster, new CardinalRiteImpactPacket(
					8, 0.52F, level.random.nextInt()));
		}
		if (rite.getPhaseTicks() >= CardinalRiteFinaleTiming.TOTAL_TICKS) {
			rite.markComplete();
		}
	}

	private static HumanitySpriteEntity findOrCreateDaemon(ServerLevel level,
			ActiveCardinalRite rite, float scale) {
		HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(
				level, rite.getPlayerUUID(), rite.getCenterPos());
		if (daemon != null) return daemon;
		daemon = EntityInit.humanity_sprite.get().create(level);
		if (daemon == null) return null;
		daemon.initialize(centerPosition(rite, scale), scale);
		daemon.bindToRite(rite.getPlayerUUID());
		return level.addFreshEntity(daemon) ? daemon : null;
	}

	private static boolean moveDaemonToward(HumanitySpriteEntity daemon, Vec3 target) {
		Vec3 delta = target.subtract(daemon.position());
		double distance = delta.length();
		if (distance <= CardinalRiteFinaleTiming.DAEMON_TRAVEL_BLOCKS_PER_TICK) {
			daemon.setPos(target);
			daemon.setFlying(false);
			return true;
		}
		Vec3 step = delta.scale(CardinalRiteFinaleTiming.DAEMON_TRAVEL_BLOCKS_PER_TICK / distance);
		daemon.setPos(daemon.position().add(step));
		daemon.faceDirection(delta.x, delta.z);
		daemon.setFlying(true);
		return false;
	}

	private static Vec3 centerPosition(ActiveCardinalRite rite, float scale) {
		return Vec3.atCenterOf(rite.getCenterPos()).add(0.0D,
				0.5D + CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT * scale * 0.5D, 0.0D);
	}

	private static float matureDaemonScale(ActiveCardinalRite rite) {
		double matureHeight = 3.0D + rite.getRiteSize() * 0.5D;
		return (float) (matureHeight / CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT);
	}

	private static void emitOfferingAbsorption(ServerLevel level, HumanitySpriteEntity daemon,
			BlockPos brazierPos, ItemStack stack) {
		double x = brazierPos.getX() + 0.5D;
		double y = brazierPos.getY() + 1.2D;
		double z = brazierPos.getZ() + 0.5D;
		Vec3 pull = daemon.position().subtract(x, y, z).normalize().scale(0.18D);
		level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack),
				x, y, z, 32, 0.22D, 0.18D, 0.22D, 0.12D);
		level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack),
				x, y, z, 12, Math.abs(pull.x), Math.abs(pull.y), Math.abs(pull.z), 0.18D);
	}

	private static double smoothstep(double value) {
		return value * value * (3.0D - 2.0D * value);
	}

	private static double lerp(double amount, double start, double end) {
		return start + (end - start) * amount;
	}

	private static void tickConsecration(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		rite.incrementIdleTicks();
		drawAnchors(level, rite, recipe);
		if (rite.getIdleTicks() >= CardinalRiteCeremonyRules.CONSECRATION_TIMEOUT_TICKS) {
			rite.markCollapsed();
			caster.displayClientMessage(Component.literal("The untouched consecration gutters out.")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
	}

	private static void tickOrdeal(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (rite.getPhaseTicks() == 1) beginWave(level, caster, rite);
		drawAnchors(level, rite, recipe);
		tickAllies(level, rite);
		pruneThreats(level, rite);
		ResourceLocation sigilId = CardinalRiteInteractionHandler.sigilForWave(rite);
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
			if (sigil != null
					&& rite.getSigilProgress().getOrDefault(progressKey, 0) >= sigil.nodes().size()
					&& rite.areAnchorsConsecrated()) {
				rite.completeWave();
				return;
			}
		} else {
			applyThreatPressure(level, caster, rite, recipe);
			if (rite.getPhaseTicks() > 40 && rite.getRiteThreats().isEmpty()
					&& rite.areAnchorsConsecrated()) {
				rite.completeWave();
				return;
			}
		}
		if (rite.getPhaseTicks() == WAVE_TIMEOUT_TICKS) {
			if (!attendantCatchesMiss(level, rite)) {
				rite.addInstability(12 + CardinalRiteCeremonyRules.formIndex(recipe.getRiteType()) * 3);
			}
			clearThreats(level, rite);
			if (rite.areAnchorsConsecrated()) rite.completeWave();
			else caster.displayClientMessage(Component.literal(
					"The ordeal cannot advance while the cardinal stations run dry.")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
		if (rite.getPhaseTicks() % 20 == 0) {
			rite.applyAnchorDeficitPressure();
		}
	}

	private static void tickStillInterval(ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		if (rite.getPhaseTicks() == 1) {
			caster.displayClientMessage(Component.literal("A still interval: repair, reinforce, and breathe.")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
		}
		drawSupportSockets(caster.serverLevel(), rite, recipe);
		drawAnchors(caster.serverLevel(), rite, recipe);
		int duration = recipe.getCeremony().stillIntervalTicks();
		if (rite.getPhaseTicks() >= duration) {
			if (rite.areAnchorsConsecrated()) {
				rite.finishStillInterval();
			} else {
				if (rite.getPhaseTicks() == duration) {
					caster.displayClientMessage(Component.literal(
							"The rite waits: restore every cardinal station.")
							.withStyle(ChatFormatting.DARK_RED), false);
				}
				if (rite.getPhaseTicks() % 20 == 0) {
					rite.addInstability(CardinalRiteCeremonyRules.anchorDeficitInstability(
							rite.getAnchorBloodMl()));
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
			default -> wave.startsWith("discover_")
					? "An unknown Ichorian shape wakes. Read it before it reads you."
					: "A response sigil is demanded.";
		};
		caster.displayClientMessage(Component.literal(cue).withStyle(ChatFormatting.DARK_RED), false);
		if (!wave.startsWith("discover_") && !"response_sigil".equals(wave)) {
			spawnThreats(level, caster, rite, wave);
		}
	}

	private static void spawnThreats(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			String wave) {
		int count = Math.max(1, 1 + rite.getDegree() / 3);
		for (int i = 0; i < count; i++) {
			Entity entity = switch (wave) {
				case "fargone_dive" -> EntityInit.fargone.get().create(level);
				case "rogue_will" -> EntityInit.will.get().create(level);
				case "false_omens" -> EntityInit.blood_drunk_puppeteer.get().create(level);
				default -> EntityInit.bloodlicker.get().create(level);
			};
			if (entity == null) continue;
			double angle = Math.PI * 2.0D * i / count;
			double radius = rite.getRiteSize() / 2.0D + 4.0D;
			entity.moveTo(rite.getCenterPos().getX() + 0.5D + Math.cos(angle) * radius,
					rite.getCenterPos().getY() + ("fargone_dive".equals(wave) ? 5.0D : 1.0D),
					rite.getCenterPos().getZ() + 0.5D + Math.sin(angle) * radius,
					(float) Math.toDegrees(angle), 0.0F);
			entity.getPersistentData().putBoolean(CardinalRiteThreatRules.RITE_BOUND_TAG, true);
			entity.getPersistentData().putUUID(RITE_CASTER, rite.getPlayerUUID());
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
			case "fargone_dive" -> HemoCapabilityAccess.getBloodVolume(caster).ifPresent(volume -> {
				int touching = (int) rite.getRiteThreats().stream()
						.map(level::getEntity).filter(java.util.Objects::nonNull)
						.filter(entity -> CardinalRiteThreatRules.canDrainCaster(entity.distanceToSqr(caster)))
						.count();
				int drain = Math.min(touching * 5, (int) volume.getBloodVolume());
				if (drain > 0) {
					volume.drain(drain);
					BloodVolumeEvents.syncVolume(caster, volume);
				}
			});
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
				int ring = Math.floorMod(anchorOrdinal++, Math.max(1, rite.getDegree()));
				for (int i = ring * 4; i < Math.min(anchors.length, ring * 4 + 4); i++) {
					if (rite.bloodNeededForAnchor(i) <= 0) continue;
					int drawn = CardinalRiteAllyService.spend(level, rite, entry.getKey(), 10);
					if (drawn > 0) rite.fillAnchor(i, drawn);
					break;
				}
			}
		}
		if (rite.getPhaseTicks() % 100 == 0) {
			for (var entry : rite.getAllyRoles().entrySet()) {
				if (entry.getValue() != CardinalRiteAllyRole.WARDEN
						|| !CardinalRiteAllyService.isAvailable(level, rite, entry.getKey())
						|| CardinalRiteAllyService.spend(level, rite, entry.getKey(), 25) < 25) continue;
				for (UUID threatId : rite.getRiteThreats()) {
					Entity threat = level.getEntity(threatId);
					if (threat instanceof Mob mob) {
						mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
						break;
					}
				}
			}
		}
	}

	private static void drawAllyStations(ServerLevel level, ActiveCardinalRite rite) {
		if (rite.getDegree() < 5 || level.getGameTime() % 4 != 0) return;
		for (var marker : CardinalRiteAllyService.markers().entrySet()) {
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
			if (entity != null) entity.discard();
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

	private static void drawAnchors(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (level.getGameTime() % 3 != 0) return;
		int[] blood = rite.getAnchorBloodMl();
		for (int i = 0; i < recipe.getCeremony().anchors().size() && i < blood.length; i++) {
			BlockPos offset = recipe.getCeremony().anchors().get(i).offset();
			float fill = blood[i] / (float) CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
			if (rite.isInstabilityDamagedAnchor(i)) continue;
			if (CardinalRiteAnchorVisualRules.boundaryVisual(
					blood[i], CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML)
					== CardinalRiteAnchorVisualRules.Visual.SANGUINE_BLOB) continue;
			ParticleColor color = new ParticleColor(130 + (int) (100 * fill), 15, 25);
			Vec3 aimPoint = CardinalRiteTargetGeometry.anchorAimPoint(rite.getCenterPos(), offset);
			drawInteractionMarker(level, color,
					aimPoint.x, aimPoint.y, aimPoint.z,
					i % 4 == 0 ? 4 : 2, 0.14D, 0.02D, 0.14D);
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
			BlockPos offset) {
		drawSigil(level, rite, id, offset, id.toString());
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
		BlockPos[] fakes = {
				rite.getCenterPos().offset(2, 1, 2),
				rite.getCenterPos().offset(-2, 1, -2),
				rite.getCenterPos().offset(2, 1, -2)
		};
		boolean revealed = CardinalRiteSupportSigilRules.revealsFalseOmens(
				isComplete(rite, "mnemonic"), isComplete(rite, "lens"));
		ParticleColor fakeColor = revealed ? new ParticleColor(90, 210, 255)
				: new ParticleColor(185, 40, 220);
		for (BlockPos fake : fakes) {
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
				case BLOOD_CELL -> level.sendParticles(BloodCellParticleFactory.createData(color),
						x, y, z, bloodCellCount, spreadX, spreadY, spreadZ, 0.0D);
				case GLOW -> level.sendParticles(GlowParticleFactory.createData(color),
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
