package com.vincenthuto.hemomancy.common.succession;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ProgressionDialogueNpc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import java.util.*;

/** Each Harbinger profession's workplace blocks, from {@code hemomancy:succession_profession/<role>.json}. */
public final class SuccessionProfessions extends JsonResourceReloadListener<List<ResourceLocation>> {
    private static final Codec<List<ResourceLocation>> CODEC = ResourceLocation.CODEC.listOf().fieldOf("workplaces").codec();
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
    public SuccessionProfessions() {
        super("succession_profession", "succession profession", LogManager.getLogger(), CODEC, SuccessionProfessions::publish);
    }
    private static void publish(Map<ResourceLocation, List<ResourceLocation>> loaded) {
        Map<String, Set<ResourceLocation>> result = new HashMap<>();
        for (String role : ROLES) {
            List<ResourceLocation> blocks = loaded.get(Hemomancy.rloc(role));
            if (blocks != null) result.put(role, Set.copyOf(blocks));
        }
        workplaces = Map.copyOf(result);
    }
}
