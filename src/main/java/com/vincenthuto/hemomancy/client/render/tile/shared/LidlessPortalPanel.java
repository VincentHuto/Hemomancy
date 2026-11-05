package com.vincenthuto.hemomancy.client.render.tile.shared;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.block.shared.LidlessTheme;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * The panel a portal's third eye is cut from, front and back, in the Lidless material. The pose must already face the
 * portal's front along +Z. Vertex colour carries what differs per portal: red its theme, green how open it is, blue
 * whether it is seated between a Monolith's halves. See {@code world/lidless_door_panel}.
 */
public final class LidlessPortalPanel {
	private static final float HALF_WIDTH = 1.95F;
	private static final float TOP = 3.54F;

	private LidlessPortalPanel() {
	}

	/** {@code turned} is how far the void has turned toward the Chamber's colours, 0 to 1; {@code liveTexture} a view to show, or 0. */
	public static void draw(PoseStack ms, MultiBufferSource buffers, float time, float seed, Direction facing, float opening,
			boolean seated, LidlessTheme theme, float turned, int liveTexture) {
		Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
		Vec3 up = new Vec3(0, 1, 0);
		float red = theme.ordinal() / 2.0F;
		float blue = seated ? 1.0F : 0.0F;
		// Two passes: the body hard, writing depth, then the rim's soft fringe over it. The front face looks along
		// -facing into the void, the back face along +facing; they share one rim, so neither peeks out past the other.
		for (int pass = 0; pass < 2; pass++) {
			for (int side = 0; side < 2; side++) {
				Vec3 forward = side == 0 ? normal.scale(-1) : normal;
				Vec3 right = up.cross(forward);
				float z = side == 0 ? 0.03F : -0.03F;
				float nz = side == 0 ? 1.0F : -1.0F;
				VertexConsumer vc = buffers.getBuffer(pass == 0
						? HemoRenderTypes.lidlessPortalSolid(time, seed, right, up, forward, turned, liveTexture)
						: HemoRenderTypes.lidlessPortal(time, seed, right, up, forward, turned, liveTexture));
				Matrix4f mat = ms.last().pose();
				vc.addVertex(mat, -HALF_WIDTH, 0.0F, z).setColor(red, opening, blue, 1.0F).setUv(0.0F, 0.0F).setLight(0xF000F0).setNormal(ms.last(), 0.0F, 0.0F, nz);
				vc.addVertex(mat, HALF_WIDTH, 0.0F, z).setColor(red, opening, blue, 1.0F).setUv(1.0F, 0.0F).setLight(0xF000F0).setNormal(ms.last(), 0.0F, 0.0F, nz);
				vc.addVertex(mat, HALF_WIDTH, TOP, z).setColor(red, opening, blue, 1.0F).setUv(1.0F, 1.0F).setLight(0xF000F0).setNormal(ms.last(), 0.0F, 0.0F, nz);
				vc.addVertex(mat, -HALF_WIDTH, TOP, z).setColor(red, opening, blue, 1.0F).setUv(0.0F, 1.0F).setLight(0xF000F0).setNormal(ms.last(), 0.0F, 0.0F, nz);
			}
		}
	}
}
