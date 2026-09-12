package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Body-relative state meters, drawn with the authored school materials even on Minimal particles. */
final class SchoolStateVisuals {
    private SchoolStateVisuals() {}

    static void collect(PoseStack poses, LuxUmbraBatch batch, VertexConsumer iron, Vec3 camera,
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
                    case RIME -> {
                        for (int i = 0; i < meter.levels; i++) {
                            double y = height * (.14 + i * .18);
                            ring(poses, v, localCamera, radius, y, 0xE3F3FF, alpha, false);
                            VisceralMesh.tube(poses, v, new Vec3(-.07, y - .06, -radius),
                                    new Vec3(.04, y + .06, -radius), .009, 0xE3F3FF, alpha);
                        }
                    }
                    case DISRUPTED -> {
                        Vec3 a = right.scale(-.28).add(0, height + .2, 0);
                        Vec3 b = a.add(right.scale(.24)).add(0, .12, 0);
                        FerricDuctilisGeometry.ribbon(poses, v, a, b, .09, .01, time, living.getId(), localCamera, 0xFFF0BE, alpha);
                        FerricDuctilisGeometry.ribbon(poses, v, b.add(right.scale(.1)).add(0, -.08, 0),
                                b.add(right.scale(.35)), .09, .01, time, living.getId()+1, localCamera, 0xFFF0BE, alpha);
                    }
                    case ILLUMINATED -> ring(poses, v, localCamera, radius * 1.2, height + .3, 0xFFE9A5, alpha, true);
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

    private static void ring(PoseStack poses, VertexConsumer v, Vec3 camera, double radius, double y, int color, float alpha, boolean diamond) {
        int sides = diamond ? 4 : 12;
        for (int i = 0; i < sides; i++) {
            double a = i * Math.PI * 2 / sides, b = (i + 1) * Math.PI * 2 / sides;
            Vec3 start = new Vec3(Math.cos(a) * radius, y, Math.sin(a) * radius);
            Vec3 end = new Vec3(Math.cos(b) * radius, y, Math.sin(b) * radius);
            if (diamond) LuxUmbraGeometry.ribbon(poses, v, start, end, camera, .075,
                    LuxUmbraGeometry.RIBBON, i, color, alpha, 0, 1);
            else {
                VisceralMesh.tube(poses, v, start, end, .018, color, alpha);
                ThermalGeometry.powder(poses, v, start, new Vec3(1, 0, 0), new Vec3(0, 1, 0), .09, i, alpha);
            }
        }
    }
}
