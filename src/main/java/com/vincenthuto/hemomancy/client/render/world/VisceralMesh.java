package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Small textured surfaces used by spell bodies, strands and material fragments. */
public final class VisceralMesh {
    private VisceralMesh() {}

    public static void quad(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
            int color, float alpha, double u0, double v0, double u1, double v1) {
        // One texture tile per block at most, including narrow faces and fragment edges.
        u1 = u0 + Math.copySign(Math.min(Math.abs(u1-u0), Math.max(a.distanceTo(b),d.distanceTo(c))),u1-u0);
        v1 = v0 + Math.copySign(Math.min(Math.abs(v1-v0), Math.max(a.distanceTo(d),b.distanceTo(c))),v1-v0);
        vertex(p,v,a,color,alpha,u0,v0); vertex(p,v,b,color,alpha,u1,v0);
        vertex(p,v,c,color,alpha,u1,v1); vertex(p,v,d,color,alpha,u0,v1);
    }

    private static void vertex(PoseStack p, VertexConsumer v, Vec3 point, int color, float alpha, double u, double texV) {
        v.addVertex(p.last().pose(), (float)point.x, (float)point.y, (float)point.z)
                .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, (int)(Mth.clamp(alpha,0,1)*255))
                .setUv((float)u, (float)texV);
    }

    public static int shade(int color, double factor) {
        int r = (int)Math.min(255, ((color >> 16) & 255) * factor);
        int g = (int)Math.min(255, ((color >> 8) & 255) * factor);
        int b = (int)Math.min(255, (color & 255) * factor);
        return (r << 16) | (g << 8) | b;
    }

    public static void tube(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, double width, int color, float alpha) {
        segment(p,v,a,b,width,width,color,alpha,0,a.distanceTo(b));
    }

    public static void segment(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, double startWidth, double endWidth,
            int color, float alpha, double texStart, double texEnd) {
        Vec3 axis = b.subtract(a);
        if (axis.lengthSqr() < 1e-12 || alpha <= 0) return;
        Vec3 side = VisceralGeometry.side(axis), up = axis.normalize().cross(side);
        double circumference = Math.PI*2*Math.max(startWidth,endWidth);
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3, next = (i + 1) * Math.PI / 3;
            Vec3 ra = side.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3 rb = side.scale(Math.cos(next)).add(up.scale(Math.sin(next)));
            int tint = shade(color, .72 + .28 * Math.cos(angle - .6));
            quad(p,v,a.add(ra.scale(startWidth)),a.add(rb.scale(startWidth)),
                    b.add(rb.scale(endWidth)),b.add(ra.scale(endWidth)),tint,alpha,
                    i/6.0*circumference,texStart,(i+1)/6.0*circumference,texEnd);
        }
    }

    public static void strand(PoseStack p, VertexConsumer v, Vec3 start, Vec3 end, double width,
            double bend, double phase, int color, float alpha) {
        Vec3 last = start;
        double textureDistance = phase*.015;
        for (int i = 1; i <= 12; i++) {
            double t = i / 12.0, previous = (i - 1) / 12.0;
            Vec3 next = VisceralGeometry.strandPoint(start,end,t,bend,phase);
            double a = width * (.95 - previous * .75), b = width * (.95 - t * .75);
            double nextDistance = textureDistance + last.distanceTo(next);
            segment(p,v,last,next,a,b,color,alpha,textureDistance,nextDistance);
            textureDistance = nextDistance;
            last = next;
        }
    }

    public static void drop(PoseStack p, VertexConsumer v, Vec3 at, double width, double height,
            int color, float alpha, double flow) {
        for (int row = 0; row < 8; row++) {
            double t0 = row / 8.0, t1 = (row + 1) / 8.0;
            double y0 = (t0 * 2 - 1) * height, y1 = (t1 * 2 - 1) * height;
            double w0 = Math.sin(Math.PI * t0) * width * (.65 + t0*.5);
            double w1 = Math.sin(Math.PI * t1) * width * (.65 + t1*.5);
            latheBand(p,v,y0,y1,w0,w1,12,color,alpha,flow+y0,flow+y1,at);
        }
    }

    public static void latheBand(PoseStack p, VertexConsumer v, double y0, double y1, double r0, double r1,
            int sides, int color, float alpha, double tex0, double tex1) {
        latheBand(p,v,y0,y1,r0,r1,sides,color,alpha,tex0,tex1,Vec3.ZERO);
    }

    private static void latheBand(PoseStack p, VertexConsumer v, double y0, double y1, double r0, double r1,
            int sides, int color, float alpha, double tex0, double tex1, Vec3 center) {
        double circumference = Math.PI*2*Math.max(r0,r1);
        for (int i = 0; i < sides; i++) {
            double a = i*Math.PI*2/sides, b=(i+1)*Math.PI*2/sides;
            quad(p,v,new Vec3(center.x+Math.cos(a)*r0,center.y+y0,center.z+Math.sin(a)*r0),
                    new Vec3(center.x+Math.cos(b)*r0,center.y+y0,center.z+Math.sin(b)*r0),
                    new Vec3(center.x+Math.cos(b)*r1,center.y+y1,center.z+Math.sin(b)*r1),
                    new Vec3(center.x+Math.cos(a)*r1,center.y+y1,center.z+Math.sin(a)*r1),
                    shade(color,.7+.3*Math.cos(a-.7)),alpha,
                    i/(double)sides*circumference,tex0,(i+1)/(double)sides*circumference,tex1);
        }
    }

    /** A thin lamella with curved shoulders and a real back/edge, also used for shard silhouettes. */
    public static void plate(PoseStack p, VertexConsumer v, double width, double height, double thickness,
            int color, float alpha, double flow) {
        for (int i = 0; i < 8; i++) {
            double t0=i/8.0,t1=(i+1)/8.0;
            double w0=width*(.3+.7*Math.sin(Math.PI*t0)),w1=width*(.3+.7*Math.sin(Math.PI*t1));
            double y0=(t0*2-1)*height,y1=(t1*2-1)*height;
            double z0=thickness+Math.sin(t0*Math.PI)*thickness,z1=thickness+Math.sin(t1*Math.PI)*thickness;
            for(int side : new int[]{-1,1}) {
                quad(p,v,new Vec3(-w0,y0,side*z0),new Vec3(w0,y0,side*z0),new Vec3(w1,y1,side*z1),
                        new Vec3(-w1,y1,side*z1),shade(color,side==1?1:.6),alpha,0,y0+flow,2*width,y1+flow);
                quad(p,v,new Vec3(side*w0,y0,-z0),new Vec3(side*w0,y0,z0),new Vec3(side*w1,y1,z1),
                        new Vec3(side*w1,y1,-z1),shade(color,.5),alpha,0,y0,2*thickness,y1);
            }
        }
    }

    public static void sword(PoseStack p, VertexConsumer v, float alpha, double time) {
        // Tapered blade with a narrow tip, parallel cutting edges and a raised dark fuller.
        double[] y={-.57,-.43,-.18,.15,.43,.62,.83};
        double[] width={.105,.145,.14,.125,.10,.065,0};
        for(int i=0;i<y.length-1;i++) for(int face : new int[]{-1,1}) {
            Vec3 ridge0=new Vec3(0,y[i],.045*face),ridge1=new Vec3(0,y[i+1],.045*face);
            for(int edge : new int[]{-1,1}) {
                quad(p,v,new Vec3(width[i]*edge,y[i],0),ridge0,ridge1,new Vec3(width[i+1]*edge,y[i+1],0),
                        edge==1?0xD32943:0x871426,alpha,edge==1?0:.145,y[i]+time*.007,
                        edge==1?.145:.29,y[i+1]+time*.007);
            }
        }
        for(int face : new int[]{-1,1})
            strand(p,v,new Vec3(0,-.48,.048*face),new Vec3(0,.65,.048*face),.012,.018,time*.025,0x470913,alpha);
        for(int side : new int[]{-1,1}) {
            strand(p,v,new Vec3(0,-.56,0),new Vec3(side*.32,-.40,0),.055,.10,side,0x94172B,alpha);
        }
        tube(p,v,new Vec3(0,-.57,0),new Vec3(0,-.90,0),.042,0x66101E,alpha);
        for(int i=0;i<4;i++) tube(p,v,new Vec3(-.045,-.6-i*.065,.03),new Vec3(.045,-.64-i*.065,.03),
                .012,0xD43649,alpha);
        drop(p,v,new Vec3(0,-.94,0),.07,.09,0xA31B30,alpha,time*.007);
    }

    public static void shard(PoseStack p, VertexConsumer v, double size, int variant, float alpha) {
        double skew=(variant%3-1)*.18;
        Vec3[] rim={new Vec3(-.55*size,-.7*size,0),new Vec3(.24*size,-size,0),
                new Vec3(.6*size,.12*size,0),new Vec3(skew*size,1.25*size,0),new Vec3(-.4*size,.45*size,0)};
        for(int i=0;i<rim.length;i++) {
            Vec3 a=rim[i],b=rim[(i+1)%rim.length];
            for(int face:new int[]{-1,1}) {
                Vec3 ridge=new Vec3(skew*size,.15*size,face*.026);
                quad(p,v,a,b,ridge,ridge,i%2==0?0xFFE3C3:0xF8AB94,alpha*.7F,0,0,1,1);
            }
            segment(p,v,a,b,.005,.004,0xFFF3D5,alpha,0,1);
        }
        strand(p,v,rim[0].add(0,0,.025),rim[3].add(0,0,.025),.009,size*.14,variant,0xFFCD8D,alpha);
    }

    public static void boundary(PoseStack p, VertexConsumer v, double radius, int color, float alpha, double time) {
        if(UndulatingVertexConsumer.omitsBoundary(v))return;
        v=UndulatingVertexConsumer.fixed(v);
        // The central trace keeps the server's square footprint exact; fine branches grow inward.
        for(int side : new int[]{-1,1}) for(int axis=0;axis<2;axis++) {
            for(int i=0;i<12;i++) {
                double a=-radius+2*radius*i/12,b=-radius+2*radius*(i+1)/12;
                Vec3 start=axis==0?new Vec3(a,.045,side*radius):new Vec3(side*radius,.045,a);
                Vec3 end=axis==0?new Vec3(b,.045,side*radius):new Vec3(side*radius,.045,b);
                segment(p,v,start,end,.018,.018,color,alpha,i/12.0+time*.002,(i+1)/12.0+time*.002);
                if(i%3==1) {
                    Vec3 inward=axis==0?new Vec3(.12,.008,-side*.28):new Vec3(-side*.28,.008,.12);
                    strand(p,v,start,start.add(inward),.012,.06,i,color,alpha*.6F);
                }
            }
        }
    }

    public static void locatorBox(PoseStack p, VertexConsumer v, double r, int color, float alpha) {
        // Dowsing can show 24 blocks at once; each locator needs only the twelve box edges.
        for(int sign:new int[]{-1,1})for(int height:new int[]{-1,1}) {
            tube(p,v,new Vec3(-r,height*r,sign*r),new Vec3(r,height*r,sign*r),.012,color,alpha);
            tube(p,v,new Vec3(sign*r,height*r,-r),new Vec3(sign*r,height*r,r),.012,color,alpha);
        }
        for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})
            tube(p,v,new Vec3(x*r,-r,z*r),new Vec3(x*r,r,z*r),.012,color,alpha);
    }
}
