package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoBookFilter;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.item.shared.GuideBookTooltipHelper;
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
        GuideBookTooltipHelper.appendFilteredUnreadEntryLine(Hemomancy.rloc("sanctumsanguinium"), tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player player  , InteractionHand hand) {
        BookPlaceboReloadListener test = BookPlaceboReloadListener.INSTANCE;
        BookCodeModel book = test.getBookByTitle(Hemomancy.rloc("sanctumsanguinium"));

        if (!lvl.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MemoHelper.migrateLegacyLiberStack(serverPlayer, player.getItemInHand(hand));
        }

        if (lvl.isClientSide && book != null) {
            BookCodeModel filtered = new MemoBookFilter().filter(book, player);
            filtered.setTheme(new BookTheme(
                    Hemomancy.rloc("textures/gui/guide/book.png"),
                    0xAA0000,
                    Hemomancy.rloc("textures/gui/guide/hemo_overlay.png")));
            HLGuiGuideTitlePage.openScreenViaItem(filtered);
        }

        return super.use(lvl, player, hand);
    }

}
