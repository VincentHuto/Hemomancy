package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.client.data.LidlessPassageClientState;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** The low drone that fills the void while the local player crosses a Lidless Door; it stops when they are out. */
public final class LidlessPassageSound extends AbstractTickableSoundInstance {
	private static final float VOLUME = 0.7F;

	public LidlessPassageSound() {
		super(SoundInit.RITE_AMBIENT_DRONE.get(), SoundSource.AMBIENT, RandomSource.create());
		this.looping = true;
		this.delay = 0;
		this.relative = true;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.volume = VOLUME;
		this.pitch = 0.8F;
	}

	@Override
	public void tick() {
		if (LidlessPassageClientState.local() == null) stop();
	}
}
