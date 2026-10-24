package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.network.chat.Component;

public record MaterialAtlasBucket(
		MaterialAtlasPath path,
		String id,
		Component label,
		Component nickname,
		int color,
		int centerX,
		int centerY,
		int plaqueX,
		int plaqueY
) {
}
