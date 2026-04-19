package com.vincenthuto.hemomancy.client.particle;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A flat equilateral triangle of pure black that flies outward from the
 * Sanguine Monolith when it shatters. Each shard tumbles at a unique speed
 * and fades out over its brief lifetime.
 */
@OnlyIn(Dist.CLIENT)
public class MonolithShardParticle extends TextureSheetParticle {

    private final float initSize;
    private final float rollSpeed;

    public MonolithShardParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz,
                                  float size, int lifetime, float rollSpeed,
                                  SpriteSet sprite) {
        super(level, x, y, z, 0, 0, 0);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.initSize = size;
        this.quadSize = size;
        this.lifetime = lifetime;
        this.rollSpeed = rollSpeed;
        this.roll = (float) (level.random.nextFloat() * Math.PI * 2);
        this.oRoll = this.roll;
        this.setColor(0f, 0f, 0f);
        this.alpha = 1.0f;
        this.pickSprite(sprite);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880; // full-bright
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
        this.yd -= 0.006;     // gravity
        this.xd *= 0.965;
        this.yd *= 0.99;
        this.zd *= 0.965;
        this.roll += this.rollSpeed;

        float lifeCoeff = (float) this.age / (float) this.lifetime;
        // quadratic ease so full opacity holds longer then drops fast at end
        this.alpha = 1.0f - lifeCoeff * lifeCoeff;
        this.quadSize = this.initSize * (1.0f - lifeCoeff);
    }

    @Override
    public boolean isAlive() {
        return this.age < this.lifetime;
    }

    /**
     * Renders as an equilateral triangle by emitting a degenerate quad whose
     * first and fourth vertices are coincident, collapsing one edge to a point.
     */
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 camPos = camera.getPosition();
        float px = (float) (Mth.lerp(partialTick, this.xo, this.x) - camPos.x());
        float py = (float) (Mth.lerp(partialTick, this.yo, this.y) - camPos.y());
        float pz = (float) (Mth.lerp(partialTick, this.zo, this.z) - camPos.z());

        Quaternionf q = new Quaternionf(camera.rotation());
        q.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));

        float scale = this.getQuadSize(partialTick);

        // Equilateral triangle: tip at top, base at bottom. Unit circumradius.
        // v0 and v3 are the same point — the degenerate edge collapses this quad
        // into a proper triangle when rendered in QUADS mode.
        Vector3f v0 = new Vector3f(0f,      1f,    0f).rotate(q).mul(scale).add(px, py, pz);
        Vector3f v1 = new Vector3f(-0.866f, -0.5f, 0f).rotate(q).mul(scale).add(px, py, pz);
        Vector3f v2 = new Vector3f(0.866f,  -0.5f, 0f).rotate(q).mul(scale).add(px, py, pz);
        Vector3f v3 = new Vector3f(0f,      1f,    0f).rotate(q).mul(scale).add(px, py, pz);

        float u0   = this.getU0(partialTick);
        float u1   = this.getU1(partialTick);
        float vt   = this.getV0(partialTick);
        float vb   = this.getV1(partialTick);
        float uMid = (u0 + u1) * 0.5f;
        int light  = this.getLightColor(partialTick);

        buffer.vertex(v0.x(), v0.y(), v0.z()).uv(uMid, vt).color(0f, 0f, 0f, this.alpha).uv2(light).endVertex();
        buffer.vertex(v1.x(), v1.y(), v1.z()).uv(u0,   vb).color(0f, 0f, 0f, this.alpha).uv2(light).endVertex();
        buffer.vertex(v2.x(), v2.y(), v2.z()).uv(u1,   vb).color(0f, 0f, 0f, this.alpha).uv2(light).endVertex();
        buffer.vertex(v3.x(), v3.y(), v3.z()).uv(uMid, vt).color(0f, 0f, 0f, this.alpha).uv2(light).endVertex();
    }
}
