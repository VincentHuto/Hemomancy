package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.ContainmentAnchorBlock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A Saint's mausoleum heard from inside. Hemorath's: a slow heartbeat in the stone that grows louder, quicker and lower
 * as the listener goes down toward the pit, and blood dripping somewhere close. Seraphae's: a faint choir that swells a
 * little toward the infirmary, and the soft tick of her containment anchors nearby. Putriciel's: fire crackling in the
 * burned bands, wet wood creaking in the rotten ones, and a pyre flaring somewhere far off every half minute or so. His
 * bands are not stored, so they are read from the blocks tagged {@code hemomancy:putriciel_burned} and
 * {@code hemomancy:putriciel_rotten} inside the hallowed pieces near the listener. Velorum's is near silence: ice creaks
 * somewhere in the walls once in a long while, and the local player's own footsteps come back muffled (quieter and
 * lower; a true low-pass would need OpenAL's effects extension) while they stand in his hallowed pieces. The client
 * finds the complex through its loaded ward anchor (the hallowed boxes it carries); outside them the ambience fades
 * out. Tuning lives in {@link MausoleumAmbienceRules}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MausoleumAmbience {
	/** Tries at finding a drip spot inside a hallowed box before giving the drip up. */
	private static final int DRIP_TRIES = 4;
	/** First beat after entering, so the beat does not land on the step through the door. */
	private static final int FIRST_BEAT_DELAY = 20;

	private static ClientLevel world;
	/** The Saint whose complex is being heard (or fading out); null before the first. */
	private static EnumSaintType saint;
	private static float presence;
	private static float depth;
	private static int untilBeat = FIRST_BEAT_DELAY;
	private static SoundInstance lastBeat;
	private static RiteLoopSound choir;
	/** Lower halves of the containment anchors found by the last look around the listener. */
	private static final List<BlockPos> ANCHORS = new ArrayList<>();
	private static int untilScan;
	/** Burning burned-band blocks and rotten-band blocks found by the last look around the listener (Putriciel). */
	private static final List<BlockPos> BURNED = new ArrayList<>();
	private static final List<BlockPos> ROTTEN = new ArrayList<>();
	private static int untilFlare = MausoleumAmbienceRules.FLARE_FIRST_DELAY;
	private static int untilIceCreak = MausoleumAmbienceRules.ICE_CREAK_FIRST_DELAY;
	/** Tries at finding a spot for an ice creak inside a hallowed box before giving it up. */
	private static final int ICE_CREAK_TRIES = 6;
	private static final TagKey<Block> PUTRICIEL_BURNED = TagKey.create(Registries.BLOCK, Hemomancy.rloc("putriciel_burned"));
	private static final TagKey<Block> PUTRICIEL_ROTTEN = TagKey.create(Registries.BLOCK, Hemomancy.rloc("putriciel_rotten"));

	private MausoleumAmbience() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			if (world != null) clear();
			return;
		}
		if (world != mc.level) {
			clear();
			world = mc.level;
		}
		if (mc.isPaused()) return;

		Entity listener = mc.getCameraEntity() == null ? mc.player : mc.getCameraEntity();
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		WardAnchorBlockEntity anchor = holding(WardAnchors.loaded(mc.level), camera);
		if (anchor != null && anchor.saint() != saint) {
			// Into another Saint's complex: its own ambience starts from silence.
			stopSounds();
			forgetNearby();
			presence = 0.0F;
			saint = anchor.saint();
		}
		boolean wasSilent = presence <= MausoleumAmbienceRules.SILENT;
		presence = MausoleumAmbienceRules.easePresence(presence, anchor != null);
		if (anchor != null) {
			float target = depthOf(anchor.hallowedBoxes(), listener.getY());
			// Coming in from silence the depth is taken as found; the fade-in keeps the first sounds soft.
			depth = wasSilent ? target : MausoleumAmbienceRules.easeDepth(depth, target);
		}
		if (presence <= MausoleumAmbienceRules.SILENT) {
			untilBeat = FIRST_BEAT_DELAY;
			untilFlare = MausoleumAmbienceRules.FLARE_FIRST_DELAY;
			untilIceCreak = MausoleumAmbienceRules.ICE_CREAK_FIRST_DELAY;
			fadeChoir();
			forgetNearby();
			return;
		}

		RandomSource random = mc.level.getRandom();
		if (saint == EnumSaintType.SERAPHAE) {
			seraphae(mc, camera, random);
		} else if (saint == EnumSaintType.PUTRICIEL) {
			putriciel(mc, anchor, camera, random);
		} else if (saint == EnumSaintType.VELORUM) {
			velorum(mc, anchor, camera, random);
		} else {
			hemorath(mc, anchor, camera, random);
		}
	}

	/** On logout or a change of level: every sound stops and the next visit starts from silence. */
	public static void clear() {
		stopSounds();
		world = null;
		saint = null;
		presence = 0.0F;
		depth = 0.0F;
		untilBeat = FIRST_BEAT_DELAY;
		untilFlare = MausoleumAmbienceRules.FLARE_FIRST_DELAY;
		untilIceCreak = MausoleumAmbienceRules.ICE_CREAK_FIRST_DELAY;
		forgetNearby();
	}

	/** Drops every block found by the looks around the listener, so the next tick looks again. */
	private static void forgetNearby() {
		ANCHORS.clear();
		BURNED.clear();
		ROTTEN.clear();
		untilScan = 0;
	}

	private static void stopSounds() {
		if (lastBeat != null) Minecraft.getInstance().getSoundManager().stop(lastBeat);
		if (choir != null) Minecraft.getInstance().getSoundManager().stop(choir);
		lastBeat = null;
		choir = null;
	}

	/** The loaded complex with an ambience whose hallowed pieces hold the camera, if any. */
	private static WardAnchorBlockEntity holding(Iterable<WardAnchorBlockEntity> anchors, Vec3 camera) {
		for (WardAnchorBlockEntity anchor : anchors) {
			if (MausoleumAmbienceRules.voiced(anchor.saint()) && anchor.inHallowed(camera)) return anchor;
		}
		return null;
	}

	/** Depth between the highest and lowest hallowed floors; a floor's walking level is one above the box's base. */
	private static float depthOf(List<BoundingBox> boxes, double feetY) {
		int highest = Integer.MIN_VALUE;
		int lowest = Integer.MAX_VALUE;
		for (BoundingBox box : boxes) {
			highest = Math.max(highest, box.minY() + 1);
			lowest = Math.min(lowest, box.minY() + 1);
		}
		return boxes.isEmpty() ? 0.0F : MausoleumAmbienceRules.depth(feetY, highest, lowest);
	}

	/* ---- Hemorath: heartbeat and drips ---- */

	private static void hemorath(Minecraft mc, WardAnchorBlockEntity anchor, Vec3 camera, RandomSource random) {
		if (--untilBeat <= 0) {
			untilBeat = MausoleumAmbienceRules.beatInterval(depth);
			lastBeat = beat(MausoleumAmbienceRules.heartVolume(depth) * presence,
					MausoleumAmbienceRules.heartPitch(depth));
			mc.getSoundManager().play(lastBeat);
		}
		if (anchor != null && random.nextFloat() < MausoleumAmbienceRules.dripChance(depth)) {
			drip(mc, anchor.hallowedBoxes(), camera, random);
		}
	}

	/** One heartbeat, heard at the listener so it never pans or falls off: the stone is beating, not a spot in it. */
	private static SimpleSoundInstance beat(float volume, float pitch) {
		return new SimpleSoundInstance(SoundInit.MAUSOLEUM_HEMORATH_HEARTBEAT.get().getLocation(), SoundSource.AMBIENT,
				volume, pitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE,
				0.0D, 0.0D, 0.0D, true);
	}

	/** A drip a few blocks off, from above head height, at a spot inside a hallowed piece. */
	private static void drip(Minecraft mc, List<BoundingBox> boxes, Vec3 camera, RandomSource random) {
		for (int i = 0; i < DRIP_TRIES; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double distance = 2.0D + random.nextDouble() * 4.0D;
			double x = camera.x + Math.cos(angle) * distance;
			double y = camera.y + 0.5D + random.nextDouble() * 3.0D;
			double z = camera.z + Math.sin(angle) * distance;
			if (!inside(boxes, BlockPos.containing(x, y, z))) continue;
			float volume = MausoleumAmbienceRules.dripVolume(depth) * presence;
			float pitch = 0.9F + random.nextFloat() * 0.2F;
			mc.getSoundManager().play(new SimpleSoundInstance(SoundInit.MAUSOLEUM_HEMORATH_DRIP.get(),
					SoundSource.AMBIENT, volume, pitch, random, x, y, z));
			return;
		}
	}

	private static boolean inside(List<BoundingBox> boxes, BlockPos pos) {
		for (BoundingBox box : boxes) {
			if (box.isInside(pos)) return true;
		}
		return false;
	}

	/* ---- Seraphae: choir and anchors ---- */

	private static void seraphae(Minecraft mc, Vec3 camera, RandomSource random) {
		// The choir is everywhere in her ward at once, so it is kept on the listener and never pans.
		float volume = MausoleumAmbienceRules.choirVolume(depth) * presence;
		if (RiteSoundBed.needsStart(choir, choir != null && mc.getSoundManager().isActive(choir))) {
			choir = new RiteLoopSound(SoundInit.MAUSOLEUM_SERAPHAE_CHOIR.get(), camera.x, camera.y, camera.z);
			mc.getSoundManager().play(choir);
		}
		choir.steer(volume, MausoleumAmbienceRules.choirPitch(depth), camera.x, camera.y, camera.z);

		if (--untilScan <= 0) {
			untilScan = MausoleumAmbienceRules.ANCHOR_SCAN_INTERVAL;
			scanAnchors(mc.level, BlockPos.containing(camera));
		}
		if (!ANCHORS.isEmpty() && random.nextFloat() < MausoleumAmbienceRules.anchorTickChance(depth)) {
			BlockPos at = ANCHORS.get(random.nextInt(ANCHORS.size()));
			mc.getSoundManager().play(new SimpleSoundInstance(SoundInit.MAUSOLEUM_SERAPHAE_ANCHOR_TICK.get(),
					SoundSource.AMBIENT, MausoleumAmbienceRules.anchorTickVolume(depth) * presence,
					MausoleumAmbienceRules.anchorTickPitch(random.nextFloat()), random,
					at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D));
		}
	}

	/** Lets the choir fade itself out (it stops once quiet) as the listener leaves. */
	private static void fadeChoir() {
		if (choir != null) choir.fadeOut();
		choir = null;
	}

	/** Every containment anchor's lower half in a small box around the listener; run every couple of seconds. */
	private static void scanAnchors(ClientLevel level, BlockPos centre) {
		ANCHORS.clear();
		Block block = BlockInit.containment_anchor_block.get();
		int across = MausoleumAmbienceRules.ANCHOR_SCAN_RADIUS;
		int up = MausoleumAmbienceRules.ANCHOR_SCAN_HEIGHT;
		for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-across, -up, -across), centre.offset(across, up, across))) {
			BlockState state = level.getBlockState(pos);
			if (state.is(block) && state.getValue(ContainmentAnchorBlock.HALF) == DoubleBlockHalf.LOWER) {
				ANCHORS.add(pos.immutable());
			}
		}
	}

	/* ---- Putriciel: crackle, creak and the distant flare ---- */

	private static void putriciel(Minecraft mc, WardAnchorBlockEntity anchor, Vec3 camera, RandomSource random) {
		// While fading out past the ward's edge the last look stands; there are no boxes to look inside.
		if (anchor != null && --untilScan <= 0) {
			untilScan = MausoleumAmbienceRules.BAND_SCAN_INTERVAL;
			scanBands(mc.level, anchor.hallowedBoxes(), BlockPos.containing(camera));
		}
		int burned = BURNED.size();
		int rotten = ROTTEN.size();
		if (burned > 0 && random.nextFloat() < MausoleumAmbienceRules.crackleChance(burned, rotten)) {
			BlockPos at = BURNED.get(random.nextInt(burned));
			bandSound(mc, SoundInit.MAUSOLEUM_PUTRICIEL_CRACKLE.get(), at,
					MausoleumAmbienceRules.crackleVolume(distance(camera, at)),
					MausoleumAmbienceRules.cracklePitch(random.nextFloat()), random);
		}
		if (rotten > 0 && random.nextFloat() < MausoleumAmbienceRules.creakChance(burned, rotten)) {
			BlockPos at = ROTTEN.get(random.nextInt(rotten));
			bandSound(mc, SoundInit.MAUSOLEUM_PUTRICIEL_CREAK.get(), at,
					MausoleumAmbienceRules.creakVolume(distance(camera, at)),
					MausoleumAmbienceRules.creakPitch(random.nextFloat()), random);
		}
		if (--untilFlare <= 0) {
			untilFlare = MausoleumAmbienceRules.flareInterval(random.nextFloat());
			mc.getSoundManager().play(flare(camera, MausoleumAmbienceRules.flareVolume(burned, rotten) * presence,
					MausoleumAmbienceRules.flarePitch(random.nextFloat()), random));
		}
	}

	private static void bandSound(Minecraft mc, SoundEvent event, BlockPos at, float volume, float pitch,
			RandomSource random) {
		mc.getSoundManager().play(new SimpleSoundInstance(event, SoundSource.AMBIENT, volume * presence, pitch, random,
				at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D));
	}

	/**
	 * A pyre flaring far down the halls: placed well off in a random direction so it pans, but never attenuated by the
	 * engine, whose 16-block rolloff would leave nothing of it; how faint it is comes from the rules.
	 */
	private static SimpleSoundInstance flare(Vec3 camera, float volume, float pitch, RandomSource random) {
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double distance = MausoleumAmbienceRules.flareDistance(random.nextFloat());
		return new SimpleSoundInstance(SoundInit.MAUSOLEUM_PUTRICIEL_FLARE.get().getLocation(), SoundSource.AMBIENT,
				volume, pitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE,
				camera.x + Math.cos(angle) * distance, camera.y + 1.0D, camera.z + Math.sin(angle) * distance, false);
	}

	private static double distance(Vec3 camera, BlockPos at) {
		return Math.sqrt(at.distToCenterSqr(camera));
	}

	/**
	 * Every burned-band block still burning and every rotten-band block inside the hallowed pieces in a small box
	 * around the listener; run every couple of seconds.
	 */
	private static void scanBands(ClientLevel level, List<BoundingBox> boxes, BlockPos centre) {
		BURNED.clear();
		ROTTEN.clear();
		int across = MausoleumAmbienceRules.BAND_SCAN_RADIUS;
		int up = MausoleumAmbienceRules.BAND_SCAN_HEIGHT;
		for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-across, -up, -across), centre.offset(across, up, across))) {
			if (!inside(boxes, pos)) continue;
			BlockState state = level.getBlockState(pos);
			if (state.is(PUTRICIEL_BURNED)) {
				if (burning(state)) BURNED.add(pos.immutable());
			} else if (state.is(PUTRICIEL_ROTTEN)) {
				ROTTEN.add(pos.immutable());
			}
		}
	}

	/** A burned-band block is heard unless it has a {@code lit} switch that is off (a guttered pyre is silent). */
	private static boolean burning(BlockState state) {
		for (Property<?> property : state.getProperties()) {
			if (property instanceof BooleanProperty lit && "lit".equals(property.getName())) return state.getValue(lit);
		}
		return true;
	}

	/* ---- Velorum: near silence, the ice creaking, the listener's own steps muffled ---- */

	private static void velorum(Minecraft mc, WardAnchorBlockEntity anchor, Vec3 camera, RandomSource random) {
		if (anchor == null || --untilIceCreak > 0) return;
		untilIceCreak = MausoleumAmbienceRules.iceCreakInterval(random.nextFloat());
		List<BoundingBox> boxes = anchor.hallowedBoxes();
		for (int i = 0; i < ICE_CREAK_TRIES; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double distance = MausoleumAmbienceRules.iceCreakDistance(random.nextFloat());
			double x = camera.x + Math.cos(angle) * distance;
			double y = camera.y - 0.5D + random.nextDouble() * 3.0D;
			double z = camera.z + Math.sin(angle) * distance;
			if (!inside(boxes, BlockPos.containing(x, y, z))) continue;
			mc.getSoundManager().play(new SimpleSoundInstance(SoundInit.MAUSOLEUM_VELORUM_CREAK.get(), SoundSource.AMBIENT,
					MausoleumAmbienceRules.iceCreakVolume(depth) * presence,
					MausoleumAmbienceRules.iceCreakPitch(random.nextFloat()), random, x, y, z));
			return;
		}
	}

	/**
	 * The local player's own footsteps inside a loaded Velorum ward's hallowed pieces come back quieter and lower, as if
	 * the ice took the rest. The step is played on the client level for the local player only, so it is caught there.
	 */
	@SubscribeEvent
	public static void muffleOwnStep(PlayLevelSoundEvent.AtPosition event) {
		if (!event.getLevel().isClientSide() || event.getSound() == null) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || event.getLevel() != mc.level) return;
		if (!MausoleumAmbienceRules.ownFootstep(event.getSound().value().getLocation().getPath(),
				event.getSource() == SoundSource.PLAYERS, mc.player.position().distanceToSqr(event.getPosition()))) {
			return;
		}
		BlockPos feet = mc.player.blockPosition();
		for (WardAnchorBlockEntity anchor : WardAnchors.loaded(mc.level)) {
			if (MausoleumAmbienceRules.mufflesSteps(anchor.saint(), anchor.hallowedBoxes(), feet)) {
				event.setNewVolume(event.getNewVolume() * MausoleumAmbienceRules.MUFFLED_STEP_VOLUME);
				event.setNewPitch(event.getNewPitch() * MausoleumAmbienceRules.MUFFLED_STEP_PITCH);
				return;
			}
		}
	}
}
