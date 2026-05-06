package com.vincenthuto.hemomancy.common.item.harbinger.memories;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class HematicMemoryItem extends Item {

	public HematicMemoryItem(Properties properties) {
		super(properties.stacksTo(64));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Used to recall ancient whispers"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return Component
				.literal(HLTextUtils.stringToBloody(
						HLTextUtils.convertInitToLang(HLTextUtils.getItemRegistryName(stack.getItem()))))
				.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
//		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn)
//				.orElseThrow(NullPointerException::new);
//		if (!worldIn.isClientSide) {
//			volume.setActive(!volume.isActive());
//			PacketHandler.sendToPlayer((ServerPlayer) playerIn, //					new PacketBloodVolumeServer(volume));
//		}
		return super.use(worldIn, playerIn, handIn);
	}

}