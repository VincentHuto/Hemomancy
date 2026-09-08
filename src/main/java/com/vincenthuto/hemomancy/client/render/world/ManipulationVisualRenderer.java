package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

/** Solid silhouettes and translucent motion ribbons, independent of particle settings. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ManipulationVisualRenderer {
    private static final List<Cue> CUES = new ArrayList<>();
    private static ClientLevel world;
    private static final int BLOOD = 0xB40C38, EDGE = 0xFF6470, WHITE = 0xFFF3CF, ICE = 0x9BE9F4;

    private ManipulationVisualRenderer() {}

    public static void accept(ManipulationVisualPacket packet) {
        ClientLevel current = Minecraft.getInstance().level;
        if (world != current) { CUES.clear(); world = current; }
        if (current == null || !Float.isFinite(packet.radius()) || packet.radius() < 0 || packet.radius() > 100000
                || (packet.radius()>128 && packet.form()!=ManipulationVisuals.Form.DEBT && packet.form()!=ManipulationVisuals.Form.HOUR)
                || !finite(packet.from()) || !finite(packet.to())) return;
        long now = current.getGameTime();
        long born = now;
        boolean keyed = packet.entityId() >= 0 || packet.form() == ManipulationVisuals.Form.WELL
                || packet.form() == ManipulationVisuals.Form.BEACON || packet.form() == ManipulationVisuals.Form.ORE;
        if (keyed) {
            for (Cue cue : CUES) {
                if (sameSource(cue.packet, packet)) {
                    born = cue.born; break;
                }
            }
            CUES.removeIf(c -> sameSource(c.packet, packet));
        }
        if (packet.ticks() <= 0 || (packet.count() <= 0 && (packet.form() == ManipulationVisuals.Form.CROWN
                || packet.form() == ManipulationVisuals.Form.CHOIR || packet.form() == ManipulationVisuals.Form.WARD))) return;
        if (CUES.size() >= 192) CUES.removeFirst();
        CUES.add(new Cue(packet, born, now + Math.min(packet.ticks(), 12000)));
    }

    private static boolean finite(Vec3 v) {
        return Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }

    private static boolean sameSource(ManipulationVisualPacket a, ManipulationVisualPacket b) {
        return a.form() == b.form() && a.entityId() == b.entityId()
                && (a.entityId() >= 0 || a.from().equals(b.from()));
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        ClientLevel current = Minecraft.getInstance().level;
        if (world != current) { CUES.clear(); world = current; }
        if (current != null) CUES.removeIf(c -> c.until <= current.getGameTime());
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || world == null) return;
        var mc = Minecraft.getInstance();
        if (world != mc.level) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double time = world.getGameTime() + partial;
        Vec3 camera = event.getCamera().getPosition();
        // AFTER_LEVEL has already popped the world's camera matrix and composited clouds.
        PoseStack poses = new PoseStack();
        poses.mulPose(event.getModelViewMatrix());
        var buffers = mc.renderBuffers().bufferSource();
        for (Cue cue : CUES) {
            var packet = cue.packet;
            Vec3 from = packet.from();
            if (packet.entityId() >= 0) {
                var entity = world.getEntity(packet.entityId());
                if (entity == null || !entity.isAlive()) continue;
                if (packet.form() == ManipulationVisuals.Form.MARK && entity instanceof net.minecraft.world.entity.LivingEntity living
                        && !living.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.conductive_mark)) continue;
                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    var required=switch(packet.form()) {
                        case RETORT -> com.vincenthuto.hemomancy.common.init.EffectInit.iron_retort;
                        case HUNGER -> com.vincenthuto.hemomancy.common.init.EffectInit.insatiable_hunger;
                        case GRAVE -> com.vincenthuto.hemomancy.common.init.EffectInit.grave_debt;
                        case WOUND -> com.vincenthuto.hemomancy.common.init.EffectInit.blood_loss;
                        default -> null;
                    };
                    if(required!=null && !living.hasEffect(required))continue;
                }
                from = entity.getPosition(partial);
                if (packet.form().name().endsWith("_CHARGE")) from = from.add(0,entity.getEyeHeight(),0);
            }
            if (from.distanceToSqr(camera) > 128 * 128) continue;
            float age = (float) (time - cue.born);
            float fade = Mth.clamp((float) (cue.until - time) / 8, 0, 1);
            poses.pushPose();
            poses.translate(from.x - camera.x, from.y - camera.y, from.z - camera.z);
            draw(packet, poses, buffers.getBuffer(packet.form() == ManipulationVisuals.Form.ORE
                    ? HemoRenderTypes.MANIPULATION_LOCATOR : packet.form() == ManipulationVisuals.Form.CROWN
                    ? HemoRenderTypes.QLIPHOTH_CORE : HemoRenderTypes.QLIPHOTH_GLOW), time, age, fade);
            poses.popPose();
        }
        buffers.endBatch(HemoRenderTypes.QLIPHOTH_GLOW);
        buffers.endBatch(HemoRenderTypes.QLIPHOTH_CORE);
        buffers.endBatch(HemoRenderTypes.MANIPULATION_LOCATOR);
    }

    private static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v,
            double time, float age, float fade) {
        double r = packet.radius();
        Vec3 end = packet.to().subtract(packet.from());
        float grow = Mth.clamp(age / 6, .05F, 1);
        switch (packet.form()) {
            case PHOENIX_READY -> {
                for(int i=0;i<3;i++) {double a=time*.025+i*Math.PI*2/3;
                    Vec3 root=new Vec3(Math.cos(a)*.55,.4,Math.sin(a)*.55);
                    Vec3 tip=root.add(Math.cos(a)*.2,.55,Math.sin(a)*.2);
                    tube(p,v,root,tip,.045,0xF57236,fade*.7F);
                    tube(p,v,root.add(0,.2,0),tip.add(.1,-.13,0),.035,0xFFD6A0,fade*.6F);
                }
            }
            case IRON_HEART, BLACK_HEART -> {
                p.translate(0,1.25,.35);
                double beat=1+Math.sin(time*.22)*.06;
                p.scale((float)beat,(float)beat,(float)beat);
                int color=packet.form()==ManipulationVisuals.Form.IRON_HEART?0xD2C0B8:0x68364F;
                diamond(p,v,.22,.28,.08,color,fade*.65F);
                crystal(p,v,new Vec3(-.12,.12,0),.13,color,fade*.7F);
                crystal(p,v,new Vec3(.12,.12,0),.13,color,fade*.7F);
            }
            case COMMAND -> {
                p.translate(0,r+.35,0);
                int color=packet.count()==1?0xFF6273:packet.count()==2?0xF1C487:0xBA91EC;
                ring(p,v,.35,0,color,fade,.04);
                for(int i=0;i<3;i++) {double a=i*Math.PI*2/3;
                    tube(p,v,new Vec3(Math.cos(a)*.35,0,Math.sin(a)*.35),new Vec3(Math.cos(a)*.3,.3,Math.sin(a)*.3),.045,color,fade);}
                // The endpoint is refreshed from the controller, not a guessed target.
                tube(p,v,Vec3.ZERO,end.add(0,-r-.35,0),.012,color,fade*.3F);
            }
            case CIRCUIT -> {
                for(int i=0;i<6;i++) {double a=i*Math.PI/3+time*.09;
                    Vec3 at=new Vec3(Math.cos(a)*.45,1+Math.sin(a)*.25,Math.sin(a)*.45);
                    tube(p,v,at,at.add(0,.18,0),.035,0xA0FFE0,fade);}
            }
            case CAUTERIZE, RUSH -> {
                boolean seal=packet.form()==ManipulationVisuals.Form.CAUTERIZE;
                for(int i=0;i<5;i++) {
                    double y=.4+i*.23, gap=seal?Math.max(0,.35-age*.025):.3;
                    tube(p,v,new Vec3(-gap,y,.34),new Vec3(gap,y+.1,.34),.03,seal?0xFFAD6B:EDGE,fade);
                    if(!seal)tube(p,v,new Vec3(-.3,y,0),new Vec3(-.3,y-.15,0),.035,BLOOD,fade);
                }
            }
            case NEEDLE_CHARGE, FAN_CHARGE, LANCE_CHARGE -> {
                Vec3 direction=end.normalize(),side=direction.cross(new Vec3(0,1,0)).normalize();
                int count=packet.form()==ManipulationVisuals.Form.FAN_CHARGE?7:packet.form()==ManipulationVisuals.Form.LANCE_CHARGE?3:4;
                for(int i=0;i<count;i++) {
                    double offset=(i-(count-1)*.5)*.12;
                    Vec3 tip=direction.scale(1.3).add(side.scale(offset)).add(0,-.25,0);
                    Vec3 axis=packet.form()==ManipulationVisuals.Form.FAN_CHARGE?direction.add(side.scale(offset*2)):direction;
                    tube(p,v,tip.subtract(axis.scale((packet.form()==ManipulationVisuals.Form.LANCE_CHARGE?.85:.4)*r)),tip,.018,EDGE,fade);
                }
            }
            case MORTAR_CHARGE, ANEURYSM_CHARGE, ICE_CHARGE, WELL_CHARGE, LIGHTNING_CHARGE, IRON_CHARGE, GAZE_CHARGE -> {
                Vec3 focus=end.normalize().scale(1.25).add(0,-.25,0);
                p.translate(focus.x,focus.y,focus.z);
                int color=switch(packet.form()) {case ICE_CHARGE -> ICE;case WELL_CHARGE -> 0x9A478B;
                    case LIGHTNING_CHARGE -> 0xB1FFE3;case IRON_CHARGE -> 0xD6C0B9;default -> EDGE;};
                if(packet.form()==ManipulationVisuals.Form.MORTAR_CHARGE || packet.form()==ManipulationVisuals.Form.ANEURYSM_CHARGE)
                    crystal(p,v,Vec3.ZERO,(.08+.26*r)*(1+Math.sin(time*.3)*.08),BLOOD,fade);
                for(int i=0;i<6;i++) {double a=i*Math.PI/3+time*.05;
                    Vec3 at=new Vec3(Math.cos(a)*(.15+r*.25),Math.sin(a)*(.15+r*.25),0);
                    if(packet.form()==ManipulationVisuals.Form.ICE_CHARGE)crystal(p,v,at,.06+r*.07,color,fade);
                    else tube(p,v,at,at.add(Math.sin(a)*.12,Math.cos(a)*.12,0),.02,color,fade);
                }
            }
            case CLOUD -> {
                p.pushPose();p.translate(0,-10,0);boxBoundary(p,v,r,EDGE,fade*.45F);p.popPose();
                for(int i=0;i<18;i++) {
                    double x=Math.sin(i*13.7)*r,z=Math.cos(i*7.3)*r;
                    double fall=(time*.14+i*.57)%10;
                    tube(p,v,new Vec3(x,-fall,z),new Vec3(x,-Math.min(10,fall+.7),z),.018,BLOOD,fade*.7F);
                }
            }
            case MAGNET -> {
                ring(p,v,r,.08,0xCE9D9D,fade*.5F,.03);
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4, drift=1-(time*.035+i*.13)%1;
                    Vec3 at=new Vec3(Math.cos(a)*r*drift,.2,Math.sin(a)*r*drift);
                    tube(p,v,at,at.scale(.85),.04,0xB0A4A0,fade);
                }
            }
            case BLOOM -> {
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4;
                    Vec3 root=new Vec3(0,.15,0), left=new Vec3(Math.cos(a-.25)*r*.65,.5*grow,Math.sin(a-.25)*r*.65);
                    Vec3 tip=new Vec3(Math.cos(a)*r*grow,.15,Math.sin(a)*r*grow),right=new Vec3(Math.cos(a+.25)*r*.65,.5*grow,Math.sin(a+.25)*r*.65);
                    quad(p,v,root,left,tip,right,i%2==0?0x6D243A:0x8B5447,fade*.7F);
                    tube(p,v,root,tip,.025,0xB6B17C,fade);
                }
                crystal(p,v,new Vec3(0,.4,0),.4,0x94AA67,fade);
            }
            case RETORT -> {
                p.translate(0,1.1,0);diamond(p,v,.65,.7,.16,0xB4A9A5,fade*.5F);
                tube(p,v,new Vec3(-.4,-.2,.18),new Vec3(.4,.2,.18),.04,WHITE,fade);
            }
            case HUNGER -> {
                p.translate(0,1.3,0);
                for(int i=0;i<8;i++){double a=i*Math.PI/4;
                    Vec3 root=new Vec3(Math.cos(a)*.5,Math.sin(a)*.35,0);
                    tube(p,v,root,root.scale(.65),.055,0xC2C09B,fade);}
            }
            case GRAVE -> {
                p.translate(0,2.2,0);diamond(p,v,.25,.45,.08,0x738954,fade*.8F);
                tube(p,v,new Vec3(-.35,0,0),new Vec3(.35,0,0),.025,0xD0B599,fade);
            }
            case ORE -> {
                for(int y=0;y<2;y++) {p.pushPose();p.translate(0,y-.5,0);boxBoundary(p,v,.48,0xF0C985,fade*.55F);p.popPose();}
                for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})tube(p,v,new Vec3(x*.48,-.46,z*.48),new Vec3(x*.48,.54,z*.48),.015,WHITE,fade*.5F);
            }
            case VERDICT_CHARGE -> {
                Vec3 direction=end.normalize(), focus=direction.scale(1.15);
                orientedRing(p,v,focus,direction,.15+r*.55,WHITE,fade);
                orientedRing(p,v,focus.add(direction.scale(.3)),direction,.2+r*.35,0xFFBE71,fade*.5F);
                crystal(p,v,focus,.07+r*.15,WHITE,fade);
                tube(p,v,focus,end,.009,WHITE,fade*.35F);
            }
            case GLASS_CHARGE -> {
                Vec3 focus=end.normalize().scale(1.4);
                for(int i=0;i<8;i++) {double a=time*.08+i*Math.PI/4;
                    crystal(p,v,focus.add(Math.cos(a)*(.65-.3*r),Math.sin(a)*(.65-.3*r),0),.05+.18*r,0xFFB39B,fade);}
            }
            case CROWN_CHARGE -> {
                p.translate(0,-1.62,0);
                for(int i=0;i<Math.max(1,(int)Math.ceil(8*r));i++) {
                    Vec3 offset=ManipulationVisuals.swordOffset(time,i);
                    p.pushPose();p.translate(offset.x,offset.y,offset.z);p.scale(1,(float)Math.max(.05,r),1);
                    sword(p,v,fade*.4F);p.popPose();
                }
            }
            case BELL_CHARGE -> {
                p.translate(0,1,0);
                for(int i=0;i<6;i++) ring(p,v,(.2+i*.1)*Math.max(.1,r),.7-i*.15,0xAD8270,fade*.6F,.045);
            }
            case THREAD_CHARGE -> {
                Vec3 focus=end.normalize().scale(1.2);
                tube(p,v,focus.add(-.25,-.2,0),focus.add(.25,.2,0),.025,EDGE,fade);
                tube(p,v,focus.add(-.25,.2,0),focus.add(.25,-.2,0),.025,WHITE,fade);
            }
            case CROWN -> {
                for (int i = 0; i < Math.min(8, packet.count()); i++) {
                    Vec3 offset = ManipulationVisuals.swordOffset(time, i);
                    p.pushPose(); p.translate(offset.x, offset.y, offset.z);
                    p.mulPose(Axis.YP.rotation((float) (time * .025 + i * Math.PI / 4)));
                    p.scale(grow, grow, grow);
                    sword(p, v, fade); p.popPose();
                }
            }
            case VERDICT -> {
                tube(p,v,Vec3.ZERO,end,r*.18,WHITE,fade);
                tube(p,v,Vec3.ZERO,end,r*.42,0xFFD783,fade*.22F);
                for(int i=0;i<3;i++) {
                    double along = (age*.12+i/3.0)%1;
                    orientedRing(p,v,end.scale(along),end,r,0xFFE6A0,fade*.6F);
                }
                star(p,v,end,r*1.2,WHITE,fade);
            }
            case GLASS -> {
                for(int i=0;i<24;i++) {
                    double a=i*2.39996, y=1-2*(i+.5)/24;
                    Vec3 direction=new Vec3(Math.cos(a)*Math.sqrt(1-y*y),y,Math.sin(a)*Math.sqrt(1-y*y));
                    double expansion=r*Math.min(1,age/9.0);
                    Vec3 tip=direction.scale(expansion);
                    crystal(p,v,tip, .18+r*.06,0xFF9F89,fade);
                    tube(p,v,tip.scale(.6),tip,.025,0xFFDFAD,fade*.6F);
                }
                ring(p,v,r*Math.min(1,age/8.0),.03,0xFF6937,fade,.07);
            }
            case THREAD -> {
                Vec3 middle=end.scale(.5), kick=new Vec3(0,Math.min(1,age*.06),0);
                double gap=Math.min(.45,age/35.0);
                tube(p,v,Vec3.ZERO,middle.scale(1-gap).add(kick),.025,EDGE,fade);
                tube(p,v,middle.scale(1+gap).add(kick.scale(-1)),end,.025,EDGE,fade);
                tube(p,v,middle.add(-.6,-.6,0),middle.add(.6,.6,0),.06,WHITE,fade*Math.max(0,1-age/12));
            }
            case RUPTURE -> {
                double expansion=r*Math.min(1,age/8);
                for(int i=0;i<12;i++) {
                    double a=i*Math.PI/6;
                    Vec3 tip=new Vec3(Math.cos(a)*expansion,Math.sin(i*2)*expansion*.25,Math.sin(a)*expansion);
                    Vec3 branch=tip.scale(.6).add(0,.25,0);
                    tube(p,v,Vec3.ZERO,branch,.07,BLOOD,fade);
                    tube(p,v,branch,tip,.035,EDGE,fade);
                    crystal(p,v,tip,.14,BLOOD,fade);
                }
                ring(p,v,expansion,0,BLOOD,fade*.6F,.045);
            }
            case SWORD_IMPACT -> {
                for(int i=0;i<9;i++) {
                    double a=i*Math.PI*2/9;
                    Vec3 tip=new Vec3(Math.cos(a),.3+Math.sin(i*2)*.5,Math.sin(a)).scale(.15+age*.07);
                    tube(p,v,tip.scale(.4),tip,.055,BLOOD,fade);
                }
                star(p,v,Vec3.ZERO,.5,EDGE,fade*Math.max(0,1-age/10));
            }
            case WELL -> {
                boxBoundary(p,v,r,0xAC487F,fade*.45F);
                // Descending spiral ribbons feed a dark, faceted throat.
                ring(p,v,r,.04,0xAC487F,fade*.8F,.09);
                for(int arm=0;arm<5;arm++) {
                    Vec3 last=null;
                    for(int i=0;i<=36;i++) {
                        double t=i/36.0, a=arm*Math.PI*2/5+t*7-time*.08;
                        double radius=r*(1-t)*grow;
                        Vec3 next=new Vec3(Math.cos(a)*radius,.12+Math.sin(t*Math.PI)*.65,Math.sin(a)*radius);
                        if(last!=null) tube(p,v,last,next,.05+.13*t,arm%2==0?0x6B1F57:0x220D36,fade);
                        last=next;
                    }
                }
                crystal(p,v,new Vec3(0,.2,0),.5,0x17091F,fade);
            }
            case STILLNESS -> {
                boxBoundary(p,v,r,ICE,fade*.6F);
                for(int i=0;i<12;i++) {
                    double a=i*Math.PI/6;
                    crystal(p,v,new Vec3(Math.cos(a)*r,.25,Math.sin(a)*r),.38,ICE,fade);
                }
                ring(p,v,r*.97,.15+Math.sin(time*.04)*.1,ICE,fade*.4F,.025);
            }
            case BEACON -> {
                boxBoundary(p,v,r,WHITE,fade*.45F);
                ring(p,v,r,.04,0xFBD6B6,fade*.65F,.05);
                for(int i=0;i<4;i++) {
                    double a=i*Math.PI/2+time*.02;
                    tube(p,v,new Vec3(Math.cos(a)*.4,.1,Math.sin(a)*.4),new Vec3(0,2.5,0),.045,WHITE,fade);
                }
                crystal(p,v,new Vec3(0,1.5+Math.sin(time*.1)*.1,0),.32,EDGE,fade);
                ring(p,v,r*((time%20)/20),.06,EDGE,fade*.5F,.035);
            }
            case PHOENIX -> phoenix(p,v,age,fade);
            case BELL -> {
                double swing=Math.sin(age*.25)*.18;
                p.translate(0,1.8,0);p.mulPose(Axis.ZP.rotation((float)swing));
                for(int i=0;i<8;i++) {
                    double h=i*.17, width=.25+Math.pow(i/7.0,2)*.7;
                    ring(p,v,width,1.1-h,0x855554,fade,.11);
                }
                tube(p,v,new Vec3(0,1,0),new Vec3(swing,-.2,0),.08,0xD69683,fade);
                p.translate(0,-1.8,0);ring(p,v,r*Math.min(1,age/16),.1,EDGE,fade*.65F,.07);
            }
            case DRAIN, SUTURE -> {
                boolean drain=packet.form()==ManipulationVisuals.Form.DRAIN;
                for(int strand=0;strand<3;strand++) {
                    Vec3 last=Vec3.ZERO;
                    for(int i=1;i<=20;i++) {
                        double t=i/20.0, a=t*15+time*.2+strand*Math.PI*2/3;
                        Vec3 next=end.scale(t).add(Math.sin(a)*.12,Math.cos(a)*.12,0);
                        tube(p,v,last,next,.025,drain?BLOOD:WHITE,fade*.8F);last=next;
                    }
                    double along=(time*.06+strand/3.0)%1;
                    crystal(p,v,end.scale(drain?1-along:along),.12,drain?EDGE:WHITE,fade);
                }
            }
            case WARD, CHOIR -> {
                int count=packet.form()==ManipulationVisuals.Form.CHOIR?Math.min(3,packet.count()):Math.min(8,packet.count());
                for(int i=0;i<count;i++) {
                    double a=time*.035+i*Math.PI*2/Math.max(1,count);
                    p.pushPose();p.translate(Math.cos(a)*1.05,1.1,Math.sin(a)*1.05);
                    p.mulPose(Axis.YP.rotation((float)-a));
                    diamond(p,v,.36,.6,.1,packet.form()==ManipulationVisuals.Form.CHOIR?0xBAADAA:BLOOD,fade*.7F);
                    p.popPose();
                }
            }
            case FURNACE -> {
                boxBoundary(p,v,r,0xE96B37,fade*.35F);
                for(int i=0;i<8;i++) {
                    double a=i*Math.PI/4+time*.04;
                    Vec3 start=new Vec3(Math.cos(a)*.8,.1,Math.sin(a)*.8);
                    Vec3 tip=new Vec3(Math.cos(a+.45)*.65,1.2+Math.sin(time*.12+i)*.4,Math.sin(a+.45)*.65);
                    tube(p,v,start,tip,.12,0xC62C23,fade*.6F);
                    tube(p,v,start,tip,.035,0xFFD794,fade);
                }
            }
            case MARK -> {
                p.translate(0,1.2,0);
                for(int i=0;i<4;i++) {
                    double a=time*.035+i*Math.PI/2;
                    Vec3 a1=new Vec3(Math.cos(a)*.6,Math.sin(a)*.6,0);
                    Vec3 a2=new Vec3(Math.cos(a+.65)*.4,Math.sin(a+.65)*.4,0);
                    tube(p,v,a1,a2,.04,0x91E8DB,fade);
                }
                crystal(p,v,Vec3.ZERO,.12,WHITE,fade);
            }
            case EYE -> {
                p.translate(0,2.5,0);
                for(int i=0;i<16;i++) {
                    double x=-.7+i*.0875, nx=x+.0875;
                    double y=.3*Math.sin((x+.7)/1.4*Math.PI),ny=.3*Math.sin((nx+.7)/1.4*Math.PI);
                    tube(p,v,new Vec3(x,y,0),new Vec3(nx,ny,0),.035,WHITE,fade);
                    tube(p,v,new Vec3(x,-y,0),new Vec3(nx,-ny,0),.035,WHITE,fade);
                }
                crystal(p,v,Vec3.ZERO,.18,EDGE,fade);
            }
            case WOUND -> {
                for(int i=0;i<3;i++) tube(p,v,new Vec3(-.3+i*.2,1.5,.35),new Vec3(-.1+i*.2,.65,.35),.045,BLOOD,fade);
            }
            case FORGE -> {
                for(int i=0;i<6;i++) {double a=i*Math.PI/3+time*.08;
                    crystal(p,v,new Vec3(Math.cos(a)*.35,.7+Math.sin(a)*.2,Math.sin(a)*.35),.14,0xFFB15A,fade);}
                ring(p,v,.5*grow,.45,0xFF774C,fade,.04);
                tube(p,v,new Vec3(-.45,1.3-age*.015,0),new Vec3(.45,1.3-age*.015,0),.12,0xCFB6A0,fade);
            }
            case MENDING -> {
                for(int i=0;i<5;i++) {
                    double y=.5+i*.12;
                    tube(p,v,new Vec3(-.3,y,0),new Vec3(.3,y+.08,0),.018,WHITE,fade);
                    tube(p,v,new Vec3(-.3,y,0),new Vec3(-.3,y+.12,0),.018,EDGE,fade);
                }
            }
            case GROWTH, BONE, ICE -> {
                int color=packet.form()==ManipulationVisuals.Form.GROWTH?0x90AF6E:packet.form()==ManipulationVisuals.Form.BONE?0xD6C4A5:ICE;
                for(int i=0;i<8;i++) {double a=i*Math.PI/4;
                    Vec3 base=new Vec3(Math.cos(a)*r,.05,Math.sin(a)*r);
                    tube(p,v,base,base.add(-base.x*.25,grow*(.7+i%3*.25),-base.z*.25),.09,color,fade);
                    crystal(p,v,base.add(0,grow*.6,0),.18,color,fade);
                }
            }
            case UPDRAFT -> {
                if(packet.count()==4)ring(p,v,4*Math.min(1,age/12),.1,0xFFAF8A,fade,.08);
                if(packet.count()==3)ring(p,v,.7,.05,0xFFD6A0,fade,.06);
                for(int j=0;j<3;j++) for(int i=0;i<18;i++) {
                    double a=i*.35+j*Math.PI*2/3+time*.08,y=i*(packet.count()==2?.23:packet.count()==3?.06:.13);
                    Vec3 a1=new Vec3(Math.cos(a)*r,y,Math.sin(a)*r);
                    Vec3 a2=new Vec3(Math.cos(a+.35)*r,y+.13,Math.sin(a+.35)*r);
                    tube(p,v,a1,a2,.035,0xFFC0A0,fade);
                }
            }
            case TELEPORT -> {
                for(int i=0;i<8;i++) {double a=i*Math.PI/4;
                    tube(p,v,new Vec3(Math.cos(a)*.4,0,Math.sin(a)*.4),new Vec3(Math.cos(a+.7)*.7,2,Math.sin(a+.7)*.7),.065,0x6F376B,fade);}
            }
            case FLARE -> star(p,v,Vec3.ZERO,r*grow,WHITE,fade);
            case DEBT, HOUR -> {
                for(int i=0;i<12;i++) {
                    double a=i*Math.PI/6-time*.025;
                    Vec3 point=new Vec3(Math.cos(a)*.8,2.4+Math.sin(a)*.8,0);
                    tube(p,v,point.scale(.9).add(0,.24,0),point,.03,EDGE,fade);
                }
                tube(p,v,new Vec3(0,2.4,0),new Vec3(Math.sin(time*.08)*.55,2.4+Math.cos(time*.08)*.55,0),.035,WHITE,fade);
            }
            case VEIL -> {
                for(int i=0;i<6;i++) {double a=i*Math.PI/3+time*.015;
                    tube(p,v,new Vec3(Math.cos(a)*.6,.2,Math.sin(a)*.6),new Vec3(Math.cos(a+.8)*.6,1.8,Math.sin(a+.8)*.6),.1,0x35203F,fade*.65F);}
            }
        }
    }

    @SubscribeEvent
    public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
        var mc=Minecraft.getInstance();if(mc.player==null || world!=mc.level || mc.options.hideGui)return;
        int y=event.getGuiGraphics().guiHeight()-65;
        for(Cue cue:CUES) {
            var p=cue.packet;if(p.entityId()!=mc.player.getId())continue;
            boolean tithe=p.form()==ManipulationVisuals.Form.DEBT;
            if(!tithe && p.form()!=ManipulationVisuals.Form.HOUR)continue;
            long seconds=Math.max(0,(cue.until-world.getGameTime()+19)/20);
            String text=tithe?"Tithe: "+p.count()+" mL reserve | "+Math.round(p.radius())+" mL due | "+seconds+"s"
                    :"Endless Hour: "+String.format(java.util.Locale.ROOT,"%.1f",p.radius())+" HP deferred | "+seconds+"s";
            int x=(event.getGuiGraphics().guiWidth()-mc.font.width(text))/2;
            event.getGuiGraphics().fill(x-4,y-2,x+mc.font.width(text)+4,y+11,0xBB170A16);
            event.getGuiGraphics().drawString(mc.font,text,x,y,seconds<=5?0xFF6878:0xEAC3B6);y-=15;
        }
    }

    public static void sword(PoseStack p, VertexConsumer v, float alpha) {
        // Broad double-edged blade, raised ridge, hooked guard, wrapped grip and pommel.
        diamond(p,v,.17,.76,.055,BLOOD,alpha);
        tube(p,v,new Vec3(0,-.6,.058),new Vec3(0,.69,.058),.015,EDGE,alpha);
        tube(p,v,new Vec3(-.34,-.57,0),new Vec3(.34,-.57,0),.055,0x681C32,alpha);
        tube(p,v,new Vec3(-.34,-.57,0),new Vec3(-.28,-.42,0),.035,EDGE,alpha);
        tube(p,v,new Vec3(.34,-.57,0),new Vec3(.28,-.42,0),.035,EDGE,alpha);
        tube(p,v,new Vec3(0,-.6,0),new Vec3(0,-.91,0),.05,0x351426,alpha);
        crystal(p,v,new Vec3(0,-.94,0),.085,EDGE,alpha);
    }

    public static void projectileSword(PoseStack p, MultiBufferSource buffers, Vec3 direction, float alpha) {
        p.pushPose();
        Vec3 d=direction.normalize();
        p.mulPose(new org.joml.Quaternionf().rotationTo(0,1,0,(float)d.x,(float)d.y,(float)d.z));
        sword(p,buffers.getBuffer(HemoRenderTypes.QLIPHOTH_CORE),alpha);
        p.popPose();
    }

    public static void bloodShot(PoseStack p,MultiBufferSource buffers,Vec3 velocity,int form,float age) {
        VertexConsumer v=buffers.getBuffer(HemoRenderTypes.QLIPHOTH_CORE);
        double size=form==2?.35:.14;
        p.pushPose();
        Vec3 direction=velocity.lengthSqr()<.0001?new Vec3(0,1,0):velocity.normalize();
        p.mulPose(new org.joml.Quaternionf().rotationTo(0,1,0,(float)direction.x,(float)direction.y,(float)direction.z));
        diamond(p,v,size,size*1.6,size,BLOOD,1);
        diamond(p,v,size*.4,size*1.7,size*.4,EDGE,.9F);
        for(int i=0;i<(form==1?3:1);i++) {
            double a=age*.4+i*Math.PI*2/3;
            tube(p,v,new Vec3(Math.cos(a)*size,0,Math.sin(a)*size),
                    new Vec3(Math.cos(a+.8)*size*.3,-(form==1?1.1:.6),Math.sin(a+.8)*size*.3),.025,EDGE,.8F);
        }
        if(form==2) {ring(p,v,.37,0,0x6F1330,.8F,.035);ring(p,v,.25,-.2,EDGE,.8F,.025);}
        if(form==3) ring(p,v,.23,0,EDGE,.6F,.018);
        p.popPose();
    }

    private static void phoenix(PoseStack p, VertexConsumer v, float age, float fade) {
        p.translate(0,1+age*.025,0);
        double spread=Math.min(1,age/8.0), flap=Math.sin(age*.16)*.4;
        diamond(p,v,.24,.8,.18,0xF15135,fade);
        crystal(p,v,new Vec3(0,.95,0),.23,0xFFD698,fade);
        tube(p,v,new Vec3(0,.94,0),new Vec3(0,.84,-.48),.055,WHITE,fade);
        for(int side:new int[]{-1,1}) for(int i=0;i<9;i++) {
            Vec3 root=new Vec3(side*(.2+i*.22)*spread,.45+i*.09+flap,0);
            Vec3 tip=new Vec3(side*(.6+i*.27)*spread,-.9+i*.15+flap,.15+i*.06);
            Vec3 shoulder=new Vec3(side*.15,.35,0);
            quad(p,v,shoulder,root,root.add(0,-.32,.02),shoulder,0xA81730,fade);
            Vec3 ridge=root.lerp(tip,.45);
            quad(p,v,root.add(-.1,0,0),ridge.add(-.13,0,0),tip,root.add(.1,0,0),0xF65732,fade);
            tube(p,v,root,tip,.025,0xFFD39A,fade);
        }
        for(int i=-2;i<=2;i++) tube(p,v,new Vec3(0,-.3,0),new Vec3(i*.27,-1.7, .5+Math.abs(i)*.2),.08,0xFF9851,fade);
    }

    private static void boxBoundary(PoseStack p, VertexConsumer v, double r, int color, float alpha) {
        for(int side:new int[]{-1,1}) {
            tube(p,v,new Vec3(-r,.04,side*r),new Vec3(r,.04,side*r),.025,color,alpha);
            tube(p,v,new Vec3(side*r,.04,-r),new Vec3(side*r,.04,r),.025,color,alpha);
        }
    }

    private static void star(PoseStack p,VertexConsumer v,Vec3 center,double radius,int color,float alpha) {
        for(int i=0;i<8;i++) {double a=i*Math.PI/4;
            tube(p,v,center,center.add(Math.cos(a)*radius,Math.sin(a)*radius,0),i%2==0?.045:.02,color,alpha);}
        tube(p,v,center.add(0,0,-radius),center.add(0,0,radius),.04,color,alpha);
    }

    private static void orientedRing(PoseStack p, VertexConsumer v, Vec3 at, Vec3 axis, double radius,int color,float alpha) {
        Vec3 d=axis.normalize();p.pushPose();p.translate(at.x,at.y,at.z);
        p.mulPose(new org.joml.Quaternionf().rotationTo(0,1,0,(float)d.x,(float)d.y,(float)d.z));
        ring(p,v,radius,0,color,alpha,.025);p.popPose();
    }

    private static void ring(PoseStack p,VertexConsumer v,double radius,double y,int color,float alpha,double width) {
        for(int i=0;i<48;i++) {double a=i*Math.PI/24,b=(i+1)*Math.PI/24;
            tube(p,v,new Vec3(Math.cos(a)*radius,y,Math.sin(a)*radius),new Vec3(Math.cos(b)*radius,y,Math.sin(b)*radius),width,color,alpha);}
    }

    private static void crystal(PoseStack p, VertexConsumer v, Vec3 center, double size, int color,float alpha) {
        p.pushPose();p.translate(center.x,center.y,center.z);diamond(p,v,size*.55,size,size*.55,color,alpha);p.popPose();
    }

    private static void diamond(PoseStack p,VertexConsumer v,double x,double y,double z,int color,float alpha) {
        Vec3 top=new Vec3(0,y,0),bottom=new Vec3(0,-y,0);
        Vec3[] waist={new Vec3(-x,0,0),new Vec3(0,0,z),new Vec3(x,0,0),new Vec3(0,0,-z)};
        for(int i=0;i<4;i++) {
            int shade=i%2==0?color:((color&0xFEFEFE)>>1);
            quad(p,v,top,waist[i],waist[(i+1)%4],top,shade,alpha);
            quad(p,v,bottom,waist[(i+1)%4],waist[i],bottom,shade,alpha);
        }
    }

    private static void tube(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,double width,int color,float alpha) {
        Vec3 axis=b.subtract(a);if(axis.lengthSqr()<1e-9 || alpha<=0)return;
        Vec3 side=axis.normalize().cross(Math.abs(axis.normalize().y)>.9?new Vec3(1,0,0):new Vec3(0,1,0)).normalize().scale(width);
        Vec3 up=axis.normalize().cross(side).normalize().scale(width);
        quad(p,v,a.add(side),b.add(side),b.subtract(side),a.subtract(side),color,alpha);
        quad(p,v,a.add(up),b.add(up),b.subtract(up),a.subtract(up),color,alpha);
    }

    private static void quad(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int color,float alpha) {
        for(Vec3 point:new Vec3[]{a,b,c,d}) v.addVertex(p.last().pose(),(float)point.x,(float)point.y,(float)point.z)
                .setColor((color>>16)&255,(color>>8)&255,color&255,(int)(Mth.clamp(alpha,0,1)*255));
    }

    private record Cue(ManipulationVisualPacket packet,long born,long until) {}
}
