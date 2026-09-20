package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.*;

@EventBusSubscriber(modid=Hemomancy.MOD_ID, value=Dist.CLIENT, bus=EventBusSubscriber.Bus.MOD)
public final class ClairaudiographAnalysis {
    private static final Map<ResourceLocation,CompletableFuture<SpectrogramAnalysis>> CACHE = new LinkedHashMap<>(32,.75F,true);
    private static final ExecutorService DECODER = Executors.newSingleThreadExecutor(r -> {var t=new Thread(r,"Hemomancy audio analysis"); t.setDaemon(true); return t;});
    private ClairaudiographAnalysis() {}
    public static synchronized CompletableFuture<SpectrogramAnalysis> load(ResourceLocation file) {
        if (CACHE.containsKey(file)) return CACHE.get(file);
        var resources = Minecraft.getInstance().getResourceManager();
        var future = CompletableFuture.supplyAsync(()->decode(resources,file),DECODER);
        CACHE.put(file,future);
        while(CACHE.size()>24) CACHE.remove(CACHE.keySet().iterator().next()).cancel(false);
        return future;
    }
    @SubscribeEvent public static void reloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resources -> clear());
    }
    private static synchronized void clear() { CACHE.values().forEach(future->future.cancel(false));CACHE.clear(); ClairaudiographSounds.clear(); }
    private static SpectrogramAnalysis decode(ResourceManager resources, ResourceLocation file) {
        try (var stream = new JOrbisAudioStream(resources.open(file))) {
            var format=stream.getFormat();
            if(format.getSampleSizeInBits()!=16) throw new java.io.IOException("Unsupported PCM format: " + format);
            int rate=(int)format.getSampleRate(), channels=format.getChannels(), size=0;
            float[][] pcm=new float[channels][rate*4];
            while(true) {
                var buffer=stream.read(65536).order(format.isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN);
                if(!buffer.hasRemaining()) break;
                while(buffer.remaining()>=channels*2) {
                    if(size>=rate*120) throw new java.io.IOException("Analysis clip exceeds 120 seconds");
                    if(size==pcm[0].length) for(int channel=0;channel<channels;channel++)pcm[channel]=Arrays.copyOf(pcm[channel],Math.min(rate*120,pcm[channel].length*2));
                    for(int channel=0;channel<channels;channel++)pcm[channel][size]=buffer.getShort()/32768F;
                    size++;
                }
            }
            for(int channel=0;channel<channels;channel++)pcm[channel]=Arrays.copyOf(pcm[channel],size);
            return SpectrogramAnalysis.analyzeChannels(pcm,rate);
        } catch(Exception exception) { throw new CompletionException("Cannot analyze " + file,exception); }
    }
}
