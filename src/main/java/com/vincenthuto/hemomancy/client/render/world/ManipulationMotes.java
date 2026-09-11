package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Bounded decorative mesh particles. None carry a hitbox, boundary, charge or status. */
final class ManipulationMotes {
    private enum Kind { DROP, FRAGMENT, VAPOR, RESIDUE }
    private record Mote(Vec3 origin, Vec3 velocity, ManipulationMaterials material, Kind kind,
            int color, long born, int life, float size, double seed) {}
    private static final List<Mote> MOTES=new ArrayList<>();
    private ManipulationMotes() {}

    static void clear() { MOTES.clear(); }

    static void emit(ManipulationVisualPacket packet, ClientLevel level, boolean expiry) {
        Form form=packet.form();
        if(AnimusMortemGeometry.handles(form))return;
        if(FerricDuctilisGeometry.handles(form))return;
        if(ThermalGeometry.handles(form))return;
        if (LuxUmbraGeometry.handles(form)) {
            if (expiry) LuxUmbraEffects.mist(packet.from(), new Vec3(0, .012, 0),
                    LuxUmbraGeometry.umbra(form), Math.min(.45f, packet.radius() * .18f), 18);
            return;
        }
        Kind kind;
        int color;
        if(expiry) {
            if(form==Form.ORE)return;
            kind=switch(ManipulationMaterials.forForm(form)) {
                case FERRIC,CONGEATIO -> Kind.FRAGMENT;
                case MORTEM,TENEBRIS -> Kind.RESIDUE;
                case FLAMMEUS -> Kind.VAPOR;
                default -> Kind.DROP;
            };
            color=switch(ManipulationMaterials.forForm(form)) {
                case FERRIC -> 0xAB8982;
                case CONGEATIO -> 0xAECBD9;
                case MORTEM,TENEBRIS -> 0x80475E;
                case LUX -> 0xD9A2AC;
                default -> 0xA91C35;
            };
        } else {
            switch(form) {
                case SWORD_IMPACT, RUPTURE, DRAIN -> {kind=Kind.DROP;color=0xA91C35;}
                case GLASS -> {kind=Kind.FRAGMENT;color=0xF7B599;}
                case FURNACE, CAUTERIZE, PHOENIX -> {kind=Kind.VAPOR;color=0xB76D6A;}
                case BELL, BLOOM -> {kind=Kind.RESIDUE;color=0x84724F;}
                default -> {return;}
            }
        }
        Vec3 origin=packet.from();
        if(packet.entityId()>=0) {
            var entity=level.getEntity(packet.entityId());if(entity==null)return;
            origin=entity.position();
        }
        long now=level.getGameTime();
        for(int i=0;i<6;i++) {
            double angle=i*2.39996+(origin.x+origin.z)*.13;
            double radius=expiry?Math.min(4,packet.radius())*(.3+i*.1):.18;
            Vec3 at=origin.add(Math.cos(angle)*radius,expiry?.15:.3,Math.sin(angle)*radius);
            Vec3 velocity=new Vec3(Math.cos(angle)*.025,kind==Kind.VAPOR?.025:.04,Math.sin(angle)*.025);
            if(MOTES.size()>=128)MOTES.removeFirst();
            MOTES.add(new Mote(at,velocity,ManipulationMaterials.forForm(form),kind,color,now,18+i*2,.03F+i*.005F,angle));
        }
    }

    static void render(PoseStack p, MultiBufferSource buffers, Vec3 camera, double time, ParticleStatus setting) {
        MOTES.removeIf(m->time-m.born>=m.life);
        if(setting==ParticleStatus.MINIMAL)return;
        for(int i=0;i<MOTES.size();i++) {
            if(setting==ParticleStatus.DECREASED && i%2!=0)continue;
            Mote m=MOTES.get(i);
            if(m.origin.distanceToSqr(camera)>48*48)continue;
            double age=time-m.born,life=age/m.life;
            double fall=m.kind==Kind.VAPOR?0:age*age*.001;
            Vec3 at=m.origin.add(m.velocity.scale(age)).add(0,-fall,0).subtract(camera);
            float alpha=(float)(1-life)*.75F;
            var v=buffers.getBuffer(m.material.renderType());
            p.pushPose();p.translate(at.x,at.y,at.z);
            switch(m.kind) {
                case DROP -> VisceralMesh.drop(p,v,Vec3.ZERO,m.size,m.size*1.8,m.color,alpha,0);
                case FRAGMENT -> {
                    p.mulPose(Axis.YP.rotation((float)(m.seed+age*.08)));
                    p.mulPose(Axis.ZP.rotation((float)(m.seed+age*.045)));
                    VisceralMesh.plate(p,v,m.size,m.size*1.5,.008,m.color,alpha,m.seed);
                }
                case VAPOR -> VisceralMesh.strand(p,v,Vec3.ZERO,new Vec3(.05*Math.sin(age*.1+m.seed),.2+life*.15,0),
                        m.size*(1+life),.08,m.seed+age*.025,m.color,alpha*.24F);
                case RESIDUE -> VisceralMesh.plate(p,v,m.size,m.size*.7,.004,m.color,alpha,m.seed);
            }
            p.popPose();
        }
    }
}
