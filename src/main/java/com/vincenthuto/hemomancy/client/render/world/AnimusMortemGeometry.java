package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

/** Blood pressure, circulation and decay expressed with a small set of shader surfaces. */
final class AnimusMortemGeometry {
    private static final int TINT=0xFFFFFF, CORE=0xFFD2C4, CLOT=0x7A1020;
    private AnimusMortemGeometry() {}
    static boolean handles(Form form) {
        return switch(form) {
            case CLOUD,GROWTH,COMMAND,RUSH,RUPTURE,
                    MORTAR_CHARGE,DRAIN,WOUND,HUNGER,
                    GRAVE,BLACK_HEART,BLOOM,DEBT,ANIMUS_IMPACT,ANIMUS_CONJURE,MORTEM_CONJURE,
                    MORTEM_BURST,GRAVE_REFUND,HUNGER_COLLAPSE,BLACKHEART_RUPTURE,TITHE_RETURN,TITHE_COLLECT,
                    ROT_INFECTION,COMMUNION,SOVEREIGN_LASH -> true;
            default -> false;
        };
    }
    static boolean mortem(Form form){return ManipulationMaterials.forForm(form)==ManipulationMaterials.MORTEM;}

    static void draw(ManipulationVisualPacket packet,PoseStack p,VertexConsumer v,Vec3 camera,Vec3 right,Vec3 up,
            double time,float age,float formation,float alpha) {
        if(alpha<=0)return;
        Form form=packet.form();double r=packet.radius();int count=packet.count();
        int seed=Math.floorMod(packet.entityId()>=0?packet.entityId():packet.from().hashCode(),113);
        Vec3 end=packet.to().subtract(packet.from());
        switch(form) {
            case MORTAR_CHARGE -> {
                Vec3 aim=end.lengthSqr()<1e-8?new Vec3(0,0,1):end.normalize();
                Vec3 focus=ChargeVisualGeometry.focus(form,aim);
                // A heavy payload gathers: sheath, glossy core, clots drawn into orbit and drips it cannot yet hold.
                double size=.13+r*.23;
                card(p,v,focus,right,up,size*1.5,size*1.6,FOCUS,seed,TINT,alpha*.3f);
                card(p,v,focus,right,up,size,size*1.1,FOCUS,seed+1,TINT,alpha);
                card(p,v,focus.add(right.scale(-size*.3)).add(up.scale(size*.35)),right,up,size*.3,size*.3,FOCUS,seed+2,CORE,alpha*.8f);
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI*2/4+time*.045;
                    Vec3 from=focus.add(right.scale(Math.cos(a)*(.45-r*.12))).add(up.scale(Math.sin(a)*.32));
                    stream(p,v,from,focus,camera,.055,.1,i+time*.08,RIBBON,seed+i,TINT,alpha*.8f*(float)(1-r*.6));
                    double orbit=time*.3+i*Math.PI/2;
                    Vec3 clot=focus.add(right.scale(Math.cos(orbit)*size*1.5)).add(up.scale(Math.sin(orbit)*size*1.5));
                    card(p,v,clot,right,up,size*.22*r,size*.22*r,FOCUS,seed+10+i,i%2==0?CLOT:TINT,alpha);
                }
                for(int i=0;i<3;i++) {
                    double cycle=(time*.05+i*.33)%1;
                    Vec3 drip=focus.add(right.scale((i-1)*size*.4)).add(up.scale(-size-cycle*.6));
                    card(p,v,drip,right,up,.03,.05,FOCUS,seed+20+i,TINT,alpha*(float)(1-cycle)*(float)r);
                }
            }
            case CLOUD -> {
                // A cumulus of blood: a dark clotted underbelly, lighter billows above, a heart that beats inside it.
                double drift=time*(count==3?.032:.009);
                for(int i=0;i<8;i++) {
                    double a=i*2.39996+drift,distance=r*(i%3)*.22;
                    Vec3 at=new Vec3(Math.cos(a)*distance,Math.sin(a*1.7)*.18+(i%2)*.2,Math.sin(a)*distance);
                    card(p,v,at,right,up,r*.65,.35+r*.14,WISP,seed+i,i%2==0?CLOT:TINT,alpha*.6f);
                }
                float beat=(float)Math.pow(Math.max(0,Math.sin(time*.22+seed)),6);
                card(p,v,new Vec3(0,.1,0),right,up,r*.25+.1*beat,r*.25+.1*beat,FOCUS,seed+9,CORE,alpha*(.25f+.35f*beat));
                // Rain: drops fall from the underbelly, each trailing a short streak.
                for(int i=0;i<10;i++) {
                    double a=i*2.39996+seed,d=r*.7*((i*.37)%1),cycle=(time*.07+i*.19)%1;
                    Vec3 top=new Vec3(Math.cos(a)*d,-.15,Math.sin(a)*d),drop=top.add(0,-cycle*2.4,0);
                    ribbonPath(p,v,List.of(drop.add(0,.35,0),drop),camera,.005,.02,RIBBON,seed+20+i,TINT,alpha*.6f*(float)(1-cycle*.5));
                    card(p,v,drop,right,up,.025,.04,FOCUS,seed+40+i,TINT,alpha*.7f);
                }
            }
            case COMMAND -> {
                Vec3 crown=new Vec3(0,Math.min(2.6,r+.35),0);
                spiral(p,v,crown,camera,.34,.16,time*(count==1?.1:.025),count==3?3:2,seed,alpha);
                if(count<3 && end.lengthSqr()>.05)stream(p,v,crown,end,camera,.065,.18,time*.04,RIBBON,seed,TINT,alpha*.65f);
                // Control strings hang from the crown to the head and shoulders of the commanded body.
                double body=Math.max(1,crown.y-.45);
                Vec3[] holds={new Vec3(0,body,0),new Vec3(.28,body-.35,0),new Vec3(-.28,body-.35,0)};
                for(int i=0;i<holds.length;i++) {
                    double a=i*Math.PI*2/3+time*(count==1?.1:.025);
                    Vec3 top=crown.add(Math.cos(a)*.34,.08,Math.sin(a)*.34);
                    double tug=Math.sin(time*.15+i*2)*.04;
                    ribbonPath(p,v,List.of(top,top.lerp(holds[i],.5).add(tug,0,0),holds[i]),camera,.012,.008,RIBBON,seed+20+i,CLOT,alpha*.7f);
                }
            }
            case RUSH -> {
                spiral(p,v,new Vec3(0,.35,0),camera,.38,1.2,-age*.35,3,seed,alpha*.7f);
                // Blood pumps up the body in rings, one beat after another.
                for(int ring=0;ring<3;ring++) {
                    double cycle=(age*.06+ring/3.0)%1;
                    List<Vec3> loop=new ArrayList<>(13);
                    for(int i=0;i<=12;i++) {
                        double a=i*Math.PI/6;
                        loop.add(new Vec3(Math.cos(a)*.42,.15+cycle*1.6,Math.sin(a)*.42));
                    }
                    ribbonPath(p,v,loop,camera,.05,RIBBON,seed+10+ring,ring==0?CLOT:TINT,alpha*(float)Math.sin(cycle*Math.PI));
                }
            }
            case GROWTH -> {
                // Vein-roots climb from the ground up the body, branching and budding as they rise.
                double grow=Math.min(1,(age+1)/10.0);
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2+seed*.2;
                    List<Vec3> root=new ArrayList<>(9);
                    for(int j=0;j<=8;j++) {
                        double t=j/8.0*grow,turn=a+t*1.4;
                        root.add(new Vec3(Math.cos(turn)*.35*(1-t*.4),.03+t*1.3,Math.sin(turn)*.35*(1-t*.4)));
                    }
                    ribbonPath(p,v,root,camera,.05,.012,RIBBON,seed+i,TINT,alpha);
                    Vec3 fork=root.get(4),bud=fork.add(Math.cos(a+.9)*.22,.25*grow,Math.sin(a+.9)*.22);
                    ribbonPath(p,v,List.of(fork,fork.lerp(bud,.5).add(0,.04,0),bud),camera,.025,.006,RIBBON,seed+10+i,TINT,alpha*.85f);
                    card(p,v,bud,right,up,.035,.035,FOCUS,seed+20+i,CORE,alpha*(float)grow);
                }
            }
            case DRAIN,GRAVE_REFUND -> {
                stream(p,v,end,Vec3.ZERO,camera,.10,.18,time*.025,RIBBON,seed,TINT,alpha);
                for(int i=0;i<4;i++) {
                    double travel=(age*.065+i*.25)%1;
                    Vec3 at=end.lerp(Vec3.ZERO,travel);
                    card(p,v,at,right,up,.075,.12,FOCUS,seed+i,TINT,alpha*(float)Math.sin(travel*Math.PI));
                }
            }
            // The detailed fissures are on the posed model; out here each status moves its own way.
            case WOUND -> {
                // Dark drops fall from the wound and gather in a small pool.
                ground(p,v,new Vec3(0,.04,0),.28+.06*Math.sin(time*.02),POOL,seed,TINT,alpha*.55f);
                for(int i=0;i<4;i++) {
                    double a=i*2.39996+seed,cycle=(time*.03+i*.27)%1;
                    Vec3 from=new Vec3(Math.cos(a)*.3,1.1-i*.15,Math.sin(a)*.3);
                    Vec3 at=from.add(0,-cycle*cycle*(from.y-.05),0);
                    ribbonPath(p,v,List.of(at.add(0,.12*cycle+.02,0),at),camera,.006,.022,RIBBON,seed+i,TINT,alpha*.6f);
                    card(p,v,at,right,up,.035,.05,FOCUS,seed+10+i,TINT,alpha*.7f);
                }
            }
            case HUNGER -> {
                // Strands spiral inward to a starving core.
                Vec3 core=new Vec3(0,1.0,0);
                for(int i=0;i<4;i++) {
                    List<Vec3> spiral=new ArrayList<>(11);
                    double base=i*Math.PI/2+time*.03;
                    for(int j=0;j<=10;j++) {
                        double t=j/10.0,a=base+t*2.6,radius=.85*(1-t);
                        spiral.add(core.add(Math.cos(a)*radius,Math.sin(t*Math.PI)*.15-.2*(1-t),Math.sin(a)*radius));
                    }
                    ribbonPath(p,v,spiral,camera,.012,.04,RIBBON,seed+i,TINT,alpha*.5f);
                }
                card(p,v,core,right,up,.12,.12,WISP,seed+5,TINT,alpha*.45f);
            }
            case GRAVE -> {
                // Grave dust settles; a faint grave lies open under the feet.
                double turn=seed*.31;
                Vec3 along=new Vec3(Math.cos(turn),0,Math.sin(turn)),across=new Vec3(-along.z,0,along.x);
                List<Vec3> outline=new ArrayList<>(5);
                for(int corner=0;corner<=4;corner++) {
                    int c=corner%4;
                    outline.add(along.scale(c<2?.65:-.65).add(across.scale(c==0||c==3?.32:-.32)).add(0,.04,0));
                }
                ribbonPath(p,v,outline,camera,.025,RIBBON,seed,TINT,alpha*.45f);
                for(int i=0;i<6;i++) {
                    double a=i*2.39996+seed,cycle=(time*.012+i*.17)%1;
                    Vec3 at=new Vec3(Math.cos(a)*.45,1.6-cycle*1.5,Math.sin(a)*.45);
                    card(p,v,at,right,up,.05,.05,10,seed+10+i,TINT,alpha*.6f*(float)Math.sin(cycle*Math.PI));
                }
            }
            case ROT_INFECTION -> {
                // Spores puff up off the body over a patch of hyphae.
                ground(p,v,new Vec3(0,.04,0),.45,8,seed,TINT,alpha*.5f);
                for(int i=0;i<5;i++) {
                    double a=i*2.39996+seed,cycle=(time*.02+i*.2)%1;
                    Vec3 at=new Vec3(Math.cos(a)*(.25+cycle*.25),.4+cycle*1.1,Math.sin(a)*(.25+cycle*.25));
                    card(p,v,at,right,up,.07+cycle*.05,.07+cycle*.05,10,seed+10+i,TINT,alpha*.55f*(float)Math.sin(cycle*Math.PI));
                }
            }
            case BLACK_HEART -> {
                Vec3 heart=new Vec3(0,0,0);double saturation=Mth.clamp(r/12,0,1);
                double beat=(.7+saturation*.4)*(1+Math.sin(time*(.14+saturation*.12))*.07);
                card(p,v,heart,right,up,.26*beat,.32*beat,WISP,seed+5,TINT,alpha*.35f);
                card(p,v,heart,right,up,.18*beat,.24*beat,FOCUS,seed,TINT,alpha);
                spiral(p,v,heart.add(0,-.15,0),camera,.16+saturation*.16,.22+saturation*.25,time*.014,2,seed,alpha*.65f);
                // Clotted veins wrap the heart, thickening as it saturates.
                for(int vein=0;vein<3;vein++) {
                    List<Vec3> wrap=new ArrayList<>(9);
                    for(int i=0;i<=8;i++) {
                        double a=i*Math.PI/8-Math.PI/2+vein*2.1;
                        wrap.add(heart.add(right.scale(Math.cos(a)*.17*beat)).add(up.scale(Math.sin(a)*.22*beat)));
                    }
                    ribbonPath(p,v,wrap,camera,.012,.02+saturation*.02,RIBBON,seed+10+vein,CLOT,alpha);
                }
            }
            case DEBT -> {
                Vec3 clot=new Vec3(0,2.25,0);
                card(p,v,clot,right,up,.18,.25,FOCUS,seed,TINT,alpha);
                spiral(p,v,clot,camera,.38,.12,-time*.015,2,seed,alpha*.7f);
            }
            case COMMUNION -> {
                // A shared pool of blood under the feet the victims' streams feed.
                ground(p,v,new Vec3(0,.04,0),.45+.05*Math.sin(time*.03),POOL,seed+9,TINT,alpha*.6f);
                for(int i=0;i<3;i++) {
                    double a=i*2.39996+seed;
                    Vec3 foot=new Vec3(Math.cos(a)*.3,.05,Math.sin(a)*.3);
                    stream(p,v,foot,foot.add(0,.65,0),camera,.07,.05,time*.018+i,RIBBON,seed+i,TINT,alpha*.6f);
                }
            }
            case BLOOM -> {
                double spread=Math.max(.1,r*Math.min(1,(age+1)/12.0));

                for(int i=0;i<7;i++) {
                    double a=i*2.39996+seed;
                    Vec3 at=new Vec3(Math.cos(a)*spread*.65,.25+Math.sin(age*.07+i)*.12,Math.sin(a)*spread*.65);
                    card(p,v,at,right,up,.40,.50,WISP,seed+i,TINT,alpha*.55f);
                }
            }
            case RUPTURE,ANIMUS_IMPACT,MORTEM_BURST,BLACKHEART_RUPTURE -> {
                double spread=Math.max(.05,Math.min(r,4)*Math.min(1,(age+1)/8.0));
                // A hot core flashes, then tapered streaks and clotted beads burst outward.
                if(age<4)card(p,v,new Vec3(0,.2,0),right,up,.12+.1*(1-age/4),.12+.1*(1-age/4),FOCUS,seed+30,CORE,alpha*(1-age/4f));
                for(int i=0;i<8;i++) {
                    double a=i*2.39996+seed;
                    Vec3 at=new Vec3(Math.cos(a)*spread,.18+Math.sin(i*2.1)*.2+age*.012,Math.sin(a)*spread);
                    ribbonPath(p,v,List.of(at.scale(.45),at.scale(.75),at),camera,.012,.07,RIBBON,seed+i,i%3==0?CLOT:TINT,alpha);
                    card(p,v,at,right,up,.065,.10,FOCUS,seed+i,i%3==0?CLOT:TINT,alpha);
                }
            }
            case SOVEREIGN_LASH -> {
                // A whip of sinew lashes from the caster to a mob it turns, snaps taut and fades; the crown flares once.
                double lash=Mth.clamp(age/4.0,0,1),slack=Math.max(0,1-age/6.0);
                Vec3 side=end.lengthSqr()<1e-6?new Vec3(1,0,0):end.cross(new Vec3(0,1,0)).normalize();
                List<Vec3> whip=new ArrayList<>(13);
                for(int i=0;i<=12;i++) {
                    double t=i/12.0*lash;
                    whip.add(end.scale(t).add(side.scale(Math.sin(t*Math.PI*3-age)*.25*slack*t)).add(0,Math.sin(t*Math.PI)*.4*slack,0));
                }
                ribbonPath(p,v,whip,camera,.06,.015,RIBBON,seed,TINT,alpha);
                ribbonPath(p,v,whip,camera,.018,.005,RIBBON,seed+1,CLOT,alpha);
                if(lash>=1)card(p,v,end,right,up,.18*slack+.06,.18*slack+.06,FOCUS,seed+2,CORE,alpha*(float)slack);
                if(age<6)card(p,v,new Vec3(0,.85,0),right,up,.3*(1-age/6),.3*(1-age/6),FOCUS,seed+3,CORE,alpha*(1-age/6f));
            }
            case ANIMUS_CONJURE,MORTEM_CONJURE -> {
                Vec3 aim=end.lengthSqr()>.001?end.normalize():new Vec3(0,1,0);
                Vec3 focus=aim.scale(.8);
                // Blood is drawn to the hand from around the body and pulled out into the weapon's line.
                double draw=Math.min(1,age/8.0);
                for(int i=0;i<3;i++) {
                    double a=i*Math.PI*2/3+time*.05;
                    Vec3 from=new Vec3(Math.cos(a)*.45,.6+i*.25,Math.sin(a)*.45);
                    stream(p,v,from,focus,camera,.05,.12,time*.06+i,RIBBON,seed+i,TINT,alpha*(float)(1-draw*.6));
                }
                ribbonPath(p,v,List.of(focus,focus.add(aim.scale(.9*draw))),camera,.05,.006,RIBBON,seed+5,CLOT,alpha*(float)draw);
                card(p,v,focus,right,up,.1,.1,FOCUS,seed+6,CORE,alpha*.8f);
            }
            case HUNGER_COLLAPSE,TITHE_RETURN,TITHE_COLLECT -> {
                double shrink=Math.max(.02,1-age/20.0);Vec3 center=new Vec3(0,1,0);
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2+time*.015;
                    Vec3 from=center.add(Math.cos(a)*shrink*.7,(i%2-.5)*.4,Math.sin(a)*shrink*.7);
                    stream(p,v,from,center,camera,.10,.1,time*.015+i,RIBBON,seed+i,TINT,alpha);
                }
            }
            default -> throw new IllegalArgumentException("Not a blood form: "+form);
        }
    }
    private static void spiral(PoseStack p,VertexConsumer v,Vec3 center,Vec3 camera,double radius,double height,double phase,int arms,int seed,float alpha) {
        for(int arm=0;arm<arms;arm++) {
            var points=new ArrayList<Vec3>(13);
            for(int j=0;j<=12;j++) {double t=j/12.0,a=phase+arm*Math.PI*2/arms+t*3.0;
                points.add(center.add(Math.cos(a)*radius,t*height,Math.sin(a)*radius));}
            ribbonPath(p,v,points,camera,.075,RIBBON,seed+arm,TINT,alpha);
        }
    }
}
