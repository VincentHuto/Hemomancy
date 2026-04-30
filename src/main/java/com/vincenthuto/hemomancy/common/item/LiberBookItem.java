package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.liber.LiberReadTracker;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.IBookPageFilter;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoBookFilter;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoHelper;
import com.vincenthuto.hutoslib.client.screen.guide.HLGuiGuideTitlePage;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Shared base class for Hemomancy Liber book items.
 *
 * <p>Both the Liber Sanguinum ({@link BloodyBookItem}) and Liber Immaculatus
 * ({@link UnstainedBookItem}) share identical open/close and tooltip logic;
 * only their book-registry key, tooltip description, tooltip colour, and
 * {@link LiberReadTracker} prefix differ.  Those values are supplied as
 * constructor parameters so each subclass remains a thin, one-method wrapper.
 *
 * <p>The class also implements {@link ILiberBook} so that
 * {@link MemoHelper#isLiber(ItemStack)} can identify any Liber book via an
 * {@code instanceof} check rather than hard-coded {@code ItemInit} field
 * references.
 */
public abstract class LiberBookItem extends ItemGuideBook implements ILiberBook {

    private final ResourceLocation bookRegistryKey;
    private final String readTrackerPrefix;
    private final IBookPageFilter pageFilter;

    /**
     * @param prop             standard item properties
     * @param texture          animated-book texture passed to {@link ItemGuideBook}
     * @param bookRegistryKey  resource location used to look up the book in
     *                         {@link BookPlaceboReloadListener}
     *                         (e.g. {@code Hemomancy.rloc("sanctumsanguinium")})
     * @param readTrackerPrefix path prefix used by {@link LiberReadTracker} to scope
     *                         unread-entry counting to this book
     *                         (e.g. {@code "sanctumsanguinium/"})
     */
    protected LiberBookItem(Properties prop, ResourceLocation texture,
                            ResourceLocation bookRegistryKey, String readTrackerPrefix) {
        super(prop, texture);
        this.bookRegistryKey = bookRegistryKey;
        this.readTrackerPrefix = readTrackerPrefix;
        this.pageFilter = MemoBookFilter.INSTANCE;
    }

    // ------------------------------------------------------------------
    // ILiberBook
    // ------------------------------------------------------------------

    @Override
    public String getBookPath() {
        return bookRegistryKey.getPath();
    }

    // ------------------------------------------------------------------
    // Tooltip / description helpers (subclass-supplied)
    // ------------------------------------------------------------------

    /** Short flavour description shown in the item tooltip. */
    protected abstract Component getDescription();

    /** Colour used for the "unread entries" badge line in the tooltip. */
    protected ChatFormatting getUnreadBadgeColour() {
        return ChatFormatting.GOLD;
    }

    // ------------------------------------------------------------------
    // Item behaviour
    // ------------------------------------------------------------------

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(getDescription());
        appendUnreadBadge(tooltip);
    }

    @OnlyIn(Dist.CLIENT)
    private void appendUnreadBadge(List<Component> tooltip) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        HemoCapabilityAccess.getLiberKnowledge(mc.player).ifPresent(knowledge -> {
            int unread = LiberReadTracker.countUnread(mc.player.getUUID(), knowledge, readTrackerPrefix);
            if (unread > 0) {
                tooltip.add(Component.literal(getUnreadBadgeColour() + "⬤ " + unread
                        + " unread entr" + (unread == 1 ? "y" : "ies")));
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BookCodeModel book = BookPlaceboReloadListener.INSTANCE.getBookByTitle(bookRegistryKey);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MemoHelper.migrateLegacyLiberStack(serverPlayer, player.getItemInHand(hand));
        }

        if (level.isClientSide) {
            HLGuiGuideTitlePage.openScreenViaItem(pageFilter.filter(book, player));
            HemoCapabilityAccess.getLiberKnowledge(player)
                    .ifPresent(k -> LiberReadTracker.acknowledge(player.getUUID(), k.getUnlockedEntries()));
        }

        return super.use(level, player, hand);
    }
}
