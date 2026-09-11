package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodCloudCarrierEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Trails retain the actual travelled path, including guided turns and orbiting halos. */
final class BloodProjectileEffects {
    private static final LinkedHashMap<Integer,Trail> TRAILS=new LinkedHashMap<>();
    private static ClientLevel world;
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
            if(trail.points.isEmpty()||trail.points.getLast().distanceToSqr(at)>.0001) {
                if(trail.points.size()>=10)trail.points.removeFirst();trail.points.add(at);trail.lastMoved=now;
            }
        }
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
            float trailAlpha=alpha*Math.max(0,1-(float)(time-trail.lastMoved)/8);
            double width=entity instanceof BloodShotEntity shot&&shot.visualForm()==2?.17:.07;
            LuxUmbraGeometry.ribbonPath(p,v,path,camera,width,0,seed,0xFFFFFF,trailAlpha*.8f);
            if(alive&&!(entity instanceof BloodNeedleEntity)) {
                boolean mortar=entity instanceof BloodShotEntity shot&&shot.visualForm()==2;
                double size=mortar?.32:entity instanceof BloodCloudCarrierEntity?.22:.13;
                Vec3 axis=entity.getDeltaMovement().normalize();
                if(axis.lengthSqr()<.001)axis=up;
                Vec3 across=LuxUmbraGeometry.facingSide(axis,camera.subtract(at));
                LuxUmbraGeometry.card(p,v,at,across,axis,size,size*(mortar?1.15:1.7),4,seed,0xFFFFFF,alpha);
                LuxUmbraGeometry.card(p,v,at,right,up,size*.7,size*.7,4,seed,0xFFFFFF,alpha*.8f);
            }
        }
        p.popPose();
    }
    private static final class Trail {final Entity entity;final List<Vec3> points=new ArrayList<>();long lastSeen,lastMoved;Trail(Entity entity){this.entity=entity;}}
}
