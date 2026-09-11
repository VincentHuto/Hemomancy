package com.vincenthuto.hemomancy.client.render.world;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BloodSurfaceVerticesTest {
    @Test void authoredFaceContrastSurvivesTheMaterialPalette() {
        var capture=new Capture();var surface=new BloodSurfaceVertices(capture);
        surface.setColor(96,7,12,180);assertEquals(96,capture.red);assertEquals(180,capture.alpha);
        surface.setColor(200,12,30,180);assertEquals(200,capture.red);
        surface.setColor(128,128,128,77);assertEquals(255,capture.red);assertEquals(77,capture.alpha);
    }
    @Test void tiledModelUvsCannotSelectTheDropletOrSporeMasks() {
        var capture=new Capture();var surface=new BloodSurfaceVertices(capture);
        for(float coordinate:new float[]{-1,0,.5f,1,4}) {
            surface.setUv(coordinate,coordinate);
            assertEquals(9,(int)Math.floor(capture.u/2));
            assertTrue(capture.v>=0&&capture.v<1);
        }
    }
    private static final class Capture implements VertexConsumer {
        int red,alpha;float u,v;
        public VertexConsumer addVertex(float x,float y,float z){return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){red=r;alpha=a;assertEquals(r,g);assertEquals(r,b);return this;}
        public VertexConsumer setUv(float u,float v){this.u=u;this.v=v;return this;}
        public VertexConsumer setUv1(int u,int v){return this;}
        public VertexConsumer setUv2(int u,int v){return this;}
        public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
}
