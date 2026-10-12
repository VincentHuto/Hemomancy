package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Wiring for the Blood Shot flight body and splash. */
class BloodShotVisualSourceTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final String WORLD = "src/main/java/com/vincenthuto/hemomancy/client/render/world/";

    @Test void splashFormIsAppendedAndDrawnBeforeTheSharedBloodTier() throws IOException {
        String visuals = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationVisuals.java");
        assertTrue(visuals.contains("RIPOSTE_STRIKE, BLOOD_SPLASH"), "forms are encoded by ordinal and must be appended");
        String renderer = read(WORLD + "ManipulationVisualRenderer.java");
        int splash = renderer.indexOf("if(BloodShotGeometry.handles(packet.form()))");
        assertTrue(splash > 0 && splash < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
        assertTrue(renderer.contains("BloodProjectileEffects.shotRemoved(shot)"), "removed shots must splash, not leave generic residue");
        assertFalse(renderer.contains("void bloodShot("), "the retired textured projectile mesh came back");
    }

    @Test void flightDelegatesToTheLayeredBodyAndLodgedShotsSplashOnContact() throws IOException {
        String effects = read(WORLD + "BloodProjectileEffects.java");
        assertTrue(effects.contains("BloodShotGeometry.shot("));
        assertTrue(effects.contains("lodge(trail,now,BloodShotGeometry.splashRadius("));
        assertTrue(effects.contains("shot.visualForm()==2 || trail!=null && trail.splashed"),
                "mortars and already-lodged shots must not splash twice");
    }

    @Test void mortarBlastUsesTheSplashMortarVariant() throws IOException {
        String entity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/BloodShotEntity.java");
        assertTrue(entity.contains("Form.BLOOD_SPLASH,position(),position().add(heading),2,16,3)"));
        assertTrue(read(WORLD + "ManipulationBoundaryStyle.java").contains("case BLOOD_SPLASH -> count==BloodShotGeometry.MORTAR"),
                "the mortar keeps its blast ring");
    }

    @Test void needlesDrawInTheSharedPassAndSplashOnPuncture() throws IOException {
        String renderer = read(WORLD + "ManipulationVisualRenderer.java");
        int charge = renderer.indexOf("if(ForgedNeedleGeometry.handlesCharge(packet.form()))");
        assertTrue(charge > 0 && charge < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
        assertTrue(renderer.contains("needle && !needle.isCoronationSword()) {\n            BloodProjectileEffects.needleRemoved(needle)"),
                "coronation swords keep their own impact; ordinary needles splash");
        String effects = read(WORLD + "BloodProjectileEffects.java");
        assertTrue(effects.contains("BloodNeedleGeometry.flight("));
        assertTrue(effects.contains("ForgedNeedleGeometry.flightFlow(") && effects.contains("ForgedNeedleGeometry.flightIron("),
                "forged Ferric needles draw their iron and their blood in separate passes");
        assertTrue(effects.contains("lodge(trail,now,BloodNeedleGeometry.PUNCTURE)"));
        String needleRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/projectile/BloodNeedleRenderer.java");
        assertFalse(needleRenderer.contains("drawVertex"), "the flat arrow mesh would double-draw the needle");
        assertTrue(needleRenderer.contains("projectileSword("), "coronation swords still render through the entity");
    }

    @Test void lodgedProjectilesKeepTheirFlightHeadingAndStainTheStruckFace() throws IOException {
        String effects = read(WORLD + "BloodProjectileEffects.java");
        assertTrue(effects.contains("if(!trail.splashed && (trail.points.isEmpty()"),
                "server corrections after lodging turned every needle to one axis");
        assertTrue(effects.contains("return velocity.lengthSqr()>1e-6?velocity:trail.heading;"));
        assertTrue(effects.contains("world.clip(") && effects.contains("hit.getDirection().getNormal()"),
                "stains must lie on the struck face, not stand across the flight line");
    }

    @Test void onlyStruckBodiesSplashAndWakesAreTrimmed() throws IOException {
        String effects = read(WORLD + "BloodProjectileEffects.java");
        assertTrue(effects.contains("if(!struckBody(shot,trail))return;") && effects.contains("if(!struckBody(needle,trail))return;"),
                "projectiles leaving tracking range arrive as discards and must not splash in the sky");
        assertTrue(effects.contains("BloodShotGeometry.shot(p,v,trim(path,") && effects.contains("BloodNeedleGeometry.flight(p,v,trim(path,"));
    }

    @Test void guidedShotsResyncWhileSteering() throws IOException {
        String entity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/BloodShotEntity.java");
        int steer = entity.indexOf("private void steerTowardTarget()");
        assertTrue(steer > 0 && entity.indexOf("syncLaunchVelocity();", steer) > steer,
                "without a per-tick resync the client flies straight and snaps");
    }

    @Test void fastProjectilesSyncTheirTrueVelocity() throws IOException {
        // Vanilla clamps synced velocity to 3.9 blocks/tick per axis; needles and shots launch faster.
        for (String name : new String[] { "BloodNeedleEntity", "BloodShotEntity" }) {
            String entity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/" + name + ".java");
            assertTrue(entity.contains("EntityDataSerializers.VECTOR3"), name);
            assertTrue(entity.contains("super.shoot(x, y, z, velocity, inaccuracy);\n        syncLaunchVelocity();"), name);
            assertTrue(entity.contains("if (velocity.lengthSquared() > 0) setDeltaMovement(velocity.x, velocity.y, velocity.z);"), name);
        }
    }

    private static String read(String path) throws IOException {
        return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
    }
}
