package com.vincenthuto.hemomancy.render.handler;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.client.IWeatherRenderHandler;

public class BloodMoonWeatherRenderHandler implements IWeatherRenderHandler {

	private static final ResourceLocation BLOOD_MOON_LOCATION = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/environment/blood_moon_phases.png");
	private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
	private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
	private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");

	public BloodMoonWeatherRenderHandler() {

	}

	@Override
	public void render(int ticks, float partialTicks, ClientLevel world, Minecraft mc, LightTexture lightmapIn,
			double xIn, double yIn, double zIn) {
		LevelRenderer lr = mc.levelRenderer;
		float f = mc.level.getRainLevel(partialTicks);
		if (!(f <= 0.0F)) {
			lightmapIn.turnOnLightLayer();
			Level level = mc.level;
			int x = Mth.floor(xIn);
			int y = Mth.floor(yIn);
			int z = Mth.floor(zIn);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			int l = 5;
			if (Minecraft.useFancyGraphics()) {
				l = 10;
			}

			RenderSystem.depthMask(Minecraft.useShaderTransparency());
			int i1 = -1;
			float f1 = ticks + partialTicks;
			RenderSystem.setShader(GameRenderer::getParticleShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

			for (int j1 = z - l; j1 <= z + l; ++j1) {
				for (int k1 = x - l; k1 <= x + l; ++k1) {
					int l1 = (j1 - z + 16) * 32 + k1 - x + 16;
					double d0 = lr.rainSizeX[l1] * 0.5D;
					double d1 = lr.rainSizeZ[l1] * 0.5D;
					blockpos$mutableblockpos.set(k1, 0, j1);
					Biome biome = level.getBiome(blockpos$mutableblockpos);
					if (biome.getPrecipitation() != Biome.Precipitation.NONE) {
						int i2 = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos$mutableblockpos)
								.getY();
						int j2 = y - l;
						int k2 = y + l;
						if (j2 < i2) {
							j2 = i2;
						}

						if (k2 < i2) {
							k2 = i2;
						}

						int l2 = i2;
						if (i2 < y) {
							l2 = y;
						}

						if (j2 != k2) {
							Random random = new Random(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761);
							blockpos$mutableblockpos.set(k1, j2, j1);
							float f2 = biome.getBaseTemperature();
							if (f2 >= 0.15F) {
								if (i1 != 0) {
									if (i1 >= 0) {
										tesselator.end();
									}

									i1 = 0;
									RenderSystem.setShaderTexture(0, RAIN_LOCATION);
									bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
								}

								int i3 = ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
								//Invert f3 to make particles fly up not down
								float f3 = -(i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
								f3 = -f3;
								double d2 = k1 + 0.5D - xIn;
								double d4 = j1 + 0.5D - zIn;
								float f4 = (float) Math.sqrt(d2 * d2 + d4 * d4) / l;
								//Invert f5 to do some cool transparency at the ground
								float f5 = ((1.0F - f4 * f4) * 0.5f + 0.5F) * f;
								//f5 = -f5;
								blockpos$mutableblockpos.set(k1, l2, j1);
								int j3 = LevelRenderer.getLightColor(level, blockpos$mutableblockpos);
								bufferbuilder.vertex(k1 - xIn - d0 + 0.5D, k2 - yIn, j1 - zIn - d1 + 0.5D)
										.uv(0.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).uv2(j3).endVertex();
								bufferbuilder.vertex(k1 - xIn + d0 + 0.5D, k2 - yIn, j1 - zIn + d1 + 0.5D)
										.uv(1.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).uv2(j3).endVertex();
								bufferbuilder.vertex(k1 - xIn + d0 + 0.5D, j2 - yIn, j1 - zIn + d1 + 0.5D)
										.uv(1.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).uv2(j3).endVertex();
								bufferbuilder.vertex(k1 - xIn - d0 + 0.5D, j2 - yIn, j1 - zIn - d1 + 0.5D)
										.uv(0.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).uv2(j3).endVertex();
							} else {
								if (i1 != 1) {
									if (i1 >= 0) {
										tesselator.end();
									}

									i1 = 1;
									RenderSystem.setShaderTexture(0, SNOW_LOCATION);
									bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
								}

								float f6 = -((ticks & 511) + partialTicks) / 512.0F;
								float f7 = (float) (random.nextDouble() + f1 * 0.01D * ((float) random.nextGaussian()));
								float f8 = (float) (random.nextDouble() + f1 * (float) random.nextGaussian() * 0.001D);
								double d3 = k1 + 0.5D - xIn;
								double d5 = j1 + 0.5D - zIn;
								float f9 = (float) Math.sqrt(d3 * d3 + d5 * d5) / l;
								float f10 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * f;
								blockpos$mutableblockpos.set(k1, l2, j1);
								int k3 = LevelRenderer.getLightColor(level, blockpos$mutableblockpos);
								int l3 = k3 >> 16 & '\uffff';
								int i4 = k3 & '\uffff';
								int j4 = (l3 * 3 + 240) / 4;
								int k4 = (i4 * 3 + 240) / 4;
								bufferbuilder.vertex(k1 - xIn - d0 + 0.5D, k2 - yIn, j1 - zIn - d1 + 0.5D)
										.uv(0.0F + f7, j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4)
										.endVertex();
								bufferbuilder.vertex(k1 - xIn + d0 + 0.5D, k2 - yIn, j1 - zIn + d1 + 0.5D)
										.uv(1.0F + f7, j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4)
										.endVertex();
								bufferbuilder.vertex(k1 - xIn + d0 + 0.5D, j2 - yIn, j1 - zIn + d1 + 0.5D)
										.uv(1.0F + f7, k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4)
										.endVertex();
								bufferbuilder.vertex(k1 - xIn - d0 + 0.5D, j2 - yIn, j1 - zIn - d1 + 0.5D)
										.uv(0.0F + f7, k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4)
										.endVertex();
							}
						}
					}
				}
			}

			if (i1 >= 0) {
				tesselator.end();
			}

			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			lightmapIn.turnOffLightLayer();
		}

	}

}
