package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Putriciel's presentation contracts (design section 16): no vanilla particle in any Putriciel file, the ember layer
 * returns before drawing in a rot phase, the fire geometry draws only with the fire or the ending, the dispatcher hook
 * order, and the pyres' Flammeus shader fire.
 */
class PutricielPresentationGuardrailTest {
    private static List<Path> putricielFiles() throws Exception {
        List<Path> found = new ArrayList<>();
        try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
            files.filter(path -> path.getFileName().toString().startsWith("Putriciel") && path.toString().endsWith(".java"))
                    .forEach(found::add);
        }
        return found;
    }

    @Test void noPutricielFileUsesAVanillaParticleOrHitGlow() throws Exception {
        List<Path> files = putricielFiles();
        assertTrue(files.size() >= 20, "found " + files.size());
        for (Path file : files) {
            String source = SourceAssertions.read(file);
            assertFalse(source.contains("ParticleTypes."), file + " uses a vanilla particle");
            assertFalse(source.contains("hitGlow("), file + " uses hitGlow; traces are glow motes");
        }
    }

    @Test void theEmberLayerNeverDrawsInTheRot() {
        String layer = SourceAssertions.readMainJava("client/render/layer/mob/PutricielEmberLayer.java");
        String body = SourceAssertions.methodBody(layer, "public void render(");
        int gate = body.indexOf("motion.stage() == PutricielStage.ROT) return;");
        assertTrue(gate > 0, "the ember layer returns in a rot phase");
        assertTrue(gate < body.indexOf("renderToBuffer("), "before it draws");
    }

    @Test void theFireGeometryDrawsOnlyWithTheFireOrTheEnding() {
        String facade = SourceAssertions.readMainJava("client/render/world/PutricielOfferingGeometry.java");
        assertTrue(facade.contains("if (body.fire()) PutricielFireGeometry.body("));
        assertTrue(facade.contains("return motion.stage().fire() || motion.endingTime() >= 0.0F;"));
        String fire = SourceAssertions.readMainJava("client/render/world/PutricielFireGeometry.java");
        assertTrue(SourceAssertions.methodBody(fire, "static void body(").contains("if (ember <= .01F) return;"));
    }

    @Test void bespokeGeometryUsesTheSharedDispatcherBeforeGenericGeometry() {
        String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("PutricielOfferingGeometry.collect(world, poses, FLOWS"));
        assertTrue(renderer.indexOf("if(PutricielOfferingGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
        assertTrue(SourceAssertions.readMainJava("client/render/world/ManipulationMotes.java").contains("if(PutricielOfferingGeometry.handles(form))return;"));
    }

    /** Track 10: the pyres burn the Flammeus shader fire the Iron Brazier burns, with Hemomancy particles only. */
    @Test void pyresBurnTheFlammeusShaderFireWithHemomancyParticles() {
        String flames = SourceAssertions.readMainJava("client/render/world/PyreFlames.java");
        assertTrue(flames.contains("ThermalRenderTypes.STEADY_FLAME"));
        assertTrue(flames.contains("LuxUmbraGeometry.card("));
        assertTrue(flames.contains("ThermalParticles.lick("));
        assertTrue(SourceAssertions.readMainJava("client/render/tile/harbinger/decoration/RotPyreRenderer.java")
                .contains("PyreFlames.render("));
        for (String source : new String[]{"client/render/world/PyreFlames.java",
                "common/block/harbinger/decoration/RotPyreBlock.java",
                "common/block/harbinger/decoration/ReliquarySconceBlock.java",
                "common/block/harbinger/puzzle/SpawnMarkBlock.java",
                "common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java"}) {
            assertFalse(SourceAssertions.readMainJava(source).contains("ParticleTypes."), source);
        }
    }

    @Test void putricielIsOnTheKitAndTheLegacyPiecesAreGone() {
        String boss = SourceAssertions.readMainJava("common/entity/boss/saint/putriciel/PutricielEntity.java");
        assertTrue(boss.contains("protected SaintKit createKit()"));
        for (String legacy : new String[]{"registerTrialGoals(", "tickSecondaryAttacks(", "setVisualState(", "sendFloorRing(",
                "SaintAttackPattern", "tickRise()", "doHurtTarget(", "WITHER_AMBIENT"}) {
            assertFalse(boss.contains(legacy), "PutricielEntity still uses " + legacy);
        }
        assertTrue(boss.contains("BloodAccounting.Source.of(\"putriciel_offering\""), "the Offering's named source");
        assertFalse(Files.exists(RepoPaths.mainJava("common/entity/boss/saint/putriciel/PutricielCleansingRules.java")));
    }

    // ── visual pass 2 (2026-11-06) ───────────────────────────────────

    /** Their own pass draws every telegraph and hazard in both of the dispatcher's passes; the kit's tints never show. */
    @Test void theirOwnPassClaimsEveryMoveAndHazard() {
        String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("PutricielTelegraphPass.collect(world, poses, FLOWS, camera, right, up, time, partial, solids);"),
                "the pass runs in the solid and the translucent pass");
        String style = SourceAssertions.readMainJava("client/render/world/PutricielTelegraphStyle.java");
        assertTrue(style.contains("return draw.move() instanceof PutricielMove;"));
        assertTrue(style.contains("return PutricielHazard.byId(draw.hazard().kind()) != null;"));
        assertFalse(style.contains("SaintTelegraphPainter") || style.contains("canvas."), "the style paints nothing itself");
        String pass = SourceAssertions.readMainJava("client/render/world/PutricielTelegraphPass.java");
        for (String move : new String[]{"CENSER_SWING", "CENSER_BACKSWING", "CENSER_DROP", "OFFERING_GRASP", "ROT_BREATH", "SAC_SWELL",
                "SHELF_VOLLEY", "ROOT_SNARE", "CENSER_ORBIT", "MOULD_COMMUNION", "BLOOM_OF_ROT", "IGNITION", "PURGING_SWEEP", "CENSER_CAST",
                "CAUTERISING_LANE", "PYRE_HEART", "PYRE_LINES", "FULL_PURGE", "OFFERING", "EMBERING"}) {
            assertTrue(pass.contains("case " + move + " ->") || pass.contains("case " + move + ",") || pass.contains(", " + move + " ->"),
                    "the pass draws " + move);
        }
        for (String hazard : new String[]{"ROT_COLONY", "ROT_CLOUD", "PURGING_FIRE", "FIRE_WALL"}) {
            assertTrue(pass.contains("case " + hazard + " ->"), "the pass draws the " + hazard + " hazard");
        }
    }

    /** Their materials are their own shaders, registered once, with render types built once. */
    @Test void theirMaterialsAreTheirOwnShadersBuiltOnce() {
        String shaders = SourceAssertions.readMainJava("client/render/shader/ShaderInit.java");
        String types = SourceAssertions.readMainJava("client/render/world/PutricielRenderTypes.java");
        for (String name : new String[]{"ground", "spore", "growth"}) {
            String holder = "PUTRICIEL_" + name.toUpperCase(java.util.Locale.ROOT);
            assertTrue(shaders.contains("public static ShaderHolder " + holder + " = new ShaderHolder(Hemomancy.rloc(\"world/putriciel_" + name + "\")"));
            assertTrue(shaders.contains("registerShader(event, " + holder + ".createInstance(provider));"));
            assertTrue(types.contains("static final RenderType " + name.toUpperCase(java.util.Locale.ROOT) + " = SchoolRenderTypes.create(CLOCK, \"putriciel_"
                    + name + "\", ShaderInit." + holder), "built once as a constant");
            for (String extension : new String[]{".json", ".vsh", ".fsh"}) {
                assertTrue(Files.exists(RepoPaths.ASSETS.resolve("shaders/core/world/putriciel_" + name + extension)), name + extension);
            }
        }
        assertTrue(types.replaceAll("\\s+", " ").contains("ShaderInit.PUTRICIEL_GROWTH, DefaultVertexFormat.POSITION_TEX_COLOR, true, true"),
                "the growths are solid and write depth");
    }

    /**
     * The ruling in colour: no rot colour in their shaders leans red, and no fire colour leans green or yellow (its
     * green never stands over its blue by more than a hair).
     */
    @Test void theRotIsNeverRedAndTheFireNeverGreenOrYellow() {
        java.util.Set<String> fire = java.util.Set.of("SOOT", "CHAR", "CHARCOAL", "ASH", "EMBER", "CRIMSON", "HEAT", "WHITE_HOT");
        java.util.regex.Pattern constant = java.util.regex.Pattern.compile(
                "const vec3 (\\w+) = vec3\\(([0-9.]+), ([0-9.]+), ([0-9.]+)\\);");
        int rot = 0, flame = 0;
        for (String name : new String[]{"putriciel_ground", "putriciel_spore", "putriciel_growth"}) {
            String shader = SourceAssertions.read(RepoPaths.ASSETS.resolve("shaders/core/world/" + name + ".fsh"));
            java.util.regex.Matcher m = constant.matcher(shader);
            while (m.find()) {
                double r = Double.parseDouble(m.group(2)), g = Double.parseDouble(m.group(3)), b = Double.parseDouble(m.group(4));
                if (fire.contains(m.group(1))) {
                    assertFalse(name.equals("putriciel_spore"), "the spores carry no fire: " + m.group(1));
                    assertTrue(g <= b * 1.1D + 0.01D, name + " " + m.group(1) + " leans green or yellow");
                    flame++;
                } else {
                    assertTrue(r <= g * 1.35D + 0.01D, name + " " + m.group(1) + " leans red");
                    rot++;
                }
            }
        }
        assertTrue(rot > 15 && flame > 8, "the palettes were read: " + rot + " rot, " + flame + " fire");
    }

    // ── revision 2026-11-06: the purifier on fire and the rot sludge ──

    /**
     * The body fire and the burning layer draw only in the stages of R1.9: IGNITING and BURNING, the Embering under its
     * tick 45 (the layer under 30), and the ending's fire beats; never in a rot stage.
     */
    @Test void theBodyFireAndTheBurningLayerDrawOnlyInTheFire() {
        String rules = SourceAssertions.readMainJava("common/entity/boss/saint/putriciel/PutricielBodyFireRules.java");
        String body = SourceAssertions.methodBody(rules, "public static boolean drawsBody(");
        assertTrue(body.contains("case ROT -> false;"), "the body fire never draws in the rot");
        assertTrue(body.contains("case EMBERING -> m.stageTime() < SMOKE_OUT;"));
        assertTrue(body.contains("return m.endingTime() >= ENDING_FROM && m.endingTime() < ENDING_OUT;"));
        String layer = SourceAssertions.methodBody(rules, "public static boolean drawsLayer(");
        assertTrue(layer.contains("case ROT -> false;") && layer.contains("case EMBERING -> m.stageTime() < EMBERS_OUT;"));
        assertTrue(rules.contains("SMOKE_OUT = 45.0F") && rules.contains("EMBERS_OUT = 30.0F"), "45 for the body, 30 for the layer");
        assertTrue(SourceAssertions.methodBody(rules, "public static Flame anchor(").contains("if (!drawsBody(m)) return Flame.NONE;"));
        assertFalse(rules.contains("net.minecraft.client") || rules.contains("hemomancy.client"), "the rules name no client class");

        String geometry = SourceAssertions.readMainJava("client/render/world/PutricielBodyFireGeometry.java");
        assertTrue(SourceAssertions.methodBody(geometry, "static void draw(").contains("if (!PutricielBodyFireRules.drawsBody(m)) return;"));
        String collect = SourceAssertions.methodBody(geometry, "static void collect(");
        int gate = collect.indexOf("if (!burning) return;");
        assertTrue(gate > 0 && gate < collect.indexOf("draw(s.p(), s.flame()"), "the pass's body fire returns before drawing out of the fire");
        assertTrue(SourceAssertions.readMainJava("client/render/world/PutricielTelegraphPass.java").contains("PutricielBodyFireGeometry.collect(s, boss);"),
                "the body fire draws in the pass's fire layer");

        String burn = SourceAssertions.readMainJava("client/render/layer/mob/PutricielBurnLayer.java");
        int burnGate = burn.indexOf("PutricielBodyFireRules.drawsLayer(");
        assertTrue(burnGate > 0, "the burning layer reads the same gate");
        int draw = burn.indexOf("renderToBuffer(");
        assertTrue(draw < 0 || burnGate < draw, "before it draws");
    }

    /** The sludge is drawn by their own pass in all three layers, and every floor read rides it. */
    @Test void theSludgeAndItsFireAreDrawnByTheirOwnPass() {
        String pass = SourceAssertions.readMainJava("client/render/world/PutricielTelegraphPass.java");
        assertTrue(pass.contains("case ROT_SLUDGE, SLUDGE_FIRE ->"), "the pass draws the pools and their fire");
        assertTrue(pass.contains("case FIRE_RUNNER ->"), "and the runners");
        assertTrue(pass.contains("PutricielGroundGeometry.begin(PutricielSludgeField.of(hazards, partial));"), "the frame's pools are known first");
        String ground = SourceAssertions.readMainJava("client/render/world/PutricielGroundGeometry.java");
        assertTrue(ground.contains("y + field.lift(wx, wz)"), "a floor patch rides the pools");
        assertTrue(ground.contains("field.lift(edge[0], edge[1])"), "and so does a trench");
        String sludge = SourceAssertions.readMainJava("client/render/world/PutricielSludgeGeometry.java");
        assertTrue(sludge.contains("static final int SLUDGE = 5;"));
        String growth = SourceAssertions.read(RepoPaths.ASSETS.resolve("shaders/core/world/putriciel_growth.fsh"));
        assertTrue(growth.contains("if (!sludge && formation < 0.995 && threshold > formation) discard;"), "the sludge's alpha is its place");
        assertTrue(growth.contains("// SLUDGE: thick wet rot with body"));
        assertTrue(SourceAssertions.read(RepoPaths.ASSETS.resolve("shaders/core/world/putriciel_spore.fsh")).contains("// ASH: an uneven flake"));
        assertTrue(SourceAssertions.read(RepoPaths.ASSETS.resolve("shaders/core/world/putriciel_ground.fsh")).contains("// ── STAIN:"));
        // the new constants are read by the colour ruling above
        for (String constant : new String[]{"SLUDGE_DEEP", "SLUDGE_SHALLOW", "GLOSS", "SCUM", "MENISCUS", "COLD_SHEEN", "MATTE_CRUST"}) {
            assertTrue(growth.contains("const vec3 " + constant + " = vec3("), constant);
        }
    }

    /** The revision's files build no render type, draw no vanilla particle and keep the fire on the Flammeus material. */
    @Test void theRevisionsFilesBuildNoRenderTypeAndNoVanillaParticle() {
        for (String file : new String[]{"client/render/world/PutricielBodyFireGeometry.java", "client/render/world/PutricielSludgeGeometry.java",
                "client/render/world/PutricielSludgeField.java", "common/entity/boss/saint/putriciel/PutricielBodyFireRules.java"}) {
            String source = SourceAssertions.readMainJava(file);
            assertFalse(source.contains("ParticleTypes."), file + " uses a vanilla particle");
            assertFalse(source.contains("RenderType.create(") || source.contains("SchoolRenderTypes.create(") || source.contains("getBuffer("),
                    file + " builds or binds a render type of its own");
            assertFalse(source.contains("hitGlow("), file);
        }
        String geometry = SourceAssertions.readMainJava("client/render/world/PutricielBodyFireGeometry.java");
        assertTrue(geometry.contains("HemoParticleData.emberSpark("), "the ember sparks are Hemomancy particles");
        assertTrue(geometry.contains("ParticleStatus.MINIMAL) return;"), "none under Minimal particles");
    }
}
