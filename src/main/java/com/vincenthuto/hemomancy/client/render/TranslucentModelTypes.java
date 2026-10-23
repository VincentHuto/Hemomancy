package com.vincenthuto.hemomancy.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;

/** A model's render type drawn with the translucent entity shader and sorted, held per base type. */
public final class TranslucentModelTypes {
	private static final SeededRenderTypes<RenderType, Boolean, RenderType> TYPES =
			new SeededRenderTypes<>(64, (renderType, values) -> new RenderType(renderType.toString() + "_translucent",
					renderType.format(), renderType.mode(), renderType.bufferSize(), renderType.affectsCrumbling(), true,
					() -> {
						renderType.setupRenderState();
						RenderSystem.setShader(GameRenderer::getRendertypeEntityTranslucentShader);
					},
					() -> renderType.clearRenderState()) {
			});

	private TranslucentModelTypes() {
	}

	public static RenderType translucent(RenderType renderType) {
		return TYPES.get(renderType, Boolean.TRUE);
	}
}
