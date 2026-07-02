package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

public record MaterialAtlasBucket(
		MaterialAtlasPath path,
		String id,
		String label,
		int color,
		int centerX,
		int centerY,
		int plaqueX,
		int plaqueY
) {
}
