package com.vincenthuto.hemomancy.common.worldgen;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.Material.AIR;
import static com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.Material.SCAB;
import static org.junit.jupiter.api.Assertions.*;

class PhlegethonticFormationPlanTest {
    @Test void dryCavernProducesDeterministicFloorAndRoofFormationsInsideItsClearance() {
        Map<PhlegethonticTerrainPlan.Voxel,PhlegethonticTerrainPlan.Material> terrain=new HashMap<>();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,39,z),SCAB);
            for(int y=40;y<=61;y++)terrain.put(new PhlegethonticTerrainPlan.Voxel(x,y,z),AIR);
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,62,z),SCAB);
        }

        var first=PhlegethonticFormationPlan.plan(8675309L,0,0,terrain);
        var second=PhlegethonticFormationPlan.plan(8675309L,0,0,terrain);

        assertEquals(first,second,"The same fresh chunk must reconstruct identical formations");
        assertTrue(first.stream().anyMatch(segment -> segment.direction()==Direction.UP),
                "The dry floor needs stalagmites");
        assertTrue(first.stream().anyMatch(segment -> segment.direction()==Direction.DOWN),
                "The roof needs stalactites");
        assertTrue(first.stream().allMatch(segment -> terrain.get(segment.pos())==AIR),
                "Every segment must stay inside the carved cavern");
        assertTrue(first.stream().allMatch(segment -> segment.pos().y()>=40 && segment.pos().y()<=61));
    }

    @Test void floodedColumnsDoNotGrowStalagmitesThroughIchor() {
        Map<PhlegethonticTerrainPlan.Voxel,PhlegethonticTerrainPlan.Material> terrain=new HashMap<>();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,39,z),SCAB);
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,40,z),PhlegethonticTerrainPlan.Material.ICHOR);
            for(int y=41;y<=61;y++)terrain.put(new PhlegethonticTerrainPlan.Voxel(x,y,z),AIR);
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,62,z),SCAB);
        }

        var formations=PhlegethonticFormationPlan.plan(20260912L,0,0,terrain);

        assertFalse(formations.stream().anyMatch(segment -> segment.direction()==Direction.UP),
                "Floor spikes must not replace or grow through ichor");
        assertTrue(formations.stream().anyMatch(segment -> segment.direction()==Direction.DOWN));
    }

    @Test void openSkyAboveTheCarvedColumnDoesNotProduceFloatingStalactites() {
        Map<PhlegethonticTerrainPlan.Voxel,PhlegethonticTerrainPlan.Material> terrain=new HashMap<>();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,39,z),SCAB);
            for(int y=40;y<=61;y++)terrain.put(new PhlegethonticTerrainPlan.Voxel(x,y,z),AIR);
        }

        var formations=PhlegethonticFormationPlan.plan(8675309L,0,0,terrain);

        assertFalse(formations.stream().anyMatch(segment -> segment.direction()==Direction.DOWN),
                "A roof formation must begin against a solid ceiling");
    }

    @Test void disconnectedAirPocketsDoNotProducePartialFloatingChains() {
        Map<PhlegethonticTerrainPlan.Voxel,PhlegethonticTerrainPlan.Material> terrain=new HashMap<>();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,39,z),SCAB);
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,40,z),AIR);
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,61,z),AIR);
            terrain.put(new PhlegethonticTerrainPlan.Voxel(x,62,z),SCAB);
        }

        var formations=PhlegethonticFormationPlan.plan(8675309L,0,0,terrain);

        assertTrue(formations.stream().allMatch(segment -> terrain.get(segment.pos())==AIR),
                "Every emitted chain must be contiguous air from its supporting surface to its tip");
    }
}
