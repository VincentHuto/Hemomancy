package com.vincenthuto.hemomancy.item.memories;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.util.TextUtils;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.PacketKnownManipulationServer;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.RegistryObject;

public class ItemBloodMemory extends Item {

	RegistryObject<BloodManipulation> manip;

	public ItemBloodMemory(Properties properties, RegistryObject<BloodManipulation> manip) {
		super(properties.stacksTo(1));
		this.manip = manip;
	}

	public BloodManipulation getManip() {
		return manip.get();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return new TextComponent(
				HLTextUtils.stringToBloody(HLTextUtils.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (getManip() != null) {
			tooltip.add(new TextComponent(getManip().getProperName()));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		IKnownManipulations known = playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		LinkedHashMap<BloodManipulation, ManipLevel> knownList = known.getKnownManips();

		if (handIn == InteractionHand.MAIN_HAND) {
			ItemStack stack = playerIn.getItemInHand(handIn);
			if (!worldIn.isClientSide) {
				if (volume.isActive()) {
					if (!playerIn.isShiftKeyDown()) {
						if (!known.doesListContainName(knownList, getManip())) {
							knownList.put(getManip(), ManipLevel.BLANK);
							PacketHandler.CHANNELKNOWNMANIPS.send(
									PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
									new PacketKnownManipulationServer(known));
							stack.shrink(1);
						} else {
							playerIn.displayClientMessage(new TextComponent("Player Already Knowns This Manipulation!")
									.withStyle(ChatFormatting.DARK_RED), true);
						}
					}
				} else {
					playerIn.displayClientMessage(
							new TextComponent("You lack understanding of what your even holding...")
									.withStyle(ChatFormatting.DARK_RED),
							true);
				}
			}
		}
		return super.use(worldIn, playerIn, handIn);

	}
}