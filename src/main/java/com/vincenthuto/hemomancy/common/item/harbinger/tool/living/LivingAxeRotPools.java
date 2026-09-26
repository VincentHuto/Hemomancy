package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolDamageSource;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingAxeRotPools {
	private record CriticalStamp(UUID target, long tick) { }
	private static final Map<UUID, CriticalStamp> CRITICALS = new HashMap<>();
	private static final ArrayList<Pool> POOLS = new ArrayList<>();

	private LivingAxeRotPools() { }

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void critical(CriticalHitEvent event) {
		Player player = event.getEntity();
		if (player.level().isClientSide || !event.isVanillaCritical() || !event.isCriticalHit()
				|| !(player.getMainHandItem().getItem() instanceof LivingAxeItem)) return;
		CRITICALS.put(player.getUUID(), new CriticalStamp(event.getTarget().getUUID(),
				player.level().getGameTime()));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void damage(LivingDamageEvent.Post event) {
		if (!(event.getSource() instanceof SchoolDamageSource source)
				|| !source.context().ability().getPath().equals("living_axe")
				|| !(event.getSource().getEntity() instanceof Player player)
				|| !(player.level() instanceof ServerLevel level)
				|| !SchoolDamage.hasHealthOrAbsorptionDamage(event)) return;
		CriticalStamp critical = CRITICALS.remove(player.getUUID());
		if (critical == null || critical.tick != level.getGameTime()
				|| !critical.target.equals(event.getEntity().getUUID())) return;
		Vec3 feet = event.getEntity().position();
		var ground = level.clip(new ClipContext(feet.add(0, .1, 0),
				new Vec3(feet.x, level.getMinBuildHeight(), feet.z),
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, event.getEntity()));
		if (ground.getType() == HitResult.Type.BLOCK) createOrRefresh(level, player, ground.getLocation());
	}

	private static void createOrRefresh(ServerLevel level, Player owner, Vec3 center) {
		long now = level.getGameTime();
		Pool merged = null;
		Iterator<Pool> pools = POOLS.iterator();
		while (pools.hasNext()) {
			Pool pool = pools.next();
			if (pool.level == level && pool.owner.equals(owner.getUUID())
					&& Math.abs(pool.center.y - center.y) <= 1.25D
					&& LivingAxeRotRules.overlaps(pool.center.x, pool.center.z, center.x, center.z)) {
				if (merged == null) merged = pool;
				else {
					merged.nextPulseAt = Math.min(merged.nextPulseAt, pool.nextPulseAt);
					merged.expiresAt = Math.max(merged.expiresAt, pool.expiresAt);
					pools.remove();
				}
			}
		}
		if (merged == null) {
			POOLS.add(new Pool(level, owner.getUUID(), center, now, now + LivingAxeRotRules.LIFETIME_TICKS));
		} else {
			merged.center = center;
			merged.expiresAt = LivingAxeRotRules.mergedExpiry(now, merged.expiresAt);
		}
		show(level, center);
	}

	private static void show(ServerLevel level, Vec3 center) {
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.BLOOM, center, center,
				LivingAxeRotRules.RADIUS, LivingAxeRotRules.LIFETIME_TICKS);
	}

	@SubscribeEvent
	public static void tick(ServerTickEvent.Post event) {
		Iterator<Pool> iterator = POOLS.iterator();
		while (iterator.hasNext()) {
			Pool pool = iterator.next();
			long now = pool.level.getGameTime();
			if (now >= pool.expiresAt || !pool.level.hasChunkAt(net.minecraft.core.BlockPos.containing(pool.center))) {
				iterator.remove();
				continue;
			}
			if (now >= pool.nextPulseAt) {
				pool.pulse();
				pool.nextPulseAt = now + LivingAxeRotRules.PULSE_INTERVAL_TICKS;
			}
		}
		CRITICALS.clear();
	}

	@SubscribeEvent
	public static void stopped(ServerStoppedEvent event) {
		CRITICALS.clear();
		POOLS.clear();
	}

	private static final class Pool {
		private final ServerLevel level;
		private final UUID owner;
		private Vec3 center;
		private long nextPulseAt;
		private long expiresAt;

		private Pool(ServerLevel level, UUID owner, Vec3 center, long createdAt, long expiresAt) {
			this.level = level;
			this.owner = owner;
			this.center = center;
			this.nextPulseAt = createdAt;
			this.expiresAt = expiresAt;
		}

		private void pulse() {
			if (!(level.getEntity(owner) instanceof LivingEntity attacker) || !attacker.isAlive()) return;
			AABB bounds = new AABB(center, center).inflate(LivingAxeRotRules.RADIUS, 1.25D,
					LivingAxeRotRules.RADIUS);
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds,
					this::canHarm)) {
				var damage = SchoolDamage.reaction(attacker, "living_axe_rot_pool", EnumBloodTendency.MORTEM);
				target.hurt(damage, LivingAxeRotRules.DAMAGE_PER_PULSE);
				SchoolStates.apply(attacker, target, SchoolState.NECROSIS, LivingAxeRotRules.NECROSIS_TICKS);
			}
		}

		private boolean canHarm(LivingEntity target) {
			if (!target.isAlive() || !LivingAxeRotRules.contains(center.x, center.z, target.getX(), target.getZ())) {
				return false;
			}
			LivingEntity attacker = level.getEntity(owner) instanceof LivingEntity living ? living : null;
			if (attacker == null || target == attacker || attacker.isAlliedTo(target) || target.isAlliedTo(attacker)) {
				return false;
			}
			return !(attacker instanceof Player player) || ManipulationCombatHelper.canHarm(player, target);
		}
	}
}
