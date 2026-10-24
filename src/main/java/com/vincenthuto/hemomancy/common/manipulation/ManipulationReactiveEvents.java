package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.damage.*;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ferric.IronheartedEvents;
import com.vincenthuto.hemomancy.common.manipulation.mortem.BlackheartedEvents;
import com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncManipulationHearts;
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
	/** The pool a ward had left when it lapsed, keyed to the tick it lapsed, so a quick recast cannot refill it. */
	private static final Map<UUID, Ward> WARD_REMNANTS = new HashMap<>();
	/** Game tick of a melee swing that broke a Penumbral Drift, so the blow it started still lands as an ambush. */
	private static final Map<UUID, Long> PENUMBRAL_AMBUSH = new HashMap<>();
	private static final Map<UUID, UnclosingEye> UNCLOSING_EYES = new HashMap<>();
	/** Session tier: each player's passive trigger cooldowns by manipulation name, cleared at respawn and logout. */
	private static final Map<UUID, Map<String, Long>> PASSIVE_COOLDOWNS = new HashMap<>();
	private static final List<EclipseWell> ECLIPSE_WELLS = new ArrayList<>();
	private static final List<GazeSlam> GAZE_SLAMS = new ArrayList<>();
	private static final List<HematicBeacon> HEMATIC_BEACONS = new ArrayList<>();

	private ManipulationReactiveEvents() {
	}

	public static void clearSessionState() {
		CORONATIONS.clear();
		CIRCUIT_HITS.clear();
		SANGUINE_WARDS.clear();
		WARD_REMNANTS.clear();
		PENUMBRAL_AMBUSH.clear();
		UNCLOSING_EYES.clear();
		PASSIVE_COOLDOWNS.clear();
		ECLIPSE_WELLS.clear();
		GAZE_SLAMS.clear();
		HEMATIC_BEACONS.clear();
	}

	public static void armCoronation(Player player, int lances, float strength) {
		CORONATIONS.put(player.getUUID(), new Coronation(lances, strength,
				player.level().getGameTime() + 400, SchoolDamage.context(ManipulationInit.crimson_coronation.get(), player)));
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.CROWN, 1.15, 400, lances);
	}

	public static void armLivingCircuit(Player player) {
		CIRCUIT_HITS.put(player.getUUID(), player.level().getGameTime() + 40);
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.CIRCUIT, 1, 40, 1);
	}

    public static boolean isEclipseDarkness(Level level, net.minecraft.core.BlockPos pos) {
        return ECLIPSE_WELLS.stream().anyMatch(well -> well.dimension.equals(level.dimension())
                && level.getGameTime() < well.until && Vec3.atCenterOf(pos).distanceToSqr(well.center) <= well.radius * well.radius);
    }

    /** A well without its pulse bite or collapse, as the visual reviews place it. */
    public static void createEclipseWell(ServerLevel level, Vec3 center, double radius, int duration, UUID owner) {
        createEclipseWell(level, center, radius, duration, owner, 0, false);
    }

    /**
     * @param pulseDamage damage each {@link com.vincenthuto.hemomancy.common.manipulation.tenebris.EclipseWellManip#PULSE_TICKS}
     *                    pulse deals to Obscured bodies inside, already scaled by the caster's Crimson Mastery
     * @param collapses   whether the well scatters the enemies at its heart when it lapses (a full-charge cast)
     */
    public static void createEclipseWell(ServerLevel level, Vec3 center, double radius, int duration, UUID owner,
            float pulseDamage, boolean collapses) {
		long now = level.getGameTime();
		ECLIPSE_WELLS.add(new EclipseWell(level.dimension(), center, radius, now, now + duration, owner,
                SchoolDamage.context(ManipulationInit.eclipse_well.get(), level.getPlayerByUUID(owner)),
                pulseDamage, collapses));
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.WELL, center, center, radius, duration);
	}

	public static void createHematicBeacon(ServerLevel level, Vec3 center, double radius, int duration, UUID owner) {
		HematicBeacon beacon = new HematicBeacon(level.dimension(), center, radius,
				level.getGameTime() + duration, level.getGameTime() + 20, owner,
                SchoolDamage.context(ManipulationInit.hematic_beacon.get(), level.getPlayerByUUID(owner)));
		HEMATIC_BEACONS.add(beacon);
		pulseHematicBeacon(level, beacon);
	}

	public static final float PENUMBRAL_AMBUSH_MULTIPLIER = 1.5F;
	public static final float SANGUINE_WARD_POOL = 6.0F;
	public static final float SANGUINE_WARD_REFILL = 1.0F;
	/** A ward recast within this many ticks of lapsing resumes its old pool instead of a fresh one. */
	public static final int SANGUINE_WARD_RESUME_TICKS = 100;

	public static void refreshSanguineWard(Player player) {
		long now = player.level().getGameTime();
		Ward current = SANGUINE_WARDS.get(player.getUUID());
		if (current != null && current.until < now) {
			WARD_REMNANTS.put(player.getUUID(), current);
			current = null;
		}
		if (current == null) {
			Ward remnant = WARD_REMNANTS.remove(player.getUUID());
			if (remnant != null && now - remnant.until <= SANGUINE_WARD_RESUME_TICKS) current = remnant;
		}
		float pool = current == null ? SANGUINE_WARD_POOL : Math.min(SANGUINE_WARD_POOL, current.pool + SANGUINE_WARD_REFILL);
		SANGUINE_WARDS.put(player.getUUID(), new Ward(pool, now + 25));
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.WARD, 1, 25, (int)Math.ceil(pool));
	}

	/** Remaining Sanguine Ward pool, or 0 when no ward is up. */
	public static float sanguineWardPool(Player player) {
		Ward ward = SANGUINE_WARDS.get(player.getUUID());
		return ward == null || ward.until < player.level().getGameTime() ? 0 : ward.pool;
	}

	public static void armUnclosingEye(Player player, int duration) {
		UNCLOSING_EYES.put(player.getUUID(), new UnclosingEye(player.level().getGameTime() + duration,
                SchoolDamage.context(ManipulationInit.unclosing_eye.get(), player)));
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.EYE, 1, duration, 1);
	}

	public static void scheduleDeadlyGazeSlam(ServerLevel level, Player owner, LivingEntity target, int delay, float damage) {
		GAZE_SLAMS.add(new GazeSlam(level.dimension(), owner.getUUID(), target.getUUID(), level.getGameTime() + delay, damage,
                SchoolDamage.context(ManipulationInit.deadly_gaze.get(), owner)));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
		if (EndlessHourManip.isSettlement(event.getSource())) {
			EndlessHourManip.waiveMitigation(event);
			return;
		}
		long now = player.level().getGameTime();
		Entity attacker = event.getSource().getEntity();
		Ward ward = SANGUINE_WARDS.get(player.getUUID());
		if (ward != null) {
			if (now > ward.until) WARD_REMNANTS.put(player.getUUID(), SANGUINE_WARDS.remove(player.getUUID()));
			else {
				float absorbed = Math.min(ward.pool, event.getAmount());
				event.setAmount(event.getAmount() - absorbed);
				SANGUINE_WARDS.put(player.getUUID(), new Ward(ward.pool - absorbed, ward.until));
                ManipulationVisuals.attached(player, ManipulationVisuals.Form.WARD, 1,
                        (int)(ward.until-now), (int)Math.ceil(ward.pool-absorbed));
                if(absorbed>0) ManipulationVisuals.burst(player.serverLevel(),ManipulationVisuals.Form.NERVE_HIT,
                        player.position().add(0,1,0),player.position(),.6,10);
                // The ward is a charged nerve sheath: whoever strikes it directly is jolted and briefly stalled.
                if (absorbed > 0 && attacker instanceof LivingEntity striker && striker != player
                        && event.getSource().getDirectEntity() == striker
                        && ManipulationCombatHelper.canHarm(player, striker)) {
                    com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects.synapticJolt(player, striker);
                    com.vincenthuto.hemomancy.common.manipulation.ductilis.SynapticJoltManip.staggerTarget(striker);
                }
			}
		}


		if (active(player, "sovereign_instinct") && attacker != null) sovereignInstinct(player);
	}

	/**
	 * The hearts answer last, after every ward and multiplier: Blackhearted first turns wither into saturation,
	 * then Ironhearted spends its hearts on whatever is left. A Blackhearted rupture strikes past both.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onHeartDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0F
				|| BlackheartedEvents.isRupturing(player)) return;
		var state = HemoCapabilityAccess.getPowerGuardrails(player);
		float saturation = state.getNecroticSaturation();
		long refractoryUntil = state.getBlackheartedCooldownUntil();
		float damage = BlackheartedEvents.metabolize(player, event.getSource(), event.getAmount());
		boolean changed = saturation != state.getNecroticSaturation()
				|| refractoryUntil != state.getBlackheartedCooldownUntil() || state.getIronHeartHealth() > 0.0F;
		damage = IronheartedEvents.absorb(player, damage);
		event.setAmount(damage);
		if (changed) syncHearts(player);
	}

	/** Owner sync for the Ironhearted and Blackhearted state, with their attached visuals. */
	public static void syncHearts(ServerPlayer player) {
		var state = HemoCapabilityAccess.getPowerGuardrails(player);
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.IRON_HEART, state.getIronHeartHealth(),
				state.getIronHeartHealth() > 0
						? (int) Math.max(0, state.getIronHeartExpiryTick() - player.level().getGameTime()) : 0, 1);
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.BLACK_HEART, state.getNecroticSaturation(),
				state.getNecroticSaturation() > 0 ? 30 : 0, 1);
		PacketHandler.sendToPlayer(player, new PacketSyncManipulationHearts(state));
	}

    @SubscribeEvent
    public static void coronationLanded(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !SchoolDamage.hasHealthOrAbsorptionDamage(event)
                || event.getSource() instanceof SchoolDamageSource school && school.context().kind() != SchoolHitContext.Kind.DIRECT) return;
        long now = player.level().getGameTime();
        Entity attacker = event.getSource().getEntity();
		Coronation crown = CORONATIONS.get(player.getUUID());
		if (crown != null && now < crown.until && crown.lances > 0 && attacker instanceof LivingEntity living
				&& ManipulationCombatHelper.canHarm(player, living)) {
			BloodNeedleEntity needle = new BloodNeedleEntity(player.level(), player);
			needle.setDamageTendency(EnumBloodTendency.ANIMUS);
            SchoolDamage.capture(needle, crown.hit.newAttack().child(SchoolHitContext.Kind.REACTION));
            needle.setCoronationSword(true);
            Vec3 launch = player.position().add(ManipulationVisuals.swordOffset(now, crown.lances - 1));
            needle.setPos(launch);
			needle.setBaseDamage((1.0D + 2.0D * crown.strength)
					* com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(player));
			if (crown.strength >= 1.0F) needle.configurePiercing((byte) 1);
			Vec3 direction = living.getEyePosition().subtract(needle.position()).normalize();
			needle.shoot(direction.x, direction.y, direction.z, 2.4F + crown.strength, 1.0F);
			player.level().addFreshEntity(needle);
            ManipulationVisuals.attached(player, ManipulationVisuals.Form.CROWN, 1.15,
                    (int) (crown.until - now), crown.lances - 1);
			if (crown.lances == 1) CORONATIONS.remove(player.getUUID());
			else CORONATIONS.put(player.getUUID(), new Coronation(crown.lances - 1, crown.strength, crown.until, crown.hit));
		}

    }

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onFinalDamage(LivingDamageEvent.Pre event) {
		EndlessHourManip.deferDamage(event);
		if (event.getSource().getEntity() instanceof ServerPlayer attacker && event.getEntity() != attacker
				&& selected(attacker, "penumbral_drift")) {
			// Breaking cover from the drift lands as an ambush: the first blow bites deeper and leaves the victim Obscured.
			// A melee swing ends the drift before its damage arrives, so the swing's tick is remembered.
			Long swing = PENUMBRAL_AMBUSH.remove(attacker.getUUID());
			if (ManipulationChannelManager.isChanneling(attacker.getUUID())
					|| swing != null && swing == attacker.level().getGameTime()) {
				event.setNewDamage(event.getNewDamage() * PENUMBRAL_AMBUSH_MULTIPLIER);
				com.vincenthuto.hemomancy.common.damage.SchoolStates.apply(attacker, event.getEntity(),
						com.vincenthuto.hemomancy.common.damage.SchoolState.OBSCURED, 80);
			}
			ManipulationChannelManager.stop(attacker);
		}
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		// Endless Hour settlement waives absorption, so only health stands between the debt and death.
		float buffer = player.getHealth() + (EndlessHourManip.isSettlement(event.getSource()) ? 0 : player.getAbsorptionAmount());
		if (event.getNewDamage() < buffer || !phoenixDebtReady(player)
				|| !trigger(player, ManipulationInit.phoenix_debt.get(), 6000)) return;
		LastRiteHelper.consume(player, LastRiteHelper.PHOENIX_DEBT_ID);
		event.setNewDamage(0);
		player.setHealth(1.0F);
		player.getActiveEffects().stream()
				.filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
				.map(MobEffectInstance::getEffect).toList().forEach(player::removeEffect);
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true));
		if (player.level() instanceof ServerLevel level) {
                var hit = com.vincenthuto.hemomancy.common.damage.SchoolHitContext.direct(
                        com.vincenthuto.hemomancy.Hemomancy.rloc("phoenix_debt"), EnumBloodTendency.FLAMMEUS,
                        ManipulationInit.phoenix_debt.get().getSecondaryTend(), player)
                        .child(com.vincenthuto.hemomancy.common.damage.SchoolHitContext.Kind.REACTION);
            float eruption = 8.0F * (float) com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(player);
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5),
					candidate -> ManipulationCombatHelper.canHarm(player, candidate))) {
                target.hurt(com.vincenthuto.hemomancy.common.damage.SchoolDamage.attributed(
                        level.damageSources().inFire(), hit, player), eruption);
			}
			// Attached so the wings follow the body that paid for them; radius is the blast's reach.
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.PHOENIX, 5, 48, 1);
		}
	}

	@SubscribeEvent
	public static void onAttack(AttackEntityEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (!selected(player, "penumbral_drift")) return;
		if (ManipulationChannelManager.isChanneling(player.getUUID()))
			PENUMBRAL_AMBUSH.put(player.getUUID(), player.level().getGameTime());
		ManipulationChannelManager.stop(player);
	}

    @SubscribeEvent
    public static void circuitLanded(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player
                || !event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
                || event.getSource() instanceof SchoolDamageSource school && school.context().kind() != SchoolHitContext.Kind.DIRECT
                || !SchoolDamage.hasHealthOrAbsorptionDamage(event)
                || !ManipulationCombatHelper.canHarm(player, event.getEntity())) return;
        Long armedUntil = CIRCUIT_HITS.remove(player.getUUID());
        if (armedUntil == null || player.level().getGameTime() > armedUntil) return;
        LivingEntity target = event.getEntity();
        SchoolHitHelper.markConductive(target, 160);
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.CIRCUIT, 0, 0, 0);
        ManipulationVisuals.attached(target, ManipulationVisuals.Form.MARK, 1, 160, 1);
        com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects.conductiveArc(player, target, 0);
    }

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		CrimsonTitheManip.tickDebt(player);
        if (player.tickCount % 20 == 0) {
            var data = player.getPersistentData();
            long now=player.level().getGameTime();
            if(active(player,"sovereign_instinct") && passiveCooldown(player,"sovereign_instinct")<=now)
                ManipulationVisuals.attached(player,ManipulationVisuals.Form.COMMAND,1.8,25,3);
            if(phoenixDebtReady(player)
                    && passiveCooldown(player,"phoenix_debt")<=now)
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
		UnclosingEye eye = UNCLOSING_EYES.get(player.getUUID());
		if (eye == null || eye.until <= player.level().getGameTime()) UNCLOSING_EYES.remove(player.getUUID());
		else if (player.tickCount % 10 == 0) {
			for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class,
					player.getBoundingBox().inflate(32), entity -> entity != player && entity.isAlive())) {
				SchoolStates.apply(eye.hit, player, target, SchoolState.ILLUMINATED, 25, 1);
			}
			SchoolStates.apply(eye.hit, player, player, SchoolState.ILLUMINATED, 25, 1);
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
                ManipulationVisuals.burst(level,ManipulationVisuals.Form.GAZE_SLAM,living.position(),living.position(),
                        Math.max(1,living.getBbWidth()*1.6),16);
				living.hurt(SchoolDamage.attributed(level.damageSources().magic(), slam.hit, owner), slam.damage);
			}
		}
		Iterator<EclipseWell> iterator = ECLIPSE_WELLS.iterator();
		while (iterator.hasNext()) {
			EclipseWell well = iterator.next();
			if (!well.dimension.equals(level.dimension())) continue;
			if (now >= well.until) {
				iterator.remove();
				if (well.collapses) collapseEclipseWell(level, well);
				continue;
			}
			boolean pulse = well.pulseDamage > 0 && now > well.start
					&& (now - well.start) % com.vincenthuto.hemomancy.common.manipulation.tenebris.EclipseWellManip.PULSE_TICKS == 0;
			if (now % 5 != 0 && !pulse) continue;
			Player owner = level.getPlayerByUUID(well.owner);
			// Each pulse is an unattended zone tick, like a Blood Cloud's: damage only (the well itself keeps bodies
			// Obscured) and no concealment break for the caster.
			SchoolHitContext bite = pulse ? well.hit.newAttack().child(SchoolHitContext.Kind.PERIODIC) : null;
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
					new AABB(well.center, well.center).inflate(well.radius),
					candidate -> candidate.isAlive() && candidate != owner && (owner != null && ManipulationCombatHelper.canHarm(owner, candidate)))) {
				if (pulse && SchoolStates.has(target, SchoolState.OBSCURED))
					ManipulationParticles.hurt(ManipulationInit.eclipse_well.get(), target,
							SchoolDamage.attributed(level.damageSources().magic(), bite, owner), well.pulseDamage);
				if (now % 5 != 0 || !target.isAlive()) continue;
				Vec3 pull = well.center.subtract(target.position());
				if (pull.lengthSqr() > .01) target.push(pull.x * .04, Math.max(0, pull.y) * .02, pull.z * .04);
				SchoolStates.apply(well.hit, owner, target, SchoolState.OBSCURED, 25, 1);
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

	/** Called by {@code TransientServerStateEvents} when the player logs out. */
	public static void logout(Player player) {
		EndlessHourManip.settleDebt(player);
		clear(player);
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
		Vec3 chest = player.position().add(0, player.getBbHeight() * .6, 0);
		for (int i = 0; i < mobs.size(); i++) {
			Mob mob = mobs.get(i);
			boolean boss = isBoss(mob);
			// Bodies too large to command shrug the instinct off; bosses only feel its pressure.
			if (!boss && !HematicCommandManager.canCommand(mob)) continue;
			// The instinct is seen: a lash of sinew from the sovereign to every body it turns.
			if (player.level() instanceof ServerLevel level)
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.SOVEREIGN_LASH, chest,
						mob.position().add(0, mob.getBbHeight() * .8, 0), 1, 14);
			if (boss) {
                SchoolStates.apply(SchoolDamage.context(ManipulationInit.sovereign_instinct.get(), player),
                        player, mob, SchoolState.PRESSURE, 100, 1);
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
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && ManipulationCombatHelper.canHarm(owner, entity))) {
			SchoolStates.apply(beacon.hit, owner, target, SchoolState.ILLUMINATED, 30, 1);
		}
        ManipulationParticles.accent(level, EnumBloodTendency.LUX, beacon.center.add(0, 1.35, 0), Vec3.ZERO);
	}

	/** Phoenix Debt only promises a rebirth the caster can pay for at its live price. */
	public static boolean phoenixDebtReady(ServerPlayer player) {
		BloodManipulation phoenix = ManipulationInit.phoenix_debt.get();
		if (!active(player, "phoenix_debt") || !LastRiteHelper.canFire(player, LastRiteHelper.PHOENIX_DEBT_ID)
				|| VascularSystemEvents.isSectionDead(player, phoenix.getSection())) return false;
		double price = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger
				.collect(player, phoenix).effectiveCost();
		return price >= 0 && HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() >= price;
	}

	private static boolean trigger(ServerPlayer player, BloodManipulation manipulation, int cooldown) {
		long now = player.level().getGameTime();
		if (passiveCooldown(player, manipulation.getName()) > now || !manipulation.tryPerformPassiveTrigger(player)) return false;
		PASSIVE_COOLDOWNS.computeIfAbsent(player.getUUID(), id -> new HashMap<>()).put(manipulation.getName(), now + cooldown);
		return true;
	}

	private static long passiveCooldown(ServerPlayer player, String name) {
		Map<String, Long> cooldowns = PASSIVE_COOLDOWNS.get(player.getUUID());
		return cooldowns == null ? 0L : cooldowns.getOrDefault(name, 0L);
	}

	private static boolean active(ServerPlayer player, String name) {
		BloodManipulation manipulation = ManipulationInit.getByName(name);
		return manipulation != null && manipulation.isPassiveReady(player);
	}

	private static boolean selected(ServerPlayer player, String name) {
		return HemoCapabilityAccess.getKnownManipulations(player).getSelectedManip() != null && name.equals(HemoCapabilityAccess.getKnownManipulations(player).getSelectedManip().getName());
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
		WARD_REMNANTS.remove(player);
		PENUMBRAL_AMBUSH.remove(player);
		UNCLOSING_EYES.remove(player);
		PASSIVE_COOLDOWNS.remove(player);
		ECLIPSE_WELLS.removeIf(well -> well.owner.equals(player));
		GAZE_SLAMS.removeIf(slam -> slam.owner.equals(player));
		HEMATIC_BEACONS.removeIf(beacon -> beacon.owner.equals(player));
	}

	/** The one boss predicate every manipulation cap, command limit and immunity reads. */
	public static boolean isBoss(LivingEntity target) {
		return target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
				|| target instanceof net.minecraft.world.entity.boss.wither.WitherBoss
				|| target instanceof net.minecraft.world.entity.monster.warden.Warden
				|| target instanceof net.minecraft.world.entity.monster.ElderGuardian
				|| target.getType().is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES)
				|| target.getClass().getName().startsWith("com.vincenthuto.hemomancy.common.entity.boss.");
	}

	/** A crown still holding swords; recasting would only overwrite it. */
	public static boolean coronationActive(Player player) {
		Coronation crown = CORONATIONS.get(player.getUUID());
		return crown != null && crown.lances > 0 && player.level().getGameTime() < crown.until;
	}

	private record Coronation(int lances, float strength, long until, SchoolHitContext hit) { }
	private record Ward(float pool, long until) { }
    private record UnclosingEye(long until, SchoolHitContext hit) { }
	private record GazeSlam(ResourceKey<Level> dimension, UUID owner, UUID target, long at, float damage, SchoolHitContext hit) { }
	private record EclipseWell(ResourceKey<Level> dimension, Vec3 center, double radius, long start, long until, UUID owner,
			SchoolHitContext hit, float pulseDamage, boolean collapses) { }

	/** A full-charge well caving in: every non-boss enemy at its heart is flung elsewhere. */
	private static void collapseEclipseWell(ServerLevel level, EclipseWell well) {
		Player owner = level.getPlayerByUUID(well.owner);
		if (owner == null) return;
		double reach = com.vincenthuto.hemomancy.common.manipulation.tenebris.EclipseWellManip.COLLAPSE_RADIUS;
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
				new AABB(well.center, well.center).inflate(reach),
				candidate -> candidate.isAlive() && !isBoss(candidate)
						&& ManipulationCombatHelper.isHostileTarget(owner, candidate)
						&& candidate.getBoundingBox().distanceToSqr(well.center) <= reach * reach))
			com.vincenthuto.hemomancy.common.manipulation.tenebris.EclipseWellManip.scatter(level, target);
	}

	private static final class HematicBeacon {
		private final ResourceKey<Level> dimension;
		private final Vec3 center;
		private final double radius;
		private final long until;
		private final UUID owner;
		private long nextPulse;
        private final SchoolHitContext hit;

		private HematicBeacon(ResourceKey<Level> dimension, Vec3 center, double radius, long until,
				long nextPulse, UUID owner, SchoolHitContext hit) {
			this.dimension = dimension;
			this.center = center;
			this.radius = radius;
			this.until = until;
			this.nextPulse = nextPulse;
            this.hit = hit;
			this.owner = owner;
		}
	}
}
