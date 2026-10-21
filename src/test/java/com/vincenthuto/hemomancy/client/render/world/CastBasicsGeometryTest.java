package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestRules;
import com.vincenthuto.hemomancy.common.manipulation.ferric.CellHandRules;
import com.vincenthuto.hemomancy.common.manipulation.ferric.WeaponConjureRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The early-game basics, the Living Arsenal conjure, the cell hands and the avatar draw finite, bounded meshes. */
class CastBasicsGeometryTest {
    private static final CasterFrame THIRD = CasterFrame.standing(false);
    private static final CasterFrame FIRST = CasterFrame.standing(true);
    private static final CasterFrame DASHING = new CasterFrame(30, 30, 1, false, false, THIRD.hand(), new Vec3(0, -.2, 1),
            new Vec3(-.4, 0, .7), new Vec3(.3, 0, -.6), null);
    private static final CasterFrame PALM = new CasterFrame(0, 0, 1, false, true, new Vec3(.3, 1.2, .5), new Vec3(0, 0, 1),
            Vec3.ZERO, Vec3.ZERO, new Vec3(-.4, .5, .77).normalize());
    private static final float[] AGES = { 0, 1, 2.5f, 4, 6, 8, 10, 12, 14, 16, 18, 21, 23.9f };

    @Test void everyBodyAnchoredFormIsClaimedAndNoLongerBySharedTiers() {
        for (Form form : new Form[] { Form.RUSH, Form.WARD, Form.MENDING, Form.WEAPON_CONJURE, Form.CELL_HAND, Form.AVATAR_MANIFEST }) {
            assertTrue(ManipulationVisualRenderer.bodyAnchored(form), form.name());
            assertFalse(AnimusMortemGeometry.handles(form), form.name());
            assertFalse(FerricDuctilisGeometry.handles(form), form.name());
        }
        assertFalse(ManipulationVisualRenderer.bodyAnchored(Form.RIPOSTE));
    }

    @Test void rushWardAndMendingStayFiniteAndBounded() {
        for (CasterFrame frame : new CasterFrame[] { THIRD, FIRST, DASHING })
            for (float age : AGES) {
                Capture rush = new Capture(), flame = new Capture();
                BloodRushGeometry.draw(packet(Form.RUSH, 1), new PoseStack(), rush, flame, CAMERA, R, U, 100 + age, age, 1, frame);
                check(rush, 2400, "rush"); check(flame, 800, "rush flame");
                for (int pool : new int[] { 1, 3, 6 }) {
                    Capture ward = new Capture();
                    SanguineWardGeometry.draw(packet(Form.WARD, pool), new PoseStack(), ward, CAMERA, R, U, 100 + age, age, 1, frame, 1.8, .6);
                    check(ward, 2400, "ward");
                }
                Capture iron = new Capture(), mend = new Capture();
                SanguineMendingGeometry.draw(packet(Form.MENDING, 1), new PoseStack(), iron, mend, CAMERA, R, U, 100 + age, age, 1, frame);
                check(iron, 600, "mending iron"); check(mend, 1600, "mending");
            }
        Capture rush = new Capture();
        BloodRushGeometry.draw(packet(Form.RUSH, 1), new PoseStack(), rush, new Capture(), CAMERA, R, U, 105, 5, 1, DASHING);
        assertFalse(rush.points.isEmpty(), "the launch should draw its wake");
    }

    @Test void wardCrownOnlyClosesOnAFullPool() {
        Capture full = new Capture(), worn = new Capture();
        SanguineWardGeometry.draw(packet(Form.WARD, 6), new PoseStack(), full, CAMERA, R, U, 120, 20, 1, THIRD, 1.8, .6);
        SanguineWardGeometry.draw(packet(Form.WARD, 5), new PoseStack(), worn, CAMERA, R, U, 120, 20, 1, THIRD, 1.8, .6);
        assertTrue(full.points.size() > worn.points.size(), "a full pool adds the crown");
        double top = full.points.stream().mapToDouble(p -> p.y).max().orElse(0);
        assertTrue(top > 1.6, "the crown should sit over the head: " + top);
    }

    @Test void everyWeaponConjuresInBothPasses() {
        for (int weapon = 0; weapon < WeaponConjureRules.WEAPONS; weapon++)
            for (CasterFrame frame : new CasterFrame[] { THIRD, FIRST })
                for (float age : AGES) {
                    var batch = new LuxUmbraBatch();
                    Capture iron = new Capture(), core = new Capture(), flows = new Capture();
                    var packet = packet(Form.WEAPON_CONJURE, WeaponConjureRules.encode(weapon, 3));
                    WeaponConjureGeometry.draw(packet, new PoseStack(), batch, iron, core, true, CAMERA, R, U, 100 + age, age, 1, frame);
                    WeaponConjureGeometry.draw(packet, new PoseStack(), batch, FerricDuctilisGeometry.DISCARD, core, false, CAMERA, R, U,
                            100 + age, age, 1, frame);
                    batch.drawMaterials(material -> flows);
                    check(flows, 4000, "weapon " + weapon);
                    check(iron, 1200, "weapon iron " + weapon);
                    if (age >= 8 && age <= 16) assertFalse(flows.points.isEmpty(), "weapon " + weapon + " at " + age);
                    if (WeaponConjureRules.school(weapon) != WeaponConjureRules.School.FERRIC) assertTrue(iron.points.isEmpty());
                }
    }

    @Test void aHeldWeaponIsCoatedOnItsOwnFacesFromTheGripOut() {
        // A stand-in weapon: a column of faces rising from the grip, as the held-pose capture would place them.
        Vec3 grip = new Vec3(.35, .9, .3);
        List<Vec3[]> faces = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Vec3 base = grip.add(0, i * .07, 0);
            faces.add(new Vec3[] { base, base.add(.05, 0, 0), base.add(.05, .07, 0), base.add(0, .07, 0) });
        }
        var held = new WeaponConjureGeometry.Held(faces, grip);
        double early = 0, late = 0;
        for (float age : AGES) {
            var batch = new LuxUmbraBatch();
            Capture flows = new Capture(), iron = new Capture();
            var packet = packet(Form.WEAPON_CONJURE, WeaponConjureRules.encode(WeaponConjureRules.BLADE, 1));
            WeaponConjureGeometry.draw(packet, new PoseStack(), batch, iron, new Capture(), false, CAMERA, R, U, 100 + age, age, 1, THIRD, held, null);
            batch.drawMaterials(material -> flows);
            check(flows, 4000, "held blade at " + age);
            assertTrue(iron.points.isEmpty(), "the real weapon carries the iron; the coat adds none");
            double top = flows.points.stream().mapToDouble(p -> p.y).max().orElse(0);
            if (age == 6) early = top;
            if (age == 10) late = top;
        }
        assertTrue(late > early + .2, "the blood spreads further up the weapon as the sketch runs: " + early + " -> " + late);
    }

    @Test void cellHandsAndAvatarStayFiniteAndBounded() {
        for (int mode = CellHandRules.SUMMON_PROJECTION; mode <= CellHandRules.ABSORBING; mode++)
            for (CasterFrame frame : new CasterFrame[] { THIRD, FIRST, PALM })
                for (float age : AGES) {
                    Capture hand = new Capture();
                    CellHandGeometry.draw(packet(Form.CELL_HAND, mode), new PoseStack(), hand, CAMERA, R, U, 100 + age, age, 1, frame);
                    check(hand, 3200, "cell hand " + mode);
                }
        for (int stage = 0; stage < AvatarManifestRules.STAGES; stage++)
            for (int count : new int[] { AvatarManifestRules.manifest(stage), AvatarManifestRules.dismiss(stage) })
                for (CasterFrame frame : new CasterFrame[] { THIRD, FIRST })
                    for (float age : AGES) {
                        Capture avatar = new Capture();
                        AvatarManifestGeometry.draw(packet(Form.AVATAR_MANIFEST, count), new PoseStack(), avatar, CAMERA, R, U,
                                100 + age, age, 1, frame);
                        check(avatar, 3200, "avatar " + count);
                    }
    }

    @Test void inUseTheHandReachesWhatItDrainsOrPoursInto() {
        Vec3 pig = new Vec3(.2, .5, 3.1), floor = new Vec3(-.4, 0, 2.6);
        for (CasterFrame frame : new CasterFrame[] { THIRD, PALM }) {
            Capture drink = new Capture(), pour = new Capture();
            CellHandGeometry.draw(packet(Form.CELL_HAND, CellHandRules.ABSORBING), new PoseStack(), drink, CAMERA, R, U, 110, 10, 1,
                    frame, List.of(pig, pig.add(1, 0, 0)));
            CellHandGeometry.draw(packet(Form.CELL_HAND, CellHandRules.PROJECTING), new PoseStack(), pour, CAMERA, R, U, 110, 10, 1,
                    frame, List.of(floor));
            check(drink, 4000, "absorbing with targets");
            check(pour, 2000, "projecting onto a landing");
            assertTrue(drink.points.stream().anyMatch(p -> p.distanceTo(pig) < .2), "the threads root into the drained body");
            assertTrue(pour.points.stream().anyMatch(p -> p.distanceTo(floor) < .2), "the ropes land where the blood is poured");
            assertTrue(pour.points.stream().anyMatch(p -> p.distanceTo(frame.hand()) < .2), "the ropes leave the palm");
        }
    }

    @Test void laterAvatarStagesGrowMoreSinew() {
        Capture bare = new Capture(), complete = new Capture();
        AvatarManifestGeometry.draw(packet(Form.AVATAR_MANIFEST, AvatarManifestRules.manifest(0)), new PoseStack(), bare, CAMERA, R, U, 112, 12, 1, THIRD);
        AvatarManifestGeometry.draw(packet(Form.AVATAR_MANIFEST, AvatarManifestRules.manifest(4)), new PoseStack(), complete, CAMERA, R, U, 112, 12, 1, THIRD);
        assertTrue(complete.points.size() > bare.points.size() * 2);
        double tallest = complete.points.stream().mapToDouble(p -> p.y).max().orElse(0);
        assertTrue(tallest > 3, "the complete avatar is twice the caster's height: " + tallest);
    }

    @Test void firstPersonKeepsTheAvatarBelowTheEyes() {
        Capture avatar = new Capture();
        AvatarManifestGeometry.draw(packet(Form.AVATAR_MANIFEST, AvatarManifestRules.manifest(4)), new PoseStack(), avatar, CAMERA, R, U, 105, 5, 1, FIRST);
        assertTrue(avatar.points.stream().allMatch(p -> p.y < 1.4), "first person should not wall off the view");
    }

    private static final Vec3 CAMERA = new Vec3(3, 2.5, -5), R = new Vec3(1, 0, 0), U = new Vec3(0, 1, 0);

    private static ManipulationVisualPacket packet(Form form, int count) {
        return new ManipulationVisualPacket(form, 7, Vec3.ZERO, Vec3.ZERO, 1, 24, count);
    }

    private static void check(Capture mesh, int budget, String what) {
        assertEquals(0, mesh.points.size() % 4, what);
        assertTrue(mesh.points.size() <= budget, what + " exceeded its budget: " + mesh.points.size());
        assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr())), what);
        assertTrue(mesh.points.stream().allMatch(p -> p.lengthSqr() < 64), what + " strayed from the caster");
    }

    static final class Capture implements VertexConsumer {
        final List<Vec3> points = new ArrayList<>();
        public VertexConsumer addVertex(float x, float y, float z) { points.add(new Vec3(x, y, z)); return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        public VertexConsumer setUv(float u, float v) { assertTrue(Float.isFinite(u) && Float.isFinite(v)); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
