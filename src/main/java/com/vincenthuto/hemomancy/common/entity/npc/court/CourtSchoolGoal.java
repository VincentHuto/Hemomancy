package com.vincenthuto.hemomancy.common.entity.npc.court;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;

/**
 * Swimming with the school. The leader swims its loop waypoint by waypoint, skipping any that isn't open water (a
 * seamount, a shelf) and any it can't reach; the others hold their stations behind it and speed up when they fall
 * behind. A school whose leader is gone for a few seconds takes the member with the lowest id as its new leader, so
 * the school keeps moving. Fighting a hostile comes first: the defending goal outranks this one.
 */
public final class CourtSchoolGoal extends Goal {
	/** How far a member looks for the rest of its school. */
	public static final double SCHOOL_RANGE = 48.0D;
	private static final int LEADER_LOST_TICKS = 100;

	private final CourtMemberEntity member;
	@Nullable
	private CourtMemberEntity leader;
	private int lostFor;
	private int repath;
	private int stuckChecks;
	private double lastDistance = Double.MAX_VALUE;

	public CourtSchoolGoal(CourtMemberEntity member) {
		this.member = member;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		return member.school() != null && member.isInWater() && member.getTarget() == null;
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void stop() {
		member.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (member.isSchoolLeader()) lead();
		else follow();
	}

	private void lead() {
		if (member.tickCount % 20 == 0 && yieldsLead()) return;
		BlockPos centre = member.schoolCentre();
		int radius = member.schoolRadius();
		int count = CourtSchoolRules.waypoints(radius);
		double[] point = null;
		// Skip waypoints that aren't loaded open water; give up after one lap and hold still.
		for (int tries = 0; tries < count; tries++) {
			point = CourtSchoolRules.waypoint(centre.getX() + 0.5D, centre.getZ() + 0.5D, radius, member.schoolWaypoint());
			BlockPos at = BlockPos.containing(point[0], centre.getY(), point[1]);
			if (member.level().isLoaded(at) && member.level().getFluidState(at).is(FluidTags.WATER)) break;
			member.advanceWaypoint();
			point = null;
		}
		if (point == null) return;
		double dx = point[0] - member.getX(), dz = point[1] - member.getZ();
		double distance = Math.sqrt(dx * dx + dz * dz);
		if (distance < 3.5D) {
			next();
			return;
		}
		if (++repath % 40 == 0) {
			// Not closing on the waypoint for a few checks: something is in the way, so take the next one.
			if (distance > lastDistance - 1.0D && ++stuckChecks >= 3) {
				next();
				return;
			}
			lastDistance = distance;
		}
		if (repath % 10 == 1 || member.getNavigation().isDone()) {
			member.getNavigation().moveTo(point[0], centre.getY() + 0.5D, point[1], 1.0D);
		}
	}

	private void next() {
		member.advanceWaypoint();
		stuckChecks = 0;
		lastDistance = Double.MAX_VALUE;
		repath = 0;
	}

	private void follow() {
		if (leader != null && (!leader.isAlive() || leader.isRemoved() || !leader.isSchoolLeader())) leader = null;
		// Look again at once when the leader goes, then once a second while it stays gone.
		if (member.tickCount % 20 == 0 || (leader == null && lostFor == 0)) leader = findLeader();
		if (leader == null) {
			if (++lostFor >= LEADER_LOST_TICKS) elect();
			return;
		}
		lostFor = 0;
		double[] station = CourtSchoolRules.stationAt(leader.getX(), leader.getZ(), leader.getYRot(), member.schoolSlot());
		double y = leader.getY() + station[1];
		double dx = station[0] - member.getX(), dy = y - member.getY(), dz = station[2] - member.getZ();
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		double speed = distance > 10.0D ? 1.7D : distance > 4.0D ? 1.25D : 0.95D;
		if (++repath % 8 == 0 || member.getNavigation().isDone()) {
			member.getNavigation().moveTo(station[0], y, station[2], speed);
		}
	}

	@Nullable
	private CourtMemberEntity findLeader() {
		return member.level().getEntitiesOfClass(CourtMemberEntity.class, new AABB(member.blockPosition()).inflate(SCHOOL_RANGE),
				other -> other != member && other.isAlive() && other.isSchoolLeader() && member.school().equals(other.school()))
				.stream().findFirst().orElse(null);
	}

	/**
	 * Two leaders in one school (one strayed out of sight and the rest elected another): the one with the higher id
	 * drops back into the first free station.
	 */
	private boolean yieldsLead() {
		var school = member.level().getEntitiesOfClass(CourtMemberEntity.class, new AABB(member.blockPosition()).inflate(SCHOOL_RANGE),
				other -> other != member && other.isAlive() && member.school().equals(other.school()));
		boolean outranked = school.stream().anyMatch(other -> other.isSchoolLeader() && other.getUUID().compareTo(member.getUUID()) < 0);
		if (!outranked) return false;
		int slot = 1;
		while (true) {
			int taken = slot;
			if (school.stream().noneMatch(other -> other.schoolSlot() == taken)) break;
			slot++;
		}
		member.followSchool(slot);
		member.getNavigation().stop();
		return true;
	}

	/** No leader in sight for a while: the member of the school with the lowest id leads from here. */
	private void elect() {
		lostFor = 0;
		var school = member.level().getEntitiesOfClass(CourtMemberEntity.class, new AABB(member.blockPosition()).inflate(SCHOOL_RANGE),
				other -> other.isAlive() && member.school().equals(other.school()));
		school.stream().min(Comparator.comparing(CourtMemberEntity::getUUID)).ifPresent(first -> {
			if (first == member) member.leadSchool();
		});
	}
}
