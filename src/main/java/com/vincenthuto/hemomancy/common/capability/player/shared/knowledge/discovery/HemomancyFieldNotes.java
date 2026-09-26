package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hutoslib.common.book.FieldNotes;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Adapts Hemomancy knowledge and dictation costs to the shared field-notes UI. */
public final class HemomancyFieldNotes implements FieldNotes.Provider {
    @Override public boolean available(Player player) {
        var books = BookPlaceboReloadListener.INSTANCE;
        return books.getBookByTitle(ResourceLocation.parse("hemomancy:fanesanguinium")) != null
                || books.getBookByTitle(ResourceLocation.parse("hemomancy:liberimmaculatus")) != null;
    }
    @Override public int pending(Player player) {
        return player.getData(HemoAttachmentTypes.LIBER_KNOWLEDGE).getPendingMemos().size();
    }
    @Override public List<Component> tooltip(Player player) {
        var knowledge = player.getData(HemoAttachmentTypes.LIBER_KNOWLEDGE);
        return List.of(
                Component.translatable("screen.hemomancy.virtual_field_notes.harbinger", knowledge.countPendingMemosByPath(MemoDefinition.MemoPath.HARBINGER)).withStyle(ChatFormatting.RED),
                Component.translatable("screen.hemomancy.virtual_field_notes.unstained", knowledge.countPendingMemosByPath(MemoDefinition.MemoPath.UNSTAINED)).withStyle(ChatFormatting.AQUA),
                Component.translatable("screen.hemomancy.virtual_field_notes.shared", knowledge.countPendingMemosByPath(MemoDefinition.MemoPath.SHARED)),
                Component.translatable("screen.hemomancy.virtual_field_notes.hint").withStyle(ChatFormatting.GRAY));
    }
    @Override public boolean accepts(ItemStack book) { return MemoHelper.isLiber(book); }
    @Override public boolean acceptsDesk(net.minecraft.world.level.block.Block desk) {
        return desk instanceof com.vincenthuto.hemomancy.common.block.harbinger.decoration.HarbingerEscritoireBlock;
    }
    @Override public boolean canDictate(Player player, ItemStack book) { return MemoHelper.hasPendingForLiber(player, book); }
    @Override public void dictate(ServerPlayer player, ItemStack book) { MemoHelper.dictatePendingToLiber(player, book); }
}
