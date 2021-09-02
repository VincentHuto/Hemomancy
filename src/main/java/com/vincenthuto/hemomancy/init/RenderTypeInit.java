package com.vincenthuto.hemomancy.init;

import java.util.OptionalDouble;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RenderTypeInit extends RenderType {

	public RenderTypeInit(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	@SuppressWarnings("unused")
	private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0D));

	public static RenderType getCrimsonGlint() {
		return CRIMSON_GLINT;
	}

	private static final RenderType CRIMSON_GLINT = create("glint_direct", DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS, 256, false, false,
			RenderType.CompositeState.builder().setShaderState(RENDERTYPE_GLINT_DIRECT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(
							new ResourceLocation(Hemomancy.MOD_ID, "textures/item/crimson_item_glint.png"), true,
							false))
					.setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false));

}
