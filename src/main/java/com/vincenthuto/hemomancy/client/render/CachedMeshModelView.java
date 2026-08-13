package com.vincenthuto.hemomancy.client.render;

import org.joml.Matrix4f;

/** Composes Minecraft's camera model-view with a cached mesh's local pose. */
public final class CachedMeshModelView {
    private CachedMeshModelView() { }

    public static Matrix4f compose(Matrix4f cameraModelView, Matrix4f localPose) {
        return new Matrix4f(cameraModelView).mul(localPose);
    }
}
