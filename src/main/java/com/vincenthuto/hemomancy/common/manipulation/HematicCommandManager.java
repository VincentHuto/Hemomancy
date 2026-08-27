package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
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
	private static final Map<UUID, UUID> IMPRESSED_BY_CASTER = new HashMap<>();

	private HematicCommandManager() {
	}

	public static boolean rebuke(ServerPlayer caster, LivingEntity target) {
		return apply(caster, target, Mode.REBUKED, HematicCommandRules.REBUKE_DURATION_TICKS);
	}

	public static boolean impress(ServerPlayer caster, LivingEntity target) {
		if (!canCommand(target)) return false;
		UUID previousId = IMPRESSED_BY_CASTER.get(caster.getUUID());
		if (previousId != null) {
			if (caster.serverLevel().getEntity(previousId) instanceof Mob previous) release(previous);
			COMMANDS.remove(previousId);
		}
		return apply(caster, target, Mode.IMPRESSED,
				HematicCommandRules.impressmentDurationTicks(target.getMaxHealth()));
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
		return state != null && state.mode == Mode.IMPRESSED && state.caster.equals(caster.getUUID());
	}

	private static boolean apply(ServerPlayer caster, LivingEntity target, Mode mode, int duration) {
		if (!canCommand(target) || !(target instanceof Mob mob)) return false;
		CommandState old = COMMANDS.put(target.getUUID(), new CommandState(caster.getUUID(),
				caster.level().dimension(), caster.level().getGameTime() + duration, mode));
		if (old != null && old.mode == Mode.IMPRESSED) IMPRESSED_BY_CASTER.remove(old.caster, target.getUUID());
		if (mode == Mode.IMPRESSED) IMPRESSED_BY_CASTER.put(caster.getUUID(), target.getUUID());
		mob.setTarget(null);
		mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
		feedback(caster.serverLevel(), mob, mode);
		return true;
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || COMMANDS.isEmpty()) return;
		long now = level.getGameTime();
		Iterator<Map.Entry<UUID, CommandState>> iterator = COMMANDS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, CommandState> entry = iterator.next();
			CommandState state = entry.getValue();
			if (!state.dimension.equals(level.dimension())) continue;
			Entity entity = level.getEntity(entry.getKey());
			ServerPlayer caster = level.getServer().getPlayerList().getPlayer(state.caster);
			if (!(entity instanceof Mob mob) || !mob.isAlive() || caster == null || now >= state.until) {
				if (entity instanceof Mob mob) release(mob);
				if (state.mode == Mode.IMPRESSED) IMPRESSED_BY_CASTER.remove(state.caster, entry.getKey());
				iterator.remove();
				continue;
			}
			if (state.mode == Mode.REBUKED) tickRebuke(mob, caster, now);
			else tickImpressed(mob, caster);
		}
	}

	@SubscribeEvent
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		Entity attacker = event.getSource().getEntity();
		if (!(attacker instanceof LivingEntity livingAttacker)) return;
		CommandState defenderState = COMMANDS.get(event.getEntity().getUUID());
		if (defenderState != null && defenderState.mode == Mode.IMPRESSED
				&& alliedToCaster(event.getEntity().level(), defenderState.caster, livingAttacker)) {
			event.setAmount(0.0F);
			return;
		}
		CommandState attackerState = COMMANDS.get(livingAttacker.getUUID());
		if (attackerState != null && attackerState.mode == Mode.IMPRESSED
				&& alliedToCaster(event.getEntity().level(), attackerState.caster, event.getEntity())) {
			event.setAmount(0.0F);
		}
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
		ServerPlayer caster = serverLevel.getServer().getPlayerList().getPlayer(casterId);
		return caster != null && (entity == caster || caster.isAlliedTo(entity) || entity.isAlliedTo(caster));
	}

	private static void release(Mob mob) {
		mob.setTarget(null);
		mob.targetSelector.enableControlFlag(Goal.Flag.TARGET);
	}

	private static void feedback(ServerLevel level, Mob target, Mode mode) {
		level.sendParticles(mode == Mode.REBUKED ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.WITCH,
				target.getX(), target.getY() + target.getBbHeight() * 0.6D,
				target.getZ(), 28, 0.45D, 0.55D, 0.45D, 0.03D);
		level.playSound(null, target.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS,
				0.7F, mode == Mode.REBUKED ? 0.7F : 1.15F);
	}

	public static void clearSessionState() {
		COMMANDS.clear();
		IMPRESSED_BY_CASTER.clear();
	}

	private enum Mode { REBUKED, IMPRESSED }

	private record CommandState(UUID caster, ResourceKey<Level> dimension, long until, Mode mode) {
	}
}
