package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ManipulationReactiveEvents {
	private static final Map<UUID, Coronation> CORONATIONS = new HashMap<>();
	private static final Map<UUID, Long> CIRCUIT_HITS = new HashMap<>();
	private static final Map<UUID, Ward> SANGUINE_WARDS = new HashMap<>();
	private static final Map<UUID, Long> UNCLOSING_EYES = new HashMap<>();
	private static final Map<String, Long> PASSIVE_COOLDOWNS = new HashMap<>();
	private static final List<EclipseWell> ECLIPSE_WELLS = new ArrayList<>();
	private static final List<GazeSlam> GAZE_SLAMS = new ArrayList<>();
	private static final List<HematicBeacon> HEMATIC_BEACONS = new ArrayList<>();

	private ManipulationReactiveEvents() {
	}

	public static void clearSessionState() {
		CORONATIONS.clear();
		CIRCUIT_HITS.clear();
		SANGUINE_WARDS.clear();
		UNCLOSING_EYES.clear();
		PASSIVE_COOLDOWNS.clear();
		ECLIPSE_WELLS.clear();
		GAZE_SLAMS.clear();
		HEMATIC_BEACONS.clear();
	}

	public static void armCoronation(Player player, int lances, float strength) {
		CORONATIONS.put(player.getUUID(), new Coronation(lances, strength,
				player.level().getGameTime() + 400));
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.CROWN, 1.15, 400, lances);
	}

	public static void armLivingCircuit(Player player) {
		CIRCUIT_HITS.put(player.getUUID(), player.level().getGameTime() + 40);
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.CIRCUIT, 1, 40, 1);
	}

	public static void createEclipseWell(ServerLevel level, Vec3 center, double radius, int duration, UUID owner) {
		ECLIPSE_WELLS.add(new EclipseWell(level.dimension(), center, radius, level.getGameTime() + duration, owner));
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.WELL, center, center, radius, duration);
	}

	public static void createHematicBeacon(ServerLevel level, Vec3 center, double radius, int duration, UUID owner) {
		HematicBeacon beacon = new HematicBeacon(level.dimension(), center, radius,
				level.getGameTime() + duration, level.getGameTime() + 20, owner);
		HEMATIC_BEACONS.add(beacon);
		pulseHematicBeacon(level, beacon);
	}

	public static void refreshSanguineWard(Player player) {
		long until = player.level().getGameTime() + 25;
		Ward current = SANGUINE_WARDS.get(player.getUUID());
		SANGUINE_WARDS.put(player.getUUID(), new Ward(current == null || current.until < player.level().getGameTime() ? 6.0F : Math.min(8.0F, current.pool + 2.0F), until));
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.WARD, 1, 25, (int)Math.ceil(SANGUINE_WARDS.get(player.getUUID()).pool));
	}

	public static void armUnclosingEye(Player player, int duration) {
		UNCLOSING_EYES.put(player.getUUID(), player.level().getGameTime() + duration);
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.EYE, 1, duration, 1);
	}

	public static void scheduleDeadlyGazeSlam(ServerLevel level, Player owner, LivingEntity target, int delay, float damage) {
		GAZE_SLAMS.add(new GazeSlam(level.dimension(), owner.getUUID(), target.getUUID(), level.getGameTime() + delay, damage));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
		long now = player.level().getGameTime();
		Entity attacker = event.getSource().getEntity();
		Ward ward = SANGUINE_WARDS.get(player.getUUID());
		if (ward != null) {
			if (now > ward.until) SANGUINE_WARDS.remove(player.getUUID());
			else {
				float absorbed = Math.min(ward.pool, event.getAmount());
				event.setAmount(event.getAmount() - absorbed);
				SANGUINE_WARDS.put(player.getUUID(), new Ward(ward.pool - absorbed, ward.until));
                ManipulationVisuals.attached(player, ManipulationVisuals.Form.WARD, 1,
                        (int)(ward.until-now), (int)Math.ceil(ward.pool-absorbed));
                if(absorbed>0) ManipulationVisuals.burst(player.serverLevel(),ManipulationVisuals.Form.NERVE_HIT,
                        player.position().add(0,1,0),player.position(),.6,10);
			}
		}

		Coronation crown = CORONATIONS.get(player.getUUID());
		if (crown != null && now < crown.until && crown.lances > 0 && attacker instanceof LivingEntity living
				&& ManipulationCombatHelper.canHarm(player, living)) {
			BloodNeedleEntity needle = new BloodNeedleEntity(player.level(), player);
			needle.setDamageTendency(EnumBloodTendency.ANIMUS);
            needle.setCoronationSword(true);
            Vec3 launch = player.position().add(ManipulationVisuals.swordOffset(now, crown.lances - 1));
            needle.setPos(launch);
			needle.setBaseDamage(1.0D + 2.0D * crown.strength);
			if (crown.strength >= 1.0F) needle.configurePiercing((byte) 1);
			Vec3 direction = living.getEyePosition().subtract(needle.position()).normalize();
			needle.shoot(direction.x, direction.y, direction.z, 2.4F + crown.strength, 1.0F);
			player.level().addFreshEntity(needle);
            ManipulationVisuals.attached(player, ManipulationVisuals.Form.CROWN, 1.15,
                    (int) (crown.until - now), crown.lances - 1);
			if (crown.lances == 1) CORONATIONS.remove(player.getUUID());
			else CORONATIONS.put(player.getUUID(), new Coronation(crown.lances - 1, crown.strength, crown.until));
		}

		if (active(player, "sovereign_instinct") && attacker != null) sovereignInstinct(player);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onFinalDamage(LivingDamageEvent.Pre event) {
		EndlessHourManip.deferDamage(event);
		if (event.getSource().getEntity() instanceof ServerPlayer attacker && event.getEntity() != attacker
				&& selected(attacker, "penumbral_drift")) ManipulationChannelManager.stop(attacker);
		if (!(event.getEntity() instanceof ServerPlayer player) || event.getNewDamage() < player.getHealth() + player.getAbsorptionAmount()
				|| !active(player, "phoenix_debt")
				|| !LastRiteHelper.canFire(player, LastRiteHelper.PHOENIX_DEBT_ID)
				|| !trigger(player, ManipulationInit.phoenix_debt.get(), 6000)) return;
		LastRiteHelper.consume(player, LastRiteHelper.PHOENIX_DEBT_ID);
		event.setNewDamage(0);
		player.setHealth(1.0F);
		player.getActiveEffects().stream()
				.filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
				.map(MobEffectInstance::getEffect).toList().forEach(player::removeEffect);
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true));
		if (player.level() instanceof ServerLevel level) {
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5),
					candidate -> ManipulationCombatHelper.canHarm(player, candidate))) {
				target.igniteForSeconds(5);
				target.hurt(level.damageSources().inFire(), 8.0F);
			}
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.PHOENIX, player.position(), player.position(), 5, 48);
		}
	}

	@SubscribeEvent
	public static void onAttack(AttackEntityEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Long armedUntil = event.getTarget() instanceof LivingEntity victim && ManipulationCombatHelper.canHarm(player, victim)
				? CIRCUIT_HITS.remove(player.getUUID()) : null;
		if (armedUntil != null && player.level().getGameTime() <= armedUntil
				&& event.getTarget() instanceof LivingEntity target) {
			target.addEffect(new MobEffectInstance(EffectInit.conductive_mark, 160, 0, false, true));
			SchoolHitHelper.markConductive(target, 160);
            ManipulationVisuals.attached(player, ManipulationVisuals.Form.CIRCUIT, 0, 0, 0);
            ManipulationVisuals.attached(target, ManipulationVisuals.Form.MARK, 1, 160, 1);
            com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects.conductiveArc(player,target,0);
		}
		if (selected(player, "penumbral_drift")) ManipulationChannelManager.stop(player);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		CrimsonTitheManip.tickDebt(player);
        if (player.tickCount % 20 == 0) {
            var data = player.getPersistentData();
            long now=player.level().getGameTime();
            if(active(player,"sovereign_instinct") && PASSIVE_COOLDOWNS.getOrDefault(player.getUUID()+":sovereign_instinct",0L)<=now)
                ManipulationVisuals.attached(player,ManipulationVisuals.Form.COMMAND,1.8,25,3);
            if(active(player,"phoenix_debt") && LastRiteHelper.canFire(player,LastRiteHelper.PHOENIX_DEBT_ID)
                    && PASSIVE_COOLDOWNS.getOrDefault(player.getUUID()+":phoenix_debt",0L)<=now)
                ManipulationVisuals.attached(player,ManipulationVisuals.Form.PHOENIX_READY,1,25,1);
            long tithe = data.getLong("hemomancy:crimson_tithe_expiry") - player.level().getGameTime();
            double reserve = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve.get(player);
            if (tithe > 0) ManipulationVisuals.attached(player, ManipulationVisuals.Form.DEBT,
                    Math.max(0, data.getDouble("hemomancy:crimson_tithe_stored") - reserve) * 2, (int)Math.max(0, tithe), (int)reserve);
            long hour = data.getLong("hemomancy:endless_hour_expiry") - player.level().getGameTime();
            if (hour > 0) ManipulationVisuals.attached(player, ManipulationVisuals.Form.HOUR, data.getFloat("hemomancy:endless_hour_deferred"), (int)Math.max(0, hour), 1);
        }
		EndlessHourManip.tickEndlessHour(player);
		if (active(player, "phoenix_debt")) {
			if (!LastRiteHelper.hasArmedSource(player)) LastRiteHelper.arm(player, LastRiteHelper.PHOENIX_DEBT_ID);
		} else LastRiteHelper.clearIfArmed(player, LastRiteHelper.PHOENIX_DEBT_ID);
		long eyeUntil = UNCLOSING_EYES.getOrDefault(player.getUUID(), 0L);
		if (eyeUntil <= player.level().getGameTime()) UNCLOSING_EYES.remove(player.getUUID());
		else if (player.tickCount % 10 == 0) {
			for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class,
					player.getBoundingBox().inflate(32), entity -> entity != player && entity.isAlive())) {
				target.removeEffect(MobEffects.INVISIBILITY);
				target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 25, 0, false, false));
			}
			player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 25, 0, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 25, 0, false, false, true));
		}
		Coronation crown = CORONATIONS.get(player.getUUID());
		if (crown != null) {
			if (player.level().getGameTime() >= crown.until) CORONATIONS.remove(player.getUUID());
			else if (player.tickCount % 20 == 0) {
                ManipulationVisuals.attached(player, ManipulationVisuals.Form.CROWN, 1.15,
                        (int) (crown.until - player.level().getGameTime()), crown.lances);
			}
		}
		if (player.tickCount % 10 != 0 || !ManipulationChannelManager.isChanneling(player.getUUID())
				|| !selected(player, "penumbral_drift")) return;
		for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(12),
				mob -> mob.getTarget() == player && !isBoss(mob))) mob.setTarget(null);
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		long now = level.getGameTime();
		Iterator<GazeSlam> slams = GAZE_SLAMS.iterator();
		while (slams.hasNext()) {
			GazeSlam slam = slams.next();
			if (!slam.dimension.equals(level.dimension()) || now < slam.at) continue;
			slams.remove();
			Entity target = level.getEntity(slam.target);
			Player owner = level.getPlayerByUUID(slam.owner);
			if (target instanceof LivingEntity living && owner != null && ManipulationCombatHelper.canHarm(owner, living)) {
				living.setDeltaMovement(living.getDeltaMovement().x, -1.5D, living.getDeltaMovement().z);
                ManipulationVisuals.burst(level,ManipulationVisuals.Form.SWORD_IMPACT,living.position(),living.position(),1,18);
				ManipulationCombatHelper.hurt(ManipulationInit.deadly_gaze.get(), owner, living, level, slam.damage);
			}
		}
		Iterator<EclipseWell> iterator = ECLIPSE_WELLS.iterator();
		while (iterator.hasNext()) {
			EclipseWell well = iterator.next();
			if (!well.dimension.equals(level.dimension())) continue;
			if (now >= well.until) {
				iterator.remove();
				continue;
			}
			if (now % 5 != 0) continue;
			Player owner = level.getPlayerByUUID(well.owner);
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
					new AABB(well.center, well.center).inflate(well.radius),
					candidate -> candidate.isAlive() && candidate != owner && (owner != null && ManipulationCombatHelper.canHarm(owner, candidate)))) {
				Vec3 pull = well.center.subtract(target.position());
				if (pull.lengthSqr() > .01) target.push(pull.x * .04, Math.max(0, pull.y) * .02, pull.z * .04);
				target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, false, true));
				target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 25, 0, false, true));
				if (target instanceof Mob mob) mob.setTarget(null);
			}
			if (now % 20 == 0) ManipulationVisuals.burst(level, ManipulationVisuals.Form.WELL, well.center, well.center, well.radius, (int) (well.until - now));
		}
		Iterator<HematicBeacon> beacons = HEMATIC_BEACONS.iterator();
		while (beacons.hasNext()) {
			HematicBeacon beacon = beacons.next();
			if (!beacon.dimension.equals(level.dimension())) continue;
			if (now >= beacon.until) {
				beacons.remove();
				continue;
			}
			if (now < beacon.nextPulse) continue;
			beacon.nextPulse = now + 20;
			pulseHematicBeacon(level, beacon);
		}
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		EndlessHourManip.settleDebt(event.getEntity());
		clear(event.getEntity());
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		clear(event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) EndlessHourManip.clearDebt(player);
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			EndlessHourManip.clearDebt(event.getOriginal());
			EndlessHourManip.clearDebt(event.getEntity());
		} else EndlessHourManip.transferDebt(event.getOriginal(), event.getEntity());
	}

	private static void sovereignInstinct(ServerPlayer player) {
		List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(10),
				mob -> ManipulationCombatHelper.canHarm(player, mob) && mob.getTarget() == player && !HemoEntityPredicates.NOBLOOD.test(mob));
		if (mobs.size() < 4 || !trigger(player, ManipulationInit.sovereign_instinct.get(), 600)) return;
		for (int i = 0; i < mobs.size(); i++) {
			Mob mob = mobs.get(i);
			if (isBoss(mob)) {
				mob.setTarget(null);
				mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true));
			} else {
				HematicCommandManager.redirect(player, mob, mobs.get((i + 1) % mobs.size()), 100);
			}
		}
	}

	private static void pulseHematicBeacon(ServerLevel level, HematicBeacon beacon) {
		Player owner = level.getPlayerByUUID(beacon.owner);
		if (owner == null) return;
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.BEACON, beacon.center, beacon.center, beacon.radius, (int) (beacon.until - level.getGameTime()));
        AABB area = new AABB(beacon.center, beacon.center).inflate(beacon.radius);
		for (Player ally : level.getEntitiesOfClass(Player.class, area,
				player -> player.isAlive() && ManipulationCombatHelper.allied(owner, player))) {
			ally.heal(0.4F);
			ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, false, true));
		}
		for (Mob mob : level.getEntitiesOfClass(Mob.class, area,
				entity -> entity.isAlive() && ManipulationCombatHelper.canHarm(owner, entity))) {
			mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, true));
		}
        ManipulationParticles.accent(level, EnumBloodTendency.LUX, beacon.center.add(0, 1.35, 0), Vec3.ZERO);
	}

	private static boolean trigger(ServerPlayer player, BloodManipulation manipulation, int cooldown) {
		String key = player.getUUID() + ":" + manipulation.getName();
		long now = player.level().getGameTime();
		if (PASSIVE_COOLDOWNS.getOrDefault(key, 0L) > now || !manipulation.tryPerformPassiveTrigger(player)) return false;
		PASSIVE_COOLDOWNS.put(key, now + cooldown);
		return true;
	}

	private static boolean active(ServerPlayer player, String name) {
		BloodManipulation manipulation = ManipulationInit.getByName(name);
		return manipulation != null && manipulation.isPassiveReady(player);
	}

	private static boolean selected(ServerPlayer player, String name) {
		return HemoCapabilityAccess.getKnownManipulations(player)
				.map(known -> known.getSelectedManip() != null && name.equals(known.getSelectedManip().getName()))
				.orElse(false);
	}

	private static void clear(Player entity) {
        UUID player = entity.getUUID();
        if (entity.level() instanceof ServerLevel current) {
            for (var well : ECLIPSE_WELLS) if (well.owner.equals(player)) {
                var level=current.getServer().getLevel(well.dimension);
                if(level!=null)ManipulationVisuals.burst(level,ManipulationVisuals.Form.WELL,well.center,well.center,0,0);
            }
            for (var beacon : HEMATIC_BEACONS) if (beacon.owner.equals(player)) {
                var level=current.getServer().getLevel(beacon.dimension);
                if(level!=null)ManipulationVisuals.burst(level,ManipulationVisuals.Form.BEACON,beacon.center,beacon.center,0,0);
            }
        }
		CORONATIONS.remove(player);
		CIRCUIT_HITS.remove(player);
		SANGUINE_WARDS.remove(player);
		UNCLOSING_EYES.remove(player);
		PASSIVE_COOLDOWNS.keySet().removeIf(key -> key.startsWith(player + ":"));
		ECLIPSE_WELLS.removeIf(well -> well.owner.equals(player));
		GAZE_SLAMS.removeIf(slam -> slam.owner.equals(player));
		HEMATIC_BEACONS.removeIf(beacon -> beacon.owner.equals(player));
	}

	public static boolean isBoss(LivingEntity target) {
		return target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
				|| target instanceof net.minecraft.world.entity.boss.wither.WitherBoss
				|| target.getClass().getName().startsWith("com.vincenthuto.hemomancy.common.entity.boss.");
	}

	private record Coronation(int lances, float strength, long until) { }
	private record Ward(float pool, long until) { }
	private record GazeSlam(ResourceKey<Level> dimension, UUID owner, UUID target, long at, float damage) { }
	private record EclipseWell(ResourceKey<Level> dimension, Vec3 center, double radius, long until, UUID owner) { }

	private static final class HematicBeacon {
		private final ResourceKey<Level> dimension;
		private final Vec3 center;
		private final double radius;
		private final long until;
		private final UUID owner;
		private long nextPulse;

		private HematicBeacon(ResourceKey<Level> dimension, Vec3 center, double radius, long until,
				long nextPulse, UUID owner) {
			this.dimension = dimension;
			this.center = center;
			this.radius = radius;
			this.until = until;
			this.nextPulse = nextPulse;
			this.owner = owner;
		}
	}
}
