package com.vincenthuto.hemomancy.client.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CachedMeshModelViewTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void cameraModelViewIsAppliedBeforeTheTreeLocalPose() {
        Matrix4f cameraModelView = new Matrix4f().rotateY((float) (Math.PI * 0.5));
        Matrix4f treeLocalPose = new Matrix4f().translate(3.0F, 4.0F, 5.0F);

        Vector3f transformedOrigin = CachedMeshModelView.compose(cameraModelView, treeLocalPose)
                .transformPosition(new Vector3f());

        assertEquals(5.0F, transformedOrigin.x, EPSILON);
        assertEquals(4.0F, transformedOrigin.y, EPSILON);
        assertEquals(-3.0F, transformedOrigin.z, EPSILON);
    }

    @Test
    void compositionDoesNotMutateSharedRenderMatrices() {
        Matrix4f cameraModelView = new Matrix4f().rotateX(0.37F);
        Matrix4f treeLocalPose = new Matrix4f().translate(2.0F, 3.0F, 4.0F);
        Matrix4f originalCamera = new Matrix4f(cameraModelView);
        Matrix4f originalLocal = new Matrix4f(treeLocalPose);

        CachedMeshModelView.compose(cameraModelView, treeLocalPose);

        assertEquals(originalCamera, cameraModelView);
        assertEquals(originalLocal, treeLocalPose);
    }
}
