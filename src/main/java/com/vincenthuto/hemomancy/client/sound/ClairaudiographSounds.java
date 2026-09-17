package com.vincenthuto.hemomancy.client.sound;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.ClairaudiographSoundPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.util.*;
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class ClairaudiographSounds {
    private record Key(ResourceLocation dimension,BlockPos pos) {}
    private static final Map<Key,OwnedSound> sounds=new HashMap<>();
    private static OwnedSound preview;
    public static void accept(ClairaudiographSoundPacket p) {
        var mc=Minecraft.getInstance();
        if(mc.level==null || !mc.level.dimension().location().equals(p.dimension())) return;
        var key=new Key(p.dimension(),p.pos()); var old=p.preview()?preview:sounds.get(key);
        if(p.stop()) {
            if(old!=null && old.token<=p.token()) {old.finish();sounds.remove(key);}
            if(preview!=null && preview.owner.getBlockPos().equals(p.pos())) {preview.finish();preview=null;}
            return;
        }
        if(!p.preview() && preview!=null && preview.owner.getBlockPos().equals(p.pos())) {preview.finish();preview=null;}
        if(p.preview() && sounds.containsKey(key) && mc.getSoundManager().isActive(sounds.get(key))) return;
        if(!p.preview() && old!=null && old.token>p.token()) return;
        if(!(mc.level.getBlockEntity(p.pos()) instanceof ClairaudiographBlockEntity owner)) return;
        if(old!=null && mc.getSoundManager().isActive(old)) return;
        var id=ResourceLocation.tryParse(p.sound()); if(id==null) return;
        var event=mc.getSoundManager().getSoundEvent(id);
        if(event==null || event.getWeight()==0) {
            if(mc.player!=null) mc.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.hemomancy.clairaudiograph.missing_audio"),true);
            return;
        }
        var sound=new OwnedSound(p,owner);
        if(p.preview()) preview=sound; else sounds.put(key,sound);
        mc.getSoundManager().play(sound);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        sounds.values().removeIf(s->{if(!s.valid() || !mc.getSoundManager().isActive(s)){s.finish();return true;}return false;});
        if(preview!=null && (!preview.valid() || !mc.getSoundManager().isActive(preview))) {preview.finish();preview=null;}
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        sounds.values().forEach(OwnedSound::finish);sounds.clear();if(preview!=null)preview.finish();preview=null;
    }
    private static final class OwnedSound extends AbstractTickableSoundInstance {
        private final ClairaudiographBlockEntity owner;
        private final long token;
        private final boolean preview;
        OwnedSound(ClairaudiographSoundPacket p,ClairaudiographBlockEntity owner) {
            super(SoundEvent.createFixedRangeEvent(ResourceLocation.parse(p.sound()),32),SoundSource.BLOCKS,RandomSource.create());
            this.owner=owner;token=p.token();preview=p.preview();pitch=p.pitch();volume=1;
            x=p.pos().getX()+.5;y=p.pos().getY()+.5;z=p.pos().getZ()+.5;
            // Explicit linear gain controls 32-block range independently of sound asset attenuation.
            attenuation=Attenuation.NONE;
            var player=Minecraft.getInstance().player;
            volume=preview?.65F:player==null?0:(float)Math.max(0,1-Math.sqrt(player.distanceToSqr(x,y,z))/32);
        }
        boolean valid() {
            var mc=Minecraft.getInstance();return mc.level!=null && mc.player!=null && owner.getLevel()==mc.level && !owner.isRemoved()
                && mc.level.hasChunkAt(owner.getBlockPos()) && mc.level.getBlockEntity(owner.getBlockPos())==owner
                && (preview || mc.player.distanceToSqr(x,y,z)<1024);
        }
        void finish(){stop();Minecraft.getInstance().getSoundManager().stop(this);}
        @Override public void tick(){
            if(!valid()){finish();return;}
            var mc=Minecraft.getInstance();
            volume=preview?.65F:(float)Math.max(0,1-Math.sqrt(mc.player.distanceToSqr(x,y,z))/32);
        }
    }
}
