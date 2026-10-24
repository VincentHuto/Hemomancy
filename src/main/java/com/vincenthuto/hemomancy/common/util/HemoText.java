package com.vincenthuto.hemomancy.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Translated text with arguments. A translatable component can only send components, numbers, booleans and strings to
 * the client; anything else ({@code BlockPos}, {@code UUID}, an enum) fails to encode. This passes those four through and
 * sends any other argument as its {@code String.valueOf}, the same text string concatenation produced.
 */
public final class HemoText {
	private HemoText() {
	}

	public static MutableComponent translatable(String key, Object... args) {
		Object[] sendable = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			sendable[i] = arg instanceof Component || arg instanceof Number || arg instanceof Boolean || arg instanceof String
					? arg : String.valueOf(arg);
		}
		return Component.translatable(key, sendable);
	}
}
