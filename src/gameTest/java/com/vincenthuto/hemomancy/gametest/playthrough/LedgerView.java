package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.mission.shared.BloodBeneathProgress;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;

/**
 * The Harbinger Assignment Ledger page as the server sent it ({@link PlaythroughPlayer#ledger()}), with the late
 * chapters worked out the way {@code HarbingerAssignmentLedgerScreen} draws them (its {@code renderLivingCovenant}
 * and {@code renderBloodBeneathBlood}), so a test can say what the player reads. Harness only: this mirrors the
 * screen, it does not move code out of it.
 */
public record LedgerView(OpenHarbingerAssignmentLedgerPacket packet) {
	public int degree() {
		return packet.degree();
	}

	public EnumArchonPath path() {
		return packet.archonPath();
	}

	/** The Living Covenant bar: Chamber return, Throne bound, Vigil completed, out of 3. */
	public int livingCovenantProgress() {
		return (packet.chamberReturned() ? 1 : 0) + (packet.covenantThroneBound() ? 1 : 0)
				+ (packet.covenantVigilCompleted() ? 1 : 0);
	}

	public boolean livingCovenantComplete() {
		return packet.livingCovenantComplete();
	}

	/** The Blood Beneath Blood bar, out of {@link #bloodBeneathTotal()}. */
	public int bloodBeneathProgress() {
		return BloodBeneathProgress.movements(packet.pomesConsumed(), packet.qliphothCommunionComplete(),
				packet.archonPath(), packet.severedPortalOpen(), packet.vesperDefeated());
	}

	public int bloodBeneathTotal() {
		return BloodBeneathProgress.TOTAL;
	}

	public boolean bloodBeneathComplete() {
		return packet.archonPath() == EnumArchonPath.SILENTIUM || packet.archonPath() == EnumArchonPath.APOTHEOS;
	}

	/** The description key the Blood Beneath card shows, chosen as the screen chooses it. */
	public String bloodBeneathKey() {
		String base = "screen.hemomancy.harbinger_assignment_ledger.blood_beneath.";
		EnumArchonPath path = packet.archonPath();
		if (path == EnumArchonPath.APOTHEOS) return base + "apotheos_complete";
		if (path == EnumArchonPath.APOTHEOS_PENDING) return base + "apotheos_pending";
		if (path == EnumArchonPath.SILENTIUM) return base + "complete";
		if (packet.severedPortalOpen()) return base + "ordeal";
		if (path == EnumArchonPath.SILENT_PENDING && packet.vesperDefeated()) return base + "absorb";
		if (path == EnumArchonPath.SILENT_PENDING) return base + "prune";
		if (packet.qliphothCommunionComplete()) return base + "revelation";
		return base + "pomes";
	}

	public String describe() {
		return "degree=" + degree() + " path=" + path() + " livingCovenant=" + livingCovenantProgress() + "/3"
				+ (livingCovenantComplete() ? "(complete)" : "") + " bloodBeneath=" + bloodBeneathProgress() + "/"
				+ bloodBeneathTotal() + " " + bloodBeneathKey() + " foundedBloodline=" + packet.foundedBloodline()
				+ " fane=" + packet.foundingFaneEstablished();
	}

	@Override
	public String toString() {
		return describe();
	}
}
