package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.liber.LiberReadTracker;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
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

public class UnstainedBookItem extends ItemGuideBook {
    public UnstainedBookItem(Properties prop, ResourceLocation loc) {
        super(prop, loc);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.literal(ChatFormatting.AQUA + "A guide to the Unstained and their ways."));
        appendUnreadLine(tooltip);
    }

    @OnlyIn(Dist.CLIENT)
    private static void appendUnreadLine(List<Component> tooltip) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        HemoCapabilityAccess.getLiberKnowledge(mc.player).ifPresent(knowledge -> {
            int unread = LiberReadTracker.countUnread(mc.player.getUUID(), knowledge, "liberimmaculatus/");
            if (unread > 0) {
                tooltip.add(Component.literal(ChatFormatting.GOLD + "⬤ " + unread
                        + " unread entr" + (unread == 1 ? "y" : "ies")));
            }
        });
    }

//	@Override
//	public boolean hurtEnemy(ItemStack stack, LivingEntity entity, @Nonnull LivingEntity attacker) {
//		if (!(entity instanceof Player) && entity != null) {
//			double range = 8;
//			List<LivingEntity> alreadyTargetedEntities = new ArrayList<>();
//			int dmg = 5;
//			long lightningSeed = entity.level.random.nextLong();
//
//			Predicate<Entity> selector = e -> e instanceof LivingEntity && !(e instanceof Player)
//					&& !alreadyTargetedEntities.contains(e);
//
//			Random rand = new Random(lightningSeed);
//			LivingEntity lightningSource = entity;
//			int hops = entity.level.isThundering() ? 10 : 4;
//			for (int i = 0; i < hops; i++) {
//				List<Entity> entities = entity.level.getEntities(lightningSource,
//						new AABB(lightningSource.getX() - range, lightningSource.getY() - range,
//								lightningSource.getZ() - range, lightningSource.getX() + range,
//								lightningSource.getY() + range, lightningSource.getZ() + range),
//						selector);
//				if (entities.isEmpty()) {
//					break;
//				}
//
//				LivingEntity target = (LivingEntity) entities.get(rand.nextInt(entities.size()));
//				if (attacker instanceof Player player) {
//					target.hurt(this.damageSources().playerAttack(player), dmg);
//				} else {
//					target.hurt(this.damageSources().mobAttack(attacker), dmg);
//				}
//
//				HutosLib.proxy.lightningFX(VecHelper.fromEntityCenter(lightningSource),
//						VecHelper.fromEntityCenter(target), 1, 0x0179C4, 0xAADFFF);
//
//				alreadyTargetedEntities.add(target);
//				lightningSource = target;
//				dmg--;
//			}
//		}
//
//		return super.hurtEnemy(stack, entity, attacker);
//	}

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

        if (lvl.isClientSide) {
            HLGuiGuideTitlePage.openScreenViaItem(MemoBookFilter.filterForPlayer(book, p_41433_));
            HemoCapabilityAccess.getLiberKnowledge(p_41433_)
                    .ifPresent(k -> LiberReadTracker.acknowledge(p_41433_.getUUID(), k.getUnlockedEntries()));
        }

        return super.use(lvl, p_41433_, p_41434_);
    }

}
