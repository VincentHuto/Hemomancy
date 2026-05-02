package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoBookFilter;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.util.GuideBookTooltipHelper;
import com.vincenthuto.hutoslib.client.screen.guide.HLGuiGuideTitlePage;
import com.vincenthuto.hutoslib.common.book.BookTheme;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.ChatFormatting;
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

public class UnstainedBookItem extends ItemGuideBook {
    public UnstainedBookItem(Properties prop, ResourceLocation loc) {
        super(prop, loc);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        GuideBookTooltipHelper.appendFilteredUnreadEntryLine(Hemomancy.rloc("liberimmaculatus"), tooltip);
        tooltip.add(Component.literal(ChatFormatting.AQUA + "A guide to the Unstained and their ways."));
    }

    public Rarity getRarity(ItemStack par1ItemStack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p_41433_, InteractionHand p_41434_) {
        BookPlaceboReloadListener test = BookPlaceboReloadListener.INSTANCE;
        BookCodeModel book = test.getBookByTitle(Hemomancy.rloc("liberimmaculatus"));

        if (!lvl.isClientSide && p_41433_ instanceof ServerPlayer serverPlayer) {
            MemoHelper.migrateLegacyLiberStack(serverPlayer, p_41433_.getItemInHand(p_41434_));
        }

        if (lvl.isClientSide && book != null) {
            BookCodeModel filtered = new MemoBookFilter().filter(book, p_41433_);
            filtered.setTheme(new BookTheme(
                    Hemomancy.rloc("textures/gui/guide/book.png"),
                    0x88AACC,
                    Hemomancy.rloc("textures/gui/guide/hemo_overlay.png")));
            HLGuiGuideTitlePage.openScreenViaItem(filtered);
        }

        return super.use(lvl, p_41433_, p_41434_);
    }

}
