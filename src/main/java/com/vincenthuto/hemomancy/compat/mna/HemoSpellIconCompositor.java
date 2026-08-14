package com.vincenthuto.hemomancy.compat.mna;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Builds and animates bordered MnA spell icons. */
public class HemoSpellIconCompositor {

	private static final ResourceLocation BLOODY_BORDER = Hemomancy.rloc("textures/mna/bloody_border.png");
	private static final int FRAME_COUNT = 32;
	/** How many ticks between frame advances. Higher = slower animation. */
	private static final int TICKS_PER_FRAME = 3;

	private record PendingIcon(ResourceLocation baseIcon, ResourceLocation compositeLoc) {}
	private static final List<PendingIcon> PENDING = new ArrayList<>();

	private static class AnimatedIcon {
		final NativeImage[] frames;
		final DynamicTexture texture;
		final int width;
		final int height;

		AnimatedIcon(NativeImage[] frames, DynamicTexture texture, int width, int height) {
			this.frames = frames;
			this.texture = texture;
			this.width = width;
			this.height = height;
		}
	}

	private static final List<AnimatedIcon> ANIMATED = new ArrayList<>();

	/** Returns the texture location that will be generated for an icon. */
	public static ResourceLocation borderedIcon(ResourceLocation baseIcon) {
		String basePath = baseIcon.getPath()
				.replace("textures/", "")
				.replace(".png", "");
		ResourceLocation compositeLoc = Hemomancy.rloc(basePath + "_bordered");
		PENDING.add(new PendingIcon(baseIcon, compositeLoc));
		return compositeLoc;
	}

	/** Loads the border and icons, then registers the generated textures. */
	public static void generateAll() {
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

		NativeImage borderSheet;
		try {
			borderSheet = loadImage(resourceManager, BLOODY_BORDER);
		} catch (IOException e) {
			Hemomancy.LOGGER.error("Failed to load bloody border texture: {}", e.getMessage());
			return;
		}

		int borderSheetW = borderSheet.getWidth();
		int borderSheetH = borderSheet.getHeight();
		int borderFrameH = borderSheetH / FRAME_COUNT;

		for (PendingIcon pending : PENDING) {
			try {
				NativeImage baseOriginal = loadImage(resourceManager, pending.baseIcon());
				int baseW = baseOriginal.getWidth();
				int baseH = baseOriginal.getHeight();

				NativeImage[] frames = new NativeImage[FRAME_COUNT];
				for (int f = 0; f < FRAME_COUNT; f++) {
					NativeImage frame = new NativeImage(baseW, baseH, false);
					for (int y = 0; y < baseH; y++) {
						for (int x = 0; x < baseW; x++) {
							frame.setPixelRGBA(x, y, baseOriginal.getPixelRGBA(x, y));
						}
					}
					int borderYOffset = f * borderFrameH;
					for (int y = 0; y < baseH; y++) {
						for (int x = 0; x < baseW; x++) {
							int bx = Math.min(x * borderSheetW / baseW, borderSheetW - 1);
							int by = Math.min(y * borderFrameH / baseH, borderFrameH - 1);
							int borderPixel = borderSheet.getPixelRGBA(bx, borderYOffset + by);
							int borderAlpha = (borderPixel >> 24) & 0xFF;
							if (borderAlpha > 0) {
								frame.setPixelRGBA(x, y, alphaBlend(frame.getPixelRGBA(x, y), borderPixel));
							}
						}
					}
					frames[f] = frame;
				}

				baseOriginal.close();

				NativeImage initialPixels = copyImage(frames[0]);
				DynamicTexture dynamicTexture = new DynamicTexture(initialPixels);
				Minecraft.getInstance().getTextureManager()
						.register(pending.compositeLoc(), dynamicTexture);

				ANIMATED.add(new AnimatedIcon(frames, dynamicTexture, baseW, baseH));

			} catch (IOException e) {
				Hemomancy.LOGGER.error("Failed to composite bloody border onto {}: {}",
						pending.baseIcon(), e.getMessage());
			}
		}

		borderSheet.close();
		PENDING.clear();
	}

	/** Advances the generated icon animations. */
	private static int lastFrame = -1;

	public static void tick() {
		if (ANIMATED.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		int frame = (int) ((mc.level.getGameTime() / TICKS_PER_FRAME) % FRAME_COUNT);
		if (frame == lastFrame) return;
		lastFrame = frame;

		for (AnimatedIcon anim : ANIMATED) {
			NativeImage target = anim.texture.getPixels();
			if (target == null) continue;

			NativeImage source = anim.frames[frame];
			for (int y = 0; y < anim.height; y++) {
				for (int x = 0; x < anim.width; x++) {
					target.setPixelRGBA(x, y, source.getPixelRGBA(x, y));
				}
			}
			anim.texture.upload();
		}
	}

	private static NativeImage loadImage(ResourceManager resourceManager, ResourceLocation location) throws IOException {
		try (InputStream stream = resourceManager.getResource(location).orElseThrow().open()) {
			return NativeImage.read(stream);
		}
	}

	private static NativeImage copyImage(NativeImage source) {
		NativeImage copy = new NativeImage(source.getWidth(), source.getHeight(), false);
		for (int y = 0; y < source.getHeight(); y++) {
			for (int x = 0; x < source.getWidth(); x++) {
				copy.setPixelRGBA(x, y, source.getPixelRGBA(x, y));
			}
		}
		return copy;
	}

	/**
	 * Alpha-blend foreground over background (both in ABGR format as used by NativeImage).
	 */
	private static int alphaBlend(int background, int foreground) {
		int fgA = (foreground >> 24) & 0xFF;
		int fgB = (foreground >> 16) & 0xFF;
		int fgG = (foreground >> 8) & 0xFF;
		int fgR = foreground & 0xFF;

		int bgA = (background >> 24) & 0xFF;
		int bgB = (background >> 16) & 0xFF;
		int bgG = (background >> 8) & 0xFF;
		int bgR = background & 0xFF;

		float srcA = fgA / 255.0f;
		float dstA = bgA / 255.0f;
		float outA = srcA + dstA * (1.0f - srcA);

		if (outA == 0) return 0;

		int outR = (int) ((fgR * srcA + bgR * dstA * (1.0f - srcA)) / outA);
		int outG = (int) ((fgG * srcA + bgG * dstA * (1.0f - srcA)) / outA);
		int outB = (int) ((fgB * srcA + bgB * dstA * (1.0f - srcA)) / outA);
		int outAi = (int) (outA * 255.0f);

		return (outAi << 24) | (outB << 16) | (outG << 8) | outR;
	}
}
