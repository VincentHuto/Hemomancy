package com.huto.hemomancy.particle;

import java.util.UUID;

import com.huto.hemomancy.Hemomancy;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LaserParticle extends BreakingParticle {
	private BlockState blockState;
	private UUID playerUUID;
	private double sourceX;
	private double sourceY;
	private double sourceZ;
	private int speedModifier;

	public LaserParticle(ClientWorld world, double d, double d1, double d2, double xSpeed, double ySpeed, double zSpeed,
			float size, float red, float green, float blue, boolean depthTest, float maxAgeMul, BlockState blockState) {
		this(world, d, d1, d2, xSpeed, ySpeed, zSpeed, size, red, green, blue, depthTest, maxAgeMul);
		this.blockState = blockState;
		this.setSprite(
				Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(blockState));
	}

	public LaserParticle(ClientWorld world, double d, double d1, double d2, double xSpeed, double ySpeed, double zSpeed,
			float size, float red, float green, float blue, boolean depthTest, float maxAgeMul) {
		super(world, d, d1, d2, ItemStack.EMPTY);
		// super applies wiggle to motion so set it here instead
		motionX = xSpeed;
		motionY = ySpeed;
		motionZ = zSpeed;
		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleGravity = 0;
		particleScale *= size;
		maxAge = Math.round(maxAgeMul);
		setSize(0.001F, 0.001F);
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		sourceX = d;
		sourceY = d1;
		sourceZ = d2;
		this.canCollide = false;
	}

	@Override
	public void renderParticle(IVertexBuilder builder, ActiveRenderInfo activeRenderInfo, float partialTicks) {
		super.renderParticle(builder, activeRenderInfo, partialTicks);
	}

	public boolean particleToPlayer(PlayerEntity player) {
		boolean partToPlayer = false;
		BlockPos sourcePos = new BlockPos(sourceX, sourceY, sourceZ);
		if (!(world.getBlockState(sourcePos) == this.blockState))
			partToPlayer = true;
		return partToPlayer;
	}

	// [VanillaCopy] of super, without drag when onGround is true
	@Override
	public void tick() {
		double moveX;
		double moveY;
		double moveZ;
		// this.particleScale = 0.5f;
		// double getPartScale = this.getScale(0);

		if (this.playerUUID == null) {
			this.setExpired();
			return;
		}
		// Some calculations for the particle motion
		PlayerEntity player = world.getPlayerByUuid(this.playerUUID);
		if (player == null) {
			this.setExpired();
			return;
		}
		Vector3d playerPos = player.getPositionVec().add(0, player.getEyeHeight(), 0);
		Vector3d blockPos = new Vector3d(sourceX, sourceY, sourceZ);
		Vector3d look = player.getLookVec(); // or getLook(partialTicks)
		// The next 3 variables are directions on the screen relative to the players
		// look direction. So right = to the right of the player, regardless of facing
		// direction.
		Vector3d right = new Vector3d(-look.z, 0, look.x).normalize();
		Vector3d forward = look;
		Vector3d down = right.crossProduct(forward);

		// These are used to calculate where the particles are going. We want them going
		// into the laser, so we move the destination right, down, and forward a bit.
		right = right.scale(0.65f);
		forward = forward.scale(0.85f);
		down = down.scale(-0.35);

		// Take the player's eye position, and shift it to where the end of the laser is
		// (Roughly)
		Vector3d laserPos = playerPos.add(right);
		laserPos = laserPos.add(forward);
		laserPos = laserPos.add(down);

		// Get the current position of the particle, and figure out the vector of where
		// it's going
		Vector3d partPos = new Vector3d(this.posX, this.posY, this.posZ);
		Vector3d targetDirection = new Vector3d(laserPos.getX() - this.posX, laserPos.getY() - this.posY,
				laserPos.getZ() - this.posZ);

		// The total distance between the laser's endpoint and the block(s) we're mining
		double totalDistance = blockPos.distanceTo(laserPos);

		// Figure out if the particles are flowing TO the player, or BACK to the blocks
		// This is like age, how many ticks the thing has been around, but we reset it
		// when we send particles back to their source so can't use age.
		speedModifier++;
		// Basically we want it to get faster the longer its been around, up to a limit
		int speedAdjust = (30 - speedModifier) <= 0 ? 1 : (30 - speedModifier);
		// Get the distance between the laser (endpoint) and current particle position
		double distance = laserPos.distanceTo(partPos);
		// Remove the particle from the game if its really close to the laser endpoint.
		if (distance < 0.25) {
			this.setExpired();
		}
		// Apply the spinning effect, but only if the particle has been around for a
		// bit, and slow the spin it gets closer to player.
		if (age > 5) {
			float spinSpeed = MathHelper.lerp(1 - (float) distance / (float) totalDistance, 1.1f, 0.05f);
			targetDirection = targetDirection.add(targetDirection.crossProduct(look).scale(spinSpeed).mul(3, 3, 3));
		}
		this.particleScale = particleScale
				* MathHelper.lerp(1 - (float) distance / (float) totalDistance, 1.05f, 0.85f);
		moveX = (targetDirection.getX()) / speedAdjust;
		moveY = (targetDirection.getY()) / speedAdjust;
		moveZ = (targetDirection.getZ()) / speedAdjust;
		if (age < 5) {
			int compressionFactor = 7;
			moveX = moveX * ((1 - Math.abs(look.x)) * compressionFactor);
			moveY = moveY * ((1 - Math.abs(look.y)) * compressionFactor);
			moveZ = moveZ * ((1 - Math.abs(look.z)) * compressionFactor);
		}
		if (this.age++ >= this.maxAge) {
			this.setExpired();
		}
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.move(moveX, moveY, moveZ);

	}

	public void setGravity(float value) {
		particleGravity = value;
	}

	public void setSpeed(float mx, float my, float mz) {
		motionX = mx;
		motionY = my;
		motionZ = mz;
	}

}