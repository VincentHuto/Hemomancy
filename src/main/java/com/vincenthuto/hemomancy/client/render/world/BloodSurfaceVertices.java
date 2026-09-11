package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;

/** Selects the unmasked blood surface for authored model UVs. */
public final class BloodSurfaceVertices implements VertexConsumer {
    private final VertexConsumer target;
    public BloodSurfaceVertices(VertexConsumer target){this.target=target;}
    public VertexConsumer addVertex(float x,float y,float z){target.addVertex(x,y,z);return this;}
    public VertexConsumer setColor(int r,int g,int b,int a){
        // Keep the authored sword's face shading; neutral model tint belongs to the material.
        int shade=r==g&&g==b?255:Math.max(r,Math.max(g,b));
        target.setColor(shade,shade,shade,a);return this;
    }
    public VertexConsumer setUv(float u,float v){target.setUv(18+Math.clamp(u,0F,1.99F)*.49f,Math.clamp(v,0F,1.99F)*.49f);return this;}
    public VertexConsumer setUv1(int u,int v){target.setUv1(u,v);return this;}
    public VertexConsumer setUv2(int u,int v){target.setUv2(u,v);return this;}
    public VertexConsumer setNormal(float x,float y,float z){target.setNormal(x,y,z);return this;}
}
