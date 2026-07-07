package com.vincenthuto.hemomancy.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record LivingWeaponGraftData(LivingWeaponForm form) {
	public static final Codec<LivingWeaponGraftData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					LivingWeaponForm.CODEC.fieldOf("form").forGetter(LivingWeaponGraftData::form))
			.apply(instance, LivingWeaponGraftData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, LivingWeaponGraftData> STREAM_CODEC =
			LivingWeaponForm.STREAM_CODEC.map(LivingWeaponGraftData::new, LivingWeaponGraftData::form);

	public static Optional<LivingWeaponGraftData> fromStack(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(stack.get(DataComponentInit.LIVING_WEAPON_GRAFT_DATA.get()));
	}

	public static ItemStack createStack(LivingWeaponForm form) {
		ItemStack stack = new ItemStack(ItemInit.living_weapon_graft.get());
		stack.set(DataComponentInit.LIVING_WEAPON_GRAFT_DATA.get(), new LivingWeaponGraftData(form));
		return stack;
	}
}
