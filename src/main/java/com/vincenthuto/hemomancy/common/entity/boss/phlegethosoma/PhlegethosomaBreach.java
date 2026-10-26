package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.worldgen.terrain.phlegethontic.PhlegethonticTags;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Per-animal attack route. All movement is swept through loaded, unobstructed space. */
final class PhlegethosomaBreach {
    private final PhlegethosomaEntity boss;
    private final Set<UUID> contacts = new HashSet<>();
    private Vec3 dive, strike, landing, segmentStart;
    private double surface;
    private int replans;

    PhlegethosomaBreach(PhlegethosomaEntity boss) { this.boss = boss; }

    boolean begin(LivingEntity target) {
        if(!boss.isIchorSwimming() || boss.distanceTo(target)<6 || boss.distanceTo(target)>14)return false;
        Vec3 deep = deepPoint(boss.position());
        if(deep==null)return false;
        Vec3 aim = target.position().add(0,.25,0);
        Vec3 direction = aim.subtract(boss.position()).multiply(1,0,1).normalize();
        Vec3 returnPoint = findLanding(aim.add(direction.scale(3)),aim);
        if(returnPoint==null || !clearLine(boss.position(),deep) || !clearArc(deep,aim,3))return false;
        dive=deep;strike=aim;landing=returnPoint;surface=deep.y+2.2;
        replans=0;contacts.clear();boss.change(PhlegethosomaEntity.Action.DIVE);
        boss.playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE,1,.55F);
        return true;
    }

    void returnToPool() {
        if(landing==null) { finish();return; }
        segmentStart=boss.position();boss.change(PhlegethosomaEntity.Action.RETURN_TO_ICHOR);
    }

    void tick(ServerLevel level, LivingEntity target) {
        int t=boss.actionTime();
        switch(boss.action()) {
            case DIVE -> {
                ripple(level);
                if(!boss.valid(target) || !moveToward(dive,.22)) { returnToPool();return; }
                if(t>=PhlegethosomaBreachRules.TIMELINE.windup()) {
                    if(boss.getBoundingBox().maxY>surface-.3 || !clearArc(boss.position(),strike,3)) { finish();return; }
                    segmentStart=boss.position();boss.change(PhlegethosomaEntity.Action.BREACH);
                    boss.playSound(SoundEvents.GENERIC_SPLASH,1.4F,.65F);
                }
            }
            case BREACH -> {
                Vec3 desired=PhlegethosomaBreachRules.arc(segmentStart,strike,3,t/(double)PhlegethosomaBreachRules.TIMELINE.active());
                if(!moveToward(desired,.9)) { returnToPool();return; }
                AABB sweep=boss.getBoundingBox().expandTowards(boss.getDeltaMovement()).inflate(.45);
                for(LivingEntity victim:level.getEntitiesOfClass(LivingEntity.class,sweep,boss::valid)) {
                    if(boss.visible(victim) && contacts.add(victim.getUUID())
                            && victim.hurt(boss.damageSources().mobAttack(boss),PhlegethosomaBreachRules.DAMAGE)) {
                        Vec3 direction=victim.position().subtract(boss.position()).multiply(1,0,1).normalize();
                        victim.knockback(.5,-direction.x,-direction.z);
                    }
                }
                if(t>=PhlegethosomaBreachRules.TIMELINE.active())returnToPool();
            }
            case RETURN_TO_ICHOR -> {
                Vec3 desired=PhlegethosomaBreachRules.arc(segmentStart,landing,.35,t/(double)PhlegethosomaBreachRules.TIMELINE.recovery());
                if(!moveToward(desired,.9)) {
                    if(replans++>=1) { finish();return; }
                    Vec3 replacement=findLanding(boss.position(),boss.position());
                    if(replacement==null) { finish();return; }
                    landing=replacement;returnToPool();return;
                }
                if(t>=PhlegethosomaBreachRules.TIMELINE.recovery() && boss.position().distanceToSqr(landing)<.3) {
                    boss.playSound(SoundEvents.GENERIC_SPLASH,1.2F,.7F);finish();
                } else if(t>PhlegethosomaBreachRules.TIMELINE.recovery()+12)finish();
            }
            default -> {}
        }
    }

    private void finish() {
        dive=null;strike=null;landing=null;segmentStart=null;contacts.clear();
        boss.setDeltaMovement(Vec3.ZERO);boss.change(PhlegethosomaEntity.Action.RECOVERY);
    }

    private boolean moveToward(Vec3 destination,double speed) {
        Vec3 delta=PhlegethosomaBreachRules.step(boss.position(),destination);
        if(delta.lengthSqr()>speed*speed)delta=delta.normalize().scale(speed);
        if(!clearLine(boss.position(),boss.position().add(delta)))return false;
        if(delta.horizontalDistanceSqr()>.001)boss.faceBreach(delta);
        boss.setDeltaMovement(delta);boss.getNavigation().stop();return true;
    }

    private Vec3 findLanding(Vec3 near,Vec3 departure) {
        Vec3 best=null;double distance=Double.MAX_VALUE;
        for(int x=-6;x<=6;x+=2)for(int z=-6;z<=6;z+=2) {
            Vec3 point=deepPoint(near.add(x,0,z));
            if(point==null)continue;
            point=point.add(0,.6,0);
            double d=point.distanceToSqr(near);
            if(d<distance && clearAt(point) && clearArc(departure,point,.35)) {best=point;distance=d;}
        }
        return best;
    }

    private Vec3 deepPoint(Vec3 near) {
        BlockPos column=BlockPos.containing(near);
        for(int y=6;y>=-6;y--) {
            BlockPos top=column.offset(0,y,0);
            if(!boss.level().hasChunkAt(top) || !ichor(top) || ichor(top.above()))continue;
            if(!ichor(top.below()) || !ichor(top.below(2)))return null;
            Vec3 point=new Vec3(column.getX()+.5,top.getY()+1-2.2,column.getZ()+.5);
            return clearAt(point)?point:null;
        }
        return null;
    }

    private boolean ichor(BlockPos pos) {return boss.level().getFluidState(pos).is(PhlegethonticTags.ICHOR);}

    private boolean clearArc(Vec3 start,Vec3 end,double clearance) {
        Vec3 previous=start;
        for(int i=1;i<=32;i++) {
            Vec3 point=PhlegethosomaBreachRules.arc(start,end,clearance,i/32.0);
            if(!clearLine(previous,point))return false;
            previous=point;
        }
        return true;
    }

    private boolean clearLine(Vec3 start,Vec3 end) {
        int samples=Math.max(1,(int)Math.ceil(start.distanceTo(end)/.2));
        for(int i=0;i<=samples;i++)if(!clearAt(start.lerp(end,i/(double)samples)))return false;
        return true;
    }

    private boolean clearAt(Vec3 position) {
        AABB box=boss.getBoundingBox().move(position.subtract(boss.position()));
        if(box.minY<boss.level().getMinBuildHeight() || box.maxY>=boss.level().getMaxBuildHeight())return false;
        for(BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))) {
            if(!boss.level().hasChunkAt(pos) || boss.level().getFluidState(pos).is(FluidTags.LAVA))return false;
        }
        return boss.level().getWorldBorder().isWithinBounds(box) && boss.level().noCollision(boss,box);
    }

    private void ripple(ServerLevel level) {
        if(boss.tickCount%3!=0)return;
        var particle=new ColorParticleData(HLParticleInit.glow.get(),new ParticleColor(155,25,23));
        for(int i=0;i<8;i++) {
            double angle=i*Math.PI/4+boss.actionTime()*.08;
            level.sendParticles(particle,dive.x+Math.cos(angle)*1.2,surface+.04,dive.z+Math.sin(angle)*1.2,0,0,.025,0,1);
        }
    }
}
