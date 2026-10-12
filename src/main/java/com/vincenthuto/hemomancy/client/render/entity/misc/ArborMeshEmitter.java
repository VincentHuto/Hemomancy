package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.worldgen.arbor.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pure mesh emission for the Arbor's cached layers. Closed solids (tubes, caps, ellipsoids) wind
 * counter-clockwise when seen from outside so their layers can be back-face culled; only the flat
 * leaf blades go to the double-sided {@link MeshConsumers#leaves()} layer.
 */
final class ArborMeshEmitter {
    static final int BARK_TINT = 0xFF9B4E4B;
    static final int DARK_BARK_TINT = 0xFF572B32;
    static final Map<String, Integer> FAMILY_COLORS = Map.of(
            "core", 0xFFDA2636, "living_staff", 0xFFE3A62F, "mycelial", 0xFF4FAE68,
            "scars", 0xFFC8D1DC, "covenant", 0xFFD64D75, "summons", 0xFF376FC7);
    private static final double TRUNK_SPAN = ArborSkeleton.TRUNK_SPAN;
    private static final double STREAMER_LENGTH = 1.3;
    private static final double STREAMER_FLOOR_CLEARANCE = .9;
    // Matches the former three taper points after three-way smoothing.
    private static final int TIP_SAMPLES = 9;

    private ArborMeshEmitter() { }

    record MeshConsumers(VertexConsumer bark, VertexConsumer mycelium, VertexConsumer leaves,
            VertexConsumer fungus) { }

    static void emitStaticTree(ArborStaticVisualKey key, List<ArborOfWillLayout.FruitPlacement> placements,
            MeshConsumers consumers) {
        PoseStack.Pose pose = new PoseStack().last();
        float height = (float) ArborOfWillVisualRules.treeHeight(key.degree());
        float rootRadius = (float) ArborOfWillVisualRules.rootRadius(key.degree(), key.chamberRadius());
        int whorls = Math.max(1, ArborOfWillVisualRules.visibleWhorls(key.degree()));
        float foliage = key.foliage();
        float trunkHeight = ArborBarkMeshes.trunkHeight(key.degree());
        renderRootsAndHeartwood(pose, consumers, key.packedLight(), trunkHeight, rootRadius, foliage, key.degree(), key.pomes());
        renderBoughs(pose, consumers, key.packedLight(), placements, whorls, foliage, key.degree());
        renderFruitStems(pose, consumers, key.packedLight(), placements, whorls, key.degree());
        if (key.degree() >= 8) {
            ArborApotheosisMeshes.emitCrown(pose, consumers, key.packedLight(), key.degree());
        }
    }

    private static void renderRootsAndHeartwood(PoseStack.Pose pose, MeshConsumers consumers, int light,
            float height, float rootRadius, float foliage, int degree, int pomes) {
        for (int i = 0; i < 7; i++) {
            texturedTerminalTube(pose, consumers.bark(), light,
                    ArborCanopyGeometry.root(i, rootRadius), .34F, .035F, DARK_BARK_TINT, 1.8F,.42F);
        }

        ArborBarkMeshes.emitTrunk(pose, consumers, light, height, degree, pomes);
        double top = height * TRUNK_SPAN;
        List<ArborCanopyGeometry.Point> trunk = List.of(ArborBarkMeshes.centre(top * .85, top), ArborBarkMeshes.centre(top, top));
        float trunkCrownSize=.82F+foliage*.72F;
        if (foliage > .06F) {
            renderTerminalCrown(pose,consumers,light,trunk,FAMILY_COLORS.get("core"),
                    trunkCrownSize,foliage,90);
        }
        // Two short leaders split the crown into the heart-shaped aperture described in the design. They stay
        // level with the bough crests so nothing of the trunk rises above where the boughs spring.
        double leaderBase = ArborSkeleton.trunkTop(degree);
        double scale = ArborSkeleton.crownScale(degree);
        for (int side : new int[]{-1, 1}) {
            List<ArborCanopyGeometry.Point> leader = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                double t = i / 6.0;
                leader.add(new ArborCanopyGeometry.Point(side * (.06 + .62 * Math.sin(t * Math.PI * .5)),
                        leaderBase - .5 * scale + 1.05 * scale * Math.sin(t * Math.PI * .8), .03 + .12 * Math.sin(t * Math.PI)));
            }
            float leaderCrownSize=.68F+foliage*.48F;
            texturedTerminalTube(pose, consumers.bark(), light,
                    leader, .16F, .045F, BARK_TINT, 2.2F,leaderCrownSize*.18F);
            if (foliage > .06F) {
                renderTerminalCrown(pose,consumers,light,leader,FAMILY_COLORS.get("core"),
                        leaderCrownSize,foliage,96+side);
            }
        }
    }

    /** A willow streamer: a thin pendant twig hanging from a bough with leaf blades trailing down it. */
    private static void renderStreamer(PoseStack.Pose pose, MeshConsumers consumers, int light,
            ArborCanopyGeometry.Point top, int familyColor, float foliage, int seed) {
        double length = Math.min(STREAMER_LENGTH, top.y() - STREAMER_FLOOR_CLEARANCE);
        if (length < .3) return;
        double radial = Math.max(1.0e-4, top.radialDistance());
        double ox = top.x() / radial, oz = top.z() / radial;
        List<ArborCanopyGeometry.Point> twig = List.of(top,
                top.add(new ArborCanopyGeometry.Point(ox * .05, -length * .5, oz * .05)),
                top.add(new ArborCanopyGeometry.Point(ox * .10, -length, oz * .10)));
        texturedTubeProfiled(pose, consumers.bark(), light, twig, .016F, .006F, .006F, DARK_BARK_TINT, .8F, 1.0F,
                ArborRenderBudget.STREAMER_TUBE_SIDES, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        int blades = 2 + Math.round(foliage * 5);
        int leafLight = LightTexture.pack(11, 8);
        for (int k = 0; k < blades; k++) {
            double u = (k + 1) / (double) (blades + 1);
            double side = (k + seed) % 2 == 0 ? 1 : -1;
            ArborCanopyGeometry.Point at = top.add(new ArborCanopyGeometry.Point(ox * .1 * u, -length * u, oz * .1 * u));
            ArborCanopyGeometry.Point direction = new ArborCanopyGeometry.Point(
                    ox * .3 - oz * .6 * side, -1, oz * .3 + ox * .6 * side).normalized();
            int tint = mix(k % 2 == 0 ? 0xFF9A2845 : 0xFF642638, familyColor, .28F);
            texturedLeafBlade(pose, consumers.leaves(), leafLight,
                    new ArborCanopyGeometry.Leaflet(at, direction, .24, .045, 0, 0, .03), tint);
        }
    }

    /** One bough per family out to its highest earned station, with a fruiting spur at each occupied station. */
    private static void renderBoughs(PoseStack.Pose pose, MeshConsumers consumers, int light,
            List<ArborOfWillLayout.FruitPlacement> placements, int whorls, float foliage, int degree) {
        boolean[][] occupied = new boolean[ArborSkeleton.FAMILY_COUNT][ArborSkeleton.MAX_WHORL + 1];
        for (ArborOfWillLayout.FruitPlacement fruit : placements) {
            if (fruit.whorl() <= whorls) occupied[familyIndex(fruit.family())][fruit.whorl()] = true;
        }
        for (int family = 0; family < ArborSkeleton.FAMILY_COUNT; family++) {
            int familyColor = FAMILY_COLORS.get(ArborOfWillLayout.orderedFamilies().get(family));
            int woodTint = mix(BARK_TINT, familyColor, .18F);
            List<ArborCanopyGeometry.Point> bough = ArborSkeleton.bough(family, whorls, degree);
            float boughCrownSize = .70F + foliage * .54F;
            texturedTerminalTube(pose, consumers.bark(), light, bough, .20F, .05F, woodTint, 3.1F,
                    boughCrownSize * .18F);
            if (foliage > .06F) {
                renderTerminalCrown(pose, consumers, light, bough, familyColor, boughCrownSize, foliage, family * 37);
            }
            List<ArborCanopyGeometry.Point> streamers = ArborSkeleton.streamerAnchors(family, whorls, degree);
            for (int k = 0; k < streamers.size(); k++) {
                renderStreamer(pose, consumers, light, streamers.get(k).add(new ArborCanopyGeometry.Point(0, -.03, 0)),
                        familyColor, foliage, family * 13 + k);
            }
            for (int whorl = 1; whorl <= whorls; whorl++) {
                if (!occupied[family][whorl]) continue;
                ArborCanopyGeometry.Point station = ArborSkeleton.station(family, whorl, degree);
                ArborCanopyGeometry.Point tip = ArborSkeleton.spurTip(family, whorl, degree);
                ArborCanopyGeometry.Point middle = new ArborCanopyGeometry.Point((station.x() + tip.x()) * .5,
                        (station.y() + tip.y()) * .5 + .07, (station.z() + tip.z()) * .5);
                texturedTubeProfiled(pose, consumers.bark(), light, List.of(station, middle, tip),
                        .055F, .03F, .03F, mix(BARK_TINT, familyColor, .24F), 1.0F, 1.0F,
                        ArborRenderBudget.TWIG_TUBE_SIDES, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
                if (foliage > .24F) {
                    renderLeafCluster(pose, consumers, light, middle.add(new ArborCanopyGeometry.Point(0, .2, 0)),
                            familyColor, .42F + .2F * foliage, foliage, family * 11 + whorl * 3);
                }
            }
        }
    }

    private static void renderFruitStems(PoseStack.Pose pose, MeshConsumers consumers, int light,
            List<ArborOfWillLayout.FruitPlacement> placements, int whorls, int degree) {
        for (ArborOfWillLayout.FruitPlacement fruit : placements) {
            if (fruit.whorl() > whorls) continue;
            int color = FAMILY_COLORS.getOrDefault(fruit.family(), FAMILY_COLORS.get("core"));
            List<ArborCanopyGeometry.Point> stem = ArborSkeleton.fruitStem(
                    ArborSkeleton.spurTip(familyIndex(fruit.family()), fruit.whorl(), degree),
                    new ArborCanopyGeometry.Point(fruit.x(), fruit.y(), fruit.z()));
            texturedTubeProfiled(pose, consumers.bark(), light, stem, .024F, .012F, .012F,
                    mix(color, 0xFF6B452B, .38F), .75F, 1.0F,
                    ArborRenderBudget.STEM_TUBE_SIDES, ArborRenderBudget.MINOR_TUBE_SUBDIVISIONS);
        }
    }

    private static int familyIndex(String family) {
        return Math.max(0, ArborOfWillLayout.orderedFamilies().indexOf(family));
    }

    private static void renderLeafCluster(PoseStack.Pose pose, MeshConsumers consumers, int light,
            ArborCanopyGeometry.Point p, int familyColor, float size, float foliage, int seed) {
        int leafLight=LightTexture.pack(11,8);
        float bud=ArborCanopyGeometry.foliageBudRadius(size,foliage);
        int petalColor=mix(0xFF642638,familyColor,.24F);
        int darkPetalColor=mix(petalColor,0xFF160A12,.30F);
        minorEllipsoid(pose,consumers.mycelium(),light,p.x(),p.y(),p.z(),bud,bud*.90F,bud,darkPetalColor);
        List<ArborCanopyGeometry.Leaflet> crown=ArborCanopyGeometry.foliageCrown(p,size,foliage,seed);
        for(int i=0;i<crown.size();i++) {
            ArborCanopyGeometry.Leaflet leaf=crown.get(i);
            int tint=mix(i%3==0?0xFF9A2845:i%3==1?0xFF642638:0xFF3F202C,familyColor,.24F);
            texturedLeafBlade(pose,consumers.leaves(),leafLight,leaf,tint);
        }
    }

    private static void renderTerminalCrown(PoseStack.Pose pose, MeshConsumers consumers, int light,
            List<ArborCanopyGeometry.Point> path, int familyColor, float size, float foliage, int seed) {
        ArborCanopyGeometry.Point center=ArborCanopyGeometry.terminalFoliageCenter(path,size*.18F);
        renderLeafCluster(pose,consumers,light,center,familyColor,size,foliage,seed);
    }

    private static void texturedTerminalTube(PoseStack.Pose pose,VertexConsumer out,int light,
            List<ArborCanopyGeometry.Point> path,float startRadius,float endRadius,int color,float vScale,
            float attachmentDistance) {
        // Smooth before extending the tip: Catmull-Rom across the short taper spacing overshoots and
        // folds the branch back on itself where the authored path meets the taper.
        List<ArborCanopyGeometry.Point> smoothed=ArborCanopyGeometry.smooth(path,ArborRenderBudget.MAJOR_TUBE_SUBDIVISIONS);
        List<ArborCanopyGeometry.Point> tapered=new ArrayList<>(smoothed);
        if(smoothed.size()>=2 && attachmentDistance>0) {
            ArborCanopyGeometry.Point end=smoothed.get(smoothed.size()-1);
            ArborCanopyGeometry.Point tangent=end.subtract(smoothed.get(smoothed.size()-2)).normalized();
            for(int i=1;i<=TIP_SAMPLES;i++) tapered.add(end.add(tangent.scale(attachmentDistance*i/TIP_SAMPLES)));
        }
        float taperStart=(smoothed.size()-1)/(float)Math.max(1,tapered.size()-1);
        texturedTubeProfiled(pose,out,light,tapered,startRadius,endRadius,0F,color,vScale,taperStart,
                ArborRenderBudget.MAJOR_TUBE_SIDES,1);
    }

    static void texturedTubeProfiled(PoseStack.Pose pose,VertexConsumer out,int light,
            List<ArborCanopyGeometry.Point> path,float startRadius,float branchEndRadius,float tipRadius,
            int color,float vScale,float taperStart,int sides,int subdivisions) {
        if(path.size()<2)return;
        path=ArborCanopyGeometry.smooth(path,subdivisions);
        double[][] n1=new double[path.size()][3],n2=new double[path.size()][3],tangents=new double[path.size()][3];
        for(int i=0;i<path.size();i++) {
            ArborCanopyGeometry.Point before=path.get(Math.max(0,i-1));
            ArborCanopyGeometry.Point after=path.get(Math.min(path.size()-1,i+1));
            double tx=after.x()-before.x(),ty=after.y()-before.y(),tz=after.z()-before.z();
            double len=Math.sqrt(tx*tx+ty*ty+tz*tz); if(len<1.0e-5){tx=0;ty=1;tz=0;len=1;}
            tx/=len;ty/=len;tz/=len;
            tangents[i][0]=tx;tangents[i][1]=ty;tangents[i][2]=tz;
            // Parallel-transport the previous ring frame so rings never twist between samples; a fixed
            // reference axis snaps the frame near vertical and folds thin segments into bowtie quads.
            double px,py,pz;
            if(i>0) {
                double along=n1[i-1][0]*tx+n1[i-1][1]*ty+n1[i-1][2]*tz;
                px=n1[i-1][0]-tx*along;py=n1[i-1][1]-ty*along;pz=n1[i-1][2]-tz*along;
            } else {
                px=py=pz=0;
            }
            double pl=Math.sqrt(px*px+py*py+pz*pz);
            if(pl<1.0e-4) {
                double rx=Math.abs(ty)>.92?1:0,ry=Math.abs(ty)>.92?0:1;
                px=-tz*ry;py=tz*rx;pz=tx*ry-ty*rx;
                pl=Math.sqrt(px*px+py*py+pz*pz);
            }
            n1[i][0]=px/pl;n1[i][1]=py/pl;n1[i][2]=pz/pl;
            n2[i][0]=ty*n1[i][2]-tz*n1[i][1];
            n2[i][1]=tz*n1[i][0]-tx*n1[i][2];
            n2[i][2]=tx*n1[i][1]-ty*n1[i][0];
        }
        float[] radii=new float[path.size()];
        for(int i=0;i<path.size();i++) {
            radii[i]=ArborCanopyGeometry.pointedRadius(i/(float)(path.size()-1),taperStart,startRadius,branchEndRadius);
        }
        // Neighbouring rings cross on the inside of a bend once they outgrow the bend radius; keep them
        // just inside that radius so tight hooks pinch instead of turning inside out.
        for(int segment=0;segment<path.size()-1;segment++) {
            double[] ta=tangents[segment],tb=tangents[segment+1];
            double turn=Math.acos(Math.max(-1,Math.min(1,ta[0]*tb[0]+ta[1]*tb[1]+ta[2]*tb[2])));
            if(turn<1.0e-6) continue;
            float limit=(float)(.95*.5*path.get(segment).distanceTo(path.get(segment+1))/Math.tan(Math.min(turn,3.1)*.5));
            radii[segment]=Math.min(radii[segment],limit);
            radii[segment+1]=Math.min(radii[segment+1],limit);
        }
        for(int segment=0;segment<path.size()-1;segment++) {
            ArborCanopyGeometry.Point a=path.get(segment),b=path.get(segment+1);
            float t0=segment/(float)(path.size()-1),t1=(segment+1)/(float)(path.size()-1);
            float r0=radii[segment],r1=radii[segment+1];
            for(int side=0;side<sides;side++) {
                double q0=side*Math.PI*2/sides,q1=(side+1)*Math.PI*2/sides;
                double c0=Math.cos(q0),s0=Math.sin(q0),c1=Math.cos(q1),s1=Math.sin(q1);
                tubeVertex(pose,out,light,a,r0,n1[segment],n2[segment],c0,s0,side/(float)sides,t0*vScale,color);
                tubeVertex(pose,out,light,a,r0,n1[segment],n2[segment],c1,s1,(side+1)/(float)sides,t0*vScale,color);
                tubeVertex(pose,out,light,b,r1,n1[segment+1],n2[segment+1],c1,s1,(side+1)/(float)sides,t1*vScale,color);
                tubeVertex(pose,out,light,b,r1,n1[segment+1],n2[segment+1],c0,s0,side/(float)sides,t1*vScale,color);
            }
        }
        ArborCanopyGeometry.Point startTangent=path.get(0).subtract(path.get(1));
        ArborCanopyGeometry.Point endTangent=path.get(path.size()-1).subtract(path.get(path.size()-2));
        tubeCap(pose,out,light,path.get(0),startTangent,radii[0],sides,color);
        if(tipRadius>1.0e-5F) {
            tubeCap(pose,out,light,path.get(path.size()-1),endTangent,Math.min(tipRadius,radii[path.size()-1]),sides,color);
        }
    }

    private static void tubeCap(PoseStack.Pose pose,VertexConsumer out,int light,
            ArborCanopyGeometry.Point center,ArborCanopyGeometry.Point tangent,float radius,int sides,int color) {
        List<ArborCanopyGeometry.Point> ring=ArborCanopyGeometry.capRing(center,tangent,radius,sides);
        ArborCanopyGeometry.Point normal=tangent.normalized();
        for(int side=0;side<ring.size();side++) {
            int next=(side+1)%ring.size();
            double a0=side*Math.PI*2.0/ring.size(),a1=next*Math.PI*2.0/ring.size();
            texturedVertex(pose,out,light,center.x(),center.y(),center.z(),.5F,.5F,
                    normal.x(),normal.y(),normal.z(),color);
            texturedVertex(pose,out,light,ring.get(side).x(),ring.get(side).y(),ring.get(side).z(),
                    (float)(.5+.5*Math.cos(a0)),(float)(.5+.5*Math.sin(a0)),normal.x(),normal.y(),normal.z(),color);
            texturedVertex(pose,out,light,ring.get(next).x(),ring.get(next).y(),ring.get(next).z(),
                    (float)(.5+.5*Math.cos(a1)),(float)(.5+.5*Math.sin(a1)),normal.x(),normal.y(),normal.z(),color);
            texturedVertex(pose,out,light,center.x(),center.y(),center.z(),.5F,.5F,
                    normal.x(),normal.y(),normal.z(),color);
        }
    }

    private static void tubeVertex(PoseStack.Pose pose,VertexConsumer out,int light,ArborCanopyGeometry.Point p,
            float radius,double[] n1,double[] n2,
            double cosine,double sine,float u,float v,int color) {
        double nx=n1[0]*cosine+n2[0]*sine,ny=n1[1]*cosine+n2[1]*sine,nz=n1[2]*cosine+n2[2]*sine;
        texturedVertex(pose,out,light,p.x()+nx*radius,p.y()+ny*radius,p.z()+nz*radius,u,v,nx,ny,nz,color);
    }

    private static void fruitEllipsoid(PoseStack.Pose pose, VertexConsumer out, int light,
            double cx,double cy,double cz,double rx,double ry,double rz,int color) {
        texturedEllipsoid(pose,out,light,cx,cy,cz,rx,ry,rz,color,
                ArborRenderBudget.FOCAL_ELLIPSOID_LATITUDES,ArborRenderBudget.FOCAL_ELLIPSOID_LONGITUDES);
    }

    static void minorEllipsoid(PoseStack.Pose pose, VertexConsumer out, int light,
            double cx,double cy,double cz,double rx,double ry,double rz,int color) {
        texturedEllipsoid(pose,out,light,cx,cy,cz,rx,ry,rz,color,
                ArborRenderBudget.MINOR_ELLIPSOID_LATITUDES,ArborRenderBudget.MINOR_ELLIPSOID_LONGITUDES);
    }

    private static void texturedEllipsoid(PoseStack.Pose pose, VertexConsumer out, int light,
            double cx,double cy,double cz,double rx,double ry,double rz,int color,int latitudes,int longitudes) {
        for(int lat=0;lat<latitudes;lat++) {
            double t0=Math.PI*lat/latitudes,t1=Math.PI*(lat+1)/latitudes;
            for(int lon=0;lon<longitudes;lon++) {
                double p0=Math.PI*2*lon/longitudes,p1=Math.PI*2*(lon+1)/longitudes;
                ellipsoidVertex(pose,out,light,cx,cy,cz,rx,ry,rz,t0,p0,lon/(float)longitudes,lat/(float)latitudes,color);
                ellipsoidVertex(pose,out,light,cx,cy,cz,rx,ry,rz,t0,p1,(lon+1)/(float)longitudes,lat/(float)latitudes,color);
                ellipsoidVertex(pose,out,light,cx,cy,cz,rx,ry,rz,t1,p1,(lon+1)/(float)longitudes,(lat+1)/(float)latitudes,color);
                ellipsoidVertex(pose,out,light,cx,cy,cz,rx,ry,rz,t1,p0,lon/(float)longitudes,(lat+1)/(float)latitudes,color);
            }
        }
    }

    private static void texturedLeafBlade(PoseStack.Pose pose,VertexConsumer out,int light,
            ArborCanopyGeometry.Leaflet leaf,int color) {
        ArborCanopyGeometry.Point direction=leaf.direction().normalized();
        ArborCanopyGeometry.Point reference=Math.abs(direction.y())>.88
                ?new ArborCanopyGeometry.Point(1,0,0):new ArborCanopyGeometry.Point(0,1,0);
        ArborCanopyGeometry.Point side=direction.cross(reference).normalized();
        ArborCanopyGeometry.Point crown=side.cross(direction).normalized();
        for(int plane=0;plane<ArborRenderBudget.LEAF_RIBBON_PLANES;plane++) {
            double angle=plane*Math.PI/2.0;
            ArborCanopyGeometry.Point lateral=side.scale(Math.cos(angle)).add(crown.scale(Math.sin(angle))).normalized();
            ArborCanopyGeometry.Point normal=direction.cross(lateral).normalized();
            for(int segment=0;segment<ArborRenderBudget.LEAF_RIBBON_SEGMENTS;segment++) {
                double t0=segment/(double)ArborRenderBudget.LEAF_RIBBON_SEGMENTS;
                double t1=(segment+1)/(double)ArborRenderBudget.LEAF_RIBBON_SEGMENTS;
                leafRibbonVertex(pose,out,light,leaf,direction,crown,lateral,normal,t0,-1,
                        (float)t0,0,color);
                leafRibbonVertex(pose,out,light,leaf,direction,crown,lateral,normal,t1,-1,
                        (float)t1,0,color);
                leafRibbonVertex(pose,out,light,leaf,direction,crown,lateral,normal,t1,1,
                        (float)t1,1,color);
                leafRibbonVertex(pose,out,light,leaf,direction,crown,lateral,normal,t0,1,
                        (float)t0,1,color);
            }
        }
    }

    private static void leafRibbonVertex(PoseStack.Pose pose,VertexConsumer out,int light,
            ArborCanopyGeometry.Leaflet leaf,ArborCanopyGeometry.Point direction,
            ArborCanopyGeometry.Point crown,ArborCanopyGeometry.Point lateral,
            ArborCanopyGeometry.Point normal,double t,double sideSign,
            float u,float v,int color) {
        double taper=Math.pow(Math.sin(Math.PI*t),.72);
        double bend=Math.sin(Math.PI*t)*leaf.curl();
        ArborCanopyGeometry.Point center=leaf.center().add(direction.scale(leaf.length()*t)).add(crown.scale(bend));
        ArborCanopyGeometry.Point point=center.add(lateral.scale(sideSign*leaf.width()*taper));
        texturedVertex(pose,out,light,point.x(),point.y(),point.z(),u,v,
                normal.x(),normal.y(),normal.z(),color);
    }

    private static void ellipsoidVertex(PoseStack.Pose pose,VertexConsumer out,int light,double cx,double cy,double cz,
            double rx,double ry,double rz,double theta,double phi,float u,float v,int color) {
        double nx=Math.sin(theta)*Math.cos(phi),ny=Math.cos(theta),nz=Math.sin(theta)*Math.sin(phi);
        texturedVertex(pose,out,light,cx+nx*rx,cy+ny*ry,cz+nz*rz,u,v,nx,ny,nz,color);
    }

    static void texturedVertex(PoseStack.Pose pose,VertexConsumer out,int light,double x,double y,double z,
            float u,float v,double nx,double ny,double nz,int color) {
        Matrix4f matrix=pose.pose();Matrix3f normals=pose.normal();
        Vector3f normal=new Vector3f((float)nx,(float)ny,(float)nz).mul(normals).normalize();
        out.addVertex(matrix,(float)x,(float)y,(float)z).setColor(argbR(color),argbG(color),argbB(color),argbA(color))
                .setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(normal.x(),normal.y(),normal.z());
    }

    static int mix(int a,int b,float amount) {
        amount=Math.max(0,Math.min(1,amount));
        return ((int)(argbA(a)+(argbA(b)-argbA(a))*amount)<<24)|((int)(argbR(a)+(argbR(b)-argbR(a))*amount)<<16)
                |((int)(argbG(a)+(argbG(b)-argbG(a))*amount)<<8)|(int)(argbB(a)+(argbB(b)-argbB(a))*amount);
    }
    static int argbA(int c){return c>>>24&255;} static int argbR(int c){return c>>>16&255;}
    static int argbG(int c){return c>>>8&255;} static int argbB(int c){return c&255;}
}
