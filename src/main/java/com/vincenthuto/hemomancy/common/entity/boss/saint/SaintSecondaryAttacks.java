package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One committed secondary cast per Saint. Ground warnings and hits keep the same snapshot. */
final class SaintSecondaryAttacks {
    private SaintAttackPattern move;
    private Vec3 origin, target;
    private int remaining, cooldown=140, next;
    private final Set<UUID> struck=new HashSet<>();

    void tick(SaintTrialEntity boss, boolean allowed) {
        if(!(boss.level() instanceof ServerLevel server))return;
        if(!allowed || !boss.isAlive() || boss.hasTrialEnded()) {
            cancel(boss);cooldown=80;return;
        }
        if(move==null) {
            if(--cooldown>0)return;
            var victim=server.getEntitiesOfClass(Player.class,boss.getBoundingBox().inflate(14),
                    p->ManipulationCombatHelper.canHarmFor(boss,p) && boss.distanceToSqr(p)<=196 && boss.hasLineOfSight(p))
                    .stream().min(Comparator.comparingDouble(boss::distanceToSqr)).orElse(null);
            if(victim==null){cooldown=20;return;}
            move=select(boss.saintType(),next%2);
            origin=boss.position();target=victim.position();remaining=move.duration();struck.clear();
            boss.secondaryState(move.id(),remaining);
            boss.getNavigation().stop();boss.getLookControl().setLookAt(victim,90,90);
            victim.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.hemomancy.saint_pattern."+move.name().toLowerCase(java.util.Locale.ROOT)),true);
            ManipulationVisuals.attached(boss,form(boss.saintType()),0,0,0);
            ManipulationVisuals.attached(boss,form(boss.saintType()),target,move.radius(),move.duration(),move.cue());
            server.playSound(null,origin.x,origin.y,origin.z,SoundEvents.BEACON_ACTIVATE,SoundSource.HOSTILE,.8F,.55F+move.id()*.08F);
            return;
        }
        int age=move.duration()- --remaining;
        boss.secondaryState(move.id(),remaining);
        if(move.damageTick(age)) {
            if(move.pulsed())struck.clear();
            Vec3 aim=target.subtract(origin);
            for(Player player:server.getEntitiesOfClass(Player.class,new AABB(origin,origin).inflate(24),
                    p->ManipulationCombatHelper.canHarmFor(boss,p) && boss.hasLineOfSight(p))) {
                Vec3 relative=player.position().subtract(origin);
                double floor=(move==SaintAttackPattern.REGROWTH_KNOT || move==SaintAttackPattern.SPORE_GARDEN
                        || move==SaintAttackPattern.SHADOW_CROSS)?target.y:origin.y;
                double height=move==SaintAttackPattern.MARROW_RUPTURE || move==SaintAttackPattern.FROZEN_ADVANCE?.9:2.5;
                if(Math.abs(player.getY()-floor)>height || struck.contains(player.getUUID())
                        || !move.hits(relative.x,relative.z,aim.x,aim.z,age))continue;
                struck.add(player.getUUID());
                apply(boss,player,move);
            }
            if(age==move.windup())server.playSound(null,origin.x,origin.y,origin.z,SoundEvents.BEACON_DEACTIVATE,SoundSource.HOSTILE,1,.7F);
        }
        if(remaining<=0){next++;cancel(boss);cooldown=100;}
    }

    void cancel(SaintTrialEntity boss) {
        if(move!=null)ManipulationVisuals.attached(boss,form(boss.saintType()),0,0,0);
        move=null;struck.clear();boss.secondaryState(0,0);
    }
    private static SaintAttackPattern select(EnumSaintType saint,int index) {
        return switch(saint) {
            case HEMORATH -> index==0?SaintAttackPattern.IRON_RAKE:SaintAttackPattern.MARROW_RUPTURE;
            case SERAPHAE -> index==0?SaintAttackPattern.NERVE_LANCE:SaintAttackPattern.REGROWTH_KNOT;
            case PUTRICIEL -> index==0?SaintAttackPattern.SPORE_GARDEN:SaintAttackPattern.CENSER_SWEEP;
            case VELORUM -> index==0?SaintAttackPattern.FROZEN_ADVANCE:SaintAttackPattern.SHADOW_CROSS;
        };
    }
    private static ManipulationVisuals.Form form(EnumSaintType saint) {
        return switch(saint) {
            case HEMORATH -> ManipulationVisuals.Form.HEMORATH_RELIQUARY;
            case SERAPHAE -> ManipulationVisuals.Form.SERAPHAE_RELIQUARY;
            case PUTRICIEL -> ManipulationVisuals.Form.PUTRICIEL_OFFERING;
            case VELORUM -> ManipulationVisuals.Form.VELORUM_RIME;
        };
    }
    private static void apply(SaintTrialEntity boss,Player player,SaintAttackPattern move) {
        if(move==SaintAttackPattern.REGROWTH_KNOT && player.getHealth()<player.getMaxHealth())player.heal(1);
        else player.hurt(boss.damageSources().indirectMagic(boss,boss),move.damage());
        switch(move) {
            case IRON_RAKE, MARROW_RUPTURE -> player.knockback(.45, boss.getX()-player.getX(),boss.getZ()-player.getZ());
            case NERVE_LANCE, REGROWTH_KNOT -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,0,false,false,true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,15,2,false,false,true));
            }
            case SPORE_GARDEN, CENSER_SWEEP -> player.addEffect(new MobEffectInstance(MobEffects.WITHER,40,0,false,false,true));
            case FROZEN_ADVANCE -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,2,false,false,true));
            case SHADOW_CROSS -> player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,40,0,false,false,true));
        }
    }
}
