package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.hutoslib.client.particle.ParticleColor;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemBloodyFlask extends Item {

	float amount;

	public ItemBloodyFlask(Properties prop, float amount) {
		super(prop.maxStackSize(16));
		this.amount = amount;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (!worldIn.isRemote) {
			IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			float curr = volume.getBloodVolume();
			if (curr >= volume.getMaxBloodVolume()) {
				playerIn.sendStatusMessage(new StringTextComponent("Blood Volume Full"), true);
			} else {
				volume.addBloodVolume(amount);
				for (int i = 0; i < 30; i++) {
					PacketHandler.sendBloodFlaskParticles(playerIn.getPositionVec(), ParticleColor.BLOOD, 64f,
							(RegistryKey<World>) worldIn.getDimensionKey());
				}
				PacketHandler.CHANNELBLOODVOLUME.send(
						PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
						new PacketBloodVolumeServer(volume));
				stack.shrink(1);
				playerIn.sendBreakAnimation(handIn);
			}
		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		tooltip.add(new StringTextComponent("Used to Quickly Gain " + amount + "ml of Blood"));

	}

}