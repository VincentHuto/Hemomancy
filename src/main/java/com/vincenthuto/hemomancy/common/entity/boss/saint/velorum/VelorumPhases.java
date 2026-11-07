package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import net.minecraft.world.BossEvent;

/**
 * Velorum's three phases (design section 3): The Hour, The Congregation and The Endless Hour. The Saint decides the
 * phase ({@link VelorumEntity#wantedPhase}): a phase floor is passed for good when the Bow he makes at it ends, or when
 * damage inside an opening carries his health under it, so the card plays as the Bow ends. A phase once reached is kept.
 */
public final class VelorumPhases {
	public static final String NAME_HOUR = "boss.hemomancy.velorum.name";
	public static final String NAME_CONGREGATION = "boss.hemomancy.velorum.phase.congregation";
	public static final String NAME_ENDLESS_HOUR = "boss.hemomancy.velorum.phase.endless_hour";

	private VelorumPhases() {
	}

	public static SaintPhaseTable table() {
		return SaintPhaseTable.bySaint(saint -> saint instanceof VelorumEntity velorum ? velorum.wantedPhase() : 1,
				new SaintPhaseTable.Phase(1, NAME_HOUR, BossEvent.BossBarColor.BLUE, 1.0F, null),
				new SaintPhaseTable.Phase(2, NAME_CONGREGATION, BossEvent.BossBarColor.WHITE, VelorumGuardRules.FLOORS[0],
						BossPresentationIds.SCENE_VELORUM_CONGREGATION_CARD),
				new SaintPhaseTable.Phase(3, NAME_ENDLESS_HOUR, BossEvent.BossBarColor.PURPLE, VelorumGuardRules.FLOORS[1],
						BossPresentationIds.SCENE_VELORUM_HOUR_CARD)).ratchet();
	}
}
