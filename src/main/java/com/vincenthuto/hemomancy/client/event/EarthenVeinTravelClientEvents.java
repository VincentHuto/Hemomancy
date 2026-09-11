package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.EarthenVeinDestinationLayout;
import com.vincenthuto.hemomancy.client.render.world.EarthenVeinDestinationTargeting;
import com.vincenthuto.hemomancy.client.render.world.SanguineTendrilRibbonRenderer;
import com.vincenthuto.hemomancy.client.vein.EarthenVeinTravelClientState;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.vein.SelectEarthenVeinDestinationPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.mojang.math.Axis;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class EarthenVeinTravelClientEvents {
	private static final double MARKER_REACH = 8.5D;
	private static final double MARKER_RADIUS = 0.48D;
	@Nullable private static CameraType cameraBeforeTransit;

	private EarthenVeinTravelClientEvents() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		EarthenVeinTravelClientState.tick();
		Minecraft minecraft = Minecraft.getInstance();
		if (EarthenVeinTravelClientState.isLocalCinematic()) {
			if (cameraBeforeTransit == null) cameraBeforeTransit = minecraft.options.getCameraType();
			minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
		} else if (cameraBeforeTransit != null) {
			minecraft.options.setCameraType(cameraBeforeTransit);
			cameraBeforeTransit = null;
		}
	}

	@SubscribeEvent
	public static void onMovement(MovementInputUpdateEvent event) {
		if (!EarthenVeinTravelClientState.isLocalCinematic()) return;
		var input = event.getInput();
		input.forwardImpulse = 0.0F;
		input.leftImpulse = 0.0F;
		input.up = input.down = input.left = input.right = input.jumping = input.shiftKeyDown = false;
	}

	@SubscribeEvent
	public static void onDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
		if (EarthenVeinTravelClientState.isLocalCinematic()) event.setDistance(Math.max(4.0F, event.getDistance()));
	}

	@SubscribeEvent
	public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
		if (!event.isUseItem()) return;
		Minecraft minecraft = Minecraft.getInstance();
		var selected = hovered(minecraft);
		var selection = EarthenVeinTravelClientState.selection();
		if (selected == null || selection == null) return;
		PacketHandler.sendToServer(new SelectEarthenVeinDestinationPacket(
				selection.nonce(), selected.destination().id()));
		event.setCanceled(true);
		event.setSwingHand(true);
	}

	@SubscribeEvent
	public static void renderWorld(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
		Minecraft minecraft = Minecraft.getInstance();
		var selection = EarthenVeinTravelClientState.selection();
		if (selection == null || minecraft.level == null || minecraft.player == null
				|| !minecraft.level.dimension().location().equals(selection.sourceDimension())) return;
		Vec3 source = Vec3.atBottomCenterOf(selection.source()).add(0.0D, 0.72D, 0.0D);
		List<EarthenVeinDestinationLayout.Marker> markers = EarthenVeinDestinationLayout.layout(
				source, selection.source(), selection.sourceDimension(), selection.destinations());
		EarthenVeinDestinationLayout.Marker hovered = hovered(minecraft, markers);
		Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
		float time = minecraft.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(true);

		VertexConsumer glow = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
		SanguineTendrilRibbonRenderer.render(poseStack, glow, strands(source, markers, hovered, time), camera, true);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
		VertexConsumer core = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
		SanguineTendrilRibbonRenderer.render(poseStack, core, strands(source, markers, hovered, time), camera, false);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);

		ItemStack ghostVein = new ItemStack(BlockInit.earthen_vein.get());
		for (var marker : markers) {
			poseStack.pushPose();
			poseStack.translate(marker.center().x - camera.x, marker.center().y - camera.y, marker.center().z - camera.z);
			poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.2F));
			float scale = marker == hovered ? 0.54F : 0.42F;
			poseStack.scale(scale, scale, scale);
			minecraft.getItemRenderer().renderStatic(ghostVein, ItemDisplayContext.FIXED,
					LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, buffer,
					minecraft.level, marker.destination().id().hashCode());
			poseStack.popPose();
		}
		buffer.endBatch();
	}

	@SubscribeEvent
	public static void renderTooltip(RenderGuiEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		var marker = hovered(minecraft);
		if (marker == null) return;
		GuiGraphics graphics = event.getGuiGraphics();
		int x = graphics.guiWidth() / 2;
		int y = graphics.guiHeight() / 2 + 14;
		var destination = marker.destination();
		graphics.drawCenteredString(minecraft.font, Component.literal(destination.name())
				.withStyle(ChatFormatting.DARK_RED), x, y, 0xFFF2D8D8);
		graphics.drawCenteredString(minecraft.font, Component.literal(
				destination.dimension().getPath() + "  " + destination.position().toShortString()),
				x, y + 11, 0xFFC2B8B8);
		graphics.drawCenteredString(minecraft.font, Component.literal(destination.cost() + " mB")
				.withStyle(ChatFormatting.RED), x, y + 22, 0xFFFF6868);
	}

	@Nullable
	private static EarthenVeinDestinationLayout.Marker hovered(Minecraft minecraft) {
		var selection = EarthenVeinTravelClientState.selection();
		if (selection == null || minecraft.level == null) return null;
		Vec3 source = Vec3.atBottomCenterOf(selection.source()).add(0.0D, 0.72D, 0.0D);
		return hovered(minecraft, EarthenVeinDestinationLayout.layout(source, selection.source(),
				selection.sourceDimension(), selection.destinations()));
	}

	@Nullable
	private static EarthenVeinDestinationLayout.Marker hovered(Minecraft minecraft,
			List<EarthenVeinDestinationLayout.Marker> markers) {
		if (minecraft.player == null) return null;
		return EarthenVeinDestinationTargeting.select(markers, minecraft.player.getEyePosition(),
				minecraft.player.getViewVector(1.0F), MARKER_REACH, MARKER_RADIUS).orElse(null);
	}

	private static List<DisplayStrand> strands(Vec3 source, List<EarthenVeinDestinationLayout.Marker> markers,
			@Nullable EarthenVeinDestinationLayout.Marker hovered, float time) {
		List<DisplayStrand> strands = new ArrayList<>(markers.size());
		for (int index = 0; index < markers.size(); index++) {
			var marker = markers.get(index);
			List<DisplayJoint> joints = new ArrayList<>();
			Vec3 delta = marker.center().subtract(source);
			Vec3 side = new Vec3(-delta.z, 0.0D, delta.x).normalize();
			for (int joint = 0; joint <= 12; joint++) {
				float progress = joint / 12.0F;
				double curl = Math.sin(progress * Math.PI * 2.0D + time * 0.12D + index) * 0.10D
						* Math.sin(progress * Math.PI);
				Vec3 center = source.add(delta.scale(progress)).add(side.scale(curl));
				float tip = 1.0F - Math.abs(progress * 2.0F - 1.0F);
				float width = 0.012F + tip * (marker == hovered ? 0.075F : 0.052F);
				joints.add(new DisplayJoint(center, width, marker == hovered ? 1.0F : 0.70F));
			}
			strands.add(new DisplayStrand(index, List.copyOf(joints)));
		}
		return strands;
	}

	private record DisplayStrand(int index, List<DisplayJoint> joints)
			implements SanguineTendrilRibbonRenderer.Strand { }
	private record DisplayJoint(Vec3 center, float halfWidth, float opacity)
			implements SanguineTendrilRibbonRenderer.Joint { }
}
