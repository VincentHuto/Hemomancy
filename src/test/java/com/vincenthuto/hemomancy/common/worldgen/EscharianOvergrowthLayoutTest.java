package com.vincenthuto.hemomancy.common.worldgen;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.*;
import com.vincenthuto.hemomancy.common.worldgen.config.EscharianOvergrowthConfiguration;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;

class EscharianOvergrowthLayoutTest {
    private static final EscharianOvergrowthConfiguration CONFIG = EscharianOvergrowthConfiguration.DEFAULT;
    private static void connected(Plan plan) {
        Set<Cell> seen = new HashSet<>(); ArrayDeque<Cell> todo = new ArrayDeque<>();
        todo.add(plan.backing().keySet().iterator().next());
        while(!todo.isEmpty()) { Cell c=todo.remove(); if(!seen.add(c))continue;
            for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++) {
                int distance=Math.abs(dx)+Math.abs(dy)+Math.abs(dz);
                Cell next=c.offset(dx,dy,dz);
                if(distance>0 && distance<=(plan.ground()?1:2) && plan.backing().containsKey(next) && !seen.contains(next))todo.add(next);
            } }
        assertEquals(plan.backing().keySet(),seen);
    }
    private static void plants(Plan plan, Predicate<Cell> air) {
        assertTrue(plan.plants().size()>=3);
        if(plan.ground()) {
            Set<Cell> available=new HashSet<>();
            plan.backing().forEach((c,layer) -> {
                if(layer==Layer.CENTER)for(Direction d:Direction.values()) {
                    Cell target=c.relative(d);
                    if(!plan.backing().containsKey(target) && air.test(target))available.add(target);
                }
            });
            assertEquals(available,plan.plants().keySet(),"Cover every available dark top/side position");
            assertTrue(plan.plants().size()>=5,"Even the smallest pile must carry several cups");
        } else {
            long exposed=plan.backing().keySet().stream().filter(c -> Arrays.stream(Direction.values()).anyMatch(d ->
                    !plan.backing().containsKey(c.relative(d)) && air.test(c.relative(d)))).count();
            assertTrue(plan.plants().size()>=Math.ceil(exposed*.5));
            assertTrue(plan.plants().size()<=Math.floor(exposed*.7));
        }
        Set<Cell> supports=new HashSet<>();
        plan.plants().forEach((c,plant) -> {
            assertTrue(air.test(c)); assertFalse(plan.backing().containsKey(c));
            assertTrue(plant.count()>=1 && plant.count()<=5);
            Cell support=c.relative(plant.facing().getOpposite()); assertEquals(Layer.CENTER,plan.backing().get(support));
            if(!plan.ground())assertTrue(supports.add(support), "Only one plant per decorated patch cell");
        });
    }
    @Test void flatPatchesHaveTenToThirtyConnectedCellsAndRealFaceAttachments() {
        Set<Integer> counts=new HashSet<>();
        for(Direction normal:Direction.values()) {
            Predicate<Cell> natural=c -> c.x()*normal.getStepX()+c.y()*normal.getStepY()+c.z()*normal.getStepZ()<=0;
            for(int seed=0;seed<128;seed++) {
                var plan=EscharianOvergrowthLayout.patch(seed,new Cell(0,0,0),CONFIG,natural,natural.negate());
                assertFalse(plan.empty()); assertTrue(plan.backing().size()>=10 && plan.backing().size()<=30);
                assertTrue(plan.backing().values().stream().allMatch(l -> l==Layer.CENTER));
                assertTrue(plan.plants().values().stream().allMatch(plant -> plant.facing()==normal));
                plan.plants().values().forEach(plant -> counts.add(plant.count()));
                assertEquals(plan,EscharianOvergrowthLayout.patch(seed,new Cell(0,0,0),CONFIG,natural,natural.negate()));
                connected(plan); plants(plan,natural.negate());
            }
        }
        assertEquals(Set.of(1,2,3,4,5),counts,"Worldgen must use all five authored Scyphus states");
    }
    @Test void patchesWrapSteppedWallsAndCeilingCorners() {
        Predicate<Cell> natural=c -> c.z()<=Math.floorDiv(c.x(),2) || c.y()>=4;
        Set<Direction> normals=new HashSet<>(); Set<Integer> depths=new HashSet<>();
        for(int seed=0;seed<64;seed++) {
            var plan=EscharianOvergrowthLayout.patch(seed,new Cell(0,2,0),CONFIG,natural,natural.negate());
            assertFalse(plan.empty()); connected(plan); plants(plan,natural.negate());
            plan.backing().keySet().forEach(c -> { assertTrue(natural.test(c)); depths.add(c.z()); });
            plan.plants().values().forEach(plant -> normals.add(plant.facing()));
        }
        assertTrue(depths.size()>3); assertTrue(normals.contains(Direction.DOWN));
        assertTrue(normals.contains(Direction.SOUTH)); assertTrue(normals.contains(Direction.WEST));
    }
    @Test void pilesAreSolidSmallTaperedAndOnlyHaveGroundRims() {
        Predicate<Cell> natural=c -> c.y()<0;
        Set<String> dimensions=new HashSet<>();
        for(int seed=0;seed<256;seed++) {
            var plan=EscharianOvergrowthLayout.pile(seed,new Cell(0,0,0),natural,natural.negate());
            assertFalse(plan.empty()); connected(plan); plants(plan,natural.negate());
            int minX=99,maxX=-99,minZ=99,maxZ=-99,maxY=0;
            for(var entry:plan.backing().entrySet()) {
                Cell c=entry.getKey(); minX=Math.min(minX,c.x()); maxX=Math.max(maxX,c.x());
                minZ=Math.min(minZ,c.z()); maxZ=Math.max(maxZ,c.z()); maxY=Math.max(maxY,c.y());
                for(int y=0;y<c.y();y++)assertTrue(plan.backing().containsKey(new Cell(c.x(),y,c.z())));
                if(entry.getValue()==Layer.RIM) { assertEquals(0,c.y());
                    assertTrue(Direction.Plane.HORIZONTAL.stream().anyMatch(d -> !plan.backing().containsKey(c.relative(d)))); }
                if(c.y()>0)assertEquals(Layer.CENTER,entry.getValue());
            }
            assertTrue(maxX-minX+1>=3 && maxX-minX+1<=4); assertTrue(maxZ-minZ+1>=3 && maxZ-minZ+1<=4);
            assertTrue(maxY>=1 && maxY<=2); dimensions.add((maxX-minX+1)+":"+(maxZ-minZ+1)+":"+maxY);
            assertEquals(plan,EscharianOvergrowthLayout.pile(seed,new Cell(0,0,0),natural,natural.negate()));
        }
        assertEquals(8,dimensions.size());
    }
    @Test void inadequateSurfacesAndUnsupportedOrObstructedPilesAreRejected() {
        assertTrue(EscharianOvergrowthLayout.patch(0,new Cell(0,0,0),CONFIG,c -> c.equals(new Cell(0,0,0)),c -> c.y()>0).empty());
        assertTrue(EscharianOvergrowthLayout.pile(0,new Cell(0,0,0),c -> false,c -> true).empty());
        assertTrue(EscharianOvergrowthLayout.pile(0,new Cell(0,0,0),c -> c.y()<0,c -> c.y()>0).empty());
    }
}
