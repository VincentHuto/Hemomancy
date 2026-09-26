package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hutoslib.client.screen.guide.BookReaderScreen;
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
            BookReaderScreen.open(book, this, null, HemoCapabilityAccess.getLiberKnowledge(player).orElse(null));
        }

        return super.use(lvl, player, hand);
    }

}
