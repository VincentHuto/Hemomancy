package com.vincenthuto.hemomancy.gametest;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedSagittaryEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedSagittarySpawnRules;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.worldgen.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.phys.AABB;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class PhlegethonticWorldValidation {
    private record Sample(ChunkPos chunk,long start) {}
    private static final ThreadLocal<Sample> ACTIVE=new ThreadLocal<>();
    private static final java.util.concurrent.ConcurrentMap<Long,java.util.concurrent.atomic.DoubleAdder> COSTS=new java.util.concurrent.ConcurrentHashMap<>();
    private static final Queue<String> BAD_WRITES=new ConcurrentLinkedQueue<>();
    public static void begin(ChunkPos chunk){ACTIVE.set(new Sample(chunk,System.nanoTime()));}
    public static void end(boolean changed){var sample=ACTIVE.get();if(sample!=null)COSTS.computeIfAbsent(sample.chunk.toLong(),key -> new java.util.concurrent.atomic.DoubleAdder()).add((System.nanoTime()-sample.start)/1e6);ACTIVE.remove();}
    public static void checkWrite(BlockPos pos) {
        var sample=ACTIVE.get();if(sample!=null && !sample.chunk.equals(new ChunkPos(pos)))BAD_WRITES.add(sample.chunk+" -> "+pos);
    }
    @GameTestGenerator
    public static Collection<TestFunction> tests() {
        if(System.getProperty("hemomancy.phlegethontic.validationSeed")==null)return List.of();
        terrablender.api.Regions.register(new terrablender.api.Region(
                net.minecraft.resources.ResourceLocation.parse("phlegethontic_validation:companion"),terrablender.api.RegionType.NETHER,1) {
            @Override public void addBiomes(net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> registry,
                    java.util.function.Consumer<com.mojang.datafixers.util.Pair<net.minecraft.world.level.biome.Climate.ParameterPoint,
                            net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>>> mapper) {
                var foreign=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
                        net.minecraft.resources.ResourceLocation.parse("phlegethontic_validation:foreign_nether"));
                addBiome(mapper,net.minecraft.world.level.biome.Climate.parameters(0,0,0,0,0,0,0),foreign);
                addBiome(mapper,net.minecraft.world.level.biome.Climate.parameters(0,-.5F,0,0,0,0,0),DEFERRED_PLACEHOLDER);
                addBiome(mapper,net.minecraft.world.level.biome.Climate.parameters(.4F,0,0,0,0,0,0),DEFERRED_PLACEHOLDER);
                addBiome(mapper,net.minecraft.world.level.biome.Climate.parameters(0,.5F,0,0,0,0,.375F),DEFERRED_PLACEHOLDER);
                addBiome(mapper,net.minecraft.world.level.biome.Climate.parameters(-.5F,0,0,0,0,0,.175F),DEFERRED_PLACEHOLDER);
            }
        });
        return List.of(new TestFunction("phlegethontic_worldgen","fresh_nether_seed",
                "phlegethontic_validation:room",1200,0,true,PhlegethonticWorldValidation::inspect),
                new TestFunction("phlegethontic_worldgen","scyphus_attachment",
                        "phlegethontic_validation:room",100,0,true,h -> {
                    EscharianScyphusAcceptance.run(h.makeMockPlayer(GameType.SURVIVAL),h.absolutePos(new BlockPos(3,5,3)));
                    h.succeed();
                }));
    }
    private static void inspect(GameTestHelper h) {
        try {
            ServerLevel level=h.getLevel().getServer().getLevel(Level.NETHER);
            long seed=Long.getLong("hemomancy.phlegethontic.validationSeed",42L);
            int seaLevel=level.getChunkSource().getGenerator().getSeaLevel();
            h.assertTrue(level!=null && level.getSeed()==seed,"Validation must use the requested fresh Nether seed");
            var found=level.findClosestBiome3d(b -> b.is(BiomeInit.PHLEGETHONTIC_BASIN),new BlockPos(0,55,0),8192,32,64);
            h.assertTrue(found!=null,"Basin not found within 8192 blocks");
            ChunkPos center=new ChunkPos(found.getFirst());
            long start=System.nanoTime();int cores=0,exposed=0,basinColumns=0,basinIchor=0,pointedSegments=0,unsupportedPointedSegments=0,origins=0,successfulOrigins=0;
            int overgrowthBlocks=0,overgrowthCenters=0,overgrowthRims=0,overgrowthOutsideBasin=0;
            List<BlockPos> overgrowthPositions=new ArrayList<>();
            List<Double> chunks=new ArrayList<>();
            // The wider generated area also exercises structure references at the edge of the dependency region.
            Set<Long> initialFluid=new HashSet<>();
            Set<Long> materializedLayouts=new HashSet<>(),raisedRiverLayouts=new HashSet<>();
            int raisedRiverColumns=0,archColumns=0;
            List<ChunkPos> generationOrder=new ArrayList<>();
            for(int x=center.x-12;x<center.x+12;x++)for(int z=center.z-12;z<center.z+12;z++)generationOrder.add(new ChunkPos(x,z));
            if(System.getProperty("hemomancy.phlegethontic.validationOrder","forward").equals("reverse"))Collections.reverse(generationOrder);
            for(var chunk:generationOrder) {
                long before=System.nanoTime();level.getChunk(chunk.x,chunk.z);chunks.add((System.nanoTime()-before)/1e6);
                if(Math.abs(chunk.x-center.x)<=6 && Math.abs(chunk.z-center.z)<=6)level.setChunkForced(chunk.x,chunk.z,true);
            }
            java.security.MessageDigest terrainHash=java.security.MessageDigest.getInstance("SHA-256");
            for(int x=center.x-5;x<center.x+5;x++)for(int z=center.z-5;z<center.z+5;z++) {
                var originPath=PhlegethonticVeinPath.fromChunk(seed,x,z,level.getMinBuildHeight());
                if(!originPath.isEmpty()) {
                    origins++;
                    if(originPath.stream().anyMatch(n -> level.getFluidState(new BlockPos(n.x(),n.y(),n.z())).is(PhlegethonticTags.ICHOR)))successfulOrigins++;
                }
                for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++) {
                    BlockPos column=new BlockPos(x*16+dx,55,z*16+dz);
                    if(level.getBiome(column).is(BiomeInit.PHLEGETHONTIC_BASIN))basinColumns++;
                    for(int y=5;y<=100;y++) {
                        BlockPos p=new BlockPos(column.getX(),y,column.getZ());
                        var state=level.getBlockState(p);
                        if(state.is(BlockInit.escharian_overgrowth.get()) || state.is(BlockInit.escharian_overgrowth_rim.get())) {
                            overgrowthBlocks++;overgrowthPositions.add(p.immutable());
                            if(state.is(BlockInit.escharian_overgrowth_rim.get()))overgrowthRims++;else overgrowthCenters++;
                            terrainHash.update((p.toShortString()+state.toString()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            if(!level.getBiome(p).is(BiomeInit.PHLEGETHONTIC_BASIN))overgrowthOutsideBasin++;
                        }
                        if(state.is(BlockInit.pointed_blood_scorched_scab.get())) {
                            pointedSegments++;
                            Direction direction=state.getValue(PointedDripstoneBlock.TIP_DIRECTION);
                            BlockPos supportPos=p.relative(direction.getOpposite());
                            var support=level.getBlockState(supportPos);
                            boolean sameChain=support.is(BlockInit.pointed_blood_scorched_scab.get())
                                    && support.getValue(PointedDripstoneBlock.TIP_DIRECTION)==direction;
                            if(!sameChain && !support.isFaceSturdy(level,supportPos,direction))unsupportedPointedSegments++;
                        }
                        if(state.is(BlockInit.blood_scorched_scab.get()) || state.is(BlockInit.pointed_blood_scorched_scab.get())
                                || state.getFluidState().is(PhlegethonticTags.ICHOR))
                            terrainHash.update((p.toShortString()+state.toString()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        if(!level.getFluidState(p).is(PhlegethonticTags.ICHOR))continue;
                        initialFluid.add(p.asLong());
                        if(y>20){
                            h.assertTrue(y<=seaLevel+36,"Basin fluid exceeds the Nether generator's sea level +36 at "+p);
                            basinIchor++;continue;
                        }
                        cores++;
                        for(Direction d:Direction.values())if(level.getBlockState(p.relative(d)).isAir()){exposed++;break;}
                    }
                }
            }
            // Measure surviving courses over the full generated area, separately from the 100-chunk vein sample.
            for(var chunk:generationOrder) {
                var layouts=PhlegethonticBasinLayout.nearChunk(seed,chunk.x,chunk.z,seaLevel);
                for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++) {
                    BlockPos column=new BlockPos(chunk.getMinBlockX()+dx,55,chunk.getMinBlockZ()+dz);
                    PhlegethonticBasinLayout.Column course=null;long layoutId=0;
                    for(var layout:layouts) {
                        var sample=layout.sample(column.getX()+.5,column.getZ()+.5);
                        if(PhlegethonticTerrainPlan.prefers(sample,course)){course=sample;layoutId=layout.id();}
                    }
                    if(course.distance()<=0 && level.getFluidState(new BlockPos(column.getX(),course.surface(),column.getZ())).is(PhlegethonticTags.ICHOR)) {
                        materializedLayouts.add(layoutId);
                        if(course.raised()) {
                            raisedRiverLayouts.add(layoutId);raisedRiverColumns++;
                            for(int y=Math.max(24,course.floor()-28)+3;y<course.floor()-5;y++)
                                if(level.getBlockState(new BlockPos(column.getX(),y,column.getZ())).isAir()){archColumns++;break;}
                        }
                    }
                }
            }
            var guardians=level.getEntitiesOfClass(ExcoriatedSagittaryEntity.class,
                    new AABB((center.x-5)*16,0,(center.z-5)*16,(center.x+5)*16,128,(center.z+5)*16),ExcoriatedSagittaryEntity::isSentinel);
            long uniqueHomes=guardians.stream().map(ExcoriatedSagittaryEntity::home).distinct().count();
            var generatedAreaGuardians=level.getEntitiesOfClass(ExcoriatedSagittaryEntity.class,
                    new AABB((center.x-12)*16,0,(center.z-12)*16,(center.x+12)*16,128,(center.z+12)*16),ExcoriatedSagittaryEntity::isSentinel);
            long generatedAreaUniqueHomes=generatedAreaGuardians.stream().map(ExcoriatedSagittaryEntity::home).distinct().count();
            Set<Long> inspectedSentinelLayouts=new HashSet<>();int validSentinelCandidates=0;
            for(var chunk:generationOrder)for(var layout:PhlegethonticBasinLayout.nearChunk(seed,chunk.x,chunk.z,seaLevel)) {
                if(!inspectedSentinelLayouts.add(layout.id()))continue;
                for(var candidate:layout.sentinelCandidates()) {
                    if(candidate.x()<(center.x-5)*16 || candidate.x()>=(center.x+5)*16
                            || candidate.z()<(center.z-5)*16 || candidate.z()>=(center.z+5)*16)continue;
                    if(ExcoriatedSagittarySpawnRules.validShore(level,new BlockPos(candidate.x(),candidate.y(),candidate.z())))
                        validSentinelCandidates++;
                }
            }
            int overgrowthComponentsWithoutIchor=componentsWithoutIchor(level,overgrowthPositions,10);
            List<BlockPos> generatedGrowth=growthPositions(level,generationOrder);
            var colonies=inspectColonies(level,generatedGrowth);
            Map<Direction,List<BlockPos>> plants=scyphusPlants(level,blockPositions(level,generationOrder,state -> state.is(BlockInit.escharian_scyphus.get())));
            Map<Integer,Long> scyphusCounts=new TreeMap<>();
            for(int count=1;count<=5;count++) {
                int expected=count;
                scyphusCounts.put(count,plants.values().stream().flatMap(List::stream)
                        .filter(pos -> level.getBlockState(pos).getValue(com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock.COUNT)==expected).count());
            }
            long unsupportedPlants=plants.values().stream().flatMap(List::stream).filter(pos -> !level.getBlockState(pos).canSurvive(level,pos)).count();
            long wrongPlantBacking=plants.values().stream().flatMap(List::stream).filter(pos -> {
                var facing=level.getBlockState(pos).getValue(com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock.FACING);
                var backing=level.getBlockState(pos.relative(facing.getOpposite()));
                return !backing.is(BlockInit.escharian_overgrowth.get());
            }).count();
            long elevatedRims=generatedGrowth.stream().filter(pos -> level.getBlockState(pos).is(BlockInit.escharian_overgrowth_rim.get())
                    && !level.getBlockState(pos.below()).is(PhlegethonticTags.OVERGROWTH_SUPPORT)).count();
            List<Double> costs=generationOrder.stream().map(c -> COSTS.getOrDefault(c.toLong(),new java.util.concurrent.atomic.DoubleAdder()).sum()).sorted().toList();
            Collections.sort(chunks);
            Set<Long> activeLayouts=new HashSet<>();int basinCount=0,channelCount=0,raisedLayouts=0;
            for(var chunk:generationOrder) {
                var layout=PhlegethonticBasinLayout.create(seed,Math.floorDiv(chunk.x,8),Math.floorDiv(chunk.z,8),seaLevel);
                if(activeLayouts.contains(layout.id()) || !level.getBiome(new BlockPos(chunk.getMiddleBlockX(),55,chunk.getMiddleBlockZ())).is(BiomeInit.PHLEGETHONTIC_BASIN))continue;
                activeLayouts.add(layout.id());basinCount+=layout.basins().size();channelCount+=layout.channels().size();
                if(layout.channels().stream().anyMatch(PhlegethonticBasinLayout.Channel::raised))raisedLayouts++;
            }
            JsonObject report=new JsonObject();report.addProperty("seed",seed);report.addProperty("basin",found.getFirst().toShortString());
            report.addProperty("discoveryDistance",Math.hypot(found.getFirst().getX(),found.getFirst().getZ()));
            report.addProperty("sampledChunks",100);report.addProperty("activeVeinOrigins",origins);
            report.addProperty("successfulVeinOrigins",successfulOrigins);
            report.addProperty("generatedChunks",generationOrder.size());report.addProperty("terrainHash",HexFormat.of().formatHex(terrainHash.digest()));
            report.addProperty("generationOrder",System.getProperty("hemomancy.phlegethontic.validationOrder","forward"));
            report.addProperty("buriedIchorBlocks",cores);report.addProperty("exposedBuriedCores",exposed);
            report.addProperty("basinColumns",basinColumns);report.addProperty("basinIchorBlocks",basinIchor);
            report.addProperty("pointedScabSegments",pointedSegments);
            report.addProperty("unsupportedPointedScabSegments",unsupportedPointedSegments);
            report.addProperty("escharianOvergrowthBlocks",overgrowthBlocks);
            report.addProperty("escharianOvergrowthCenters",overgrowthCenters);
            report.addProperty("escharianOvergrowthRims",overgrowthRims);
            if(!overgrowthPositions.isEmpty())report.addProperty("escharianExample",overgrowthPositions.getFirst().toShortString());
            report.addProperty("escharianOutsideBasin",overgrowthOutsideBasin);
            report.addProperty("escharianComponentsWithoutNearbyIchor",overgrowthComponentsWithoutIchor);
            JsonObject orientations=new JsonObject(),examples=new JsonObject();
            for(var entry:plants.entrySet()) {
                orientations.addProperty(entry.getKey().getName(),entry.getValue().size());
                if(!entry.getValue().isEmpty())examples.addProperty(entry.getKey().getName(),entry.getValue().getFirst().toShortString());
            }
            report.add("escharianScyphusByFacing",orientations);
            JsonObject counts=new JsonObject();scyphusCounts.forEach((count,total) -> counts.addProperty(Integer.toString(count),total));
            report.add("escharianScyphusByCount",counts);
            report.add("escharianScyphusExamples",examples);
            report.addProperty("escharianOrientationSampleChunks",generationOrder.size());
            report.addProperty("escharianGeneratedAreaBlocks",generatedGrowth.size());
            report.addProperty("escharianUnsupportedScyphus",unsupportedPlants);
            report.addProperty("escharianWrongPlantBacking",wrongPlantBacking);
            report.addProperty("escharianElevatedRims",elevatedRims);
            report.add("escharianColonies",colonies);
            report.addProperty("escharianGeneratedAreaRims",generatedGrowth.stream().filter(pos -> level.getBlockState(pos).is(BlockInit.escharian_overgrowth_rim.get())).count());
            report.addProperty("sentinels",guardians.size());report.addProperty("uniqueSentinelHomes",uniqueHomes);
            report.addProperty("generatedAreaSentinels",generatedAreaGuardians.size());
            report.addProperty("generatedAreaUniqueSentinelHomes",generatedAreaUniqueHomes);
            report.addProperty("validSentinelCandidates",validSentinelCandidates);
            report.addProperty("outOfChunkWrites",BAD_WRITES.size());report.addProperty("timedChunks",costs.size());
            report.addProperty("activeLayouts",activeLayouts.size());report.addProperty("plannedConnectedBasins",basinCount);
            report.addProperty("plannedChannels",channelCount);report.addProperty("layoutsWithRaisedCourses",raisedLayouts);
            report.addProperty("materializedLayouts",materializedLayouts.size());report.addProperty("materializedRaisedLayouts",raisedRiverLayouts.size());
            report.addProperty("raisedRiverColumns",raisedRiverColumns);report.addProperty("archColumns",archColumns);
            report.addProperty("featureMedianMs",percentile(costs,.5));report.addProperty("featureP95Ms",percentile(costs,.95));
            report.addProperty("chunkMedianMs",percentile(chunks,.5));report.addProperty("chunkP95Ms",percentile(chunks,.95));
            report.addProperty("generationAndScanSeconds",(System.nanoTime()-start)/1e9);
            Files.writeString(Path.of("phlegethontic-validation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            LogUtils.getLogger().info("PHLEGETHONTIC_WORLD_VALIDATION {}",report);
            h.assertTrue(BAD_WRITES.isEmpty(),"Feature wrote outside its owning chunk: "+BAD_WRITES);
            h.assertTrue(exposed==0,"Found "+exposed+" exposed buried cores");
            h.assertTrue(guardians.size()==uniqueHomes,"Duplicate sentinel homes");
            h.assertTrue(generatedAreaGuardians.size()==generatedAreaUniqueHomes,"Duplicate generated-area sentinel homes");
            h.assertTrue(basinIchor>0,"Discovered Basin did not materialize any ichor");
            h.assertTrue(pointedSegments>0,"Discovered Basin did not materialize pointed scab formations");
            h.assertTrue(unsupportedPointedSegments==0,
                    "Found "+unsupportedPointedSegments+" pointed scab segments without a continuous supported chain");
            h.assertTrue(overgrowthOutsideBasin==0,"Escharian Overgrowth escaped the Basin biome");
            if(seed==20260912L) {
                h.assertTrue(overgrowthBlocks>0,"Known-positive seed did not materialize Escharian Overgrowth");
                h.assertTrue(colonies.get("invalid").getAsInt()==0,"Known-positive colony sizes, ground rims, tapering or coverage failed: "+colonies);
                int walls=Direction.Plane.HORIZONTAL.stream().mapToInt(d -> plants.get(d).size()).sum();
                int ceilings=plants.get(Direction.DOWN).size();
                h.assertTrue(generatedGrowth.stream().anyMatch(pos -> level.getBlockState(pos).is(BlockInit.escharian_overgrowth_rim.get())),"Known-positive seed must generate ground piles");
                h.assertTrue(walls>0 && ceilings>0,"Known-positive seed must contain both wall and ceiling colonies");
                h.assertTrue(walls+ceilings>plants.get(Direction.UP).size(),"Wall and ceiling colonies must dominate ground plants");
                h.assertTrue(scyphusCounts.values().stream().allMatch(total -> total>0),"Known-positive seed must use all five Scyphus counts: "+scyphusCounts);
            }
            h.assertTrue(colonies.get("unrooted").getAsInt()==0,"Every colony needs an infested foundation and a venous stone transition: "+colonies);
            h.assertTrue(unsupportedPlants==0 && wrongPlantBacking==0,"Scyphus must attach to valid colony backing");
            h.assertTrue(elevatedRims==0,"Separate rim blocks must be on the ground");
            h.assertTrue(overgrowthComponentsWithoutIchor==0,"An Escharian Overgrowth component has no anchor near ichor");
            h.runAfterDelay(120,() -> {
                List<String> escapes=new ArrayList<>();int flowing=0;
                for(int x=center.x-5;x<center.x+5;x++)for(int z=center.z-5;z<center.z+5;z++) {
                    var layouts=PhlegethonticBasinLayout.nearChunk(seed,x,z,seaLevel);
                    for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)for(int y=5;y<=100;y++) {
                        BlockPos p=new BlockPos(x*16+dx,y,z*16+dz);
                        if(!level.getFluidState(p).is(PhlegethonticTags.ICHOR) || initialFluid.contains(p.asLong()))continue;
                        flowing++;
                        if(y<=20 || PhlegethonticTerrainPlan.column(layouts,p.getX(),p.getZ()).distance()>0
                                || !level.getBiome(p).is(BiomeInit.PHLEGETHONTIC_BASIN))escapes.add(p.toShortString());
                    }
                }
                report.addProperty("fluidTickDuration",120);report.addProperty("newFlowingBlocks",flowing);
                report.addProperty("fluidEscapes",escapes.size());
                var loadedGuardians=level.getEntitiesOfClass(ExcoriatedSagittaryEntity.class,
                        new AABB((center.x-5)*16,0,(center.z-5)*16,(center.x+5)*16,128,(center.z+5)*16),ExcoriatedSagittaryEntity::isSentinel);
                var loadedGeneratedAreaGuardians=level.getEntitiesOfClass(ExcoriatedSagittaryEntity.class,
                        new AABB((center.x-12)*16,0,(center.z-12)*16,(center.x+12)*16,128,(center.z+12)*16),ExcoriatedSagittaryEntity::isSentinel);
                report.addProperty("sentinels",loadedGuardians.size());
                report.addProperty("uniqueSentinelHomes",loadedGuardians.stream().map(ExcoriatedSagittaryEntity::home).distinct().count());
                report.addProperty("generatedAreaSentinels",loadedGeneratedAreaGuardians.size());
                report.addProperty("generatedAreaUniqueSentinelHomes",loadedGeneratedAreaGuardians.stream()
                        .map(ExcoriatedSagittaryEntity::home).distinct().count());
                try {Files.writeString(Path.of("phlegethontic-validation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}
                catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
                for(int x=center.x-6;x<=center.x+6;x++)for(int z=center.z-6;z<=center.z+6;z++)level.setChunkForced(x,z,false);
                h.assertTrue(loadedGuardians.size()==loadedGuardians.stream().map(ExcoriatedSagittaryEntity::home).distinct().count(),"Duplicate loaded sentinel homes");
                h.assertTrue(loadedGeneratedAreaGuardians.size()==loadedGeneratedAreaGuardians.stream()
                        .map(ExcoriatedSagittaryEntity::home).distinct().count(),"Duplicate generated-area sentinel homes");
                h.assertTrue(escapes.isEmpty(),"Fluid escaped the planned courses: "+escapes.stream().limit(8).toList());h.succeed();
            });
        } catch(Exception e){throw new IllegalStateException("Fresh Nether validation failed",e);}
    }
    private static boolean nearIchor(ServerLevel level,BlockPos origin,int radius) {
        for(int dx=-radius;dx<=radius;dx++)for(int dy=-EscharianOvergrowthAnchors.OVERHEAD_ICHOR_REACH;dy<=radius;dy++)for(int dz=-radius;dz<=radius;dz++)
            if(level.getFluidState(origin.offset(dx,dy,dz)).is(PhlegethonticTags.ICHOR))return true;
        return false;
    }
    private static int componentsWithoutIchor(ServerLevel level,List<BlockPos> positions,int radius) {
        Set<BlockPos> remaining=new HashSet<>(positions);int misses=0;
        Set<BlockPos> visited=new HashSet<>();
        while(!remaining.isEmpty()) {
            BlockPos first=remaining.iterator().next();ArrayDeque<BlockPos> pending=new ArrayDeque<>();pending.add(first);
            boolean near=false;
            while(!pending.isEmpty()) {
                BlockPos current=pending.removeFirst();if(!visited.add(current))continue;
                remaining.remove(current);
                if(!near)near=nearIchor(level,current,radius);
                for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++) {
                    if(Math.abs(dx)+Math.abs(dy)+Math.abs(dz)>2)continue;
                    BlockPos neighbor=current.offset(dx,dy,dz);
                    // A sampled boundary can cut a colony into apparent islands. Follow the complete
                    // component through already loaded terrain before judging its proximity to ichor.
                    if(visited.contains(neighbor) || !level.hasChunk(neighbor.getX()>>4,neighbor.getZ()>>4))continue;
                    var state=level.getBlockState(neighbor);
                    if(state.is(BlockInit.escharian_overgrowth.get()) || state.is(BlockInit.escharian_overgrowth_rim.get()))
                        pending.addLast(neighbor);
                }
            }
            if(!near)misses++;
        }
        return misses;
    }
    private static List<BlockPos> growthPositions(ServerLevel level,List<ChunkPos> chunks) {
        return blockPositions(level,chunks,state -> state.is(BlockInit.escharian_overgrowth.get()) || state.is(BlockInit.escharian_overgrowth_rim.get()));
    }
    private static List<BlockPos> blockPositions(ServerLevel level,List<ChunkPos> chunks,
            java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> filter) {
        List<BlockPos> result=new ArrayList<>();
        for(var pos:chunks) {
            var chunk=level.getChunk(pos.x,pos.z);
            var sections=chunk.getSections();
            for(int index=0;index<sections.length;index++) {
                var section=sections[index];
                if(!section.getStates().maybeHas(filter))continue;
                int bottom=(chunk.getMinSection()+index)*16;
                for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<16;y++) {
                    var state=section.getBlockState(x,y,z);
                    if(filter.test(state))
                        result.add(new BlockPos(pos.getMinBlockX()+x,bottom+y,pos.getMinBlockZ()+z));
                }
            }
        }
        return result;
    }
    private static JsonObject inspectColonies(ServerLevel level,List<BlockPos> positions) {
        Set<BlockPos> remaining=new HashSet<>(positions);int patches=0,piles=0,invalid=0,unrooted=0;
        JsonArray sizes=new JsonArray();
        while(!remaining.isEmpty()) {
            Set<BlockPos> group=new HashSet<>();ArrayDeque<BlockPos> pending=new ArrayDeque<>();
            BlockPos first=remaining.iterator().next();remaining.remove(first);pending.add(first);
            while(!pending.isEmpty()) {
                BlockPos current=pending.removeFirst();group.add(current);
                for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++) {
                    if(Math.abs(dx)+Math.abs(dy)+Math.abs(dz)>2)continue;
                    BlockPos neighbor=current.offset(dx,dy,dz);if(remaining.remove(neighbor))pending.add(neighbor);
                }
            }
            boolean ground=group.stream().anyMatch(pos -> level.getBlockState(pos).is(BlockInit.escharian_overgrowth_rim.get()));
            if(ground)piles++;else patches++;
            int minY=group.stream().mapToInt(BlockPos::getY).min().orElseThrow();
            int width=group.stream().mapToInt(BlockPos::getX).max().orElseThrow()-group.stream().mapToInt(BlockPos::getX).min().orElseThrow()+1;
            int depth=group.stream().mapToInt(BlockPos::getZ).max().orElseThrow()-group.stream().mapToInt(BlockPos::getZ).min().orElseThrow()+1;
            int height=group.stream().mapToInt(BlockPos::getY).max().orElseThrow()-minY+1;
            boolean valid=ground ? width>=3 && width<=4 && depth>=3 && depth<=4 && height>=2 && height<=3
                    : group.size()>=10 && group.size()<=30;
            int surface=0,decorated=0;
            Set<BlockPos> groundAvailable=new HashSet<>(),groundPlants=new HashSet<>();
            for(BlockPos pos:group) {
                if(ground) {
                    if(pos.getY()>minY && !group.contains(pos.below()))valid=false;
                    if(level.getBlockState(pos).is(BlockInit.escharian_overgrowth_rim.get()) && (pos.getY()!=minY
                            || Direction.Plane.HORIZONTAL.stream().allMatch(d -> group.contains(pos.relative(d)))))valid=false;
                }
                boolean exposed=false,plant=false;
                for(Direction face:Direction.values()) {
                    BlockPos target=pos.relative(face);
                    var state=level.getBlockState(target);
                    if(ground && level.getBlockState(pos).is(BlockInit.escharian_overgrowth.get())
                            && (state.isAir() || state.is(BlockInit.escharian_scyphus.get()))) {
                        groundAvailable.add(target);
                        if(state.is(BlockInit.escharian_scyphus.get()))groundPlants.add(target);
                    }
                    exposed|=state.isAir() || state.is(BlockInit.escharian_scyphus.get());
                    plant|=state.is(BlockInit.escharian_scyphus.get()) && state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock.FACING)==face;
                }
                if(exposed)surface++;if(plant)decorated++;
            }
            valid &= ground ? groundPlants.size()>=5 && groundPlants.equals(groundAvailable)
                    : decorated>=3 && decorated>=Math.ceil(surface*.5) && decorated<=Math.floor(surface*.7);
            boolean rooted=true;
            if(ground)for(BlockPos pos:group)if(pos.getY()==minY && !level.getBlockState(pos.below()).is(BlockInit.infested_venous_stone.get()))rooted=false;
            Set<BlockPos> infested=new HashSet<>(),venous=new HashSet<>();
            for(BlockPos pos:group)for(int dx=-4;dx<=4;dx++)for(int dy=-4;dy<=4;dy++)for(int dz=-4;dz<=4;dz++) {
                if(Math.abs(dx)+Math.abs(dy)+Math.abs(dz)>5)continue;
                BlockPos target=pos.offset(dx,dy,dz);
                if(!level.hasChunk(target.getX()>>4,target.getZ()>>4))continue;
                var state=level.getBlockState(target);
                if(state.is(BlockInit.infested_venous_stone.get()))infested.add(target);
                if(state.is(BlockInit.venous_stone.get()))venous.add(target);
            }
            if(!rooted || infested.isEmpty() || venous.isEmpty())unrooted++;
            if(!valid)invalid++;
            JsonObject size=new JsonObject();size.addProperty("ground",ground);size.addProperty("blocks",group.size());
            size.addProperty("infestedStone",infested.size());size.addProperty("venousStone",venous.size());
            size.addProperty("dimensions",width+"x"+height+"x"+depth);size.addProperty("plants",ground?groundPlants.size():decorated);sizes.add(size);
        }
        JsonObject result=new JsonObject();result.addProperty("patches",patches);result.addProperty("piles",piles);
        result.addProperty("invalid",invalid);result.addProperty("unrooted",unrooted);result.add("sizes",sizes);return result;
    }
    private static Map<Direction,List<BlockPos>> scyphusPlants(ServerLevel level,List<BlockPos> backing) {
        Map<Direction,List<BlockPos>> result=new EnumMap<>(Direction.class);
        for(Direction normal:Direction.values())result.put(normal,new ArrayList<>());
        for(BlockPos pos:backing) {
            var state=level.getBlockState(pos);
            result.get(state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock.FACING)).add(pos);
        }
        return result;
    }
    private static double percentile(List<Double> values,double percentile){return values.isEmpty()?0:values.get(Math.min(values.size()-1,(int)(values.size()*percentile)));}
}
