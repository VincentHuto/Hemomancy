package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringSim.Blocks;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringSim.Contact;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringSim.Kind;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringSim.Mote;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OutpouringSimTest {
    private static final OutpouringRules.Profile PERFECTUS = OutpouringRules.profile(EnumManipulationRank.PERFECTUS);
    private static final int TOP = 4;
    private static final Blocks FLOOR = blocks((x, y, z) -> y < 64);
    private final List<OutpouringSim> sims = new ArrayList<>();

    interface Solid { boolean at(int x, int y, int z); }
    private static Blocks blocks(Solid solid) {
        return new Blocks() {
            public boolean solid(int x, int y, int z) { return solid.at(x, y, z); }
            public boolean liquid(int x, int y, int z) { return false; }
        };
    }

    private OutpouringSim sim(EnumBloodTendency school) {
        var sim = new OutpouringSim(school, null, 42);
        sims.add(sim);
        return sim;
    }

    @AfterEach void release() { sims.forEach(OutpouringSim::discard); }

    private static void burst(OutpouringSim sim, double x, double y, double z, double ox, double oy, double oz) {
        sim.emit(new double[]{x, y, z}, 1, new double[]{ox, oy, oz}, 1, PERFECTUS, TOP, true, false);
    }

    private static List<Contact> run(OutpouringSim sim, Blocks blocks, int steps) {
        List<Contact> all = new ArrayList<>();
        for (int i = 0; i < steps; i++) all.addAll(sim.step(blocks, .5, 64, .5, false, 64));
        return all;
    }

    @Test void animusFeedsAWhirlwind() {
        var sim = sim(EnumBloodTendency.ANIMUS);
        burst(sim, .5, 65.5, .8, 0, 0, 1);
        var contacts = run(sim, FLOOR, 30);
        assertTrue(contacts.stream().anyMatch(c -> c.face() == OutpouringSim.UP), "landing blood pools on the floor");
        Mote wisp = sim.motes().stream().filter(m -> m.kind == Kind.WISP).findFirst().orElseThrow();
        double before = Math.atan2(wisp.z - .5, wisp.x - .5), height = wisp.y;
        run(sim, FLOOR, 6);
        assertNotEquals(before, Math.atan2(wisp.z - .5, wisp.x - .5), 1e-3, "the whirlwind turns round the caster");
        assertTrue(wisp.y > height, "and climbs");
    }

    @Test void flammeusRises() {
        var sim = sim(EnumBloodTendency.FLAMMEUS);
        burst(sim, .5, 65, .5, 0, 0, 1);
        run(sim, FLOOR, 6);
        double mean = sim.motes().stream().filter(m -> m.kind == Kind.FLAME).mapToDouble(m -> m.y).average().orElseThrow();
        assertTrue(mean > 65, "flames climb: " + mean);
    }

    @Test void congeatioFreezesOnContact() {
        var sim = sim(EnumBloodTendency.CONGEATIO);
        var walled = blocks((x, y, z) -> y < 64 || x >= 2);
        burst(sim, 1.2, 66.5, .5, 1, 0, 0);
        var contacts = run(sim, walled, 20);
        assertTrue(contacts.stream().anyMatch(c -> c.face() == 4 && c.bx() == 2), "frost lands on the wall's west face");
        assertTrue(sim.motes().stream().anyMatch(m -> m.kind == Kind.SHARD && m.stuck));
    }

    @Test void ductilisSnapsAndSparks() {
        var sim = sim(EnumBloodTendency.DUCTILIS);
        var box = blocks((x, y, z) -> Math.abs(x) >= 2 || Math.abs(z) >= 2 || y < 64 || y >= 68);
        burst(sim, .5, 65.5, .5, 0, 0, 1);
        assertTrue(run(sim, box, 15).stream().anyMatch(Contact::spark));
    }

    @Test void luxOrbsNeverContact() {
        var sim = sim(EnumBloodTendency.LUX);
        var box = blocks((x, y, z) -> Math.abs(x) >= 2 || Math.abs(z) >= 2 || y < 64 || y >= 68);
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            sim.emit(new double[]{.5, 65.5, .7}, 1, new double[]{0, 0, 1}, 1, PERFECTUS, TOP, i == 0, false);
            contacts.addAll(sim.step(box, .5, 64, .5, false, 64));
            assertTrue(sim.motes().stream().filter(m -> m.kind == Kind.ORB).count() <= OutpouringSim.orbCap(TOP));
        }
        assertTrue(contacts.isEmpty());
    }

    @Test void tenebrisSinksAndSpreads() {
        var sim = sim(EnumBloodTendency.TENEBRIS);
        burst(sim, .5, 65.5, .5, 0, 0, 1);
        run(sim, FLOOR, 5);
        double early = meanRadius(sim);
        run(sim, FLOOR, 15);
        assertTrue(meanRadius(sim) > early);
    }

    private static double meanRadius(OutpouringSim sim) {
        return sim.motes().stream().mapToDouble(m -> Math.hypot(m.x - .5, m.z - .5)).average().orElseThrow();
    }

    @Test void withdrawReturnsToExit() {
        var sim = sim(EnumBloodTendency.ANIMUS);
        burst(sim, .5, 65.5, .8, 0, 0, 1);
        run(sim, FLOOR, 3);
        for (int i = 0; i < 20; i++) sim.step(FLOOR, .5, 64, .5, true, 64);
        assertEquals(0, sim.size());
        assertTrue(sim.finished());
    }

    @Test void enclosedCasterKeepsMotesInsideAndBounded() {
        Solid tunnel = (x, y, z) -> x != 0 || y < 64 || y > 65;
        var blocks = blocks(tunnel);
        for (var school : EnumBloodTendency.values()) {
            var sim = sim(school);
            for (int i = 0; i < 8; i++) burst(sim, .5, 65.5, .7, 0, 0, 1);
            for (int i = 0; i < 30; i++) {
                sim.step(blocks, .5, 64, .5, false, 64);
                for (Mote m : sim.motes())
                    assertFalse(tunnel.at((int) Math.floor(m.x), (int) Math.floor(m.y), (int) Math.floor(m.z)),
                            school + " " + m.kind + " inside a wall at " + m.x + "," + m.y + "," + m.z);
                assertTrue(sim.size() <= OutpouringSim.PER_CAST_CAP);
            }
        }
    }

    @Test void globalMoteCapHolds() {
        List<OutpouringSim> many = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            var sim = sim(EnumBloodTendency.MORTEM);
            for (int b = 0; b < 10; b++) burst(sim, .5, 65.5, .5, 0, 0, 1);
            many.add(sim);
        }
        assertTrue(many.stream().mapToInt(OutpouringSim::size).sum() <= OutpouringSim.GLOBAL_CAP);
    }

    @Test void contactsRespectBudget() {
        var sim = sim(EnumBloodTendency.CONGEATIO);
        for (int b = 0; b < 4; b++) burst(sim, .5, 64.4, .5, 0, -1, 0);
        for (int i = 0; i < 10; i++) assertTrue(sim.step(FLOOR, .5, 64, .5, false, 1).size() <= 1);
    }

    @Test void woundOnlyIsATrickle() {
        var full = sim(EnumBloodTendency.ANIMUS);
        var wound = sim(EnumBloodTendency.ANIMUS);
        full.emit(new double[]{.5, 65.5, .7}, 1, new double[]{0, 0, 1}, 1, PERFECTUS, TOP, true, false);
        wound.emit(new double[]{.5, 65.5, .7}, 1, new double[]{0, 0, 1}, 1, PERFECTUS, TOP, true, true);
        assertTrue(wound.size() < full.size() / 4, "the spell's own blood carries the effect");
    }

    @Test void luxAndUmbraUseManySmallMotes() {
        assertTrue(OutpouringSim.orbCap(TOP) >= 16, "many small orbs at Perfectus");
        var lux = sim(EnumBloodTendency.LUX);
        burst(lux, .5, 65.5, .5, 0, 0, 1);
        assertTrue(lux.motes().stream().allMatch(m -> m.size <= .12f), "orbs stay small");
        var umbra = sim(EnumBloodTendency.TENEBRIS);
        burst(umbra, .5, 65.5, .5, 0, 0, 1);
        assertTrue(umbra.motes().stream().allMatch(m -> m.size <= .2f), "fog starts as small puffs");
    }

    @Test void umbralFogFallsHeavyAndRollsSlowly() {
        var sim = sim(EnumBloodTendency.TENEBRIS);
        burst(sim, .5, 66.5, .5, 0, 0, 1);
        double startY = sim.motes().stream().mapToDouble(m -> m.y).average().orElseThrow();
        double startR = meanRadius(sim);
        run(sim, FLOOR, 8);
        double drop = startY - sim.motes().stream().mapToDouble(m -> m.y).average().orElseThrow();
        double spread = meanRadius(sim) - startR;
        assertTrue(drop > spread * 1.5, "the fog pours down before it spreads: drop " + drop + " spread " + spread);
        run(sim, FLOOR, 12);
        for (Mote m : sim.motes()) {
            double speed = Math.hypot(m.vx, m.vz);
            assertTrue(speed < .06, "settled fog rolls slowly: " + speed);
        }
    }
}
