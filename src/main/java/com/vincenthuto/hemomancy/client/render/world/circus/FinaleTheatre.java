package com.vincenthuto.hemomancy.client.render.world.circus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.RingmasterFinaleClientData;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleEchoCue;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleEchoes;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.TrueSelf;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

/**
 * The theatre in the Ringmaster's tent (2026-11-18), drawn in its own world pass after the veil on the tent's faces
 * ({@link FinaleMergeRenderer}): a phantom audience in the seats, stage lights falling on the echoes, and the echoes
 * themselves, the true selves of the professors this player passed, each at its station round the ring in an idle
 * loop of its act. Everything stands outside the ring's column, which stays plainly real. The echoes are client-only
 * stand-ins drawn by their own renderers; nothing here is in the world.
 */
public final class FinaleTheatre {
	/** Each echo begins one of its gestures this often, staggered across the stations. */
	private static final int GESTURE_EVERY = 200;

	private static final Map<String, Entity> ECHOES = new HashMap<>();
	@Nullable
	private static ClientLevel echoesOf;

	private FinaleTheatre() {
	}

	/** Where station {@code index} stands round {@code centre}, on the ring's own floor. */
	public static Vec3 station(Vec3 centre, int index) {
		return FinaleEchoes.station(centre, index);
	}

	/** Draws the theatre under {@code pose} (the camera's rotation, camera-relative positions). */
	public static void render(PoseStack pose, float partial) {
		Minecraft mc = Minecraft.getInstance();
		CircusRingmasterEntity ringmaster = RingmasterFinaleClientData.ringmaster();
		if (mc.level == null || ringmaster == null || !RingmasterFinaleClientData.isActive()) return;
		if (echoesOf != mc.level) {
			ECHOES.clear();
			echoesOf = mc.level;
		}
		Camera camera = mc.gameRenderer.getMainCamera();
		Vec3 eye = camera.getPosition();
		Vec3 centre = RingmasterFinaleClientData.copyCentre();
		float time = AnimationTime.gameTime(mc.level, partial);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

		List<OrdealProfessor> roster = OrdealProfessor.all();
		List<String> passed = FinaleEchoes.roles(ringmaster.echoRoles());
		List<Vec3> lit = new ArrayList<>();
		for (int i = 0; i < roster.size(); i++) {
			OrdealProfessor professor = roster.get(i);
			if (!passed.contains(professor.role())) continue;
			Vec3 at = station(centre, i);
			lit.add(at);
			Entity echo = echo(mc.level, professor);
			// While his cue has this professor's echo out on the stage, its stand-in leaves the dark.
			if (echo == null || called(mc.level, echo.getType(), centre)) continue;
			perform(echo, professor, i, at, centre, time);
			mc.getEntityRenderDispatcher().render(echo, at.x - eye.x, at.y - eye.y, at.z - eye.z, echo.getYRot(), partial,
					pose, buffers, LightTexture.FULL_BRIGHT);
		}
		buffers.endBatch();

		ShowMarks stage = new ShowMarks(List.of(), List.of(), List.of(), List.of(), new ShowMarks.Ring(centre, 7.0D));
		Spotlights.render(buffers.getBuffer(HemoRenderTypes.QLIPHOTH_GLOW), pose.last().pose(), eye, stage, lit, 1.0F);
		buffers.endBatch(HemoRenderTypes.QLIPHOTH_GLOW);
		PhantomFigures.render(pose, buffers, eye, camera.getLookVector(), PhantomFigures.planFinale(centre, time, 0.55F),
				List.of(), time);
		buffers.endBatch();
	}

	@Nullable
	private static Entity echo(ClientLevel level, OrdealProfessor professor) {
		return ECHOES.computeIfAbsent(professor.role(), role -> {
			Entity made = EntityType.byString(Hemomancy.rloc(role).toString()).map(type -> type.create(level)).orElse(null);
			if (made instanceof TrueSelf self) self.setTrueSelf(true);
			return made;
		});
	}

	/** Stands the echo at its station facing the ring, and starts its next gesture on its beat. */
	private static void perform(Entity echo, OrdealProfessor professor, int index, Vec3 at, Vec3 centre, float time) {
		float yaw = (float) Math.toDegrees(Math.atan2(-(centre.x - at.x), centre.z - at.z));
		echo.setPos(at);
		echo.xo = echo.xOld = at.x;
		echo.yo = echo.yOld = at.y;
		echo.zo = echo.zOld = at.z;
		echo.setYRot(yaw);
		echo.yRotO = yaw;
		echo.setYHeadRot(yaw);
		if (echo instanceof net.minecraft.world.entity.LivingEntity living) {
			living.yBodyRot = living.yBodyRotO = yaw;
			living.yHeadRotO = yaw;
		}
		int now = (int) time + index * 37;
		int last = echo.tickCount;
		echo.tickCount = now;
		// Only the signature's gesture: the others belong to moves made at a fighter (the Strongman's haul jerks on
		// nothing out here).
		int kind = FinaleEchoCue.signature(professor).map(move -> professor.choreography().gestureFor(move.id())).orElse(-1);
		if (kind >= 0 && kind < CircusPerformerEntity.GESTURES && echo instanceof CircusPerformerEntity performer
				&& now / GESTURE_EVERY != last / GESTURE_EVERY) {
			performer.gesture(kind, Vec3.atBottomCenterOf(BlockPos.containing(centre)));
			performer.handleEntityEvent((byte) (CircusPerformerEntity.GESTURE_EVENT + kind));
		}
	}

	private static boolean called(ClientLevel level, EntityType<?> type, Vec3 centre) {
		return !level.getEntities(type, new net.minecraft.world.phys.AABB(centre, centre).inflate(64.0D),
				found -> found instanceof TrueSelf self && self.isTrueSelf()).isEmpty();
	}

	public static void clear() {
		ECHOES.clear();
		echoesOf = null;
	}
}
