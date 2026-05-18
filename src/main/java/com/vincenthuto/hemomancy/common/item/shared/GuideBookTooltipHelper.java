package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoBookFilter;
import com.vincenthuto.hutoslib.client.book.BookReadTracker;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GuideBookTooltipHelper {
    private GuideBookTooltipHelper() {
    }

    @OnlyIn(Dist.CLIENT)
    public static void appendFilteredUnreadEntryLine(ResourceLocation bookId, List<Component> tooltip) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int unread = countFilteredUnreadEntries(bookId, player);
        if (unread > 0) {
            tooltip.add(Component.literal(ChatFormatting.GOLD + "⬤ " + unread
                    + " unread entr" + (unread == 1 ? "y" : "ies")));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static int countFilteredUnreadEntries(ResourceLocation bookId, Player player) {
        if (player == null) {
            return 0;
        }

        BookCodeModel book = BookPlaceboReloadListener.INSTANCE.getBookByTitle(bookId);
        if (book == null) {
            return 0;
        }

        BookCodeModel filtered = new MemoBookFilter().filter(book, player);
        Set<ResourceLocation> visiblePageIds = new HashSet<>();
        if (filtered.getChapters() != null) {
            for (var chapter : filtered.getChapters()) {
                if (chapter.getPages() == null) {
                    continue;
                }
                for (var page : chapter.getPages()) {
                    if (page.getId() != null) {
                        visiblePageIds.add(page.getId());
                    }
                }
            }
        }

        return BookReadTracker.countUnread(player.getUUID(), visiblePageIds);
    }
}

