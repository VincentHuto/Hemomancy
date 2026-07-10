package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ManipulationTreeCoverageSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Pattern MANIP_REGISTER =
			Pattern.compile("MANIPS\\.register\\(\"([^\"]+)\"");
	private static final Pattern TREE_REGISTER =
			Pattern.compile("\\bregister\\(\"([^\"]+)\"");
	private static final Pattern TREE_REGISTER_LINE =
			Pattern.compile("\\bregister\\(\"([^\"]+)\",\\s*([^,]+),\\s*([^,\\)]+)"
					+ "(?:,\\s*((?:\"[^\"]+\"\\s*,?\\s*)*))?\\)(.*?);", Pattern.DOTALL);
	private static final Pattern LAYOUT_TENDENCY_CALL =
			Pattern.compile("\\.setLayoutTendency\\(([^)]+)\\)");
	private static final Pattern SOFT_PARENTS_CALL =
			Pattern.compile("\\.setSoftParents\\((.*?)\\)");
	private static final Pattern STAFF_FORM_TENDENCY =
			Pattern.compile("new StaffWeaponFormManip\\(\"([^\"]+)\".*?EnumBloodTendency\\.([A-Z]+)",
					Pattern.DOTALL);
	private static final Pattern QUOTED_TEXT = Pattern.compile("\"([^\"]+)\"");
	private static final Map<String, String> LIVING_WEAPON_TENDENCIES = Map.of(
			"conjure_blade", "ANIMUS",
			"conjure_axe", "MORTEM",
			"conjure_spear", "LUX",
			"conjure_claws", "TENEBRIS",
			"conjure_crossbow", "DUCTILIS",
			"conjure_torch", "FLAMMEUS",
			"conjure_flail", "CONGEATIO"
	);
	private static final Map<String, List<String>> LIVING_WEAPON_TREE_PARENTS = Map.of(
			"conjure_blade", List.of("vital_effusion"),
			"conjure_axe", List.of("exsanguinate"),
			"conjure_spear", List.of("crimson_sight"),
			"conjure_claws", List.of("void_shroud"),
			"conjure_crossbow", List.of("hemolymphal_pulse"),
			"conjure_torch", List.of("crimson_flame_conjuration"),
			"conjure_flail", List.of("glacial_bastion", "glacial_rampart")
	);
	private static final List<String> LIVING_WEAPON_FORMS = List.of(
			"conjure_blade",
			"conjure_axe",
			"conjure_spear",
			"conjure_claws",
			"conjure_crossbow",
			"conjure_torch",
			"conjure_flail"
	);
	private static final Set<String> NON_TREE_MANIPULATIONS = Set.of(
			"hemosynthesis", "blood_lamp", "crimson_harvest", "sanguine_excavation", "vital_reservoir");

	private ManipulationTreeCoverageSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String manipInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		String treeInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java");

		Set<String> registeredManips = extract(MANIP_REGISTER, manipInit);
		Set<String> treeNodes = extract(TREE_REGISTER, treeInit);

		List<String> missingNodes = new ArrayList<>();
		for (String manip : registeredManips) {
			if (!treeNodes.contains(manip) && !NON_TREE_MANIPULATIONS.contains(manip)) {
				missingNodes.add(manip);
			}
		}

		if (!missingNodes.isEmpty()) {
			throw new AssertionError("Manipulation tree is missing nodes for " + missingNodes);
		}

		List<String> duplicateNodes = duplicates(TREE_REGISTER, treeInit);
		if (!duplicateNodes.isEmpty()) {
			throw new AssertionError("Manipulation tree has duplicate nodes for " + duplicateNodes);
		}

		Map<String, TreeNode> nodes = treeNodesByName(treeInit);
		assertLivingWeaponFormsBranchFromStaff(nodes);
		assertLivingWeaponFormsHaveDistinctTendencies(manipInit);
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static Set<String> extract(Pattern pattern, String text) {
		Matcher matcher = pattern.matcher(text);
		Set<String> values = new LinkedHashSet<>();
		while (matcher.find()) {
			values.add(matcher.group(1));
		}
		return values;
	}

	private static List<String> duplicates(Pattern pattern, String text) {
		Matcher matcher = pattern.matcher(text);
		Set<String> seen = new LinkedHashSet<>();
		List<String> duplicates = new ArrayList<>();
		while (matcher.find()) {
			String value = matcher.group(1);
			if (!seen.add(value)) {
				duplicates.add(value);
			}
		}
		return duplicates;
	}

	private static Map<String, TreeNode> treeNodesByName(String text) {
		Map<String, TreeNode> nodes = new java.util.LinkedHashMap<>();
		Matcher matcher = TREE_REGISTER_LINE.matcher(text);
		while (matcher.find()) {
			String name = matcher.group(1);
			String y = matcher.group(3).trim();
			String parentsText = matcher.group(4);
			String chainText = matcher.group(5);
			String layoutTendency = captureFirst(LAYOUT_TENDENCY_CALL, chainText);
			List<String> parents = quotedValues(parentsText);
			List<String> softParents = quotedValues(captureFirst(SOFT_PARENTS_CALL, chainText));
			nodes.put(name, new TreeNode(y, parents, softParents, layoutTendency));
		}
		return nodes;
	}

	private static String captureFirst(Pattern pattern, String text) {
		if (text == null) {
			return null;
		}
		Matcher matcher = pattern.matcher(text);
		return matcher.find() ? matcher.group(1) : null;
	}

	private static List<String> quotedValues(String text) {
		List<String> values = new ArrayList<>();
		if (text == null) {
			return values;
		}
		Matcher quoted = QUOTED_TEXT.matcher(text);
		while (quoted.find()) {
			values.add(quoted.group(1));
		}
		return values;
	}

	private static void assertLivingWeaponFormsBranchFromStaff(Map<String, TreeNode> nodes) {
		TreeNode staff = require(nodes, "conjure_staff");
		for (String form : LIVING_WEAPON_FORMS) {
			if (staff.parents().contains(form)) {
				throw new AssertionError("conjure_staff should not depend on living weapon form " + form);
			}
		}

		for (String form : LIVING_WEAPON_FORMS) {
			TreeNode node = require(nodes, form);
			List<String> expectedTreeParents = LIVING_WEAPON_TREE_PARENTS.get(form);
			if (!node.parents().equals(expectedTreeParents)) {
				throw new AssertionError(form + " should branch in its own tendency tree from "
						+ expectedTreeParents + ", found " + node.parents());
			}
			if (node.parents().contains("conjure_staff")) {
				throw new AssertionError(form + " should not use conjure_staff as a hard tree parent");
			}
			if (!node.softParents().equals(List.of("conjure_staff"))) {
				throw new AssertionError(form + " should use conjure_staff as a soft parent, found "
						+ node.softParents());
			}
			if (node.layoutTendency() != null) {
				throw new AssertionError(form + " should use its gameplay tendency for tree placement");
			}
		}
	}

	private static void assertLivingWeaponFormsHaveDistinctTendencies(String manipInit) {
		Map<String, String> actual = new java.util.LinkedHashMap<>();
		Matcher matcher = STAFF_FORM_TENDENCY.matcher(manipInit);
		while (matcher.find()) {
			actual.put(matcher.group(1), matcher.group(2));
		}
		for (Map.Entry<String, String> expected : LIVING_WEAPON_TENDENCIES.entrySet()) {
			String actualTendency = actual.get(expected.getKey());
			if (!expected.getValue().equals(actualTendency)) {
				throw new AssertionError(expected.getKey() + " should be " + expected.getValue()
						+ ", found " + actualTendency);
			}
		}
		if (new LinkedHashSet<>(actual.values()).size() != LIVING_WEAPON_TENDENCIES.size()) {
			throw new AssertionError("Living weapon forms should each use a different tendency, found " + actual);
		}
	}

	private static TreeNode require(Map<String, TreeNode> nodes, String name) {
		TreeNode node = nodes.get(name);
		if (node == null) {
			throw new AssertionError("Missing tree node " + name);
		}
		return node;
	}

	private record TreeNode(String y, List<String> parents, List<String> softParents, String layoutTendency) {
	}
}
