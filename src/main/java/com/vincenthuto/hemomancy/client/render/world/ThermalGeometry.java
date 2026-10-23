package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.BombardierFlameGeometry;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraBatch.Material.*;

/** Material-specific silhouettes: gas sheets, shallow pools, thin glass plates and heavy cruor. */
final class ThermalGeometry {
    private static final int WHITE=0xFFFFFF;
    /** Cruor shell shape: translucent frost, used for strands, rings and clock hands. */
    static final int FROST_STRAND=4;
    private ThermalGeometry() {}

    static boolean handles(Form form) {
        return switch(form) {
            case FURNACE,IGNITION,BOMBARDIER_SWEEP,GLASS,GLASS_CHARGE,UPDRAFT,FORGE,CAUTERIZE,
                    FLAME_CONJURE,FROST_CONJURE,ICE,ICE_CHARGE,BONE,STILLNESS,HOUR,HOUR_BREAK,
                    CRYOGENIC_PULSE,CRUOR_FORM,CRUOR_BREAK,FROZEN_VEINS,RIMEBOUND,CRUOR_SURFACE,FROST_ADVANCE -> true;
            default -> false;
        };
    }

    static boolean cold(Form form) {
        return switch(form) {
            case ICE,ICE_CHARGE,BONE,STILLNESS,HOUR,HOUR_BREAK,CRYOGENIC_PULSE,CRUOR_FORM,CRUOR_BREAK,
                    FROZEN_VEINS,RIMEBOUND,FROST_CONJURE,CRUOR_SURFACE,FROST_ADVANCE -> true;
            default -> false;
        };
    }

    static void draw(ManipulationVisualPacket packet,PoseStack p,LuxUmbraBatch batch,VertexConsumer core,
            Vec3 from,Vec3 camera,Vec3 right,Vec3 up,double time,float age,float formation,float opacity,
            ThermalSurface surface,ThermalFragments fragments,float partial,boolean particles) {
        if(opacity<=.002)return;
        Form form=packet.form();
        int seed=packet.entityId()>=0?packet.entityId():packet.from().hashCode();
        double r=Math.min(8,Math.max(.2,packet.radius()));
        var flame=batch.vertices(FLAME);var frost=batch.vertices(CRUOR);
        switch(form) {
            case BOMBARDIER_SWEEP -> {
                Vec3 ray = packet.to().subtract(packet.from());
                double length = Math.min(10, ray.length());
                if (length < .01) break;
                Vec3 direction = ray.normalize();
                int count = BombardierFlameGeometry.visualSampleCount(length);
                for (int i = 0; i < count; i++) {
                    double along = BombardierFlameGeometry.visualSampleDistance(length, i, count);
                    double curl = Math.sin(i * 2.39996 + time * .08) * (.045 + along / length * .055);
                    Vec3 at = direction.scale(along).add(right.scale(curl));
                    double taper = 1.0 - along / length * .55;
                    fire(p, flame, at, right, up, (.16 + random(seed, i) * .11) * taper,
                            (.42 + random(seed, i + 37) * .38) * taper, seed + i, opacity);
                    if (particles && i % 3 == 0)
                        smoke(p, flame, at.add(0, .15, 0), right, up, .18 * taper, .34 * taper,
                                seed + i + 91, opacity * .35F);
                }
            }
            case FURNACE,IGNITION -> {
                boolean burst=form==Form.IGNITION || packet.entityId()<0;
                float spread=burst?Mth.clamp(age/13,0,1):formation;
                pool(p,batch,from,surface,spread,opacity,seed);
                int count=burst?18:14;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996;
                    double distance=r*(.22+.7*random(seed,i))*spread;
                    Vec3 root=surface.onSurface(from,new Vec3(Math.cos(a)*distance,.06,Math.sin(a)*distance));
                    double cycle=burst?age*.07:(time*.075+i*.29)%1;
                    // Tongues rise from the ground and lean away from the source, a burst throwing them high and wide.
                    double height=(.55+random(seed,i+12)*.75)*(burst?1.25*Math.min(1,age/5.0):.85+.25*Math.sin(time*.3+i));
                    Vec3 lean=new Vec3(Math.cos(a),0,Math.sin(a)).scale(burst?.35:.12);
                    flameRibbon(p,flame,tongue(root,lean,height,time,seed+i),camera,.25+random(seed,i+41)*.22,seed+i,opacity);
                    if(particles && i%3==0)smoke(p,flame,root.add(.15,1.0+cycle*.6,0),right,up,.42,.72,seed+i,opacity*.45f);
                }
            }
            case GLASS,GLASS_CHARGE -> {
                boolean charge=form==Form.GLASS_CHARGE;
                Vec3 focus=charge?packet.to().subtract(packet.from()).normalize().scale(1.3):Vec3.ZERO;
                int count=charge?11:30;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996,y=1-2*(i+.5)/count,h=Math.sqrt(1-y*y);
                    Vec3 direction=new Vec3(Math.cos(a)*h,y,Math.sin(a)*h);
                    Vec3 at=charge?focus.add(direction.scale(.42+(.22+.06*Math.sin(time*.04+i))*(1-packet.radius()))):
                            fragments.offset(i,partial);
                    p.pushPose();p.translate(at.x,at.y,at.z);
                    p.mulPose(Axis.YP.rotation((float)(a+(charge?time*.018:age*(.04+random(seed,i)*.07)))));
                    p.mulPose(Axis.XP.rotation((float)(y*2+(charge?.2:age*.065))));
                    double size=(charge?.2:.25)+random(seed,i+7)*(charge?.16:.27);
                    shard(p,batch.vertices(GLASS),size,seed+i,opacity);
                    p.popPose();
                }
                if(charge || age<12) {
                    float flash=opacity*(charge?.65f:1-Mth.clamp(age/12,0,1));
                    fire(p,flame,focus,right,up,charge?.28:r*.5,charge?.45:r*.65,seed,flash);
                }
                if(particles && !charge)smoke(p,flame,focus.add(0,age*.025,0),right,up,r*.45,r*.55,seed+24,opacity*.45f);
            }
            case UPDRAFT -> {
                int mode=packet.count();
                boolean narrow=mode==2,hover=mode==3,blast=mode==4;
                for(int i=0;i<(blast?14:8);i++) {
                    double a=i*2.39996,spread=blast?Math.min(3.8,age*.18):narrow?.18:.4;
                    Vec3 at=new Vec3(Math.cos(a)*spread,-.25+((time*.1+i*.19)%1)*.55,Math.sin(a)*spread);
                    // Tongues spiral up the column, twisting as they climb and drawing in toward the axis.
                    double height=(narrow?1.7:hover?1.25:1.0)*1.2,twist=blast?.6:1.6;
                    List<Vec3> spine=new ArrayList<>(7);
                    for(int k=0;k<=6;k++) {
                        double t=k/6.0,turn=a+t*twist+time*.05,radius=spread*(1-.35*t);
                        spine.add(new Vec3(Math.cos(turn)*radius,-.25+t*height,Math.sin(turn)*radius));
                    }
                    flameRibbon(p,flame,spine,camera,narrow?.24:blast?.45:.36,seed+i,opacity);
                    if(particles && i%2==0)smoke(p,flame,at.add(0,.8,0),right,up,.4,.8,seed+i,opacity*.36f);
                    if(blast && age<34) {
                        Vec3 drop=fragments.offset(i,partial).add(0,.35,0);
                        p.pushPose();p.translate(drop.x,drop.y,drop.z);
                        p.scale(.7f,1.15f,.7f);
                        chunk(p,batch.vertices(MOLTEN),.085+random(seed,i)*.05,seed+i,opacity,1);
                        p.popPose();
                    }
                }
            }
            case FORGE -> {
                p.pushPose();p.translate(0,.95,0);
                p.pushPose();p.mulPose(Axis.YP.rotation((float)(time*.013)));
                chunk(p,batch.vertices(MOLTEN),.42,seed,opacity,2);
                p.popPose();
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI*.5+time*.04;
                    LuxUmbraGeometry.ground(p,batch.vertices(MOLTEN),new Vec3(Math.cos(a)*.14,i*.035,Math.sin(a)*.14),.48,0,seed+i,WHITE,opacity);
                    fire(p,flame,new Vec3(Math.cos(a)*.27,.04,Math.sin(a)*.27),right,up,.18,.46,seed+i,opacity);
                }
                p.popPose();
            }
            case CAUTERIZE -> {
                // A molten seam closes up the body while short tongues lick off it and gutter out.
                float seal=1-Mth.clamp(age/24,0,1);
                List<Vec3> seam=new ArrayList<>(6);
                for(int i=0;i<5;i++) {
                    double a=i*2.39996;
                    Vec3 at=new Vec3(Math.cos(a)*.3,.4+i*.23,Math.sin(a)*.3);
                    seam.add(at);
                    Vec3 lean=new Vec3(Math.cos(a),0,Math.sin(a)).scale(.25);
                    flameRibbon(p,flame,tongue(at,lean,.32*seal,time,seed+i),camera,.1,seed+i,opacity*seal);
                    if(particles)smoke(p,flame,at.add(0,age*.017,0),right,up,.14,.34,seed+i,opacity*.45f);
                }
                LuxUmbraGeometry.ribbonPath(p,batch.vertices(MOLTEN),seam,camera,.05*seal,.02*seal,0,seed+9,WHITE,opacity*seal);
            }
            case FLAME_CONJURE -> {
                int count=7;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996+time*.016;
                    Vec3 at=new Vec3(Math.cos(a)*.34,.35+(i%3)*.32,Math.sin(a)*.34);
                    // Small tongues lick up round the body, leaning out from it.
                    flameRibbon(p,flame,tongue(at,new Vec3(Math.cos(a),0,Math.sin(a)).scale(.2),.32,time,seed+i),camera,.1,seed+i,opacity*.8f);
                    if(particles)smoke(p,flame,at.add(0,.4,0),right,up,.16,.36,seed+i,opacity*.25f);
                }
            }
            case STILLNESS,ICE,CRYOGENIC_PULSE -> {
                boolean still=form==Form.STILLNESS;
                double spread=still?formation:Math.min(1,age/12.0);
                cracks(p,frost,from,surface,(float)spread,opacity,seed);
                int count=still?11:16;
                Vec3[] held=new Vec3[count];
                for(int i=0;i<count;i++) {
                    double a=i*2.39996,d=r*(.22+.68*random(seed,i));
                    double expansion=still?spread:1-Math.exp(-age*.1);
                    Vec3 at=new Vec3(Math.cos(a)*d*expansion,.22+(i%4)*.12+(still?(i%3)*.35:0),Math.sin(a)*d*expansion);
                    if(still)at=at.add(0,Math.sin(time*.013+i)*.025,0);
                    held[i]=at;
                    double size=(.18+random(seed,i+9)*.25)*formation;
                    clot(p,core,frost,at,size,seed+i,still?i*.7:age*.013+i,opacity);
                    if(particles)powder(p,frost,at.add(0,.02,0),right,up,.63+(still?.1:age*.009),seed+i,opacity*.9f);
                }
                if(still)lattice(p,frost,held,camera,seed,opacity*formation);
                else frostFingers(p,frost,camera,r*(1-Math.exp(-age*.1)),seed,opacity);
            }
            case ICE_CHARGE -> {
                Vec3 focus=packet.to().subtract(packet.from()).normalize().scale(1.25);
                for(int i=0;i<7;i++) {
                    double a=i*2.39996+time*.01,d=.55-packet.radius()*.24;
                    Vec3 at=focus.add(Math.cos(a)*d,Math.sin(i*1.7)*d,Math.sin(a)*d);
                    clot(p,core,frost,at,.11+packet.radius()*.1,seed+i,i+time*.012,opacity);
                    if(particles)powder(p,frost,at,right,up,.2,seed+i,opacity*.5f);
                }
            }
            case RIMEBOUND -> shackles(p,core,frost,camera,right,up,formation,opacity,seed,particles);
            case BONE,FROZEN_VEINS,FROST_CONJURE -> {
                boolean bloom=form==Form.BONE;
                int count=bloom?8:4;
                double size=bloom?.19:.085;
                for(int i=0;i<count;i++) {
                    double a=i*2.39996;
                    double growth=bloom?Math.min(1,age/10.0):formation;
                    double distance=.3+(bloom?Math.max(0,age-12)*.016:0);
                    Vec3 at=new Vec3(Math.cos(a)*distance,.25+(i%5)*.29,Math.sin(a)*distance);
                    if(form!=Form.FROZEN_VEINS)clot(p,core,frost,at,size*growth,seed+i,a+age*.009,opacity);
                    if(particles)powder(p,frost,at.add(0,-age*.001,0),right,up,.23,seed+i,opacity*.6f);
                }
            }
            case CRUOR_FORM,CRUOR_BREAK,FROST_ADVANCE -> {
                boolean breaking=form==Form.CRUOR_BREAK;
                double delay=form==Form.FROST_ADVANCE?Math.min(16,packet.from().distanceTo(packet.to())*1.4):0;
                if(age<delay)return;
                float growth=formation*Mth.clamp((float)(age-delay)/8,0,1);
                for(int i=0;i<7;i++) {
                    double a=i*2.39996;
                    Vec3 at=new Vec3(Math.cos(a)*.31,.2+(i%3)*.18,Math.sin(a)*.31);
                    if(breaking)at=at.add(fragments.offset(i,partial));
                    else at=at.scale(1.4-.4*growth);
                    clot(p,core,frost,at,(.16+random(seed,i)*.16)*(breaking?1:growth),seed+i,a+age*(breaking?.025:.006),opacity);
                    if(particles)powder(p,frost,at,right,up,.27,seed+i,opacity*.7f);
                }
            }
            case CRUOR_SURFACE -> blockFaces(p,frost,packet.count(),seed,opacity);
            case HOUR,HOUR_BREAK -> {
                boolean broken=form==Form.HOUR_BREAK;
                // HOUR.radius is debt, never a physical radius.
                double burden=Math.min(1,packet.radius()/30.0);
                // A clock of frozen blood stands around the body, facing whoever watches it.
                Vec3 face=new Vec3(0,1.15,0);
                for(int i=0;i<12;i++) {
                    double a=i*Math.PI/6;
                    Vec3 at=face.add(right.scale(Math.cos(a)*.72)).add(up.scale(Math.sin(a)*.72));
                    if(broken)at=at.add(fragments.offset(i,partial));
                    clot(p,core,frost,at,(i%3==0?.08:.05)+burden*.05,seed+i,a+(broken?age*.045:0),opacity);
                    if(particles && i%3==0)powder(p,frost,at,right,up,.14+burden*.1,seed+i,opacity*.35f);
                }
                if(!broken) {
                    List<Vec3> rim=new ArrayList<>(25);
                    for(int i=0;i<=24;i++) {
                        double a=i*Math.PI/12;
                        rim.add(face.add(right.scale(Math.cos(a)*.72)).add(up.scale(Math.sin(a)*.72)));
                    }
                    LuxUmbraGeometry.ribbonPath(p,frost,rim,camera,.025,FROST_STRAND,seed+20,WHITE,opacity*.6f);
                    // The hour hand points at the debt held; the minute hand keeps turning.
                    double hour=Math.PI/2-burden*Math.PI*2,minute=Math.PI/2-time*.02;
                    Vec3 hourTip=face.add(right.scale(Math.cos(hour)*.4)).add(up.scale(Math.sin(hour)*.4));
                    Vec3 minuteTip=face.add(right.scale(Math.cos(minute)*.6)).add(up.scale(Math.sin(minute)*.6));
                    LuxUmbraGeometry.ribbonPath(p,frost,List.of(face,hourTip),camera,.05,.012,FROST_STRAND,seed+21,WHITE,opacity);
                    LuxUmbraGeometry.ribbonPath(p,frost,List.of(face,minuteTip),camera,.035,.008,FROST_STRAND,seed+22,WHITE,opacity);
                    clot(p,core,frost,face,.07,seed+23,time*.01,opacity);
                }
            }
            default -> {}
        }
    }

    /** Frost creeping out across the ground as branching fingers, the freeze front of a grasp or pulse. */
    private static void frostFingers(PoseStack p,VertexConsumer frost,Vec3 camera,double reach,int seed,float alpha) {
        if(reach<.05)return;
        for(int finger=0;finger<7;finger++) {
            double a=finger*Math.PI*2/7+random(seed,finger+60)*.5;
            double length=reach*(.6+.4*random(seed,finger+70));
            List<Vec3> path=new ArrayList<>(7);
            for(int i=0;i<=6;i++) {
                double t=i/6.0,wander=Math.sin(t*5+finger)*.12*t;
                path.add(new Vec3(Math.cos(a+wander)*length*t,.03,Math.sin(a+wander)*length*t));
            }
            LuxUmbraGeometry.ribbonPath(p,frost,path,camera,.1,.012,FROST_STRAND,seed+80+finger,WHITE,alpha*.8f);
            Vec3 fork=path.get(3);
            double b=a+(finger%2==0?.6:-.6);
            LuxUmbraGeometry.ribbonPath(p,frost,List.of(fork,fork.add(Math.cos(b)*length*.3,0,Math.sin(b)*length*.3)),camera,
                    .05,.008,FROST_STRAND,seed+90+finger,WHITE,alpha*.7f);
        }
    }

    /** Frozen strands joining each suspended clot to its two nearest neighbours, so the stillness reads as a held lattice. */
    private static void lattice(PoseStack p,VertexConsumer frost,Vec3[] held,Vec3 camera,int seed,float alpha) {
        for(int i=0;i<held.length;i++) {
            int first=-1,second=-1;
            for(int j=0;j<held.length;j++) {
                if(j==i)continue;
                double d=held[i].distanceToSqr(held[j]);
                if(first<0 || d<held[i].distanceToSqr(held[first])){second=first;first=j;}
                else if(second<0 || d<held[i].distanceToSqr(held[second]))second=j;
            }
            for(int j:new int[]{first,second})if(j>i)
                LuxUmbraGeometry.ribbonPath(p,frost,List.of(held[i],held[j]),camera,.03,.03,FROST_STRAND,seed+100+i*3+j,WHITE,alpha*.7f);
        }
    }

    /** Rimebound shackles: rings of ice at the ankles and the hands, chained down to frozen anchors in the ground. */
    private static void shackles(PoseStack p,VertexConsumer core,VertexConsumer frost,Vec3 camera,Vec3 right,Vec3 up,
            float formation,float alpha,int seed,boolean particles) {
        double[] heights={.18,1.0},radii={.36,.44};
        for(int ring=0;ring<2;ring++) {
            List<Vec3> loop=new ArrayList<>(13);
            for(int i=0;i<=12;i++) {
                double a=i*Math.PI/6+ring*.3;
                Vec3 at=new Vec3(Math.cos(a)*radii[ring],heights[ring]+Math.sin(a*2)*.02,Math.sin(a)*radii[ring]);
                loop.add(at);
                if(i<12 && i%2==0)clot(p,core,frost,at,.06*formation,seed+ring*20+i,a,alpha);
            }
            LuxUmbraGeometry.ribbonPath(p,frost,loop,camera,.05*formation,FROST_STRAND,seed+40+ring,WHITE,alpha);
        }
        for(int chain=0;chain<3;chain++) {
            double a=chain*Math.PI*2/3+seed;
            Vec3 cuff=new Vec3(Math.cos(a)*radii[0],heights[0],Math.sin(a)*radii[0]);
            Vec3 anchor=new Vec3(Math.cos(a)*.95,.05,Math.sin(a)*.95);
            clot(p,core,frost,anchor,.12*formation,seed+60+chain,a,alpha);
            for(int link=1;link<5;link++) {
                double t=link/5.0;
                Vec3 at=cuff.lerp(anchor,t).add(0,-Math.sin(t*Math.PI)*.08,0);
                clot(p,core,frost,at,.035*formation,seed+70+chain*5+link,a+link,alpha);
            }
            if(particles)powder(p,frost,anchor.add(0,.05,0),right,up,.2,seed+80+chain,alpha*.5f);
        }
    }

    private static void pool(PoseStack p,LuxUmbraBatch batch,Vec3 origin,ThermalSurface surface,float spread,float alpha,int seed) {
        int i=0;
        for(var patch:surface.patches()) {
            Vec3 at=patch.center().subtract(origin);
            if(at.horizontalDistance()>spread*8+.2){i++;continue;}
            LuxUmbraGeometry.ground(p,batch.vertices(MOLTEN),at,patch.halfWidth()*Math.max(.1,spread),0,seed+i++,WHITE,alpha);
        }
    }
    private static void cracks(PoseStack p,VertexConsumer v,Vec3 origin,ThermalSurface surface,float growth,float alpha,int seed) {
        int i=0;
        for(var patch:surface.patches())LuxUmbraGeometry.ground(p,v,patch.center().subtract(origin),patch.halfWidth(),2,seed+i++,WHITE,alpha*growth);
    }
    /**
     * The spine of a flame tongue rising from {@code root}: it leans by {@code lean} as it climbs and its upper half
     * sways, so successive tongues lick rather than stand.
     */
    static List<Vec3> tongue(Vec3 root,Vec3 lean,double height,double time,int seed) {
        List<Vec3> spine=new ArrayList<>(7);
        Vec3 across=lean.lengthSqr()<1e-6?new Vec3(1,0,0):new Vec3(-lean.z,0,lean.x).normalize();
        for(int i=0;i<=6;i++) {
            double t=i/6.0,sway=Math.sin(time*.3+seed*1.7+t*3)*.18*t*t*height;
            spine.add(root.add(0,t*height,0).add(lean.scale(t*t*height)).add(across.scale(sway)));
        }
        return spine;
    }

    /**
     * One continuous flame along a bent spine. The flame shader draws root to sooty tip along a card's height, so the
     * height coordinate runs down the spine instead of across it and the whole tongue bends as one flame.
     */
    static void flameRibbon(PoseStack p,VertexConsumer v,List<Vec3> spine,Vec3 camera,double width,int seed,float alpha) {
        if(spine.size()<2 || alpha<=.002 || spine.getFirst().distanceToSqr(spine.getLast())<1e-6)return;
        Vec3 previousLeft=null,previousRight=null,previousSide=null;
        double previousV=0;
        int phase=MaterialQuads.seedTile(seed)*2;
        for(int i=0;i<spine.size();i++) {
            Vec3 at=spine.get(i);
            Vec3 tangent=spine.get(Math.min(i+1,spine.size()-1)).subtract(spine.get(Math.max(0,i-1)));
            Vec3 side=tangent.lengthSqr()<1e-8?(previousSide==null?new Vec3(1,0,0):previousSide)
                    :LuxUmbraGeometry.facingSide(tangent.normalize(),camera.subtract(at));
            if(previousSide!=null && side.dot(previousSide)<0)side=side.scale(-1);
            Vec3 left=at.subtract(side.scale(width)),right=at.add(side.scale(width));
            double rise=i/(spine.size()-1.0);
            if(i>0) {
                vertex(p,v,previousLeft,0,(float)(phase+previousV),alpha);
                vertex(p,v,previousRight,1,(float)(phase+previousV),alpha);
                vertex(p,v,right,1,(float)(phase+rise),alpha);
                vertex(p,v,left,0,(float)(phase+rise),alpha);
            }
            previousLeft=left;previousRight=right;previousSide=side;previousV=rise;
        }
    }

    static void fire(PoseStack p,VertexConsumer v,Vec3 bottom,Vec3 right,Vec3 up,double width,double height,int seed,float alpha) {
        LuxUmbraGeometry.card(p,v,bottom.add(0,height*.75,0),right,up,width,height,0,seed,WHITE,alpha);
    }
    static void smoke(PoseStack p,VertexConsumer v,Vec3 at,Vec3 right,Vec3 up,double width,double height,int seed,float alpha) {
        LuxUmbraGeometry.card(p,v,at,right,up,width,height,1,seed,WHITE,alpha);
    }
    static void powder(PoseStack p,VertexConsumer v,Vec3 at,Vec3 right,Vec3 up,double size,int seed,float alpha) {
        LuxUmbraGeometry.card(p,v,at,right,up,size,size*.7,1,seed,WHITE,alpha);
    }

    static void shard(PoseStack p,VertexConsumer v,double size,int seed,float alpha) {
        Vec3[] outline={new Vec3(-.48,-.76,0),new Vec3(.24,-.52,0),new Vec3(.59,.34,0),
                new Vec3(.08,1,0),new Vec3(-.36,.22,0)};
        double thickness=size*.045;
        for(int i=0;i<outline.length;i++) {
            Vec3 a=outline[i].multiply(size*(.7+random(seed,0)*.6),size,1),b=outline[(i+1)%outline.length].multiply(size*(.7+random(seed,0)*.6),size,1);
            glassFace(p,v,new Vec3(0,0,thickness),a.add(0,0,thickness),b.add(0,0,thickness),size,seed,alpha);
            glassFace(p,v,new Vec3(0,0,-thickness),b.add(0,0,-thickness),a.add(0,0,-thickness),size,seed,alpha);
            quad(p,v,a.add(0,0,-thickness),b.add(0,0,-thickness),b.add(0,0,thickness),a.add(0,0,thickness),1,seed,alpha);
        }
    }
    private static void glassFace(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,Vec3 c,double size,int seed,float alpha) {
        for(Vec3 at:new Vec3[]{a,b,c,c})vertex(p,v,at,(float)(.5+at.x/(size*2.1)),
                (float)(MaterialQuads.seedTile(seed)*2+.45+at.y/(size*2.3)),alpha);
    }
    static void clot(PoseStack p,VertexConsumer core,VertexConsumer shell,Vec3 at,double size,int seed,double rotation,float alpha) {
        if(size<=.005)return;
        p.pushPose();p.translate(at.x,at.y,at.z);p.mulPose(Axis.YP.rotation((float)rotation));
        p.mulPose(Axis.ZP.rotation((float)(random(seed,4)*.8-.4)));
        chunk(p,core,size,seed,alpha,0);
        chunk(p,shell,size*1.018,seed,alpha,4);
        p.popPose();
    }
    static void chunk(PoseStack p,VertexConsumer v,double size,int seed,float alpha,int shape) {
        for(int i=0;i<6;i++) {
            double a=i*Math.PI/3,b=(i+1)*Math.PI/3;
            double ra=size*(.8+random(seed,i)*.4),rb=size*(.8+random(seed,(i+1)%6)*.4);
            Vec3 low=new Vec3(Math.cos(a)*ra,-size*.55,Math.sin(a)*ra),next=new Vec3(Math.cos(b)*rb,-size*.55,Math.sin(b)*rb);
            Vec3 high=low.multiply(.76,-1.35,.76).add(size*.13,0,0),highNext=next.multiply(.76,-1.35,.76).add(size*.13,0,0);
            quad(p,v,low,next,highNext,high,shape,seed+i,alpha);
            Vec3 top=new Vec3(size*.08,size*.95,-size*.11),bottom=new Vec3(-size*.13,-size*.8,0);
            quad(p,v,high,highNext,top,top,shape,seed+i,alpha);
            quad(p,v,next,low,bottom,bottom,shape,seed+i,alpha);
        }
    }
    static void blockFaces(PoseStack p,VertexConsumer v,int mask,int seed,float alpha) {
        for(var side:net.minecraft.core.Direction.values()) {
            if((mask&(1<<side.ordinal()))==0)continue;
            Vec3 normal=Vec3.atLowerCornerOf(side.getNormal());
            Vec3 right=side.getAxis()==net.minecraft.core.Direction.Axis.Y?new Vec3(1,0,0):new Vec3(-normal.z,0,normal.x);
            Vec3 up=normal.cross(right);
            LuxUmbraGeometry.card(p,v,new Vec3(0,.5,0).add(normal.scale(.503)),right,up,.5,.5,3,seed+side.ordinal(),WHITE,alpha);
        }
    }
    private static double random(int seed,int slot){return ThermalMotion.random(seed,slot);}
    private static void quad(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int shape,int seed,float alpha) {
        MaterialQuads.quad(v,p.last().pose(),a.x,a.y,a.z,b.x,b.y,b.z,c.x,c.y,c.z,d.x,d.y,d.z,shape,MaterialQuads.seedTile(seed),WHITE,alpha,0,1,false);
    }
    private static void vertex(PoseStack p,VertexConsumer v,Vec3 at,float u,float texV,float alpha) {
        MaterialQuads.vertex(v,p.last().pose(),at.x,at.y,at.z,WHITE,alpha,u,texV);
    }
}
