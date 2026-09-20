package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.antecedent.AncientRecordings;
import com.vincenthuto.hemomancy.common.network.ClairaudiographSoundPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class ClairaudiographSounds {
    private record Key(ResourceLocation dimension,BlockPos pos) {}
    private static final Map<Key,OwnedSound> sounds=new HashMap<>();
    private static OwnedSound preview;
    private ClairaudiographSounds() {}
    public static List<BlockPos> positions() { return sounds.keySet().stream().map(Key::pos).toList(); }
    public record View(SpectrogramAnalysis analysis, double seconds, float pitch, String status, AncientRecordings.Program program) {}
    public static View view(BlockPos pos, float partial) {
        var mc=Minecraft.getInstance();
        if(mc.level==null) return new View(null,0,1,"idle",null);
        var sound=sounds.get(new Key(mc.level.dimension().location(),pos));
        if(sound==null && preview!=null && preview.owner.getBlockPos().equals(pos)) sound=preview;
        if(sound==null) return new View(null,0,1,"idle",null);
        var future=sound.analysis;
        String status=future==null || !future.isDone()?"loading":future.isCompletedExceptionally()?"unavailable":"playing";
        return new View(status.equals("playing")?future.getNow(null):null,sound.seconds(partial),sound.getPitch(),status,sound.program);
    }
    public static void accept(ClairaudiographSoundPacket packet) {
        var mc=Minecraft.getInstance();
        if(mc.level==null || !mc.level.dimension().location().equals(packet.dimension())) return;
        var key=new Key(packet.dimension(),packet.pos());
        var old=packet.preview()?preview:sounds.get(key);
        if(packet.stop()) {
            if(old!=null && old.token<=packet.token()) {old.finish(); sounds.remove(key);}
            if(preview!=null && preview.owner.getBlockPos().equals(packet.pos())) {preview.finish(); preview=null;}
            return;
        }
        if(old!=null && old.token>packet.token()) return;
        if(old!=null && old.token==packet.token() && !packet.preview()) return;
        if(!(mc.level.getBlockEntity(packet.pos()) instanceof ClairaudiographBlockEntity owner)) return;
        var id=ResourceLocation.tryParse(packet.sound());
        if(id==null || mc.getSoundManager().getSoundEvent(id)==null) return;
        if(old!=null) old.finish();
        if(preview!=null) {preview.finish(); preview=null;}
        var sound=new OwnedSound(packet,owner);
        // Resolve even when muted. Playback and analysis then share this exact sound variant.
        sound.resolve(mc.getSoundManager());
        if(packet.preview()) preview=sound; else sounds.put(key,sound);
        mc.getSoundManager().play(sound);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        sounds.values().removeIf(sound->{if(!sound.valid()){sound.finish(); return true;}return false;});
        if(preview!=null && !preview.valid()) {preview.finish(); preview=null;}
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }
    public static void clear() {
        sounds.values().forEach(OwnedSound::finish); sounds.clear();
        if(preview!=null) {preview.finish(); preview=null;}
    }
    private static final class OwnedSound extends AbstractTickableSoundInstance {
        private final ClairaudiographBlockEntity owner;
        private final long token,started;
        private final boolean preview;
        private final int offset;
        private final AncientRecordings.Program program;
        private CompletableFuture<SpectrogramAnalysis> analysis;
        private WeighedSoundEvents resolved;
        private float resolvedPitch=1;
        OwnedSound(ClairaudiographSoundPacket packet,ClairaudiographBlockEntity owner) {
            super(SoundEvent.createFixedRangeEvent(ResourceLocation.parse(packet.sound()),32),SoundSource.BLOCKS,RandomSource.create(packet.token()));
            this.owner=owner; token=packet.token(); preview=packet.preview(); offset=packet.elapsed();
            program=AncientRecordings.byId(packet.program()); started=owner.getLevel().getGameTime();
            pitch=packet.pitch(); volume=1; attenuation=Attenuation.NONE;
            x=packet.pos().getX()+.5; y=packet.pos().getY()+.5; z=packet.pos().getZ()+.5;
        }
        @Override public WeighedSoundEvents resolve(SoundManager manager) {
            if(resolved==null) {
                resolved=super.resolve(manager);
                if(resolved!=null && sound!=null) {
                    resolvedPitch=Math.clamp(super.getPitch(),.5F,2F); analysis=ClairaudiographAnalysis.load(sound.getPath());
                    // Stream the resolved file so ordinary recordings also resume at the measured playhead.
                    sound=new Sound(sound.getLocation(),sound.getVolume(),sound.getPitch(),sound.getWeight(),sound.getType(),true,false,sound.getAttenuationDistance());
                }
            }
            return resolved;
        }
        @Override public float getPitch() { return resolvedPitch; }
        @Override public boolean canStartSilent() { return true; }
        double seconds(float partial) { return (owner.getLevel().getGameTime()-started+offset+partial)/20.0; }
        boolean valid() {
            var mc=Minecraft.getInstance();
            if(mc.level==null || mc.player==null || owner.getLevel()!=mc.level || owner.isRemoved()
                || !mc.level.hasChunkAt(owner.getBlockPos()) || mc.level.getBlockEntity(owner.getBlockPos())!=owner
                || mc.player.distanceToSqr(x,y,z)>=1024) return false;
            if(preview) {
                if(analysis!=null && analysis.isDone() && !analysis.isCompletedExceptionally())
                    return seconds(0)<analysis.getNow(null).frames()/(30.0*getPitch())+.2;
                return seconds(0)<120;
            }
            return mc.level.getGameTime()-started<5 || owner.playing();
        }
        @Override public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers,Sound sound,boolean looping) {
            return buffers.getStream(sound.getPath(),looping).thenApply(stream->{
                try {
                    long remaining=(long)(seconds(0)*getPitch()*stream.getFormat().getFrameRate())*stream.getFormat().getFrameSize();
                    while(remaining>0) {var skipped=stream.read((int)Math.min(65536,remaining)); if(!skipped.hasRemaining()) break; remaining-=skipped.remaining();}
                    return stream;
                } catch(java.io.IOException exception) {
                    try {stream.close();} catch(java.io.IOException suppressed) {exception.addSuppressed(suppressed);}
                    throw new java.util.concurrent.CompletionException(exception);
                }
            });
        }
        void finish() { stop(); Minecraft.getInstance().getSoundManager().stop(this); }
        @Override public void tick() {
            if(!valid()) {finish(); return;}
            var player=Minecraft.getInstance().player;
            volume=preview?.65F:(float)Math.max(0,1-Math.sqrt(player.distanceToSqr(x,y,z))/32);
        }
    }
}
