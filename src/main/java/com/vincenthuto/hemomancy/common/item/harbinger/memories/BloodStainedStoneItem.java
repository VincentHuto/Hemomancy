package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.KnownManipulationServerPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BloodStainedStoneItem extends Item {

	public BloodStainedStoneItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		SoundSource soundcategory = entityLiving instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
		worldIn.playSound((Player) null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(),
				SoundEvents.BEACON_DEACTIVATE, soundcategory, 1.0F,
				1.0F / (worldIn.random.nextFloat() * 0.5F + 1.0F) + 0.2F);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		IBloodVolume volCap = HemoCapabilityAccess.getBloodVolume(playerIn)
				.orElseThrow(NullPointerException::new);
		IKnownManipulations manips = HemoCapabilityAccess.getKnownManipulations(playerIn)
				.orElseThrow(NullPointerException::new);

		if (volCap.isActive()) {
			if (!worldIn.isClientSide) {
				PacketHandler.sendToPlayer((ServerPlayer) playerIn, new KnownManipulationServerPacket(manips));
			} else {
			//	ClientEvents.openManipGui();
			}
		} else {
			playerIn.displayClientMessage(Component.literal("The stone feels warm in your hands..."), true);
		}
		return InteractionResultHolder.consume(stack);

	}

}
