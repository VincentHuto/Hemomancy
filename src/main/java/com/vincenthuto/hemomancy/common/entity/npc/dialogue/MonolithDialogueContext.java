package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/** Transient server authority for the currently opened block conversation. */
public final class MonolithDialogueContext {
    private record Context(ResourceKey<Level> dimension, BlockPos pos,
            WeakReference<SanguineMonolithBlockEntity> block, Set<String> events, long expires) {}
    private static final Map<ServerPlayer, Context> OPEN = new WeakHashMap<>();
    private MonolithDialogueContext() {}

    public static void open(ServerPlayer player, BlockPos pos, DialogueTree tree) {
        clear(player);
        if (!(player.level().getBlockEntity(pos) instanceof SanguineMonolithBlockEntity block)) return;
        Set<String> events = tree.nodes().values().stream().flatMap(node -> node.options().stream())
                .map(DialogueOption::eventId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        OPEN.put(player, new Context(player.level().dimension(), pos.immutable(),
                new WeakReference<>(block), Set.copyOf(events), player.level().getGameTime() + 1200));
    }

    public static void clear(ServerPlayer player) { OPEN.remove(player); }

    public static SanguineMonolithBlockEntity takeBlock(ServerPlayer player, String eventId) {
        if (!permits(player, eventId)) return null;
        SanguineMonolithBlockEntity block = OPEN.get(player).block().get();
        clear(player);
        return block;
    }

    public static boolean permits(ServerPlayer player, String eventId) {
        Context context = OPEN.get(player);
        if (context == null || !context.events().contains(eventId)) return false;
        if (!context.dimension().equals(player.level().dimension())
                || player.level().getGameTime() > context.expires()
                || player.distanceToSqr(context.pos().getCenter()) > 64
                || !player.level().hasChunkAt(context.pos())
                || context.block().get() == null
                || player.level().getBlockEntity(context.pos()) != context.block().get()) {
            clear(player);
            return false;
        }
        return true;
    }
}
