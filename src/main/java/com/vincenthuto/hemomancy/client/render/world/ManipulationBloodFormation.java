package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import net.minecraft.world.phys.Vec3;

/** Small connecting ligaments and beads, never a lingering gameplay subject or field outline. */
final class ManipulationBloodFormation {
    private ManipulationBloodFormation() {}

    static boolean retainsDissolvingBody(Form form) {
        if(form.name().endsWith("_CHARGE"))return false;
        return switch(form) {
            case VERDICT,CROWN,CHOIR,WARD,RETORT,MARK,WOUND,HUNGER,GRAVE,DEBT,HOUR,
                    COMMAND,CIRCUIT,BEACON,ORE,IRON_HEART,BLACK_HEART,PHOENIX_READY,EYE -> false;
            default -> true;
        };
    }

    static void draw(ManipulationVisualPacket packet,PoseStack poses,VertexConsumer vertices,
            float progress,boolean departing,double time) {
        float alpha=departing?(1-progress)*.7F:(float)Math.sin(Math.PI*progress)*.75F;
        if(alpha<=.001F)return;
        double height=switch(packet.form()) {
            case CROWN -> 2.7;
            case CROWN_CHARGE -> 1.1;
            case BELL -> 2;
            case BELL_CHARGE -> 1.2;
            case IRON_HEART -> 0;
            case CHOIR,WARD,RETORT,BLACK_HEART,MARK,WOUND,HUNGER,CIRCUIT,
                    RUSH,CAUTERIZE,MENDING,FORGE,PHOENIX,PHOENIX_READY -> 1;
            case GRAVE -> 2.2;
            case EYE,DEBT,HOUR -> 2.4;
            case COMMAND -> packet.radius()+.35;
            default -> .12;
        };
        double reach=Math.min(.65,Math.max(.22,packet.radius()*.15));
        var school=ManipulationMaterials.forForm(packet.form());
        int blood=school==ManipulationMaterials.MORTEM?0x631C2C:
                school==ManipulationMaterials.TENEBRIS?0x451329:0xA21B35;
        for(int i=0;i<3;i++) {
            double angle=i*2.39996+packet.from().hashCode()*.0001;
            Vec3 focus=new Vec3(Math.cos(angle)*reach*.25,height,Math.sin(angle)*reach*.25);
            if(packet.form().name().endsWith("_CHARGE") && packet.form()!=Form.CROWN_CHARGE
                    && packet.form()!=Form.BELL_CHARGE && packet.form()!=Form.THREAD_CHARGE) {
                Vec3 direction=packet.to().subtract(packet.from()).normalize();
                focus=ChargeVisualGeometry.alongAim(direction,packet.form()==Form.GLASS_CHARGE?1.4:1.25)
                        .add(Math.cos(angle)*.05,0,Math.sin(angle)*.05);
            }
            if(packet.form()==Form.CROWN || packet.form()==Form.CROWN_CHARGE) {
                int count=packet.form()==Form.CROWN?Math.min(8,packet.count()):(int)Math.ceil(8*Math.min(1,packet.radius()));
                if(i>=count)break;
                focus=com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.swordOffset(time,i)
                        .add(0,packet.form()==Form.CROWN_CHARGE?-2.42:-.8,0);
            } else if(packet.form()==Form.CHOIR || packet.form()==Form.WARD) {
                int count=Math.min(packet.form()==Form.CHOIR?3:8,packet.count());
                if(i>=count)break;
                double orbit=time*(packet.form()==Form.CHOIR?.018:.025)+i*Math.PI*2/count;
                focus=new Vec3(Math.cos(orbit)*1.05,.8,Math.sin(orbit)*1.05);
            }
            Vec3 source=focus.add(Math.cos(angle)*reach,-.35,Math.sin(angle)*reach);
            double travel=departing?1-progress:progress;
            Vec3 bead=source.lerp(focus,travel);
            Vec3 tail=source.lerp(focus,Math.max(0,travel-.4));
            if(departing) {
                bead=focus.add(Math.cos(angle)*progress*.15,-progress*progress*.5,Math.sin(angle)*progress*.15);
                tail=focus.lerp(bead,.25);
            }
            VisceralMesh.strand(poses,vertices,tail,bead,.022*(1-progress*.5),.045,
                    angle+progress,blood,alpha);
            VisceralMesh.drop(poses,vertices,bead,.035,.065,blood,alpha,0);
        }
    }
}
