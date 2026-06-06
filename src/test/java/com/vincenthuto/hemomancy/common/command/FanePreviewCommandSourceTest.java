package com.vincenthuto.hemomancy.common.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FanePreviewCommandSourceTest {
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java");
	private static final Path EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/FoundingFaneEvents.java");

	private FanePreviewCommandSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String events = Files.readString(EVENTS).replace("\r\n", "\n");

		assertContains("command exposes fane root", command, "Commands.literal(\"fane\")");
		assertContains("command exposes preview branch", command, "Commands.literal(\"preview\")");
		assertContains("command exposes member preview", command, "Commands.literal(\"member\")");
		assertContains("command exposes mundane preview", command, "Commands.literal(\"mundane\")");
		assertContains("command exposes outsider preview", command, "Commands.literal(\"outsider\")");
		assertContains("command exposes rival preview", command, "Commands.literal(\"rival\")");
		assertContains("command exposes clear preview", command, "Commands.literal(\"clear\")");
		assertContains("command sets member override", command,
				"setFanePreview(ctx.getSource(), FaneBoundaryRelation.MEMBER)");
		assertContains("command sets mundane override", command,
				"setFanePreview(ctx.getSource(),\n\t\t\t\t\t\t\t\t\t\t\t\tFaneBoundaryRelation.MUNDANE_OUTSIDER)");
		assertContains("command clears override", command, "clearFanePreview(ctx.getSource())");
		assertContains("events store per-viewer preview relation", events,
				"FANE_BOUNDARY_PREVIEWS");
		assertContains("events use preview relation during sync", events,
				"previewRelation(viewer).orElseGet");
		assertContains("preview is not persisted", events, "Map<UUID, FaneBoundaryRelation>");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
