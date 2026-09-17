package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.google.gson.*;
import com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ClairaudiographCatalogue extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {
    public record Choice(ClairaudiographRecording recording, int interval) {}
    private static Map<String, List<Choice>> entries = Map.of();
    private static long revision;
    public static long revision() { return revision; }
    public static List<Choice> choices(String source) { return entries.getOrDefault(source, List.of()); }
    public static Choice allowed(ClairaudiographRecording recording) {
        if (recording == null || !recording.readable() || !BuiltInRegistries.SOUND_EVENT.containsKey(ResourceLocation.parse(recording.sound()))) return null;
        return choices(recording.source()).stream().filter(c -> c.recording.equals(recording)).findFirst().orElse(null);
    }
    @Override protected Map<ResourceLocation, JsonObject> prepare(ResourceManager manager, ProfilerFiller profiler) {
        var result = new TreeMap<ResourceLocation, JsonObject>(Comparator.comparing(ResourceLocation::toString));
        manager.listResources("clairaudiograph_sounds", id -> id.getPath().endsWith(".json")).forEach((id,resource) -> {
            try(var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) { result.put(id, JsonParser.parseReader(reader).getAsJsonObject()); }
            catch(Exception e) { LogManager.getLogger().warn("Clairaudiograph {}: {}", id, e.getMessage()); }
        });
        return result;
    }
    public static List<Choice> parse(JsonObject json) {
        String source = json.get("entity").getAsString();
        if (!source.contains(":") || ResourceLocation.tryParse(source) == null) throw new IllegalArgumentException("Invalid source ID");
        var result = new ArrayList<Choice>();
        var seen = new HashSet<ClairaudiographRecording>();
        for(var element : json.getAsJsonArray("sounds")) {
            var j = element.getAsJsonObject();
            var recording = new ClairaudiographRecording(source, j.get("sound").getAsString(), j.get("kind").getAsString(), j.has("pitch") ? j.get("pitch").getAsFloat() : 1F);
            double rawInterval = j.has("replay_interval_ticks") ? j.get("replay_interval_ticks").getAsDouble() : 100;
            if (!Double.isFinite(rawInterval) || rawInterval != Math.rint(rawInterval)) throw new IllegalArgumentException("Interval must be an integer");
            int interval = (int) rawInterval;
            if(!recording.readable() || interval < 20 || interval > 12000 || !seen.add(recording)) throw new IllegalArgumentException("Invalid or duplicate sound choice");
            result.add(new Choice(recording, interval));
        }
        if(result.size() > 64) throw new IllegalArgumentException("At most 64 choices per creature");
        result.sort(Comparator.comparing((Choice c) -> c.recording.kind()).thenComparing(c -> c.recording.sound()));
        return List.copyOf(result);
    }
    @Override protected void apply(Map<ResourceLocation, JsonObject> prepared, ResourceManager manager, ProfilerFiller profiler) {
        var next = new HashMap<String, List<Choice>>();
        var claimed = new HashSet<String>();
        // Lexically first resource wins; a duplicate cannot depend on resource iteration order.
        prepared.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString))).forEach(entry -> {
            try {
                var choices = parse(entry.getValue());
                String source = entry.getValue().get("entity").getAsString();
                if(!claimed.add(source)) throw new IllegalArgumentException("Duplicate entity; lexically first resource wins: " + source);
                var available = choices.stream().filter(c -> BuiltInRegistries.SOUND_EVENT.containsKey(ResourceLocation.parse(c.recording.sound()))).toList();
                if(available.size() != choices.size()) LogManager.getLogger().warn("Clairaudiograph {}: missing sound events omitted", entry.getKey());
                next.put(source, available);
            } catch(Exception e) { LogManager.getLogger().warn("Clairaudiograph {}: {}", entry.getKey(), e.getMessage()); }
        });
        entries = Map.copyOf(next);
        revision++;
    }
}
