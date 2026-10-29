package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.MausoleumWardRenderer;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.MausoleumWardRules;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What the client remembers of a mausoleum's ward answering: the last few strikes (where, which face, how hard, how
 * old), which the ward shader rings out from, and the Apotheos bodies the hallowed ground is burning. Strikes in quick
 * succession on the same spot flash harder ({@link MausoleumWardRules#flashStrength}); a burn lingers a moment after
 * its last pulse, which comes about once a second while it lasts.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MausoleumWardClientState {
	private static final int MAX_STRIKES = 12;
	/** Strikes this close (squared, in blocks) to an earlier one in the window escalate it. */
	private static final double SAME_SPOT_SQR = 2.25;
	/** How long a burn holds after its last pulse. */
	private static final int BURN_LINGER_TICKS = 30;
	private static final int BURN_FADE_IN_TICKS = 10;
	private static final int BURN_FADE_OUT_TICKS = 12;
	/** The colour a burn or strike takes when no loaded ward claims it. */
	private static final int UNCLAIMED_COLOR = 0xE8D8B0;
	private static final ParticleColor SOOT = new ParticleColor(18, 12, 10);
	private static final ParticleColor CHAR = new ParticleColor(255, 92, 40);

	private static final List<Strike> STRIKES = new ArrayList<>();
	private static final Int2ObjectOpenHashMap<Burn> BURNING = new Int2ObjectOpenHashMap<>();
	private static ClientLevel world;

	private MausoleumWardClientState() {
	}

	/** One strike on the ward. */
	public static final class Strike {
		private final BlockPos pos;
		private final Direction face;
		private final WardStrikePacket.Kind kind;
		private final float power;
		private int age;

		Strike(BlockPos pos, Direction face, WardStrikePacket.Kind kind, float power) {
			this.pos = pos;
			this.face = face;
			this.kind = kind;
			this.power = power;
		}

		public BlockPos pos() {
			return pos;
		}

		public Direction face() {
			return face;
		}

		public boolean threshold() {
			return kind == WardStrikePacket.Kind.THRESHOLD;
		}

		public float power() {
			return power;
		}

		public int age() {
			return age;
		}

		/** Where it landed: just off the centre of the struck face. */
		public Vec3 origin() {
			return Vec3.atCenterOf(pos).add(face.getStepX() * 0.51, face.getStepY() * 0.51, face.getStepZ() * 0.51);
		}

		/** Its life from 0 (landing) to 1 (gone) at a partial tick. */
		public float life(float partialTick) {
			return Mth.clamp((age + partialTick) / MausoleumWardRules.STRIKE_LIFE_TICKS, 0.0F, 1.0F);
		}
	}

	private static final class Burn {
		private final int color;
		private int left;
		private int burned;

		Burn(int color) {
			this.color = color;
		}
	}

	/** The strikes still ringing, oldest first. */
	public static List<Strike> strikes() {
		return Collections.unmodifiableList(STRIKES);
	}

	public static boolean hasStrikes() {
		return !STRIKES.isEmpty();
	}

	public static void accept(WardStrikePacket packet) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) return;
		reset(level);
		WardAnchorBlockEntity anchor = MausoleumWardRenderer.anchorAt(level, packet.pos());
		int color = anchor == null ? UNCLAIMED_COLOR : MausoleumWardRules.wardColor(anchor.saint());
		if (packet.kind() == WardStrikePacket.Kind.BURN) {
			if (packet.entityId() < 0) return;
			Burn burn = BURNING.get(packet.entityId());
			if (burn == null) {
				burn = new Burn(color);
				BURNING.put(packet.entityId(), burn);
			}
			burn.left = BURN_LINGER_TICKS;
			return;
		}
		int inWindow = 1;
		for (Strike strike : STRIKES) {
			if (strike.age < MausoleumWardRules.STRIKE_WINDOW_TICKS
					&& strike.pos.distSqr(packet.pos()) <= SAME_SPOT_SQR) inWindow++;
		}
		Strike strike = new Strike(packet.pos().immutable(), packet.face(), packet.kind(),
				MausoleumWardRules.flashStrength(inWindow));
		if (STRIKES.size() >= MAX_STRIKES) STRIKES.removeFirst();
		STRIKES.add(strike);
		MausoleumWardRenderer.invalidate(packet.pos());
		motes(level, strike, color);
	}

	/**
	 * A few fine motes shaken loose from the struck face, each on a 1/16 pixel of it, falling; more for a harder
	 * blow. At the threshold they hang in the sheet where the visitor touched it and sink.
	 */
	private static void motes(ClientLevel level, Strike strike, int color) {
		RandomSource random = level.random;
		ParticleColor mote = new ParticleColor((color >> 16) & 255, (color >> 8) & 255, color & 255);
		Direction face = strike.face;
		Vec3 centre = strike.origin();
		int count = 3 + Math.round(strike.power * 2.0F) + (strike.threshold() ? 4 : 0);
		double spread = strike.threshold() ? 1.4 : 0.5;
		for (int i = 0; i < count; i++) {
			double a = Math.floor((random.nextDouble() - 0.5) * spread * 16.0) / 16.0 + 1.0 / 32.0;
			double b = Math.floor((random.nextDouble() - 0.5) * spread * 16.0) / 16.0 + 1.0 / 32.0;
			Vec3 at = switch (face.getAxis()) {
				case X -> centre.add(0.0, a, b);
				case Y -> centre.add(a, 0.0, b);
				case Z -> centre.add(a, b, 0.0);
			};
			double out = 0.004 + random.nextDouble() * 0.006;
			level.addParticle(HemoParticleData.glow(mote), at.x, at.y, at.z, face.getStepX() * out,
					face.getStepY() * out - 0.008 - random.nextDouble() * 0.01, face.getStepZ() * out);
		}
	}

	/** How far the hallowed ground's burn has taken {@code entityId}, from 0 to 1; 0 when it is not burning. */
	public static float burn(int entityId, float partialTick) {
		Burn burn = BURNING.get(entityId);
		if (burn == null) return 0.0F;
		float in = Mth.clamp((burn.burned + partialTick) / BURN_FADE_IN_TICKS, 0.0F, 1.0F);
		float out = Mth.clamp((burn.left - partialTick) / BURN_FADE_OUT_TICKS, 0.0F, 1.0F);
		return Math.min(in, out);
	}

	/** The ward colour burning into {@code entityId} (0xRRGGBB). */
	public static int burnColor(int entityId) {
		Burn burn = BURNING.get(entityId);
		return burn == null ? UNCLAIMED_COLOR : burn.color;
	}

	public static boolean anyBurning() {
		return !BURNING.isEmpty();
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (STRIKES.isEmpty() && BURNING.isEmpty()) return;
		if (mc.level == null) {
			clear();
			return;
		}
		reset(mc.level);
		if (mc.isPaused()) return;
		STRIKES.removeIf(strike -> ++strike.age >= MausoleumWardRules.STRIKE_LIFE_TICKS);
		var burns = BURNING.int2ObjectEntrySet().iterator();
		while (burns.hasNext()) {
			var entry = burns.next();
			Burn burn = entry.getValue();
			Entity entity = mc.level.getEntity(entry.getIntKey());
			if (--burn.left <= 0 || entity == null || !entity.isAlive()) {
				burns.remove();
				continue;
			}
			burn.burned++;
			smoke(mc, entity, burn);
		}
	}

	/**
	 * Smoke rising off a burning body: a thread of soot every tick and a rare ember, from the lower body only when it
	 * is the viewer's own in first person, so it never fogs the view.
	 */
	private static void smoke(Minecraft mc, Entity entity, Burn burn) {
		if (entity.isInvisible()) return;
		RandomSource random = entity.getRandom();
		AABB box = entity.getBoundingBox();
		boolean ownView = entity == mc.getCameraEntity() && mc.options.getCameraType() == CameraType.FIRST_PERSON;
		double top = ownView ? 0.55 : 0.95;
		double x = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
		double y = box.minY + box.getYsize() * random.nextDouble() * top;
		double z = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);
		mc.level.addParticle(HemoParticleData.darkGlow(SOOT), x, y, z, (random.nextDouble() - 0.5) * 0.004,
				0.012 + random.nextDouble() * 0.012, (random.nextDouble() - 0.5) * 0.004);
		if (burn.burned % 5 == 0) {
			mc.level.addParticle(HemoParticleData.emberSpark(CHAR, 0.05F, 0.8F, 14), x, y, z, 0.0,
					0.02 + random.nextDouble() * 0.01, 0.0);
		}
	}

	private static void reset(ClientLevel level) {
		if (world != level) {
			STRIKES.clear();
			BURNING.clear();
			world = level;
		}
	}

	/** On logout: strikes and burns belong to the world they were seen in, and entity ids are reused. */
	public static void clear() {
		STRIKES.clear();
		BURNING.clear();
		world = null;
	}
}
