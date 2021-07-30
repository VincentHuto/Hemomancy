package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.event.ClientTickHandler;
import com.huto.hemomancy.model.block.ModelFloatingHeart;
import com.huto.hemomancy.tile.TileEntityMortalDisplay;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class RenderMortalDisplay extends BlockEntityRenderer<TileEntityMortalDisplay> {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private final ModelFloatingHeart heart = new ModelFloatingHeart();
	public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_floating_heart.png");

	public RenderMortalDisplay(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TileEntityMortalDisplay te, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

		double ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks - 1.3 * 0.14;
		matrixStackIn.push();
		matrixStackIn.translate(0.5D, 1.2D, 0.5D);
		matrixStackIn.rotate(new Quaternion(Vector3f.XN, 180, true));
		float currentTime = te.getWorld().getGameTime() + partialTicks;
		matrixStackIn.translate(0D, (Math.sin(Math.PI * currentTime / 2 / 32) / 5) + 0.1D, 0D);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees((float) ticks / 2));
		float scale = (float) Math.abs(Math.cos(currentTime * 0.015f) * 0.25f) + 0.4f;
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.translate(0, -scale * 0.7f - 0.2f, 0);
		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer
				.getImpl(Tessellator.getInstance().getBuffer());
		IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(heart.getRenderType(texture));
		heart.render(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		irendertypebuffer$impl.finish();
		matrixStackIn.pop();

	}
}
