package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * The professors whose echoes perform in the Ringmaster's dark (2026-11-18): only those whose ordeal this player passed,
 * which is the summon the ordeal teaches (owner). Sent to clients as a role mask on his stage copy, one bit per
 * professor in roster order.
 */
public final class FinaleEchoes {
	private FinaleEchoes() {
	}

	/** The echoes' stations: one fixed angle per professor in roster order, this far round the copy's ring. */
	public static final double STATION_RADIUS = 22.0D;

	/** Where the echo of roster professor {@code index} stands round {@code centre}, on the ring's own floor. */
	public static Vec3 station(Vec3 centre, int index) {
		int stations = OrdealProfessor.all().size();
		double angle = Math.PI * 2.0D * index / stations + Math.PI / stations;
		return centre.add(Math.cos(angle) * STATION_RADIUS, 0.0D, Math.sin(angle) * STATION_RADIUS);
	}

	/** The professors {@code player} has passed, in roster order. */
	public static List<OrdealProfessor> passed(Player player) {
		List<OrdealProfessor> passed = new ArrayList<>();
		for (OrdealProfessor professor : OrdealProfessor.all())
			if (CircusApprenticeshipProgress.knows(player, professor.summon())) passed.add(professor);
		return passed;
	}

	public static int roleMask(List<OrdealProfessor> professors) {
		List<OrdealProfessor> roster = OrdealProfessor.all();
		int mask = 0;
		for (OrdealProfessor professor : professors) {
			int bit = roster.indexOf(professor);
			if (bit >= 0) mask |= 1 << bit;
		}
		return mask;
	}

	/** The roles in {@code mask}, in roster order. */
	public static List<String> roles(int mask) {
		List<OrdealProfessor> roster = OrdealProfessor.all();
		List<String> roles = new ArrayList<>();
		for (int bit = 0; bit < roster.size(); bit++)
			if ((mask & 1 << bit) != 0) roles.add(roster.get(bit).role());
		return roles;
	}
}
