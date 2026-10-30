package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.data.PossessionSceneClientState;
import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PossessionSkipPacket;
import net.minecraft.client.gui.GuiGraphics;

import static com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.*;

/**
 * One half of a possession's cinematic, as the possessor sees it. The halves share their shape: letterboxed, input and
 * HUD held while the server holds the body, a camera move around the framed entity, and black across each crossing.
 * The world effects are drawn for everyone near by {@code PossessionSceneRenderer}; this class only places the camera
 * and the screen art. A skip asks the server to run what is left of the sequence, which leaves the state the scenes
 * would have.
 */
public final class PossessionScene implements BossScene {
	private static final int BLACK_HOLD = 30;

	private final Scene scene;

	public PossessionScene(Scene scene) {
		this.scene = scene;
	}

	/** The scene tick the server's sequence stops holding the body: the settle, or the crossing for a first half. */
	private int held() {
		return switch (scene) {
			case DEPARTURE, BROKEN_GARDENS, CLEAN_GARDENS -> length();
			case ARRIVAL -> ARRIVAL_CONTROL;
			case BROKEN_RITE -> BROKEN_REMANIFEST_END;
			case CLEAN_RITE -> CLEAN_REMANIFEST_END;
		};
	}

	@Override
	public int length() {
		// A first half holds its black past its cut, until the crossing's clone ends it.
		boolean firstHalf = scene == Scene.DEPARTURE || scene == Scene.BROKEN_GARDENS || scene == Scene.CLEAN_GARDENS;
		return firstHalf ? scene.length() + BLACK_HOLD : scene.length();
	}

	@Override
	public float letterbox(float t) {
		int out = held();
		return BossSceneTracks.keys(t, 0, scene == Scene.DEPARTURE ? 0 : 1, 8, 1, out, 1, out + 10, 0);
	}

	@Override
	public float fade(float t) {
		return switch (scene) {
			case DEPARTURE -> BossSceneTracks.keys(t, DEPARTURE_BLACK, 0, DEPARTURE_BLACK + 8, 1);
			case BROKEN_GARDENS -> BossSceneTracks.keys(t, BROKEN_BLACK, 0, BROKEN_BLACK + 8, 1);
			case CLEAN_GARDENS -> BossSceneTracks.keys(t, CLEAN_BLACK, 0, CLEAN_BLACK + 8, 1);
			case ARRIVAL -> BossSceneTracks.keys(t, 0, 1, ARRIVAL_FADE_IN, 0);
			case BROKEN_RITE, CLEAN_RITE -> BossSceneTracks.keys(t, 0, 1, FADE_IN, 0);
		};
	}

	@Override
	public boolean hudHidden(float t) {
		return t < held();
	}

	@Override
	public boolean inputLocked(float t) {
		return t < held();
	}

	@Override
	public boolean skippable(float t) {
		return t < held();
	}

	@Override
	public void skipped() {
		PacketHandler.sendToServer(PossessionSkipPacket.INSTANCE);
		PossessionSceneClientState.skippedLocally();
	}

	@Override
	public CinematicShot shot() {
		return switch (scene) {
			// From behind the caster toward the surrogate, round to three-quarters, then down to the floor.
			case DEPARTURE -> CinematicShot.toward(new Key(0, 8, 6.5F, 2.1F, 0.9F),
					new Key(DEPARTURE_DAEMON_START, 24, 5.8F, 2.0F, 1.0F),
					new Key(DEPARTURE_DAEMON_ARRIVES, 42, 4.6F, 1.5F, 0.8F),
					new Key(DEPARTURE_TENDRILS_RISE + 6, 50, 3.6F, 0.35F, 0.35F),
					new Key(scene.length(), 56, 3.2F, 0.3F, 0.15F));
			// Low and wide on the seized facsimile, closing as it is filled.
			case ARRIVAL -> CinematicShot.of(new Key(0, 145, 3.8F, 1.7F, 0.7F),
					new Key(ARRIVAL_INJECT_END, 60, 3.0F, 1.2F, 0.8F),
					new Key(ARRIVAL_CONTROL, 20, 2.6F, 1.1F, 1.0F));
			case BROKEN_GARDENS -> CinematicShot.of(new Key(0, 28, 3.0F, 1.3F, 0.8F),
					new Key(BROKEN_BURST, 32, 3.4F, 1.5F, 0.7F), new Key(scene.length(), 48, 4.0F, 2.2F, 0.2F));
			case CLEAN_GARDENS -> CinematicShot.of(new Key(0, 30, 3.0F, 1.2F, 0.9F),
					new Key(CLEAN_COLLAPSE_END, 40, 2.8F, 1.0F, 0.3F), new Key(scene.length(), 52, 3.4F, 1.6F, 0.1F));
			case BROKEN_RITE -> CinematicShot.of(new Key(0, 30, 5.2F, 1.2F, 0.6F),
					new Key(BROKEN_BLAST, 24, 5.6F, 1.6F, 1.2F), new Key(BROKEN_SLAM, 12, 4.6F, 1.6F, 1.3F),
					new Key(scene.length(), 0, 4.2F, 1.7F, 1.5F));
			case CLEAN_RITE -> CinematicShot.of(new Key(0, 40, 5.6F, 1.0F, 0.5F),
					new Key(CLEAN_REFORM_END, 26, 5.0F, 2.0F, 1.9F), new Key(CLEAN_REMANIFEST_END, 10, 3.8F, 1.6F, 1.5F));
		};
	}

	@Override
	public boolean shotActive(float t) {
		return t < held();
	}

	@Override
	public CameraOffsets offsets(float t) {
		return switch (scene) {
			case DEPARTURE -> CameraOffsets.shake(BossSceneTracks.keys(t, DEPARTURE_TENDRILS_RISE - 1, 0,
					DEPARTURE_TENDRILS_RISE, 0.9F, DEPARTURE_TENDRILS_CLOSE, 0.4F, DEPARTURE_BLACK, 0));
			case BROKEN_GARDENS -> CameraOffsets.shake(BossSceneTracks.keys(t, BROKEN_BURST - 1, 0.2F, BROKEN_BURST,
					1.6F, BROKEN_BURST + 14, 0.1F));
			case BROKEN_RITE -> CameraOffsets.shake(BossSceneTracks.keys(t, BROKEN_BLAST - 1, 0.3F, BROKEN_BLAST, 1.8F,
					BROKEN_SLAM, 0.5F, BROKEN_SLAM + 1, 1.2F, BROKEN_REMANIFEST_END + 10, 0));
			default -> CameraOffsets.NONE;
		};
	}

	/** After a broken return the view settles through a dark, violet bruise: the will came back hurt. */
	@Override
	public void overlay(GuiGraphics graphics, int width, int height, float t, float time) {
		if (scene != Scene.BROKEN_RITE) return;
		float daze = BossSceneTracks.keys(t, BROKEN_SLAM, 0, BROKEN_REMANIFEST_END, 1, length(), 0);
		if (daze <= 0.0F) return;
		int edge = Math.round(150 * daze);
		int inner = Math.round(40 * daze);
		int band = Math.max(1, height / 3);
		graphics.fillGradient(0, 0, width, band, edge << 24 | 0x12061A, inner << 24 | 0x12061A);
		graphics.fillGradient(0, height - band, width, height, inner << 24 | 0x12061A, edge << 24 | 0x12061A);
		graphics.fill(0, 0, width, height, Math.round(36 * daze) << 24 | 0x1E0A24);
	}
}
