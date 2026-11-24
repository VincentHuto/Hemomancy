package com.vincenthuto.hemomancy.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Bakes the cube's fixed rotation and independent Blockbench face UVs. */
public final class FaceUvCube extends ModelPart.Cube {
    private static final Direction[] DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP
    };

    // The Y reflection swaps the top and bottom face names and reverses the winding.
    private static final int[][] CORNERS = {
            {1, 0, 2, 3}, {5, 1, 3, 7}, {4, 5, 7, 6}, {0, 4, 6, 2}, {0, 1, 5, 4}, {6, 7, 3, 2}
    };

    private final Vector3f[] corners = new Vector3f[8];
    private final Vector3f[] normals = new Vector3f[6];
    private final float[] uv;
    private final boolean reverseWinding;

    public FaceUvCube(int width, int height, float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                   float x, float y, float z, float pitch, float yaw, float roll, float... uv) {
        this(width, height, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, pitch, yaw, roll, false, uv);
    }

    public FaceUvCube(int width, int height, float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                   float x, float y, float z, float pitch, float yaw, float roll, boolean reverseWinding, float... uv) {
        super(0, 0, minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ,
                0, 0, 0, false, width, height, Set.of());
        var rotation = new Quaternionf().rotationZYX(roll * Mth.DEG_TO_RAD, yaw * Mth.DEG_TO_RAD, pitch * Mth.DEG_TO_RAD);
        for (int i = 0; i < corners.length; i++) {
            corners[i] = new Vector3f((i & 1) == 0 ? minX : maxX, (i & 2) == 0 ? minY : maxY,
                    (i & 4) == 0 ? minZ : maxZ).rotate(rotation).add(x, y, z).div(16);
        }
        for (int i = 0; i < normals.length; i++) {
            Direction side = DIRECTIONS[i];
            normals[i] = new Vector3f(side.getStepX(), side.getStepY(), side.getStepZ()).rotate(rotation);
        }
        this.uv = uv;
        this.reverseWinding = reverseWinding;
        for (int i = 0; i < uv.length; i++) {
            uv[i] /= i % 2 == 0 ? width : height;
        }
    }

    @Override
    public void compile(PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        var position = new Vector3f();
        var normal = new Vector3f();
        for (int face = 0; face < DIRECTIONS.length; face++) {
            int start = face * 4;
            if (Float.isNaN(uv[start])) continue;
            pose.transformNormal(normals[face], normal);
            for (int vertex = 0; vertex < 4; vertex++) {
                // An X reflection in the entity renderer reverses the triangle winding again.
                int i = reverseWinding ? 3 - vertex : vertex;
                pose.pose().transformPosition(corners[CORNERS[face][i]], position);
                float u = uv[start + (i == 1 || i == 2 ? 2 : 0)];
                float v = uv[start + (i >= 2 ? 3 : 1)];
                buffer.addVertex(position.x(), position.y(), position.z(), color, u, v, overlay, light,
                        normal.x(), normal.y(), normal.z());
            }
        }
    }
}
