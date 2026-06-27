package com.vincenthuto.hemomancy.client.render.world;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;

public final class MnemonicLowtideChamberThemeSourceTest {
	private static final Path MANAGER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java");
	private static final Path REGISTRY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/ChamberSkyThemeRegistry.java");
	private static final Path SHADER_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
	private static final Path HEMO_RENDER_TYPES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
	private static final Path HELPERS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/ChamberOfWillRenderHelpers.java");
	private static final Path EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java");
	private static final Path LOWTIDE_FLUID_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_fluid.json");
	private static final Path LOWTIDE_FLUID_SHADER_VERTEX = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_fluid.vsh");
	private static final Path LOWTIDE_FLUID_SHADER_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_fluid.fsh");
	private static final Path COMMAND_TEST = Path.of(
			"src/test/java/com/vincenthuto/hemomancy/common/command/ChamberThemeCommandSourceTest.java");
	private static final Path LANG = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");
	private static final Path LORE = Path.of("docs/LORE_REFERENCE.md");
	private static final Path LOWTIDE_SKY = Path.of(
			"src/main/resources/assets/hemomancy/textures/environment/mnemonic_lowtide_sky.png");
	private static final Path LOWTIDE_CLOUDS = Path.of(
			"src/main/resources/assets/hemomancy/textures/environment/mnemonic_lowtide_clouds.png");
	private static final Path LOWTIDE_FLUID = Path.of(
			"src/main/resources/assets/hemomancy/textures/environment/mnemonic_lowtide_fluid.png");

	private MnemonicLowtideChamberThemeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String manager = read(MANAGER);
		String registry = read(REGISTRY);
		String lowtideRegistry = sliceMethod(registry, "ChamberSkyTheme mnemonicLowtide",
				"register(mnemonicLowtide");
		String shaderInit = read(SHADER_INIT);
		String hemoRenderTypes = read(HEMO_RENDER_TYPES);
		String helpers = read(HELPERS);
		String effects = read(EFFECTS);
		String lowtideBaseSkybox = sliceMethod(effects, "static void renderMnemonicLowtideBaseSkybox",
				"static void renderMnemonicLowtideDepthEffects");
		String lowtideSkullVault = sliceMethod(effects, "static void renderMnemonicLowtideSkullVault",
				"static void renderMnemonicLowtideBlackFluid");
		String lowtideFluidSurface = sliceMethod(effects, "static void renderMnemonicLowtideFluidSurface",
				"static void renderMnemonicLowtideMemoryFragments");
		String lowtideArteryBridges = sliceMethod(effects, "static void renderMnemonicLowtideArteryBridges",
				"static void renderMnemonicLowtideMemoryPearls");
		String lowtideBridgeRib = sliceMethod(effects, "static void addLowtideBridgeRib",
				"static Vec3 lowtideBridgeSide");
		String commandTest = read(COMMAND_TEST);
		String lang = read(LANG);
		String reference = read(REFERENCE);
		String lore = read(LORE);

		assertContains("manager declares mnemonic lowtide theme id", manager,
				"THEME_MNEMONIC_LOWTIDE = Hemomancy.rloc(\"mnemonic_lowtide\")");
		assertBefore("lowtide cycles after default and before archon", manager,
				"THEME_WILL_DEFAULT,\n            THEME_MNEMONIC_LOWTIDE,\n            THEME_ARCHON_REVELATION");
		assertBefore("degree seven archon revelation outranks lowtide", manager,
				"if (degree >= 7) {\n            return new ChamberState(1, THEME_ARCHON_REVELATION);\n        }",
				"if (degree >= 6 && HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player))");
		assertContains("lowtide trigger uses committed Vein-Mason scar loadout milestone", manager,
				"HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player)");
		assertContains("lowtide entry message is one-time persistent player state", manager,
				"KEY_MNEMONIC_LOWTIDE_ENTRY_SEEN");
		assertContains("lowtide entry message uses localization", manager,
				"Component.translatable(\"message.hemomancy.chamber.mnemonic_lowtide_entry\")");

		assertContains("registry exposes lowtide sky texture", registry, "MNEMONIC_LOWTIDE_SKY");
		assertContains("registry exposes lowtide cloud texture", registry, "MNEMONIC_LOWTIDE_CLOUDS");
		assertContains("registry exposes lowtide fluid texture", registry, "MNEMONIC_LOWTIDE_FLUID");
		assertContains("registry registers lowtide effects", registry,
				"new MnemonicLowtideChamberEffects(mnemonicLowtide)");
		assertContains("registry binds generated lowtide textures", registry,
				".textures(MNEMONIC_LOWTIDE_SKY, MNEMONIC_LOWTIDE_CLOUDS, MNEMONIC_LOWTIDE_CLOUDS, DEFAULT_NOISE)");
		assertContains("registry tints lowtide skybox with dark cranial maroon", lowtideRegistry,
				".skybox(0xFF5A1810, 0xFF380E08)");
		assertContains("registry gives lowtide deep-crimson nebula accent (no gold)", lowtideRegistry,
				".nebula(0x4A1710, 0x9E2F1D, 0x5C1208)");
		assertContains("registry gives lowtide separated ivory red umber and gold shared tints", lowtideRegistry,
				".tints(0xE7D4B0, 0xD15B3E, 0x9C7C66, 0xF2C86D)");
		assertContains("registry keeps lowtide slow and sparse", registry,
				".motion(0.54F)");
		assertContains("registry suppresses blue-vein and neural shared layers for lowtide", registry,
				".layers(1, 0, 1, 0)");

		assertContains("effects class exists", effects, "final class MnemonicLowtideChamberEffects");
		assertContains("lowtide effects own the bottom-fluid base skybox", effects,
				"renderMnemonicLowtideBaseSkybox(");
		assertContains("lowtide base skybox renders all six faces", lowtideBaseSkybox,
				"for (int face = 0; face < 6; face++)");
		assertContains("lowtide effects bind the fluid texture to the bottom face", lowtideBaseSkybox,
				"face == 0 ? ChamberSkyThemeRegistry.MNEMONIC_LOWTIDE_FLUID : theme.skyTexture()");
		assertContains("lowtide skull vault stays off the bottom-fluid face", lowtideSkullVault,
				"for (int face = 1; face < 6; face++)");
		assertContains("lowtide effects own skull vault rendering", effects, "renderMnemonicLowtideSkullVault(");
		assertContains("lowtide effects own black fluid rendering", effects, "renderMnemonicLowtideBlackFluid(");
		assertContains("lowtide effects render fluid through the Lowtide fluid shader", effects,
				"HemoRenderTypes.mnemonicLowtideFluid(");
		assertContains("lowtide fluid shader samples the generated fluid texture", effects,
				"ChamberSkyThemeRegistry.MNEMONIC_LOWTIDE_FLUID");
		assertContains("lowtide fluid surface is subdivided for vertex ripple animation", lowtideFluidSurface,
				"int grid = 36");
		assertContains("lowtide fluid surface emits per-cell quads", lowtideFluidSurface,
				"for (int cellZ = 0; cellZ < grid; cellZ++)");
		assertContains("lowtide effects own memory fragments", effects, "renderMnemonicLowtideMemoryFragments(");
		assertContains("lowtide effects own nerve roots", effects, "renderMnemonicLowtideNerveRoots(");
		assertContains("lowtide effects own outpost silhouettes", effects, "renderMnemonicLowtideOutposts(");
		assertContains("lowtide effects own artery bridges", effects, "renderMnemonicLowtideArteryBridges(");
		assertContains("lowtide artery bridges use physical arch segment geometry", effects,
				"renderMnemonicLowtideBridgeArchSegment(");
		assertContains("lowtide artery bridges draw solid deck quads", effects, "addLowtideBridgeDeckQuad(");
		assertContains("lowtide artery bridges draw raised ribs", effects, "addLowtideBridgeRib(");
		assertContains("lowtide artery bridges have actual width and underside depth", effects,
				"float bridgeHalfWidth = skyDistance *");
		assertContains("lowtide artery bridges have actual underside thickness", effects,
				"float bridgeDepth = skyDistance *");
		assertContains("lowtide artery bridges are placed in the sky band above the fluid horizon", lowtideArteryBridges,
				"float baseY = skyDistance *");
		assertNotContains("lowtide artery bridge ribs should not drop supports into the lake", lowtideBridgeRib,
				"-skyDistance * 0.58F");
		assertContains("lowtide effects own memory pearls", effects, "renderMnemonicLowtideMemoryPearls(");
		assertContains("lowtide effects own synapse flashes", effects, "renderMnemonicLowtideSynapseFlashes(");
		assertContains("lowtide effects own red vein strands", effects, "renderMnemonicLowtideRedVeinStrands(");
		assertContains("lowtide effects own horizon haze band", effects,
				"renderMnemonicLowtideHorizonHaze(");
		assertContains("lowtide fog override uses dark cranial maroon", effects,
				"new Vec3(0.22D, 0.09D, 0.07D)");
		assertContains("lowtide sparse synapse pass skips empty buffers", effects,
				"\t\t\tif (emitted) {\n\t\t\t\tBufferUploader.drawWithShader(buffer.buildOrThrow());");
		assertContains("lowtide sparse strand pass tracks emitted geometry", effects,
				"emitted |= addLowtide3DStroke(buffer, matrix");
		assertNotContains("lowtide effects should not delegate themed render bodies to helpers", effects,
				"ChamberOfWillRenderHelpers.renderMnemonicLowtide");
		assertNotContains("shared helpers should not contain lowtide render bodies", helpers,
				"renderMnemonicLowtide");

		assertContains("command guard knows lowtide theme", commandTest, "mnemonic_lowtide");
		assertContains("lang has lowtide entry line", lang,
				"\"message.hemomancy.chamber.mnemonic_lowtide_entry\": \"The tide has gone out. What remains was never meant to be seen.\"");
		assertContains("reference documents lowtide progression", reference, "`hemomancy:mnemonic_lowtide`");
		assertContains("lore documents lowtide emotional register", lore, "Mnemonic Lowtide");
		assertContains("shader init registers lowtide fluid shader", shaderInit, "MNEMONIC_LOWTIDE_FLUID");
		assertContains("shader init uses lowtide fluid shader location", shaderInit,
				"Hemomancy.rloc(\"world/mnemonic_lowtide_fluid\")");
		assertContains("render type exposes lowtide fluid shader pass", hemoRenderTypes,
				"mnemonicLowtideFluid(ResourceLocation texture, float gameTime");
		assertContains("render type sets lowtide ripple time", hemoRenderTypes,
				"setUniform(shader, \"HemoTime\", gameTime)");
		assertContains("render type resets lowtide fluid uniforms", hemoRenderTypes,
				"mnemonic_lowtide_fluid_uniforms");

		assertTexture("lowtide sky", LOWTIDE_SKY);
		assertTexture("lowtide clouds", LOWTIDE_CLOUDS);
		assertTexture("lowtide fluid", LOWTIDE_FLUID);
		assertLowtidePalette("lowtide sky palette", LOWTIDE_SKY);
		assertLowtidePalette("lowtide cloud palette", LOWTIDE_CLOUDS);
		assertLowtideFluidPalette("lowtide fluid palette", LOWTIDE_FLUID);
		assertLowtideTextureContrast("lowtide sky contrast", LOWTIDE_SKY);
		assertLowtideTextureContrast("lowtide cloud contrast", LOWTIDE_CLOUDS);
		assertNoLargeBakedPaperBlobs("lowtide sky baked paper scale", LOWTIDE_SKY);
		assertNoLargeBakedPaperBlobs("lowtide cloud baked paper scale", LOWTIDE_CLOUDS);
		assertQuietSkyboxCorners("lowtide sky quiet corners", LOWTIDE_SKY);
		assertQuietSkyboxCorners("lowtide cloud quiet corners", LOWTIDE_CLOUDS);
		assertSoftEdges("lowtide sky edges", LOWTIDE_SKY);
		assertSoftEdges("lowtide cloud edges", LOWTIDE_CLOUDS);
		assertSoftEdges("lowtide fluid edges", LOWTIDE_FLUID);
		assertShaderFile("lowtide fluid shader json", LOWTIDE_FLUID_SHADER_JSON,
				"\"vertex\": \"hemomancy:world/mnemonic_lowtide_fluid\"",
				"\"fragment\": \"hemomancy:world/mnemonic_lowtide_fluid\"",
				"\"HemoTime\"", "\"RippleStrength\"", "\"ReflectionStrength\"");
		assertShaderFile("lowtide fluid shader vertex", LOWTIDE_FLUID_SHADER_VERTEX,
				"uniform float HemoTime;", "uniform float RippleStrength;", "float waveHeight",
				"vec3 animatedPosition");
		assertShaderFile("lowtide fluid shader fragment", LOWTIDE_FLUID_SHADER_FRAGMENT,
				"uniform float HemoTime;", "float rippleNormal", "texture(Sampler0, fract(rippleUv))",
				"ReflectionStrength");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static String sliceMethod(String source, String startMarker, String nextMarker) {
		int start = source.indexOf(startMarker);
		if (start < 0) {
			throw new AssertionError("missing method marker " + startMarker);
		}
		int end = source.indexOf(nextMarker, start + startMarker.length());
		if (end < 0) {
			throw new AssertionError("missing next method marker " + nextMarker);
		}
		return source.substring(start, end);
	}

	private static void assertTexture(String label, Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
		BufferedImage image = ImageIO.read(path.toFile());
		if (image == null) {
			throw new AssertionError(label + ": unreadable image " + path);
		}
		if (image.getWidth() != 512 || image.getHeight() != 512) {
			throw new AssertionError(label + ": expected 512x512, got " + image.getWidth() + "x" + image.getHeight());
		}
	}

	private static void assertShaderFile(String label, Path path, String... expected) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
		String shader = read(path);
		for (String marker : expected) {
			assertContains(label, shader, marker);
		}
	}

	private static void assertLowtidePalette(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		long darkRed = 0;
		long umber = 0;
		long ivoryGold = 0;
		long sampled = 0;
		for (int y = 0; y < image.getHeight(); y += 4) {
			for (int x = 0; x < image.getWidth(); x += 4) {
				int rgb = image.getRGB(x, y);
				int r = (rgb >> 16) & 255;
				int g = (rgb >> 8) & 255;
				int b = rgb & 255;
				sampled++;
				if (r > g + 14 && g >= b && r < 128) {
					darkRed++;
				}
				if (r > 38 && r < 150 && g > 24 && g < 112 && b < 82) {
					umber++;
				}
				if (r > 128 && g > 100 && b > 72 && r >= b + 20) {
					ivoryGold++;
				}
			}
		}
		if (darkRed < sampled / 18 || umber < sampled / 22 || ivoryGold < sampled / 80) {
			throw new AssertionError(label + ": palette lacks lowtide maroon/umber/ivory-gold balance");
		}
	}

	private static void assertLowtideFluidPalette(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		long darkFluid = 0;
		long mutedRipple = 0;
		long sampled = 0;
		for (int y = 0; y < image.getHeight(); y += 4) {
			for (int x = 0; x < image.getWidth(); x += 4) {
				int rgb = image.getRGB(x, y);
				int r = (rgb >> 16) & 255;
				int g = (rgb >> 8) & 255;
				int b = rgb & 255;
				int brightness = r + g + b;
				sampled++;
				if (brightness < 82 && r >= g && g >= b) {
					darkFluid++;
				}
				if (r > 42 && r < 132 && g > 22 && g < 86 && b < 64) {
					mutedRipple++;
				}
			}
		}
		if (darkFluid < sampled / 2 || mutedRipple < sampled / 18) {
			throw new AssertionError(label + ": bottom face should read as dark black-red fluid");
		}
	}

	private static void assertLowtideTextureContrast(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		double[] samples = new double[(424 - 88) * (424 - 88)];
		int index = 0;
		for (int y = 88; y < 424; y++) {
			for (int x = 88; x < 424; x++) {
				int rgb = image.getRGB(x, y);
				int r = (rgb >> 16) & 255;
				int g = (rgb >> 8) & 255;
				int b = rgb & 255;
				samples[index++] = r * 0.2126D + g * 0.7152D + b * 0.0722D;
			}
		}
		java.util.Arrays.sort(samples);
		double p5 = samples[(int) (samples.length * 0.05D)];
		double p95 = samples[(int) (samples.length * 0.95D)];
		double total = 0.0D;
		for (double sample : samples) {
			total += sample;
		}
		double mean = total / samples.length;
		double variance = 0.0D;
		for (double sample : samples) {
			double delta = sample - mean;
			variance += delta * delta;
		}
		double deviation = Math.sqrt(variance / samples.length);
		if (p95 < 72.0D || p95 - p5 < 36.0D || deviation < 13.0D) {
			throw new AssertionError(label + ": central lowtide texture is too tonally flat, p5=" + p5
					+ " p95=" + p95 + " deviation=" + deviation);
		}
	}

	private static void assertNoLargeBakedPaperBlobs(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		boolean[][] mask = new boolean[image.getWidth()][image.getHeight()];
		boolean[][] visited = new boolean[image.getWidth()][image.getHeight()];
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y);
				int r = (rgb >> 16) & 255;
				int g = (rgb >> 8) & 255;
				int b = rgb & 255;
				mask[x][y] = r > 92 && g > 62 && b > 42 && r >= g + 10 && g >= b && r + g + b > 210;
			}
		}

		int largest = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (!mask[x][y] || visited[x][y]) {
					continue;
				}
				largest = Math.max(largest, connectedComponentSize(mask, visited, x, y));
			}
		}
		if (largest > 720) {
			throw new AssertionError(label + ": large baked tan paper blob found, size=" + largest);
		}
	}

	private static void assertQuietSkyboxCorners(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		int margin = 48;
		int edgeGradientPixels = 0;
		int cornerGradientPixels = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int edgeDistance = Math.min(Math.min(x, y),
						Math.min(image.getWidth() - 1 - x, image.getHeight() - 1 - y));
				boolean inEdge = edgeDistance < margin;
				boolean inCorner = (x < margin || x >= image.getWidth() - margin)
						&& (y < margin || y >= image.getHeight() - margin);
				if (!inEdge && !inCorner) {
					continue;
				}

				int gradient = 0;
				if (x + 1 < image.getWidth()) {
					gradient = Math.max(gradient, colorDifference(image.getRGB(x, y), image.getRGB(x + 1, y)));
				}
				if (y + 1 < image.getHeight()) {
					gradient = Math.max(gradient, colorDifference(image.getRGB(x, y), image.getRGB(x, y + 1)));
				}
				if (gradient <= 22) {
					continue;
				}
				if (inEdge) {
					edgeGradientPixels++;
				}
				if (inCorner) {
					cornerGradientPixels++;
				}
			}
		}
		if (edgeGradientPixels > 650 || cornerGradientPixels > 50) {
			throw new AssertionError(label + ": clipped high-detail pixels too close to skybox edges, edge="
					+ edgeGradientPixels + " corner=" + cornerGradientPixels);
		}
	}

	private static int connectedComponentSize(boolean[][] mask, boolean[][] visited, int startX, int startY) {
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(new int[]{startX, startY});
		visited[startX][startY] = true;
		int size = 0;
		while (!queue.isEmpty()) {
			int[] point = queue.removeFirst();
			size++;
			addIfMasked(mask, visited, queue, point[0] + 1, point[1]);
			addIfMasked(mask, visited, queue, point[0] - 1, point[1]);
			addIfMasked(mask, visited, queue, point[0], point[1] + 1);
			addIfMasked(mask, visited, queue, point[0], point[1] - 1);
		}
		return size;
	}

	private static void addIfMasked(boolean[][] mask, boolean[][] visited, ArrayDeque<int[]> queue, int x, int y) {
		if (x < 0 || y < 0 || x >= mask.length || y >= mask[0].length || visited[x][y] || !mask[x][y]) {
			return;
		}
		visited[x][y] = true;
		queue.add(new int[]{x, y});
	}

	private static void assertSoftEdges(String label, Path path) throws IOException {
		BufferedImage image = ImageIO.read(path.toFile());
		double horizontalDifference = edgeDifference(image, true);
		double verticalDifference = edgeDifference(image, false);
		if (horizontalDifference > 54.0D || verticalDifference > 54.0D) {
			throw new AssertionError(label + ": skybox edges are too harsh, h=" + horizontalDifference
					+ " v=" + verticalDifference);
		}
	}

	private static double edgeDifference(BufferedImage image, boolean horizontal) {
		long total = 0;
		int samples = 0;
		if (horizontal) {
			for (int y = 0; y < image.getHeight(); y += 4) {
				total += colorDifference(image.getRGB(0, y), image.getRGB(image.getWidth() - 1, y));
				samples++;
			}
		} else {
			for (int x = 0; x < image.getWidth(); x += 4) {
				total += colorDifference(image.getRGB(x, 0), image.getRGB(x, image.getHeight() - 1));
				samples++;
			}
		}
		return total / (double) Math.max(1, samples);
	}

	private static int colorDifference(int a, int b) {
		int ar = (a >> 16) & 255;
		int ag = (a >> 8) & 255;
		int ab = a & 255;
		int br = (b >> 16) & 255;
		int bg = (b >> 8) & 255;
		int bb = b & 255;
		return Math.abs(ar - br) + Math.abs(ag - bg) + Math.abs(ab - bb);
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}

	private static void assertBefore(String label, String text, String before, String after) {
		int beforeIndex = text.indexOf(before);
		int afterIndex = text.indexOf(after);
		if (beforeIndex < 0) {
			throw new AssertionError(label + ": missing first marker " + before);
		}
		if (afterIndex < 0) {
			throw new AssertionError(label + ": missing second marker " + after);
		}
		if (beforeIndex >= afterIndex) {
			throw new AssertionError(label + ": wrong order");
		}
	}

	private static void assertBefore(String label, String text, String expected) {
		assertContains(label, text, expected);
	}
}
