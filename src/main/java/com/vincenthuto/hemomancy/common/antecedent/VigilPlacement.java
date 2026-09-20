package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.mixin.core.SinglePoolElementAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import com.vincenthuto.hemomancy.mixin.core.VigilTemplateAccessor;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import java.util.ArrayList;
import java.util.List;

/** Fit an optional annex after vanilla has finished selecting every city piece. */
public final class VigilPlacement {
    private VigilPlacement() {}
    public static boolean supports(PoolElementStructurePiece center) {
        if (!(center.getElement() instanceof SinglePoolElement single)) return false;
        var id = ((SinglePoolElementAccessor) single).hemomancy$template().left().orElse(null);
        return id != null && id.getNamespace().equals("minecraft")
                && id.getPath().matches("ancient_city/city_center/city_center_[123]");
    }
    public static List<PoolElementStructurePiece> find(StructureTemplateManager templates,
            LevelHeightAccessor height, List<StructurePiece> city) {
        if (city.isEmpty() || !(city.getFirst() instanceof PoolElementStructurePiece center) || !supports(center))
            return List.of();
        var protection=protect(templates,city);
        record Entrance(BlockPos floor, Rotation rotation) {}
        var entrances = new HashSet<Entrance>();
        for (var piece : city) {
            if (!(piece instanceof PoolElementStructurePiece pool) || !(pool.getElement() instanceof SinglePoolElement single)) continue;
            var id=((SinglePoolElementAccessor)single).hemomancy$template().left().orElse(null);
            if (id==null || !id.getNamespace().equals("minecraft")) continue;
            var path=id.getPath();
            if (!path.startsWith("ancient_city/") || pool.getElement().getProjection()!=StructureTemplatePool.Projection.RIGID) continue;
            var template=templates.getOrCreate(id);
            var palettes=((VigilTemplateAccessor)template).hemomancy$palettes();
            if (palettes.isEmpty()) continue;
            var blocks=new HashMap<BlockPos,BlockState>();
            for(var block:palettes.getFirst().blocks()) blocks.put(block.pos(),block.state());
            for(var block:palettes.getFirst().blocks()) {
                var local=block.pos();
                var floor=pool.getPosition().offset(local.rotate(pool.getRotation()));
                if(floor.getY()<height.getMinBuildHeight()+13 || floor.getY()>center.getPosition().getY()+11) continue;
                for(var outward:Direction.Plane.HORIZONTAL) {
                    var outside=local.relative(outward);
                    var edge=blocks.get(outside);
                    if(edge!=null && !edge.isAir() && !edge.is(net.minecraft.world.level.block.Blocks.STRUCTURE_VOID)) continue;
                    boolean clear=true;
                    for(int side=-1;side<=1 && clear;side++) {
                        var at=local.relative(outward.getClockWise(),side);
                        var support=blocks.get(at);
                        if(support==null || !support.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE,at)) {clear=false;break;}
                        for(int y=1;y<=3;y++) {
                            var space=blocks.get(at.above(y));
                            var world=pool.getPosition().offset(at.above(y).rotate(pool.getRotation()));
                            if(protection.solid().contains(world) || space!=null && !space.isAir()
                                    && !space.is(net.minecraft.world.level.block.Blocks.STRUCTURE_VOID)) {clear=false;break;}
                        }
                    }
                    if(!clear) continue;
                    var direction=pool.getRotation().rotate(outward);
                    var rotation=switch(direction) {
                        case SOUTH -> Rotation.NONE; case WEST -> Rotation.CLOCKWISE_90;
                        case NORTH -> Rotation.CLOCKWISE_180; default -> Rotation.COUNTERCLOCKWISE_90;
                    };
                    entrances.add(new Entrance(floor.relative(direction),rotation));
                }
            }
        }
        // Search across the finished city, without preferring or reserving the portal.
        // Position hashing makes selection deterministic without consuming city RNG.
        var ordered=new ArrayList<>(entrances);
        ordered.sort(Comparator.comparingLong((Entrance e)->net.minecraft.util.Mth.getSeed(e.floor()))
                .thenComparingInt(e->e.floor().getX()).thenComparingInt(e->e.floor().getZ())
                .thenComparingInt(e->e.rotation().ordinal()));
        for(var entrance:ordered) for(var layout:VigilModules.ALL) {
            var rotation=Rotation.values()[Math.floorMod(entrance.rotation().ordinal()-layout.entryRotation().ordinal(),4)];
            var origin=entrance.floor().subtract(layout.entry().rotate(rotation));
            var result=candidate(templates,height,protection,layout,origin,rotation);
            if(!result.isEmpty()) return result;
        }
        return List.of();
    }
    private record Protection(Set<BlockPos> blocks, Set<BlockPos> solid, Set<BlockPos> headroom, List<StructurePiece> unknown) {}
    private static Protection protect(StructureTemplateManager templates,List<StructurePiece> city) {
        var occupied=new HashSet<BlockPos>();
        var solid=new HashSet<BlockPos>();
        var headroom=new HashSet<BlockPos>();
        var unknown=new ArrayList<StructurePiece>();
        for(var piece:city) {
            if(!(piece instanceof PoolElementStructurePiece pool) || !(pool.getElement() instanceof SinglePoolElement single)) {
                unknown.add(piece);continue;
            }
            var id=((SinglePoolElementAccessor)single).hemomancy$template().left().orElse(null);
            if(id==null || pool.getElement().getProjection()!=StructureTemplatePool.Projection.RIGID) {unknown.add(piece);continue;}
            var template=templates.getOrCreate(id);
            for(var palette:((VigilTemplateAccessor)template).hemomancy$palettes()) for(var block:palette.blocks()) {
                if(block.state().is(net.minecraft.world.level.block.Blocks.STRUCTURE_VOID))continue;
                var at=pool.getPosition().offset(block.pos().rotate(pool.getRotation()));
                occupied.add(at);
                if(!block.state().isAir())solid.add(at);
                if(block.state().isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE,block.pos())) {
                    headroom.add(at.above());headroom.add(at.above(2));
                }
            }
        }
        headroom.removeAll(occupied);
        return new Protection(occupied,solid,headroom,unknown);
    }

    private static List<PoolElementStructurePiece> candidate(StructureTemplateManager templates,
            LevelHeightAccessor height, Protection protection, VigilModules.Layout layout, BlockPos origin, Rotation rotation) {
        var result = new ArrayList<PoolElementStructurePiece>();
        for (var part : layout.parts()) {
            var element = StructurePoolElement.single("hemomancy:antecedent/"+part.name())
                    .apply(StructureTemplatePool.Projection.RIGID);
            var position = origin.offset(part.offset().rotate(rotation));
            var bounds = element.getBoundingBox(templates,position,rotation);
            if (bounds.minY() < height.getMinBuildHeight()+5 || bounds.maxY() >= height.getMaxBuildHeight()
                    || protection.unknown().stream().anyMatch(piece -> piece.getBoundingBox().intersects(bounds))) return List.of();
            var template=templates.getOrCreate(net.minecraft.resources.ResourceLocation.parse("hemomancy:antecedent/"+part.name()));
            for(var palette:((VigilTemplateAccessor)template).hemomancy$palettes()) for(var block:palette.blocks()) {
                var at=position.offset(block.pos().rotate(rotation));
                if(protection.blocks().contains(at) || !block.state().isAir() && protection.headroom().contains(at)) return List.of();
            }
            result.add(new PoolElementStructurePiece(templates,element,position,0,rotation,bounds,LiquidSettings.IGNORE_WATERLOGGING));
        }
        return List.copyOf(result);
    }
}
