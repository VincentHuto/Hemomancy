package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodCloudCarrierEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Trails retain the actual travelled path, including guided turns and orbiting halos. */
public final class BloodProjectileEffects {
    private static final LinkedHashMap<Integer,Trail> TRAILS=new LinkedHashMap<>();
    private static ClientLevel world;

    /** Forgets everything, so nothing outlives the world it was seen in. */
    public static void clear() {
        TRAILS.clear();
        world = null;
    }
    private BloodProjectileEffects() {}
    private static void reset(){if(world!=Minecraft.getInstance().level){TRAILS.clear();world=Minecraft.getInstance().level;}}
    static void tick() {
        reset();if(world==null)return;long now=world.getGameTime();
        TRAILS.values().removeIf(t->now-t.lastSeen>8);
        for(Entity entity:world.entitiesForRendering()) {
            if(!(entity instanceof BloodShotEntity)&&!(entity instanceof BloodNeedleEntity)&&!(entity instanceof BloodCloudCarrierEntity))continue;
            if(entity.distanceToSqr(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())>128*128)continue;
            Trail trail=TRAILS.get(entity.getId());
            if(trail==null){if(TRAILS.size()>=192)TRAILS.pollFirstEntry();trail=new Trail(entity);TRAILS.put(entity.getId(),trail);}
            trail.lastSeen=now;
            Vec3 at=entity instanceof BloodShotEntity shot?shot.visualPosition(1):entity.position();
            // Once lodged, server position corrections must not grow the path or turn the needle.
            if(!trail.splashed && (trail.points.isEmpty()||trail.points.getLast().distanceToSqr(at)>.0001)) {
                if(trail.points.size()>=10)trail.points.removeFirst();trail.points.add(at);trail.lastMoved=now;
                if(entity.getDeltaMovement().lengthSqr()>1e-4)trail.heading=entity.getDeltaMovement();
            }
            if(entity instanceof BloodShotEntity shot && shot.visualForm()!=3)
                lodge(trail,now,BloodShotGeometry.splashRadius(BloodShotGeometry.size(shot.visualForm())));
            else if(entity instanceof BloodNeedleEntity needle && !needle.isCoronationSword())
                lodge(trail,now,BloodNeedleGeometry.PUNCTURE);
        }
    }

    /** A projectile that lodges in a block stops here long before the server removes it, so it splashes on contact. */
    private static void lodge(Trail trail,long now,float radius) {
        if(trail.splashed || trail.points.size()<2 || now-trail.lastMoved<1)return;
        trail.splashed=true;
        Vec3 at=trail.points.getLast();
        Vec3 d=trail.heading.lengthSqr()>1e-6?trail.heading.normalize():travel(trail,new Vec3(0,-1,0)).normalize();
        // Find the face it struck so the stain lies on it and the spray leaves it; the splash direction points into the face.
        var hit=world.clip(new net.minecraft.world.level.ClipContext(at.subtract(d),at.add(d.scale(1.5)),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,trail.entity));
        if(hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK)
            splash(hit.getLocation(),Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(-1),radius,BloodShotGeometry.BLOCK);
        else splash(at,d,radius,BloodShotGeometry.BLOCK);
    }

    /** Server removal after an entity hit, expiry or a lost owner. Mortars splash from the server's own blast. */
    static void shotRemoved(BloodShotEntity shot) {
        Trail trail=TRAILS.get(shot.getId());
        if(shot.visualForm()==2 || trail!=null && trail.splashed)return;
        if(trail!=null)trail.splashed=true;
        if(!struckBody(shot,trail))return;
        splash(shot.position(),trail==null?shot.getDeltaMovement():travel(trail,shot.getDeltaMovement()),
                BloodShotGeometry.splashRadius(BloodShotGeometry.size(shot.visualForm())),BloodShotGeometry.ENTITY);
    }

    /** A needle removed in flight punctured something; one that lodged already splashed against the block. */
    static void needleRemoved(BloodNeedleEntity needle) {
        Trail trail=TRAILS.get(needle.getId());
        if(trail!=null && trail.splashed)return;
        if(trail!=null)trail.splashed=true;
        if(!struckBody(needle,trail))return;
        splash(needle.position(),trail==null?needle.getDeltaMovement():travel(trail,needle.getDeltaMovement()),
                BloodNeedleGeometry.PUNCTURE,BloodShotGeometry.NEEDLE);
    }

    /**
     * The client sees every server removal as a discard, including projectiles that simply left tracking range or an
     * orbit that expired; only splash when a body other than the shooter is actually where the projectile ended.
     */
    private static boolean struckBody(Entity projectile,Trail trail) {
        Vec3 reach=trail==null || trail.heading.lengthSqr()<1e-6?Vec3.ZERO:trail.heading.normalize().scale(1.5);
        Entity owner=projectile instanceof net.minecraft.world.entity.projectile.Projectile shot?shot.getOwner():null;
        return !world.getEntities(projectile,projectile.getBoundingBox().expandTowards(reach).expandTowards(reach.scale(-1)).inflate(.6),
                e->e instanceof net.minecraft.world.entity.LivingEntity && e!=owner).isEmpty();
    }

    /** The drawn wake stops a few blocks behind the head; fast projectiles would otherwise drag lines across the sky. */
    static List<Vec3> trim(List<Vec3> path,double length) {
        if(path.size()<2)return path;
        List<Vec3> kept=new ArrayList<>();
        Vec3 previous=path.getLast();kept.add(previous);
        double left=length;
        for(int i=path.size()-2;i>=0 && left>0;i--) {
            Vec3 point=path.get(i);
            double step=point.distanceTo(previous);
            if(step>left){kept.add(previous.lerp(point,left/step));break;}
            kept.add(point);left-=step;previous=point;
        }
        java.util.Collections.reverse(kept);
        return kept;
    }

    // The client may already have bounced the arrow off a body it cannot hurt; the whole path keeps the true heading.
    private static Vec3 travel(Trail trail,Vec3 fallback) {
        return trail.points.size()>=2?trail.points.getLast().subtract(trail.points.getFirst()):fallback;
    }

    // Needles arc under gravity: fly along the live velocity, lodge along the last velocity seen in flight.
    private static Vec3 heading(Trail trail,Vec3 velocity) {
        return velocity.lengthSqr()>1e-6?velocity:trail.heading;
    }

    private static void splash(Vec3 at,Vec3 direction,float radius,int variant) {
        Vec3 d=direction.lengthSqr()<1e-6?new Vec3(0,1,0):direction.normalize();
        ManipulationVisualRenderer.accept(new ManipulationVisualPacket(ManipulationVisuals.Form.BLOOD_SPLASH,-1,at,at.add(d),
                radius,BloodShotGeometry.SPLASH_TICKS,variant));
    }

    static void render(PoseStack p,LuxUmbraBatch batch,Vec3 camera,Vec3 right,Vec3 up,double time,float partial) {
        if(world==null)return;
        var v=batch.vertices(LuxUmbraBatch.Material.ANIMUS);
        p.pushPose();p.translate(-camera.x,-camera.y,-camera.z);
        for(Trail trail:TRAILS.values()) {
            Entity entity=trail.entity;boolean alive=!entity.isRemoved();
            Vec3 at=alive?(entity instanceof BloodShotEntity shot?shot.visualPosition(partial):entity.getPosition(partial)):entity.position();
            if(at.distanceToSqr(camera)>128*128)continue;
            int seed=Math.floorMod(entity.getId(),113);
            float alpha=alive?1:Math.max(0,1-(float)(time-trail.lastSeen)/8);
            var path=new ArrayList<>(trail.points);
            if(!path.isEmpty())path.set(path.size()-1,at);
            // A wake left behind by a lodged or spent projectile drains quickly instead of hanging in the air.
            float trailAlpha=alpha*Math.max(0,1-(float)(time-trail.lastMoved)/(trail.splashed?4:8));
            if(entity instanceof BloodShotEntity shot) {
                Entity owner=shot.visualForm()==3?shot.getOwner():null;
                Vec3 tether=owner==null?null:owner.getPosition(partial).add(0,owner.getBbHeight()*.62,0);
                BloodShotGeometry.shot(p,v,trim(path,shot.visualForm()==2?4:3),alive?shot.getDeltaMovement():Vec3.ZERO,camera,right,up,time,
                        AnimationTime.age(shot, partial),shot.visualForm(),seed,alpha,trailAlpha,alive && !trail.splashed,tether);
                continue;
            }
            if(entity instanceof BloodNeedleEntity needle && needle.isForged()) {
                // Forged iron is drawn in the solid pass; here only its blood wake, glint and wound.
                ForgedNeedleGeometry.flightFlow(p,batch.vertices(LuxUmbraBatch.Material.DUCTILIS),trim(path,needle.isLanceNeedle()?3:2),
                        heading(trail,alive && !trail.splashed?needle.getDeltaMovement():Vec3.ZERO),camera,right,up,time,
                        AnimationTime.age(needle, partial),seed,alive?alpha*needle.swordOpacity():0,trailAlpha,needle.isLanceNeedle(),trail.splashed);
                continue;
            }
            if(entity instanceof BloodNeedleEntity needle && !needle.isCoronationSword()) {
                BloodNeedleGeometry.flight(p,v,trim(path,needle.isLanceNeedle()?3:2),heading(trail,alive && !trail.splashed?needle.getDeltaMovement():Vec3.ZERO),camera,right,up,time,
                        AnimationTime.age(needle, partial),seed,alive?alpha*needle.swordOpacity():0,trailAlpha,needle.isLanceNeedle(),
                        trail.splashed,needle.shakeTime-partial);
                continue;
            }
            LuxUmbraGeometry.ribbonPath(p,v,path,camera,.07,0,seed,0xFFFFFF,trailAlpha*.8f);
            if(alive&&!(entity instanceof BloodNeedleEntity)) {
                double size=.22;
                Vec3 axis=entity.getDeltaMovement().normalize();
                if(axis.lengthSqr()<.001)axis=up;
                Vec3 across=LuxUmbraGeometry.facingSide(axis,camera.subtract(at));
                LuxUmbraGeometry.card(p,v,at,across,axis,size,size*1.7,4,seed,0xFFFFFF,alpha);
                LuxUmbraGeometry.card(p,v,at,right,up,size*.7,size*.7,4,seed,0xFFFFFF,alpha*.8f);
            }
        }
        p.popPose();
    }
    /** Solid pass: the iron bodies of forged needles. */
    static void renderIron(PoseStack p,com.mojang.blaze3d.vertex.VertexConsumer iron,Vec3 camera,double time,float partial) {
        if(world==null)return;
        p.pushPose();p.translate(-camera.x,-camera.y,-camera.z);
        for(Trail trail:TRAILS.values()) {
            if(!(trail.entity instanceof BloodNeedleEntity needle) || !needle.isForged())continue;
            boolean alive=!needle.isRemoved();
            if(!alive)continue;
            Vec3 at=needle.getPosition(partial);
            if(at.distanceToSqr(camera)>128*128)continue;
            var path=new ArrayList<>(trail.points);
            if(!path.isEmpty())path.set(path.size()-1,at);else path.add(at);
            ForgedNeedleGeometry.flightIron(p,iron,trim(path,needle.isLanceNeedle()?3:2),
                    heading(trail,!trail.splashed?needle.getDeltaMovement():Vec3.ZERO),camera,AnimationTime.age(needle, partial),
                    Math.floorMod(needle.getId(),113),needle.swordOpacity(),needle.isLanceNeedle(),trail.splashed,needle.shakeTime-partial);
        }
        p.popPose();
    }
    private static final class Trail {final Entity entity;final List<Vec3> points=new ArrayList<>();long lastSeen,lastMoved;boolean splashed;Vec3 heading=Vec3.ZERO;Trail(Entity entity){this.entity=entity;}}
}
