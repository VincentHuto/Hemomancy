package com.huto.hemomancy.item.rune;

import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.RuneType;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.item.ItemStack;

public class ItemVasculariumCharm extends ItemRune implements IRune {

	public ItemVasculariumCharm(Properties properties, EnumBloodTendency tendencyIn, float deepenAmount) {
		super(properties.maxStackSize(1), tendencyIn, deepenAmount);

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		IBloodVolume volCap = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		if (volCap != null) {
			volCap.toggleActive();
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.RARE;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {

		return true;
	}

	@Override
	public RuneType getRuneType() {
		return RuneType.VASC;
	}

}
