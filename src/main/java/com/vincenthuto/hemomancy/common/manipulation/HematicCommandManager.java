package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.HematicCommandState.Command;
import com.vincenthuto.hemomancy.common.manipulation.HematicCommandState.Mode;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.NerveCommandRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Rebuke, Impressment, Sovereign redirection and the Marionette. The command itself lives on the mob
 * ({@link HematicCommandState}, an attachment), so it survives the mob unloading and the server restarting; the maps here
 * are only an index of the loaded mobs under command, rebuilt as each one joins its level. A command whose caster is
 * away waits (the mob stays out of its own targeting) until the caster returns or it runs out. The Marionette is a
 * channel and ends when its body unloads.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HematicCommandManager {
    /** Loaded mobs under command, by UUID; the command is read from each mob's attachment. */
    private static final Map<UUID, Mob> BODIES = new HashMap<>();
    private static final Map<UUID, MarionetteMoveGoal> MOVEMENT = new HashMap<>();
    private static final String COMMAND_PROJECTILE_OWNER = "hemomancy:command_projectile_owner";
    private static final Map<UUID, Order> ORDERS = new HashMap<>();
    private record Order(UUID enemy, Vec3 ground) {}
    public interface CommandedCreeper { void hemomancy$cancelCommandedFuse(); }
	private static final Map<UUID, UUID> IMPRESSED_BY_CASTER = new HashMap<>();

	private HematicCommandManager() {
	}

	public static boolean rebuke(ServerPlayer caster, LivingEntity target) {
		return apply(caster, target, Mode.REBUKED, HematicCommandRules.REBUKE_DURATION_TICKS, null);
	}

	public static boolean impress(ServerPlayer caster, LivingEntity target) {
		if (!available(caster, target)) return false;
        if (isMarionetteChannel(caster)) ManipulationChannelManager.stop(caster, false);
		UUID previousId = IMPRESSED_BY_CASTER.get(caster.getUUID());
		if (previousId != null && BODIES.get(previousId) instanceof Mob previous) release(previous);
		IMPRESSED_BY_CASTER.remove(caster.getUUID());
		return apply(caster, target, Mode.IMPRESSED,
				HematicCommandRules.impressmentDurationTicks(target.getMaxHealth()), null);
	}

	public static boolean canCommand(LivingEntity target) {
		return HematicCommandRules.canCommand(target instanceof Mob, target instanceof ServerPlayer,
				HemoEntityPredicates.isBoss(target), HemoEntityPredicates.NOBLOOD.test(target), target.getMaxHealth());
	}

	public static boolean isRebuked(LivingEntity target) {
		Command state = command(target);
		return state != null && state.mode() == Mode.REBUKED;
	}

	public static boolean isImpressed(LivingEntity target, ServerPlayer caster) {
		Command state = command(target);
		return state != null && state.controlled() && state.caster().equals(caster.getUUID());
	}

	public static boolean redirect(ServerPlayer caster, Mob mob, LivingEntity target, int duration) {
		return apply(caster, mob, Mode.REDIRECTED, duration, target.getUUID());
	}

	/** The command on a loaded, indexed mob, or null. */
	private static Command command(Entity entity) {
		return entity != null && BODIES.containsKey(entity.getUUID()) ? HemoCapabilityAccess.getHematicCommand(entity) : null;
	}

	private static Command command(UUID id) {
		Mob mob = BODIES.get(id);
		return mob == null ? null : HemoCapabilityAccess.getHematicCommand(mob);
	}

    public static boolean available(ServerPlayer caster, LivingEntity target) {
        return availableForMarionette(caster, target)
                && !target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_binding);
    }

    /** Marionette takes a Blood-Bound body by releasing the binding; the other commands refuse a captive. */
    public static boolean availableForMarionette(ServerPlayer caster, LivingEntity target) {
        Command existing = command(target);
        return canCommand(target) && ManipulationCombatHelper.canHarm(caster, target)
                && (existing == null || existing.caster().equals(caster.getUUID()))
                && !(target instanceof net.minecraft.world.entity.OwnableEntity ownable && ownable.getOwnerUUID() != null)
                && !(target instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon bound
                    && bound.hemomancy$getOwnerUUID() != null)
                && !(target.getVehicle() instanceof com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity);
    }

    public static Mob marionette(ServerPlayer caster) {
        UUID id = IMPRESSED_BY_CASTER.get(caster.getUUID());
        Command state = id == null ? null : command(id);
        return state != null && state.mode() == Mode.MARIONETTE ? BODIES.get(id) : null;
    }

    public static boolean isMarionetteChannel(ServerPlayer caster) {
        return marionette(caster) != null;
    }

    public static boolean acquireMarionette(ServerPlayer caster, LivingEntity target) {
        if (!(target instanceof Mob) || !availableForMarionette(caster, target) || caster.distanceToSqr(target) > 64)
            return false;
        UUID previous = IMPRESSED_BY_CASTER.get(caster.getUUID());
        Mob previousBody = previous == null ? null : BODIES.get(previous);
        if (previousBody != null) release(previousBody);
        target.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_binding);
        return apply(caster, target, Mode.MARIONETTE, SanguineMarionetteRules.MAX_TICKS, null);
    }

    public static boolean maintainMarionette(ServerPlayer caster) {
        Mob mob = marionette(caster);
        if (mob == null || !mob.isAlive() || !caster.isAlive() || mob.level() != caster.level()) return false;
        Command state = command(mob);
        return SanguineMarionetteRules.maintain(caster.level().getGameTime(),
                state.until() - SanguineMarionetteRules.MAX_TICKS, caster.distanceTo(mob));
    }

    public static void releaseMarionette(UUID caster) {
        UUID id = IMPRESSED_BY_CASTER.get(caster);
        Command state = id == null ? null : command(id);
        if (state != null && state.mode() == Mode.MARIONETTE) {
            Mob mob = BODIES.get(id);
            if (mob != null) release(mob);
        }
    }

    public static boolean orderAttack(ServerPlayer caster, LivingEntity enemy) {
        Mob mob = marionette(caster);
        if (mob == null || enemy == mob || !ManipulationCombatHelper.canHarm(caster, enemy)
                || caster.distanceToSqr(enemy) > 256 || !mob.canAttack(enemy)) return false;
        ORDERS.put(mob.getUUID(), new Order(enemy.getUUID(), null));
        mob.setTarget(enemy);
        mob.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, enemy);
        return true;
    }

    public static boolean orderMove(ServerPlayer caster, net.minecraft.core.BlockPos ground) {
        Mob mob = marionette(caster);
        if (mob == null || caster.position().distanceToSqr(Vec3.atBottomCenterOf(ground)) > 256
                || !caster.level().getWorldBorder().isWithinBounds(ground)) return false;
        var path = mob.getNavigation().createPath(ground, 0);
        if (path == null || !path.canReach()) return false;
        ORDERS.put(mob.getUUID(), new Order(null, Vec3.atBottomCenterOf(ground)));
        mob.setTarget(null);
        mob.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET);
        mob.getNavigation().moveTo(path, 1.1);
        mob.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
                new net.minecraft.world.entity.ai.memory.WalkTarget(ground, 1.1f, 1));
        return true;
    }

    public static void aimMarionette(ServerPlayer caster) {
        if (caster.level().getGameTime() % 5 != 0) return;
        LivingEntity enemy = ManipulationCombatHelper.aimedTarget(caster, caster.level(), 16, .985);
        if (enemy != null && orderAttack(caster, enemy)) return;
        Vec3 eye = caster.getEyePosition();
        var ground = caster.level().clip(new net.minecraft.world.level.ClipContext(eye,
                eye.add(caster.getLookAngle().scale(16)), net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, caster));
        if (ground.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && ground.getDirection() == net.minecraft.core.Direction.UP) orderMove(caster, ground.getBlockPos().above());
    }

    private static Order marionetteOrder(Mob mob) {
        Command state = command(mob);
        return state != null && state.mode() == Mode.MARIONETTE ? ORDERS.get(mob.getUUID()) : null;
    }

    private static LivingEntity orderedEnemy(Mob mob) {
        Order order = marionetteOrder(mob);
        if (order == null || order.enemy == null || !(mob.level() instanceof ServerLevel level)) return null;
        Entity enemy = level.getEntity(order.enemy);
        return enemy instanceof LivingEntity living && living.isAlive() && mayAffect(mob, living) && mob.canAttack(living)
                ? living : null;
    }

    public static void maintainBrainOrder(Mob mob) {
        Command state = command(mob);
        if (state == null || state.mode() != Mode.MARIONETTE) return;
        LivingEntity enemy = orderedEnemy(mob);
        mob.setTarget(enemy);
        mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, java.util.Optional.ofNullable(enemy));
        Order order = marionetteOrder(mob);
        if (order != null && order.ground != null && mob.position().distanceToSqr(order.ground) > 1)
            mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(order.ground, 1.1f, 1));
    }

    /** Only order memories are reserved; species combat, cooldowns and locomotion remain native. */
    public static boolean mayChangeBrainMemory(Mob mob, MemoryModuleType<?> type, Object value) {
        Command state = command(mob);
        if (state == null || state.mode() != Mode.MARIONETTE) return true;
        if (type == MemoryModuleType.ATTACK_TARGET) return value == orderedEnemy(mob);
        Order order = marionetteOrder(mob);
        if (type == MemoryModuleType.WALK_TARGET && order != null && order.ground != null) {
            if (value == null) return mob.position().distanceToSqr(order.ground) <= 1;
            return value instanceof WalkTarget walk && walk.getTarget().currentPosition().distanceToSqr(order.ground) < .01;
        }
        return true;
    }

    @SubscribeEvent
    public static void commandedTarget(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        Command state = command(mob);
        if (state != null && state.mode() == Mode.MARIONETTE) event.setNewAboutToBeSetTarget(orderedEnemy(mob));
    }

    private static boolean apply(ServerPlayer caster, LivingEntity target, Mode mode, int duration, UUID redirectedTarget) {
		if (!(target instanceof Mob mob) || !ManipulationCombatHelper.canHarm(caster, target)
				|| !canCommand(target)) return false;
		Command existing = command(target);
        if (existing != null && !existing.caster().equals(caster.getUUID())) return false;
        boolean targetDisabled = existing != null ? existing.targetDisabled()
                : ((com.vincenthuto.hemomancy.mixin.core.GoalSelectorAccessor)mob.targetSelector)
                    .hemomancy$disabledFlags().contains(Goal.Flag.TARGET);
        Command old = existing;
        HemoCapabilityAccess.setHematicCommand(mob, new Command(caster.getUUID(),
				caster.level().getGameTime() + duration, mode, redirectedTarget, targetDisabled));
        BODIES.put(mob.getUUID(), mob);
        if (mode == Mode.MARIONETTE && !MOVEMENT.containsKey(mob.getUUID())) {
            var goal = new MarionetteMoveGoal(mob);
            MOVEMENT.put(mob.getUUID(), goal);
            mob.goalSelector.addGoal(0, goal);
        }
		if (old != null && old.controlled()) IMPRESSED_BY_CASTER.remove(old.caster(), target.getUUID());
		if (mode == Mode.IMPRESSED || mode == Mode.MARIONETTE) IMPRESSED_BY_CASTER.put(caster.getUUID(), target.getUUID());
		mob.setTarget(null);
		mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
		if (mode == Mode.REDIRECTED) {
			Entity redirected = caster.serverLevel().getEntity(redirectedTarget);
			mob.setTarget(redirected instanceof LivingEntity living ? living : null);
		} else {
			feedback(caster.serverLevel(), mob, mode);
			// The command overrides what the body was doing: a nerve interruption, with a bolt from the caster's eyes.
			if (mode == Mode.REBUKED || mode == Mode.IMPRESSED) {
				SchoolStates.apply(caster, mob, SchoolState.DISRUPTED, 10);
				if (mode == Mode.REBUKED) DuctilisLightningEffects.synapticJolt(caster, mob);
				else DuctilisLightningEffects.conductiveMark(caster, mob);
			}
		}
        visual(caster,mob,command(mob));
		return true;
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || BODIES.isEmpty()) return;
		long now = level.getGameTime();
		for (Mob mob : java.util.List.copyOf(BODIES.values())) {
			if (mob.level() != level) continue;
			Command state = HemoCapabilityAccess.getHematicCommand(mob);
			if (state == null) {
				BODIES.remove(mob.getUUID());
				continue;
			}
			ServerPlayer caster = level.getPlayerByUUID(state.caster()) instanceof ServerPlayer player ? player : null;
			if (!mob.isAlive() || now >= state.until()
                    || state.mode() == Mode.MARIONETTE && (caster == null || !maintainMarionette(caster))) {
				release(mob);
                if (state.mode() == Mode.MARIONETTE && caster != null) ManipulationChannelManager.stop(caster, false);
				continue;
			}
			if (caster == null) {
				// The caster is away: the body waits, out of its own targeting, until they return or the command runs out.
				mob.setTarget(null);
				continue;
			}
	            if(now%5==0) visual(caster,mob,state);
			if (state.mode() == Mode.REBUKED) tickRebuke(mob, caster, now);
			else if (state.mode() == Mode.REDIRECTED) {
				Entity redirected = level.getEntity(state.redirectedTarget());
				mob.setTarget(redirected instanceof LivingEntity living && ManipulationCombatHelper.canHarm(caster, living) ? living : null);
			} else if (state.mode() == Mode.MARIONETTE) {
                maintainBrainOrder(mob);
            } else tickImpressed(mob, caster);
		}
	}

	@SubscribeEvent
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        if (direct != null && direct.getPersistentData().hasUUID(COMMAND_PROJECTILE_OWNER)) {
            UUID owner = direct.getPersistentData().getUUID(COMMAND_PROJECTILE_OWNER);
            if (!(event.getEntity().level().getPlayerByUUID(owner) instanceof ServerPlayer caster)
                    || !ManipulationCombatHelper.canHarm(caster, event.getEntity())) {
                event.setCanceled(true);
                return;
            }
        }
		Entity attacker = event.getSource().getEntity();
		if (!(attacker instanceof LivingEntity livingAttacker)) return;
		Command defenderState = command(event.getEntity());
		if (defenderState != null && defenderState.controlled()
				&& alliedToCaster(event.getEntity().level(), defenderState.caster(), livingAttacker)) {
			event.setCanceled(true);
			return;
		}
		Command attackerState = command(livingAttacker);
		if (attackerState != null && attackerState.controlled()
				&& alliedToCaster(event.getEntity().level(), attackerState.caster(), event.getEntity())) {
			event.setCanceled(true);
		}
	}

    @SubscribeEvent
    public static void explosion(net.neoforged.neoforge.event.level.ExplosionEvent.Detonate event) {
        Entity source = event.getExplosion().getDirectSourceEntity();
        event.getAffectedEntities().removeIf(entity -> entity instanceof LivingEntity living && !mayAffect(source, living));
    }

    public static void copyAttackOwner(Entity source, Entity target) {
        UUID owner = attackOwner(source);
        if (owner != null) target.getPersistentData().putUUID(COMMAND_PROJECTILE_OWNER, owner);
    }

    private static UUID attackOwner(Entity source) {
        if (source == null) return null;
        if (source.getPersistentData().hasUUID(COMMAND_PROJECTILE_OWNER))
            return source.getPersistentData().getUUID(COMMAND_PROJECTILE_OWNER);
        Command state = command(source);
        return state != null && state.controlled() ? state.caster() : null;
    }

    public static boolean mayAffect(Entity source, LivingEntity target) {
        UUID owner = attackOwner(source);
        if (owner == null || target.level().isClientSide) return true;
        return target.level().getPlayerByUUID(owner) instanceof ServerPlayer caster
                && ManipulationCombatHelper.canHarm(caster, target);
    }

    @SubscribeEvent
    public static void launched(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || event.getLevel().isClientSide) return;
        Entity attack = event.getEntity();
        Entity origin = attack instanceof net.minecraft.world.entity.TraceableEntity traceable ? traceable.getOwner()
                : attack instanceof net.minecraft.world.entity.monster.Vex vex ? vex.getOwner() : null;
        if (origin instanceof LivingEntity owner) {
            copyAttackOwner(owner, attack);
            if (owner instanceof ServerPlayer player && isMarionetteChannel(player))
                ManipulationChannelManager.stop(player, false);
        }
    }

    private static final class MarionetteMoveGoal extends Goal {
        private final Mob mob;
        MarionetteMoveGoal(Mob mob) {
            this.mob = mob;
            setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        @Override public boolean canUse() {
            var state = command(mob);
            var order = ORDERS.get(mob.getUUID());
            return state != null && state.mode() == Mode.MARIONETTE && order != null && order.ground != null;
        }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void tick() {
            var order = ORDERS.get(mob.getUUID());
            if (order == null || order.ground == null) return;
            Vec3 point = order.ground;
            if (mob.position().distanceToSqr(point) <= 1) mob.getNavigation().stop();
            else if (mob.tickCount % 5 == 0 || mob.getNavigation().isDone())
                mob.getNavigation().moveTo(point.x, point.y, point.z, 1.1);
            mob.getLookControl().setLookAt(point.x, point.y + mob.getEyeHeight(), point.z);
            mob.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
                    new net.minecraft.world.entity.ai.memory.WalkTarget(point, 1.1f, 1));
        }
        @Override public void stop() { mob.getNavigation().stop(); }
    }

	private static void tickRebuke(Mob mob, ServerPlayer caster, long now) {
		mob.setTarget(null);
		if (now % 10L != 0L) return;
		Vec3 away = mob.position().subtract(caster.position());
		if (away.horizontalDistanceSqr() < 0.01D) {
			away = new Vec3(mob.getRandom().nextDouble() - 0.5D, 0.0D,
					mob.getRandom().nextDouble() - 0.5D);
		}
		away = away.normalize().scale(12.0D);
		mob.getNavigation().moveTo(mob.getX() + away.x, mob.getY(), mob.getZ() + away.z, 1.35D);
	}

	private static void tickImpressed(Mob mob, ServerPlayer caster) {
		LivingEntity hostile = caster.getLastHurtByMob();
		if (!validHostile(mob, caster, hostile)) {
			hostile = mob.level().getEntitiesOfClass(Mob.class, new AABB(caster.blockPosition()).inflate(20.0D),
					candidate -> candidate != mob && candidate.getTarget() != null
							&& alliedToCaster(mob.level(), caster.getUUID(), candidate.getTarget())
							&& validHostile(mob, caster, candidate))
					.stream().findFirst().orElse(null);
		}
		mob.setTarget(hostile);
		if (hostile == null && mob.distanceToSqr(caster) > 36.0D) {
			mob.getNavigation().moveTo(caster, 1.15D);
		}
	}

	private static boolean validHostile(Mob impressed, ServerPlayer caster, LivingEntity target) {
		return target != null && target.isAlive() && target != impressed
				&& !alliedToCaster(impressed.level(), caster.getUUID(), target)
				&& impressed.canAttack(target);
	}

	private static boolean alliedToCaster(Level level, UUID casterId, LivingEntity entity) {
		if (!(level instanceof ServerLevel serverLevel)) return false;
		ServerPlayer caster = serverLevel.getPlayerByUUID(casterId) instanceof ServerPlayer player ? player
                : serverLevel.getServer().getPlayerList().getPlayer(casterId);
		return caster != null && !ManipulationCombatHelper.canHarm(caster, entity);
	}

    private static void visual(ServerPlayer caster, Mob mob, Command state) {
        if (state.mode() == Mode.MARIONETTE) {
            int ticks = (int)Math.min(10, state.until() - caster.level().getGameTime());
            var tether = new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(
                    ManipulationVisuals.Form.MARIONETTE_TETHER, mob.getId(), mob.position(), caster.getEyePosition(),
                    mob.getBbHeight(), ticks, caster.getId());
            PacketHandler.sendTracking(mob, tether);
            Order order = ORDERS.get(mob.getUUID());
            if (order != null) {
                Entity enemy = order.enemy == null ? null : mob.level() instanceof ServerLevel level ? level.getEntity(order.enemy) : null;
                Vec3 destination = enemy == null ? order.ground : enemy.position();
                if (destination != null) PacketHandler.sendTracking(mob,
                        new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(
                                ManipulationVisuals.Form.MARIONETTE_ORDER, mob.getId(), mob.position(), destination,
                                1, ticks, enemy == null ? -1 : enemy.getId()));
            }
            return;
        }
        // Rebuke and Impressment are Ductilis nerve commands; Sovereign Instinct's redirection keeps the Animus crown.
        boolean nerve = state.mode() == Mode.REBUKED || state.mode() == Mode.IMPRESSED;
        PacketHandler.sendNear(caster.serverLevel(),
                mob.getX(),mob.getY(),mob.getZ(),64,
                new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(
                        nerve ? ManipulationVisuals.Form.NERVE_COMMAND : ManipulationVisuals.Form.COMMAND,mob.getId(),mob.position(),
                        caster.getEyePosition(),mob.getBbHeight(),(int)Math.min(10,state.until()-caster.level().getGameTime()),
                        nerve ? (state.mode() == Mode.REBUKED ? NerveCommandRules.REBUKE : NerveCommandRules.IMPRESS)
                                : state.mode().ordinal()+1));
    }

	private static void release(Mob mob) {
        Command old = HemoCapabilityAccess.getHematicCommand(mob);
        HemoCapabilityAccess.setHematicCommand(mob, null);
        if (old != null && old.controlled()) IMPRESSED_BY_CASTER.remove(old.caster(), mob.getUUID());
        BODIES.remove(mob.getUUID());
        ORDERS.remove(mob.getUUID());
        Goal movement = MOVEMENT.remove(mob.getUUID());
        if (movement != null) mob.goalSelector.removeGoal(movement);
        mob.stopUsingItem();
        mob.goalSelector.getAvailableGoals().stream()
                .filter(net.minecraft.world.entity.ai.goal.WrappedGoal::isRunning).toList()
                .forEach(net.minecraft.world.entity.ai.goal.WrappedGoal::stop);
        if (mob instanceof net.minecraft.world.entity.monster.Creeper creeper && !creeper.isIgnited())
            ((CommandedCreeper)creeper).hemomancy$cancelCommandedFuse();
        mob.getNavigation().stop();
        mob.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
        mob.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.LOOK_TARGET);
        ManipulationVisuals.attached(mob,ManipulationVisuals.Form.COMMAND,0,0,0);
        ManipulationVisuals.attached(mob,ManipulationVisuals.Form.NERVE_COMMAND,0,0,0);
        ManipulationVisuals.attached(mob,ManipulationVisuals.Form.MARIONETTE_TETHER,0,0,0);
        ManipulationVisuals.attached(mob,ManipulationVisuals.Form.MARIONETTE_ORDER,0,0,0);
		mob.setTarget(null);
		mob.targetSelector.setControlFlag(Goal.Flag.TARGET, old == null || !old.targetDisabled());
	}

    @SubscribeEvent
    public static void tracking(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking event) {
        Command state = command(event.getTarget());
        if (state != null && event.getTarget() instanceof Mob mob
                && mob.level().getPlayerByUUID(state.caster()) instanceof ServerPlayer caster) visual(caster, mob, state);
    }

    /** A mob carrying a command rejoins the index as it loads; a stale or spent command is dropped instead. */
    @SubscribeEvent
    public static void loaded(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) return;
        Command state = HemoCapabilityAccess.getHematicCommand(mob);
        if (state == null) return;
        UUID current = state.controlled() ? IMPRESSED_BY_CASTER.get(state.caster()) : null;
        if (state.mode() == Mode.MARIONETTE || event.getLevel().getGameTime() >= state.until()
                || current != null && !current.equals(mob.getUUID())) {
            HemoCapabilityAccess.setHematicCommand(mob, null);
            return;
        }
        BODIES.put(mob.getUUID(), mob);
        if (state.controlled()) IMPRESSED_BY_CASTER.put(state.caster(), mob.getUUID());
        mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
    }

    /**
     * A commanded mob that unloads keeps its command and leaves the index; one that dies or is discarded is released.
     * The Marionette ends either way, since its caster's channel cannot hold a body that is not there.
     */
    @SubscribeEvent
    public static void unloaded(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && BODIES.containsKey(mob.getUUID())) {
            Command state = HemoCapabilityAccess.getHematicCommand(mob);
            boolean marionette = state != null && state.mode() == Mode.MARIONETTE;
            var reason = mob.getRemovalReason();
            if (state == null || marionette || reason == null || !reason.shouldSave()) {
                release(mob);
            } else {
                BODIES.remove(mob.getUUID());
                ORDERS.remove(mob.getUUID());
                Goal movement = MOVEMENT.remove(mob.getUUID());
                if (movement != null) mob.goalSelector.removeGoal(movement);
                if (state.controlled()) IMPRESSED_BY_CASTER.remove(state.caster(), mob.getUUID());
            }
            if (marionette && event.getLevel() instanceof ServerLevel level
                    && level.getPlayerByUUID(state.caster()) instanceof ServerPlayer caster)
                ManipulationChannelManager.stop(caster, false);
        } else if (event.getEntity() instanceof ServerPlayer player) releaseMarionette(player.getUUID());
    }

    @SubscribeEvent
    public static void casterAttack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isMarionetteChannel(player))
            ManipulationChannelManager.stop(player, false);
    }

	private static void feedback(ServerLevel level, Mob target, Mode mode) {
		level.playSound(null, target.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS,
				0.7F, mode == Mode.REBUKED ? 0.7F : 1.15F);
	}

	/**
	 * Forgets the index of loaded bodies. The commands stay on the mobs, which is what lets them outlast a restart;
	 * each body rejoins the index when it loads again.
	 */
	public static void clearSessionState() {
		BODIES.clear();
		MOVEMENT.clear();
		ORDERS.clear();
		IMPRESSED_BY_CASTER.clear();
	}

	/** Ends every command on a loaded body and gives each its own targeting back (tests use this to clean up). */
	public static void releaseAll() {
		for (Mob mob : java.util.List.copyOf(BODIES.values())) release(mob);
	}
}
