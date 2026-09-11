package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenPendingWhisperPacket;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PendingWhispers {
    private static final String KEY = "hemomancy:pending_whispers";
    private static final Map<ServerPlayer, UUID> OPEN = new WeakHashMap<>();
    private PendingWhispers() {}

    public static boolean isWhisper(DialogueTree tree) {
        return tree.entityId() == 0 && "hemomancy.whisper.speaker_name".equals(tree.speakerName());
    }

    private static ListTag inbox(ServerPlayer player) {
        return player.getPersistentData().getList(KEY, Tag.TAG_COMPOUND);
    }

    public static void enqueue(ServerPlayer player, DialogueTree tree) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        byte[] bytes;
        try {
            tree.toNetwork(buffer);
            bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
        } finally { buffer.release(); }
        ListTag list = inbox(player);
        for (Tag tag : list) {
            if (Arrays.equals(((CompoundTag) tag).getByteArray("tree"), bytes)) return;
        }
        CompoundTag entry = new CompoundTag();
        entry.putUUID("id", UUID.randomUUID());
        entry.putByteArray("tree", bytes);
        entry.putBoolean("decision", tree.nodes().values().stream().flatMap(n -> n.options().stream())
                .anyMatch(o -> "archon_choice_silence".equals(o.eventId())
                        || "archon_choice_eighth_degree".equals(o.eventId())));
        list.add(entry);
        player.getPersistentData().put(KEY, list);
        notifyPending(player);
    }

    private static void notifyPending(ServerPlayer player) {
        if (inbox(player).isEmpty() || player.connection == null) return;
        player.displayClientMessage(Component.translatable("hemomancy.whisper.pending", inbox(player).size())
                .withStyle(ChatFormatting.DARK_GREEN).append(Component.literal(" "))
                .append(Component.translatable("hemomancy.whisper.listen").withStyle(style -> style
                        .withColor(ChatFormatting.GREEN).withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hemowhisper")))), false);
    }

    public static boolean open(ServerPlayer player) {
        String busy = player.containerMenu != player.inventoryMenu ? "machine"
                : player.isUsingItem() ? "item"
                : player.getLastHurtByMob() != null && recentCombat(player.tickCount, player.getLastHurtByMobTimestamp())
                        || player.getLastHurtMob() != null && recentCombat(player.tickCount, player.getLastHurtMobTimestamp()) ? "combat"
                : CardinalRiteSavedData.get(player.serverLevel()).hasActiveRite(player.getUUID()) ? "rite" : null;
        if (busy != null) {
            player.displayClientMessage(Component.translatable("hemomancy.whisper.busy." + busy), false);
            return false;
        }
        ListTag list = inbox(player);
        if (list.isEmpty()) return false;
        CompoundTag entry = list.getCompound(0);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(entry.getByteArray("tree")));
        DialogueTree tree;
        try { tree = DialogueTree.fromNetwork(buffer); }
        finally { buffer.release(); }
        UUID id = entry.getUUID("id");
        OPEN.put(player, id);
        MonolithDialogueContext.clear(player);
        PacketDistributor.sendToPlayer(player, new OpenPendingWhisperPacket(id, tree));
        return true;
    }

    private static boolean recentCombat(int tick, int lastCombatTick) {
        long elapsed = (long) tick - lastCombatTick;
        return lastCombatTick > 0 && elapsed >= 0 && elapsed < 100;
    }

    public static void close(ServerPlayer player, UUID id) {
        if (!id.equals(OPEN.get(player))) return;
        OPEN.remove(player);
        ListTag list = inbox(player);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!id.equals(entry.getUUID("id"))) continue;
            if (!entry.getBoolean("decision") || !player.getPersistentData()
                    .getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING)) list.remove(i);
            break;
        }
        player.getPersistentData().put(KEY, list);
        notifyPending(player);
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("hemowhisper")
                .executes(context -> open(context.getSource().getPlayerOrException()) ? 1 : 0));
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) notifyPending(player);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(KEY)) event.getEntity().getPersistentData()
                .put(KEY, event.getOriginal().getPersistentData().getList(KEY, Tag.TAG_COMPOUND).copy());
    }
}
