package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * One door for held Blood Absorption and Projection aimed at a Saint's fight: at the Saint's body, a companion or an
 * arena set piece. {@code BloodAbsorptionItem}, {@code LivingStaffItem} and {@code BloodProjectionItem} ask it before
 * their own paths, with their own reach and rate. It asks the Saints whose arena lists the player as a challenger,
 * nearest first, through {@code SaintTrialEntity.onChannel}; the first handled result wins and the item's own paths do
 * not run that tick. A legacy Saint passes. Each Saint resolves its own targets from the request and pays only through
 * {@link #pay} with its named source.
 */
public final class SaintChannelRouter {
	public enum Kind { ABSORB, PROJECT }

	/** How far round the player Saints are asked. */
	public static final double SEARCH = 48.0D;
	public static final int CUE_GAP = 40;

	public record Request(ServerPlayer player, Kind kind, double ratePerTick, double reach, boolean livingStaff, Vec3 eye,
			Vec3 look, @Nullable EntityHitResult entity, @Nullable BlockHitResult block, int useTicks) {
	}

	public record Result(boolean handled, double blood, @Nullable Component cue) {
		public static final Result PASS = new Result(false, 0.0D, null);

		public static Result taken(double blood) {
			return new Result(true, Math.max(0.0D, blood), null);
		}

		/** Handled: no blood moves, and a rate-limited action-bar cue says why. */
		public static Result refused(Component cue) {
			return new Result(true, 0.0D, cue);
		}
	}

	private static final Map<UUID, Long> CUES = new HashMap<>();

	private SaintChannelRouter() {
	}

	/** Called after the Vesper hook in BloodAbsorptionItem.onUseTick and LivingStaffItem's absorption. */
	public static Result tryAbsorb(Level level, LivingEntity user, double reach, double ratePerTick, boolean livingStaff) {
		return route(level, user, Kind.ABSORB, reach, ratePerTick, livingStaff);
	}

	/** Called in BloodProjectionItem.performProjection after EarlyInitiation, before the Will, rite and block paths. */
	public static Result tryProject(Level level, LivingEntity user, double reach, double ratePerTick, boolean livingStaff) {
		return route(level, user, Kind.PROJECT, reach, ratePerTick, livingStaff);
	}

	/**
	 * Whether a Saint's own body may be drained by the ordinary living-target path: never a Saint, never a companion.
	 * {@code BloodAbsorptionItem.isValidAbsorptionTarget} asks this.
	 */
	public static boolean refusesOrdinaryDrain(Entity entity) {
		return entity instanceof SaintTrialEntity || entity instanceof SaintCompanion;
	}

	private static Result route(Level level, LivingEntity user, Kind kind, double reach, double rate, boolean staff) {
		if (level.isClientSide || !(user instanceof ServerPlayer player)) return Result.PASS;
		List<SaintTrialEntity> saints = new ArrayList<>(level.getEntitiesOfClass(SaintTrialEntity.class,
				new AABB(player.position(), player.position()).inflate(SEARCH),
				saint -> saint.kit() != null && saint.arena() != null && saint.arena().challengers().contains(player)));
		if (saints.isEmpty()) return Result.PASS;
		saints.sort(Comparator.comparingDouble(saint -> saint.distanceToSqr(player)));
		Request request = request(player, kind, reach, rate, staff);
		Result result = first(saints, saint -> saint.channel(request));
		if (result.handled() && result.cue() != null) cue(player, result.cue());
		return result;
	}

	/** The pure routing rule: the first handled result among candidates in order, else PASS. */
	public static <T> Result first(List<T> candidates, Function<T, Result> ask) {
		for (T candidate : candidates) {
			Result result = ask.apply(candidate);
			if (result != null && result.handled()) return result;
		}
		return Result.PASS;
	}

	private static Request request(ServerPlayer player, Kind kind, double reach, double rate, boolean staff) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		Vec3 end = eye.add(look.scale(reach));
		BlockHitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
				ClipContext.Fluid.NONE, player));
		Vec3 entityEnd = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
		EntityHitResult entity = ProjectileUtil.getEntityHitResult(player, eye, entityEnd,
				player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0D),
				candidate -> !candidate.isSpectator() && candidate.isPickable(), reach * reach);
		return new Request(player, kind, rate, reach, staff, eye, look, entity,
				block.getType() == HitResult.Type.MISS ? null : block, player.getTicksUsingItem());
	}

	/** Whether the request aims at {@code point}: within reach, inside the cone, and in clear sight if asked. */
	public static boolean aimsAt(Request request, Vec3 point, double reach, double coneDegrees, boolean needsSight) {
		if (!aimsAt(request.eye(), request.look(), point, reach, coneDegrees)) return false;
		if (!needsSight) return true;
		return request.player().level().clip(new ClipContext(request.eye(), point, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, request.player())).getType() == HitResult.Type.MISS;
	}

	/** The pure aim rule: within {@code reach} of the eye and within half of {@code coneDegrees} of the look. */
	public static boolean aimsAt(Vec3 eye, Vec3 look, Vec3 point, double reach, double coneDegrees) {
		Vec3 to = point.subtract(eye);
		double distance = to.length();
		if (distance > reach) return false;
		if (distance < 1.0E-6D) return true;
		double lookLength = look.length();
		if (lookLength < 1.0E-6D) return false;
		double cos = to.dot(look) / (distance * lookLength);
		return cos >= Math.cos(Math.toRadians(coneDegrees / 2.0D));
	}

	/** The Saint's one way to take blood from the channelling player. */
	public static BloodAccounting.Payment pay(Request request, double amount, BloodAccounting.Source source) {
		return BloodAccounting.pay(request.player(), amount, source);
	}

	/** Fills the volume up to its room and syncs it; returns what went in. */
	public static double credit(ServerPlayer player, double amount) {
		if (amount <= 0.0D) return 0.0D;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		double room = Math.max(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume());
		double added = Math.min(room, amount);
		if (added <= 0.0D) return 0.0D;
		volume.addBloodVolume(added);
		BloodVolumeEvents.syncVolume(player, volume);
		return added;
	}

	private static void cue(ServerPlayer player, Component cue) {
		long now = player.level().getGameTime();
		Long last = CUES.get(player.getUUID());
		if (last != null && now - last < CUE_GAP) return;
		CUES.put(player.getUUID(), now);
		player.displayClientMessage(cue, true);
	}

	public static void clearSessionState() {
		CUES.clear();
	}

	public static void logout(ServerPlayer player) {
		CUES.remove(player.getUUID());
	}
}
