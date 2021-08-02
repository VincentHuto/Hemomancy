package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.gui.manips.GuiChooseManip;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketKnownManipulationServer;

import net.minecraft.client.Minecraft;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemBloodStainedStone extends Item {

	public ItemBloodStainedStone(Properties prop) {
		super(prop);
		prop.stacksTo(1);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		IBloodVolume volCap = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		IKnownManipulations manips = playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		if (volCap.isActive()) {
			if (!worldIn.isClientSide) {
				BloodManipulation selected = manips.getSelectedManip();
				List<BloodManipulation> known = manips.getKnownManips();
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
						new PacketKnownManipulationServer(known, selected));
			} else {
				Minecraft.getInstance().setScreen(new GuiChooseManip(playerIn));
				playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
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
