package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.SyncSchoolCombatS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SchoolCombatEvents {
    private record Prepared(SchoolHitContext hit, SchoolState consumed, boolean hadNecrosis,
                            boolean hadLodestone, boolean hadConductor, boolean payoff) {}
    private record Impact(long tick, SchoolHitContext hit) {}
    private static final Map<DamageSource, Map<UUID, Prepared>> PENDING = new WeakHashMap<>();
    private static final Map<LivingEntity, Impact> MAGNETIC_IMPACTS = new WeakHashMap<>();
    private SchoolCombatEvents() {}

    @SubscribeEvent public static void launch(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide) {
            if (!event.loadedFromDisk() && event.getEntity() instanceof Projectile projectile
                    && projectile.getOwner() instanceof LivingEntity owner) {
                if (com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(owner)) {
                    event.setCanceled(true);
                    return;
                }
                SchoolStates.clear(owner, SchoolState.VEILED);
            }
            SchoolDamage.captureSpawn(event.getEntity());
            if (event.getEntity() instanceof LivingEntity living) SchoolStates.data(living).restore(living);
        }
    }

    @SubscribeEvent public static void attack(AttackEntityEvent event) {
        SchoolDamage.captureSwing(event.getEntity());
        SchoolStates.clear(event.getEntity(), SchoolState.VEILED);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void prepare(LivingIncomingDamageEvent event) {
        if (event.getAmount() <= 0 || event.getEntity().level().isClientSide) return;
        LivingEntity target = event.getEntity();
        MAGNETIC_IMPACTS.remove(target);
        SchoolHitContext hit = event.getSource() instanceof SchoolDamageSource source ? source.context() : null;
        SchoolState consumed = null;
        boolean payoff = false;
        float damage = event.getAmount();
        float stateBonus = 0;
        if (hit != null) {
            Entity owner = event.getSource().getEntity();
            if (owner instanceof Player player && target != player
                    && !com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarm(player, target)) {
                event.setCanceled(true);
                return;
            }
            if (hit.kind() == SchoolHitContext.Kind.DIRECT && owner instanceof LivingEntity caster)
                SchoolStates.clear(caster, SchoolState.VEILED);
            SchoolCombatState data = SchoolStates.data(target);
            if (hit.kind() == SchoolHitContext.Kind.REACTION && data.hasPaid(hit.rootAttack())) {
                event.setCanceled(true);
                return;
            }
            if (hit.kind() == SchoolHitContext.Kind.DIRECT && !data.hasPaid(hit.rootAttack())) {
                String ability = hit.ability().getPath();
                float bonus = 0;
                int pressure = SchoolStates.levels(target, SchoolState.PRESSURE);
                if ((ability.equals("blood_aneurysm")
                        || ability.equals("living_blade") && hit.charge() >= 0.99f && pressure == 3) && pressure > 0) {
                    bonus = SchoolStateRules.ruptureDamage(pressure);
                    consumed = SchoolState.PRESSURE;
                } else if (ability.equals("funeral_bell") && SchoolStates.has(target, SchoolState.NECROSIS)) {
                    var debt = data.get(SchoolState.NECROSIS);
                    bonus = debt == null ? 0 : debt.stored * hit.charge();
                    consumed = SchoolState.NECROSIS;
                } else if (ability.equals("vitric_combustion") && SchoolStates.has(target, SchoolState.SEARING)) {
                    var heat = data.get(SchoolState.SEARING);
                    bonus = heat == null ? 0 : SchoolStateRules.combustionDamage(heat.expires, heat.nextTick, target.level().getGameTime());
                    consumed = SchoolState.SEARING;
                } else if (ability.equals("prismatic_reproof") && SchoolStates.has(target, SchoolState.ILLUMINATED)) {
                    damage *= 2;
                    consumed = SchoolState.ILLUMINATED;
                } else if (ability.equals("white_verdict") && (SchoolStates.has(target, SchoolState.ILLUMINATED)
                        || SchoolStates.has(target, SchoolState.VEILED) || target.isInvisible())) {
                    damage *= 1.5f;
                    if (SchoolStates.has(target, SchoolState.ILLUMINATED)) consumed = SchoolState.ILLUMINATED;
                    payoff = true;
                }
                payoff |= consumed != null;
                if (!payoff && hit.primary() == EnumBloodTendency.FERRIC
                        && SchoolStates.levels(target, SchoolState.RIME) == 3) {
                    bonus = 4;
                    consumed = SchoolState.RIME;
                    payoff = true;
                }
                stateBonus = bonus;
            }
            boolean fixedPayoff = event.getSource().is(HemoDamageTypes.SEARING)
                    || event.getSource().is(HemoDamageTypes.DECAY_RELEASE)
                    || hit.kind() == SchoolHitContext.Kind.REACTION
                        && (hit.ability().getPath().equals("conductive_arc") || hit.ability().getPath().equals("grave_debt"));
            if (!fixedPayoff && owner instanceof Player player) {
                damage *= TendencyAffinityRules.damageMultiplier(player, target, hit.primary(), hit.secondary());
            }
            damage += stateBonus;
            if (hit.ability().getPath().equals("osseous_bloom")
                    && com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.isBoss(target)) damage = Math.min(12, damage);
            event.setAmount(damage);
        }
        PENDING.computeIfAbsent(event.getSource(), ignored -> new HashMap<>()).put(target.getUUID(),
                new Prepared(hit, consumed, SchoolStates.has(target, SchoolState.NECROSIS),
                        SchoolStates.has(target, SchoolState.LODESTONE),
                        target.hasEffect(EffectInit.conductive_mark) || target.hasEffect(EffectInit.lodestone), payoff));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void discardCanceled(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) {
            var pending = PENDING.get(event.getSource());
            if (pending != null) pending.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void commit(LivingDamageEvent.Post event) {
        var pending = PENDING.get(event.getSource());
        Prepared prepared = pending == null ? null : pending.remove(event.getEntity().getUUID());
        if (prepared == null || !SchoolDamage.hasHealthOrAbsorptionDamage(event)) return;
        LivingEntity target = event.getEntity();
        SchoolHitContext hit = prepared.hit;
        SchoolCombatState data = SchoolStates.data(target);
        if (prepared.hadNecrosis && prepared.consumed != SchoolState.NECROSIS) {
            var debt = data.get(SchoolState.NECROSIS);
            boolean direct = hit != null ? hit.kind() == SchoolHitContext.Kind.DIRECT
                    : event.getSource().getEntity() instanceof LivingEntity;
            if (debt != null) {
                debt.stored = SchoolStateRules.necrosisDebt(debt.stored, event.getNewDamage(), direct);
                data.changed();
            }
        }
        if (hit == null) return;
        if (hit.kind() == SchoolHitContext.Kind.REACTION) {
            data.recordPayoff(hit.rootAttack(), target.level().getGameTime());
            return;
        }
        if (hit.kind() != SchoolHitContext.Kind.DIRECT) return;
        long now = target.level().getGameTime();
        LivingEntity caster = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        if (prepared.payoff && !data.hasPaid(hit.rootAttack())) {
            data.recordPayoff(hit.rootAttack(), now);
            if (prepared.consumed != null) SchoolStates.clear(target, prepared.consumed);
            if (target.level() instanceof ServerLevel level) {
                var form = prepared.consumed == SchoolState.RIME ? ManipulationVisuals.Form.CRUOR_BREAK
                        : prepared.consumed == SchoolState.NECROSIS ? ManipulationVisuals.Form.MORTEM_BURST
                        : prepared.consumed == SchoolState.SEARING ? ManipulationVisuals.Form.IGNITION
                        : prepared.consumed == SchoolState.ILLUMINATED ? ManipulationVisuals.Form.FLARE
                        : ManipulationVisuals.Form.ANIMUS_IMPACT;
                ManipulationVisuals.burst(level, form, target.position(), target.position(), 1, 16);
            }
        }
        if (!data.hasApplied(hit.rootAttack())) {
            data.recordApplication(hit.rootAttack(), now);
            if (caster != target && hit.role() == SchoolHitContext.Role.APPLY_STATE
                    && (prepared.consumed == null || prepared.consumed == SchoolState.RIME))
            {
                SchoolStates.applyPrimary(hit, caster, target);
                data.recordApplication(hit.rootAttack(), SchoolStates.primaryState(hit.primary()), now);
            }
        }
        if (prepared.hadLodestone && hit.primary() == EnumBloodTendency.FERRIC)
            MAGNETIC_IMPACTS.put(target, new Impact(now, hit));
        if (caster != null && prepared.hadConductor && !data.hasPaid(hit.rootAttack())) {
            Runnable arc = () -> {
                if (data.hasPaid(hit.rootAttack())) return;
                try (var scope = SchoolDamage.scope(hit, caster)) {
                    if (SchoolHitHelper.tryTriggerConductiveArc(caster, target, hit.primary(), hit.secondary(), event.getNewDamage()))
                        data.recordPayoff(hit.rootAttack(), now);
                }
            };
            try (var scope = SchoolDamage.scope(hit, caster)) {
                var discharge = com.vincenthuto.hemomancy.common.manipulation.ductilis.Discharge.current();
                if (discharge == null || !discharge.enqueueReaction(arc)) arc.run();
            }
        }
    }

    @SubscribeEvent public static void knockback(LivingKnockBackEvent event) {
        Impact impact = MAGNETIC_IMPACTS.get(event.getEntity());
        if (impact != null && impact.tick == event.getEntity().level().getGameTime()) {
            event.setStrength(event.getStrength() * 1.25f);
            var target = event.getEntity();
            target.level().getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.summon.EntityIronPillar.class,
                    target.getBoundingBox().inflate(8), pillar -> pillar.attracts(target, impact.hit.owner()))
                    .stream().min(java.util.Comparator.comparingDouble(target::distanceToSqr)).ifPresent(pillar -> {
                        event.setRatioX(target.getX() - pillar.getX());
                        event.setRatioZ(target.getZ() - pillar.getZ());
                    });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void expired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() == null || !event.getEffectInstance().is(EffectInit.necrosis)
                || event.getEntity().level().isClientSide) return;
        var debt = SchoolStates.data(event.getEntity()).remove(SchoolState.NECROSIS);
        if (debt != null && debt.stored > 0 && event.getEntity().isAlive()) {
            event.getEntity().hurt(HemoDamageTypes.stateDamage(event.getEntity().level(), HemoDamageTypes.DECAY_RELEASE,
                    debt.origin.newAttack().child(SchoolHitContext.Kind.REACTION), SchoolDamage.owner(event.getEntity(), debt.origin)), debt.stored);
        }
    }

    @SubscribeEvent public static void tick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity target) || target.level().isClientSide) return;
        var data = SchoolStates.data(target);
        data.observe(target.level().getGameTime());
        var heat = data.get(SchoolState.SEARING);
        if (heat != null) {
            if (!target.isAlive() || target.isInWaterOrRain() || target.fireImmune() || target.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                SchoolStates.clear(target, SchoolState.SEARING);
            } else if (target.level().getGameTime() >= heat.nextTick) {
                heat.nextTick += 20;
                data.changed();
                target.hurt(HemoDamageTypes.stateDamage(target.level(), HemoDamageTypes.SEARING,
                        heat.origin.child(SchoolHitContext.Kind.PERIODIC), SchoolDamage.owner(target, heat.origin)), 1);
            }
        }
        if (SchoolStates.has(target, SchoolState.DISRUPTED)) SchoolStates.interrupt(target);
        if (data.takeDirty()) PacketDistributor.sendToPlayersTrackingEntityAndSelf(target,
                new SyncSchoolCombatS2CPacket(target.getId(), data.serializeNBT(target.registryAccess())));
    }

    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity target)
            PacketDistributor.sendToPlayer(player, new SyncSchoolCombatS2CPacket(target.getId(),
                    SchoolStates.data(target).serializeNBT(target.registryAccess())));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) public static void death(LivingDeathEvent event) {
        for (SchoolState state : SchoolState.values()) SchoolStates.clear(event.getEntity(), state);
        MAGNETIC_IMPACTS.remove(event.getEntity());
    }
}
