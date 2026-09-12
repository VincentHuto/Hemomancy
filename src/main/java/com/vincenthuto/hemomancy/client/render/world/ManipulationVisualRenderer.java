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

/** Server-timed spell cues, with physical surfaces and separate Lux/Umbra flow materials. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ManipulationVisualRenderer {
    private static final List<Cue> CUES = new ArrayList<>();
    private static ClientLevel world;
    private static final LuxUmbraBatch FLOWS = new LuxUmbraBatch();
    private static final int BLOOD = 0xB40C38, EDGE = 0xFF6470, WHITE = 0xFFF3CF, ICE = 0x9BE9F4;

    private ManipulationVisualRenderer() {}

    public static void accept(ManipulationVisualPacket packet) {
        ClientLevel current = Minecraft.getInstance().level;
        if (world != current) { CUES.clear(); ManipulationMotes.clear(); ManipulationAmbientParticles.clear(); world = current; }
        if (current == null || !Float.isFinite(packet.radius()) || packet.radius() < 0 || packet.radius() > 100000
                || (packet.radius()>128 && packet.form()!=ManipulationVisuals.Form.DEBT && packet.form()!=ManipulationVisuals.Form.HOUR && packet.form()!=ManipulationVisuals.Form.HOUR_BREAK)
                || !finite(packet.from()) || !finite(packet.to())) return;
        long now = current.getGameTime();
        com.vincenthuto.hemomancy.client.render.layer.MortemSkinLayer.accept(packet,current);
        com.vincenthuto.hemomancy.client.render.layer.ThermalSkinLayer.accept(packet,current);
        boolean keyed = packet.entityId() >= 0 || packet.form() == ManipulationVisuals.Form.WELL
                || packet.form() == ManipulationVisuals.Form.BEACON || packet.form() == ManipulationVisuals.Form.ORE
                || packet.form() == ManipulationVisuals.Form.CRUOR_SURFACE;
        if (keyed) {
            for (Cue cue : CUES) {
                if (!cue.life.retiring() && sameSource(cue.packet, packet)) {
                    if (packet.ticks() <= 0 || emptyCount(packet)) retire(cue,current);
                    else {
                        cue.chargeProgress.update(packet.radius(),now+Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
                        cue.packet=packet;
                        cue.life.refresh(now,packet.ticks());
                    }
                    return;
                }
            }
        }
        if (packet.ticks() <= 0 || emptyCount(packet)) return;
        if (CUES.size() >= 192) CUES.removeFirst();
        ManipulationMotes.emit(packet,current,false);
        ManipulationAmbientParticles.formation(packet,current);
        CUES.add(new Cue(packet,packet.form()==ManipulationVisuals.Form.WHITE_VERDICT
                ? ManipulationLifecycle.afterglow(now,packet.ticks())
                : new ManipulationLifecycle(now,packet.ticks(),keyed)));
    }

    private static boolean emptyCount(ManipulationVisualPacket packet) {
        return packet.count()<=0 && (packet.form()==ManipulationVisuals.Form.CROWN
                || packet.form()==ManipulationVisuals.Form.CHOIR || packet.form()==ManipulationVisuals.Form.WARD);
    }

    private static void retire(Cue cue,ClientLevel level) {
        if(cue.packet.form().name().endsWith("_CHARGE") && cue.lastChargeFrame!=null)
            cue.packet=cue.lastChargeFrame;
        if(cue.life.retire(level.getGameTime())) {
            var packet=cue.packet;
            ManipulationMotes.emit(new ManipulationVisualPacket(packet.form(),-1,cue.lastFrom,
                    packet.to(),packet.radius(),0,packet.count()),level,true);
        }
    }

    private static boolean sourceActive(Cue cue,ClientLevel level) {
        var packet=cue.packet;
        if(packet.form()==ManipulationVisuals.Form.CRUOR_SURFACE) {
            var pos=net.minecraft.core.BlockPos.containing(packet.from());
            if(!level.getBlockState(pos).is(com.vincenthuto.hemomancy.common.init.BlockInit.frozen_cruor.get()))return false;
            int mask=0;
            for(var side:net.minecraft.core.Direction.values())
                if(!level.getBlockState(pos.relative(side)).is(com.vincenthuto.hemomancy.common.init.BlockInit.frozen_cruor.get()))mask|=1<<side.ordinal();
            cue.exposedFaces=mask;
            return true;
        }
        if(packet.entityId()<0)return true;
        var entity=level.getEntity(packet.entityId());
        if(entity==null || !entity.isAlive())return false;
        cue.lastFrom=entity.position();
        if(packet.form().name().endsWith("_CHARGE"))cue.lastFrom=cue.lastFrom.add(0,entity.getEyeHeight(),0);
        // Remote entities do not synchronize their full effect map to this client.
        if(entity==Minecraft.getInstance().player && entity instanceof net.minecraft.world.entity.LivingEntity living) {
            var required=switch(packet.form()) {
                case MARK -> com.vincenthuto.hemomancy.common.init.EffectInit.conductive_mark;
                case RETORT -> com.vincenthuto.hemomancy.common.init.EffectInit.iron_retort;
                case HUNGER -> com.vincenthuto.hemomancy.common.init.EffectInit.insatiable_hunger;
                case GRAVE -> com.vincenthuto.hemomancy.common.init.EffectInit.grave_debt;
                case WOUND -> com.vincenthuto.hemomancy.common.init.EffectInit.blood_loss;
                default -> null;
            };
            return required==null || living.hasEffect(required);
        }
        return true;
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
        if (world != current) { CUES.clear(); ManipulationMotes.clear(); ManipulationAmbientParticles.clear(); world = current; }
        LuxUmbraEffects.tick();
        BloodFlowEffects.tick();
        BloodProjectileEffects.tick();
        BloodBindingTendrilRenderer.tick();
        FerricDuctilisEffects.tick();
        ThermalParticles.tick();
        if (current != null) CUES.removeIf(c -> {
            c.fragments.update(c.packet,current.getGameTime()-c.life.born);
            if(!c.life.retiring() && (c.life.expired(current.getGameTime()) || !sourceActive(c,current)))
                retire(c,current);
            if (!c.life.retiring()) {
                c.surface.update(current,c.packet.form(),c.lastFrom,c.packet.radius());
                if (current.getGameTime()-c.life.born>8)
                    ManipulationAmbientParticles.ambient(c.packet,current);
            }
            return c.life.finished(current.getGameTime());
        });
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        boolean solids=event.getStage()==RenderLevelStageEvent.Stage.AFTER_ENTITIES;
        if ((!solids && event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL) || world==null) return;
        var mc = Minecraft.getInstance();
        if (world != mc.level) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double time = world.getGameTime() + partial;
        Vec3 camera = event.getCamera().getPosition();
        Vec3 right = new Vec3(new org.joml.Vector3f(1, 0, 0).rotate(event.getCamera().rotation()));
        Vec3 up = new Vec3(new org.joml.Vector3f(0, 1, 0).rotate(event.getCamera().rotation()));
        LuxUmbraRenderTypes.begin(time);
        ThermalRenderTypes.begin(time);
        AnimusMortemRenderTypes.begin(time);
        FerricDuctilisRenderTypes.begin(time);
        FerricDuctilisGeometry.begin();
        FLOWS.clear();
        // AFTER_LEVEL has already popped the world's camera matrix and composited clouds.
        PoseStack poses = new PoseStack();
        if(!solids)poses.mulPose(event.getModelViewMatrix());
        var buffers = mc.renderBuffers().bufferSource();
        SchoolStateVisuals.collect(poses, FLOWS, solids ? buffers.getBuffer(FerricDuctilisRenderTypes.IRON) : null,
                camera, time, partial, solids);
        for (Cue cue : CUES) {
            var packet = cue.packet;
            if(solids && !FerricDuctilisGeometry.handles(packet.form()))continue;
            Vec3 from = cue.lastFrom;
            if (!cue.life.retiring() && packet.entityId() >= 0) {
                var entity = world.getEntity(packet.entityId());
                if (!sourceActive(cue,world)) {retire(cue,world);continue;}
                if(AnimusMortemGeometry.handles(packet.form()) && entity.isInvisible())continue;
                from = entity.getPosition(partial);
                if (packet.form().name().endsWith("_CHARGE")) from = from.add(0,entity.getEyeHeight(),0);
                cue.lastFrom=from;
                if ((packet.form()==ManipulationVisuals.Form.IRON_HEART || packet.form()==ManipulationVisuals.Form.BLACK_HEART)
                        && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    cue.bodyYaw=Mth.rotLerp(partial,living.yBodyRotO,living.yBodyRot);
                    cue.bodyScale=living.getScale();
                    cue.crouching=living.isCrouching();
                }
            }
            if (from.distanceToSqr(camera) > 128 * 128) continue;
            if (packet.form().name().endsWith("_CHARGE") && !cue.life.retiring()) {
                var entity=world.getEntity(packet.entityId());
                if (entity!=null) {
                    float progress=cue.chargeProgress.sample(time);
                    if(entity==mc.player) {
                        var selected=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireKnownManipulations(mc.player).getSelectedManip();
                        int held=com.vincenthuto.hemomancy.client.event.ClientEvents.getManipulationChargeTicks();
                        if(selected!=null && held>0 && ManipulationVisuals.chargeForm(selected.getName())==packet.form())
                            progress=com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules.chargeFraction(
                                    held-1+partial,selected.getRequiredChargeTicks());
                        else if(cue.lastChargeFrame!=null) progress=cue.lastChargeFrame.radius();
                    }
                    Vec3 aim=from.add(entity.getViewVector(partial).scale(8+16*progress));
                    Vec3 to=world.clip(new net.minecraft.world.level.ClipContext(from,aim,
                            net.minecraft.world.level.ClipContext.Block.COLLIDER,
                            net.minecraft.world.level.ClipContext.Fluid.NONE,entity)).getLocation();
                    packet=new ManipulationVisualPacket(packet.form(),packet.entityId(),from,to,
                            progress,packet.ticks(),packet.count());
                    cue.lastChargeFrame=packet;
                }
            }
            float age = (float) (time - cue.life.born);
            poses.pushPose();
            poses.translate(from.x - camera.x, from.y - camera.y, from.z - camera.z);
            if(packet.form()==ManipulationVisuals.Form.IRON_HEART || packet.form()==ManipulationVisuals.Form.BLACK_HEART)
                IronHeartAttachment.apply(poses,cue.bodyYaw,cue.bodyScale*.9f,cue.crouching);
            if(FerricDuctilisGeometry.handles(packet.form())) {
                float formation=cue.life.retiring()?cue.life.retiredFormation():cue.life.formation(time);
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):cue.life.presence(time);
                var source=world.getEntity(packet.entityId());
                FerricDuctilisGeometry.draw(packet,poses,solids?buffers.getBuffer(FerricDuctilisRenderTypes.IRON):FerricDuctilisGeometry.DISCARD,
                        solids?FerricDuctilisGeometry.DISCARD:FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),camera.subtract(from),right,up,time,age,formation,opacity,
                        source instanceof net.minecraft.world.entity.LivingEntity living?living:null);
                poses.popPose();continue;
            }
            if(AnimusMortemGeometry.handles(packet.form())) {
                float formation=cue.life.retiring()?cue.life.retiredFormation():cue.life.formation(time);
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):cue.life.presence(time);
                if(!cue.surface.patches().isEmpty()) {
                    double spread=packet.radius()*Math.min(1,(age+1)/12.0);
                    int seed=Math.floorMod(packet.entityId(),113);
                    for(var patch:cue.surface.patches()) {
                        Vec3 local=patch.center().subtract(from);
                        if(local.x*local.x+local.z*local.z>spread*spread)continue;
                        LuxUmbraGeometry.ground(poses,FLOWS.vertices(LuxUmbraBatch.Material.MORTEM),local,
                                patch.halfWidth(),packet.form()==ManipulationVisuals.Form.BLOOM?8:2,seed++,0xFFFFFF,opacity*.8f);
                    }
                }
                AnimusMortemGeometry.draw(packet,poses,FLOWS.vertices(AnimusMortemGeometry.mortem(packet.form())
                        ?LuxUmbraBatch.Material.MORTEM:LuxUmbraBatch.Material.ANIMUS),camera.subtract(from),right,up,time,age,formation,opacity);
                poses.popPose();continue;
            }
            if(ThermalGeometry.handles(packet.form())) {
                float formation=cue.life.retiring()?cue.life.retiredFormation():cue.life.formation(time);
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):cue.life.presence(time);
                var thermalPacket=packet.form()==ManipulationVisuals.Form.CRUOR_SURFACE?
                        new ManipulationVisualPacket(packet.form(),packet.entityId(),packet.from(),packet.to(),packet.radius(),packet.ticks(),cue.exposedFaces):packet;
                ThermalGeometry.draw(thermalPacket,poses,FLOWS,buffers.getBuffer(ThermalRenderTypes.CORE),from,camera.subtract(from),right,up,
                        time,age,formation,opacity,cue.surface,cue.fragments,partial,mc.options.particles().get()!=net.minecraft.client.ParticleStatus.MINIMAL);
                poses.popPose();continue;
            }
            if (LuxUmbraGeometry.handles(packet.form())) {
                float formation = cue.life.retiring() ? cue.life.retiredFormation() : cue.life.formation(time);
                float opacity = cue.life.retiring() ? cue.life.retiredOpacity() * cue.life.residue(time) : cue.life.presence(time);
                LuxUmbraGeometry.draw(packet, poses, FLOWS.vertices(LuxUmbraGeometry.umbra(packet.form())), camera.subtract(from), right, up,
                        time, age, formation, opacity);
                poses.popPose();
                continue;
            }
            VertexConsumer vertices=buffers.getBuffer(packet.form() == ManipulationVisuals.Form.ORE
                    ? HemoRenderTypes.MANIPULATION_LOCATOR : ManipulationMaterials.forForm(packet.form()).renderType());
            if(cue.life.retiring()) {
                if(packet.form()!=ManipulationVisuals.Form.ORE) {
                    float departure=1-cue.life.residue(time);
                    if(ManipulationBloodFormation.retainsDissolvingBody(packet.form()) && cue.life.retiredOpacity()>0) {
                        poses.pushPose();
                        draw(packet,poses,new CoalescingVertexConsumer(vertices,cue.life.retiredFormation(),departure,true),
                                time,age,cue.life.retiredOpacity());
                        poses.popPose();
                    }
                    ManipulationBloodFormation.draw(packet,poses,vertices,departure,true,time);
                }
            } else {
                float formation=cue.life.formation(time),departure=cue.life.departure(time);
                VertexConsumer body=packet.form()==ManipulationVisuals.Form.ORE?vertices:
                        new CoalescingVertexConsumer(vertices,formation,departure);
                poses.pushPose();
                draw(packet,poses,body,time,age,1);
                poses.popPose();
                if(formation<1 && packet.form()!=ManipulationVisuals.Form.ORE)
                    ManipulationBloodFormation.draw(packet,poses,vertices,formation,false,time);
            }
            poses.popPose();
        }
        if(solids) {
            FerricDuctilisEffects.render(poses,FLOWS,buffers.getBuffer(FerricDuctilisRenderTypes.IRON),camera,right,up,time,partial,true);
            buffers.endBatch(FerricDuctilisRenderTypes.IRON);return;
        }
        BloodProjectileEffects.render(poses,FLOWS,camera,right,up,time,partial);
        BloodEffectParticles.render(poses,FLOWS,camera,right,up,time);
        BloodFlowEffects.render(poses,FLOWS,camera,right,up,time,partial);
        BloodBindingTendrilRenderer.collect(poses,FLOWS,camera,partial);
        LuxUmbraEffects.render(poses, FLOWS, camera, right, up, time, partial);
        FerricDuctilisEffects.render(poses,FLOWS,FerricDuctilisGeometry.DISCARD,camera,right,up,time,partial,false);
        BlackVeilRenderer.collect(poses, FLOWS, partial);
        ThermalParticles.render(poses,FLOWS,camera,right,up,time,partial);
        buffers.endBatch(ThermalRenderTypes.CORE);
        FLOWS.drawMaterials(material -> buffers.getBuffer(ThermalRenderTypes.material(material)));
        for(var material:LuxUmbraBatch.Material.values())buffers.endBatch(ThermalRenderTypes.material(material));
        LuxUmbraRenderTypes.finish(buffers);
        BlackVeilRenderer.boundaries(poses, partial);
        ManipulationMotes.render(poses,buffers,camera,time,mc.options.particles().get());
        for (var material : ManipulationMaterials.values()) buffers.endBatch(material.renderType());
        buffers.endBatch(HemoRenderTypes.MANIPULATION_LOCATOR);
        renderBoundaries(poses,buffers,camera,time,true);
        renderBoundaries(poses,buffers,camera,time,false);
    }

    private static void renderBoundaries(PoseStack poses,MultiBufferSource.BufferSource buffers,
            Vec3 camera,double time,boolean glow) {
        var type=glow?com.vincenthuto.hemomancy.common.init.RenderTypeInit.RITE_BOUNDARY_GLOW:
                com.vincenthuto.hemomancy.common.init.RenderTypeInit.RITE_BOUNDARY_CORE;
        VertexConsumer vertices=buffers.getBuffer(type);
        for(Cue cue:CUES) {
            if(cue.life.retiring() || cue.lastFrom.distanceToSqr(camera)>128*128)continue;
            float age=(float)(time-cue.life.born);
            var boundary=ManipulationBoundaryStyle.forForm(cue.packet.form(),cue.packet.radius(),cue.packet.count(),age);
            if(boundary==null)continue;
            poses.pushPose();
            Vec3 at=cue.lastFrom.subtract(camera);
            poses.translate(at.x,at.y+boundary.height(),at.z);
            BloodCraftRingRenderer.drawBoundary(poses,glow?vertices:null,glow?null:vertices,
                    boundary.radius(),cue.packet.form()==ManipulationVisuals.Form.STILLNESS?8:(float)time,
                    .8F,boundary.color());
            poses.popPose();
        }
        buffers.endBatch(type);
    }

    private static void draw(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v,
            double time, float age, float fade) {
        if(packet.form()!=ManipulationVisuals.Form.ORE) {
            int seed=packet.entityId()>=0?packet.entityId():packet.from().hashCode();
            v=new UndulatingVertexConsumer(v,new ManipulationUndulation(packet.form(),time,age,seed,
                    packet.radius(),packet.to().subtract(packet.from())));
        }
        if (ManipulationSignatureEffects.draw(packet.form(), packet.radius(), packet.count(),
                packet.to().subtract(packet.from()), p, v, time, age, fade)) return;
        double r = packet.radius();
        Vec3 end = packet.to().subtract(packet.from());
        float grow = Mth.clamp(age / 6, .05F, 1);
        switch (packet.form()) {

            case IRON_HEART, BLACK_HEART -> {
                if(packet.form()!=ManipulationVisuals.Form.IRON_HEART)p.translate(0,1.25,.35);
                double beat=1+Math.sin(time*.22)*.06;
                p.scale((float)beat,(float)beat,(float)beat);
                int color=packet.form()==ManipulationVisuals.Form.IRON_HEART?0xD2C0B8:0x68364F;
                diamond(p,v,.22,.28,.08,color,fade*.65F);
                crystal(p,v,new Vec3(-.12,.12,0),.13,color,fade*.7F);
                crystal(p,v,new Vec3(.12,.12,0),.13,color,fade*.7F);
            }
            case MARIONETTE_TETHER -> {
                float partial = (float)(time - Math.floor(time));
                var body = world.getEntity(packet.entityId());
                var caster = world.getEntity(packet.count());
                if (body != null && caster != null) {
                    var mc = Minecraft.getInstance();
                    boolean firstPerson = caster == mc.player && mc.options.getCameraType().isFirstPerson();
                    float yaw = Mth.rotLerp(partial, caster.yRotO, caster.getYRot()) * Mth.DEG_TO_RAD;
                    Vec3 endpoint = PuppeteerThreadEndpointRules.playerHandEndpoint(caster.getEyePosition(partial),
                            caster.getViewVector(partial), yaw,
                            caster instanceof net.minecraft.world.entity.LivingEntity living
                                    && living.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT ? 1 : -1,
                            firstPerson).add(0, firstPerson ? -.3 : 0, 0).subtract(body.getPosition(partial));
                    Vec3 root = new Vec3(0, r * .65, 0);
                    Vec3 previous = root;
                    for (int segment = 1; segment <= 32; segment++) {
                        double t = segment / 32.0;
                        Vec3 next = VisceralGeometry.strandPoint(root, endpoint, t, .12, time * .015);
                        VisceralMesh.segment(p, v, previous, next, .016 - (t - 1.0 / 32) * .01,
                                .016 - t * .01, 0xC3324D, fade * .9F, (segment - 1) * .2, segment * .2);
                        previous = next;
                    }
                    double pulse = (time * .055) % 1;
                    VisceralMesh.drop(p, v, root.lerp(endpoint, pulse), .018, .035, 0xFF9AAB, fade, 0);
                }
            }
            case MARIONETTE_ORDER -> {
                float partial = (float)(time - Math.floor(time));
                var body = world.getEntity(packet.entityId());
                var enemy = packet.count() < 0 ? null : world.getEntity(packet.count());
                Vec3 at = enemy == null ? packet.to() : enemy.getPosition(partial).add(0, enemy.getBbHeight() + .35, 0);
                if (body != null) at = at.subtract(body.getPosition(partial));
                else at = at.subtract(packet.from());
                p.translate(at.x, at.y + .04, at.z);
                if (enemy == null) {
                    ring(p, v, .28, 0, 0xFFAB96, fade, .025);
                    tube(p, v, new Vec3(-.12, 0, 0), new Vec3(.12, 0, 0), .018, EDGE, fade);
                } else {
                    diamond(p, v, .16, .25, .04, EDGE, fade);
                }
            }
            case COMMAND -> {
                p.translate(0,r+.35,0);
                int color=packet.count()==1?0xFF6273:packet.count()==2?0xF1C487:0xBA91EC;
                ring(p,v,.35,0,color,fade,.04);
                for(int i=0;i<3;i++) {double a=i*Math.PI*2/3;
                    tube(p,v,new Vec3(Math.cos(a)*.35,0,Math.sin(a)*.35),new Vec3(Math.cos(a)*.3,.3,Math.sin(a)*.3),.045,color,fade);}
                // The endpoint is refreshed from the controller, not a guessed target.
                VisceralMesh.strand(p,v,Vec3.ZERO,end.add(0,-r-.35,0),.014,.18,time*.015,color,fade*.5F);
            }
            case CIRCUIT -> {
                for(int i=0;i<5;i++) {
                    double a=i*2.39996;
                    Vec3 root=new Vec3(Math.cos(a)*.4,.65,Math.sin(a)*.4);
                    Vec3 tip=root.add(Math.sin(a)*.12,.65,Math.cos(a)*.12);
                    VisceralMesh.strand(p,v,root,tip,.014,.12,i,0xD4B35E,fade*.8F);
                    double pulse=(time*.055+i*.21)%1;
                    VisceralMesh.drop(p,v,root.lerp(tip,pulse),.024,.04,0xFFF0BE,fade,0);
                }
            }
            case RUSH -> {
                boolean seal=packet.form()==ManipulationVisuals.Form.CAUTERIZE;
                for(int i=0;i<5;i++) {
                    double y=.4+i*.23, gap=seal?Math.max(0,.35-age*.025):.3;
                    tube(p,v,new Vec3(-gap,y,.34),new Vec3(gap,y+.1,.34),.03,seal?0xFFAD6B:EDGE,fade);
                    if(!seal)tube(p,v,new Vec3(-.3,y,0),new Vec3(-.3,y-.15,0),.035,BLOOD,fade);
                }
            }
            case NEEDLE_CHARGE, FAN_CHARGE, LANCE_CHARGE -> {
                Vec3 direction=end.normalize(),side=VisceralGeometry.side(direction);
                int count=packet.form()==ManipulationVisuals.Form.FAN_CHARGE?7:packet.form()==ManipulationVisuals.Form.LANCE_CHARGE?3:4;
                for(int i=0;i<count;i++) {
                    double offset=(i-(count-1)*.5)*.12;
                    Vec3 tip=direction.scale(1.3).add(side.scale(offset));
                    Vec3 axis=packet.form()==ManipulationVisuals.Form.FAN_CHARGE?direction.add(side.scale(offset*2)):direction;
                    tube(p,v,tip.subtract(axis.scale((packet.form()==ManipulationVisuals.Form.LANCE_CHARGE?.85:.4)*r)),tip,.018,EDGE,fade);
                }
            }
            case MORTAR_CHARGE, ANEURYSM_CHARGE, ICE_CHARGE, LIGHTNING_CHARGE, IRON_CHARGE, GAZE_CHARGE -> {
                Vec3 focus=packet.form()==ManipulationVisuals.Form.GAZE_CHARGE
                        ?ChargeVisualGeometry.focus(packet.form(),end):ChargeVisualGeometry.alongAim(end,1.25);
                p.translate(focus.x,focus.y,focus.z);
                int color=switch(packet.form()) {case ICE_CHARGE -> ICE;
                    case LIGHTNING_CHARGE -> 0xFFE9AB;case IRON_CHARGE -> 0xD6C0B9;default -> EDGE;};
                if(packet.form()==ManipulationVisuals.Form.MORTAR_CHARGE || packet.form()==ManipulationVisuals.Form.ANEURYSM_CHARGE)
                    crystal(p,v,Vec3.ZERO,(.08+.26*r)*(1+Math.sin(time*.3)*.08),BLOOD,fade);
                for(int i=0;i<6;i++) {double a=i*Math.PI/3+time*.05;
                    Vec3 at=new Vec3(Math.cos(a)*(.15+r*.25),Math.sin(a)*(.15+r*.25),0);
                    if(packet.form()==ManipulationVisuals.Form.ICE_CHARGE)crystal(p,v,at,.06+r*.07,color,fade);
                    else tube(p,v,at,at.add(Math.sin(a)*.12,Math.cos(a)*.12,0),.02,color,fade);
                }
            }
            case CLOUD -> {
                for(int i=0;i<18;i++) {
                    double x=Math.sin(i*13.7)*r,z=Math.cos(i*7.3)*r;
                    double fall=(time*.14+i*.57)%10;
                    tube(p,v,new Vec3(x,-fall,z),new Vec3(x,-Math.min(10,fall+.7),z),.018,BLOOD,fade*.7F);
                }
            }
            case MAGNET -> {
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
                p.translate(0,1.1,0);
                for(int i=0;i<3;i++) {
                    p.pushPose();p.translate(0,(i-1)*.27,i*.016);
                    VisceralMesh.plate(p,v,.42,.23,.045,0xB2A5A0,fade*.8F,i*.2);
                    VisceralMesh.strand(p,v,new Vec3(-.3,-.12,.09),new Vec3(.3,.12,.09),.012,.04,i,0xA84848,fade);
                    p.popPose();
                }
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
                VisceralMesh.locatorBox(p,v,.48,0xF0C985,fade*.6F);
            }
            case RUPTURE -> {
                double expansion=r*Math.min(1,age/8);
                for(int i=0;i<12;i++) {
                    double a=i*Math.PI/6;
                    Vec3 tip=new Vec3(Math.cos(a)*expansion,Math.sin(i*2)*expansion*.25,Math.sin(a)*expansion);
                    Vec3 branch=tip.scale(.6).add(0,.25,0);
                    VisceralMesh.strand(p,v,Vec3.ZERO,branch,.07,.13,i,BLOOD,fade);
                    VisceralMesh.strand(p,v,branch,tip,.035,.1,i,EDGE,fade);
                    crystal(p,v,tip,.14,BLOOD,fade);
                }
            }
            case DRAIN -> {
                boolean drain=packet.form()==ManipulationVisuals.Form.DRAIN;
                for(int i=0;i<3;i++) {
                    double phase=i*2.1+time*(drain?.015:.008);
                    VisceralMesh.strand(p,v,Vec3.ZERO,end,.025,drain?.22:.12,phase,drain?0xA52239:0xFFF2DB,fade);
                    double along=(time*(drain?.04:.06)+i/3.0)%1;
                    if(drain)along=1-along;
                    Vec3 at=VisceralGeometry.strandPoint(Vec3.ZERO,end,along,drain?.22:.12,phase);
                    VisceralMesh.drop(p,v,at,drain?.06:.035,drain?.08:.04,drain?0xC02C42:WHITE,fade,0);
                }
            }

            case MARK -> {
                for(int i=0;i<5;i++) {
                    double a=i*Math.PI*2/5;
                    Vec3 root=new Vec3(Math.cos(a)*.31,.55,Math.sin(a)*.31);
                    Vec3 tip=new Vec3(Math.cos(a+.4)*.39,1.55,Math.sin(a+.4)*.39);
                    VisceralMesh.strand(p,v,root,tip,.018,.13,i,0xE9C569,fade);
                    Vec3 split=root.lerp(tip,.6);
                    VisceralMesh.strand(p,v,split,split.add(Math.cos(a)*.16,.24,Math.sin(a)*.16),.012,.04,i,0xB5914D,fade);
                    double pulse=(time*.045+i*.2)%1;
                    VisceralMesh.drop(p,v,VisceralGeometry.strandPoint(root,tip,pulse,.13,i),.025,.04,WHITE,fade,0);
                }
            }
            case WOUND -> {
                for(int i=0;i<3;i++) tube(p,v,new Vec3(-.3+i*.2,1.5,.35),new Vec3(-.1+i*.2,.65,.35),.045,BLOOD,fade);
            }

            case MENDING -> {
                for(int i=0;i<5;i++) {
                    double y=.5+i*.12, gap=.28*(1-Math.min(1,age/12.0));
                    Vec3 start=new Vec3(-gap,y,.03),endStitch=new Vec3(gap,y+.08,.03);
                    VisceralMesh.strand(p,v,start,endStitch,.019,.08,i,0xC6B4AE,fade);
                    VisceralMesh.strand(p,v,new Vec3(0,y,0),new Vec3(0,y+.15,0),.014,.02,i,0x9E4850,fade);
                }
            }
            case GROWTH -> {
                boolean bone=packet.form()==ManipulationVisuals.Form.BONE;
                boolean growth=packet.form()==ManipulationVisuals.Form.GROWTH;
                int color=growth?0x94B77F:bone?0xDED0AD:0xC9E6F3;
                for(int i=0;i<8;i++) {
                    double a=i*2.39996;
                    Vec3 base=new Vec3(Math.cos(a)*r,.05,Math.sin(a)*r);
                    Vec3 tip=base.add(-base.x*.25,grow*(.55+i%3*.25),-base.z*.25);
                    VisceralMesh.strand(p,v,base,tip,bone?.105:.06,growth?.17:.07,i,color,fade);
                    Vec3 branch=base.lerp(tip,.55);
                    VisceralMesh.strand(p,v,branch,tip.add(Math.sin(a)*.2,-.05,Math.cos(a)*.2),.035,.04,i,color,fade);
                    VisceralMesh.strand(p,v,base,tip,.015,.03,i,growth?0xBC5260:0x994856,fade*.65F);
                }
            }

            case DEBT -> {
                boolean tithe=packet.form()==ManipulationVisuals.Form.DEBT;
                int body=tithe?0x92674F:0xC8E3EF;
                for(int i=0;i<12;i++) {
                    double a=i*Math.PI/6;
                    Vec3 outer=new Vec3(Math.cos(a)*.7,2.4+Math.sin(a)*.7,0);
                    Vec3 inner=new Vec3(Math.cos(a+.12)*.56,2.4+Math.sin(a+.12)*.56,0);
                    VisceralMesh.strand(p,v,outer,inner,.026,.04,i,body,fade);
                }
                Vec3 center=new Vec3(0,2.4,0);
                Vec3 hand=center.add(Math.sin(time*.08)*.5,Math.cos(time*.08)*.5,0);
                VisceralMesh.strand(p,v,center,hand,.025,.02,0,WHITE,fade);
                VisceralMesh.drop(p,v,center,.065,.10,tithe?0xB82D42:WHITE,fade,0);
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
            if(cue.life.retiring())continue;
            long seconds=(cue.life.remaining(world.getGameTime())+19)/20;
            String text=tithe?"Tithe: "+p.count()+" mL reserve | "+Math.round(p.radius())+" mL due | "+seconds+"s"
                    :"Endless Hour: "+String.format(java.util.Locale.ROOT,"%.1f",p.radius())+" HP deferred | "+seconds+"s";
            int x=(event.getGuiGraphics().guiWidth()-mc.font.width(text))/2;
            event.getGuiGraphics().fill(x-4,y-2,x+mc.font.width(text)+4,y+11,0xBB170A16);
            event.getGuiGraphics().drawString(mc.font,text,x,y,seconds<=5?0xFF6878:0xEAC3B6);y-=15;
        }
    }

    public static void sword(PoseStack p, VertexConsumer v, float alpha) {
        VisceralMesh.sword(p,v,alpha,0);
    }

    public static void projectileSword(PoseStack p, MultiBufferSource buffers, Vec3 direction, float alpha) {
        projectileSword(p,buffers,direction,alpha,8);
    }

    public static void projectileSword(PoseStack p, MultiBufferSource buffers, Vec3 direction, float alpha, float age) {
        projectileSword(p,buffers,direction,alpha,age,17);
    }

    public static void projectileSword(PoseStack p, MultiBufferSource buffers, Vec3 direction, float alpha, float age,int seed) {
        p.pushPose();
        Vec3 d=direction.lengthSqr()<1e-8?new Vec3(0,1,0):direction.normalize();
        p.mulPose(new org.joml.Quaternionf().rotationTo(0,1,0,(float)d.x,(float)d.y,(float)d.z));
        AnimusMortemRenderTypes.begin(Minecraft.getInstance().level.getGameTime()+Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        var vertices=new UndulatingVertexConsumer(formingProjectile(new BloodSurfaceVertices(buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS)),age),
                new ManipulationUndulation(ManipulationVisuals.Form.CROWN,age,age,seed,1,Vec3.ZERO));
        VisceralMesh.sword(p,vertices,alpha,age);
        p.popPose();
    }

    public static void bloodShot(PoseStack p,MultiBufferSource buffers,Vec3 velocity,int form,float age) {
        bloodShot(p,buffers,velocity,form,age,31);
    }

    public static void bloodShot(PoseStack p,MultiBufferSource buffers,Vec3 velocity,int form,float age,int seed) {
        VertexConsumer v=buffers.getBuffer(ManipulationMaterials.ANIMUS.renderType());
        v=formingProjectile(v,age);
        v=new UndulatingVertexConsumer(v,new ManipulationUndulation(ManipulationVisuals.Form.RUPTURE,age,age,seed,1,Vec3.ZERO));
        double size=form==2?.35:.14;
        p.pushPose();
        Vec3 direction=velocity.lengthSqr()<.0001?new Vec3(0,1,0):velocity.normalize();
        p.mulPose(new org.joml.Quaternionf().rotationTo(0,1,0,(float)direction.x,(float)direction.y,(float)direction.z));
        VisceralMesh.drop(p,v,Vec3.ZERO,size,size*1.6,0xCF2540,1,age*.018);
        for(int i=0;i<(form==1?3:1);i++) {
            double a=age*.4+i*Math.PI*2/3;
            VisceralMesh.strand(p,v,new Vec3(Math.cos(a)*size,0,Math.sin(a)*size),
                    new Vec3(Math.cos(a+.8)*size*.3,-(form==1?1.1:.6),Math.sin(a+.8)*size*.3),.035,.10,age*.025+i,EDGE,.8F);
        }
        if(form==2) {ring(p,v,.37,0,0x6F1330,.8F,.035);ring(p,v,.25,-.2,EDGE,.8F,.025);}
        if(form==3) ring(p,v,.23,0,EDGE,.6F,.018);
        p.popPose();
    }

    public static VertexConsumer formingProjectile(VertexConsumer vertices,float age) {
        return new CoalescingVertexConsumer(vertices,.6F+.4F*ManipulationLifecycle.smooth(age/3),0);
    }

    @SubscribeEvent
    public static void projectileRemoved(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
        if(!event.getLevel().isClientSide() || Minecraft.getInstance().level!=event.getLevel())return;
        var entity=event.getEntity();
        if(!(entity instanceof com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity)
                && !(entity instanceof com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity))return;
        var reason=entity.getRemovalReason();
        if(reason!=net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
                && reason!=net.minecraft.world.entity.Entity.RemovalReason.KILLED)return;
        // Only residue survives removal; never retain a flying projectile or damage cue.
        var packet=new ManipulationVisualPacket(ManipulationVisuals.Form.RUPTURE,-1,entity.position(),
                entity.position(),.3F,8,1);
        var current=Minecraft.getInstance().level;
        if(world!=current)return;
        if(CUES.size()>=192)CUES.removeFirst();
        Cue residue=new Cue(packet,new ManipulationLifecycle(current.getGameTime(),8,false));
        residue.life.retire(current.getGameTime());
        CUES.add(residue);
        ManipulationMotes.emit(packet,current,true);
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
            double wobbleA=1+.012*Math.sin(a*5),wobbleB=1+.012*Math.sin(b*5);
            VisceralMesh.segment(p,v,new Vec3(Math.cos(a)*radius*wobbleA,y,Math.sin(a)*radius*wobbleA),
                    new Vec3(Math.cos(b)*radius*wobbleB,y,Math.sin(b)*radius*wobbleB),
                    width*(.7+.3*Math.sin(a*3)*Math.sin(a*3)),width*(.7+.3*Math.sin(b*3)*Math.sin(b*3)),
                    color,alpha,i/48.0,(i+1)/48.0);}
    }

    private static void crystal(PoseStack p, VertexConsumer v, Vec3 center, double size, int color,float alpha) {
        p.pushPose();p.translate(center.x,center.y,center.z);diamond(p,v,size*.55,size,size*.55,color,alpha);p.popPose();
    }

    private static void diamond(PoseStack p,VertexConsumer v,double x,double y,double z,int color,float alpha) {
        p.pushPose();
        if(x>0)p.scale(1,1,(float)(z/x));
        VisceralMesh.drop(p,v,Vec3.ZERO,x,y,color,alpha,0);
        p.popPose();
    }

    private static void tube(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,double width,int color,float alpha) {
        VisceralMesh.tube(p,v,a,b,width,color,alpha);
    }

    private static void quad(PoseStack p,VertexConsumer v,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int color,float alpha) {
        VisceralMesh.quad(p,v,a,b,c,d,color,alpha,0,0,1,1);
    }

    private static final class Cue {
        ManipulationVisualPacket packet;
        ManipulationVisualPacket lastChargeFrame;
        final ManipulationLifecycle life;
        final ChargeVisualProgress chargeProgress;
        final ThermalSurface surface=new ThermalSurface();
        final ThermalFragments fragments=new ThermalFragments();
        Vec3 lastFrom;
        float bodyYaw;
        float bodyScale=1;
        boolean crouching;
        int exposedFaces=63;
        Cue(ManipulationVisualPacket packet,ManipulationLifecycle life) {
            this.packet=packet;this.life=life;this.lastFrom=packet.from();
            this.chargeProgress=new ChargeVisualProgress(packet.radius(),life.born);
        }
    }
}
