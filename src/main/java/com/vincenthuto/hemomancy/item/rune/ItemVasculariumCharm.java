package com.vincenthuto.hemomancy.item.rune;

import com.vincenthuto.hemomancy.capa.player.rune.IRune;
import com.vincenthuto.hemomancy.capa.player.rune.RuneType;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemVasculariumCharm extends ItemRune implements IRune {

	public ItemVasculariumCharm(Properties properties, EnumBloodTendency tendencyIn, float deepenAmount) {
		super(properties.stacksTo(1), tendencyIn, deepenAmount);

	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		IBloodVolume volCap = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);

		if (!worldIn.isClientSide) {
			if (volCap != null) {
				volCap.toggleActive();
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
						new PacketBloodVolumeServer(volCap));
			}
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.RARE;
	}

	@Override
	public boolean isFoil(ItemStack stack) {

		return true;
	}

	@Override
	public RuneType getRuneType() {
		return RuneType.VASC;
	}

}
