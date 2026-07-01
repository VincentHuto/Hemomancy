package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialsAtlasMetadataSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private MaterialsAtlasMetadataSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String data = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java");
		String spec = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java");
		String atlasEntry = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasEntry.java");
		String trace = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasTraceLayerCache.java");
		String view = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsTabView.java");
		String controller = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsTabController.java");
		String docs = read("docs/HEMOMANCY_REFERENCE.md");

		assertContains("atlas spec exposes Harbinger path", spec, "MaterialAtlasPath.HARBINGER");
		assertContains("atlas spec exposes Unstained path", spec, "MaterialAtlasPath.UNSTAINED");
		for (String bucket : new String[] {
				"bloodcraft_core", "vascular_craft", "alchemy_enzymes", "fungal_ecology",
				"morphlings", "scars_patterns", "living_implements", "architecture",
				"still_waters_core", "cleansing_facilities", "lethean_flora",
				"pale_architecture", "anti_blood_warding", "vestments_instruments"
		}) {
			assertContains("atlas spec declares bucket " + bucket, spec, "\"" + bucket + "\"");
		}
		assertContains("atlas entries store optional authored node x", atlasEntry, "Integer nodeX");
		assertContains("atlas entries store optional authored node y", atlasEntry, "Integer nodeY");
		assertContains("atlas entries expose explicit position check", atlasEntry, "hasExplicitPosition()");
		assertContains("atlas spec keeps auto-layout entry helper", spec, "private static void entry(String materialId");
		assertContains("atlas spec exposes authored coordinate entry helper", spec, "private static void entryAt(String materialId");

		Set<String> atlasIds = extractAtlasIds(spec);
		assertAllCovered("Harbinger material atlas metadata", materialIds(data, "buildBloodEntries"), atlasIds);
		assertAllCovered("Unstained material atlas metadata", materialIds(data, "buildUnstainedEntries"), atlasIds);
		assertContains("material buckets declare representative root materials",
				spec, "rootMaterialId");

		assertContains("materials controller filters through atlas visibility",
				controller, "MaterialVisibility.NEXT_PREVIEW");
		assertContains("materials controller keeps selected details unlocked-only",
				controller, "selectedEntry.visibility() == MaterialVisibility.UNLOCKED");
		assertContains("materials view renders a central atlas hub",
				view, "drawAtlasHub");
		assertContains("materials view renders square category plaques",
				view, "drawBucketPlaques");
		assertContains("materials view positions representative root material at bucket origin",
				view, "isBucketRoot(node)");
		assertContains("materials view fans non-root nodes away from the representative root",
				view, "NODE_GAP_Y + row * NODE_GAP_Y");
		assertContains("materials view prefers authored atlas node coordinates",
				view, "hasExplicitPosition()");
		assertNotContains("materials view does not draw node-like label chips",
				view, "PLAQUE_CHIP");
		assertNotContains("materials view does not draw filled label backplates through nodes",
				view, "labelW");
		assertContains("materials view renders veiled preview nodes",
				view, "drawVeiledNode");
		assertContains("materials view uses cached atlas traces",
				view, "MaterialAtlasTraceLayerCache");
		assertNotContains("materials view no longer owns rectangular cluster columns",
				view, "CLUSTER_ITEM_COLS");
		assertNotContains("materials view no longer packs rectangular cluster grid columns",
				view, "GRID_COLS");

		assertContains("atlas trace cache bakes to a dynamic texture", trace, "DynamicTexture");
		assertContains("atlas trace cache bakes pixels through NativeImage", trace, "NativeImage");
		assertContains("atlas trace cache uses organic cubic traces", trace, "sampleCubic");
		assertContains("atlas trace cache colors traces by bucket", trace, "bucket.color()");
		assertContains("atlas trace cache connects branches to representative root materials",
				trace, "rootPositionFor");

		assertContains("reference docs mention veiled next-tier materials",
				docs, "next-tier veiled material nodes");
	}

	private static Set<String> materialIds(String data, String methodName) {
		int start = data.indexOf("private static List<MaterialEntry> " + methodName + "()");
		if (start < 0) {
			throw new AssertionError("missing method " + methodName);
		}
		int end = data.indexOf("private static List<MaterialEntry>", start + 1);
		if (end < 0) {
			end = data.length();
		}
		String body = data.substring(start, end).replaceAll("(?m)^\\s*//.*$", "");
		Set<String> ids = new LinkedHashSet<>();
		Matcher matcher = Pattern.compile("new MaterialEntry\\(\\s*\"([^\"]+)\"").matcher(body);
		while (matcher.find()) {
			ids.add(matcher.group(1));
		}
		return ids;
	}

	private static Set<String> extractAtlasIds(String spec) {
		Set<String> ids = new LinkedHashSet<>();
		Matcher matcher = Pattern.compile("entry(?:At)?\\(\\s*\"([^\"]+)\"").matcher(spec);
		while (matcher.find()) {
			ids.add(matcher.group(1));
		}
		return ids;
	}

	private static void assertAllCovered(String label, Set<String> materialIds, Set<String> atlasIds) {
		Set<String> missing = new LinkedHashSet<>(materialIds);
		missing.removeAll(atlasIds);
		if (!missing.isEmpty()) {
			throw new AssertionError(label + " missing atlas ids: " + missing);
		}
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
