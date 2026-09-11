package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.particle.BloodCellParticle;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationAccentPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hutoslib.client.particle.ParticleGlow;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/** Small authored sprite accents around the material meshes, emitted on game ticks. */
public final class ManipulationAmbientParticles {
    private static final ManipulationParticleBudget BUDGET=new ManipulationParticleBudget();
    private static ClientLevel world;
    private ManipulationAmbientParticles() {}

    public static void clear() {world=null;BUDGET.clear();ThermalParticles.clear();}

    public static void accept(ManipulationAccentPacket packet) {
        var mc=Minecraft.getInstance();
        if(!ready(mc.level,packet.position()) || !Double.isFinite(packet.direction().lengthSqr()))return;
        long now=world.getGameTime();
        if(packet.impact() && packet.targetId()>=0 && !BUDGET.hit(packet.targetId(),now))return;
        var caster=packet.impact()?null:world.getEntity(packet.targetId());
        Vec3 at=caster==null?packet.position():caster.position().add(0,caster.getBbHeight()*.55,0);
        emit(at,packet.direction(),packet.primary(),packet.secondary(),packet.impact()?12:10,
                packet.impact()?.26:.20,packet.impact(),false);
    }

    static void formation(ManipulationVisualPacket packet,ClientLevel level) {
        if(!packet.form().name().endsWith("_CHARGE"))return;
        var entity=level.getEntity(packet.entityId());
        Vec3 at=entity==null?packet.from():entity.getEyePosition();
        Vec3 aim=packet.to().subtract(at).normalize();
        at=at.add(aim.scale(1.1));
        if(ready(level,at))emit(at,Vec3.ZERO,school(packet),null,6,.22,false,true);
    }

    static void ambient(ManipulationVisualPacket packet,ClientLevel level) {
        // Stillness stays quiet after formation; charge refreshes never re-emit a burst.
        switch(packet.form()) {
            case CROWN, CHOIR, WARD, WELL, BEACON, VEIL, FURNACE, PHOENIX_READY, RETORT -> {}
            case WOUND,HUNGER,GRAVE,ROT_INFECTION,BLACK_HEART,BLOOM,COMMUNION,CLOUD -> {}
            default -> {return;}
        }
        long now=level.getGameTime();
        if(Math.floorMod(now+packet.entityId(),12)!=0)return;
        var entity=level.getEntity(packet.entityId());
        if(packet.entityId()>=0 && (entity==null || !entity.isAlive() || entity.isInvisible()))return;
        if(packet.form()==ManipulationVisuals.Form.RETORT && entity==net.minecraft.client.Minecraft.getInstance().player
                && (!(entity instanceof net.minecraft.world.entity.LivingEntity living)
                || !living.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.iron_retort)))return;
        Vec3 at=entity==null?packet.from():entity.position();
        Vec3 motion=new Vec3(0,.01,0);
        double angle=now*.065+packet.entityId();
        if(packet.form()==ManipulationVisuals.Form.CROWN) {
            if(packet.count()<=0)return;
            int slot=(int)Math.floorMod(now/12,Math.min(8,packet.count()));
            at=at.add(ManipulationVisuals.swordOffset(now,slot)).add(0,-.65,0);
        } else if(packet.form()==ManipulationVisuals.Form.WELL) {
            double radius=Math.min(4,packet.radius())*.6;
            Vec3 offset=new Vec3(Math.cos(angle)*radius,.15,Math.sin(angle)*radius);
            at=at.add(offset);motion=offset.scale(-.018);
        } else {
            double radius=packet.form()==ManipulationVisuals.Form.BEACON?.3:.8;
            at=at.add(Math.cos(angle)*radius,1.1,Math.sin(angle)*radius);
        }
        if(ready(level,at)) {
            boolean fungal=switch(packet.form()){case BLOOM,ROT_INFECTION,COMMUNION,BLACK_HEART->true;default->false;};
            if(fungal) {
                int setting=switch(Minecraft.getInstance().options.particles().get()){case ALL->0;case DECREASED->1;case MINIMAL->2;};
                BloodEffectParticles.emit(at,motion,true,BUDGET.take(now,3,setting),true,true);
            } else emit(at,motion,school(packet),null,2,.1,false,true);
        }
    }

    private static EnumBloodTendency school(ManipulationVisualPacket packet) {
        return EnumBloodTendency.valueOf(ManipulationMaterials.forForm(packet.form()).name());
    }

    private static boolean ready(ClientLevel level,Vec3 at) {
        if(world!=level){BUDGET.clear();world=level;}
        return level!=null && Double.isFinite(at.lengthSqr())
                && Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(at)<=48*48;
    }

    private static void emit(Vec3 at,Vec3 direction,EnumBloodTendency primary,EnumBloodTendency secondary,
            int requested,double spread,boolean impact,boolean soft) {
        var mc=Minecraft.getInstance();
        int setting=switch(mc.options.particles().get()){case ALL->0;case DECREASED->1;case MINIMAL->2;};
        int count=BUDGET.take(world.getGameTime(),requested,setting);
        if(primary==EnumBloodTendency.FLAMMEUS || primary==EnumBloodTendency.CONGEATIO) {
            ThermalParticles.emit(at,direction,primary==EnumBloodTendency.CONGEATIO,count,soft);
            return;
        }
        if (primary == EnumBloodTendency.TENEBRIS) {
            if (count > 0) LuxUmbraEffects.mist(at, direction.normalize().scale(soft ? .008 : .025),
                    true, soft ? .18f : impact ? .38f : .3f, soft ? 20 : 26);
            return;
        }
        if(primary==EnumBloodTendency.ANIMUS || primary==EnumBloodTendency.MORTEM) {
            BloodEffectParticles.emit(at,direction,primary==EnumBloodTendency.MORTEM,count,soft,false);
            return;
        }
        for(int i=0;i<count;i++) {
            Vec3 offset=new Vec3(world.random.nextDouble()-.5,world.random.nextDouble()-.5,world.random.nextDouble()-.5)
                    .normalize().scale(spread*(.4+world.random.nextDouble()*.6));
            Vec3 point=at.add(offset);
            Vec3 velocity=soft?direction.scale(.4).add(offset.scale(-.02))
                    :offset.scale(impact?.15:.07).add(direction.normalize().scale(impact?.025:.009)).add(0,.006,0);
            boolean cell=primary != EnumBloodTendency.LUX && i%3==0;
            EnumBloodTendency tint=primary != EnumBloodTendency.LUX && secondary!=null && i%4==3?secondary:primary;
            int rgb=cell?(primary==EnumBloodTendency.MORTEM?0x782039:0xBF2540):glowColor(tint);
            var color=new ParticleColor((rgb>>16)&255,(rgb>>8)&255,rgb&255);
            var options=cell?BloodCellParticleFactory.createData(color):GlowParticleFactory.createData(color);
            // Both authored particle classes double constructor velocity.
            var particle=mc.particleEngine.createParticle(options,point.x,point.y,point.z,
                    velocity.x*.5,velocity.y*.5,velocity.z*.5);
            int lifetime=soft?16:impact?20:24;
            float size=soft?.04F:cell?.075F:.105F;
            float alpha=soft?.30F:cell?.75F:.55F;
            if(particle instanceof BloodCellParticle blood){blood.initScale=size;blood.initAlpha=alpha;}
            if(particle instanceof ParticleGlow glow){glow.initScale=size;glow.initAlpha=alpha;}
            if(particle!=null)particle.setLifetime(lifetime);
        }
    }

    private static int glowColor(EnumBloodTendency school) {
        return switch(school) {
            case ANIMUS -> 0xE1455C;
            case MORTEM -> 0x86915B;
            case DUCTILIS -> 0xF5D681;
            case FERRIC -> 0xB6AAA4;
            case FLAMMEUS -> 0xFFAA70;
            case CONGEATIO -> 0xBCDCE8;
            case LUX -> 0xFFFAEF;
            case TENEBRIS -> 0x19171F;
        };
    }
}
