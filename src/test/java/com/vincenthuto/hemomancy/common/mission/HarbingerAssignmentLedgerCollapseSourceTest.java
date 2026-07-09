package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerAssignmentLedgerCollapseSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private HarbingerAssignmentLedgerCollapseSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		ledgerDefinesCollapsibleAssignmentSections();
		ledgerUsesDynamicAssignmentHeights();
		ledgerOrdersAssignmentsByDegree();
		ledgerSeparatesAssignmentLabelsFromTitles();
		ledgerTogglesAssignmentsFromClickHitboxes();
		ledgerProvidesGlobalCollapseControls();
	}

	private static void ledgerDefinesCollapsibleAssignmentSections() throws IOException {
		String ledger = readLedger();

		assertContains("ledger has collapsed row height", ledger, "COLLAPSED_ASSIGNMENT_HEIGHT");
		assertContains("ledger tracks collapsed assignments", ledger, "collapsedAssignments");
		assertContains("ledger tracks assignment hitboxes", ledger, "assignmentHitboxes");
		assertContains("ledger defines assignment section enum", ledger, "enum AssignmentSection");
		assertContains("ledger defines collapsed renderer", ledger, "renderCollapsedAssignment");

		for (String section : new String[] {
				"FIRST_BLOODCRAFT",
				"HERMIT_ROAD",
				"FIRST_SEPARATION",
				"RED_TAXONOMY",
				"LIVING_BESTIARY",
				"ENZYME_MASTERY",
				"WOVEN_VESSEL",
				"VEIN_MASON",
				"THE_WORN_VOW",
				"THE_THREE_ANSWERS",
				"CRIMSON_VESTMENT",
				"WEIGHT_OF_THE_FRAME",
				"THE_ASSUMED_LIMB"
		}) {
			assertContains("ledger has collapsible section " + section, ledger, section + "(");
		}
	}

	private static void ledgerUsesDynamicAssignmentHeights() throws IOException {
		String ledger = readLedger();

		assertContains("ledger computes assignment height", ledger, "assignmentHeight(AssignmentSection section)");
		assertContains("ledger renders collapsed sections before expanded content", ledger,
				"renderCollapsedAssignmentIfNeeded");
		assertContains("assignment renderer uses dynamic height", ledger,
				"assignmentHeight(section)");
		assertContains("artificer sections participate in dynamic height model", ledger,
				"AssignmentSection.THE_ASSUMED_LIMB");
		assertContains("total content height uses dynamic section height", ledger,
				"total += assignmentHeight(section)");
		assertContains("collapsed row draws only title/progress row", ledger,
				"Component.translatable(titleKey)");
	}

	private static void ledgerOrdersAssignmentsByDegree() throws IOException {
		String ledger = readLedger();

		assertContains("assignment sections carry degree metadata", ledger, "assignmentDegree");
		assertContains("ledger builds a degree-ordered section list", ledger, "ORDERED_ASSIGNMENT_SECTIONS");
		assertContains("ledger sorts sections by assignment degree first", ledger,
				"Comparator.comparingInt((AssignmentSection section) -> section.assignmentDegree)");
		assertContains("ledger renders sections from degree ordering", ledger,
				"for (AssignmentSection section : ORDERED_ASSIGNMENT_SECTIONS)");
		assertContains("D2 Artificer assignment sits with D2 entries", ledger,
				"THE_WORN_VOW(2, \"screen.hemomancy.harbinger_assignment_ledger.assignment_label.main\"");
		assertContains("D3 Artificer assignment sits with D3 entries", ledger,
				"THE_THREE_ANSWERS(3, \"screen.hemomancy.harbinger_assignment_ledger.assignment_label.main\"");
		assertContains("D7 Artificer assignment sits with D7 entries", ledger,
				"WEIGHT_OF_THE_FRAME(7, \"screen.hemomancy.harbinger_assignment_ledger.assignment_label.main\"");
	}

	private static void ledgerSeparatesAssignmentLabelsFromTitles() throws IOException {
		String ledger = readLedger();
		String language = Files.readString(Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))
				.replace("\r\n", "\n");

		assertContains("assignment sections define a label type", ledger, "assignmentLabelKey");
		assertContains("expanded rows render the assignment label above the title", ledger,
				"renderAssignmentLabel(gfx, section, x, y");
		assertContains("collapsed rows render the assignment label above the title", ledger,
				"renderAssignmentLabel(gfx, section, cardX + 22, y + 4");
		assertContains("main assignment label has language", language,
				"screen.hemomancy.harbinger_assignment_ledger.assignment_label.main");
		assertContains("side assignment label has language", language,
				"screen.hemomancy.harbinger_assignment_ledger.assignment_label.side");
	}

	private static void ledgerTogglesAssignmentsFromClickHitboxes() throws IOException {
		String ledger = readLedger();

		assertContains("mouse click tries assignment collapse toggle", ledger, "toggleAssignmentAt(mx, my)");
		assertContains("toggle lookup checks assignment panel", ledger, "isOverAssignmentPanel(mx, my)");
		assertContains("hitbox stores assignment section", ledger, "record AssignmentHitbox");
		assertContains("hitbox exposes contains", ledger, "boolean contains(double mx, double my)");
		assertContains("toggle removes expanded assignment from collapsed set", ledger,
				"collapsedAssignments.remove(hitbox.section())");
		assertContains("toggle adds collapsed assignment to collapsed set", ledger,
				"collapsedAssignments.add(hitbox.section())");
	}

	private static void ledgerProvidesGlobalCollapseControls() throws IOException {
		String ledger = readLedger();
		String language = Files.readString(Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))
				.replace("\r\n", "\n");

		assertContains("ledger defines collapse all button width", ledger, "COLLAPSE_ALL_BUTTON_WIDTH");
		assertContains("ledger defines expand all button width", ledger, "EXPAND_ALL_BUTTON_WIDTH");
		assertContains("ledger renders global collapse controls", ledger, "renderCollapseControls(gfx, mouseX, mouseY)");
		assertContains("mouse click handles collapse all button", ledger, "isOverCollapseAllButton(mx, my)");
		assertContains("mouse click handles expand all button", ledger, "isOverExpandAllButton(mx, my)");
		assertContains("collapse all fills every assignment section", ledger,
				"collapsedAssignments.addAll(EnumSet.allOf(AssignmentSection.class))");
		assertContains("expand all clears collapsed assignments", ledger, "collapsedAssignments.clear()");
		assertContains("global controls clamp assignment scroll", ledger,
				"assignmentScrollOffset = clampAssignmentScroll(assignmentScrollOffset)");
		assertContains("collapse all language exists", language,
				"screen.hemomancy.harbinger_assignment_ledger.collapse_all");
		assertContains("expand all language exists", language,
				"screen.hemomancy.harbinger_assignment_ledger.expand_all");
	}

	private static String readLedger() throws IOException {
		Path path = SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java");
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " missing: " + expected);
		}
	}
}
