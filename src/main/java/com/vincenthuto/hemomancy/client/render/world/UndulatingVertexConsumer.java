package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Displaces before the pose transform, so camera movement cannot change the contour. */
final class UndulatingVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final ManipulationUndulation undulation;
    private final Vector3f offset=new Vector3f();

    UndulatingVertexConsumer(VertexConsumer delegate,ManipulationUndulation undulation) {
        this.delegate=delegate;this.undulation=undulation;
    }

    static VertexConsumer fixed(VertexConsumer consumer) {
        if(consumer instanceof UndulatingVertexConsumer warped)return fixed(warped.delegate);
        return consumer instanceof CoalescingVertexConsumer forming?forming.delegate:consumer;
    }

    static boolean omitsBoundary(VertexConsumer consumer) {
        if(consumer instanceof UndulatingVertexConsumer warped)return omitsBoundary(warped.delegate);
        return consumer instanceof CoalescingVertexConsumer forming && forming.draining;
    }

    @Override public VertexConsumer addVertex(Matrix4f pose,float x,float y,float z) {
        undulation.offset(x,y,z,offset);
        delegate.addVertex(pose,x+offset.x,y+offset.y,z+offset.z);
        return this;
    }

    @Override public VertexConsumer addVertex(float x,float y,float z){delegate.addVertex(x,y,z);return this;}
    @Override public VertexConsumer setColor(int r,int g,int b,int a){delegate.setColor(r,g,b,a);return this;}
    @Override public VertexConsumer setUv(float u,float v){delegate.setUv(u,v);return this;}
    @Override public VertexConsumer setUv1(int u,int v){delegate.setUv1(u,v);return this;}
    @Override public VertexConsumer setUv2(int u,int v){delegate.setUv2(u,v);return this;}
    @Override public VertexConsumer setNormal(float x,float y,float z){delegate.setNormal(x,y,z);return this;}
}
