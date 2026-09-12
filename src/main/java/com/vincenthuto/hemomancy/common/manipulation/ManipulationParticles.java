package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationAccentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ManipulationParticles {
    private ManipulationParticles() {}

    public static void activate(LivingEntity caster,BloodManipulation manipulation) {
        if (!manipulation.usesDefaultActivationParticles()) return;
        if(!(caster.level() instanceof ServerLevel level))return;
        Vec3 at=caster.position().add(0,caster.getBbHeight()*.55,0);
        send(level,new ManipulationAccentPacket(manipulation.getTend(),manipulation.getSecondaryTend(),
                at,Vec3.ZERO,caster.getId(),false));
    }

    public static boolean hurt(BloodManipulation manipulation,LivingEntity target,DamageSource source,float amount) {
        if (!(source instanceof com.vincenthuto.hemomancy.common.damage.SchoolDamageSource)) {
            LivingEntity caster = source.getEntity() instanceof LivingEntity living ? living : null;
            var hit = com.vincenthuto.hemomancy.common.damage.SchoolDamage.context(manipulation, caster);
            source = com.vincenthuto.hemomancy.common.damage.SchoolDamage.attributed(source, hit,
                    caster == null ? com.vincenthuto.hemomancy.common.damage.SchoolDamage.owner(target, hit) : caster);
        }
        boolean hurt=target.hurt(source,amount);
        if(hurt)impact(target,manipulation.getTend(),manipulation.getSecondaryTend(),
                source.getSourcePosition()==null?Vec3.ZERO:target.position().subtract(source.getSourcePosition()));
        return hurt;
    }

    public static boolean hurt(BloodManipulation manipulation, LivingEntity target, DamageSource source,
                               float amount, int duration, int levels) {
        var hit = com.vincenthuto.hemomancy.common.damage.SchoolDamage.context(manipulation,
                source.getEntity() instanceof LivingEntity living ? living : null).withApplication(duration, levels);
        return hurt(manipulation, target, com.vincenthuto.hemomancy.common.damage.SchoolDamage.attributed(
                source, hit, com.vincenthuto.hemomancy.common.damage.SchoolDamage.owner(target, hit)), amount);
    }

    public static void impact(LivingEntity target,EnumBloodTendency primary,EnumBloodTendency secondary,Vec3 direction) {
        if(!(target.level() instanceof ServerLevel level))return;
        Vec3 at=target.position().add(0,target.getBbHeight()*.55,0);
        send(level,new ManipulationAccentPacket(primary==null?EnumBloodTendency.ANIMUS:primary,secondary,
                at,direction.normalize(),target.getId(),true));
    }

    public static void accent(ServerLevel level, EnumBloodTendency school, Vec3 at, Vec3 direction) {
        send(level, new ManipulationAccentPacket(school, null, at, direction, -1, false));
    }

    private static void send(ServerLevel level,ManipulationAccentPacket packet) {
        Vec3 at=packet.position();
        PacketDistributor.sendToPlayersNear(level,null,at.x,at.y,at.z,48,packet);
    }
}
