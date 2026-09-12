package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.ManipulationInterruptedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;

public final class SchoolStates {
    private SchoolStates() {}
    public static SchoolCombatState data(LivingEntity target) { return HemoCapabilityAccess.getSchoolCombat(target); }
    public static boolean has(LivingEntity target, SchoolState state) {
        if (!target.level().isClientSide) return target.hasEffect(state.effect());
        var entry = data(target).get(state);
        return entry != null && entry.expires > target.level().getGameTime();
    }
    public static int levels(LivingEntity target, SchoolState state) {
        if (target.level().isClientSide) {
            var entry = data(target).get(state);
            return has(target, state) && entry != null ? entry.levels : 0;
        }
        MobEffectInstance effect = target.getEffect(state.effect());
        return effect == null ? 0 : effect.getAmplifier() + 1;
    }

    public static boolean canApply(@Nullable LivingEntity caster, LivingEntity target, SchoolState state) {
        if (target.level().isClientSide || !target.isAlive() || target.isSpectator()) return false;
        if (state != SchoolState.VEILED && caster != null && caster != target) {
            if (caster instanceof Player player && !ManipulationCombatHelper.canHarm(player, target)) return false;
            if (caster.isAlliedTo(target) || target.isAlliedTo(caster)) return false;
        }
        if (state == SchoolState.PRESSURE && HemoEntityPredicates.NOBLOOD.test(target)) return false;
        return state != SchoolState.DISRUPTED || !ManipulationReactiveEvents.isBoss(target);
    }

    public static boolean apply(LivingEntity caster, LivingEntity target, SchoolState state, int duration) {
        return apply(caster, target, state, duration, 1);
    }

    public static boolean apply(@Nullable LivingEntity caster, LivingEntity target, SchoolState state, int duration, int levels) {
        SchoolHitContext hit = SchoolDamage.current();
        if (hit == null) hit = SchoolHitContext.direct(Hemomancy.rloc(state.name().toLowerCase(java.util.Locale.ROOT)),
                school(state), null, caster);
        return apply(hit, caster, target, state, duration, levels);
    }

    public static EnumBloodTendency school(SchoolState state) {
        return switch (state) {
            case PRESSURE -> EnumBloodTendency.ANIMUS;
            case NECROSIS -> EnumBloodTendency.MORTEM;
            case LODESTONE -> EnumBloodTendency.FERRIC;
            case SEARING -> EnumBloodTendency.FLAMMEUS;
            case RIME -> EnumBloodTendency.CONGEATIO;
            case DISRUPTED -> EnumBloodTendency.DUCTILIS;
            case ILLUMINATED -> EnumBloodTendency.LUX;
            case OBSCURED, VEILED -> EnumBloodTendency.TENEBRIS;
        };
    }

    public static boolean apply(SchoolHitContext hit, @Nullable LivingEntity caster, LivingEntity target,
                                SchoolState state, int duration, int added) {
        if (!canApply(caster, target, state)) return false;
        // A new hostile cast breaks concealment; unattended zone refreshes do not.
        if (caster != null && caster != target && state != SchoolState.VEILED
                && hit.kind() == SchoolHitContext.Kind.DIRECT && hit.equals(SchoolDamage.current()))
            clear(caster, SchoolState.VEILED);
        if (state == SchoolState.SEARING) {
            int rime = levels(target, SchoolState.RIME);
            if (rime > 0) {
                setLevels(target, SchoolState.RIME, SchoolStateRules.rimeAfterHeat(rime));
                return true;
            }
            if (!SchoolStateRules.mayIgnite(0, target.isInWaterOrRain(),
                    target.fireImmune() || target.hasEffect(MobEffects.FIRE_RESISTANCE))) return false;
        } else if (state == SchoolState.RIME && has(target, SchoolState.SEARING)) {
            clear(target, SchoolState.SEARING);
            return true;
        }
        SchoolCombatState data = data(target);
        SchoolCombatState.Entry old = data.get(state);
        long now = target.level().getGameTime();
        data.observe(now);
        int ticks = state == SchoolState.DISRUPTED ? 10 : duration > 0 ? duration : state.duration;
        SchoolCombatState.Entry entry = old == null ? new SchoolCombatState.Entry(hit, now, ticks) : old;
        int count = old == null ? 0 : old.levels;
        int next = switch (state) {
            case PRESSURE -> SchoolStateRules.pressure(count, false);
            case RIME -> hit.ability().getPath().equals("rimebound_sentence") && hit.charge() >= .99f
                    ? 3 : SchoolStateRules.rime(count, added, now, entry.lastBuild);
            default -> 1;
        };
        if (state == SchoolState.RIME && next == count && old != null) {
            entry.expires = Math.max(entry.expires, now + ticks);
            target.addEffect(new MobEffectInstance(state.effect(), ticks, entry.levels - 1, false, false, true), caster);
            data.changed();
            return true;
        }
        if (!target.canBeAffected(new MobEffectInstance(state.effect(), ticks, next - 1))) return false;
        if (!target.addEffect(new MobEffectInstance(state.effect(), ticks, next - 1, false, false, true), caster)
                && !target.hasEffect(state.effect())) return false;
        entry.levels = next;
        entry.expires = Math.max(entry.expires, now + ticks);
        entry.origin = hit;
        if (state == SchoolState.RIME) entry.lastBuild = now;
        data.put(state, entry);
        if (state == SchoolState.DISRUPTED) interrupt(target);
        return true;
    }

    public static void interrupt(LivingEntity target) {
        target.stopUsingItem();
        if (target instanceof net.minecraft.world.entity.monster.Creeper creeper && !creeper.isIgnited()) creeper.setSwellDir(-1);
        if (target instanceof ServerPlayer player) {
            ManipulationChannelManager.stop(player, false);
            if (target.getEffect(EffectInit.disrupted) != null
                    && target.getEffect(EffectInit.disrupted).getDuration() == SchoolState.DISRUPTED.duration)
                PacketHandler.sendToPlayer(player, ManipulationInterruptedPacket.INSTANCE);
        }
    }

    public static void applyPrimary(SchoolHitContext hit, @Nullable LivingEntity caster, LivingEntity target) {
        if (hit.primary() == EnumBloodTendency.DUCTILIS && (hit.ability().getPath().startsWith("living_") || hit.ability().getPath().equals("will_melee"))) {
            SchoolHitHelper.markConductive(target, 160);
            return;
        }
        SchoolState state = primaryState(hit.primary());
        apply(hit, caster, target, state, hit.duration(), hit.levels());
    }

    public static SchoolState primaryState(EnumBloodTendency tendency) {
        return switch (tendency) {
            case ANIMUS -> SchoolState.PRESSURE;
            case MORTEM -> SchoolState.NECROSIS;
            case FERRIC -> SchoolState.LODESTONE;
            case FLAMMEUS -> SchoolState.SEARING;
            case CONGEATIO -> SchoolState.RIME;
            case DUCTILIS -> SchoolState.DISRUPTED;
            case LUX -> SchoolState.ILLUMINATED;
            case TENEBRIS -> SchoolState.OBSCURED;
        };
    }

    public static void refresh(LivingEntity target, SchoolState state, int ticks) {
        var entry = data(target).get(state);
        if (entry == null || ticks <= 0) return;
        entry.expires = Math.max(entry.expires, target.level().getGameTime() + ticks);
        target.addEffect(new MobEffectInstance(state.effect(), ticks, entry.levels - 1, false, false, true));
        data(target).changed();
    }

    public static void clear(LivingEntity target, SchoolState state) {
        if (target.removeEffect(state.effect()) || !target.hasEffect(state.effect())) data(target).remove(state);
    }

    public static void setLevels(LivingEntity target, SchoolState state, int levels) {
        SchoolCombatState.Entry entry = data(target).get(state);
        if (entry == null || levels <= 0) { clear(target, state); return; }
        int duration = (int)Math.max(1, entry.expires - target.level().getGameTime());
        // Replacing the effect is necessary when reducing an amplifier.
        if (target.hasEffect(state.effect()) && !target.removeEffect(state.effect())) return;
        if (!target.addEffect(new MobEffectInstance(state.effect(), duration, levels - 1, false, false, true))) return;
        entry.levels = levels;
        data(target).put(state, entry);
    }

    public static void removed(LivingEntity target, MobEffectInstance effect) {
        for (SchoolState state : SchoolState.values()) {
            if (effect.is(state.effect())) { data(target).remove(state); return; }
        }
    }
}
