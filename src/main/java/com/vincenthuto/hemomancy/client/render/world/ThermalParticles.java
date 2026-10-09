package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Shader particles share the manipulation accent budget and update positions once per tick. */
final class ThermalParticles {
    private static final List<Particle> PARTICLES=new ArrayList<>();
    private static ClientLevel world;
    private ThermalParticles() {}
    static void clear(){PARTICLES.clear();world=null;}
    private static boolean ready() {
        var current=Minecraft.getInstance().level;
        if(current!=world){clear();world=current;}
        return world!=null;
    }
    static void emit(Vec3 at,Vec3 direction,boolean cold,int count,boolean soft) {
        if(!ready())return;
        int seed=at.hashCode()+(int)world.getGameTime();
        for(int i=0;i<Math.min(count,10);i++) {
            if(PARTICLES.size()>=192)PARTICLES.removeFirst();
            double a=i*2.39996;
            Vec3 velocity=new Vec3(Math.cos(a)*.035,cold?.015:.045,Math.sin(a)*.035)
                    .add(direction.normalize().scale(soft?.005:.025));
            PARTICLES.add(new Particle(at,velocity,cold,seed+i,world.getGameTime(),soft?24:32,soft?.16:.25,.45));
        }
    }
    /** A directed hot jet (breath weapons): fast licks inside a cone that burn most of the way out before smoking. */
    static void jet(Vec3 at,Vec3 aim,int count,double speed,double spread) {
        if(!ready())return;
        Vec3 forward=aim.normalize();
        long now=world.getGameTime();
        int seed=at.hashCode()+(int)now;
        for(int i=0;i<count;i++) {
            if(PARTICLES.size()>=192)PARTICLES.removeFirst();
            Vec3 scatter=new Vec3(ThermalMotion.random(seed,i*3)-.5,ThermalMotion.random(seed,i*3+1)-.5,
                    ThermalMotion.random(seed,i*3+2)-.5).scale(spread*2);
            double pace=speed*(.8+ThermalMotion.random(seed,i+31)*.4);
            Vec3 start=at.add(forward.scale(ThermalMotion.random(seed,i+57)*.3));
            PARTICLES.add(new Particle(start,forward.add(scatter).normalize().scale(pace),false,seed+i,now,18,
                    .2+ThermalMotion.random(seed,i+83)*.14,.75));
        }
    }
    static void tick() {
        if(!ready())return;
        long now=world.getGameTime();
        PARTICLES.removeIf(p->now-p.born>=p.life);
        for(Particle particle:PARTICLES) {
            particle.previous=particle.position;
            particle.position=particle.origin.add(ThermalMotion.drift(particle.velocity,now-particle.born,
                    particle.cold?.055:.012,particle.cold?.00025:-.0008));
        }
    }
    static void render(PoseStack poses,LuxUmbraBatch batch,Vec3 camera,Vec3 right,Vec3 up,double time,float partial) {
        if(world==null)return;
        var setting=Minecraft.getInstance().options.particles().get();
        if(setting==ParticleStatus.MINIMAL)return;
        for(int i=0;i<PARTICLES.size();i++) {
            if(setting==ParticleStatus.DECREASED && i%2==1)continue;
            Particle particle=PARTICLES.get(i);
            Vec3 at=particle.previous.lerp(particle.position,partial);
            if(at.distanceToSqr(camera)>48*48)continue;
            double progress=(time-particle.born)/particle.life;
            float alpha=(float)Math.sin(Math.PI*Math.max(0,Math.min(1,progress)));
            poses.pushPose();poses.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
            if(particle.cold)ThermalGeometry.powder(poses,batch.vertices(LuxUmbraBatch.Material.CRUOR),Vec3.ZERO,
                    right,up,particle.size*(1+progress),particle.seed,alpha);
            else if(progress<particle.fireFraction)ThermalGeometry.fire(poses,batch.vertices(LuxUmbraBatch.Material.FLAME),Vec3.ZERO,
                    right,up,particle.size*.45,particle.size*.9,particle.seed,alpha);
            else ThermalGeometry.smoke(poses,batch.vertices(LuxUmbraBatch.Material.FLAME),Vec3.ZERO,
                    right,up,particle.size*(.5+progress),particle.size*1.6,particle.seed,alpha*.55f);
            poses.popPose();
        }
    }
    private static final class Particle {
        final Vec3 origin,velocity;
        final boolean cold;
        final int seed,life;
        final long born;
        final double size,fireFraction;
        Vec3 previous,position;
        Particle(Vec3 origin,Vec3 velocity,boolean cold,int seed,long born,int life,double size,double fireFraction) {
            this.origin=origin;this.velocity=velocity;this.cold=cold;this.seed=seed;this.born=born;this.life=life;this.size=size;
            this.fireFraction=fireFraction;
            previous=position=origin;
        }
    }
}
