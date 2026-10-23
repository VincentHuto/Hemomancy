package com.vincenthuto.hemomancy.common.particle.type;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

/**
 * A Hemomancy particle type: its data's command and network codecs, which may depend on the type itself (so one
 * data class can serve several types). Never forced to spawn.
 */
public final class HemoParticleType<D extends ParticleOptions> extends ParticleType<D> {
	private final MapCodec<D> codec;
	private final StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec;

	public HemoParticleType(Function<ParticleType<D>, MapCodec<D>> codec,
			Function<ParticleType<D>, StreamCodec<? super RegistryFriendlyByteBuf, D>> streamCodec) {
		super(false);
		this.codec = codec.apply(this);
		this.streamCodec = streamCodec.apply(this);
	}

	public HemoParticleType(MapCodec<D> codec, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec) {
		super(false);
		this.codec = codec;
		this.streamCodec = streamCodec;
	}

	@Override
	public MapCodec<D> codec() {
		return codec;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec() {
		return streamCodec;
	}
}
