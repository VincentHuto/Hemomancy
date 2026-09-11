package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hutoslib.client.particle.data.TendrilGeometry;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class BloodFlowEffects {
    private static final LinkedHashMap<Long,Connection> FLOWS=new LinkedHashMap<>();
    private static ClientLevel world;
    private BloodFlowEffects() {}
    private static void reset(){if(world!=Minecraft.getInstance().level){FLOWS.clear();world=Minecraft.getInstance().level;}}
    public static void accept(BloodFlowPacket packet) {
        reset();if(world==null)return;long now=world.getGameTime();
        if(packet.stop()){FLOWS.values().stream().filter(c->c.state.packet.owner()==packet.owner()&&c.state.packet.style()==packet.style()).forEach(c->c.state.retire(now));return;}
        Connection existing=FLOWS.get(packet.key());
        if(existing!=null){existing.state.refresh(packet,now);return;}
        if(FLOWS.size()>=128)FLOWS.pollFirstEntry();
        var c=new Connection(new BloodFlowState(packet,now));
        if(c.state.resolve(TendrilAnchor.forLevel(world),now)){c.update(now);FLOWS.put(packet.key(),c);}
    }
    static void tick() {
        reset();if(world==null)return;long now=world.getGameTime();
        FLOWS.values().removeIf(c->{if(c.state.finished(now))return true;
            if(c.state.resolve(TendrilAnchor.forLevel(world),now))c.update(now);else c.previous=c.points;return false;});
    }
    static void render(PoseStack p,LuxUmbraBatch batch,Vec3 camera,Vec3 right,Vec3 up,double time,float partial) {
        if(world==null)return;
        p.pushPose();p.translate(-camera.x,-camera.y,-camera.z);
        for(var c:FLOWS.values()) {
            var state=c.state;
            if(state.start==null||Math.min(state.start.distanceToSqr(camera),state.end.distanceToSqr(camera))>128*128)continue;
            boolean living=state.packet.style()==BloodFlowPacket.Style.LIVING;
            var v=batch.vertices(living?LuxUmbraBatch.Material.ANIMUS:LuxUmbraBatch.Material.MORTEM);
            float alpha=state.opacity(time);int seed=(int)Math.floorMod(state.packet.key(),113);
            var points=new ArrayList<Vec3>(c.points.size());
            for(int i=0;i<c.points.size();i++)points.add(c.previous.get(i).lerp(c.points.get(i),partial));
            if(state.start.distanceToSqr(state.end)<.001) {
                LuxUmbraGeometry.card(p,v,state.end,right,up,.12,.16,4,seed,0xFFFFFF,alpha);continue;
            }
            LuxUmbraGeometry.ribbonPath(p,v,points,camera,state.packet.config().baseWidth(),0,seed,0xFFFFFF,alpha);
            if(state.packet.style()==BloodFlowPacket.Style.EXTRACTION||state.packet.style()==BloodFlowPacket.Style.COMMUNION)
                for(int i=0;i<3;i++) {
                    double progress=((time-state.born)*.065+i/3.0)%1;
                    int segment=Math.min(points.size()-2,(int)(progress*(points.size()-1)));
                    Vec3 at=points.get(segment).lerp(points.get(segment+1),progress*(points.size()-1)-segment);
                    LuxUmbraGeometry.card(p,v,at,right,up,.065,.095,4,seed+i,0xFFFFFF,alpha);
                }
        }
        p.popPose();
    }
    private static final class Connection {
        final BloodFlowState state;List<Vec3> previous=List.of(),points=List.of();
        Connection(BloodFlowState state){this.state=state;}
        void update(long now) {
            var config=state.packet.config().withShape(12,1,state.packet.config().baseWidth(),.2f).withBranching(0,0,0,0).clamped();
            var geometry=TendrilGeometry.generate(state.start,state.end,config,config.seed(),now-state.born,TendrilGeometry.SurfaceResolver.NONE);
            previous=points;points=geometry.strands().isEmpty()?List.of(state.start,state.end):geometry.strands().getFirst().rings().stream().map(TendrilGeometry.Ring::center).toList();
            if(points.size()<2)points=List.of(state.start,state.end);
            if(previous.size()!=points.size())previous=points;
        }
    }
}
