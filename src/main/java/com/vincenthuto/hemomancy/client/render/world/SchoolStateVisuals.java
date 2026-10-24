package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Body-relative state meters, drawn with the authored school materials even on Minimal particles. */
final class SchoolStateVisuals {
    private SchoolStateVisuals() {}

    /** {@code iron} draws the solid-pass Lodestone plates; {@code cruorCore} the translucent-pass Rime clot cores. */
    static void collect(PoseStack poses, LuxUmbraBatch batch, VertexConsumer iron, VertexConsumer cruorCore, Vec3 camera,
                        double time, float partial, boolean solids) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()
                    || living.position().distanceToSqr(camera) > 48 * 48
                    || living == mc.player && mc.options.getCameraType().isFirstPerson()
                    || living.isInvisible() && living != mc.player) continue;
            var data = SchoolStates.data(living);
            if (data.entries().isEmpty()) continue;
            Vec3 at = living.getPosition(partial).subtract(camera);
            poses.pushPose();
            poses.translate(at.x, at.y, at.z);
            float yaw = Mth.rotLerp(partial, living.yBodyRotO, living.yBodyRot);
            poses.mulPose(Axis.YP.rotationDegrees(-yaw));
            Vec3 localCamera = camera.subtract(living.getPosition(partial)).yRot(yaw * Mth.DEG_TO_RAD);
            Vec3 up = new Vec3(0, 1, 0), right = up.cross(localCamera).normalize();
            double radius = living.getBbWidth() * .55 + .035;
            double height = living.getBbHeight();
            for (var state : data.entries().entrySet()) {
                var meter = state.getValue();
                if (meter.expires <= time || solids != (state.getKey() == SchoolState.LODESTONE)) continue;
                float alpha = (float)Math.min(.85, (meter.expires - time) / 10);
                var material = switch (state.getKey()) {
                    case PRESSURE -> LuxUmbraBatch.Material.ANIMUS;
                    case NECROSIS -> LuxUmbraBatch.Material.MORTEM;
                    case SEARING -> LuxUmbraBatch.Material.FLAME;
                    case RIME -> LuxUmbraBatch.Material.CRUOR;
                    case DISRUPTED -> LuxUmbraBatch.Material.DUCTILIS;
                    case ILLUMINATED -> LuxUmbraBatch.Material.LUX;
                    default -> LuxUmbraBatch.Material.UMBRA;
                };
                VertexConsumer v = solids ? iron : batch.vertices(material);
                if (state.getKey() == SchoolState.PRESSURE)
                    v = new BloodSurfaceVertices(v);
                switch (state.getKey()) {
                    case PRESSURE -> {
                        for (int i = 0; i < meter.levels; i++) {
                            double angle = time * .018 + i * Math.PI * 2 / 3;
                            Vec3 pulse = new Vec3(Math.cos(angle) * radius, height * .6, Math.sin(angle) * radius);
                            double beat = 1 + .12 * Math.sin(time * .2);
                            VisceralMesh.drop(poses, v, pulse, .055 * beat, .11 * beat, 0xD52149, alpha, time * .02);
                        }
                    }
                    case NECROSIS -> {
                        int wounds = 1 + (int)Math.ceil(meter.stored / 2);
                        for (int i = 0; i < wounds; i++) {
                            double x = (i - (wounds - 1) * .5) * .095;
                            for (int side : new int[]{-1, 1}) {
                                LuxUmbraGeometry.ribbon(poses, v, new Vec3(x, height * .42, side * radius),
                                        new Vec3(x + .04, height * .7, side * radius), localCamera,
                                        .065, LuxUmbraGeometry.RIBBON, i, 0xFFFFFF, alpha, 0, 1);
                            }
                        }
                    }
                    case LODESTONE -> {
                        for (int side : new int[]{-1, 1}) {
                            poses.pushPose();
                            poses.translate(side * radius, height * .62, 0);
                            poses.mulPose(Axis.YP.rotationDegrees(side * 30));
                            VisceralMesh.plate(poses, v, .035, .12, .012, 0x8994AC, alpha, time * .01);
                            poses.popPose();
                        }
                    }
                    case SEARING -> {
                        for (int i = 0; i < 3; i++) {
                            double angle = i * Math.PI * 2 / 3;
                            Vec3 start = new Vec3(Math.cos(angle) * radius, height * .32, Math.sin(angle) * radius);
                            ThermalGeometry.fire(poses, v, start, right, up, .14, height * .23,
                                    living.getId() + i, alpha);
                        }
                    }
                    // The cruor core dithers below full alpha, so the clots stay opaque until the state runs out.
                    case RIME -> rime(poses, cruorCore, v, living, meter.levels,
                            Mth.clamp((float)(time - meter.lastBuild) / 8, 0, 1),
                            (float)Math.min(1, (meter.expires - time) / 10));
                    case DISRUPTED -> {
                        Vec3 a = right.scale(-.28).add(0, height + .2, 0);
                        Vec3 b = a.add(right.scale(.24)).add(0, .12, 0);
                        FerricDuctilisGeometry.ribbon(poses, v, a, b, .09, .01, time, living.getId(), localCamera, 0xFFF0BE, alpha);
                        FerricDuctilisGeometry.ribbon(poses, v, b.add(right.scale(.1)).add(0, -.08, 0),
                                b.add(right.scale(.35)), .09, .01, time, living.getId()+1, localCamera, 0xFFF0BE, alpha);
                    }
                    case ILLUMINATED -> ring(poses, v, localCamera, radius * 1.2, height + .3, 0xFFE9A5, alpha);
                    case OBSCURED, VEILED -> {
                        for (int i = 0; i < 2; i++) {
                            Vec3 start = new Vec3(-radius, height * (.75 + .12 * i), 0);
                            LuxUmbraGeometry.stream(poses, v, start, start.add(radius * 2, -.07, 0),
                                    localCamera, .10, .055, time * .025 + i, LuxUmbraGeometry.RIBBON,
                                    living.getId() + i, 0xFFFFFF, alpha);
                        }
                    }
                }
            }
            poses.popPose();
        }
    }

    /**
     * Frozen cruor crusting the body, one band per Rime level from the shins up; the newest band grows out
     * from the moment it was built, the way a Cruor Form projection sets on its target.
     */
    private static void rime(PoseStack poses, VertexConsumer core, VertexConsumer frost, LivingEntity living,
                             int levels, float newest, float alpha) {
        double width = living.getBbWidth(), height = living.getBbHeight();
        int seed = living.getId() * 31;
        for (int band = 0; band < levels; band++) {
            float growth = band == levels - 1 ? HemoEasing.smoothstep(newest) : 1;
            if (growth <= .01) continue;
            for (int i = 0; i < 5; i++) {
                int slot = band * 5 + i;
                double a = slot * 2.39996 + ThermalMotion.random(seed, slot) * .6;
                double y = height * (.16 + band * .3 + (ThermalMotion.random(seed, slot + 20) - .5) * .12);
                double out = width * (.42 + .08 * growth);
                Vec3 at = new Vec3(Math.cos(a) * out, y, Math.sin(a) * out);
                double size = width * (.14 + ThermalMotion.random(seed, slot + 40) * .1) * growth;
                ThermalGeometry.clot(poses, core, frost, at, size, seed + slot, a, alpha);
            }
        }
    }

    private static void ring(PoseStack poses, VertexConsumer v, Vec3 camera, double radius, double y, int color, float alpha) {
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI * 2 / 4, b = (i + 1) * Math.PI * 2 / 4;
            Vec3 start = new Vec3(Math.cos(a) * radius, y, Math.sin(a) * radius);
            Vec3 end = new Vec3(Math.cos(b) * radius, y, Math.sin(b) * radius);
            LuxUmbraGeometry.ribbon(poses, v, start, end, camera, .075, LuxUmbraGeometry.RIBBON, i, color, alpha, 0, 1);
        }
    }
}
