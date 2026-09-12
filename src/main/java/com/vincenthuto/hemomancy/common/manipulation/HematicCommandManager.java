package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
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

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HematicCommandManager {
	private static final Map<UUID, CommandState> COMMANDS = new HashMap<>();
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
		if (previousId != null) {
			if (caster.serverLevel().getEntity(previousId) instanceof Mob previous) release(previous);
			COMMANDS.remove(previousId);
		}
		return apply(caster, target, Mode.IMPRESSED,
				HematicCommandRules.impressmentDurationTicks(target.getMaxHealth()), null);
	}

	public static boolean canCommand(LivingEntity target) {
		boolean boss = target instanceof EnderDragon || target instanceof WitherBoss
				|| target.getClass().getName().startsWith("com.vincenthuto.hemomancy.common.entity.boss.");
		return HematicCommandRules.canCommand(target instanceof Mob, target instanceof ServerPlayer,
				boss, HemoEntityPredicates.NOBLOOD.test(target), target.getMaxHealth());
	}

	public static boolean isRebuked(LivingEntity target) {
		CommandState state = COMMANDS.get(target.getUUID());
		return state != null && state.mode == Mode.REBUKED;
	}

	public static boolean isImpressed(LivingEntity target, ServerPlayer caster) {
		CommandState state = COMMANDS.get(target.getUUID());
		return state != null && controlled(state) && state.caster.equals(caster.getUUID());
	}

	public static boolean redirect(ServerPlayer caster, Mob mob, LivingEntity target, int duration) {
		return apply(caster, mob, Mode.REDIRECTED, duration, target.getUUID());
	}

	private static boolean controlled(CommandState state) {
        return state.mode == Mode.IMPRESSED || state.mode == Mode.MARIONETTE;
    }

    public static boolean available(ServerPlayer caster, LivingEntity target) {
        CommandState existing = COMMANDS.get(target.getUUID());
        return canCommand(target) && ManipulationCombatHelper.canHarm(caster, target)
                && (existing == null || existing.caster.equals(caster.getUUID()))
                && !(target instanceof net.minecraft.world.entity.OwnableEntity ownable && ownable.getOwnerUUID() != null)
                && !(target instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon bound
                    && bound.hemomancy$getOwnerUUID() != null)
                && !(target.getVehicle() instanceof com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity)
                && !target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_binding);
    }

    public static Mob marionette(ServerPlayer caster) {
        UUID id = IMPRESSED_BY_CASTER.get(caster.getUUID());
        CommandState state = id == null ? null : COMMANDS.get(id);
        return state != null && state.mode == Mode.MARIONETTE ? BODIES.get(id) : null;
    }

    public static boolean isMarionetteChannel(ServerPlayer caster) {
        return marionette(caster) != null;
    }

    public static boolean acquireMarionette(ServerPlayer caster, LivingEntity target) {
        if (!available(caster, target) || caster.distanceToSqr(target) > 64) return false;
        UUID previous = IMPRESSED_BY_CASTER.get(caster.getUUID());
        Mob previousBody = previous == null ? null : BODIES.get(previous);
        if (previousBody != null) release(previousBody);
        return apply(caster, target, Mode.MARIONETTE, SanguineMarionetteRules.MAX_TICKS, null);
    }

    public static boolean maintainMarionette(ServerPlayer caster) {
        Mob mob = marionette(caster);
        if (mob == null || !mob.isAlive() || !caster.isAlive() || mob.level() != caster.level()) return false;
        CommandState state = COMMANDS.get(mob.getUUID());
        return SanguineMarionetteRules.maintain(caster.level().getGameTime(),
                state.until - SanguineMarionetteRules.MAX_TICKS, caster.distanceTo(mob));
    }

    public static void releaseMarionette(UUID caster) {
        UUID id = IMPRESSED_BY_CASTER.get(caster);
        CommandState state = id == null ? null : COMMANDS.get(id);
        if (state != null && state.mode == Mode.MARIONETTE) {
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
        CommandState state = COMMANDS.get(mob.getUUID());
        return state != null && state.mode == Mode.MARIONETTE ? ORDERS.get(mob.getUUID()) : null;
    }

    private static LivingEntity orderedEnemy(Mob mob) {
        Order order = marionetteOrder(mob);
        if (order == null || order.enemy == null || !(mob.level() instanceof ServerLevel level)) return null;
        Entity enemy = level.getEntity(order.enemy);
        return enemy instanceof LivingEntity living && living.isAlive() && mayAffect(mob, living) && mob.canAttack(living)
                ? living : null;
    }

    public static void maintainBrainOrder(Mob mob) {
        CommandState state = COMMANDS.get(mob.getUUID());
        if (state == null || state.mode != Mode.MARIONETTE) return;
        LivingEntity enemy = orderedEnemy(mob);
        mob.setTarget(enemy);
        mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, java.util.Optional.ofNullable(enemy));
        Order order = marionetteOrder(mob);
        if (order != null && order.ground != null && mob.position().distanceToSqr(order.ground) > 1)
            mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(order.ground, 1.1f, 1));
    }

    /** Only order memories are reserved; species combat, cooldowns and locomotion remain native. */
    public static boolean mayChangeBrainMemory(Mob mob, MemoryModuleType<?> type, Object value) {
        CommandState state = COMMANDS.get(mob.getUUID());
        if (state == null || state.mode != Mode.MARIONETTE) return true;
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
        CommandState state = COMMANDS.get(mob.getUUID());
        if (state != null && state.mode == Mode.MARIONETTE) event.setNewAboutToBeSetTarget(orderedEnemy(mob));
    }

    private static boolean apply(ServerPlayer caster, LivingEntity target, Mode mode, int duration, UUID redirectedTarget) {
		if (!(target instanceof Mob mob) || !ManipulationCombatHelper.canHarm(caster, target)
				|| (mode == Mode.REDIRECTED ? ManipulationReactiveEvents.isBoss(target) : !canCommand(target))) return false;
		CommandState existing = COMMANDS.get(target.getUUID());
        if (existing != null && !existing.caster.equals(caster.getUUID())) return false;
        boolean targetDisabled = existing != null ? existing.targetDisabled
                : ((com.vincenthuto.hemomancy.mixin.core.GoalSelectorAccessor)mob.targetSelector)
                    .hemomancy$disabledFlags().contains(Goal.Flag.TARGET);
        CommandState old = COMMANDS.put(target.getUUID(), new CommandState(caster.getUUID(),
				caster.level().dimension(), caster.level().getGameTime() + duration, mode, redirectedTarget, targetDisabled));
        BODIES.put(mob.getUUID(), mob);
        if (mode == Mode.MARIONETTE && !MOVEMENT.containsKey(mob.getUUID())) {
            var goal = new MarionetteMoveGoal(mob);
            MOVEMENT.put(mob.getUUID(), goal);
            mob.goalSelector.addGoal(0, goal);
        }
		if (old != null && controlled(old)) IMPRESSED_BY_CASTER.remove(old.caster, target.getUUID());
		if (mode == Mode.IMPRESSED || mode == Mode.MARIONETTE) IMPRESSED_BY_CASTER.put(caster.getUUID(), target.getUUID());
		mob.setTarget(null);
		mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
		if (mode == Mode.REDIRECTED) {
			Entity redirected = caster.serverLevel().getEntity(redirectedTarget);
			mob.setTarget(redirected instanceof LivingEntity living ? living : null);
		} else feedback(caster.serverLevel(), mob, mode);
        visual(caster,mob,COMMANDS.get(mob.getUUID()));
		return true;
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || COMMANDS.isEmpty()) return;
		long now = level.getGameTime();
		for (Map.Entry<UUID, CommandState> entry : java.util.List.copyOf(COMMANDS.entrySet())) {
			CommandState state = entry.getValue();
			if (!state.dimension.equals(level.dimension())) continue;
			Entity entity = level.getEntity(entry.getKey());
			ServerPlayer caster = level.getPlayerByUUID(state.caster) instanceof ServerPlayer player ? player : null;
			if (!(entity instanceof Mob mob) || !mob.isAlive() || caster == null || now >= state.until
                    || state.mode == Mode.MARIONETTE && !maintainMarionette(caster)) {
				if (BODIES.get(entry.getKey()) instanceof Mob body) release(body);
				if (controlled(state)) IMPRESSED_BY_CASTER.remove(state.caster, entry.getKey());
				COMMANDS.remove(entry.getKey());
                if (state.mode == Mode.MARIONETTE && caster != null) ManipulationChannelManager.stop(caster, false);
				continue;
			}
	            if(now%5==0) visual(caster,mob,state);
			if (state.mode == Mode.REBUKED) tickRebuke(mob, caster, now);
			else if (state.mode == Mode.REDIRECTED) {
				Entity redirected = level.getEntity(state.redirectedTarget);
				mob.setTarget(redirected instanceof LivingEntity living && ManipulationCombatHelper.canHarm(caster, living) ? living : null);
			} else if (state.mode == Mode.MARIONETTE) {
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
		CommandState defenderState = COMMANDS.get(event.getEntity().getUUID());
		if (defenderState != null && controlled(defenderState)
				&& alliedToCaster(event.getEntity().level(), defenderState.caster, livingAttacker)) {
			event.setCanceled(true);
			return;
		}
		CommandState attackerState = COMMANDS.get(livingAttacker.getUUID());
		if (attackerState != null && controlled(attackerState)
				&& alliedToCaster(event.getEntity().level(), attackerState.caster, event.getEntity())) {
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
        CommandState state = COMMANDS.get(source.getUUID());
        return state != null && controlled(state) ? state.caster : null;
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
            var state = COMMANDS.get(mob.getUUID());
            var order = ORDERS.get(mob.getUUID());
            return state != null && state.mode == Mode.MARIONETTE && order != null && order.ground != null;
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

    private static void visual(ServerPlayer caster, Mob mob, CommandState state) {
        if (state.mode == Mode.MARIONETTE) {
            int ticks = (int)Math.min(10, state.until - caster.level().getGameTime());
            var tether = new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(
                    ManipulationVisuals.Form.MARIONETTE_TETHER, mob.getId(), mob.position(), caster.getEyePosition(),
                    mob.getBbHeight(), ticks, caster.getId());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(mob, tether);
            Order order = ORDERS.get(mob.getUUID());
            if (order != null) {
                Entity enemy = order.enemy == null ? null : mob.level() instanceof ServerLevel level ? level.getEntity(order.enemy) : null;
                Vec3 destination = enemy == null ? order.ground : enemy.position();
                if (destination != null) net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(mob,
                        new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(
                                ManipulationVisuals.Form.MARIONETTE_ORDER, mob.getId(), mob.position(), destination,
                                1, ticks, enemy == null ? -1 : enemy.getId()));
            }
            return;
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(caster.serverLevel(),null,
                mob.getX(),mob.getY(),mob.getZ(),64,
                new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(
                        ManipulationVisuals.Form.COMMAND,mob.getId(),mob.position(),caster.getEyePosition(),
                        mob.getBbHeight(),(int)Math.min(10,state.until-caster.level().getGameTime()),state.mode.ordinal()+1));
    }

	private static void release(Mob mob) {
        CommandState old = COMMANDS.remove(mob.getUUID());
        if (old != null && controlled(old)) IMPRESSED_BY_CASTER.remove(old.caster, mob.getUUID());
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
        ManipulationVisuals.attached(mob,ManipulationVisuals.Form.MARIONETTE_TETHER,0,0,0);
        ManipulationVisuals.attached(mob,ManipulationVisuals.Form.MARIONETTE_ORDER,0,0,0);
		mob.setTarget(null);
		mob.targetSelector.setControlFlag(Goal.Flag.TARGET, old == null || !old.targetDisabled);
	}

    @SubscribeEvent
    public static void tracking(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking event) {
        CommandState state = COMMANDS.get(event.getTarget().getUUID());
        if (state != null && event.getTarget() instanceof Mob mob
                && mob.level().getPlayerByUUID(state.caster) instanceof ServerPlayer caster) visual(caster, mob, state);
    }

    @SubscribeEvent
    public static void unloaded(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && COMMANDS.containsKey(mob.getUUID())) {
            CommandState state = COMMANDS.get(mob.getUUID());
            release(mob);
            if (state.mode == Mode.MARIONETTE && event.getLevel() instanceof ServerLevel level
                    && level.getPlayerByUUID(state.caster) instanceof ServerPlayer caster)
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

	public static void clearSessionState() {
        for (Mob mob : java.util.List.copyOf(BODIES.values())) release(mob);
		COMMANDS.clear();
		IMPRESSED_BY_CASTER.clear();
	}

	private enum Mode { REBUKED, IMPRESSED, REDIRECTED, MARIONETTE }

	private record CommandState(UUID caster, ResourceKey<Level> dimension, long until, Mode mode, UUID redirectedTarget, boolean targetDisabled) {
	}
}
