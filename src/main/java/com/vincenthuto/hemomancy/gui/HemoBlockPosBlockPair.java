package com.vincenthuto.hemomancy.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hutoslib.client.render.block.BlockPosBlockPair;
import com.vincenthuto.hutoslib.client.render.block.LabeledBlockPattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

//OnlyIn(Dist.CLIENT);
public class HemoBlockPosBlockPair {

	Block block;
	BlockPos pos;

	public HemoBlockPosBlockPair(Block block, BlockPos pos) {
		this.block = block;
		this.pos = pos;
	}

	/***
	 * This is dumb but it'll sit here until if and when i decide to bring this over
	 * to hutoslib rendering
	 * 
	 * @param pair
	 */
	public HemoBlockPosBlockPair(BlockPosBlockPair pair) {
		this.block = pair.getBlock();
		this.pos = pair.getPos();
	}

	public static List<HemoBlockPosBlockPair> getConvertedList(List<BlockPosBlockPair> pattern) {
		List<HemoBlockPosBlockPair> pair = new ArrayList<HemoBlockPosBlockPair>();

		pattern.forEach((p) -> {
			HemoBlockPosBlockPair hemo = new HemoBlockPosBlockPair(p);
			pair.add(hemo);
		});
		return pair;
	}

	public Block getBlock() {
		return block;
	}

	public BlockPos getPos() {
		return pos;
	}

	@Override
	public String toString() {
		return block.toString() + "," + pos.toString();
	}

	public void render(LabeledBlockPattern pattern, PoseStack matrices, float partialTicks, BlockAndTintGetter getter,
			MultiBufferSource src, BlockEntityRenderDispatcher d) {
		renderBlock(matrices, pattern, block.defaultBlockState(), pos.getX(), pos.getY(), pos.getZ(), getter);

	}

	private static void renderBlock(PoseStack matrices, LabeledBlockPattern pattern, BlockState state,
			double translatex, double translatey, double translatez, BlockAndTintGetter getter) {
		matrices.pushPose();
		matrices.translate(translatex - pattern.getBlockPattern().getWidth() / 2, translatey,
				translatez -pattern.getBlockPattern().getDepth() / 2);
		BlockRenderDispatcher d = Minecraft.getInstance().getBlockRenderer();
		VertexConsumer c = Minecraft.getInstance().renderBuffers().bufferSource()
				.getBuffer(ItemBlockRenderTypes.getRenderType(state, true));
		d.renderBatched(state, BlockPos.ZERO, getter, matrices, c, false, Minecraft.getInstance().level.random,
				EmptyModelData.INSTANCE);
		matrices.popPose();
	}
}