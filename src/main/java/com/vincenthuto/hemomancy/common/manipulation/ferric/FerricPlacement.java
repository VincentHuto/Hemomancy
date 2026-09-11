package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public final class FerricPlacement {
    public record Placement(Direction facing, List<Vec3> origins) {}
    private FerricPlacement() {}

    public static Placement aimed(Player caster, Kind kind, double range) {
        Level level = caster.level();
        Vec3 eye = caster.getEyePosition(), end = eye.add(caster.getLookAngle().scale(range));
        var hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 desired = hit.getType() == HitResult.Type.MISS ? caster.position().add(caster.getLookAngle().scale(Math.min(8, range)))
                : hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(.02));
        Direction facing = Direction.fromYRot(caster.getYRot());
        BlockPos column = BlockPos.containing(desired);
        Vec3 center = supportedCenter(level, column);
        if (center == null) return null;
        var origins = new ArrayList<Vec3>();
        Direction across = facing.getClockWise();
        if (kind == Kind.SPIKE) {
            for (int i = -2; i <= 2; i++) {
                Vec3 at = supportedCenter(level, BlockPos.containing(center).relative(across, i));
                if (at == null || !valid(caster, kind, facing, at)) return null;
                origins.add(at);
            }
        } else {
            if (!valid(caster, kind, facing, center)) return null;
            if (kind == Kind.WALL) for (int i = -2; i <= 2; i++) {
                BlockPos floor = BlockPos.containing(center).relative(across, i).below();
                if (!level.hasChunkAt(floor) || !level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) return null;
            }
            origins.add(center);
        }
        return new Placement(facing, List.copyOf(origins));
    }

    private static Vec3 supportedCenter(Level level, BlockPos column) {
        for (int offset = 1; offset >= -4; offset--) {
            BlockPos at = column.above(offset), floor = at.below();
            if (!level.hasChunkAt(at) || level.isOutsideBuildHeight(at) || !level.getWorldBorder().isWithinBounds(at)) continue;
            if (level.getBlockState(at).getCollisionShape(level, at).isEmpty()
                    && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) return Vec3.atBottomCenterOf(at);
        }
        return null;
    }

    private static boolean valid(Player caster, Kind kind, Direction facing, Vec3 at) {
        Level level = caster.level();
        var box = FerricConstructShapes.bounds(kind, facing, at).deflate(.001);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ)))
            if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)) return false;
        if (level.getBlockCollisions(caster, box).iterator().hasNext()) return false;
        return level.getEntities((net.minecraft.world.entity.Entity)null,box,entity -> entity.isAlive() && (entity instanceof LivingEntity living
                ? kind!=Kind.SPIKE || !ManipulationCombatHelper.canHarm(caster,living)
                : entity.canBeCollidedWith())).isEmpty();
    }
}
