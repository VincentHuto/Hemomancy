package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Blood Shot's living droplet and its splash. A glossy core sits inside a translucent sheath with a clot at its back,
 * dragging a twisting sinew tail along the path it actually flew; on contact it bursts into a crown of droplets that
 * streak, fall and settle. Flight points are world positions (the caller translates by the camera); splash points are
 * offsets from the contact point.
 */
final class BloodShotGeometry {
    static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
    /** Splash variants carried in the packet count. */
    static final int ENTITY = 1, BLOCK = 2, MORTAR = 3, NEEDLE = 4;
    static final int SPLASH_TICKS = 16, FLASH_END = 3, CROWN_END = 7;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private BloodShotGeometry() {}

    static boolean handles(Form form) {
        return form == Form.BLOOD_SPLASH;
    }

    /** Head radius for each synced visual form: 0 shot, 1 guided, 2 mortar, 3 halo. */
    static double size(int form) {
        return form == 2 ? .32 : form == 3 ? .11 : .13;
    }

    /** Splash spread for a projectile of {@code size}; kept proportional so a mortar reads as a blast. */
    static float splashRadius(double size) {
        return (float) (size * 7);
    }

    static void shot(PoseStack p, VertexConsumer v, List<Vec3> path, Vec3 velocity, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, int form, int seed, float alpha, float wake, boolean flying, Vec3 tether) {
        if (path.isEmpty()) return;
        Vec3 head = path.getLast();
        double s = size(form) * (.6 + .4 * HemoEasing.smoothstep(age / 3.0));
        float pulse = (float) Math.pow(Math.max(0, Math.sin(time * .6 + seed)), 4);

        if (path.size() >= 2 && wake > 0) {
            // 1. Wake: a translucent sheath around a glossy core line, both pinching off toward the tail.
            ribbonPath(p, v, path, camera, s * .15, s * 1.2, RIBBON, seed, TINT, wake * .28f);
            ribbonPath(p, v, path, camera, s * .04, s * .45, RIBBON, seed + 1, TINT, wake * .85f);
            // 2. Sinew: strands twisting about the wake, thick at the head and pulsing with it.
            int strands = form == 1 ? 3 : 2;
            float sinew = wake * (.7f + .3f * pulse);
            for (int strand = 0; strand < strands; strand++) {
                List<Vec3> points = new ArrayList<>(path.size());
                for (int i = 0; i < path.size(); i++) {
                    double t = i / (path.size() - 1.0);
                    Vec3[] basis = basis(path.get(Math.min(i + 1, path.size() - 1)).subtract(path.get(Math.max(0, i - 1))));
                    double angle = t * 6 + strand * Math.PI * 2 / strands - time * .3;
                    double radius = s * .6 * t;
                    points.add(path.get(i).add(basis[0].scale(Math.cos(angle) * radius)).add(basis[1].scale(Math.sin(angle) * radius)));
                }
                ribbonPath(p, v, points, camera, s * .02, s * .14, RIBBON, seed + 2 + strand, strand == 0 ? CLOT : TINT, sinew);
            }
            if (form == 2) drips(p, v, path, camera, right, up, s, seed, wake);
        }
        if (!flying) return;

        Vec3 axis = velocity.lengthSqr() > 1e-6 ? velocity
                : path.size() >= 2 ? head.subtract(path.get(path.size() - 2)) : UP;
        axis = axis.lengthSqr() < 1e-8 ? UP : axis.normalize();
        Vec3 across = facingSide(axis, camera.subtract(head));
        Vec3 normal = axis.cross(across).normalize();
        // Surface tension: the body breathes along its flight axis.
        double wobble = 1 + .09 * Math.sin(time * .9 + seed);

        // 3. Body: sheath, then core, clot at the back and a hot highlight leading the flight.
        card(p, v, head.subtract(axis.scale(s * .2)), across, axis, s * 1.5 / wobble, s * 2.3 * wobble, FOCUS, seed, TINT, alpha * .3f);
        card(p, v, head, across, axis, s / wobble, s * 1.7 * wobble, FOCUS, seed + 1, TINT, alpha);
        card(p, v, head.subtract(axis.scale(s * .8)), across, axis, s * .5, s * .75, FOCUS, seed + 2, CLOT, alpha * .85f);
        card(p, v, head.add(axis.scale(s * .35)), right, up, s * .3, s * .3, FOCUS, seed + 3, CORE, alpha * (.7f + .3f * pulse));

        // 4. Form signatures.
        if (form == 1) {
            // Guided: two feelers sway ahead, tasting for the target.
            for (int k = -1; k <= 1; k += 2) {
                double sway = Math.sin(time * .5 + k * 1.3 + seed) * .35;
                Vec3 tip = head.add(axis.scale(s * 2.6)).add(across.scale(k * s * (1.1 + sway)));
                Vec3 mid = head.add(axis.scale(s * 1.4)).add(across.scale(k * s * .35)).add(normal.scale(sway * s * .5));
                ribbonPath(p, v, List.of(head.add(axis.scale(s * .6)), mid, tip), camera, s * .09, s * .015,
                        RIBBON, seed + 10 + k, TINT, alpha * .7f);
            }
        } else if (form == 2) {
            // Mortar: heavy clots orbit the payload.
            for (int k = 0; k < 4; k++) {
                double a = time * .35 + k * Math.PI / 2 + seed;
                Vec3 at = head.add(across.scale(Math.cos(a) * s * 1.3)).add(normal.scale(Math.sin(a) * s * 1.3));
                card(p, v, at, right, up, s * .2, s * .2, FOCUS, seed + 20 + k, k % 2 == 0 ? CLOT : TINT, alpha * .9f);
            }
        } else if (form == 3 && tether != null) {
            // Halo: a slack sinew tether back to the caster, with a bead of blood running along it.
            List<Vec3> line = new ArrayList<>(9);
            Vec3 sag = UP.scale(-Math.min(.6, head.distanceTo(tether) * .2));
            for (int i = 0; i <= 8; i++) {
                double t = i / 8.0;
                line.add(head.lerp(tether, t).add(sag.scale(Math.sin(t * Math.PI))));
            }
            ribbonPath(p, v, line, camera, s * .14, s * .06, RIBBON, seed + 30, TINT, alpha * .35f);
            double travel = (time * .05 + seed * .1) % 1;
            card(p, v, line.get((int) Math.round(travel * 8)), right, up, s * .25, s * .25, FOCUS, seed + 31, CORE, alpha * .6f);
        }
    }

    /** Mortar payloads shed drops that fall away below the stations they left. */
    private static void drips(PoseStack p, VertexConsumer v, List<Vec3> path, Vec3 camera, Vec3 right, Vec3 up,
            double s, int seed, float wake) {
        for (int i = 0; i < path.size() - 1; i += 2) {
            double behind = path.size() - 1 - i;
            Vec3 at = path.get(i).add(0, -.035 * behind * behind, 0);
            double size = s * .22 * (1 - behind / (path.size() + 1.0));
            card(p, v, at, right, up, size, size * 1.6, FOCUS, seed + 40 + i, i % 4 == 0 ? CLOT : TINT, wake * .7f);
        }
    }

    static void splash(ManipulationVisualPacket packet, PoseStack p, VertexConsumer v, Vec3 camera, Vec3 right, Vec3 up,
            double time, float age, float alpha) {
        if (alpha <= 0) return;
        int variant = Math.max(ENTITY, Math.min(NEEDLE, packet.count()));
        Vec3 d = packet.to().subtract(packet.from());
        d = d.lengthSqr() < 1e-8 ? UP : d.normalize();
        double r = Math.max(.3, packet.radius());
        int seed = Math.floorMod(packet.from().hashCode(), 113);
        Vec3[] basis = basis(d);
        double grow = HemoEasing.smoothstep(age / (double) FLASH_END);
        float settle = (float) HemoEasing.smoothstep((age - CROWN_END) / (double) (SPLASH_TICKS - CROWN_END));

        // 1. Flash: a hot core inside a bursting sheath.
        if (age < FLASH_END + 2) {
            float flash = 1 - age / (FLASH_END + 2f);
            double burst = r * (.25 + .35 * grow);
            card(p, v, Vec3.ZERO, right, up, burst, burst, FOCUS, seed, TINT, alpha * .5f * flash);
            card(p, v, Vec3.ZERO, right, up, r * .12 * flash + .04, r * .12 * flash + .04, FOCUS, seed + 1, CORE, alpha * flash);
        }

        // 2. Ground: a clinging stain on blocks, a shock ring under a mortar.
        if (variant == BLOCK) {
            double stain = r * .45 * grow * (1 + settle * .15);
            card(p, v, d.scale(-.03), basis[0], basis[1], stain, stain, POOL, seed + 2, TINT, alpha * .85f * (1 - settle * .85f));
        } else if (variant == MORTAR) {
            double ring = r * (.3 + 1.3 * HemoEasing.smoothstep(age / 10.0));
            ground(p, v, new Vec3(0, .05, 0), ring, RIPPLE, seed + 2, TINT, alpha * .7f * (1 - settle));
        }

        // 3. Crown: droplets thrown out on drag and gravity, each trailing a thinning streak of sinew.
        int count = variant == MORTAR ? 18 : variant == BLOCK ? 10 : variant == NEEDLE ? 5 : 9;
        float body = alpha * (1 - settle);
        if (body <= .002) return;
        for (int i = 0; i < count; i++) {
            Vec3 at = droplet(variant, i, seed, age, d, r);
            Vec3 before = droplet(variant, i, seed, age - 1.2, d, r);
            Vec3 tail = droplet(variant, i, seed, age - 2.6, d, r);
            Vec3 motion = at.subtract(before);
            Vec3 axis = motion.lengthSqr() < 1e-8 ? d : motion.normalize();
            double size = r * (.05 + .05 * ThermalMotion.random(seed, i + 101)) * (1 - settle * .6);
            int color = i % 4 == 0 ? CLOT : TINT;
            if (age < CROWN_END + 3)
                ribbonPath(p, v, List.of(tail, before, at), camera, size * .1, size * .6, RIBBON, seed + 10 + i, color, body * .8f);
            card(p, v, at, facingSide(axis, camera.subtract(at)), axis, size, size * 1.7, FOCUS, seed + 40 + i, color, body);
        }

        // 4. Ligaments: in the first instants thick sinew still binds the leading droplets to the wound.
        if (age < CROWN_END) {
            float hold = alpha * (1 - age / CROWN_END);
            for (int i = 0; i < (variant == NEEDLE ? 1 : 3); i++) {
                Vec3 at = droplet(variant, i, seed, age, d, r);
                ribbonPath(p, v, List.of(Vec3.ZERO, at.scale(.5).add(UP.scale(r * .05)), at), camera, r * .05, r * .012,
                        RIBBON, seed + 70 + i, TINT, hold);
            }
        }
    }

    /**
     * Where splash droplet {@code i} sits {@code age} ticks after contact, relative to the contact point. Entity hits
     * spray on through the body, block hits splash back out of the surface, a mortar bursts upward in every direction
     * and a needle puncture spurts a narrow jet along its line.
     */
    static Vec3 droplet(int variant, int i, int seed, double age, Vec3 direction, double radius) {
        double t = Math.max(0, age);
        double spin = ThermalMotion.random(seed, i) * Math.PI * 2;
        double lift = ThermalMotion.random(seed, i + 31), spread = ThermalMotion.random(seed, i + 67);
        Vec3 d = direction.lengthSqr() < 1e-8 ? UP : direction.normalize();
        Vec3[] basis = basis(d);
        Vec3 radial = basis[0].scale(Math.cos(spin)).add(basis[1].scale(Math.sin(spin)));
        Vec3 heading = switch (variant) {
            case BLOCK -> d.scale(-(.35 + .65 * lift)).add(radial.scale(.6 + .6 * spread));
            case MORTAR -> {
                double h = Math.sqrt(1 - Math.pow(.25 + .75 * lift, 2));
                yield new Vec3(Math.cos(spin) * h, .25 + .75 * lift, Math.sin(spin) * h);
            }
            case NEEDLE -> d.scale(.8 + .4 * lift).add(radial.scale(.15 + .25 * spread));
            default -> d.scale(.2 + .9 * lift).add(radial.scale(.45 + .5 * spread));
        };
        heading = heading.normalize();
        double speed = radius * (variant == MORTAR ? .42 : .28) * (.55 + .45 * spread);
        double travelled = (1 - Math.exp(-t * .35)) / .35;
        double fall = .012 * t * t * (variant == MORTAR ? 1.4 : 1);
        return heading.scale(speed * travelled).add(0, -fall, 0);
    }

    /** Two unit vectors perpendicular to {@code axis} and to each other. */
    static Vec3[] basis(Vec3 axis) {
        Vec3 a = axis.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : axis.normalize();
        Vec3 reference = Math.abs(a.y) > .9 ? new Vec3(1, 0, 0) : UP;
        Vec3 first = a.cross(reference).normalize();
        return new Vec3[] { first, first.cross(a).normalize() };
    }
}
