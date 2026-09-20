package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.antecedent.*;
import com.vincenthuto.hemomancy.common.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class AntecedentClientEffects {
    private static long acousticUntil,signalUntil,signalSince,scarUntil;
    private static BlockPos source=BlockPos.ZERO,scar=BlockPos.ZERO;
    private static float ambience=1;
    private static boolean epilogue;
    private static net.minecraft.client.multiplayer.ClientLevel lastLevel;
    private AntecedentClientEffects() {}
    public static void accept(AntecedentEffectPacket packet) {
        var mc=Minecraft.getInstance();if(mc.level==null || mc.player==null)return;
        long now=mc.level.getGameTime();
        switch(packet.kind()) {
            case 0 -> {acousticUntil=now+25; source=packet.position();}
            case 1 -> {scarUntil=now+15;scar=packet.position();mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_HEARTBEAT,1.5F,.08F));}
            case 2 -> {if(now>=signalUntil)signalSince=now;signalUntil=now+15;source=packet.position();}
            case 3 -> mc.getSoundManager().play(new SimpleSoundInstance(SoundEvents.SCULK_CLICKING,SoundSource.AMBIENT,.3F,.8F,RandomSource.create(),packet.position()));
            case 4 -> epilogue=true;
            case 5 -> {if(mc.level.getBlockEntity(packet.position()) instanceof VigilArchiveBlockEntity vessel)vessel.signal(0);}
            case 6 -> mc.getSoundManager().play(new SimpleSoundInstance(SoundEvents.GLASS_HIT,SoundSource.BLOCKS,.12F,1.7F,RandomSource.create(),packet.position()));
            case 7 -> {mc.gui.setTimes(15,70,30);mc.gui.setSubtitle(Component.translatable("hemomancy.antecedent.vigil.subtitle"));mc.gui.setTitle(Component.translatable("hemomancy.antecedent.vigil.title"));}
            default -> { }
        }
    }
    public static float ambience() { return ambience; }
    public static boolean agitated() {var level=Minecraft.getInstance().level;return level!=null && level.getGameTime()<Math.max(acousticUntil,signalUntil);}
    public static boolean directed() {var level=Minecraft.getInstance().level;return level!=null && level.getGameTime()<signalUntil;}
    public static BlockPos source() {return source;}
    public static int sampleFrame() {
        var mc=Minecraft.getInstance();if(mc.level==null || !agitated())return 0;
        if(!directed())return 1+(int)(mc.level.getGameTime()/5%2);
        long elapsed=mc.level.getGameTime()-signalSince;
        int frame=elapsed<20?1:elapsed<60?2:elapsed<120?3:elapsed<160?4:5;
        return frame+(sourceLeft()?5:0);
    }
    public static boolean sourceLeft() {
        var player=Minecraft.getInstance().player;if(player==null)return false;
        double angle=Math.atan2(-(source.getX()+.5-player.getX()),source.getZ()+.5-player.getZ())-Math.toRadians(player.getYRot());
        return Math.sin(angle)<0;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(mc.level!=lastLevel) {acousticUntil=signalUntil=scarUntil=0;ambience=1;epilogue=false;lastLevel=mc.level;}
        if(mc.player==null || mc.level==null) {ambience=1;return;}
        float target=1;
        for(var site:VigilSites.loaded(mc.level)) {
            var local=site.local(mc.player.blockPosition());
            target=Math.min(target,site.layout().ambience(local));
        }
        float previous=ambience;
        ambience+=Math.clamp(target-ambience,-.85F/40,.85F/40);
        if(previous!=ambience) mc.getSoundManager().updateSourceVolume(SoundSource.AMBIENT,mc.options.getSoundSourceVolume(SoundSource.AMBIENT));
        if(epilogue && mc.screen==null && !mc.player.isUsingItem()) {
            epilogue=false;
            com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen.open(AntecedentDialogue.epilogue());
            PacketHandler.sendToServer(new AntecedentEffectPacket(4,BlockPos.ZERO));
        }
    }
    @SubscribeEvent public static void render(RenderGuiEvent.Post event) {
        var mc=Minecraft.getInstance();if(mc.player==null || mc.level==null || mc.options.hideGui)return;
        var graphics=event.getGuiGraphics();
        if(mc.level.getGameTime()<scarUntil) {
            double dx=scar.getX()+.5-mc.player.getX(),dz=scar.getZ()+.5-mc.player.getZ();
            double angle=Math.atan2(-dx,dz)-Math.toRadians(mc.player.getYRot());
            int x=graphics.guiWidth()/2+(int)(Math.sin(angle)*42),y=graphics.guiHeight()/2-(int)(Math.cos(angle)*24);
            graphics.fill(x-2,y-2,x+2,y+2,0xFF72C4BE);
        }
        if(mc.screen!=null)return;
        String caption="";
        double closest=1024;
        for(var pos:ClairaudiographSounds.positions()) {
            double distance=mc.player.distanceToSqr(pos.getCenter());
            if(distance>=closest)continue;
            var view=ClairaudiographSounds.view(pos,0);
            if(view.program()!=null){caption=view.program().caption((int)(view.seconds()*20));closest=distance;}
        }
        if(!caption.isEmpty()) {
            var text=Component.translatable(caption);
            int width=Math.min(360,graphics.guiWidth()-24);
            var lines=mc.font.split(text,width);
            int y=graphics.guiHeight()-50-lines.size()*10;
            graphics.fill((graphics.guiWidth()-width)/2-4,y-3,(graphics.guiWidth()+width)/2+4,y+lines.size()*10+3,0xB0000000);
            for(var line:lines){graphics.drawString(mc.font,line,(graphics.guiWidth()-mc.font.width(line))/2,y,0xFFE0DDCF,false);y+=10;}
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {acousticUntil=signalUntil=scarUntil=0;ambience=1;epilogue=false;}
    @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        if(event.getEntity()!=null && AncientRecordings.get(event.getItemStack())==AncientRecordings.SEVERED)
            event.getToolTip().add(Component.translatable("hemomancy.antecedent.cylinder."+(com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.antecedent(event.getEntity()).has(AntecedentResearch.Evidence.SEVERED_RECORD_HEARD)?"heard":"unheard")));
    }
}
