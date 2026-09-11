package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationAccentPacket;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Opt-in packet/material study in the disposable VisceralReview save, not a combat test. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class VisceralVisualReview {
    private static final Form[] FORMS={Form.CROWN,Form.BELL,Form.THREAD,Form.CHOIR,Form.GLASS,Form.STILLNESS,Form.VERDICT,Form.WELL};
    private static final EnumBloodTendency[] SCHOOLS={EnumBloodTendency.ANIMUS,EnumBloodTendency.MORTEM,
            EnumBloodTendency.DUCTILIS,EnumBloodTendency.FERRIC,EnumBloodTendency.FLAMMEUS,
            EnumBloodTendency.CONGEATIO,EnumBloodTendency.LUX,EnumBloodTendency.TENEBRIS};
    private static int ticks;
    private static volatile int rosterSource=-1;
    private static String waitingScreen="";
    private VisceralVisualReview() {}

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        Minecraft mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("hemomancy.visceralReview"))return;
        if(mc.level==null || mc.player==null || mc.getSingleplayerServer()==null) {
            if(mc.screen!=null) {
                String screen=mc.screen.getClass().getSimpleName()+": "+mc.screen.getTitle().getString();
                if(!screen.equals(waitingScreen)) {
                    waitingScreen=screen;
                    org.slf4j.LoggerFactory.getLogger(VisceralVisualReview.class).info("Visceral review awaiting {}",screen);
                    for(var child:mc.screen.children())if(child instanceof net.minecraft.client.gui.components.AbstractWidget widget)
                        org.slf4j.LoggerFactory.getLogger(VisceralVisualReview.class).info("Review screen button: {}",widget.getMessage().getString());
                }
            }
            return;
        }
        if(!mc.getSingleplayerServer().getWorldData().getLevelName().equals("VisceralReview"))
            throw new IllegalStateException("Visceral review requires the disposable VisceralReview world");
        mc.options.pauseOnLostFocus=false;
        mc.options.hideGui=true;
        if(mc.screen!=null)mc.setScreen(null);
        boolean boundaries=Boolean.getBoolean("hemomancy.visceralBoundaries");
        boolean roster=Boolean.getBoolean("hemomancy.visceralRoster") || boundaries;
        Form[] rosterForms=boundaries?new Form[]{Form.WELL,Form.STILLNESS,Form.BEACON,Form.FURNACE,
                Form.CLOUD,Form.MAGNET,Form.RUPTURE,Form.UPDRAFT}:Form.values();
        int length=roster?45:90;
        int tick=ticks++,scene=tick/length,phase=tick%length;
        if(scene>=(roster?rosterForms.length:32)){mc.stop();return;}
        int mode=roster?1:scene/8,index=scene%8;
        Form form=roster?rosterForms[scene]:FORMS[index];
        double x=scene*40+.5;
        if(phase==0) {
            mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            mc.options.particles().set(mode==3?net.minecraft.client.ParticleStatus.MINIMAL:net.minecraft.client.ParticleStatus.ALL);
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                if(player==null)return;
                var level=player.serverLevel();
                level.setDayTime(mode==2?18000:6000);level.setWeatherParameters(6000,0,false,false);
                for(int dx=-10;dx<=10;dx++)for(int dz=-5;dz<=17;dz++) {
                    var pos=new BlockPos((int)x+dx,99,dz);
                    level.setBlockAndUpdate(pos,((dx+dz)%2==0?net.minecraft.world.level.block.Blocks.STONE:
                            net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE).defaultBlockState());
                }
                player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                player.teleportTo(level,x+(mode==0?0:5),100,mode==0?0:-1,mode==0?0:35,mode==0?8:10);
                if(roster) {
                    var anchor=net.minecraft.world.entity.EntityType.ARMOR_STAND.create(level);
                    anchor.setPos(x,form==Form.CLOUD?110:100,4);anchor.setInvisible(true);anchor.setNoGravity(true);
                    level.addFreshEntity(anchor);rosterSource=anchor.getId();
                }
                for(int i=0;i<3;i++) {
                    var target=net.minecraft.world.entity.EntityType.HUSK.create(level);
                    target.setPos(x+(i-1)*2,100,7);target.setNoAi(true);level.addFreshEntity(target);
                }
            });
        }
        if(roster?(phase==4 || phase==12 || phase==22):phase==8 || phase==28)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            Vec3 at=new Vec3(x,roster && form==Form.CLOUD?110:100,4),end=at;
            Form current=form;
            if(!roster && phase==8) {
                current=switch(form){case CROWN->Form.CROWN_CHARGE;case BELL->Form.BELL_CHARGE;
                    case THREAD->Form.THREAD_CHARGE;case GLASS->Form.GLASS_CHARGE;case VERDICT->Form.VERDICT_CHARGE;
                    case WELL->Form.WELL_CHARGE;default->form;};
                if(current.name().endsWith("_CHARGE"))at=at.add(0,1.62,0);
            }
            if(phase==28 && (form==Form.VERDICT || form==Form.THREAD || form==Form.GLASS))at=at.add(0,1.25,0);
            if(form==Form.THREAD || form==Form.VERDICT || form==Form.DRAIN || form==Form.SUTURE
                    || current.name().endsWith("_CHARGE"))end=at.add(0,0,7);
            double radius=phase==8?.7:form==Form.WELL||form==Form.STILLNESS?3:form==Form.VERDICT?.85:2.5;
            if(roster)radius=current.name().endsWith("_CHARGE")?.7:2.5;
            PacketDistributor.sendToPlayer(player,new ManipulationVisualPacket(current,roster?rosterSource:-1,at,end,(float)radius,
                    roster?(phase==22?0:24):phase==8?18:44,
                    form==Form.CHOIR?3:boundaries && form==Form.UPDRAFT?4:8));
            if(mode==3 && phase==28) {
                PacketDistributor.sendToPlayer(player,new ManipulationVisualPacket(Form.FURNACE,-1,at.add(-2,0,2),end,2,40,1));
                PacketDistributor.sendToPlayer(player,new ManipulationVisualPacket(Form.DRAIN,-1,at.add(2,1,2),at.add(0,1,0),1,40,1));
            }
        });
        if(!roster && (phase==28 || (phase==38 && form!=Form.STILLNESS)))mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            if(player==null)return;
            Vec3 at=new Vec3(x,101.1,phase==28?4:7);
            PacketDistributor.sendToPlayer(player,new ManipulationAccentPacket(SCHOOLS[index],null,
                    at,new Vec3(0,0,1),-1,phase==38));
        });
        if(roster?(phase==5 || phase==8 || phase==16 || phase==26 || phase==32 || phase==40):
                (phase==15 || phase==31 || phase==41 || phase==66 || phase==80)) {
            String name="visceral-"+(boundaries?"boundary":roster?"roster":mode)+"-"+form.name().toLowerCase(java.util.Locale.ROOT)+"-"+phase+".png";
            Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),message->{});
        }
    }
}
