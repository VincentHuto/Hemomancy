package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The roots of a Venous Cling (core-loop Track 9): at each contact point (both hands and both feet on a wall, both
 * hands under a ceiling) a short sinew runs from the limb into the face, and a small patch of the infusion vein net
 * spreads into the stone round it. Thin, partly clear, anchored where the body holds; drawn for every clinging player
 * in range, since the cling is synced to trackers.
 */
public final class ClingVeinRenderer {
	private static final double RANGE = 48.0D;
	/** The patch of veins round each contact, as a share of a block face. */
	private static final double PATCH = 0.8D;
	/** The patch stands this far off the face, so it never fights the face for depth. */
	private static final double LIFT = 0.012D;
	private static final int VEIN_COLOR = 0xFFFFFFFF;

	private ClingVeinRenderer() {
	}

	/** One contact: where the limb is, and where its root enters the face. */
	private record Contact(Vec3 limb, Vec3 face) {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		List<Player> clinging = new ArrayList<>();
		for (Player player : mc.level.players()) {
			if (HemoCapabilityAccess.getCling(player).isClinging() && player.position().distanceToSqr(camera) < RANGE * RANGE) {
				clinging.add(player);
			}
		}
		if (clinging.isEmpty()) return;
		float time = AnimationTime.gameTime(mc.level, partialTick);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		for (Player player : clinging) patches(player, poseStack, buffers, camera, time, partialTick);
		OutpouringBrushes.begin(time);
		for (Player player : clinging) {
			int seed = player.getId() * 17;
			int i = 0;
			for (Contact contact : contacts(player, partialTick)) {
				OutpouringBrushes.sinew(poseStack, contact.limb().subtract(camera), contact.face().subtract(camera), 0.05D,
						0.03D, time, seed + i++, OutpouringBrushes.BLOOD, 0.9F);
			}
		}
		OutpouringBrushes.flush(buffers);
	}

	/** The vein net spreading into the face round each contact. */
	private static void patches(Player player, PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 camera,
			float time, float partialTick) {
		Direction face = HemoCapabilityAccess.getCling(player).face();
		Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
		int i = 0;
		for (Contact contact : contacts(player, partialTick)) {
			Vec3 at = contact.face().add(normal.scale(LIFT));
			// A unit cube whose `face` side lies on the wall, centred on the contact, shrunk about the contact.
			Vec3 corner = at.subtract(normal.scale(0.5D)).subtract(0.5D, 0.5D, 0.5D);
			float seed = (float) ((player.getId() * 5 + i++ * 23) % 127);
			RenderType veins = HemoRenderTypes.bloodInfusionVeins(time, 0.85F, seed, 0.0F, 0.0F,
					(float) (at.x - camera.x), (float) (at.y - camera.y), (float) (at.z - camera.z),
					face.getStepX(), face.getStepY(), face.getStepZ(),
					(float) (corner.x - camera.x), (float) (corner.y - camera.y), (float) (corner.z - camera.z));
			Vec3 relative = at.subtract(camera);
			poseStack.pushPose();
			poseStack.translate(relative.x, relative.y, relative.z);
			poseStack.scale((float) PATCH, (float) PATCH, (float) PATCH);
			poseStack.translate(-relative.x, -relative.y, -relative.z);
			BlockShellFaces.emitFace(buffers.getBuffer(veins), poseStack.last(), BlockPos.ZERO, face, camera.subtract(corner),
					VEIN_COLOR);
			poseStack.popPose();
			buffers.endBatch(veins);
		}
	}

	/** Hands and feet on a wall; hands only under a ceiling. Points in world space. */
	private static List<Contact> contacts(Player player, float partialTick) {
		ClingState cling = HemoCapabilityAccess.getCling(player);
		Direction face = cling.face();
		Vec3 feet = player.getPosition(partialTick);
		AABB body = player.getBoundingBox().move(feet.subtract(player.position()));
		List<Contact> contacts = new ArrayList<>();
		if (cling.surface() == ClingRules.Surface.CEILING) {
			double top = body.maxY + 0.005D;
			float yaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
			Vec3 side = new Vec3(Mth.cos(yaw), 0.0D, Mth.sin(yaw)).scale(0.32D);
			for (int s = -1; s <= 1; s += 2) {
				Vec3 hand = new Vec3(feet.x, body.maxY - 0.1D, feet.z).add(side.scale(s));
				contacts.add(new Contact(hand, new Vec3(hand.x, top, hand.z)));
			}
			return contacts;
		}
		Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
		Vec3 along = normal.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
		// The wall plane: the body's side toward the wall.
		double half = body.getXsize() * 0.5D;
		Vec3 centre = new Vec3(feet.x, feet.y, feet.z);
		Vec3 plane = centre.subtract(normal.scale(half + 0.005D));
		// Hands reach up beside the head, feet brace low; the limbs stand a little off the face.
		double[][] limbs = {{0.42D, 1.78D}, {-0.42D, 1.66D}, {0.2D, 0.18D}, {-0.2D, 0.08D}};
		for (double[] limb : limbs) {
			Vec3 onWall = plane.add(along.scale(limb[0])).add(0.0D, limb[1], 0.0D);
			Vec3 onBody = onWall.add(normal.scale(0.32D));
			contacts.add(new Contact(onBody, onWall));
		}
		return contacts;
	}
}
