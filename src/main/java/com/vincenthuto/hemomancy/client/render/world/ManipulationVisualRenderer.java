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
    private static final int EDGE = 0xFF6470;

    private ManipulationVisualRenderer() {}

    public static void accept(ManipulationVisualPacket packet) {
        ClientLevel current = Minecraft.getInstance().level;
        if (world != current) { CUES.clear(); ManipulationMotes.clear(); ManipulationAmbientParticles.clear(); PuppetStringStates.clear(); world = current; }
        if (current == null || !Float.isFinite(packet.radius()) || packet.radius() < 0 || packet.radius() > 100000
                || (packet.radius()>128 && packet.form()!=ManipulationVisuals.Form.DEBT && packet.form()!=ManipulationVisuals.Form.HOUR && packet.form()!=ManipulationVisuals.Form.HOUR_BREAK)
                || !finite(packet.from()) || !finite(packet.to())) return;
        long now = current.getGameTime();
        PuppetStringStates.accept(packet,now);
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
        // An arrival cue can beat the body's spawn packet; wait briefly for it rather than retiring.
        if(entity==null && packet.form()==ManipulationVisuals.Form.PUPPET_DESCEND && level.getGameTime()-cue.life.born<6)return true;
        // Once the haul begins the Recall carries on past its discarded body: the last clot still reels into the Crossbar.
        if(packet.form()==ManipulationVisuals.Form.VESPER_RECALL && level.getGameTime()-cue.life.born>=
                com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperRecallRules.HAUL_TICK
                && (entity==null || !entity.isAlive()))return level.getGameTime()-cue.life.born<packet.ticks();
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
        if (world != current) { CUES.clear(); ManipulationMotes.clear(); ManipulationAmbientParticles.clear(); PuppetStringStates.clear();
            com.vincenthuto.hemomancy.client.render.item.harbinger.HeldWeaponPose.clear(); world = current; }
        LuxUmbraEffects.tick();
        BloodFlowEffects.tick();
        BloodProjectileEffects.tick();
        BloodBindingTendrilRenderer.tick();
        WorkingCraftSwallowRenderer.tick();
        WorldRiteFinaleGeometry.tick();
        FerricDuctilisEffects.tick();
        ThermalParticles.tick();
        LivingTorchFlames.tick();
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
        if(!solids)com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm.worldProjection(event.getProjectionMatrix());
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
        if (!solids) MycophantSiphonTetherGeometry.collect(world, poses, FLOWS.vertices(LuxUmbraBatch.Material.CRUOR),
                camera, time, partial);
        for (Cue cue : CUES) {
            var packet = cue.packet;
            if(solids && !FerricDuctilisGeometry.handles(packet.form()) && !LodestoneCollapseGeometry.handles(packet.form())
                    && !LodestoneFieldGeometry.handles(packet.form())
                    && !WeaponConjureGeometry.handles(packet.form()) && !SanguineMendingGeometry.handles(packet.form())
                    && !ForgedNeedleGeometry.handlesCharge(packet.form()) && !CastImpactGeometry.handles(packet.form()))continue;
            Vec3 from = cue.lastFrom;
            if (!cue.life.retiring() && packet.entityId() >= 0) {
                var entity = world.getEntity(packet.entityId());
                if (!sourceActive(cue,world)) {retire(cue,world);continue;}
                // A Recall whose body has been hauled off keeps drawing at its last position.
                if (entity==null && !VesperRecallGeometry.handles(packet.form())) continue;
                if (entity!=null) {
                    if(AnimusMortemGeometry.handles(packet.form()) && entity.isInvisible())continue;
                    from = entity.getPosition(partial);
                    if (packet.form().name().endsWith("_CHARGE")) from = from.add(0,entity.getEyeHeight(),0);
                    cue.lastFrom=from;
                    if ((packet.form()==ManipulationVisuals.Form.IRON_HEART || packet.form()==ManipulationVisuals.Form.BLACK_HEART
                            || bodyAnchored(packet.form())
                            || RadiantWakeGeometry.handles(packet.form())
                            || packet.form()==ManipulationVisuals.Form.RIPOSTE || PhoenixDebtGeometry.handles(packet.form()))
                            && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                        cue.bodyYaw=Mth.rotLerp(partial,living.yBodyRotO,living.yBodyRot);
                        cue.headYaw=Mth.rotLerp(partial,living.yHeadRotO,living.yHeadRot);
                        cue.bodyScale=living.getScale();
                        cue.crouching=living.isCrouching();
                    }
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
            if(bodyAnchored(packet.form())) {
                // Body-anchored casts author their own timelines from the caster's pose, like the riposte swing.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                var source=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                if(source!=null && source.isInvisible()) {poses.popPose();continue;}
                CasterFrame frame=casterFrame(cue,source,partial,from);
                Vec3 eye=camera.subtract(from);
                var form=packet.form();
                if(WeaponConjureGeometry.handles(form)) {
                    // Harden on the weapon as it is actually held; the paired claws harden in both hands.
                    WeaponConjureGeometry.Held main=null,off=null;
                    if(source instanceof net.minecraft.world.entity.player.Player player) {
                        main=heldWeapon(player,player.getMainArm(),player.getMainHandItem(),from);
                        if(com.vincenthuto.hemomancy.common.manipulation.ferric.WeaponConjureRules.decodeWeapon(packet.count())
                                ==com.vincenthuto.hemomancy.common.manipulation.ferric.WeaponConjureRules.CLAWS)
                            off=heldWeapon(player,player.getMainArm().getOpposite(),player.getOffhandItem(),from);
                    }
                    WeaponConjureGeometry.draw(packet,poses,FLOWS,solids?buffers.getBuffer(FerricDuctilisRenderTypes.IRON):FerricDuctilisGeometry.DISCARD,
                            solids?FerricDuctilisGeometry.DISCARD:buffers.getBuffer(ThermalRenderTypes.CORE),solids,eye,right,up,time,age,opacity,frame,main,off);
                }
                else if(SanguineMendingGeometry.handles(form))
                    SanguineMendingGeometry.draw(packet,poses,solids?buffers.getBuffer(FerricDuctilisRenderTypes.IRON):FerricDuctilisGeometry.DISCARD,
                            solids?FerricDuctilisGeometry.DISCARD:FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),eye,right,up,time,age,opacity,frame);
                else if(BloodRushGeometry.handles(form))
                    BloodRushGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),FLOWS.vertices(LuxUmbraBatch.Material.FLAME),
                            eye,right,up,time,age,opacity,frame);
                else if(SanguineWardGeometry.handles(form))
                    SanguineWardGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),eye,right,up,time,age,opacity,frame,
                            source==null?1.8:source.getBbHeight(),source==null?.6:source.getBbWidth());
                else if(CellHandGeometry.handles(form)) {
                    // Reach whatever the hand is draining or pouring into, resolved exactly as the cell particles do.
                    java.util.List<Vec3> targets=java.util.List.of();
                    if(source instanceof net.minecraft.world.entity.LivingEntity living) {
                        Vec3 palm=from.add(frame.hand());
                        var found=com.vincenthuto.hemomancy.client.render.item.harbinger.CellHandParticleEffects.useTargets(living,palm);
                        if(found!=null) { Vec3 feet=from; targets=found.points().stream().map(t -> t.subtract(feet)).toList(); }
                    }
                    CellHandGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),eye,right,up,time,age,opacity,frame,targets,from);
                }
                else
                    AvatarManifestGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),eye,right,up,time,age,opacity,frame);
                poses.popPose();continue;
            }
            if(FerricDuctilisGeometry.handles(packet.form())) {
                float formation=cue.life.retiring()?cue.life.retiredFormation():cue.life.formation(time);
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):cue.life.presence(time);
                var source=world.getEntity(packet.entityId());
                FerricDuctilisGeometry.draw(packet,poses,solids?buffers.getBuffer(FerricDuctilisRenderTypes.IRON):FerricDuctilisGeometry.DISCARD,
                        solids?FerricDuctilisGeometry.DISCARD:FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),camera.subtract(from),right,up,time,age,formation,opacity,
                        source instanceof net.minecraft.world.entity.LivingEntity living?living:null);
                poses.popPose();continue;
            }
            if(LodestoneCollapseGeometry.handles(packet.form())) {
                // The collapse authors its own pull, crush and sink; iron goes to the solid pass, field and blood to the flow pass.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                LodestoneCollapseGeometry.draw(packet,poses,solids?buffers.getBuffer(FerricDuctilisRenderTypes.IRON):FerricDuctilisGeometry.DISCARD,
                        solids?FerricDuctilisGeometry.DISCARD:FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(LodestoneFieldGeometry.handles(packet.form())) {
                // The axe's field authors its own bite, held pulses and sink; iron in the solid pass, field and blood in the flow pass.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                LodestoneFieldGeometry.draw(packet,poses,solids?buffers.getBuffer(FerricDuctilisRenderTypes.IRON):FerricDuctilisGeometry.DISCARD,
                        solids?FerricDuctilisGeometry.DISCARD:FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(SearingLashGeometry.handles(packet.form())) {
                // The whip authors its own uncoil, crack and burn-away.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                SearingLashGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.FLAME),FLOWS.vertices(LuxUmbraBatch.Material.MOLTEN),
                        camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(CruorJavelinGeometry.handles(packet.form())) {
                // The javelin authors its own forming, flight, impact and shatter; clots use the solid cruor core.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                CruorJavelinGeometry.draw(packet,poses,buffers.getBuffer(ThermalRenderTypes.CORE),FLOWS.vertices(LuxUmbraBatch.Material.CRUOR),
                        camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(RadiantWakeGeometry.handles(packet.form())) {
                // Wings unfurl on their own clock and stay while the channel refreshes the cue.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                var source=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                if(source!=null && source.isInvisible()) {poses.popPose();continue;}
                boolean firstPerson=source==mc.player && mc.options.getCameraType().isFirstPerson();
                RadiantWakeGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.LUX),camera.subtract(from),right,up,
                        time,age,opacity,new RadiantWakeGeometry.Frame(cue.bodyYaw,cue.bodyScale,cue.crouching,firstPerson));
                poses.popPose();continue;
            }
            if(FruitingContagionGeometry.handles(packet.form())) {
                // Fruiting and spreading author their own growth, burst and wither.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                FruitingContagionGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.MORTEM),camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(UmbralRendGeometry.handles(packet.form())) {
                // The rend authors its own gather, tear and fray, like the riposte swing.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                UmbralRendGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.UMBRA),camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(CastImpactGeometry.handles(packet.form())) {
                // Impacts author their own flash, burst and fray; iron splinters go to the solid pass, the rest to the flow pass.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                CastImpactGeometry.draw(packet,poses,FLOWS,
                        solids?buffers.getBuffer(FerricDuctilisRenderTypes.IRON):FerricDuctilisGeometry.DISCARD,solids,
                        camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(HematicRiposteGeometry.handles(packet.form())) {
                // The swing authors its own eruption and fray; the shared entrance fade would swallow the first ticks.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                var source=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                if(source!=null && source.isInvisible()) {poses.popPose();continue;}
                boolean firstPerson=source==mc.player && mc.options.getCameraType().isFirstPerson();
                HematicRiposteGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),camera.subtract(from),right,up,
                        time,age,opacity,new HematicRiposteGeometry.Frame(cue.bodyYaw,cue.headYaw,cue.bodyScale,cue.crouching,firstPerson));
                poses.popPose();continue;
            }
            if(PuppetStringsGeometry.handles(packet.form())) {
                // Arrival and departure author their own timelines, like the riposte swing.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                var source=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                if(source!=null && source.isInvisible()) {poses.popPose();continue;}
                PuppetStringsGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(VesperRecallGeometry.handles(packet.form())) {
                // Vesper's strings run to his raised Crossbar wherever the mount is now, not where it stood at the cast.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                var puppet=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                var vesper=world.getEntity(packet.count());
                Vec3 crossbar=packet.to();
                if(vesper instanceof net.minecraft.world.entity.LivingEntity mount) {
                    double[] tip=com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperRecallRules.crossbarOffset(
                            Mth.rotLerp(partial,mount.yBodyRotO,mount.yBodyRot));
                    crossbar=mount.getPosition(partial).add(tip[0],tip[1],tip[2]);
                }
                VesperRecallGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),camera.subtract(from),right,up,
                        time,age,opacity,crossbar.subtract(from),puppet==null?0.6:puppet.getBbWidth());
                poses.popPose();continue;
            }
            if(BloodShotGeometry.handles(packet.form())) {
                // The splash authors its own flash and settle, like the riposte swing.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                BloodShotGeometry.splash(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(ForgedNeedleGeometry.handlesCharge(packet.form())) {
                // Ferric needles forge in iron (solid pass) out of gathered blood (Ductilis flow pass).
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):cue.life.presence(time);
                ForgedNeedleGeometry.charge(packet,poses,solids?buffers.getBuffer(FerricDuctilisRenderTypes.IRON):FerricDuctilisGeometry.DISCARD,
                        solids?FerricDuctilisGeometry.DISCARD:FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),camera.subtract(from),right,up,time,opacity);
                poses.popPose();continue;
            }
            if(FuneralBellGeometry.handles(packet.form())) {
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time)
                        :packet.form()==ManipulationVisuals.Form.BELL_CHARGE?cue.life.presence(time):1;
                FuneralBellGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.MORTEM),camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(CrimsonCoronationGeometry.handles(packet.form())) {
                var source=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                if(source!=null && source.isInvisible() && !cue.life.retiring()) {poses.popPose();continue;}
                float formation=cue.life.retiring()?cue.life.retiredFormation():cue.life.formation(time);
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):cue.life.presence(time);
                CrimsonCoronationGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),camera.subtract(from),right,up,time,formation,opacity);
                poses.popPose();continue;
            }
            if(UmbralTearGeometry.handles(packet.form())) {
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                UmbralTearGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.UMBRA),camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(BloodAneurysmGeometry.handles(packet.form())) {
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time)
                        :packet.form()==ManipulationVisuals.Form.ANEURYSM_CHARGE?cue.life.presence(time):1;
                BloodAneurysmGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.ANIMUS),FLOWS.vertices(LuxUmbraBatch.Material.MORTEM),
                        camera.subtract(from),right,up,time,age,opacity);
                poses.popPose();continue;
            }
            if(PhoenixDebtGeometry.handles(packet.form())) {
                // The eruption authors its own unfurl and burn; the armed heart follows the shared presence fade.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time)
                        :packet.form()==ManipulationVisuals.Form.PHOENIX_READY?cue.life.presence(time):1;
                var source=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                if(source!=null && source.isInvisible()) {poses.popPose();continue;}
                boolean firstPerson=source==mc.player && mc.options.getCameraType().isFirstPerson();
                PhoenixDebtGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.FLAME),FLOWS.vertices(LuxUmbraBatch.Material.MOLTEN),
                        camera.subtract(from),right,up,time,age,opacity,
                        new PhoenixDebtGeometry.Frame(cue.bodyYaw,cue.bodyScale,cue.crouching,firstPerson));
                poses.popPose();continue;
            }
            if(NerveCommandGeometry.handles(packet.form())) {
                // The command authors its own impulse, recoil and leash; the cue refreshes while it holds.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time):1;
                var source=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                if(source!=null && source.isInvisible()) {poses.popPose();continue;}
                NerveCommandGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),camera.subtract(from),right,up,
                        time,age,opacity,source instanceof net.minecraft.world.entity.LivingEntity living?living:null);
                poses.popPose();continue;
            }
            if(DeadlyGazeGeometry.handles(packet.form())) {
                // The charge follows the shared presence fade; the grip and slam author their own seize and release.
                float opacity=cue.life.retiring()?cue.life.retiredOpacity()*cue.life.residue(time)
                        :packet.form()==ManipulationVisuals.Form.GAZE_CHARGE?cue.life.presence(time):1;
                var source=packet.entityId()>=0?world.getEntity(packet.entityId()):null;
                boolean firstPerson=source==mc.player && mc.options.getCameraType().isFirstPerson();
                DeadlyGazeGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.DUCTILIS),FLOWS.vertices(LuxUmbraBatch.Material.UMBRA),
                        camera.subtract(from),from,right,up,time,age,opacity,
                        source instanceof net.minecraft.world.entity.LivingEntity living?living:null,firstPerson);
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
            BloodProjectileEffects.renderIron(poses,buffers.getBuffer(FerricDuctilisRenderTypes.IRON),camera,time,partial);
            buffers.endBatch(FerricDuctilisRenderTypes.IRON);return;
        }
        BloodProjectileEffects.render(poses,FLOWS,camera,right,up,time,partial);
        BloodEffectParticles.render(poses,FLOWS,camera,right,up,time);
        BloodFlowEffects.render(poses,FLOWS,camera,right,up,time,partial);
        BloodBindingTendrilRenderer.collect(poses,FLOWS,camera,partial);
        WorkingCraftSwallowGeometry.collect(poses,FLOWS,camera,partial);
        WorldRiteFinaleGeometry.collect(poses,FLOWS,camera,partial);
        LuxUmbraEffects.render(poses, FLOWS, camera, right, up, time, partial);
        FerricDuctilisEffects.render(poses,FLOWS,FerricDuctilisGeometry.DISCARD,camera,right,up,time,partial,false);
        BlackVeilRenderer.collect(poses, FLOWS, partial);
        ThermalParticles.render(poses,FLOWS,camera,right,up,time,partial);
        LivingTorchFlames.render(poses,FLOWS,camera,right,up,time);
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

    /** Casts drawn from the caster's own pose: the early-game basics, the Living Arsenal, the cell hands and the avatar. */
    static boolean bodyAnchored(ManipulationVisuals.Form form) {
        return BloodRushGeometry.handles(form) || SanguineWardGeometry.handles(form) || SanguineMendingGeometry.handles(form)
                || WeaponConjureGeometry.handles(form) || CellHandGeometry.handles(form) || AvatarManifestGeometry.handles(form);
    }

    private static WeaponConjureGeometry.Held heldWeapon(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.entity.HumanoidArm arm,net.minecraft.world.item.ItemStack stack,Vec3 from) {
        var held=com.vincenthuto.hemomancy.client.render.item.harbinger.HeldWeaponPose.held(player,arm,stack,from);
        return held==null?null:new WeaponConjureGeometry.Held(held.faces(),held.grip());
    }

    private static CasterFrame casterFrame(Cue cue,net.minecraft.world.entity.Entity source,float partial,Vec3 from) {
        var mc=Minecraft.getInstance();
        boolean firstPerson=source==mc.player && mc.options.getCameraType().isFirstPerson();
        Vec3 look=source==null?Vec3.ZERO:source.getViewVector(partial);
        Vec3 motion=source==null?Vec3.ZERO:new Vec3(source.getX()-source.xo,source.getY()-source.yo,source.getZ()-source.zo);
        int side=source instanceof net.minecraft.world.entity.LivingEntity living
                && living.getMainArm()==net.minecraft.world.entity.HumanoidArm.LEFT?-1:1;
        Vec3 hand=null,fingers=null;
        if(firstPerson && source instanceof net.minecraft.world.entity.player.Player player) {
            // First person follows the drawn cell hand's palm, or where vanilla holds an ordinary item.
            var arm=player.getMainArm();
            Vec3 palm=com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm.world(arm);
            if(palm!=null) { hand=palm.subtract(from); fingers=com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm.fingers(arm); }
            else hand=com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm.itemHand(arm).subtract(from);
        } else if(source instanceof net.minecraft.world.entity.player.Player player) {
            // Third person follows the posed model's real hand.
            Vec3 captured=com.vincenthuto.hemomancy.client.render.outpouring.OutpouringAnchors.capturedMainHand(player);
            if(captured!=null && captured.distanceToSqr(from)<9)hand=captured.subtract(from);
        }
        if(hand==null)hand=CasterFrame.estimateHand(firstPerson?cue.headYaw:cue.bodyYaw,cue.bodyScale,cue.crouching,firstPerson,look,side);
        return new CasterFrame(cue.bodyYaw,cue.headYaw,cue.bodyScale,cue.crouching,firstPerson,hand,look,motion,
                cue.packet.from().subtract(from),fingers);
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
        switch (packet.form()) {
            // Only forms no school tier claims reach this legacy path.
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
            case ORE -> {
                VisceralMesh.locatorBox(p,v,.48,0xF0C985,fade*.6F);
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
        if(entity instanceof com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity shot) {
            BloodProjectileEffects.shotRemoved(shot);
            return;
        }
        if(entity instanceof com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity needle && !needle.isCoronationSword()) {
            BloodProjectileEffects.needleRemoved(needle);
            return;
        }
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


    private static void ring(PoseStack p,VertexConsumer v,double radius,double y,int color,float alpha,double width) {
        for(int i=0;i<48;i++) {double a=i*Math.PI/24,b=(i+1)*Math.PI/24;
            double wobbleA=1+.012*Math.sin(a*5),wobbleB=1+.012*Math.sin(b*5);
            VisceralMesh.segment(p,v,new Vec3(Math.cos(a)*radius*wobbleA,y,Math.sin(a)*radius*wobbleA),
                    new Vec3(Math.cos(b)*radius*wobbleB,y,Math.sin(b)*radius*wobbleB),
                    width*(.7+.3*Math.sin(a*3)*Math.sin(a*3)),width*(.7+.3*Math.sin(b*3)*Math.sin(b*3)),
                    color,alpha,i/48.0,(i+1)/48.0);}
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

    private static final class Cue {
        ManipulationVisualPacket packet;
        ManipulationVisualPacket lastChargeFrame;
        final ManipulationLifecycle life;
        final ChargeVisualProgress chargeProgress;
        final ThermalSurface surface=new ThermalSurface();
        final ThermalFragments fragments=new ThermalFragments();
        Vec3 lastFrom;
        float bodyYaw;
        float headYaw;
        float bodyScale=1;
        boolean crouching;
        int exposedFaces=63;
        Cue(ManipulationVisualPacket packet,ManipulationLifecycle life) {
            this.packet=packet;this.life=life;this.lastFrom=packet.from();
            this.chargeProgress=new ChargeVisualProgress(packet.radius(),life.born);
        }
    }
}
