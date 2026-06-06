package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hutoslib.client.screen.guide.HLGuiGuideTitlePage;
import com.vincenthuto.hutoslib.common.book.BookTheme;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class BloodyBookItem extends ItemGuideBook {
    public BloodyBookItem(Properties prop, ResourceLocation loc) {
        super(prop, loc);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player player  , InteractionHand hand) {
        BookPlaceboReloadListener test = BookPlaceboReloadListener.INSTANCE;
        BookCodeModel book = test.getBookByTitle(Hemomancy.rloc("fanesanguinium"));

        if (!lvl.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MemoHelper.migrateLegacyLiberStack(serverPlayer, player.getItemInHand(hand));
        }

        if (lvl.isClientSide && book != null) {
            final BookCodeModel rawBook = book;
            final BookTheme theme = new BookTheme(
                    Hemomancy.rloc("textures/gui/guide/book.png"),
                    0xAA0000,
                    Hemomancy.rloc("textures/gui/guide/hemo_overlay.png"));
            BookCodeModel filtered = applyVisibilityFilters(rawBook, player);
            filtered.setTheme(theme);
            // Refresher: re-runs visibility filters against the player's current
            // knowledge, so HLGuiGuideTitlePage.refreshIfOpen() can rebuild the
            // visible chapter list when a sync packet arrives while the book is
            // open. Recomputed lazily so it picks up the freshest capability state.
            java.util.function.Supplier<BookCodeModel> refresher = () -> {
                BookCodeModel r = applyVisibilityFilters(rawBook, player);
                r.setTheme(theme);
                return r;
            };
            HLGuiGuideTitlePage.openScreen(filtered, null, player.getUUID(),
                    HemoCapabilityAccess.getLiberKnowledge(player).orElse(null),
                    refresher);
        }

        return super.use(lvl, player, hand);
    }

}
