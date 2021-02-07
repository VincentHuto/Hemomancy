package com.huto.hemomancy.particle;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Created by Bailey on 12/26/2016.
 */
public class ParticleUtil {
	public static Random r = new Random();

	public static double inRange(double min, double max) {
		return ThreadLocalRandom.current().nextDouble(min, max);
	}

	public static double getCenterOfBlock(double a) {
		return (a + .5);
	}

	public static Vector3d[] funMovement(int numPoint, double rotMod) {
		Vector3d[] points = new Vector3d[numPoint];
		// odd numbers = horizontal lines
		// even numbers = vertical lines
		double phiX = Math.PI * (2.6 - Math.sqrt(.1135)); // Golden angle in radians
		double phiO = Math.PI * (3.0 - Math.sqrt(.75)); // Golden angle in radians
		double phiE = Math.PI * (2.0 - Math.sqrt(.35)); // Golden angle in radians
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2; // y goes from 1 to -1
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i; // Golden angle Increment
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;

			// Random Swimming
			// points[i] = new Vector3d(x + Math.sin(rotThing), y, z + Math.cos(rotThing));
			// squash and stretch
			points[i] = new Vector3d(x * Math.sqrt(Math.pow(Math.tan(rotThing), 0.75)), y,
					z * Math.sqrt(Math.pow(Math.tan(rotThing), 0.75)));

		}
		return points;
	}

///https://stackoverflow.com/questions/9600801/evenly-distributing-n-points-on-a-sphere
	public static Vector3d[] squashAndStretch(int numPoint, double rotMod) {
		Vector3d[] points = new Vector3d[numPoint];
		// odd numbers = horizontal lines
		// even numbers = vertical lines
		double phiX = Math.PI * (2.6 - Math.sqrt(.1135)); // Golden angle in radians
		double phiO = Math.PI * (3.0 - Math.sqrt(.75)); // Golden angle in radians
		double phiE = Math.PI * (2.0 - Math.sqrt(.35)); // Golden angle in radians
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2; // y goes from 1 to -1
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i; // Golden angle Increment
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;

			// Random Swimming
			// points[i] = new Vector3d(x + Math.sin(rotThing), y, z + Math.cos(rotThing));
			// squash and stretch
			points[i] = new Vector3d(x * Math.pow(Math.tan(rotThing), -0.35), y,
					z * Math.pow(Math.tan(rotThing), 0.35));

		}
		return points;
	}

	public static Vector3d[] randomSwimming(int numPoint, double rotMod) {
		Vector3d[] points = new Vector3d[numPoint];
		// odd numbers = horizontal lines
		// even numbers = vertical lines
		double phiX = Math.PI * (2.6 - Math.sqrt(.1135)); // Golden angle in radians
		double phiO = Math.PI * (3.0 - Math.sqrt(.75)); // Golden angle in radians
		double phiE = Math.PI * (2.0 - Math.sqrt(.35)); // Golden angle in radians
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2; // y goes from 1 to -1
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i; // Golden angle Increment
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;

			// Random Swimming
			points[i] = new Vector3d(x + Math.sin(rotThing), y, z + Math.cos(rotThing));

		}
		return points;
	}

	public static Vector3d[] pointOnSphere(int numPoint, double rotMod) {
		Vector3d[] points = new Vector3d[numPoint];
		// odd numbers = horizontal lines
		// even numbers = vertical lines
		double phiE = Math.PI * (3.0 - Math.sqrt(.35)); // Golden angle in radians
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2; // y goes from 1 to -1
			double radius = Math.sqrt(1 - y * y);
			double theta = phiE * i; // Golden angle Increment
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			// Fibonachi sphere
			points[i] = new Vector3d(x, y, z);

		}
		return points;
	}

	public static Vector3d[] randomSphere(int numPoint, double rotMod) {
		Vector3d[] points = new Vector3d[numPoint];
		// odd numbers = horizontal lines
		// even numbers = vertical lines
		double phiX = Math.PI * (2.6 - Math.sqrt(.1135)); // Golden angle in radians
		double phiO = Math.PI * (3.0 - Math.sqrt(.75)); // Golden angle in radians
		double phiE = Math.PI * (2.0 - Math.sqrt(.35)); // Golden angle in radians
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2; // y goes from 1 to -1
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i; // Golden angle Increment
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			// Fibonachi sphere
			points[i] = new Vector3d(x, y, z);

		}
		return points;
	}

	// https://karthikkaranth.me/blog/generating-random-points-in-a-sphere/
	public static Vector3d pointInSphere() {
		double u = Math.random();
		double v = Math.random();
		double theta = u * 2.0 * Math.PI;
		double phi = Math.acos(2.0 * v - 1.0);
		double r = Math.cbrt(Math.random());
		double sinTheta = Math.sin(theta);
		double cosTheta = Math.cos(theta);
		double sinPhi = Math.sin(phi);
		double cosPhi = Math.cos(phi);
		double x = r * sinPhi * cosTheta;
		double y = r * sinPhi * sinTheta;
		double z = r * cosPhi;
		return new Vector3d(x, y, z);
	}

	public static void spawnFollowProjectile(World world, BlockPos from, BlockPos to) {
		/*
		 * EntityFollowProjectile aoeProjectile = new EntityFollowProjectile(world,
		 * from, to); world.addEntity(aoeProjectile);
		 */
	}

	public static void beam(BlockPos toThisBlock, BlockPos fromThisBlock, World world) {

		double x2 = getCenterOfBlock(toThisBlock.getX());
		double z2 = getCenterOfBlock(toThisBlock.getZ());
		double y2 = getCenterOfBlock(toThisBlock.getY());
		double x1 = getCenterOfBlock(fromThisBlock.getX());
		double z1 = getCenterOfBlock(fromThisBlock.getZ());
		double y1 = getCenterOfBlock(fromThisBlock.getY());
		double d5 = 1.2;
		double d0 = x2 - x1;
		double d1 = y2 - y1;
		double d2 = z2 - z1;
		double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
		d0 = d0 / d3;
		d1 = d1 / d3;
		d2 = d2 / d3;

		double d4 = r.nextDouble();

		while ((d4 + .65) < d3) {
			d4 += 1.8D - d5 + r.nextDouble() * (1.5D - d5);
			if (world.isRemote)
				world.addParticle(ParticleTypes.ENCHANT, x1 + d0 * d4, y1 + d1 * d4, z1 + d2 * d4, 0.0D, 0.0D, 0.0D);
			if (world instanceof ServerWorld) {
				((ServerWorld) world).spawnParticle(ParticleTypes.WITCH, x1 + d0 * d4, y1 + d1 * d4, z1 + d2 * d4,
						r.nextInt(4), 0, 0.0, 0, 0.0);
			}
		}
	}

	public static ParticleColor defaultParticleColor() {
		return new ParticleColor(255, 25, 180);
	}

	public static ParticleColor.IntWrapper defaultParticleColorWrapper() {
		return new ParticleColor.IntWrapper(255, 25, 180);
	}

	public static void spawnPoof(ServerWorld world, BlockPos pos) {
		for (int i = 0; i < 10; i++) {
			double d0 = pos.getX() + 0.5;
			double d1 = pos.getY() + 1.2;
			double d2 = pos.getZ() + .5;
			(world).spawnParticle(ParticleTypes.END_ROD, d0, d1, d2, 2, (world.rand.nextFloat() * 1 - 0.5) / 3,
					(world.rand.nextFloat() * 1 - 0.5) / 3, (world.rand.nextFloat() * 1 - 0.5) / 3, 0.1f);
		}
	}

}