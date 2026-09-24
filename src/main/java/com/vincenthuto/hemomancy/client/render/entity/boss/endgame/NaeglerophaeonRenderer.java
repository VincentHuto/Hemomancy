package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.*;
import com.vincenthuto.hemomancy.client.screen.overlay.NaeglerophaeonCaptureOverlay;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hutoslib.client.particle.BoltRenderer;
import com.vincenthuto.hutoslib.common.lightning.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class NaeglerophaeonRenderer extends EntityRenderer<NaeglerophaeonEntity> {
    private static final ResourceLocation TEXTURE=Hemomancy.rloc("textures/entity/naeglerophaeon_cube_palette.png");
    private static final ResourceLocation EYE_MARKER=Hemomancy.rloc("textures/entity/naeglerophaeon_eye_marker.png");
    private static final LightningTestConfig ARC=new LightningTestConfig(LightningTestConfig.Backend.BOLT,
            0xDDFF424D,0xDDFF424D,0xFFFFFFAB,32,0,0,0,18,3,4,3,.025F,.009F,true,0,false,2);
    private final Map<NaeglerophaeonEntity,State> states=new WeakHashMap<>();
    private static final class State {
        final NaeglerophaeonPose pose=new NaeglerophaeonPose();
        final long[] sparks={Long.MIN_VALUE,Long.MIN_VALUE,Long.MIN_VALUE};
        long lastTick=Long.MIN_VALUE;
        Vec3[] lastTail;
        Vec3 lookDirection;
        double lastLookTime=Double.NaN;
        double huntBlend, lastHuntTime=Double.NaN;
        int lastPhase;
        long releaseTick=Long.MIN_VALUE, lastRenderTick=Long.MIN_VALUE;
    }
    public NaeglerophaeonRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius=.9F; }
    @Override public boolean shouldRender(NaeglerophaeonEntity entity,Frustum frustum,
            double cameraX,double cameraY,double cameraZ) {
        return entity.shouldRender(cameraX,cameraY,cameraZ)
                && frustum.isVisible(entity.getBoundingBoxForCulling().inflate(32));
    }
    @Override public void render(NaeglerophaeonEntity entity,float yaw,float partial,PoseStack stack,MultiBufferSource buffers,int light) {
        boolean outline=Minecraft.getInstance().shouldEntityAppearGlowing(entity);
        if(entity.isInvisible() && !outline) { super.render(entity,yaw,partial,stack,buffers,light); return; }
        State state=states.computeIfAbsent(entity,e->new State());
        long tick=entity.level().getGameTime();
        if(state.lastRenderTick!=Long.MIN_VALUE && tick-state.lastRenderTick>5) {
            state.lastTail=null;
            state.lookDirection=null;
            state.lastLookTime=Double.NaN;
            state.lastHuntTime=Double.NaN;
            state.releaseTick=Long.MIN_VALUE;
        }
        state.lastRenderTick=tick;
        int previousPhase=state.lastPhase;
        boolean capturing=entity.animationPhase()==NaeglerophaeonEntity.CAPTURE || entity.isGrabbing();
        if(!capturing && (state.lastPhase==NaeglerophaeonEntity.CAPTURE || state.lastPhase==NaeglerophaeonEntity.GRAB)) state.releaseTick=tick;
        state.lastPhase=entity.animationPhase();
        state.pose.update(tick,entity.position(),Math.toRadians(entity.getYRot()),
                Math.toRadians(entity.getXRot()));
        Vec3 origin=entity.getPosition(partial);
        Entity victim=entity.level().getEntity(entity.grabbedEntityId());
        if(victim!=null && (!victim.isAlive() || entity.distanceToSqr(victim)>32*32)) victim=null;
        double renderTime=tick+partial;
        double huntTarget=entity.isHunting()?1:0;
        if(Double.isNaN(state.lastHuntTime)) state.huntBlend=huntTarget;
        else state.huntBlend+=(huntTarget-state.huntBlend)
                *(1-Math.exp(-.12*Math.clamp(renderTime-state.lastHuntTime,0,5)));
        state.lastHuntTime=renderTime;
        if(capturing && victim!=null) {
            Vec3 direction=victim.getEyePosition(partial).subtract(origin.add(0,.8,0));
            if(direction.lengthSqr()>1.0E-8) {
                direction=direction.normalize();
                if(state.lookDirection==null || previousPhase!=NaeglerophaeonEntity.CAPTURE
                        && previousPhase!=NaeglerophaeonEntity.GRAB) state.lookDirection=direction;
                else {
                    double elapsed=Double.isNaN(state.lastLookTime)?1:Math.max(0,renderTime-state.lastLookTime);
                    double blend=1-Math.exp(-.35*elapsed);
                    state.lookDirection=state.lookDirection.lerp(direction,blend).normalize();
                }
            }
        }
        state.lastLookTime=renderTime;
        var input=new NaeglerophaeonModel.Input(tick+partial,entity.getId(),
                Math.toRadians(Mth.rotLerp(partial,entity.yRotO,entity.getYRot())),
                Math.toRadians(Mth.rotLerp(partial,entity.xRotO,entity.getXRot())),entity.animationPhase(),
                tick+partial-entity.animationStart(),victim==null?null:victim.getPosition(partial).subtract(origin),
                victim==null?0:Math.toRadians(Mth.rotLerp(partial,victim.yRotO,victim.getYRot())),
                victim==null?1.8:victim.getBbHeight(),state.pose.offsets(origin),
                state.lastTail==null?null:Arrays.stream(state.lastTail).map(p->p.subtract(origin)).toArray(Vec3[]::new),
                capturing || state.releaseTick==Long.MIN_VALUE?0:Math.max(0,1-(tick+partial-state.releaseTick)/40.0),
                state.huntBlend,state.pose.turnLag(partial));
        float lookWeight=switch(entity.animationPhase()) {
            case NaeglerophaeonEntity.CAPTURE -> (float)NaeglerophaeonModel.smooth(input.phaseTicks()/20);
            case NaeglerophaeonEntity.GRAB -> 1;
            case NaeglerophaeonEntity.DISCHARGE,NaeglerophaeonEntity.RECOVERY ->
                    (float)NaeglerophaeonModel.smooth(input.releaseBlend());
            default -> 0;
        };
        boolean dynamicTail=capturing || input.releaseBlend()>0 && state.lastTail!=null;
        Vec3[] tail=null;
        if(dynamicTail) {
            tail=NaeglerophaeonBlockModel.tailPoints(input);
            if(capturing && input.victim()!=null) {
                var reach=new NaeglerophaeonModel.Input(input.time(),input.seed(),input.yaw(),input.pitch(),
                        NaeglerophaeonEntity.GRAB,input.phaseTicks(),input.victim(),input.victimYaw(),
                        input.victimHeight(),input.history(),null,0,input.hunting(),input.turns());
                Vec3[] target=NaeglerophaeonModel.build(reach,1).centers()[12];
                double blend=entity.animationPhase()==NaeglerophaeonEntity.CAPTURE
                        ?NaeglerophaeonModel.smooth(input.phaseTicks()/20):1;
                for(int j=0;j<tail.length;j++)
                    tail[j]=tail[j].lerp(NaeglerophaeonModel.sample(target,j/48D),blend);
            } else if(input.releaseBlend()>0 && input.releasedTail()!=null) {
                for(int j=0;j<tail.length;j++)
                    tail[j]=tail[j].lerp(NaeglerophaeonModel.sample(input.releasedTail(),j/48D),input.releaseBlend());
            }
        }
        if(capturing && tail!=null) state.lastTail=Arrays.stream(tail).map(p->p.add(origin)).toArray(Vec3[]::new);
        int overlay=LivingEntityRenderer.getOverlayCoords(entity,0);
        boolean markEyes=!entity.isInvisible() && NaeglerophaeonCaptureOverlay.markEyes(entity);
        for(int pass=0;pass<(markEyes?4:3);pass++) {
            RenderType material=entity.isInvisible()?RenderType.outline(TEXTURE):pass==0?RenderType.entityCutoutNoCull(TEXTURE)
                    :pass==1?RenderType.eyes(TEXTURE):pass==2?RenderType.entityTranslucent(TEXTURE)
                    :RenderType.eyes(EYE_MARKER);
            VertexConsumer consumer=buffers.getBuffer(material);
            NaeglerophaeonBlockModel.render(stack,consumer,pass,input,light,overlay,dynamicTail,
                    state.lookDirection,lookWeight,markEyes);
            if(dynamicTail) NaeglerophaeonBlockModel.renderCaptureTail(stack,consumer,pass,tail,light,overlay);
        }
        if(!entity.isInvisible() && entity.isAlive() && entityRenderDispatcher.distanceToSqr(entity)<64*64 && state.lastTick!=tick) {
            state.lastTick=tick;
            Vec3[] tips=null;
            for(var pulse:NaeglerophaeonModel.pulses(tick,entity.getId())) if(pulse.spark()) {
                int slot=pulse.limb()/4;
                if(state.sparks[slot]==pulse.event()) continue;
                state.sparks[slot]=pulse.event();
                if(tips==null) tips=NaeglerophaeonBlockModel.tips(input);
                Vec3 localTip=tips[pulse.limb()];
                if(localTip==null) continue;
                Vec3 from=localTip.add(origin);
                Vec3 forward=localTip.subtract(0,.8,0).normalize();
                for(int i=0;i<2;i++) {
                    Vec3 end=from.add(forward.scale(.65)).add(Math.sin(pulse.event()+i)*.25,.15*i,Math.cos(pulse.event()+i)*.25);
                    BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(from,end,pulse.event()+i,ARC.outerColor(),.025F,ARC),partial);
                    BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(from,end,pulse.event()+i,ARC.innerColor(),.008F,ARC),partial);
                }
            }
        }
        super.render(entity,yaw,partial,stack,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(NaeglerophaeonEntity e) { return TEXTURE; }
}
