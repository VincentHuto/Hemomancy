package com.vincenthuto.hemomancy.item.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.PacketKnownManipulationServer;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemVeinRecaller extends Item implements IDispellable {

	public ItemVeinRecaller(Properties prop) {
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
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
						new PacketKnownManipulationServer(manips));
			} else {
				Hemomancy.proxy.openVeinGui();
				
			}
		} else {
			playerIn.displayClientMessage(new TextComponent("The stone feels warm in your hands..."), true);
		}
		return InteractionResultHolder.consume(stack);

	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

}
