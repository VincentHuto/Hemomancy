package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/** Transient server authority for the currently opened block conversation. */
public final class MonolithDialogueContext {
    /** How long a conversation stays open: a hub is read and traded in, not answered at once. */
    public static final int CONVERSATION_TICKS = 6000;

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
                new WeakReference<>(block), Set.copyOf(events), player.level().getGameTime() + CONVERSATION_TICKS));
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
        return context != null && context.events().contains(eventId) && speakingTo(player) != null;
    }

    /**
     * The stone this player's open conversation is on, while it still stands in reach. A stale conversation ends here;
     * a live one stays open another {@link #CONVERSATION_TICKS}.
     */
    @Nullable
    public static SanguineMonolithBlockEntity speakingTo(ServerPlayer player) {
        Context context = OPEN.get(player);
        if (context == null) return null;
        if (!context.dimension().equals(player.level().dimension())
                || player.level().getGameTime() > context.expires()
                || player.distanceToSqr(context.pos().getCenter()) > 64
                || !player.level().hasChunkAt(context.pos())
                || context.block().get() == null
                || player.level().getBlockEntity(context.pos()) != context.block().get()) {
            clear(player);
            return null;
        }
        OPEN.put(player, new Context(context.dimension(), context.pos(), context.block(), context.events(),
                player.level().getGameTime() + CONVERSATION_TICKS));
        return context.block().get();
    }
}
