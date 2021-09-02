package com.vincenthuto.hemomancy.capa.volume;
/*
 * package com.huto.hemomancy.capa.volume;
 * 
 * import com.huto.hemomancy.init.PotionInit; import
 * com.huto.hemomancy.init.RenderTypeInit; import com.hutoslib.math.Vector3;
 * import com.mojang.blaze3d.vertex.PoseStack; import
 * com.mojang.blaze3d.vertex.VertexConsumer; import com.mojang.math.Matrix4f;
 * import com.mojang.math.Vector3d; import com.mojang.math.Vector3f;
 * 
 * import net.minecraft.client.Minecraft; import
 * net.minecraft.client.renderer.IRenderTypeBuffer; import
 * net.minecraft.client.renderer.texture.OverlayTexture; import
 * net.minecraft.world.entity.player.Player; import
 * net.minecraftforge.client.event.RenderWorldLastEvent;
 * 
 * public class RenderBloodLaser {
 * 
 * public static void renderLaser(RenderWorldLastEvent event, Player player,
 * float ticks) { Vector3 centerVec = Vector3.fromEntityCenter(player); if
 * (player.getEffect(PotionInit.blood_binding.get()) != null) { if
 * (player.level.isClientSide) { Vector3 playerPos =
 * Vector3.fromEntityCenter(player); Vector3d playerVec = new
 * Vector3d(playerPos.x, playerPos.y, playerPos.z); Vector3d part1 = new
 * Vector3d(centerVec.x + Math.sin(player.tickCount * 0.1 + Math.toRadians(30)),
 * centerVec.y, centerVec.z + Math.cos(player.tickCount * 0.1 +
 * Math.toRadians(30))); Vector3d part2 = new Vector3d(centerVec.x -
 * Math.sin(player.tickCount * 0.1 + Math.toRadians(90)), centerVec.y,
 * centerVec.z - Math.cos(player.tickCount * 0.1 + Math.toRadians(90)));
 * Vector3d part3 = new Vector3d(centerVec.x - (Math.sin(player.tickCount * 0.1
 * + Math.toRadians(-30))), centerVec.y, centerVec.z -
 * (Math.cos(player.tickCount * 0.1 + Math.toRadians(-30))));
 * 
 * drawLasers(event.getMatrixStack(), playerVec, part1, 255 / 255f, 255 / 255f,
 * 0); drawLasers(event.getMatrixStack(), playerVec, part2, 255 / 255f, 0 /
 * 255f, 0); drawLasers(event.getMatrixStack(), playerVec, part3, 255 / 255f, 0
 * / 255f, 255 / 255);
 * 
 * } } }
 * 
 * public static void drawLasers(PoseStack matrixStackIn, Vector3d to, Vector3d
 * from, float r, float g, float b) { final Minecraft mc =
 * Minecraft.getInstance(); Level world = mc.level; IRenderTypeBuffer.Impl
 * buffer = Minecraft.getInstance().renderBuffers().bufferSource(); long
 * gameTime = world.getGameTime(); double v = gameTime * 0.04; Vector3d view =
 * mc.gameRenderer.getMainCamera().getPosition(); matrixStackIn.pushPose();
 * matrixStackIn.translate(-view.x(), -view.y(), -view.z()); VertexConsumer
 * builder = buffer.getBuffer(RenderTypeInit.LASER_MAIN_ADDITIVE);
 * matrixStackIn.pushPose(); matrixStackIn.translate(from.x(), from.y(),
 * from.z()); float diffX = (float) (to.x() - from.x()); float diffY = (float)
 * (to.y() - from.y()); float diffZ = (float) (to.z() - from.z()); Vector3f
 * startLaser = new Vector3f(0, 0, 0); Vector3f endLaser = new Vector3f(diffX,
 * diffY, diffZ); Vector3f sortPos = new Vector3f((float) from.x(), (float)
 * from.y(), (float) from.z()); Matrix4f positionMatrix =
 * matrixStackIn.last().pose(); drawLaser(builder, positionMatrix, endLaser,
 * startLaser, r, g, b, 1f, 0.025f, v, v + diffY * -5.5, sortPos);
 * matrixStackIn.popPose();
 * 
 * matrixStackIn.popPose(); buffer.endBatch(RenderTypeInit.LASER_MAIN_ADDITIVE);
 * }
 * 
 * public static Vector3f adjustBeamToEyes(Vector3f from, Vector3f to, Vector3f
 * sortPos) { Player player = Minecraft.getInstance().player; Vector3f P = new
 * Vector3f((float) player.getX() - sortPos.x(), (float) player.getEyeY() -
 * sortPos.y(), (float) player.getZ() - sortPos.z());
 * 
 * Vector3f PS = from.copy(); PS.sub(P); Vector3f SE = to.copy(); SE.sub(from);
 * 
 * Vector3f adjustedVec = PS.copy(); adjustedVec.cross(SE);
 * adjustedVec.normalize(); return adjustedVec; }
 * 
 * public static void drawLaser(VertexConsumer builder, Matrix4f positionMatrix,
 * Vector3f from, Vector3f to, float r, float g, float b, float alpha, float
 * thickness, double v1, double v2, Vector3f sortPos) { Vector3f adjustedVec =
 * adjustBeamToEyes(from, to, sortPos); adjustedVec.mul(thickness); //
 * Determines how thick the beam is Vector3f p1 = from.copy();
 * p1.add(adjustedVec); Vector3f p2 = from.copy(); p2.sub(adjustedVec); Vector3f
 * p3 = to.copy(); p3.add(adjustedVec); Vector3f p4 = to.copy();
 * p4.sub(adjustedVec); builder.vertex(positionMatrix, p1.x(), p1.y(),
 * p1.z()).color(r, g, b, alpha).uv(1, (float) v1)
 * .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
 * builder.vertex(positionMatrix, p3.x(), p3.y(), p3.z()).color(r, g, b,
 * alpha).uv(1, (float) v2)
 * .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
 * builder.vertex(positionMatrix, p4.x(), p4.y(), p4.z()).color(r, g, b,
 * alpha).uv(0, (float) v2)
 * .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
 * builder.vertex(positionMatrix, p2.x(), p2.y(), p2.z()).color(r, g, b,
 * alpha).uv(0, (float) v1)
 * .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex(); }
 * 
 * }
 */