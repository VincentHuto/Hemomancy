package com.vincenthuto.hemomancy.item.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.PacketKnownManipulationServer;

import net.minecraft.network.chat.TextComponent;
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
import net.minecraftforge.network.PacketDistributor;

public class ItemBloodStainedStone extends Item {

	public ItemBloodStainedStone(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		IBloodVolume volCap = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		IKnownManipulations manips = playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);

		if (volCap.isActive()) {
			if (!worldIn.isClientSide) {
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
						new PacketKnownManipulationServer(manips));
			} else {
				Hemomancy.proxy.openManipGui();
			}
		} else {
			playerIn.displayClientMessage(new TextComponent("The stone feels warm in your hands..."), true);
		}
		return InteractionResultHolder.consume(stack);

	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		SoundSource soundcategory = entityLiving instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
		worldIn.playSound((Player) null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(),
				SoundEvents.BEACON_DEACTIVATE, soundcategory, 1.0F,
				1.0F / (worldIn.random.nextFloat() * 0.5F + 1.0F) + 0.2F);
	}

}
