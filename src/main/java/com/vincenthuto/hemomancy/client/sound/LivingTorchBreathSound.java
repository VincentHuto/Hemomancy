package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.client.player.PlayerAnimationClientState;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/** Controlled looping breath sound with no release tail. */
public final class LivingTorchBreathSound extends AbstractTickableSoundInstance {
	private final Player caster;

	public LivingTorchBreathSound(Player caster) {
		super(SoundInit.ITEM_LIVING_TORCH_BREATH_LOOP.get(), SoundSource.PLAYERS, RandomSource.create());
		this.caster = caster;
		this.looping = true;
		this.delay = 0;
		this.volume = 0.72F;
		this.pitch = 0.86F;
		updatePosition();
	}

	@Override
	public void tick() {
		if (!caster.isAlive() || caster.isRemoved() || !PlayerAnimationClientState.isBreathing(caster)) {
			forceStop();
			return;
		}
		updatePosition();
	}

	public void forceStop() {
		stop();
	}

	private void updatePosition() {
		x = caster.getX();
		y = caster.getEyeY();
		z = caster.getZ();
	}
}
