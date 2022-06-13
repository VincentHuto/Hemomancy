package com.vincenthuto.hemomancy.item.memories;

import java.util.List;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.PacketKnownManipulationServer;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.network.PacketDistributor;

public class ItemLethianDew extends Item {

	public ItemLethianDew(Item.Properties p_42979_) {
		super(p_42979_.stacksTo(16));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack p_42984_, Level p_42985_, LivingEntity p_42986_) {
		Player player = p_42986_ instanceof Player ? (Player) p_42986_ : null;
		if (player instanceof ServerPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, p_42984_);
		}

		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);

		if (known.getKnownManips().containsKey(known.getSelectedManip())) {
			known.getKnownManips().remove(known.getSelectedManip());
		}
		known.setSelectedManip(BloodManipulation.BLANK);

		if (!p_42985_.isClientSide) {
			PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
					new PacketKnownManipulationServer(known));
		}

		if (player != null) {
			player.awardStat(Stats.ITEM_USED.get(this));
			if (!player.getAbilities().instabuild) {
				p_42984_.shrink(1);
			}
		}

		p_42985_.gameEvent(p_42986_, GameEvent.DRINK, p_42986_.position());
		return p_42984_;
	}

	@Override
	public int getUseDuration(ItemStack p_43001_) {
		return 16;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack p_42997_) {
		return UseAnim.DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level p_42993_, Player p_42994_, InteractionHand p_42995_) {
		return ItemUtils.startUsingInstantly(p_42993_, p_42994_, p_42995_);
	}

	@Override
	public boolean isFoil(ItemStack p_42999_) {
		return super.isFoil(p_42999_) || !PotionUtils.getMobEffects(p_42999_).isEmpty();
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add( Component.translatable("Highly concentrated lethian dew"));
		tooltip.add( Component.translatable("Used to forget your selected manipulation"));
		tooltip.add( Component.translatable("\"Just a drop is all it takes...\""));
	}

}