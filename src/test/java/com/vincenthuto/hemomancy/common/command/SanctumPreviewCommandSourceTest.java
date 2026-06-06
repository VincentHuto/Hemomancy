package com.vincenthuto.hemomancy.common.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SanctumPreviewCommandSourceTest {
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java");
	private static final Path EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/FoundingSanctumEvents.java");

	private SanctumPreviewCommandSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String events = Files.readString(EVENTS).replace("\r\n", "\n");

		assertContains("command exposes sanctum root", command, "Commands.literal(\"sanctum\")");
		assertContains("command exposes preview branch", command, "Commands.literal(\"preview\")");
		assertContains("command exposes member preview", command, "Commands.literal(\"member\")");
		assertContains("command exposes mundane preview", command, "Commands.literal(\"mundane\")");
		assertContains("command exposes outsider preview", command, "Commands.literal(\"outsider\")");
		assertContains("command exposes rival preview", command, "Commands.literal(\"rival\")");
		assertContains("command exposes clear preview", command, "Commands.literal(\"clear\")");
		assertContains("command sets member override", command,
				"setSanctumPreview(ctx.getSource(), SanctumBoundaryRelation.MEMBER)");
		assertContains("command sets mundane override", command,
				"setSanctumPreview(ctx.getSource(),\n\t\t\t\t\t\t\t\t\t\t\t\tSanctumBoundaryRelation.MUNDANE_OUTSIDER)");
		assertContains("command clears override", command, "clearSanctumPreview(ctx.getSource())");
		assertContains("events store per-viewer preview relation", events,
				"SANCTUM_BOUNDARY_PREVIEWS");
		assertContains("events use preview relation during sync", events,
				"previewRelation(viewer).orElseGet");
		assertContains("preview is not persisted", events, "Map<UUID, SanctumBoundaryRelation>");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
