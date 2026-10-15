package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Living Staff threading an engram. The staff's ray lands on the ground the engram is draped over (or, over a gap,
 * on the plane of the loom's floor) and snaps to the nearest node. Pressing takes or threads a lane, holding sweeps
 * it along, a sneaking press cuts it back. The server reads the weaver's own look every tick the staff is held, so
 * no packet carries an aim; the client runs the same maths to keep the staff from opening a blood channel.
 */
public final class EngramTracing {
	/**
	 * What the staff points at: the loom, the node (or -1 between nodes), the point in sigil space, and the block face
	 * it touched in the world with where (both null where the ray only met the floor's plane over a gap).
	 */
	public record Aim(SomaticLoomBlockEntity loom, int node, double sx, double sz, @Nullable Vec3 point, @Nullable Direction face) {}

	private static final Map<UUID, BlockPos> SERVER_TRACES = new ConcurrentHashMap<>();
	private static final Map<UUID, BlockPos> CLIENT_TRACES = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> LAST_NODE = new ConcurrentHashMap<>();
	/** How far above or below the loom's floor the draped engram can lie and still be pointed at on the ground. */
	private static final double GROUND_BAND = 2.6D;
	/** Looms farther than this are never considered. */
	private static final double SEARCH_RADIUS = 18.0D;

	private EngramTracing() {
	}

	private static Map<UUID, BlockPos> traces(Entity player) {
		return player.level().isClientSide ? CLIENT_TRACES : SERVER_TRACES;
	}

	/** Whether this player's staff is threading an engram now: it opens no blood channel while it does. */
	public static boolean isTracing(Entity player) {
		if (player == null || !traces(player).containsKey(player.getUUID())) return false;
		// A trace outlives nothing: once the staff is no longer in use it is over, whatever missed telling us.
		return !(player instanceof net.minecraft.world.entity.LivingEntity living) || living.isUsingItem();
	}

	/** The engram this player weaves and points at within `reach`, if any. Only a loom's own weaver gets an aim. */
	@Nullable
	public static Aim aim(Player player, boolean listening) {
		for (BlockPos pos : ActiveEngramLooms.near(player.level(), player.blockPosition(), SEARCH_RADIUS)) {
			if (!(player.level().getBlockEntity(pos) instanceof SomaticLoomBlockEntity loom) || !loom.isWeaving()
					|| loom.getEngram() == null) {
				ActiveEngramLooms.untrack(player.level(), pos);
				continue;
			}
			if (!player.getUUID().equals(loom.getWeaver())) continue;
			int stage = loom.upgradeTier();
			double reach = listening ? EngramWeaveRules.listenReach(stage) : EngramWeaveRules.traceReach(stage);
			Aim aim = aimAt(player, loom, reach);
			if (aim != null) return aim;
		}
		return null;
	}

	@Nullable
	private static Aim aimAt(Player player, SomaticLoomBlockEntity loom, double reach) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getViewVector(1.0F);
		Vec3 centre = loom.sigilCentre();
		double radius = loom.sigilRadius();
		double x, z;
		Vec3 point = null;
		BlockHitResult hit = player.level().clip(new ClipContext(eye, eye.add(look.scale(reach)), ClipContext.Block.OUTLINE,
				ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.BLOCK && hit.getDirection() == Direction.UP
				&& Math.abs(hit.getLocation().y - centre.y) <= GROUND_BAND) {
			point = hit.getLocation();
			x = point.x;
			z = point.z;
		} else if (hit.getType() == HitResult.Type.BLOCK && isLoom(player, loom, hit.getBlockPos())) {
			// Pointing at the loom itself is pointing at the Vessel.
			return new Aim(loom, EngramSigilGeometry.VESSEL, 0, 0, hit.getLocation(), hit.getDirection());
		} else {
			double[] plane = EngramAim.planeHit(eye.x, eye.y, eye.z, look.x, look.y, look.z, centre.y, reach);
			if (plane == null) return null;
			x = plane[0];
			z = plane[1];
		}
		double[] sigil = EngramAim.toSigil(x - centre.x, z - centre.z, loom.sigilTurns());
		if (Math.hypot(sigil[0], sigil[1]) > radius + 1.5D) return null;
		int node = EngramAim.nearestNode(sigil[0], sigil[1], radius, EngramWeaveRules.snapRadius(loom.upgradeTier()));
		return new Aim(loom, node, sigil[0], sigil[1], point, point == null ? null : Direction.UP);
	}

	private static boolean isLoom(Player player, SomaticLoomBlockEntity loom, BlockPos pos) {
		if (pos.equals(loom.getBlockPos())) return true;
		return player.level().getBlockEntity(pos) instanceof com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity filler
				&& loom.getBlockPos().equals(filler.getMainBlockPos());
	}

	/**
	 * The staff was pressed. If its weaver points at their engram, the press takes or threads a lane (or, sneaking,
	 * cuts one) and holding begins a trace; otherwise the staff carries on as usual.
	 */
	public static boolean begin(Player player, boolean sneaking) {
		Aim aim = aim(player, false);
		if (aim == null || aim.node() < 0) return false;
		if (!player.level().isClientSide) aim.loom().engramStep(player, aim.node(), sneaking, true);
		if (sneaking) return true;
		traces(player).put(player.getUUID(), aim.loom().getBlockPos());
		if (!player.level().isClientSide) LAST_NODE.put(player.getUUID(), aim.node());
		return true;
	}

	/** Each tick the staff is held: the lane follows the node the staff now points at. */
	public static void tick(Player player) {
		BlockPos pos = traces(player).get(player.getUUID());
		if (pos == null) return;
		if (!(player.level().getBlockEntity(pos) instanceof SomaticLoomBlockEntity loom) || !loom.isWeaving()) {
			end(player);
			return;
		}
		if (player.level().isClientSide) return;
		Aim aim = aimAt(player, loom, EngramWeaveRules.traceReach(loom.upgradeTier()));
		if (aim == null || aim.node() < 0) return;
		Integer last = LAST_NODE.get(player.getUUID());
		if (last != null && last == aim.node()) return;
		LAST_NODE.put(player.getUUID(), aim.node());
		loom.engramStep(player, aim.node(), false, false);
	}

	/** The staff was let go. */
	public static boolean end(Player player) {
		BlockPos pos = traces(player).remove(player.getUUID());
		if (!player.level().isClientSide) LAST_NODE.remove(player.getUUID());
		if (pos == null) return false;
		if (!player.level().isClientSide && player.level().getBlockEntity(pos) instanceof SomaticLoomBlockEntity loom)
			loom.releaseEngram(player);
		return true;
	}

	public static void clear() {
		SERVER_TRACES.clear();
		CLIENT_TRACES.clear();
		LAST_NODE.clear();
	}

	public static void clearClient() {
		CLIENT_TRACES.clear();
	}
}
