package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.projectile.VesperScuteProjectileEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.summon.PuppetStrings;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonBodies;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Crowned Refusal's grounded moves. {@link VesperMountController} owns each move's timeline, facing and
 * completion; these bodies only apply the effects due at a given tick, aimed at the controller's locked aim.
 */
final class VesperMountMoves {
	static final String VESPER_PUPPET_TAG = "HemomancyVesperPuppet";
	static final int VESPER_PUPPET_MUSTER_SIZE = 3;
	private static final String RECALL_HOOK_KEY = "HemomancyVesperRecallHook";
	/** The cue is cast on tick 1, so its arrival flash peaks here. */
	private static final int RECALL_ARRIVAL_TICK = VesperRecallRules.CUE_TICKS - 2;

	private VesperMountMoves() {
	}

	static void tick(VesperTheCrownedRefusalEntity boss, VesperPhaseOneAttack attack, int tick,
			BossMoveTimeline.Stage stage, Vec3 aim, @Nullable LivingEntity target) {
		if (tick == 1) playWindupCue(boss, attack);
		switch (attack) {
			case ROYAL_SCUTTLE -> {
				if (stage == BossMoveTimeline.Stage.WINDUP) telegraphLine(boss, aim, 0.85F, 0.02F, 0.02F);
				if (tick == 25) {
					Vec3 rush = aim.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
					if (rush.lengthSqr() > 0.01D) boss.setDeltaMovement(rush.normalize().scale(1.65D).add(0.0D, 0.08D, 0.0D));
					boss.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.65F);
				}
				if (tick >= 25 && tick <= 42) hurtNearby(boss, 3.7D, 10.0F, 0.8D);
			}
			case PINCER_VICE -> {
				if ((stage == BossMoveTimeline.Stage.WINDUP || tick < 38) && boss.tickCount % 2 == 0
						&& boss.level() instanceof ServerLevel server) {
					VesperVisualEffects.telegraphCone(server, boss.position(), aim.subtract(boss.position()),
							5.2D, 110.0D, new ParticleColor(204, 5, 5));
				}
				if (tick == 24 || tick == 38) hurtNearbyArc(boss, aim, 5.2D, 110.0D, 12.0F);
			}
			case STINGER_SCRIPT -> {
				// Each volley's spawn points glow for a full 12 ticks before the spikes erupt there.
				if (tick % 12 == 4) {
					if (tick >= 16) summonGripSpikes(boss, boss.plannedPoints());
					boss.plannedPoints().clear();
					if (tick <= 40) planSpikePoints(boss, target != null ? target.position() : aim, 3);
				}
				if (boss.tickCount % 2 == 0 && boss.level() instanceof ServerLevel server) {
					int left = 12 - Math.floorMod(tick - 4, 12);
					for (Vec3 point : boss.plannedPoints()) {
						VesperVisualEffects.spawnMarker(server, point, VesperVisualEffects.BLOOD, left);
					}
				}
			}
			case BROOD_TRAMPLE -> {
				if (stage == BossMoveTimeline.Stage.WINDUP) telegraphRing(boss, 5.5D, 0.65F, 0.0F, 0.0F);
				if (tick == 22) boss.setDeltaMovement(boss.getDeltaMovement().add(0.0D, 0.75D, 0.0D));
				if (tick == 38) {
					hurtNearby(boss, 6.0D, 13.0F, 1.15D);
					if (boss.level() instanceof ServerLevel server) {
						Vec3 impact = boss.position().add(0.0D, 0.2D, 0.0D);
						VesperVisualEffects.telegraphRing(server, impact, 5.5D, VesperVisualEffects.BLOOD, 48);
						VesperVisualEffects.bloodCells(server, impact, VesperVisualEffects.BLOOD,
								48, 4.2D, 0.25D, 4.2D, 0.11D);
						VesperVisualEffects.darkGlow(server, impact, VesperVisualEffects.BLACK,
								30, 4.5D, 0.35D, 4.5D, 0.07D);
					}
				}
			}
			case RECALL -> {
				if (tick == 1) hookRecall(boss);
				if (stage == BossMoveTimeline.Stage.WINDUP && tick % 2 == 0) holdRecall(boss, tick);
				if (tick == VesperRecallRules.HAUL_TICK) recallPuppets(boss);
				if (tick == RECALL_ARRIVAL_TICK) recallArrival(boss);
			}
			case PUPPET_MUSTER -> {
				if (tick == 1) planMusterPoints(boss);
				if (stage == BossMoveTimeline.Stage.WINDUP && boss.tickCount % 2 == 0
						&& boss.level() instanceof ServerLevel server) {
					for (Vec3 point : boss.plannedPoints()) {
						VesperVisualEffects.descendingStrings(server, point, VesperVisualEffects.BLOOD, tick / 32.0F);
					}
				}
				if (tick == 32) summonVesperPuppet(boss, target);
			}
			case CARAPACE_ANEURYSM -> tickCarapaceAneurysm(boss, tick);
			case GRAB_IMPALEMENT -> tickGrabImpalement(boss, target, aim, tick);
			default -> { }
		}
	}

	private static void tickCarapaceAneurysm(VesperTheCrownedRefusalEntity boss, int tick) {
		VesperMountAttackRules.AneurysmStage stage = VesperMountAttackRules.aneurysmStage(tick);
		boss.setCarapaceExposed(VesperMountAttackRules.isCarapaceExposed(tick));
		if (stage == VesperMountAttackRules.AneurysmStage.BRACE) {
			telegraphRing(boss, 5.5D, 0.88F, 0.0F, 0.03F);
		}
		if (stage == VesperMountAttackRules.AneurysmStage.ERUPTION) eruptScutes(boss);
		if ((stage == VesperMountAttackRules.AneurysmStage.EXPOSED
				|| stage == VesperMountAttackRules.AneurysmStage.REFORM) && tick % 4 == 1
				&& boss.level() instanceof ServerLevel server) {
			Vec3 wound = boss.position().add(0.0D, boss.getBbHeight() * 0.42D, 0.0D);
			VesperVisualEffects.bloodCells(server, wound, VesperVisualEffects.BLOOD, 8, 1.7D, 1.0D, 2.2D, 0.025D);
			VesperVisualEffects.darkGlow(server, wound, VesperVisualEffects.BLACK, 6, 1.9D, 1.1D, 2.4D, 0.018D);
			if (tick % 12 == 1) {
				double angle = tick * 0.37D;
				Vec3 socket = wound.add(Math.cos(angle) * 1.8D, 0.25D * Math.sin(angle * 0.7D), Math.sin(angle) * 2.1D);
				VesperVisualEffects.voidTendril(server, socket, wound, boss.tickCount * 97L + tick);
			}
		}
		if (tick == 85) boss.playSound(SoundInit.ENTITY_VESPER_CARAPACE_REFORM.get(), 1.4F, 1.0F);
	}

	private static void eruptScutes(VesperTheCrownedRefusalEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 origin = boss.position().add(0.0D, boss.getBbHeight() * 0.46D, 0.0D);
		for (int ring = 0; ring < 2; ring++) {
			for (int i = 0; i < 12; i++) {
				double angle = Mth.TWO_PI * i / 12.0D + (ring == 1 ? Mth.TWO_PI / 24.0D : 0.0D);
				Vec3 direction = new Vec3(Math.cos(angle), ring == 1 ? 0.18D : -0.035D, Math.sin(angle)).normalize();
				VesperScuteProjectileEntity scute = new VesperScuteProjectileEntity(server, boss);
				scute.setPos(origin.x, origin.y + ring * 0.75D, origin.z);
				scute.shoot(direction.x, direction.y, direction.z, 1.25F, 0.0F);
				server.addFreshEntity(scute);
			}
		}
		VesperVisualEffects.bloodCells(server, origin, VesperVisualEffects.BLOOD, 44, 2.2D, 1.2D, 2.2D, 0.14D);
		VesperVisualEffects.darkGlow(server, origin, VesperVisualEffects.BLACK, 28, 2.5D, 1.4D, 2.5D, 0.09D);
		boss.playSound(SoundInit.ENTITY_VESPER_SCUTE_LAUNCH.get(), 1.8F, 1.0F);
	}

	private static void tickGrabImpalement(VesperTheCrownedRefusalEntity boss, @Nullable LivingEntity target,
			Vec3 aim, int tick) {
		LivingEntity victim = boss.getRestrainedVictim();
		VesperMountAttackRules.GrabStage stage = VesperMountAttackRules.grabStage(tick, victim != null);
		if (stage == VesperMountAttackRules.GrabStage.TELEGRAPH) {
			telegraphLine(boss, aim, 0.92F, 0.0F, 0.04F);
			if (tick == 1) boss.playSound(SoundInit.ENTITY_VESPER_GRAB_TELEGRAPH.get(), 1.2F, 1.0F);
		}
		if (tick == 15) {
			Vec3 rush = aim.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (rush.lengthSqr() > 0.01D) boss.setDeltaMovement(rush.normalize().scale(1.15D));
		}
		if (stage == VesperMountAttackRules.GrabStage.LUNGE && victim == null && target != null) boss.tryRestrain(target);
		victim = boss.getRestrainedVictim();
		if (victim != null && tick >= 21 && tick <= 42) boss.tickRestrainedVictim(tick);
		if (stage == VesperMountAttackRules.GrabStage.BITE && victim != null) {
			boss.applyGrabBite();
			if (boss.level() instanceof ServerLevel server) {
				Vec3 feet = victim.position().add(0.0D, 0.15D, 0.0D);
				VesperVisualEffects.bloodCells(server, feet, VesperVisualEffects.BLOOD, 18, 0.5D, 0.25D, 0.5D, 0.08D);
				PacketHandler.sendClawSlash(feet, boss.getLookAngle(), VesperVisualEffects.BLOOD, false, 0.8F, 48.0D, server);
			}
			boss.playSound(SoundInit.ENTITY_VESPER_GRAB_BITE.get(), 1.3F, 1.0F);
		}
		if (stage == VesperMountAttackRules.GrabStage.TAIL_WINDUP && victim != null && tick % 3 == 1
				&& boss.level() instanceof ServerLevel server) {
			Vec3 tip = boss.position().add(Vec3.directionFromRotation(0.0F, boss.getYRot()).scale(-2.0D)).add(0.0D, 3.7D, 0.0D);
			VesperVisualEffects.darkGlow(server, tip, VesperVisualEffects.BLACK, 5, 0.24D, 0.24D, 0.24D, 0.02D);
		}
		if (stage == VesperMountAttackRules.GrabStage.IMPALE && victim != null) {
			boss.applyGrabImpale();
			if (boss.level() instanceof ServerLevel server) {
				Vec3 tip = boss.position().add(Vec3.directionFromRotation(0.0F, boss.getYRot()).scale(-1.8D)).add(0.0D, 3.8D, 0.0D);
				Vec3 impact = victim.position().add(0.0D, victim.getBbHeight() * 0.48D, 0.0D);
				VesperVisualEffects.voidTendril(server, tip, impact, boss.tickCount * 131L);
				VesperVisualEffects.lightning(server, tip, impact, false, boss.tickCount * 173L);
				VesperVisualEffects.bloodCells(server, impact, VesperVisualEffects.BLOOD, 24, 0.45D, 0.7D, 0.45D, 0.1D);
			}
			boss.playSound(SoundInit.ENTITY_VESPER_GRAB_PIERCE.get(), 1.5F, 1.0F);
		}
		if (tick == 43 && victim != null) {
			boss.releaseRestrainedVictim(true);
			boss.playSound(SoundInit.ENTITY_VESPER_GRAB_RELEASE.get(), 1.2F, 1.0F);
		}
	}

	private static void planSpikePoints(VesperTheCrownedRefusalEntity boss, Vec3 around, int count) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		RandomSource random = server.getRandom();
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Mth.TWO_PI;
			double radius = 1.5D + random.nextDouble() * 5.0D;
			BlockPos base = BlockPos.containing(around.x + Mth.cos((float) angle) * radius,
					around.y, around.z + Mth.sin((float) angle) * radius);
			BlockPos pos = EndgameBossActions.findGround(server, base);
			boss.plannedPoints().add(new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D));
		}
	}

	/** Spikes only ever erupt where a marker was shown; a reload mid-volley simply skips that volley. */
	private static void summonGripSpikes(Mob boss, java.util.List<Vec3> points) {
		if (!(boss.level() instanceof ServerLevel server) || points.isEmpty()) return;
		RandomSource random = server.getRandom();
		for (Vec3 point : points) {
			EntityIronSpike spike = new EntityIronSpike(EntityInit.iron_spike.get(), server, boss);
			spike.moveTo(point.x, point.y, point.z, random.nextFloat() * 360.0F, 0.0F);
			server.addFreshEntity(spike);
		}
		Vec3 first = points.getFirst();
		server.playSound(null, first.x, first.y, first.z,
				SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 1.0F, 0.5F);
	}

	private static void planMusterPoints(VesperTheCrownedRefusalEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		boss.plannedPoints().clear();
		boss.plannedShapes().clear();
		boss.plannedShapes().addAll(boss.roster().nextMuster(boss.getRandom()));
		for (int i = 0; i < boss.plannedShapes().size(); i++) {
			BlockPos pos = EndgameBossActions.randomNearbyGround(server, boss.blockPosition(), 6);
			boss.plannedPoints().add(new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D));
		}
	}

	private static void playWindupCue(VesperTheCrownedRefusalEntity boss, VesperPhaseOneAttack attack) {
		switch (attack) {
			case ROYAL_SCUTTLE -> boss.playSound(SoundInit.ENTITY_VESPER_GRAB_TELEGRAPH.get(), 1.3F, 0.8F);
			case PINCER_VICE -> boss.playSound(SoundInit.ENTITY_VESPER_GRAB_TELEGRAPH.get(), 1.2F, 1.25F);
			case STINGER_SCRIPT -> boss.playSound(SoundInit.ENTITY_VESPER_AMBIENT.get(), 1.4F, 1.4F);
			case BROOD_TRAMPLE -> boss.playSound(SoundEvents.RAVAGER_ROAR, 1.3F, 0.5F);
			case CARAPACE_ANEURYSM -> boss.playSound(SoundInit.ENTITY_VESPER_HURT.get(), 1.5F, 0.6F);
			case PUPPET_MUSTER -> boss.playSound(SoundEvents.WARDEN_HEARTBEAT, 1.3F, 0.8F);
			default -> { }
		}
	}

	/** Calls the planned roster shapes onto their telegraphed landing points, lowered in on Vesper's strings. */
	private static void summonVesperPuppet(VesperTheCrownedRefusalEntity boss, @Nullable LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		List<String> shapes = List.copyOf(boss.plannedShapes());
		for (int i = 0; i < shapes.size() && i < VESPER_PUPPET_MUSTER_SIZE; i++) {
			String shape = shapes.get(i);
			if (boss.roster().slot(shape) != VesperPuppetRoster.Slot.AVAILABLE) continue;
			Mob puppet = PuppeteerSummonBodies.create(shape, server).orElse(null);
			if (!(puppet instanceof BoundPuppeteerSummon bound)) continue;
			Vec3 landing = i < boss.plannedPoints().size() ? boss.plannedPoints().get(i)
					: Vec3.atBottomCenterOf(EndgameBossActions.randomNearbyGround(server, boss.blockPosition(), 6));
			puppet.moveTo(landing.x, landing.y, landing.z, boss.getYRot(), 0.0F);
			bound.hemomancy$setTrialSummon(true);
			bound.hemomancy$setTrialCasterUUID(boss.getOrdealOwner());
			bound.hemomancy$setOwnerUUID(boss.getUUID());
			bound.hemomancy$setSummonName(shape);
			PuppeteerSummonDefinitions.byName(shape).ifPresent(definition ->
					BoundSummonBehavior.applyTrialStats(puppet, definition));
			puppet.addTag(VESPER_PUPPET_TAG);
			puppet.getPersistentData().putBoolean(VesperEncounterPuppetEvents.PUPPET_KEY, true);
			puppet.getPersistentData().putUUID(VesperEncounterPuppetEvents.BOSS_KEY, boss.getUUID());
			puppet.getPersistentData().putLong(VesperEncounterPuppetEvents.SINCE_KEY, server.getGameTime());
			if (target != null) puppet.setTarget(target);
			puppet.setPersistenceRequired();
			if (!server.addFreshEntity(puppet)) continue;
			PuppetStrings.descend(puppet);
			boss.roster().markOut(shape);
			Vec3 puppetCenter = puppet.position().add(0.0D, puppet.getBbHeight() * 0.5D, 0.0D);
			Vec3 throneCenter = boss.position().add(0.0D, boss.getBbHeight() * 0.62D, 0.0D);
			VesperVisualEffects.voidTendril(server, throneCenter, puppetCenter,
					boss.tickCount * 211L + puppet.getId());
			server.playSound(null, BlockPos.containing(landing), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE,
					0.9F, 1.25F + i * 0.08F);
		}
		boss.plannedShapes().clear();
	}

	/** Where his raised Crossbar's tip is now. */
	static Vec3 crossbarTip(VesperTheCrownedRefusalEntity boss) {
		double[] tip = VesperRecallRules.crossbarOffset(boss.yBodyRot);
		return boss.position().add(tip[0], tip[1], tip[2]);
	}

	/**
	 * The Crossbar goes up and his strings whip out onto every puppet that has had its turn. Hooked puppets are held
	 * (slowed) and marked on the floor until the haul: the player's last chance to kill one and strike its shape.
	 */
	private static void hookRecall(VesperTheCrownedRefusalEntity boss) {
		boss.plannedPoints().clear();
		boss.playSound(SoundEvents.CHAIN_PLACE, 1.4F, 0.6F);
		if (!(boss.level() instanceof ServerLevel server)) return;
		Vec3 tip = crossbarTip(boss);
		for (Mob puppet : ownedPuppets(boss)) {
			long since = puppet.getPersistentData().getLong(VesperEncounterPuppetEvents.SINCE_KEY);
			if (!VesperPuppetRoster.mayRecall((int) (server.getGameTime() - since))) continue;
			puppet.getPersistentData().putLong(RECALL_HOOK_KEY, server.getGameTime());
			ManipulationVisuals.attached(puppet, ManipulationVisuals.Form.VESPER_RECALL, tip, puppet.getBbHeight(),
					VesperRecallRules.CUE_TICKS, boss.getId());
			Vec3 shoulders = puppet.position().add(0.0D, puppet.getBbHeight() * 0.9D, 0.0D);
			VesperVisualEffects.bloodCells(server, shoulders, VesperVisualEffects.BLOOD, 8,
					puppet.getBbWidth() * 0.3D, 0.15D, puppet.getBbWidth() * 0.3D, 0.04D);
			server.playSound(null, puppet.blockPosition(), SoundEvents.CHAIN_HIT, SoundSource.HOSTILE, 1.0F, 0.7F);
		}
	}

	private static void holdRecall(VesperTheCrownedRefusalEntity boss, int tick) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		long now = server.getGameTime();
		boolean any = false;
		for (Mob puppet : ownedPuppets(boss)) {
			if (!hookedForRecall(puppet, now)) continue;
			any = true;
			puppet.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 1, false, false));
			if (tick % 4 == 0) {
				VesperVisualEffects.telegraphRing(server, puppet.position().add(0.0D, 0.05D, 0.0D),
						puppet.getBbWidth() * 0.7D + 0.35D, VesperVisualEffects.BLOOD, 12);
			}
		}
		if (any) {
			VesperVisualEffects.darkGlow(server, crossbarTip(boss), VesperVisualEffects.BLACK, 2, 0.12D, 0.12D, 0.12D, 0.01D);
			if (tick % 6 == 0) boss.playSound(SoundEvents.CHAIN_STEP, 0.9F, 0.5F + tick * 0.02F);
		}
	}

	private static boolean hookedForRecall(Mob puppet, long now) {
		return puppet.getPersistentData().contains(RECALL_HOOK_KEY)
				&& now - puppet.getPersistentData().getLong(RECALL_HOOK_KEY) <= VesperRecallRules.HAUL_TICK + 10;
	}

	/**
	 * Puppets that have had their turn are hauled back up on their strings to wait in the roster. With strings
	 * already cast, only the hooked puppets go, on his strings; otherwise (a reloaded Recall) each goes on its own.
	 */
	static void recallPuppets(VesperTheCrownedRefusalEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		long now = server.getGameTime();
		List<Mob> puppets = ownedPuppets(boss);
		boolean hookedAny = puppets.stream().anyMatch(puppet -> hookedForRecall(puppet, now));
		for (Mob puppet : puppets) {
			boolean hooked = hookedForRecall(puppet, now);
			puppet.getPersistentData().remove(RECALL_HOOK_KEY);
			long since = puppet.getPersistentData().getLong(VesperEncounterPuppetEvents.SINCE_KEY);
			if (hookedAny ? !hooked : !VesperPuppetRoster.mayRecall((int) (now - since))) continue;
			if (puppet instanceof BoundPuppeteerSummon bound) boss.roster().recall(bound.hemomancy$getSummonName());
			PuppetStrings.unspool(puppet, !hooked);
			if (hooked) boss.plannedPoints().add(puppet.position());
		}
		if (hookedAny) boss.playSound(SoundEvents.CHAIN_BREAK, 1.3F, 0.55F);
	}

	/** The reeled-in clots reach his hand: the Crossbar drinks them. */
	private static void recallArrival(VesperTheCrownedRefusalEntity boss) {
		if (boss.plannedPoints().isEmpty() || !(boss.level() instanceof ServerLevel server)) return;
		Vec3 tip = crossbarTip(boss);
		int hauled = boss.plannedPoints().size();
		VesperVisualEffects.bloodCells(server, tip, VesperVisualEffects.BLOOD, 6 * hauled, 0.25D, 0.25D, 0.25D, 0.05D);
		VesperVisualEffects.darkGlow(server, tip, VesperVisualEffects.BLACK, 4 * hauled, 0.3D, 0.3D, 0.3D, 0.03D);
		boss.playSound(SoundEvents.WARDEN_HEARTBEAT, 1.4F, 0.6F);
		boss.plannedPoints().clear();
	}

	static boolean hasRecallablePuppet(VesperTheCrownedRefusalEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return false;
		for (Mob puppet : ownedPuppets(boss)) {
			long since = puppet.getPersistentData().getLong(VesperEncounterPuppetEvents.SINCE_KEY);
			if (VesperPuppetRoster.mayRecall((int) (server.getGameTime() - since))) return true;
		}
		return false;
	}

	static List<Mob> ownedPuppets(VesperTheCrownedRefusalEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return List.of();
		return server.getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(64.0D),
				mob -> mob.isAlive() && !PuppetStrings.isUnspooling(mob) && mob.getTags().contains(VESPER_PUPPET_TAG)
						&& mob.getPersistentData().hasUUID(VesperEncounterPuppetEvents.BOSS_KEY)
						&& boss.getUUID().equals(mob.getPersistentData().getUUID(VesperEncounterPuppetEvents.BOSS_KEY)));
	}

	private static void telegraphLine(Mob boss, Vec3 aim, float red, float green, float blue) {
		if (!(boss.level() instanceof ServerLevel server) || boss.tickCount % 2 != 0) return;
		Vec3 start = boss.position().add(0.0D, 0.1D, 0.0D);
		VesperVisualEffects.telegraphLine(server, start, aim,
				new ParticleColor((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F)));
	}

	private static void telegraphRing(Mob boss, double radius, float red, float green, float blue) {
		if (!(boss.level() instanceof ServerLevel server) || boss.tickCount % 2 != 0) return;
		VesperVisualEffects.telegraphRing(server, boss.position(), radius,
				new ParticleColor((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F)), 28);
	}

	private static void hurtNearby(Mob boss, double radius, float damage, double push) {
		AABB area = boss.getBoundingBox().inflate(radius, 2.0D, radius);
		for (LivingEntity living : boss.level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && entity != boss && !(entity instanceof BoundPuppeteerSummon))) {
			if (!living.hurt(boss.damageSources().mobAttack(boss), damage)) continue;
			Vec3 away = living.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() > 0.01D) {
				away = away.normalize().scale(push);
				living.push(away.x, 0.28D, away.z);
			}
		}
	}

	private static void hurtNearbyArc(Mob boss, Vec3 aim, double radius, double degrees, float damage) {
		Vec3 facing = aim.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (facing.lengthSqr() < 0.01D) facing = boss.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
		facing = facing.normalize();
		double minimumDot = Math.cos(Math.toRadians(degrees * 0.5D));
		for (LivingEntity living : boss.level().getEntitiesOfClass(LivingEntity.class,
				boss.getBoundingBox().inflate(radius), entity -> entity.isAlive() && entity != boss
						&& !(entity instanceof BoundPuppeteerSummon))) {
			Vec3 toward = living.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			if (toward.lengthSqr() > 0.01D && facing.dot(toward.normalize()) >= minimumDot) {
				living.hurt(boss.damageSources().mobAttack(boss), damage);
			}
		}
	}
}
