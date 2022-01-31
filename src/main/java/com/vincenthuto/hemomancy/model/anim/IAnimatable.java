package com.vincenthuto.hemomancy.model.anim;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.capa.block.vein.IEarthenVeinLoc;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulations;
import com.vincenthuto.hemomancy.capa.player.rune.IRune;
import com.vincenthuto.hemomancy.capa.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public interface IAnimatable {
	Animation NO_ANIMATION = new Animation(0);

	int getAnimationTick();

	void setAnimationTick(int tick);

	Animation getAnimation();

	void setAnimation(Animation animation);

	Animation[] getAnimations();

	default boolean noAnimations() {
		return getAnimation() == NO_ANIMATION;
	}

	default void updateAnimations() {
		
		Animation current = getAnimation();
		if (current != NO_ANIMATION) {
			int tick = getAnimationTick();
			current.tick(this, tick);
			if (++tick >= current.getDuration()) {
				setAnimation(NO_ANIMATION);
				tick = 0;
			}
			setAnimationTick(tick);
		}
	}


	class CapImpl implements IAnimatable {

		public static final Capability<IAnimatable> CAPABILITY = CapabilityManager
				.get(new CapabilityToken<IAnimatable>() {
				});;

		private int animationTick = 0;
		private Animation animation;

		@Override
		public int getAnimationTick() {
			return animationTick;
		}

		@Override
		public void setAnimationTick(int tick) {
			this.animationTick = tick;
		}

		@Override
		public Animation getAnimation() {
			return animation;
		}

		@Override
		public void setAnimation(Animation animation) {
			this.animation = animation;
		}

		@Override
		public Animation[] getAnimations() {
			return new Animation[0];
		}
	}
}