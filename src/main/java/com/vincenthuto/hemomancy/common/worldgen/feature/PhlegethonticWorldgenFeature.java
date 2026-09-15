package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.worldgen.*;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.*;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedSagittarySpawnRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import java.util.*;

public final class PhlegethonticWorldgenFeature extends Feature<NoneFeatureConfiguration> {
    private static final Map<net.minecraft.server.level.ServerLevel,com.google.common.cache.Cache<Long,Optional<BlockPos>>> SENTINEL_SHORES=
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final com.google.common.cache.Cache<Long,List<Voxel>> SENTINEL_CANDIDATES=
            com.google.common.cache.CacheBuilder.newBuilder().maximumSize(2048).build();
    private final boolean basin;
    public PhlegethonticWorldgenFeature(boolean basin) {super(NoneFeatureConfiguration.CODEC);this.basin=basin;}

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level=context.level();
        if(!level.getLevel().dimension().equals(Level.NETHER)) return false;
        ChunkPos chunk=new ChunkPos(context.origin());
        if(!basin && PhlegethonticVeinPath.nearChunk(level.getSeed(),chunk.x,chunk.z,level.getMinBuildHeight()).isEmpty())return false;
        List<BoundingBox> protectedBounds=new ArrayList<>();
        for(int x=chunk.x-1;x<=chunk.x+1;x++) for(int z=chunk.z-1;z<=chunk.z+1;z++) {
            var neighbor=level.getChunk(x,z);
            for(var reference:neighbor.getAllReferences().entrySet())for(long origin:reference.getValue()) {
                int originX=ChunkPos.getX(origin),originZ=ChunkPos.getZ(origin);
                if(!level.hasChunk(originX,originZ)) {
                    // A neighbor can reference a start beyond this generation region's dependency radius.
                    // Seal against that neighbor instead of requesting an unavailable chunk.
                    protectedBounds.add(new BoundingBox(x*16,level.getMinBuildHeight(),z*16,
                            x*16+15,level.getMaxBuildHeight()-1,z*16+15));
                    continue;
                }
                var start=level.getChunk(originX,originZ).getStartForStructure(reference.getKey());
                if(start!=null && start.isValid())protectedBounds.add(start.getBoundingBox());
            }
        }
        if(!basin) {
            level.getChunk(chunk.x,chunk.z).setData(HemoAttachmentTypes.PHLEGETHONTIC_PENDING_VEIN,List.copyOf(protectedBounds));
            return true;
        }
        return materialize(level,level.getChunk(chunk.x,chunk.z),protectedBounds,true)>0;
    }

    /** Neighbor FEATURES are complete at LIGHT. Persisted jobs also survive saving an unfinished protochunk. */
    public static int finishVein(WorldGenLevel level,ChunkAccess chunk) {
        if(!chunk.hasData(HemoAttachmentTypes.PHLEGETHONTIC_PENDING_VEIN))return 0;
        int placed=materialize(level,chunk,chunk.getData(HemoAttachmentTypes.PHLEGETHONTIC_PENDING_VEIN),false);
        chunk.removeData(HemoAttachmentTypes.PHLEGETHONTIC_PENDING_VEIN);
        if(placed>0)chunk.initializeLightSources();
        return placed;
    }

    private static int materialize(WorldGenLevel level,ChunkAccess writable,List<BoundingBox> protectedBounds,boolean basin) {
        ChunkPos chunk=writable.getPos();
        Map<Voxel,BlockState> states=new HashMap<>();
        java.util.function.Function<Voxel,BlockState> state=p -> states.computeIfAbsent(p,
                key -> level.getBlockState(new BlockPos(key.x(),key.y(),key.z())));
        java.util.function.Predicate<Voxel> protectedPosition=p -> p.y()<level.getMinBuildHeight()+5
                || p.y()>=level.getMaxBuildHeight()-5 || protectedBounds.stream().anyMatch(b -> b.isInside(p.x(),p.y(),p.z()));
        java.util.function.Predicate<Voxel> replaceable=p -> {
            if(protectedPosition.test(p)) return false;
            if(!basin && PhlegethonticOreReservations.reserved(level,new BlockPos(p.x(),p.y(),p.z())))return false;
            BlockState existing=state.apply(p);
            if(existing.hasBlockEntity()) return false;
            return existing.is(basin?PhlegethonticTags.BASIN_REPLACEABLE:PhlegethonticTags.VEIN_REPLACEABLE)
                    || existing.is(BlockInit.blood_scorched_scab.get())
                    || basin && existing.is(BlockInit.pointed_blood_scorched_scab.get())
                    || existing.getFluidState().is(PhlegethonticTags.ICHOR)
                    || basin && existing.isAir();
        };
        List<PhlegethonticBasinLayout> layouts=basin?PhlegethonticBasinLayout.nearChunk(level.getSeed(),chunk.x,chunk.z,
                level.getLevel().getChunkSource().getGenerator().getSeaLevel()):List.of();
        Map<Voxel,Material> plan;
        if(basin) {
            plan=PhlegethonticTerrainPlan.basin(layouts,level.getSeed(),chunk.x,chunk.z,
                    level.getMinBuildHeight(),level.getMaxBuildHeight(),
                    p -> !protectedPosition.test(p) && level.getBiome(new BlockPos(p.x(),p.y(),p.z())).is(BiomeInit.PHLEGETHONTIC_BASIN)
                            && replaceable.test(p),replaceable);
        } else {
            var nodes=PhlegethonticVeinPath.nearChunk(level.getSeed(),chunk.x,chunk.z,level.getMinBuildHeight());
            if(nodes.isEmpty()) return 0;
            plan=PhlegethonticTerrainPlan.vein(nodes,chunk.x,chunk.z,replaceable);
        }
        int placed=0;
        // Finish every solid before opening cavities or introducing ticking fluid.
        for(int pass=0;pass<3;pass++) for(var entry:plan.entrySet()) {
            Material material=entry.getValue();
            int stage=material==Material.ICHOR?2:material==Material.AIR?1:0;
            if(stage!=pass) continue;
            Voxel p=entry.getKey();BlockPos pos=new BlockPos(p.x(),p.y(),p.z());
            if(!PhlegethonticTerrainPlan.owns(p,chunk.x,chunk.z))throw new IllegalStateException("Phlegethontic plan escaped its chunk");
            if(basin) {
                if(!level.ensureCanWrite(pos))continue;
                if(level.setBlock(pos,block(material),2))placed++;
            } else {
                BlockState replacement=block(material);
                if(!writable.getBlockState(pos).equals(replacement)){writable.setBlockState(pos,replacement,false);placed++;}
            }
            if(material==Material.ICHOR)level.scheduleTick(pos,FluidInit.PHLEGETHONTIC_ICHOR.get(),20);
        }
        if(basin && placed>0) {
            placed+=formations(level,chunk,plan);
            sentinels(level,chunk,layouts);
        }
        return placed;
    }

    private static int formations(WorldGenLevel level,ChunkPos chunk,Map<Voxel,Material> terrain) {
        int placed=0;
        for(var segment:PhlegethonticFormationPlan.plan(level.getSeed(),chunk.x,chunk.z,terrain,
                p -> level.getBlockState(new BlockPos(p.x(),p.y(),p.z())).isAir(),
                (p,direction) -> {
                    BlockPos supportPos=new BlockPos(p.x(),p.y(),p.z());
                    return level.getBlockState(supportPos).isFaceSturdy(level,supportPos,direction);
                })) {
            BlockPos pos=new BlockPos(segment.pos().x(),segment.pos().y(),segment.pos().z());
            if(!level.ensureCanWrite(pos) || !level.getBlockState(pos).isAir())continue;
            BlockState state=BlockInit.pointed_blood_scorched_scab.get().defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION,segment.direction())
                    .setValue(PointedDripstoneBlock.THICKNESS,segment.thickness())
                    .setValue(PointedDripstoneBlock.WATERLOGGED,false);
            if(level.setBlock(pos,state,2))placed++;
        }
        return placed;
    }

    private static BlockState block(Material material) {
        return switch(material) {
            case AIR -> Blocks.CAVE_AIR.defaultBlockState();
            case SCAB -> BlockInit.blood_scorched_scab.get().defaultBlockState();
            case BLACKSTONE -> Blocks.BLACKSTONE.defaultBlockState();
            case BASALT -> Blocks.BASALT.defaultBlockState();
            case VENOUS -> BlockInit.venous_stone.get().defaultBlockState();
            case ICHOR -> BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get().defaultBlockState();
        };
    }

    private static void sentinels(WorldGenLevel level,ChunkPos chunk,List<PhlegethonticBasinLayout> layouts) {
        if(level.getDifficulty()==Difficulty.PEACEFUL) return;
        for(var layout:layouts) {
            List<Voxel> candidates=SENTINEL_CANDIDATES.asMap().computeIfAbsent(layout.id(),key -> layout.sentinelCandidates());
            if(candidates.stream().noneMatch(p -> PhlegethonticTerrainPlan.owns(p,chunk.x,chunk.z)))continue;
            com.google.common.cache.Cache<Long,Optional<BlockPos>> shores;
            synchronized(SENTINEL_SHORES) {
                shores=SENTINEL_SHORES.computeIfAbsent(level.getLevel(),key -> com.google.common.cache.CacheBuilder.newBuilder().maximumSize(2048).build());
            }
            Optional<BlockPos> cached=shores.getIfPresent(layout.id());
            if(cached==null) {
                cached=Optional.ofNullable(sentinelShore(level,layout,candidates));shores.put(layout.id(),cached);
            }
            if(cached.isEmpty())continue;
            BlockPos pos=cached.get();
            if(!new ChunkPos(pos).equals(chunk)) continue;
            if(!ExcoriatedSagittarySpawnRules.validShore(level,pos)) continue;
            var guardian=EntityInit.excoriated_sagittary.get().create(level.getLevel());
            if(guardian==null) continue;
            guardian.setUUID(new UUID(layout.id(),PhlegethonticRules.seed(level.getSeed(),chunk.x,chunk.z,layout.id())));
            guardian.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            guardian.setHome(pos,true);
            guardian.setPersistenceRequired();
            guardian.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),MobSpawnType.CHUNK_GENERATION,null);
            level.addFreshEntity(guardian);
        }
    }

    private static BlockPos sentinelShore(WorldGenLevel level,PhlegethonticBasinLayout layout,List<Voxel> candidates) {
            var layouts=PhlegethonticBasinLayout.nearChunk(level.getSeed(),layout.cellX()*8,layout.cellZ()*8,
                    level.getLevel().getChunkSource().getGenerator().getSeaLevel());
            for(Voxel candidate:candidates) {
                boolean shore=true;
                for(int dx=-1;dx<=1 && shore;dx++)for(int dz=-1;dz<=1 && shore;dz++) {
                    var c=PhlegethonticTerrainPlan.column(layouts,candidate.x()+dx,candidate.z()+dz);
                    if(c.distance()<1 || c.distance()>9 || candidate.y()<c.surface()+2 || candidate.y()>c.surface()+4) {shore=false;break;}
                    for(int y=c.floor()-2;y<=candidate.y()+2;y++)
                        if(!level.getLevel().getUncachedNoiseBiome((candidate.x()+dx)>>2,y>>2,(candidate.z()+dz)>>2)
                                .is(BiomeInit.PHLEGETHONTIC_BASIN)) {shore=false;break;}
                }
                if(shore && theoreticalIchorNear(level,layouts,candidate))
                    return new BlockPos(candidate.x(),candidate.y(),candidate.z());
            }
            return null;
    }

    private static boolean theoreticalIchorNear(WorldGenLevel level,List<PhlegethonticBasinLayout> layouts,Voxel candidate) {
        for(int dx=-8;dx<=8;dx++)for(int dz=-8;dz<=8;dz++) {
            if(dx*dx+dz*dz>64)continue;
            int x=candidate.x()+dx,z=candidate.z()+dz;
            var column=PhlegethonticTerrainPlan.column(layouts,x,z);
            if(column.distance()<=0 && Math.abs(column.surface()-candidate.y())<=2
                    && level.getLevel().getUncachedNoiseBiome(x>>2,column.surface()>>2,z>>2)
                    .is(BiomeInit.PHLEGETHONTIC_BASIN))return true;
        }
        return false;
    }
}
