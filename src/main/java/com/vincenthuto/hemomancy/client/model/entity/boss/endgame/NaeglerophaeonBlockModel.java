package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Renders the authored Blockbench cubes, UVs and bone hierarchy without generating a smooth mesh. */
public final class NaeglerophaeonBlockModel {
    private static final String MODEL="/assets/hemomancy/entity_geometry/naeglerophaeon_cube_geometry.json";
    private static final float INV_ATLAS=1F/128F;
    private static final float PIXEL=1F/16F;
    private static final String[] FACE_NAMES={"north","south","west","east","up","down"};
    private static final int[][] FACE_CORNERS={
            {0,2,3,1},{4,5,7,6},{0,4,6,2},{1,3,7,5},{2,6,7,3},{0,1,5,4}};
    private static final float[][] NORMALS={
            {0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0},{0,-1,0}};
    private static final Group ROOT=load();

    private record Cube(String name,float[] from,float[] to,float[][] uv,int material) {}
    private record Group(String name,float[] origin,float[] rotation,List<Object> children) {}

    private NaeglerophaeonBlockModel() {}

    private static Group load() {
        try(var stream=NaeglerophaeonBlockModel.class.getResourceAsStream(MODEL)) {
            if(stream==null) throw new IllegalStateException("Missing Naeglerophaeon Blockbench model: "+MODEL);
            JsonObject model=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String,Cube> cubes=new HashMap<>();
            for(JsonElement entry:model.getAsJsonArray("elements")) {
                JsonObject cube=entry.getAsJsonObject();
                float[][] uv=new float[6][];
                JsonObject faces=cube.getAsJsonObject("faces");
                for(int i=0;i<6;i++) uv[i]=floats(faces.getAsJsonObject(FACE_NAMES[i]).getAsJsonArray("uv"));
                int tile=(int)(uv[0][0]/32)+(int)(uv[0][1]/32)*4;
                cubes.put(cube.get("uuid").getAsString(),new Cube(cube.get("name").getAsString(),floats(cube.getAsJsonArray("from")),
                        floats(cube.getAsJsonArray("to")),uv,tile));
            }
            return group(model.getAsJsonArray("outliner").get(0).getAsJsonObject(),cubes);
        } catch(Exception e) {
            throw new IllegalStateException("Cannot load Naeglerophaeon Blockbench model",e);
        }
    }

    private static Group group(JsonObject json,Map<String,Cube> cubes) {
        List<Object> children=new ArrayList<>();
        for(JsonElement entry:json.getAsJsonArray("children")) {
            if(entry.isJsonObject()) children.add(group(entry.getAsJsonObject(),cubes));
            else {
                Cube cube=cubes.get(entry.getAsString());
                if(cube==null) throw new IllegalStateException("Unknown Blockbench cube "+entry.getAsString());
                children.add(cube);
            }
        }
        return new Group(json.get("name").getAsString(),vector(json,"origin"),vector(json,"rotation"),children);
    }

    private static float[] vector(JsonObject json,String key) {
        return json.has(key) ? floats(json.getAsJsonArray(key)) : new float[]{0,0,0};
    }

    private static float[] floats(JsonArray array) {
        float[] out=new float[array.size()];
        for(int i=0;i<out.length;i++) out[i]=array.get(i).getAsFloat();
        return out;
    }

    /** Passes: 0 opaque tissue, 1 luminous nodes, 2 membrane, 3 marked core eyes. */
    public static void render(PoseStack stack,VertexConsumer consumer,int pass,
            NaeglerophaeonModel.Input input,int light,int overlay,boolean dynamicTail,
            Vec3 lookDirection,float lookWeight,boolean markEyes) {
        stack.pushPose();
        orientBody(stack,input);
        renderGroup(ROOT,stack,consumer,pass,input,light,overlay,-1,-1,dynamicTail,null,null,
                headRotation(input,lookDirection,lookWeight),false,markEyes);
        stack.popPose();
    }

    private static Quaternionf headRotation(NaeglerophaeonModel.Input input,Vec3 direction,float weight) {
        if(direction==null || direction.lengthSqr()<1.0E-8 || weight<=0) return null;
        // The orb cluster faces -Z in the authored model; undo body heading before turning that axis.
        Quaternionf body=new Quaternionf().rotationY((float)-input.yaw())
                .rotateX((float)input.pitch()).rotateY((float)Math.PI);
        Vector3f local=new Vector3f((float)direction.x,(float)direction.y,(float)direction.z);
        body.conjugate().transform(local);
        float yaw=(float)Math.atan2(-local.x,-local.z);
        float pitch=(float)Math.atan2(local.y,Math.hypot(local.x,local.z));
        return new Quaternionf().slerp(new Quaternionf().rotationY(yaw).rotateX(pitch),
                Mth.clamp(weight,0,1));
    }

    public static Vec3[] tips(NaeglerophaeonModel.Input input) {
        Vec3[] tips=new Vec3[12];
        PoseStack stack=new PoseStack();
        orientBody(stack,input);
        renderGroup(ROOT,stack,null,-1,input,0,0,-1,-1,false,tips,null,null,false,false);
        return tips;
    }

    public static Vec3[] tailPoints(NaeglerophaeonModel.Input input) {
        Vec3[] points=new Vec3[49];
        PoseStack stack=new PoseStack();
        orientBody(stack,input);
        renderGroup(ROOT,stack,null,-1,input,0,0,-1,-1,false,null,points,null,false,false);
        points[48]=points[47].add(points[47].subtract(points[46]));
        return points;
    }

    private static void orientBody(PoseStack stack,NaeglerophaeonModel.Input input) {
        stack.translate(0,.8,0);
        stack.mulPose(new Quaternionf().rotationY((float)-input.yaw()));
        stack.mulPose(new Quaternionf().rotationX((float)input.pitch()));
        stack.mulPose(new Quaternionf().rotationY((float)Math.PI));
        int phase=input.phase();
        double swim=phase==NaeglerophaeonEntity.IDLE || phase==NaeglerophaeonEntity.VOLLEY?input.hunting():0;
        stack.translate(Math.sin(input.time()*.26)*.12*swim,Math.sin(input.time()*.52)*.035*swim,0);
        stack.mulPose(new Quaternionf().rotationY((float)(Math.sin(input.time()*.26+.6)*.10*swim)));
        double shake=switch(phase) {
            case NaeglerophaeonEntity.TRANSITION -> .09;
            case NaeglerophaeonEntity.OVERLOAD -> .05;
            case NaeglerophaeonEntity.NOVA -> input.phaseTicks()<NOVA_WINDUP?.05*input.phaseTicks()/NOVA_WINDUP:0;
            default -> 0;
        };
        if(shake>0) stack.translate(Math.sin(input.time()*2.9)*shake,Math.sin(input.time()*3.7+1)*shake,Math.sin(input.time()*3.1+2)*shake);
        if(phase==NaeglerophaeonEntity.LASH && input.phaseTicks()>=LASH_STRIKE)
            stack.mulPose(new Quaternionf().rotationZ((float)(smooth((float)((input.phaseTicks()-LASH_STRIKE)/6))*Math.PI*2)));
        float size=bodyScale(input);
        if(size!=1) stack.scale(size,size,size);
    }

    /** Collapses into a synaptic node on a dive and unfolds again on emergence. */
    public static float bodyScale(NaeglerophaeonModel.Input input) {
        if(input.phase()==NaeglerophaeonEntity.NERVE_DIVE) return 1-.85F*smooth((float)(input.phaseTicks()/12));
        if(input.phase()==NaeglerophaeonEntity.NERVE_EMERGE) return .15F+.85F*smooth((float)(input.phaseTicks()/6));
        return 1;
    }

    /** Core orb swell; drained orbs sit slightly shrunken. */
    static float coreCharge(NaeglerophaeonModel.Input input) {
        double t=input.phaseTicks();
        return switch(input.phase()) {
            case NaeglerophaeonEntity.GRAB -> (float)Math.min(1,t/100);
            case NaeglerophaeonEntity.CHARGE, NaeglerophaeonEntity.CAPTURE -> (float)Math.min(1,t/20);
            case NaeglerophaeonEntity.LUNGE_WINDUP -> (float)Math.min(1,t/LUNGE_WINDUP)*.6F;
            case NaeglerophaeonEntity.CONDUCT -> (float)Math.min(1,t/15);
            case NaeglerophaeonEntity.NOVA -> (float)Math.min(1,t/NOVA_WINDUP);
            case NaeglerophaeonEntity.TRANSITION -> .7F+.3F*(float)Math.abs(Math.sin(t*.9));
            case NaeglerophaeonEntity.OVERLOAD -> .6F+.4F*(float)Math.max(0,1-Math.floorMod((long)t,30)/8.0);
            case NaeglerophaeonEntity.DRAINED -> -.15F;
            default -> 0;
        };
    }

    private static float smooth(float t) {
        t=Mth.clamp(t,0,1);
        return t*t*(3-2*t);
    }

    private static float propulsionStroke(double time) {
        double cycle=Math.floorMod((long)Math.floor(time),100)+(time-Math.floor(time));
        if(cycle<72) return smooth((float)(cycle/72));
        if(cycle<84) return 1-smooth((float)((cycle-72)/12));
        return 0;
    }

    private static final float LUNGE_WINDUP=NaeglerophaeonCombatRules.LUNGE_WINDUP,
            LASH_STRIKE=NaeglerophaeonCombatRules.LASH_WINDUP, NOVA_WINDUP=NaeglerophaeonCombatRules.NOVA_WINDUP;
    private static final float[] FLARE={40,26,16,8}, TUCK={-14,-9,-6,-3}, RADIAL={70,30,10,0},
            UMBRELLA={58,42,28,14}, REACH={25,18,10,5}, LIMP={-3,-2,-1,0}, EASED={20,12,6,2};

    private static float ringAngle(NaeglerophaeonModel.Input input,int segment,int limb) {
        float[] closed={-5,-3,-2,-1};
        float[] spread={13,8,5,2};
        float[] hunting={-9,-5,-3,-1};
        float stroke=propulsionStroke(input.time()-segment*3);
        float idle=closed[segment]+spread[segment]*stroke;
        float swim=hunting[segment]+(float)Math.sin(input.time()*.26-segment*.35)*(2.4F-segment*.5F);
        float base=Mth.lerp((float)input.hunting(),idle,swim);
        double t=input.phaseTicks();
        float in=smooth((float)(t/4));
        return switch(input.phase()) {
            case NaeglerophaeonEntity.LUNGE_WINDUP -> Mth.lerp(smooth((float)(t/LUNGE_WINDUP)),base,FLARE[segment]);
            case NaeglerophaeonEntity.LUNGE -> Mth.lerp(smooth((float)(t/2)),FLARE[segment],TUCK[segment]);
            case NaeglerophaeonEntity.LASH -> t<LASH_STRIKE
                    ?Mth.lerp(in,base,TUCK[segment])+(float)Math.sin(t*1.7+limb)*1.5F
                    :Mth.lerp(smooth((float)((t-LASH_STRIKE)/2)),TUCK[segment],RADIAL[segment]);
            case NaeglerophaeonEntity.NOVA -> t<NOVA_WINDUP
                    ?Mth.lerp(in,base,TUCK[segment]-2)+(float)(Math.sin(t*2.3+limb*1.3)*t/NOVA_WINDUP*2)
                    :Mth.lerp(smooth((float)((t-NOVA_WINDUP)/2)),TUCK[segment],RADIAL[segment]);
            case NaeglerophaeonEntity.CONDUCT, NaeglerophaeonEntity.CHARGE -> Mth.lerp(in*.6F,base,REACH[segment]);
            case NaeglerophaeonEntity.TRANSITION -> Mth.lerp(in,base,FLARE[segment])+(float)Math.sin(input.time()*1.9+limb*2.1+segment)*6;
            case NaeglerophaeonEntity.NERVE_DIVE -> Mth.lerp(in,base,TUCK[segment]);
            case NaeglerophaeonEntity.NERVE_EMERGE -> Mth.lerp(smooth((float)(t/6)),FLARE[segment],base);
            case NaeglerophaeonEntity.OVERLOAD -> Mth.lerp(smooth((float)(t/20)),base,UMBRELLA[segment])
                    +(float)(Math.sin(input.time()*.9+limb*1.7+segment*.8)*(3+segment*1.5));
            case NaeglerophaeonEntity.DRAINED -> LIMP[segment]+(float)Math.sin(input.time()*.05+limb)*1.2F
                    +(Math.floorMod((long)input.time()+limb*7,53)<2?(float)Math.sin(input.time()*4)*4:0);
            case NaeglerophaeonEntity.RECOVERY -> Mth.lerp(smooth((float)(t/10)),EASED[segment],base);
            default -> base;
        };
    }

    private static void bendRing(PoseStack stack,float angle,float degrees) {
        float tangentX=-(float)Math.sin(angle), tangentY=(float)Math.cos(angle);
        stack.mulPose(new Quaternionf().rotationAxis(degrees*Mth.DEG_TO_RAD,tangentX,tangentY,0));
    }

    private static void renderGroup(Group group,PoseStack stack,VertexConsumer consumer,int pass,
            NaeglerophaeonModel.Input input,int light,int overlay,int limb,int bend,boolean dynamicTail,
            Vec3[] tips,Vec3[] tailPoints,Quaternionf headAim,boolean coreEye,boolean markEyes) {
        String name=group.name();
        int tailBend=name.startsWith("capture bend ")?Integer.parseInt(name.substring(13))-1:-1;
        coreEye|=name.startsWith("embedded orb ");
        if(dynamicTail && name.equals("Thirty-block crimson capture tendril")) return;
        if(name.startsWith("tendril ")) limb=Integer.parseInt(name.substring(8))-1;
        if(name.endsWith(" bone")) bend=switch(name) {
            case "root bone" -> 0;
            case "bend bone" -> 1;
            case "sweep bone" -> 2;
            case "tip bone" -> 3;
            default -> bend;
        };
        stack.pushPose();
        float[] pivot=group.origin();
        stack.translate(pivot[0]*PIXEL,pivot[1]*PIXEL,pivot[2]*PIXEL);
        if(tailPoints!=null && name.startsWith("crimson joint ")) {
            int joint=Integer.parseInt(name.substring(14))-1;
            Vector4f at=new Vector4f(0,0,0,1);
            stack.last().pose().transform(at);
            tailPoints[joint]=new Vec3(at.x,at.y,at.z);
        }
        float[] rot=group.rotation();
        float rx=rot[0],ry=rot[1],rz=rot[2];
        if(tailBend>=0 && input.phase()==NaeglerophaeonEntity.OVERLOAD) {
            float writhe=(float)Math.sin(input.time()*.55-tailBend*.9);
            float cross=(float)Math.cos(input.time()*.71-tailBend*1.3);
            rx+=(5+tailBend*1.2F)*writhe;
            ry+=(6+tailBend*1.5F)*cross;
        } else if(tailBend>=0) {
            double rate=input.drained()?.02:.075;
            float wave=(float)(Math.sin(input.time()*rate-tailBend*.5)-Math.sin(-tailBend*.5));
            rx+=1.5F*wave;
            ry+=(2+tailBend*.7F)*wave;
        } else if(name.equals("Deep maroon core and seven embedded orbs")) {
            rz+=(float)Math.sin(input.time()*.06)*2;
        }
        if(rx!=0) stack.mulPose(new Quaternionf().rotationX(rx*Mth.DEG_TO_RAD));
        if(ry!=0) stack.mulPose(new Quaternionf().rotationY(ry*Mth.DEG_TO_RAD));
        if(rz!=0) stack.mulPose(new Quaternionf().rotationZ(rz*Mth.DEG_TO_RAD));
        if(limb>=0 && bend>=0 && name.endsWith(" bone")) {
            bendRing(stack,(float)Math.atan2(pivot[1],pivot[0]),ringAngle(input,bend,limb));
            stack.mulPose(new Quaternionf().rotationY(input.turns().ringYaw()[bend])
                    .rotateX(input.turns().ringPitch()[bend]));
        } else if(name.startsWith("web ") && name.length()>4 && Character.isDigit(name.charAt(4))) {
            int web=Integer.parseInt(name.substring(4))-1;
            float angle=(float)((web+.5)*Math.PI/6+.1);
            bendRing(stack,angle,ringAngle(input,0,web)*.72F);
            stack.mulPose(new Quaternionf().rotationY(input.turns().ringYaw()[0])
                    .rotateX(input.turns().ringPitch()[0]));
        } else if(tailBend>=0) {
            stack.mulPose(new Quaternionf().rotationY(input.turns().tailYaw()[tailBend])
                    .rotateX(input.turns().tailPitch()[tailBend]));
        }
        if(headAim!=null && name.equals("Deep maroon core and seven embedded orbs"))
            stack.mulPose(headAim);
        if(name.startsWith("pulse orb at ") && limb>=0) {
            int node=Integer.parseInt(name.substring(13,name.indexOf(" blocks")))/4;
            float activation=0;
            for(var pulse:NaeglerophaeonModel.pulses(input.time(),input.seed())) {
                if(pulse.limb()==limb) activation=Math.max(activation,
                        (float)Math.max(0,1-Math.abs(pulse.progress()-node/3D)*8));
            }
            float size=.82F+activation*.58F;
            stack.scale(size,size,size);
        } else if(name.startsWith("embedded orb ")) {
            float size=1+coreCharge(input)*.35F;
            stack.scale(size,size,size);
        } else if(name.startsWith("steady node ") && input.phase()==NaeglerophaeonEntity.OVERLOAD) {
            // A pulse climbs the hanging tail toward the crown before every blast.
            int node=Integer.parseInt(name.substring(12));
            double climb=1-Math.min(1,Math.floorMod((long)input.phaseTicks(),30)/22.0);
            float activation=(float)Math.max(0,1-Math.abs(climb-node/8.0)*5);
            float size=1+activation*.9F;
            stack.scale(size,size,size);
        }
        stack.translate(-pivot[0]*PIXEL,-pivot[1]*PIXEL,-pivot[2]*PIXEL);
        for(Object child:group.children()) {
            if(child instanceof Group next) renderGroup(next,stack,consumer,pass,input,light,overlay,limb,bend,dynamicTail,tips,tailPoints,headAim,coreEye,markEyes);
            else if(pass>=0) renderCube((Cube)child,stack,consumer,pass,light,overlay,coreEye,markEyes,
                    coreEye && input.enraged() && !input.drained()?ENRAGED_TINT:-1,input.drained());
            else if(tips!=null && limb>=0 && limb<12 && ((Cube)child).name().equals("tip discharge")) {
                Cube cube=(Cube)child;
                float[] from=cube.from(),to=cube.to();
                Vector4f at=new Vector4f((from[0]+to[0])*.5F*PIXEL,
                        (from[1]+to[1])*.5F*PIXEL,(from[2]+to[2])*.5F*PIXEL,1);
                stack.last().pose().transform(at);
                tips[limb]=new Vec3(at.x,at.y,at.z);
            }
        }
        stack.popPose();
    }

    private static final int ENRAGED_TINT=0xFFE6A8FF;

    /** Drained bodies draw their former light organs in the opaque pass instead of the emissive one. */
    private static void renderCube(Cube cube,PoseStack stack,VertexConsumer consumer,int pass,int light,int overlay,
            boolean coreEye,boolean markEyes,int tint,boolean dim) {
        int material=cube.material();
        int materialPass=material==6?2:(material==8 || material==9 || material==10)?1:0;
        if(dim && materialPass==1) materialPass=0;
        if(markEyes && coreEye && materialPass==1) materialPass=3;
        if(pass!=materialPass) return;
        float[] a=cube.from(),b=cube.to();
        float[][] points={{a[0],a[1],a[2]},{b[0],a[1],a[2]},
                {a[0],b[1],a[2]},{b[0],b[1],a[2]},
                {a[0],a[1],b[2]},{b[0],a[1],b[2]},
                {a[0],b[1],b[2]},{b[0],b[1],b[2]}};
        for(int side=0;side<6;side++) {
            int[] corners=FACE_CORNERS[side];
            float[] uv=cube.uv()[side],normal=NORMALS[side];
            float[][] tex={{uv[0],uv[1]},{uv[2],uv[1]},{uv[2],uv[3]},{uv[0],uv[3]}};
            for(int j=0;j<4;j++) {
                float[] point=points[corners[j]];
                consumer.addVertex(stack.last().pose(),point[0]*PIXEL,point[1]*PIXEL,point[2]*PIXEL)
                        .setColor(tint).setUv(tex[j][0]*INV_ATLAS,tex[j][1]*INV_ATLAS)
                        .setOverlay(overlay).setLight(pass==1 || pass==3?LightTexture.FULL_BRIGHT:light)
                        .setNormal(stack.last(),normal[0],normal[1],normal[2]);
            }
        }
    }

    public static void renderCaptureTail(PoseStack stack,VertexConsumer consumer,int pass,Vec3[] curve,int light,int overlay) {
        if(pass>=2 || curve==null) return;
        for(int j=0;j<48;j++) {
            Vec3 a=NaeglerophaeonModel.sample(curve,j/48D);
            Vec3 b=NaeglerophaeonModel.sample(curve,(j+1)/48D);
            Vec3 delta=b.subtract(a);
            if(delta.lengthSqr()<1.0E-6) continue;
            stack.pushPose();
            stack.translate(a.x,a.y,a.z);
            stack.mulPose(new Quaternionf().rotationTo(0,0,1,(float)delta.x,(float)delta.y,(float)delta.z));
            float width=(float)((6*(1-j/52D)+1.4)/16);
            float length=(float)delta.length();
            renderDynamicBox(stack,consumer,pass,light,overlay,
                    -width/2,-width/2,-.035F,width/2,width/2,length+.035F,j%4==0?1:5);
            stack.popPose();
        }
        for(int j=1;j<=8;j++) {
            Vec3 p=NaeglerophaeonModel.sample(curve,j/8D);
            stack.pushPose();
            stack.translate(p.x,p.y,p.z);
            float size=j==8?.2F:.26F;
            renderDynamicBox(stack,consumer,pass,light,overlay,-size,-size,-size,size,size,size,7);
            renderDynamicBox(stack,consumer,pass,light,overlay,-size*.52F,-size*.52F,-size- .03F,
                    size*.52F,size*.52F,-size*.2F,9);
            stack.popPose();
        }
    }

    private static void renderDynamicBox(PoseStack stack,VertexConsumer consumer,int pass,int light,int overlay,
            float x0,float y0,float z0,float x1,float y1,float z1,int material) {
        float u=(material%4)*32, v=(material/4)*32;
        float[][] uv=new float[6][];
        for(int i=0;i<6;i++) uv[i]=new float[]{u+1,v+1,u+31,v+31};
        Cube cube=new Cube("dynamic tail",new float[]{x0/PIXEL,y0/PIXEL,z0/PIXEL},
                new float[]{x1/PIXEL,y1/PIXEL,z1/PIXEL},uv,material);
        renderCube(cube,stack,consumer,pass,light,overlay,false,false,-1,false);
    }
}
