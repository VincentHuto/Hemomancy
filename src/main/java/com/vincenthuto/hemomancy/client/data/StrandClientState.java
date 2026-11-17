package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.client.player.LashCamera;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargeting;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.BrandRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.JetRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashProfile;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandForm;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSkills;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.LashAimPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Every strand this client can see (core-loop Track 12). The server sends one keyed cue when a strand opens and
 * refreshes it each pulse; this client runs the same rule class from the caster's look each tick and draws its own
 * copy, so no position is ever sent per tick. The local player's copy uses their exact look and feels immediate;
 * another player's follows their synced look, which is smoother than exact. Hits are never decided here.
 *
 * <p>A lash swings along its caster's whip aim, not their look. The local caster's comes from {@link LashCamera} and is
 * sent to the server each tick; another caster's is what the server relays. The local copy reads its own flicks and
 * tells the server of each throw; a watcher's copy guesses from the synced look and is put right by each cue's phase.
 */
public final class StrandClientState {
	/** Ticks a strand fades for after it is let go. */
	public static final int FADE_TICKS = 4;
	private static final int TRAIL = 6;
	/** Ticks the local lash may stay out against a server that says it is still charging before it gives way. */
	private static final int LOCAL_PHASE_PATIENCE = 15;
	/** The numbers a lash runs on when its caster's selection is not known here. */
	private static final LashProfile UNKNOWN_LASH = new LashProfile(4.5F, 20.0D, 12, 0, 30.0D, 30, 1.5F);
	private static final Map<Integer, Strand> STRANDS = new HashMap<>();

	private StrandClientState() {
	}

	/** One caster's strand as this client draws it. */
	public static final class Strand {
		public final int entityId;
		public StrandCue.Decoded cue;
		/** The strand's length, as the server worked it out (the verb's reach on the hand or Staff, by the form). */
		public double length;
		/** The plant's point, from the cue. */
		public Vec3 point;
		public long expires;
		/** The game time it was let go, or -1 while it is out. */
		public long released = -1;
		public final LashRules.State lash = new LashRules.State();
		/** The twin lash from the off hand (Second Strand), stepped only while the cue says the twin is out. */
		public final LashRules.State twinLash = new LashRules.State();
		/** The whip aim a live or fading lash swings along. */
		public float aimYaw, aimPitch;
		/** Ticks this copy of the lash has been out of the orb. */
		int outTicks;
		public final JetRules.State jet = new JetRules.State();
		public final BrandRules.State brand = new BrandRules.State();
		/** Where the strand ends this tick and last (a jet's or brand's contact, a spray's reach). */
		public Vec3 end, endPrevious;
		/** The tip's recent positions, newest first: the lash's swish. */
		public final Deque<Vec3> trail = new ArrayDeque<>();
		/** The twin tip's recent positions. */
		public final Deque<Vec3> twinTrail = new ArrayDeque<>();
		/** The lash's last turn rate in degrees a tick and its stroke direction, for the first-person arm. */
		public double turnRate;
		public boolean reversed;
		public boolean onCreature;

		Strand(int entityId) {
			this.entityId = entityId;
		}

		public StrandForm form() {
			return cue.form();
		}

		/** How faded the strand is, from 1 (out) to 0 (gone), at {@code time}. */
		public float presence(double time) {
			if (released < 0) return 1.0F;
			return (float) Math.max(0.0D, 1.0D - (time - released) / FADE_TICKS);
		}
	}

	public static void accept(ManipulationVisualPacket packet, ClientLevel level) {
		if (packet.entityId() < 0) return;
		long now = level.getGameTime();
		Strand strand = STRANDS.get(packet.entityId());
		if (packet.ticks() <= 0) {
			if (strand != null && strand.released < 0) strand.released = now;
			return;
		}
		StrandCue.Decoded cue = StrandCue.decode(packet.count());
		Entity caster = level.getEntity(packet.entityId());
		// A pulse already on its way when the local caster let go would bring back a lash they no longer hold.
		if (cue.form() == StrandForm.LASH && caster instanceof Player player && player == Minecraft.getInstance().player
				&& !holdsProjection(player)) return;
		if (strand == null || strand.released >= 0 || strand.cue.form() != cue.form() || strand.cue.twin() != cue.twin()) {
			strand = new Strand(packet.entityId());
			// Until the server's first aim arrives, a watcher's whip points along the caster's look, not due south.
			if (caster != null) {
				strand.aimYaw = caster.getYRot();
				strand.aimPitch = caster.getXRot();
			}
			STRANDS.put(packet.entityId(), strand);
		}
		strand.cue = cue;
		if (cue.form() == StrandForm.LASH) adoptPhase(level, strand);
		strand.length = Math.max(1.0D, packet.radius());
		strand.point = cue.form() == StrandForm.PLANT ? packet.to() : null;
		strand.expires = now + packet.ticks();
	}

	/**
	 * Brings a lash into line with the phase its cue reports. A watcher's copy takes the server's word at once. The
	 * local caster's is ahead of the server, which hears of a flick a moment after this client reads it, so it gives
	 * way only where the server knows more: a throw that met something early, or a lash the server never let out.
	 */
	private static void adoptPhase(ClientLevel level, Strand strand) {
		LashRules.Phase server = strand.cue.phase(), local = strand.lash.phase();
		Entity caster = level.getEntity(strand.entityId);
		boolean mine = caster != null && caster == Minecraft.getInstance().player;
		if (mine) {
			boolean struckEarly = local == LashRules.Phase.THROWING
					&& (server == LashRules.Phase.LIVE || server == LashRules.Phase.FADING);
			boolean neverOut = server == LashRules.Phase.CHARGING && local != LashRules.Phase.CHARGING
					&& strand.outTicks > LOCAL_PHASE_PATIENCE;
			if (!struckEarly && !neverOut) return;
		}
		LashProfile profile = profile(caster);
		LashRules.adopt(strand.lash, server, strand.cue.param(), profile);
		LashRules.adopt(strand.twinLash, server, strand.cue.param(), profile);
	}

	/** The rope and signature of the lash {@code caster} has selected, as far as this client knows it. */
	public static com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashStyle style(Entity caster) {
		LashProfile profile = profile(caster);
		return (profile == null ? UNKNOWN_LASH : profile).style();
	}

	/** The lash numbers {@code caster}'s selected manipulation carries, as far as this client knows them. */
	private static LashProfile profile(Entity caster) {
		if (!(caster instanceof Player player)) return UNKNOWN_LASH;
		return SculptingDispatcher.selected(player, Verb.PROJECT)
				.map(selected -> selected.sculpting() instanceof StrandSculpting strand ? strand.spec().lash() : null)
				.orElse(UNKNOWN_LASH);
	}

	/** A watched caster's whip aim, as the server holds it. */
	public static void acceptAim(int entityId, float yaw, float pitch) {
		Strand strand = STRANDS.get(entityId);
		if (strand == null || strand.released >= 0) return;
		strand.aimYaw = yaw;
		strand.aimPitch = pitch;
	}

	/**
	 * The attack button while the local caster's lash is held: the same release the server makes, made here at once so
	 * the strand answers the click. Returns whether a lash was there to answer.
	 */
	public static boolean localSnap(Player player) {
		Strand strand = STRANDS.get(player.getId());
		if (strand == null || strand.released >= 0 || strand.cue == null || strand.form() != StrandForm.LASH) return false;
		long now = player.level().getGameTime();
		if (!LashRules.maySnap(strand.lash, now)) return false;
		LashRules.release(strand.lash, player.getYRot(), player.getXRot(), now);
		if (strand.cue.twin()) LashRules.release(strand.twinLash, player.getYRot(), player.getXRot(), now);
		strand.aimYaw = player.getYRot();
		strand.aimPitch = player.getXRot();
		LashCamera.recentre();
		return true;
	}

	/**
	 * One client tick: every strand follows its caster's look by the shared rules, and lapsed ones fade out. The local
	 * caster's lash is let go here the tick they stop holding Projection, so the camera cap and the aim stream end at
	 * once instead of a round trip later.
	 */
	public static void tick(ClientLevel level) {
		Player me = Minecraft.getInstance().player;
		long now = level.getGameTime();
		Strand own = me == null ? null : STRANDS.get(me.getId());
		if (own != null && own.released < 0 && own.cue != null && own.form() == StrandForm.LASH && !holdsProjection(me)) {
			own.released = now;
		}
		if (me == null || !has(me)) LashCamera.reset();
		if (STRANDS.isEmpty()) return;
		STRANDS.values().removeIf(strand -> {
			Entity entity = level.getEntity(strand.entityId);
			if (strand.released < 0 && (entity == null || !entity.isAlive() || now > strand.expires)) strand.released = now;
			if (strand.released >= 0) return now - strand.released > FADE_TICKS;
			if (entity instanceof LivingEntity caster) follow(level, caster, strand, now);
			return false;
		});
	}

	private static void follow(ClientLevel level, LivingEntity caster, Strand strand, long now) {
		Vec3 eye = caster.getEyePosition(), look = caster.getLookAngle();
		Vec3 hand = StrandCue.hand(eye, look, caster.getYRot(), strand.cue.leftArm());
		strand.endPrevious = strand.end;
		switch (strand.form()) {
			case LASH -> lash(level, caster, strand, eye, look, hand, now);
			case JET, BRAND -> {
				VerbTarget target = VerbTargeting.pick(level, caster, strand.length,
						EnumSet.of(VerbTargetKind.CREATURE, VerbTargetKind.BLOCK, VerbTargetKind.AIR)).orElse(null);
				int id = target instanceof VerbTarget.Creature creature ? creature.living().getId() : -1;
				strand.onCreature = id >= 0;
				strand.end = target == null ? eye.add(look.scale(strand.length)) : target instanceof VerbTarget.Creature creature
						? creature.living().getBoundingBox().getCenter() : target.point();
				if (strand.form() == StrandForm.JET) JetRules.tick(strand.jet, id, StrandSkills.quicken(strand.cue.skill()));
				else BrandRules.tick(strand.brand, id, StrandSkills.brandSetTicks(strand.cue.skill()));
			}
			case SPRAY -> strand.end = clip(level, caster, eye, eye.add(look.scale(strand.length)));
			case PLANT -> strand.end = strand.point;
		}
		if (strand.endPrevious == null) strand.endPrevious = strand.end;
	}

	private static void lash(ClientLevel level, LivingEntity caster, Strand strand, Vec3 eye, Vec3 look, Vec3 hand, long now) {
		boolean mine = caster == Minecraft.getInstance().player;
		LashRules.State lash = strand.lash;
		LashRules.Phase before = lash.phase();
		boolean wasOut = before == LashRules.Phase.LIVE || before == LashRules.Phase.FADING;
		if (mine && wasOut && LashCamera.active()) {
			strand.aimYaw = LashCamera.aimYaw();
			strand.aimPitch = LashCamera.aimPitch();
		}
		LashProfile profile = profile(caster);
		// The caster's Taut Line rides on the cue, so this client runs their lash, not its own.
		double taut = StrandSkills.taut(strand.cue.skill());
		double turned = lash.strokeTurn();
		LashRules.Step step = LashRules.step(lash, hand, caster.getYRot(), caster.getXRot(), strand.aimYaw, strand.aimPitch,
				strand.length, now, (from, to) -> clip(level, caster, from, to), taut, profile);
		// The rules own the aim while the lash charges and through the throw.
		if (!wasOut) {
			strand.aimYaw = lash.aimYaw();
			strand.aimPitch = lash.aimPitch();
		}
		strand.outTicks = lash.phase() == LashRules.Phase.CHARGING ? 0 : strand.outTicks + 1;
		strand.turnRate = step.turnRate();
		strand.reversed = !step.resting() && lash.strokeTurn() < turned;
		if (lash.seeded()) {
			strand.trail.addFirst(lash.tip());
			while (strand.trail.size() > TRAIL) strand.trail.removeLast();
			strand.end = lash.tip();
		} else {
			strand.trail.clear();
			strand.end = hand;
		}
		if (strand.cue.twin()) {
			Vec3 off = StrandCue.hand(eye, look, caster.getYRot(), !strand.cue.leftArm());
			LashRules.step(strand.twinLash, off, caster.getYRot(), caster.getXRot(), strand.aimYaw, strand.aimPitch, strand.length,
					now, (from, to) -> clip(level, caster, from, to), taut * StrandSkills.TWIN_TAUT, profile);
			if (strand.twinLash.seeded()) {
				strand.twinTrail.addFirst(strand.twinLash.tip());
				while (strand.twinTrail.size() > TRAIL) strand.twinTrail.removeLast();
			} else {
				strand.twinTrail.clear();
			}
		}
		if (!mine) return;
		LashCamera.follow(Minecraft.getInstance().player, lash.phase(), lash.aimYaw(), lash.aimPitch());
		if (step.threw()) {
			// This client read the flick; the server, which reads none, is told which way and round what look.
			PacketHandler.sendToServer(new LashAimPacket(lash.throwCentreYaw(), lash.throwCentrePitch(), (float) lash.throwX(),
					(float) lash.throwY()));
		} else if (wasOut && (lash.phase() == LashRules.Phase.LIVE || lash.phase() == LashRules.Phase.FADING)) {
			PacketHandler.sendToServer(new LashAimPacket(strand.aimYaw, strand.aimPitch));
		}
	}

	private static boolean holdsProjection(Player player) {
		return VerbChannel.channelling(player).orElse(null) == Verb.PROJECT;
	}

	private static Vec3 clip(ClientLevel level, Entity caster, Vec3 from, Vec3 to) {
		BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
		if (hit.getType() != HitResult.Type.BLOCK) return to;
		Vec3 back = from.subtract(to);
		return hit.getLocation().add(back.lengthSqr() < 1e-8 ? Vec3.ZERO : back.normalize().scale(0.05D));
	}

	public static Strand of(int entityId) {
		return STRANDS.get(entityId);
	}

	/** Whether {@code entity} has a strand out now: its hand pours no ordinary stream meanwhile. */
	public static boolean has(Entity entity) {
		Strand strand = STRANDS.get(entity.getId());
		return strand != null && strand.released < 0;
	}

	public static Collection<Strand> all() {
		return STRANDS.values();
	}

	public static void clear() {
		STRANDS.clear();
	}
}
