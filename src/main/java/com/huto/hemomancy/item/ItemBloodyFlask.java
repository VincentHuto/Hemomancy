package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemBloodyFlask extends Item {

	float amount;

	public ItemBloodyFlask(Properties prop, float amount) {
		super(prop.stacksTo(16));
		this.amount = amount;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (!worldIn.isClientSide) {
			IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			float curr = volume.getBloodVolume();
			if (curr >= volume.getMaxBloodVolume()) {
				playerIn.displayClientMessage(new TextComponent("Blood Volume Full"), true);
			} else {
				volume.addBloodVolume(amount);
				for (int i = 0; i < 30; i++) {
					PacketHandler.sendBloodFlaskParticles(playerIn.position(), ParticleColor.BLOOD, 64f,
							worldIn.dimension());
				}
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
						new PacketBloodVolumeServer(volume));
				stack.shrink(1);
				playerIn.broadcastBreakEvent(handIn);
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		// TODO Auto-generated method stub
		tooltip.add(new TextComponent("Used to Quickly Gain " + amount + "ml of Blood"));
	}

	public float getAmount() {
		return amount;
	}

}