package com.vincenthuto.hemomancy.common.worldgen;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.*;

class PhlegethonticContainmentTest {
    @Test void basinSurfaceDithersFromScabThroughBlackstoneIntoVenousStone() {
        long seed=17;
        var layout=new PhlegethonticBasinLayout(seed,0,0,List.of(
                new PhlegethonticBasinLayout.Basin(0,0,32,32,48)),List.of());
        var layouts=List.of(layout);
        Map<Voxel,Material> plan=generateBasin(layouts,seed,-4,3,-4,3);
        EnumMap<Material,Integer> innerBlend=new EnumMap<>(Material.class);
        EnumMap<Material,Integer> middle=new EnumMap<>(Material.class);
        EnumMap<Material,Integer> outerBlend=new EnumMap<>(Material.class);
        int near=0;
        for(int x=-48;x<=48;x++)for(int z=-48;z<=48;z++) {
            double distance=PhlegethonticTerrainPlan.column(layouts,x,z).distance();
            Material top=topSolid(plan,x,z);
            if(distance>=0 && distance<2) {
                near++;
                assertEquals(Material.SCAB,top,"The unbroken edge beside ichor must remain scab");
            } else if(distance>=2 && distance<7 && top!=null) {
                innerBlend.merge(top,1,Integer::sum);
            } else if(distance>=7 && distance<10 && top!=null) {
                middle.merge(top,1,Integer::sum);
            } else if(distance>=10 && distance<16 && top!=null) {
                outerBlend.merge(top,1,Integer::sum);
            }
        }
        assertTrue(near>30);
        assertTrue(innerBlend.getOrDefault(Material.SCAB,0)>0 && innerBlend.getOrDefault(Material.BLACKSTONE,0)>0,
                "The inner band must dither scab into blackstone: "+innerBlend);
        assertEquals(middle.values().stream().mapToInt(Integer::intValue).sum(),middle.getOrDefault(Material.BLACKSTONE,0),
                "Blackstone must dominate the middle band: "+middle);
        assertTrue(outerBlend.getOrDefault(Material.BLACKSTONE,0)>0 && outerBlend.getOrDefault(Material.VENOUS,0)>0,
                "The outer band must dither blackstone into venous stone: "+outerBlend);
        assertEquals(0,outerBlend.getOrDefault(Material.SCAB,0),"Scab must not return at the outer edge");
    }

    @Test void exposedBanksAndSubmergedFloorHaveBoundedSurfaceRelief() {
        long seed=17;
        var layouts=List.of(new PhlegethonticBasinLayout(seed,0,0,List.of(
                new PhlegethonticBasinLayout.Basin(0,0,32,32,48)),List.of()));
        Map<Voxel,Material> plan=generateBasin(layouts,seed,-4,3,-4,3);
        Set<Integer> bankHeights=new HashSet<>(),floorHeights=new HashSet<>();
        Map<Long,Integer> bankByColumn=new HashMap<>();
        for(int x=-48;x<=48;x++)for(int z=-48;z<=48;z++) {
            var column=PhlegethonticTerrainPlan.column(layouts,x,z);
            Integer top=topSolidY(plan,x,z);
            if(top==null)continue;
            if(column.distance()>=2 && column.distance()<15) {
                assertTrue(top>=49 && top<=51,"Bank relief must remain within zero to two blocks: "+top);
                bankHeights.add(top);bankByColumn.put((((long)x)<<32)^(z&0xffffffffL),top);
            } else if(column.distance()<-20) {
                assertEquals(Material.ICHOR,plan.get(new Voxel(x,48,z)),"Relief must not disturb the level ichor surface");
                assertTrue(top>=38 && top<=40,"Submerged floor relief must stay within one block: "+top);
                floorHeights.add(top);
            }
        }
        assertTrue(bankHeights.size()>=3,"The exposed bank must visibly use all three subtle height levels: "+bankHeights);
        assertTrue(floorHeights.size()>=2,"The deep basin floor must not remain perfectly flat: "+floorHeights);
        for(var entry:bankByColumn.entrySet()) {
            int x=(int)(entry.getKey()>>32),z=(int)(long)entry.getKey();
            for(int[] direction:List.of(new int[]{1,0},new int[]{0,1})) {
                Integer neighbor=bankByColumn.get((((long)(x+direction[0]))<<32)^((z+direction[1])&0xffffffffL));
                if(neighbor!=null)assertTrue(Math.abs(entry.getValue()-neighbor)<=1,"Bank relief cannot form a two-block cliff");
            }
        }
    }

    @Test void everySolidFaceTouchingBasinIchorIsScab() {
        long seed=17;
        var layouts=List.of(new PhlegethonticBasinLayout(seed,0,0,List.of(
                new PhlegethonticBasinLayout.Basin(0,0,32,32,48)),List.of()));
        Map<Voxel,Material> plan=generateBasin(layouts,seed,-4,3,-4,3);
        int contacts=0;
        for(var entry:plan.entrySet())if(entry.getValue()==Material.ICHOR)for(Voxel neighbor:entry.getKey().neighbors()) {
            Material material=plan.get(neighbor);
            if(material==null || material==Material.AIR || material==Material.ICHOR)continue;
            contacts++;
            assertEquals(Material.SCAB,material,"Every solid face touching ichor must be blood-scorched scab at "+neighbor);
        }
        assertTrue(contacts>100,"The fixture must exercise the submerged floor and jagged retaining edge");
    }

    @Test void aClosedLowerCourseRetainsAnAdjacentHigherCourseAtTheBiomeEdge() {
        var channels=new ArrayList<PhlegethonticBasinLayout.Channel>();
        for(int z:List.of(8,16)) {
            int surface=z==8?56:46;
            channels.add(new PhlegethonticBasinLayout.Channel(0,1,5,List.of(
                    new PhlegethonticBasinLayout.Point(0,z,surface),
                    new PhlegethonticBasinLayout.Point(32,z,surface)),true));
        }
        var layout=new PhlegethonticBasinLayout(0,0,0,List.of(),channels);
        var plan=PhlegethonticTerrainPlan.basin(List.of(layout),0,0,0,0,128,p -> p.z()<15,p -> true);
        for(int y=47;y<=56;y++)assertEquals(Material.SCAB,plan.get(new Voxel(8,y,13)),
                "A closed low course must retain fluid from its higher neighbor before the protected border");
    }
    @Test void aLongCourseHasArchesRoughlyElevenBlocksApart() {
        var layout=new PhlegethonticBasinLayout(0,0,0,List.of(),List.of(
                new PhlegethonticBasinLayout.Channel(0,1,5,List.of(
                        new PhlegethonticBasinLayout.Point(0,8,60),
                        new PhlegethonticBasinLayout.Point(127,8,60)),true)));
        Map<Voxel,Material> course=new HashMap<>();
        for(int cx=0;cx<8;cx++)course.putAll(PhlegethonticTerrainPlan.basin(List.of(layout),17,cx,0,0,128,p -> true,p -> true));
        int arches=0;boolean previousAir=false;
        for(int x=0;x<128;x++) {
            boolean air=course.get(new Voxel(x,49,8))==Material.AIR;
            if(air && !previousAir)arches++;
            previousAir=air;
            assertEquals(Material.SCAB,course.get(new Voxel(x,57,8)),"Arch subtraction must preserve the lined trough");
        }
        assertTrue(arches>=10 && arches<=14,"A 128-block course needs approximately twelve arches; got "+arches);
    }
    @Test void protectedBlocksCloseTheVeinAndChunkOrderDoesNotChangeThePlan() {
        Set<Voxel> protectedCells = Set.of(new Voxel(15,12,8), new Voxel(9,12,8));
        List<PhlegethonticVeinPath.Node> nodes = new ArrayList<>();
        for (int x=0;x<32;x++) nodes.add(new PhlegethonticVeinPath.Node(x,12,8,2));
        var west = PhlegethonticTerrainPlan.vein(nodes,0,0,p -> !protectedCells.contains(p));
        var east = PhlegethonticTerrainPlan.vein(nodes,1,0,p -> !protectedCells.contains(p));
        Map<Voxel, Material> all = new HashMap<>(west); all.putAll(east);
        for (Voxel p : protectedCells) assertFalse(all.containsKey(p));
        for (var entry : all.entrySet()) if (entry.getValue()==Material.ICHOR) {
            for (Voxel neighbor : entry.getKey().neighbors()) {
                if (neighbor.x()<0 || neighbor.x()>31) continue;
                assertTrue(all.get(neighbor)==Material.ICHOR || all.get(neighbor)==Material.SCAB,
                        "unsealed core at " + entry.getKey() + " beside " + neighbor);
            }
        }
        assertTrue(west.keySet().stream().allMatch(p -> p.x()>=0 && p.x()<16 && p.z()>=0 && p.z()<16));
        assertTrue(east.keySet().stream().allMatch(p -> p.x()>=16 && p.x()<32 && p.z()>=0 && p.z()<16));
        assertTrue(all.containsValue(Material.ICHOR));
    }

    @Test void aCaveCannotReceiveAnUncontainedCore() {
        var nodes = List.of(new PhlegethonticVeinPath.Node(8,12,8,2));
        var plan = PhlegethonticTerrainPlan.vein(nodes,0,0,p -> p.x()<8);
        assertFalse(plan.containsValue(Material.ICHOR));
        assertTrue(plan.isEmpty(), "rejection must not leave a scab-only fragment");
    }

    @Test void adjacentChunksRemainIdenticalWhenPriorChunksHaveAlreadyMaterialized() {
        for(long seed:List.of(17L,42L,917L)) {
            List<int[]> chunks=List.of(new int[]{-2,-1},new int[]{-1,-1},new int[]{-2,0},new int[]{-1,0});
            Map<Voxel,Material> forward=generateVeins(seed,chunks);
            var reverse=new ArrayList<>(chunks);Collections.reverse(reverse);
            assertEquals(forward,generateVeins(seed,reverse));
        }
    }
    private Map<Voxel,Material> generateVeins(long seed,List<int[]> chunks) {
        Map<Voxel,Material> world=new HashMap<>();
        for(int[] chunk:chunks) {
            var plan=PhlegethonticTerrainPlan.vein(PhlegethonticVeinPath.nearChunk(seed,chunk[0],chunk[1],0),chunk[0],chunk[1],
                    p -> world.containsKey(p)?world.get(p)==Material.SCAB || world.get(p)==Material.ICHOR
                            :p.y()>=5 && p.y()<=20 && !(p.x()==-17 && p.z()==-1) && !(p.x()>-20 && p.y()>17));
            assertTrue(plan.keySet().stream().allMatch(p -> PhlegethonticTerrainPlan.owns(p,chunk[0],chunk[1])));
            world.putAll(plan);
        }
        return world;
    }

    private Map<Voxel,Material> generateBasin(List<PhlegethonticBasinLayout> layouts,long seed,
                                               int minChunkX,int maxChunkX,int minChunkZ,int maxChunkZ) {
        Map<Voxel,Material> result=new HashMap<>();
        for(int chunkX=minChunkX;chunkX<=maxChunkX;chunkX++)for(int chunkZ=minChunkZ;chunkZ<=maxChunkZ;chunkZ++)
            result.putAll(PhlegethonticTerrainPlan.basin(layouts,seed,chunkX,chunkZ,0,128,p -> true,p -> true));
        return result;
    }

    private Material topSolid(Map<Voxel,Material> plan,int x,int z) {
        return plan.entrySet().stream().filter(e -> e.getKey().x()==x && e.getKey().z()==z
                        && e.getValue()!=Material.AIR && e.getValue()!=Material.ICHOR)
                .max(Comparator.comparingInt(e -> e.getKey().y())).map(Map.Entry::getValue).orElse(null);
    }

    private Integer topSolidY(Map<Voxel,Material> plan,int x,int z) {
        return plan.entrySet().stream().filter(e -> e.getKey().x()==x && e.getKey().z()==z
                        && e.getValue()!=Material.AIR && e.getValue()!=Material.ICHOR)
                .mapToInt(e -> e.getKey().y()).max().stream().boxed().findFirst().orElse(null);
    }

    @Test void overlappingBasinLayoutsCloseAtBiomeAndStructureObstructions() {
        long seed=42;
        Map<Voxel,Material> forward=new HashMap<>(),reverse=new HashMap<>();
        java.util.function.Predicate<Voxel> allowed=p -> p.x()<51 && !(p.x()>=33 && p.x()<39 && p.z()>=55 && p.z()<61);
        for(int x:List.of(2,3)) {
            var plan=PhlegethonticTerrainPlan.basin(PhlegethonticBasinLayout.nearChunk(seed,x,3,32),seed,x,3,0,128,allowed,allowed);
            assertTrue(plan.keySet().stream().allMatch(p -> allowed.test(p) && PhlegethonticTerrainPlan.owns(p,x,3)));
            forward.putAll(plan);
        }
        for(int x:List.of(3,2))reverse.putAll(PhlegethonticTerrainPlan.basin(PhlegethonticBasinLayout.nearChunk(seed,x,3,32),seed,x,3,0,128,allowed,allowed));
        assertEquals(forward,reverse);
        for(var e:forward.entrySet())if(e.getValue()==Material.ICHOR)for(Voxel p:e.getKey().neighbors())
            assertTrue(allowed.test(p),"Fluid cannot touch a protected obstruction or biome border");
    }

    @Test void lowerShoreRetainsTheAdjacentHigherReservoir() {
        var layouts=List.of(new PhlegethonticBasinLayout(0,0,0,List.of(
                new PhlegethonticBasinLayout.Basin(0,0,18,18,46),
                new PhlegethonticBasinLayout.Basin(28,0,18,18,56)),List.of()));
        boolean exercised=false;
        for(int x=8;x<23;x++)for(int z=-24;z<=24;z++) {
            var low=PhlegethonticTerrainPlan.column(layouts,x,z);
            var high=PhlegethonticTerrainPlan.column(layouts,x+2,z);
            if(low.distance()<=0 || low.distance()>=2 || low.surface()!=46 || high.distance()>0 || high.surface()!=56)continue;
            var plan=PhlegethonticTerrainPlan.basin(layouts,0,Math.floorDiv(x,16),Math.floorDiv(z,16),0,128,p -> true,p -> true);
            for(int y=47;y<=56;y++)assertEquals(Material.SCAB,plan.get(new Voxel(x,y,z)),
                    "The low shore must retain the adjacent higher reservoir");
            exercised=true;
        }
        assertTrue(exercised,"Fixture must include a low shore next to a higher fluid column");
    }
}
