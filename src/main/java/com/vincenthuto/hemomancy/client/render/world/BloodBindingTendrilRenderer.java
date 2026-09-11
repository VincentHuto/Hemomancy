package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.BloodBindingTendrilClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Tick-sampled ground contact, interpolated authored coils, one sorted liquid surface. */
public final class BloodBindingTendrilRenderer {
    private static final Map<Long, Curve> CURVES = new LinkedHashMap<>();
    private static ClientLevel world;
    private record Curve(List<BloodBindingTendrilGeometry.Strand> previous, List<BloodBindingTendrilGeometry.Strand> current) {}
    private BloodBindingTendrilRenderer() {}
    static void tick() {
        var mc=Minecraft.getInstance();
        if(world!=mc.level){CURVES.clear();world=mc.level;}
        if(world==null || mc.player==null)return;
        var present=new HashSet<Long>();
        for(var snapshot:BloodBindingTendrilClientState.snapshots(1)) {
            present.add(snapshot.seed());
            double top=Math.max(snapshot.casterFeet().y,snapshot.targetFeet().y)+3;
            double bottom=Math.min(snapshot.casterFeet().y,snapshot.targetFeet().y)-4;
            var strands=BloodBindingTendrilGeometry.strands(snapshot.casterFeet(),snapshot.targetFeet(),snapshot.targetHeight(),
                    snapshot.seed(),world.getGameTime(),snapshot.ageTicks(),snapshot.retractionTicks(),(x,z)-> {
                        var hit=world.clip(new ClipContext(new Vec3(x,top,z),new Vec3(x,bottom,z),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));
                        return hit.getType()==HitResult.Type.BLOCK?hit.getLocation().y:Double.NaN;
                    });
            var old=CURVES.get(snapshot.seed());
            CURVES.put(snapshot.seed(),new Curve(old==null?strands:old.current,strands));
        }
        CURVES.keySet().retainAll(present);
    }
    static void collect(PoseStack poses,LuxUmbraBatch batch,Vec3 camera,float partial) {
        draw(poses,batch.vertices(LuxUmbraBatch.Material.ANIMUS),camera,partial);
    }
    /** Compatibility entry point for external render integrations. */
    public static void render(PoseStack poses,float partial) {
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        AnimusMortemRenderTypes.begin(mc.level.getGameTime()+partial);
        var buffers=mc.renderBuffers().bufferSource();
        draw(poses,buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS),mc.gameRenderer.getMainCamera().getPosition(),partial);
        buffers.endBatch(AnimusMortemRenderTypes.ANIMUS);
    }
    private static void draw(PoseStack poses,VertexConsumer target,Vec3 camera,float partial) {
        for(var curve:CURVES.values()) {
            var strands=new ArrayList<BloodBindingTendrilGeometry.Strand>();
            for(int s=0;s<curve.current.size();s++) {
                var current=curve.current.get(s);
                var old=s<curve.previous.size()?curve.previous.get(s):current;
                var joints=new ArrayList<BloodBindingTendrilGeometry.Joint>();
                for(int j=0;j<current.joints().size();j++) {
                    var b=current.joints().get(j);var a=j<old.joints().size()?old.joints().get(j):b;
                    joints.add(new BloodBindingTendrilGeometry.Joint(a.center().lerp(b.center(),partial),
                            b.halfWidth(),b.opacity(),b.groundAligned()));
                }
                strands.add(new BloodBindingTendrilGeometry.Strand(current.index(),joints));
            }
            SanguineTendrilRibbonRenderer.render(poses,new FlowVertices(target),strands,camera,false);
        }
    }
    private record FlowVertices(VertexConsumer target) implements VertexConsumer {
        public VertexConsumer addVertex(float x,float y,float z){target.addVertex(x,y,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){target.setColor(255,255,255,a);return this;}
        public VertexConsumer setUv(float u,float v){target.setUv(v,u);return this;}
        public VertexConsumer setUv1(int u,int v){target.setUv1(u,v);return this;}
        public VertexConsumer setUv2(int u,int v){target.setUv2(u,v);return this;}
        public VertexConsumer setNormal(float x,float y,float z){target.setNormal(x,y,z);return this;}
    }
}
