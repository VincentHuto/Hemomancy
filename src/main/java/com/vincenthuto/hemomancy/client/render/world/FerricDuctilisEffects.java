package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity;
import com.vincenthuto.hemomancy.common.network.ConductionPathPacket;
import com.vincenthuto.hemomancy.common.network.particle.NerveLinkPacket;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class FerricDuctilisEffects {
    private static ClientLevel world;
    private static final Map<UUID,ConductionPathPacket> NETWORKS=new LinkedHashMap<>();
    private static final List<Link> LINKS=new ArrayList<>();
    private FerricDuctilisEffects() {}
    private static boolean ready() {
        var current=Minecraft.getInstance().level;
        if(world!=current){NETWORKS.clear();LINKS.clear();world=current;}
        return world!=null;
    }
    public static void accept(ConductionPathPacket packet) {
        if(!ready())return;
        if(packet.nodes().isEmpty() || packet.expiresAt()<=world.getGameTime()){NETWORKS.remove(packet.networkId());return;}
        if(NETWORKS.size()>=64 && !NETWORKS.containsKey(packet.networkId()))NETWORKS.remove(NETWORKS.keySet().iterator().next());
        NETWORKS.put(packet.networkId(),packet);
    }
    public static void accept(NerveLinkPacket packet) {
        if(!ready())return;
        for(Link link:LINKS)if(link.packet.start().equals(packet.start()) && link.packet.end().equals(packet.end())) {
            link.until=world.getGameTime()+packet.ticks();return;
        }
        if(LINKS.size()>=128)LINKS.removeFirst();
        LINKS.add(new Link(packet,world.getGameTime()));
    }
    public static void tick() {
        if(!ready())return;
        long now=world.getGameTime();
        NETWORKS.values().removeIf(packet->packet.expiresAt()<=now);
        var resolver=TendrilAnchor.forLevel(world);
        LINKS.removeIf(link->{
            link.previousStart=link.start;link.previousEnd=link.end;
            var start=link.packet.start().resolve(resolver);var end=link.packet.end().resolve(resolver);
            if(start.isPresent() && end.isPresent() && Double.isFinite(start.get().lengthSqr()) && Double.isFinite(end.get().lengthSqr())) {
                link.start=start.get();link.end=end.get();
                if(link.previousStart==null){link.previousStart=link.start;link.previousEnd=link.end;}
            } else link.until=Math.min(link.until,now+6);
            return now>=link.until;
        });
    }
    static void render(PoseStack p,LuxUmbraBatch batch,VertexConsumer iron,Vec3 camera,Vec3 right,Vec3 up,double time,float partial,boolean solids) {
        if(!ready())return;
        var flow=solids?FerricDuctilisGeometry.DISCARD:batch.vertices(LuxUmbraBatch.Material.DUCTILIS);
        for(var entity:world.entitiesForRendering())if(entity instanceof FerricConstructEntity ferric && ferric.isPlayerConstruct()
                && ferric.isAlive() && ferric.distanceToSqr(camera)<128*128) {
            Vec3 from=ferric.getPosition(partial);
            p.pushPose();p.translate(from.x-camera.x,from.y-camera.y,from.z-camera.z);
            FerricDuctilisGeometry.construct(ferric,p,iron,flow,camera.subtract(from),right,up,time);p.popPose();
        }
        if(solids)return;
        int faces=0;
        for(var network:NETWORKS.values())for(var pos:network.nodes()) {
            if(faces>=2048)break;
            Vec3 from=Vec3.atLowerCornerOf(pos);
            if(from.distanceToSqr(camera)>96*96 || !world.hasChunkAt(pos))continue;
            float fade=Mth.clamp((float)((network.expiresAt()-time)/8),0,1);
            p.pushPose();p.translate(from.x-camera.x,from.y-camera.y,from.z-camera.z);
            var fluid=world.getFluidState(pos);
            if(fluid.is(FluidTags.WATER)) {
                if(!world.getFluidState(pos.above()).is(FluidTags.WATER)) {
                    double y=fluid.getHeight(world,pos)+.006;
                    FerricDuctilisGeometry.quad(p,flow,new Vec3(0,y,0),new Vec3(1,y,0),new Vec3(1,y,1),new Vec3(0,y,1),2,pos.hashCode(),0xFFE9A8,fade);faces++;
                }
            } else {
                var state=world.getBlockState(pos);
                if(!com.vincenthuto.hemomancy.common.manipulation.ductilis.ConductionManager.isConductor(world,pos)) {p.popPose();continue;}
                for(var box:state.getShape(world,pos).toAabbs())for(Direction side:Direction.values()) {
                    if(faces>=2048)break;
                    if(world.getBlockState(pos.relative(side)).isFaceSturdy(world,pos.relative(side),side.getOpposite()))continue;
                    double x0=box.minX-.003,x1=box.maxX+.003,y0=box.minY-.003,y1=box.maxY+.003,z0=box.minZ-.003,z1=box.maxZ+.003;
                    Vec3 a,b,c,d;
                    switch(side.getAxis()) {
                        case Y -> {double y=side==Direction.UP?y1:y0;a=new Vec3(x0,y,z0);b=new Vec3(x1,y,z0);c=new Vec3(x1,y,z1);d=new Vec3(x0,y,z1);}
                        case X -> {double x=side==Direction.EAST?x1:x0;a=new Vec3(x,y0,z0);b=new Vec3(x,y0,z1);c=new Vec3(x,y1,z1);d=new Vec3(x,y1,z0);}
                        default -> {double z=side==Direction.SOUTH?z1:z0;a=new Vec3(x0,y0,z);b=new Vec3(x1,y0,z);c=new Vec3(x1,y1,z);d=new Vec3(x0,y1,z);}
                    }
                    FerricDuctilisGeometry.quad(p,flow,a,b,c,d,2,pos.hashCode()+side.ordinal(),0xFFE4A0,fade);faces++;
                }
            }
            p.popPose();
        }
        for(Link link:LINKS) {
            if(link.start==null || link.previousStart==null)continue;
            Vec3 start=link.previousStart.lerp(link.start,partial),end=link.previousEnd.lerp(link.end,partial);
            if(start.distanceToSqr(camera)>128*128 && end.distanceToSqr(camera)>128*128)continue;
            float fade=Mth.clamp((float)((link.until-time)/6),0,1);
            p.pushPose();p.translate(start.x-camera.x,start.y-camera.y,start.z-camera.z);
            FerricDuctilisGeometry.ribbon(p,flow,Vec3.ZERO,end.subtract(start),.055,.07,time,link.packet.hashCode(),camera.subtract(start),0xFFE9A6,fade);
            p.popPose();
        }
    }
    private static final class Link {
        final NerveLinkPacket packet;long until;Vec3 start,end,previousStart,previousEnd;
        Link(NerveLinkPacket packet,long now){this.packet=packet;until=now+packet.ticks();}
    }
}
