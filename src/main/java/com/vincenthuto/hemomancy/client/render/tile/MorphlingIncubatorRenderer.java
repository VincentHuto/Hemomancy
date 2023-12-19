package com.vincenthuto.hemomancy.client.render.tile;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.event.ClientTickHandler;
import com.vincenthuto.hemomancy.common.tile.MorphlingIncubatorBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

public class MorphlingIncubatorRenderer implements BlockEntityRenderer<MorphlingIncubatorBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public MorphlingIncubatorRenderer(BlockEntityRendererProvider.Context p_173636_) {
	}

	@Override
	public void render(MorphlingIncubatorBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {

		int items = 0;
		for (int i = 1; i < te.inventory.size(); i++) {
			if (te.inventory.get(i).isEmpty()) {
				break;
			} else {
				items++;
			}
		}
		float[] angles = new float[te.inventory.size() - 1];

		float anglePer = 360F / items - 1;
		float totalAngle = 0F;
		for (int i = 1; i < angles.length; i++) {
			angles[i] = totalAngle += anglePer;
		}

		double time = te.dataAccess.get(0);

		for (int i = 0; i < te.inventory.size(); i++) {
			if (!(i > 0)) {
				ms.pushPose();
				ms.translate(0.5F, 1.55F, 0.5F);
				ms.mulPose(Vector3.YP.rotationDegrees(angles[i] + te.getLevel().getGameTime()).toMoj()); // Edit
				ms.translate(0.025F, -0.5F, 0.025F);
				ms.mulPose(Vector3.YP.rotationDegrees(90f).toMoj()); // Edit Radius Movement
				ms.translate(0D, 0.175D + i * 0.25, 0F); // Block/Item Scale
				ms.scale(0.5f, 0.5f, 0.5f);
				ItemStack stack = te.inventory.get(i);
				if (!stack.isEmpty()) {
					Vec3 loc = new Vec3(te.getBlockPos().getX() + 0.5F + 0.025F,
							te.getBlockPos().getY() + 1.55F - 0.5F + 0.175D + i * 0.25,
							te.getBlockPos().getZ() + 0.5F + 0.025F);
					te.getLevel().addParticle(ParticleTypes.MYCELIUM, (double) loc.x, (double) loc.y, (double) loc.z,
							0.0D, 0.0D, 0.0D);
					Minecraft.getInstance().getItemRenderer().renderStatic(null, stack, ItemDisplayContext.FIXED, true,
							ms, bufferIn, null, combinedLightIn, combinedOverlayIn, i);
				}
				ms.popPose();
			} else {
				ms.pushPose();
//				System.out.println("X"+i+ ": " + xMover);
//				System.out.println("Z"+i+ ": " +zMover);

				float r = 1.5f; // Radius of the circle
				float w = 1.0f; // Angular speed (radians per unit time)
				float t = (float) time * 0.125f; // Time variable
				float Cx = te.getBlockPos().getX() + 0.5f; // Center X-coordinate
				float Cy = te.getBlockPos().getY() + 1; // Center Y-coordinate
				float Cz = te.getBlockPos().getZ() + 0.5f; // Center Z-coordinate (constant in this example)
				
				//System.out.println(r);
				/*
				 * if(r>0) { r -= 8-(200-time) * 0.05f; System.out.println(r);
				 * 
				 * }
				 */


				double angle = w * t + i * Math.PI / 2;
				float x = (float) (Cx + r * Math.cos(angle));
				float y = Cy;
				float z = (float) (Cz + r * Math.sin(angle)); // Since the circle is in the XY plane, z remains constant

				Vector3f loc = new Vector3f(x, y, z);

				ms.translate(x - te.getBlockPos().getX(), y - te.getBlockPos().getY() - 0.1865f,
						z - te.getBlockPos().getZ());

				ItemStack stack = te.inventory.get(i);
				Minecraft mc = Minecraft.getInstance();
				if (!stack.isEmpty()) {

					// System.out.println(loc);
					te.getLevel().addParticle(EmberParticleFactory.createData(ParticleColor.PURPLE, 2, 0.12f, 14),
							(double) loc.x, (double) loc.y, (double) loc.z, 0.0D, 0.0D, 0.0D);
					mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, combinedLightIn,
							combinedOverlayIn, ms, bufferIn, te.getLevel(), 0);
				}
				ms.popPose();
			}

		}

	}
}
