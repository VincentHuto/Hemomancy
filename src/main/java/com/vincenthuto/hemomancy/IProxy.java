package com.vincenthuto.hemomancy;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec3;

public interface IProxy {

	default void lightningFX(Vec3 vectorStart, Vec3 vectorEnd, float ticksPerMeter, int colorOuter, int colorInner) {
		lightningFX(vectorStart, vectorEnd, ticksPerMeter, System.nanoTime(), colorOuter, colorInner);
	}

	default void lightningFX(Vec3 vectorStart, Vec3 vectorEnd, float ticksPerMeter, long seed, int colorOuter,
			int colorInner) {

	}
	

	default void openGui(Screen gui) {
	}
	

	default void registerHandlers() {
	}

	default void openBinderGui() {
	}

	default void openJarGui() {
	}

	default void openStaffGui() {
	}

	default void openManipGui() {

	}

	default void openGuideGui() {

	}

	default void openVeinGui() {

	}

}
