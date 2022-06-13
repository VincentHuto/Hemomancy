package com.vincenthuto.hemomancy.item.memories;

import java.util.List;

import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemHematicMemory extends Item {

	public ItemHematicMemory(Properties properties) {
		super(properties.stacksTo(64));
	}

	@Override
	public Component getName(ItemStack stack) {
		return  Component.translatable(
				HLTextUtils.stringToBloody(HLTextUtils.convertInitToLang(ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath()))).withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add( Component.translatable("Used to recall ancient whispers"));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
//		IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
//				.orElseThrow(NullPointerException::new);
//		if (!worldIn.isClientSide) {
//			volume.setActive(!volume.isActive());
//			PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
//					new PacketBloodVolumeServer(volume));
//		}
		return super.use(worldIn, playerIn, handIn);
	}

}