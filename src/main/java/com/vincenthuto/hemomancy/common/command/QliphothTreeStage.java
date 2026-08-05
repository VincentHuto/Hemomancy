package com.vincenthuto.hemomancy.common.command;

import com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState;

import java.util.Locale;

/** Command-facing presets for the Qliphoth's visual lifecycle. */
public enum QliphothTreeStage {
	INITIAL("initial", 0, SeveredQliphothState.LIVING),
	STAGE_1("1", 1, SeveredQliphothState.LIVING),
	STAGE_2("2", 2, SeveredQliphothState.LIVING),
	STAGE_3("3", 3, SeveredQliphothState.LIVING),
	STAGE_4("4", 4, SeveredQliphothState.LIVING),
	STAGE_5("5", 5, SeveredQliphothState.LIVING),
	STAGE_6("6", 6, SeveredQliphothState.LIVING),
	STAGE_7("7", 7, SeveredQliphothState.LIVING),
	STAGE_8("8", 8, SeveredQliphothState.LIVING),
	STAGE_9("9", 9, SeveredQliphothState.LIVING),
	FULLY_GROWN("fully_grown", 9, SeveredQliphothState.LIVING),
	PRUNED("pruned", 9, SeveredQliphothState.OPEN),
	SEALED("sealed", 9, SeveredQliphothState.SEALED);

	private final String commandName;
	private final int pomesDropped;
	private final SeveredQliphothState severedState;

	QliphothTreeStage(String commandName, int pomesDropped, SeveredQliphothState severedState) {
		this.commandName = commandName;
		this.pomesDropped = pomesDropped;
		this.severedState = severedState;
	}

	public String commandName() {
		return commandName;
	}

	public int pomesDropped() {
		return pomesDropped;
	}

	public SeveredQliphothState severedState() {
		return severedState;
	}

	public static QliphothTreeStage parse(String rawStage) {
		if (rawStage == null) {
			return null;
		}

		String stage = rawStage.trim().toLowerCase(Locale.ROOT).replace('-', '_');
		for (QliphothTreeStage candidate : values()) {
			if (candidate.commandName.equals(stage)) {
				return candidate;
			}
		}

		return switch (stage) {
			case "0", "stage_0", "start", "seed" -> INITIAL;
			case "stage_1" -> STAGE_1;
			case "stage_2" -> STAGE_2;
			case "stage_3" -> STAGE_3;
			case "stage_4" -> STAGE_4;
			case "stage_5" -> STAGE_5;
			case "stage_6" -> STAGE_6;
			case "stage_7" -> STAGE_7;
			case "stage_8" -> STAGE_8;
			case "stage_9", "full", "grown", "max" -> FULLY_GROWN;
			default -> null;
		};
	}
}
