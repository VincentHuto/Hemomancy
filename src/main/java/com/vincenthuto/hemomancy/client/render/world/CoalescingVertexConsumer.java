package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/** Blood-rich patches connect into the school material; expiry drains those patches away. */
final class CoalescingVertexConsumer implements VertexConsumer {
    final VertexConsumer delegate;
    private final float formation,departure;
    final boolean draining;
    private float alpha=1,blood;

    CoalescingVertexConsumer(VertexConsumer delegate,float formation,float departure) {
        this(delegate,formation,departure,false);
    }
    CoalescingVertexConsumer(VertexConsumer delegate,float formation,float departure,boolean draining) {
        this.delegate=delegate;this.formation=formation;this.departure=departure;
        this.draining=draining;
    }
    @Override public VertexConsumer addVertex(Matrix4f pose,float x,float y,float z) {
        alpha=ManipulationLifecycle.reveal(formation,x,y,z)
                *ManipulationLifecycle.reveal(1-departure,x,y,z);
        blood=Math.max(1-formation,departure)*.8F;
        delegate.addVertex(pose,x,y-(draining?departure*.22F:0),z);
        return this;
    }
    @Override public VertexConsumer addVertex(float x,float y,float z){delegate.addVertex(x,y,z);return this;}
    @Override public VertexConsumer setColor(int r,int g,int b,int a) {
        delegate.setColor((int)(r+(132-r)*blood),(int)(g+(13-g)*blood),
                (int)(b+(37-b)*blood),(int)(a*alpha));return this;
    }
    @Override public VertexConsumer setUv(float u,float v){delegate.setUv(u,v);return this;}
    @Override public VertexConsumer setUv1(int u,int v){delegate.setUv1(u,v);return this;}
    @Override public VertexConsumer setUv2(int u,int v){delegate.setUv2(u,v);return this;}
    @Override public VertexConsumer setNormal(float x,float y,float z){delegate.setNormal(x,y,z);return this;}
}
