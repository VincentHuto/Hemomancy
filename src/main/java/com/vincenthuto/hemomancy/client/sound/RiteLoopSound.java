package com.vincenthuto.hemomancy.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** One looping layer of a rite's sound bed; the bed steers its volume, pitch and place each tick. */
public final class RiteLoopSound extends AbstractTickableSoundInstance {
	private float targetVolume;
	private float targetPitch = 1.0F;

	public RiteLoopSound(SoundEvent event, double x, double y, double z) {
		super(event, SoundSource.AMBIENT, RandomSource.create());
		this.looping = true;
		this.delay = 0;
		// The bed steers its volume by the listener's distance itself; the engine's 16-block rolloff would cut it off.
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.volume = 0.001F;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void steer(float volume, float pitch, double x, double y, double z) {
		this.targetVolume = volume;
		this.targetPitch = pitch;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void fadeOut() {
		targetVolume = 0.0F;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	@Override
	public void tick() {
		volume += (targetVolume - volume) * 0.15F;
		pitch += (targetPitch - pitch) * 0.15F;
		if (targetVolume <= 0.001F && volume <= 0.005F) stop();
	}
}
