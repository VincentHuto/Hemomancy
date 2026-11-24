package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

/** A model cube with an independent atlas rectangle on each face. */
final class CrimsonDoeCube extends ModelPart.Cube {
    private static final Direction[] DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP
    };
    // Blockbench's up and down face names are reflected along with its Y axis.
    private static final int[][] CORNERS = {
            {1, 0, 2, 3}, {5, 1, 3, 7}, {4, 5, 7, 6}, {0, 4, 6, 2}, {0, 1, 5, 4}, {6, 7, 3, 2}
    };

    private final Vector3f[] corners;
    private final float[] uv;

    CrimsonDoeCube(float x, float y, float z, float maxX, float maxY, float maxZ, int... uv) {
        super(0, 0, x, y, z, maxX - x, maxY - y, maxZ - z,
                0, 0, 0, false, CrimsonDoeMesh.TEXTURE_WIDTH, CrimsonDoeMesh.TEXTURE_HEIGHT, Set.of());
        corners = new Vector3f[]{
                point(x, y, z), point(maxX, y, z), point(x, maxY, z), point(maxX, maxY, z),
                point(x, y, maxZ), point(maxX, y, maxZ), point(x, maxY, maxZ), point(maxX, maxY, maxZ)
        };
        this.uv = new float[uv.length];
        for (int i = 0; i < uv.length; i++) {
            this.uv[i] = (float) uv[i] / (i % 2 == 0 ? CrimsonDoeMesh.TEXTURE_WIDTH : CrimsonDoeMesh.TEXTURE_HEIGHT);
        }
    }

    private static Vector3f point(float x, float y, float z) {
        return new Vector3f(x / 16.0F, y / 16.0F, z / 16.0F);
    }

    @Override
    public void compile(PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int face = 0; face < DIRECTIONS.length; face++) {
            var direction = DIRECTIONS[face];
            pose.transformNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ(), normal);
            int uvStart = face * 4;
            for (int i = 0; i < 4; i++) {
                pose.pose().transformPosition(corners[CORNERS[face][i]], position);
                float u = uv[uvStart + (i == 1 || i == 2 ? 2 : 0)];
                float v = uv[uvStart + (i >= 2 ? 3 : 1)];
                buffer.addVertex(position.x(), position.y(), position.z(), color, u, v, packedOverlay, packedLight,
                        normal.x(), normal.y(), normal.z());
            }
        }
    }
}
