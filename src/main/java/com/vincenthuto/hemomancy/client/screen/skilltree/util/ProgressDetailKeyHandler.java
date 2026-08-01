package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

/** Routes the shared progress-screen detail-window close shortcut. */
public final class ProgressDetailKeyHandler {
	private ProgressDetailKeyHandler() {}

	public static boolean handle(int keyCode, BooleanSupplier closeDetails) {
		return keyCode == GLFW.GLFW_KEY_E && closeDetails.getAsBoolean();
	}
}
