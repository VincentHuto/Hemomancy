package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.particle.BloodCellParticle;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.SporiticSporeParticleFactory;
import com.vincenthuto.hutoslib.client.particle.ParticleGlow;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

final class BloodEffectParticles {
    private record Mist(Vec3 at,Vec3 velocity,boolean mortem,int seed,long born,int life,float size) {}
    private static final List<Mist> MISTS=new ArrayList<>();
    private static ClientLevel world;
    private BloodEffectParticles() {}
    private static void reset(){if(world!=Minecraft.getInstance().level){MISTS.clear();world=Minecraft.getInstance().level;}}
    static void emit(Vec3 at,Vec3 direction,boolean mortem,int count,boolean soft,boolean fungal) {
        reset();if(world==null||count<=0)return;var mc=Minecraft.getInstance();
        for(int i=0;i<count;i++) {
            Vec3 offset=new Vec3(world.random.nextDouble()-.5,world.random.nextDouble()-.5,world.random.nextDouble()-.5).scale(soft?.28:.55);
            Vec3 velocity=soft?offset.scale(mortem?-.025:.018).add(0,mortem?.007:.012,0):offset.scale(.18).add(direction.normalize().scale(.035));
            if(i%4==0) {
                if(MISTS.size()>=128)MISTS.removeFirst();
                MISTS.add(new Mist(at.add(offset),velocity.scale(.3),mortem,world.random.nextInt(113),world.getGameTime(),24,soft?.18f:.34f));
            }else {
                var color=mortem?new ParticleColor(fungal?146:103,fungal?126:39,fungal?72:30):new ParticleColor(198,27,54);
                var options=mortem&&fungal?SporiticSporeParticleFactory.createData(color):BloodCellParticleFactory.createData(color);
                var particle=mc.particleEngine.createParticle(options,at.x+offset.x,at.y+offset.y,at.z+offset.z,velocity.x*.5,velocity.y*.5,velocity.z*.5);
                if(particle instanceof BloodCellParticle cell){cell.initScale=soft?.035f:.065f;cell.initAlpha=.65f;}
                if(particle instanceof ParticleGlow spore){spore.initScale=.035f;spore.initAlpha=.42f;}
                if(particle!=null)particle.setLifetime(soft?28:20);
            }
        }
    }
    static void render(PoseStack p,LuxUmbraBatch batch,Vec3 camera,Vec3 right,Vec3 up,double time) {
        reset();if(world==null)return;MISTS.removeIf(m->time-m.born>=m.life);
        var setting=Minecraft.getInstance().options.particles().get();if(setting==ParticleStatus.MINIMAL)return;
        for(int i=0;i<MISTS.size();i++) {
            if(setting==ParticleStatus.DECREASED&&i%2==1)continue;Mist m=MISTS.get(i);
            if(m.at.distanceToSqr(camera)>48*48)continue;
            double age=time-m.born;Vec3 at=m.at.add(m.velocity.scale(age)).subtract(camera);
            p.pushPose();p.translate(at.x,at.y,at.z);
            LuxUmbraGeometry.card(p,batch.vertices(m.mortem?LuxUmbraBatch.Material.MORTEM:LuxUmbraBatch.Material.ANIMUS),
                    Vec3.ZERO,right,up,m.size*(1+age*.02),m.size*(1+age*.03),1,m.seed,0xFFFFFF,(float)Math.sin(age/m.life*Math.PI)*.5f);
            p.popPose();
        }
    }
}
