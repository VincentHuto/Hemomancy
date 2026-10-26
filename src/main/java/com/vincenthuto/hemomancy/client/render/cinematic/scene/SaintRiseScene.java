package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

/**
 * A Saint rising out of its broken sarcophagus, shared by all four: the camera starts low at the foot of it while
 * grave-dust lifts, rises with the Saint, and holds as it stands, where its name lands. The card names whichever Saint
 * it is, once the client has it.
 */
public final class SaintRiseScene implements BossScene {
	private static final int LENGTH = SaintRise.TICKS;
	private static final int SURFACED = SaintRise.SURFACED;
	private static final int FREE = LENGTH - HANDBACK;
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 38, 4.2F, 0.2F, 0.6F),
			new Key(SURFACED, 18, 5.6F, 0.9F, 1.8F), new Key(LENGTH, 6, 6.8F, 1.3F, 1.9F));
	private static final int RULE = 0xC8AA8A;

	@Override
	public int length() {
		return SURFACED + 40;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 0, 8, 1, FREE, 1, FREE + 10, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < FREE;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < FREE;
	}

	@Nullable
	private TitleCard card;

	@Override
	@Nullable
	public TitleCard card(@Nullable Entity boss) {
		if (card == null && boss != null)
			card = TitleCard.of(BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()).getPath(), SURFACED, 40, RULE);
		return card;
	}

	@Override
	public CinematicShot shot() {
		return SHOT;
	}

	@Override
	public boolean shotActive(float t) {
		return t < FREE;
	}

	@Override
	public CameraOffsets offsets(float t) {
		return CameraOffsets.shake(BossSceneTracks.keys(t, 0, 0.35F, SURFACED - 1, 0.5F, SURFACED, 1.4F, SURFACED + 14, 0));
	}
}
