package com.vincenthuto.hemomancy.gametest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.ManipulationVisualRenderer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in, gameTest-source-only renderer inspection; world mode requires a disposable save. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class ManipulationVisualReview {
    private static boolean opened;
    private static int worldTicks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(Boolean.getBoolean("hemomancy.visualWorldReview") && mc.level!=null && mc.player!=null
                && mc.getSingleplayerServer()!=null) reviewWorld(mc);
        if(Boolean.getBoolean("hemomancy.visualReview") && !opened && mc.screen instanceof TitleScreen) {
            opened=true;mc.setScreen(new ReviewScreen());
        }
    }

    private static void reviewWorld(Minecraft mc) {
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
        int tick=worldTicks++, scene=tick/90, phase=tick%90;
        if(scene>=12){mc.stop();return;}
        if(phase==0) {
            mc.options.setCameraType(scene%2==0?net.minecraft.client.CameraType.FIRST_PERSON:net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                if(player==null)return;
                var level=player.serverLevel();
                com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.attached(player,Form.CROWN,0,0,0);
                com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.clearSessionState();
                int x=scene*40;
                for(int dx=-8;dx<=8;dx++)for(int dz=-8;dz<=12;dz++) {
                    var pos=new net.minecraft.core.BlockPos(x+dx,99,dz);
                    level.setBlockAndUpdate(pos,((dx+dz)%2==0?net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE:net.minecraft.world.level.block.Blocks.STONE).defaultBlockState());
                }
                for(int dy=0;dy<4;dy++) level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,100+dy,9),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
                player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                player.teleportTo(level,x+.5,100,.5,0,0);
            });
        }
        if(phase==8 && scene<6)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            var form=scene<2?Form.CROWN_CHARGE:scene<4?Form.VERDICT_CHARGE:Form.GLASS_CHARGE;
            var from=player.getEyePosition();
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(player.serverLevel(),null,from.x,from.y,from.z,64,
                    new ManipulationVisualPacket(form,player.getId(),from,from.add(player.getLookAngle().scale(8)),.7F,16,1));
        });
        if(phase==45 && scene<2)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            var level=player.serverLevel();var enemy=net.minecraft.world.entity.EntityType.HUSK.create(level);
            enemy.setPos(player.position().add(2,0,6));enemy.setNoAi(true);level.addFreshEntity(enemy);
            com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.onIncomingDamage(
                new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(player,
                    new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().mobAttack(enemy),2)));
        });
        if(phase==25)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            var level=player.serverLevel();var empty=net.minecraft.world.item.ItemStack.EMPTY;
            switch(scene/2) {
                case 0 -> com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.armCoronation(player,8,1);
                case 1 -> com.vincenthuto.hemomancy.common.init.ManipulationInit.white_verdict.get().getAction(player,level,empty,player.blockPosition(),60);
                case 2 -> com.vincenthuto.hemomancy.common.init.ManipulationInit.vitric_combustion.get().getAction(player,level,empty,player.blockPosition(),60);
                case 3 -> com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.createEclipseWell(level,player.position().add(0,0,5),3,45,player.getUUID());
                case 4 -> com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.burst(level,Form.PHOENIX,player.position().add(0,0,4),player.position(),5,48);
                case 5 -> com.vincenthuto.hemomancy.common.init.ManipulationInit.absolute_stillness.get().getAction(player,level,empty,player.blockPosition(),0);
            }
        });
        if(phase==20 || phase==34 || phase==46 || phase==50 || phase==77)Screenshot.grab(mc.gameDirectory,"world-"+scene+"-"+phase+".png",mc.getMainRenderTarget(),message->{});
    }

    private static class ReviewScreen extends Screen {
        private int ticks;
        private final java.lang.reflect.Method draw;
        private static final Form[] FORMS={Form.CROWN,Form.VERDICT,Form.GLASS,Form.THREAD,Form.WELL,
                Form.STILLNESS,Form.BEACON,Form.PHOENIX,Form.BELL,Form.FURNACE,Form.DRAIN,Form.MENDING};
        ReviewScreen(){
            super(Component.literal("Manipulation geometry review"));
            try {
                draw=ManipulationVisualRenderer.class.getDeclaredMethod("draw",ManipulationVisualPacket.class,
                        PoseStack.class,VertexConsumer.class,double.class,float.class,float.class);
                draw.setAccessible(true);
            } catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        }
        @Override public void tick(){
            ticks++;
            if(ticks==40 || ticks==80 || ticks==120) Screenshot.grab(minecraft.gameDirectory,
                    "manipulation-geometry-"+ticks+".png",minecraft.getMainRenderTarget(),message->{});
            if(ticks==140)minecraft.stop();
        }
        @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){
            graphics.fill(0,0,width,height,0xFF160D16);
            graphics.drawCenteredString(font,"MANIPULATION MESH REVIEW / diagnostic, not in-world acceptance",width/2,8,0xEAC6BB);
            float age=ticks<60?3:ticks<100?12:26;
            float opacity=ticks<100?1:.35F;
            var buffers=minecraft.renderBuffers().bufferSource();
            for(int i=0;i<FORMS.length;i++) {
                int cellW=width/4, cellH=(height-28)/3;
                int x=(i%4)*cellW, y=25+(i/4)*cellH;
                graphics.drawString(font,FORMS[i].name(),x+5,y,0xD4AFB1);
                graphics.flush();
                var pose=graphics.pose();pose.pushPose();pose.translate(x+cellW/2.0,y+cellH*.65,100);
                float scale=Math.min(cellW,cellH)*.16F;pose.scale(scale,-scale,scale);
                pose.mulPose(Axis.XP.rotationDegrees(18));pose.mulPose(Axis.YP.rotationDegrees(-28));
                var form=FORMS[i];
                double radius=form==Form.WELL || form==Form.STILLNESS || form==Form.BEACON?2:1;
                var end=form==Form.VERDICT || form==Form.THREAD || form==Form.DRAIN?new Vec3(3,1,0):Vec3.ZERO;
                try {draw.invoke(null,new ManipulationVisualPacket(form,-1,Vec3.ZERO,end,(float)radius,40,8),
                        pose,buffers.getBuffer(com.vincenthuto.hemomancy.client.render.world.ManipulationMaterials.forForm(form).renderType()),100.0,age,opacity);}
                catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
                pose.popPose();buffers.endBatch(com.vincenthuto.hemomancy.client.render.world.ManipulationMaterials.forForm(form).renderType());
            }
        }
    }
}
