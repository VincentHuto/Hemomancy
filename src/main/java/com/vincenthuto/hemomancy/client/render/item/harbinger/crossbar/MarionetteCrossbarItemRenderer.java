package com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.MarionetteCrossbarModel;
import com.vincenthuto.hemomancy.client.render.world.PerformerHandAnchors;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Quaternionf;

import java.util.UUID;

/**
 * The Marionette Crossbar as a working marionette control. Held bars animate from their holder's puppets; the GUI,
 * ground and item frames show the idle bar. Third-person level draws also record where each string's knot landed,
 * so puppet threads leave the bar itself.
 */
public final class MarionetteCrossbarItemRenderer extends BlockEntityWithoutLevelRenderer {
	public static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {
		private MarionetteCrossbarItemRenderer renderer;

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			if (renderer == null) renderer = new MarionetteCrossbarItemRenderer();
			return renderer;
		}
	};
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/item/marionette_crossbar_model.png");
	private static final CrossbarMotionRules.Pose IDLE = CrossbarMotionRules.pose(0.0F, 0.0F, 0.0F, 0.0F);
	/**
	 * Model space hangs the strings along +y with the bar along -z. In a third-person hand the item's +y leaves the
	 * fist and +z runs up the arm, so a half turn about (0, 1, -1) seats the grip in the fist with the bar forward.
	 */
	private static final Quaternionf HELD = new Quaternionf().rotationAxis((float) Math.PI,
			0.0F, (float) Math.sqrt(0.5D), (float) -Math.sqrt(0.5D));
	/** The 16-pixel bar spans about a torso's width in third person and a tool's length in first person. */
	private static final float HELD_SCALE = 0.5F;
	private static final float FIRST_PERSON_SCALE = 0.6F;
	private static final float GUI_SCALE = 0.62F;
	private static final float DISPLAY_SCALE = 0.6F;
	private MarionetteCrossbarModel model;

	private MarionetteCrossbarItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (model == null) {
			model = new MarionetteCrossbarModel(Minecraft.getInstance().getEntityModels()
					.bakeLayer(MarionetteCrossbarModel.LAYER_LOCATION));
		}
		Minecraft mc = Minecraft.getInstance();
		boolean firstPerson = context.firstPerson();
		boolean thirdPerson = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				|| context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
		CrossbarHolderTracker.Holder tracked = thirdPerson ? CrossbarHolderTracker.current() : null;
		LivingEntity holder = firstPerson ? mc.player : tracked != null ? tracked.entity() : null;
		UUID crossbar = MarionetteCrossbarItem.getCrossbarId(stack);
		float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
		CrossbarMotionRules.Pose pose = holder == null ? IDLE : CrossbarMotionState.pose(holder, crossbar, partialTick);
		model.pose(pose, holder == null ? 0.0F : holder.tickCount + partialTick);

		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		orient(context, poseStack);
		VertexConsumer consumer = buffer.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, -1);
		// The vein strings warm and glow faintly while a puppet hangs from them.
		float pulse = pose.pulse();
		int glow = LightTexture.pack(Math.max(LightTexture.block(packedLight), Math.round(pulse * 12.0F)),
				LightTexture.sky(packedLight));
		int vein = FastColor.ARGB32.colorFromFloat(1.0F, 0.78F + 0.22F * pulse, 0.72F + 0.1F * pulse, 0.72F + 0.1F * pulse);
		model.renderStrings(poseStack, consumer, glow, packedOverlay, vein);
		if (tracked != null && tracked.inLevel() && holder != null) {
			Vec3[] knots = new Vec3[MarionetteCrossbarModel.STRINGS.length];
			for (int i = 0; i < knots.length; i++) {
				knots[i] = PerformerHandAnchors.worldOffset(tracked.root(), model.knot(poseStack, i));
			}
			CrossbarStringAnchors.record(holder, crossbar, knots);
		}
		poseStack.popPose();
	}

	private static void orient(ItemDisplayContext context, PoseStack poseStack) {
		if (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
			poseStack.mulPose(HELD);
			poseStack.scale(HELD_SCALE, HELD_SCALE, HELD_SCALE);
		} else if (context.firstPerson()) {
			// First-person item space is the camera's: lift the bar into view, tip its front up and turn it inward.
			float side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1.0F : 1.0F;
			poseStack.translate(0.0D, 0.18D, -0.1D);
			poseStack.mulPose(Axis.YP.rotationDegrees(side * 12.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(14.0F));
			poseStack.scale(FIRST_PERSON_SCALE, FIRST_PERSON_SCALE, FIRST_PERSON_SCALE);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		} else if (context == ItemDisplayContext.GUI) {
			poseStack.translate(0.0D, 0.12D, 0.0D);
			poseStack.mulPose(Axis.XP.rotationDegrees(28.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
			poseStack.scale(GUI_SCALE, GUI_SCALE, GUI_SCALE);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		} else {
			poseStack.translate(0.0D, 0.2D, 0.0D);
			poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}
	}
}
