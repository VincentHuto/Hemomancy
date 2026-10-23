package com.vincenthuto.hemomancy.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * The school materials' shader render types (Lux and Umbra, Thermal, Animus and Mortem, Ferric and Ductilis, the cast
 * outpouring). Each family keeps its own {@link Clock}, set by its {@code begin} with the time its caller draws at;
 * the types read it when they are drawn.
 */
public final class SchoolRenderTypes {
	private SchoolRenderTypes() {
	}

	/** One family's shader time, in ticks wrapped to a day. */
	public static final class Clock {
		private float time;

		public void begin(double ticks) {
			time = (float) (ticks % 24000);
		}
	}

	/**
	 * A quad material on a school shader: translucent and depth-tested, or {@code solid} (opaque, writing depth, not
	 * sorted). {@code depthWrite} makes a translucent one write depth as well. {@code pace} scales the clock.
	 */
	public static RenderType create(Clock clock, String name, ShaderHolder holder, VertexFormat format, boolean solid,
			boolean depthWrite, float pace) {
		return RenderType.create(name, format, VertexFormat.Mode.QUADS, 16384, false, !solid,
				RenderType.CompositeState.builder()
						.setShaderState(holder.getShard())
						.setTexturingState(uniforms(clock, name, holder, pace, () -> {}))
						.setTransparencyState(solid ? RenderType.NO_TRANSPARENCY : RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(solid || depthWrite ? RenderType.COLOR_DEPTH_WRITE : RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	/**
	 * Sets {@code HemoTime} (the clock times {@code pace}) and a white {@code ColorModulator} before a draw, then
	 * {@code extra}; restores the shader's defaults after it.
	 */
	public static RenderStateShard.TexturingStateShard uniforms(Clock clock, String name, ShaderHolder holder,
			float pace, Runnable extra) {
		return new RenderStateShard.TexturingStateShard(name + "_uniforms", () -> {
			var shader = holder.getInstance().get();
			if (shader != null) {
				shader.safeGetUniform("HemoTime").set(clock.time * pace);
				shader.safeGetUniform("ColorModulator").set(1f, 1f, 1f, 1f);
			}
			extra.run();
		}, () -> {
			if (holder.getInstance().get() instanceof ExtendedShaderInstance shader) shader.setUniformDefaults();
		});
	}
}
