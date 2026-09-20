package com.vincenthuto.hemomancy.common.succession;

import com.google.gson.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ProgressionDialogueNpc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import java.util.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SuccessionProfessions extends SimplePreparableReloadListener<Map<String, Set<ResourceLocation>>> {
    public static final List<String> ROLES = List.of("alchemist", "mnemonist", "artificer", "cicatrix_anchorite", "vicar");
    private static Map<String, Set<ResourceLocation>> workplaces = Map.of();
    public static String profession(Entity entity) {
        if (entity instanceof ProgressionDialogueNpc npc && ROLES.contains(npc.progressionDialogueId())) return npc.progressionDialogueId();
        return "";
    }
    public static ResourceLocation entityType(String role) { return Hemomancy.rloc("harbinger_" + role); }
    public static boolean accepts(String role, BlockState state) {
        return workplaces.getOrDefault(role, Set.of()).contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }
    @SubscribeEvent public static void reload(AddReloadListenerEvent event) { event.addListener(new SuccessionProfessions()); }
    @Override protected Map<String, Set<ResourceLocation>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, Set<ResourceLocation>> result = new HashMap<>();
        for (String role : ROLES) {
            var id = Hemomancy.rloc("succession_profession/" + role + ".json");
            manager.getResource(id).ifPresent(resource -> {
                try (var reader = resource.openAsReader()) {
                    var values = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("workplaces");
                    var blocks = new HashSet<ResourceLocation>();
                    for (var value : values) blocks.add(ResourceLocation.parse(value.getAsString()));
                    result.put(role, Set.copyOf(blocks));
                } catch (Exception exception) { throw new IllegalArgumentException("Invalid succession profession " + id, exception); }
            });
        }
        return Map.copyOf(result);
    }
    @Override protected void apply(Map<String, Set<ResourceLocation>> result, ResourceManager manager, ProfilerFiller profiler) { workplaces = result; }
}
