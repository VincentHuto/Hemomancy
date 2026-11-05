package com.vincenthuto.hemomancy.common.item.harbinger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Whose a Qliphoth Seed is: the Archon who took it out of the void. A bound Seed can be carried or handed around, but
 * only its owner can seat it in a rite. A Seed with no binding (Creative, commands) answers to anyone.
 */
public record SeedBinding(UUID owner, String name) {
	public static final Codec<SeedBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("owner").forGetter(SeedBinding::owner),
			Codec.STRING.fieldOf("name").forGetter(SeedBinding::name)).apply(instance, SeedBinding::new));
	public static final StreamCodec<ByteBuf, SeedBinding> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, SeedBinding::owner, ByteBufCodecs.STRING_UTF8, SeedBinding::name, SeedBinding::new);

	public static void bind(ItemStack seed, UUID owner, String name) {
		seed.set(DataComponentInit.SEED_BINDING.get(), new SeedBinding(owner, name));
	}

	/** Why this player may not use this stack in a rite, or null when they may. */
	@Nullable
	public static Component refusal(ItemStack stack, Player player) {
		SeedBinding binding = stack.get(DataComponentInit.SEED_BINDING.get());
		if (binding == null || binding.owner().equals(player.getUUID())) return null;
		return Component.translatable("message.hemomancy.qliphoth_seed.not_yours", binding.name());
	}
}
