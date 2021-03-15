
package com.huto.hemomancy.particle;

import com.huto.hemomancy.particle.data.ParticleLightningStorage;
import com.huto.hemomancy.particle.util.Segment;
import com.huto.hemomancy.util.Vector3;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ParticleLightning extends SpriteTexturedParticle {
	private ParticleLightningStorage data;
	public float colorR = 0;
	public float colorG = 0;
	public float colorB = 0;
	private static final IParticleRenderType LIGHTNING_BOLT_RENDER = new IParticleRenderType() {

		public void beginRender(BufferBuilder bufferBuilder, TextureManager textureManager) {
			ParticleLightning.beginRenderCommon(bufferBuilder, textureManager);
		}

		public void finishRender(Tessellator tessellator) {
			tessellator.draw();
			ParticleLightning.endRenderCommon();
		}

		public String toString() {
			return "hemomancy:lightning_bolt";
		}
	};

	public ParticleLightning(ClientWorld worldIn, double startX, double startY, double startZ, double endX, double endY,
			double endZ, IAnimatedSprite sprite, float r, float g, float b) {
		super(worldIn, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		this.sprite = sprite.get(this.rand);
		this.data = new ParticleLightningStorage(new Vector3(startX, startY, startZ), new Vector3(endX, endY, endZ),
				worldIn.rand.nextLong());
		this.maxAge = this.data.getMaxAge() + 5;
		this.setPosition(startX, startY, startZ);
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;
		this.data.setMaxOffset(0.2f);
		this.data.fractalize();
		this.data.finalize();
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		if (this.colorR > 1.0) {
			this.colorR = this.colorR / 255.0f;
		}
		if (this.colorG > 1.0) {
			this.colorG = this.colorG / 255.0f;
		}
		if (this.colorB > 1.0) {
			this.colorB = this.colorB / 255.0f;
		}
	}

	public ParticleLightning(ClientWorld worldIn, double startX, double startY, double startZ, double endX, double endY,
			double endZ, IAnimatedSprite sprite, float r, float g, float b, int speed, int maxAge) {
		super(worldIn, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		this.sprite = sprite.get(this.rand);
		this.data = new ParticleLightningStorage(new Vector3(startX, startY, startZ), new Vector3(endX, endY, endZ),
				worldIn.rand.nextLong(), speed, maxAge);
		this.maxAge = maxAge + 5;
		this.setPosition(startX, startY, startZ);
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;
		this.data.setMaxOffset(0.2f);
		this.data.fractalize();
		this.data.finalize();
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		if (this.colorR > 1.0) {
			this.colorR = this.colorR / 255.0f;
		}
		if (this.colorG > 1.0) {
			this.colorG = this.colorG / 255.0f;
		}
		if (this.colorB > 1.0) {
			this.colorB = this.colorB / 255.0f;
		}
	}

	public ParticleLightning(ClientWorld worldIn, double startX, double startY, double startZ, double endX, double endY,
			double endZ, IAnimatedSprite sprite, float r, float g, float b, int speed, int maxAge, int fract,
			float off) {
		super(worldIn, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		this.sprite = sprite.get(this.rand);
		this.data = new ParticleLightningStorage(new Vector3(startX, startY, startZ), new Vector3(endX, endY, endZ),
				worldIn.rand.nextLong(), speed, maxAge, fract, off);
		this.maxAge = maxAge + 5;
		this.setPosition(startX, startY, startZ);
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;
		
		this.data.fractalize();
		this.data.finalize();
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		if (this.colorR > 1.0) {
			this.colorR = this.colorR / 255.0f;
		}
		if (this.colorG > 1.0) {
			this.colorG = this.colorG / 255.0f;
		}
		if (this.colorB > 1.0) {
			this.colorB = this.colorB / 255.0f;
		}
	}

	@SuppressWarnings("unused")
	public void tick() {
		super.tick();
		this.data.onUpdate();
		if (this.age > this.getMaxAge() - 10) {
			float delta;
			this.particleAlpha = delta = (float) (this.getMaxAge() - this.age) / 10.0f;
		}
	}

	public boolean shouldCull() {
		return false;
	}

	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
		Vector3d vec3d = renderInfo.getProjectedView();
		float f = (float) (MathHelper.lerp((double) partialTicks, (double) this.prevPosX, (double) this.posX)
				- vec3d.getX());
		float f1 = (float) (MathHelper.lerp((double) partialTicks, (double) this.prevPosY, (double) this.posY)
				- vec3d.getY());
		float f2 = (float) (MathHelper.lerp((double) partialTicks, (double) this.prevPosZ, (double) this.posZ)
				- vec3d.getZ());
		Vector3 posOffset = new Vector3(f, f1, f2);
		Vector3 particleOrigin = new Vector3(this.posX, this.posY, this.posZ);
		int count = 0;
		int maxIndex = (int) Math.ceil(((float) this.data.getAge() + partialTicks) / (float) this.data.getMaxAge()
				* (float) this.data.numSegments());
		Vector3 lastEnd1 = null;
		Vector3 lastEnd2 = null;
		for (Segment s : this.data.getSegments()) {
			if (count > maxIndex)
				break;
			float width = Math.min(0.01f * this.data.getLength(), 0.04f);
			Vector3 start = s.getStart().subtract(particleOrigin);
			Vector3 end = s.getEnd().subtract(particleOrigin);
			Vector3 dir = end.subtract(start).normalize().scale(this.data.getLength() * 1.0E-4f);
			Vector3[] avector3f = new Vector3[] {
					lastEnd1 == null ? start.add(new Vector3(-width, 0.0, -width)) : lastEnd1.subtract(dir),
					lastEnd2 == null ? start.add(new Vector3(-width, 0.0, width)) : lastEnd2.subtract(dir),
					end.add(new Vector3(width, 0.0, width)), end.add(new Vector3(width, 0.0, -width)) };
			lastEnd1 = avector3f[2];
			lastEnd2 = avector3f[3];
			for (int i = 0; i < 4; ++i) {
				avector3f[i] = avector3f[i].add(posOffset);
			}
			float minU = this.getMinU();
			float maxU = this.getMaxU();
			float minV = this.getMinV();
			float maxV = this.getMaxV();
			int j = 0xF00000;
			buffer.pos((double) avector3f[3].x, (double) avector3f[3].y, (double) avector3f[3].z).tex(maxU, maxV)
					.color(this.colorR, this.colorG, this.colorB, this.particleAlpha).lightmap(j).endVertex();
			buffer.pos((double) avector3f[2].x, (double) avector3f[2].y, (double) avector3f[2].z).tex(maxU, minV)
					.color(this.colorR, this.colorG, this.colorB, this.particleAlpha).lightmap(j).endVertex();
			buffer.pos((double) avector3f[0].x, (double) avector3f[0].y, (double) avector3f[0].z).tex(minU, minV)
					.color(this.colorR, this.colorG, this.colorB, this.particleAlpha).lightmap(j).endVertex();
			buffer.pos((double) avector3f[1].x, (double) avector3f[1].y, (double) avector3f[1].z).tex(minU, maxV)
					.color(this.colorR, this.colorG, this.colorB, this.particleAlpha).lightmap(j).endVertex();
			buffer.pos((double) avector3f[3].x, (double) avector3f[3].y, (double) avector3f[3].z).tex(maxU, maxV)
					.color(this.colorR, this.colorG, this.colorB, this.particleAlpha).lightmap(j).endVertex();
			buffer.pos((double) avector3f[2].x, (double) avector3f[2].y, (double) avector3f[2].z).tex(maxU, minV)
					.color(this.colorR, this.colorG, this.colorB, this.particleAlpha).lightmap(j).endVertex();
			buffer.pos((double) avector3f[0].x, (double) avector3f[0].y, (double) avector3f[0].z).tex(minU, minV)
					.color(this.colorR, this.colorG, this.colorB, this.particleAlpha).lightmap(j).endVertex();
			buffer.pos((double) avector3f[1].x, (double) avector3f[1].y, (double) avector3f[1].z).tex(minU, maxV)
					.color(this.colorR, this.colorG, this.colorB, this.particleAlpha).lightmap(j).endVertex();

			++count;
		}
	}

	public IParticleRenderType getRenderType() {
		return LIGHTNING_BOLT_RENDER;
	}

	@SuppressWarnings("deprecation")
	private static void beginRenderCommon(BufferBuilder buffer, TextureManager textureManager) {
		RenderSystem.depthMask((boolean) false);
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc((int) 770, (int) 1);
		RenderSystem.alphaFunc((int) 516, (float) 0.003921569f);
		RenderSystem.disableLighting();
		textureManager.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
		Texture tex = textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
		tex.setBlurMipmap(true, false);
		buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

	@SuppressWarnings("deprecation")
	private static void endRenderCommon() {
		Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE)
				.restoreLastBlurMipmap();
		RenderSystem.alphaFunc((int) 516, (float) 0.1f);
		RenderSystem.disableBlend();
		RenderSystem.enableCull();
		RenderSystem.depthMask((boolean) true);
	}

}
