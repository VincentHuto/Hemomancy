package com.huto.hemomancy.particle;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ParticleUtil {
	public static Random r = new Random();
	public static ParticleColor RED = new ParticleColor(255, 0, 0);
	public static ParticleColor GREEN = new ParticleColor(0, 255, 0);
	public static ParticleColor BLUE = new ParticleColor(0, 0, 255);
	public static ParticleColor ORANGE = new ParticleColor(255, 200, 0);
	public static ParticleColor PURPLE = new ParticleColor(255, 0, 255);
	public static ParticleColor YELLOW = new ParticleColor(255, 255, 0);
	public static ParticleColor CYAN = new ParticleColor(0, 255, 255);
	public static ParticleColor WHITE = new ParticleColor(255, 255, 255);
	public static ParticleColor BlACK = new ParticleColor(0, 0, 0);

	public static double inRange(double min, double max) {
		return ThreadLocalRandom.current().nextDouble(min, max);
	}

	public static double getCenterOfBlock(double a) {
		return (a + .5);
	}

	/*
	 * An Imploding Sphere Shape
	 */
	public static Vector3d[] implode(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		// odd numbers = horizontal lines
		// even numbers = vertical lines
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2;
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			points[i] = new Vector3d(x * Math.sqrt(Math.pow(Math.tan(rotThing), 0.75)) * radMod,
					y * Math.sqrt(Math.pow(Math.tan(rotThing), 0.75)) * radMod,
					z * Math.sqrt(Math.pow(Math.tan(rotThing), 0.75)) * radMod);

		}
		return points;
	}

	/*
	 * Like Imploding Sphere except its always funneling down
	 */
	public static Vector3d[] tangentFunnel(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		// odd numbers = horizontal lines
		// even numbers = vertical lines
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2;
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			points[i] = new Vector3d(x * Math.sqrt(Math.pow(Math.tan(rotThing), 0.75)) * radMod, y * radMod,
					z * Math.sqrt(Math.pow(Math.tan(rotThing), 0.75)) * radMod);

		}
		return points;
	}

	/*
	 * Pulls in the X direction and Pinches in the Z
	 */
	public static Vector3d[] squashAndStretch(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2;
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			points[i] = new Vector3d(x * Math.pow(Math.tan(rotThing) * radMod, -0.35), y * radMod,
					z * Math.pow(Math.tan(rotThing), 0.35) * radMod);

		}
		return points;
	}

	/*
	 * Starting at a small point shoots rays out to the radius
	 */
	public static Vector3d[] inversedSphere(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2;
			double radius = Math.sqrt(1 - y * y) * 1;
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 3;
			points[i] = new Vector3d(x * Math.pow(Math.sin(rotThing), exp) * radMod,
					y * Math.pow(Math.sin(rotThing), exp * 1) * radMod, z * Math.pow(Math.sin(rotThing), exp) * radMod);

		}
		return points;
	}

	/*
	 * A misty cloud that grows up and down in a blazar shape
	 */
	public static Vector3d[] cosmicBirth(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = Math.abs(Math.sin(rotMod) - (i / ((float) numPoint - 1)) * 0.25);
			double radius = Math.sqrt(1 - y * y) * 1;
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0. ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 3;
			points[i] = new Vector3d(x * Math.pow(Math.sin(rotThing), exp) * radMod,
					y * Math.pow(Math.sin(rotThing), exp * 2) * radMod, z * Math.pow(Math.sin(rotThing), exp) * radMod);

		}
		return points;
	}

	/*
	 * A misty cloud that grows down and up in a blazar shape
	 */
	public static Vector3d[] cosmicBirthFlip(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		// odd numbers = horizontal lines
		// even numbers = vertical lines
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = -Math.abs(Math.sin(rotMod) - (i / ((float) numPoint - 1)) * 0.25);
			double radius = Math.sqrt(1 - y * y) * 1;
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0. ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 3;
			points[i] = new Vector3d(x * Math.pow(Math.sin(rotThing), exp) * radMod,
					y * Math.pow(Math.sin(rotThing), exp * 2) * radMod, z * Math.pow(Math.sin(rotThing), exp) * radMod);

		}
		return points;
	}

	/*
	 * Like Cosmic Birth but Large on outside and not inside
	 */
	public static Vector3d[] cosmicBirthInverse(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = Math.abs(Math.sin(rotMod) - (i / ((float) numPoint - 1)) * 0.25);
			double radius = Math.exp(1 - y * y) * 1;
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0. ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 3;
			points[i] = new Vector3d(x * Math.pow(Math.sin(rotThing), exp) * radMod,
					y * Math.pow(Math.sin(rotThing), exp * 2) * radMod, z * Math.pow(Math.sin(rotThing), exp) * radMod);

		}
		return points;
	}

	/*
	 * Like Cosmic Birth but Large on outside and not inside
	 */
	public static Vector3d[] cosmicBirthInverseFlip(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = -Math.abs(Math.sin(rotMod) - (i / ((float) numPoint - 1)) * 0.25);
			double radius = Math.exp(1 - y * y) * 1;
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0. ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 3;
			points[i] = new Vector3d(x * Math.pow(Math.sin(rotThing) * radMod, exp),
					y * Math.pow(Math.sin(rotThing), exp * 2) * radMod, z * Math.pow(Math.sin(rotThing), exp) * radMod);

		}
		return points;
	}

	/*
	 * Like a lotus fountain with point twords y+
	 */
	public static Vector3d[] bloomingFlower(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = -Math.abs(Math.sin(rotMod) - (i / ((float) numPoint - 1)) * 0.15);
			double radius = Math.cbrt(-Math.max(Math.sqrt(1 - y * y) * 1, 1.0));
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 2;
			points[i] = new Vector3d(x * Math.pow(Math.cos(rotThing) * radMod, exp),
					y * Math.pow(Math.sin(rotThing), exp * 2) * radMod, z * Math.pow(Math.cos(rotThing), exp) * radMod);

		}
		return points;
	}

	/*
	 * Like a lotus fountain with point twords y-
	 */
	public static Vector3d[] bloomingFlowerFlip(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = Math.abs(Math.sin(rotMod) - (i / ((float) numPoint - 1)) * 0.15);
			double radius = Math.max(Math.sqrt(1 - y * y) * 1, 0.75);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 3;
			points[i] = new Vector3d(x * Math.pow(Math.cos(rotThing) * radMod, exp),
					y * Math.pow(Math.sin(rotThing), exp * 2) * radMod, z * Math.pow(Math.cos(rotThing), exp) * radMod);

		}
		return points;
	}

	public static Vector3d[] lotusFountain(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !!isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = -Math.abs(Math.sin(rotMod) - (i / ((float) numPoint - 1)) * 0.15);
			double radius = Math.sqrt(1 - y * y) * 1;
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 3;
			points[i] = new Vector3d(x * Math.pow(Math.cos(rotThing) * radMod, exp),
					y * Math.pow(Math.sin(rotThing), exp * 2) * radMod, z * Math.pow(Math.cos(rotThing), exp) * radMod);

		}
		return points;
	}

	public static Vector3d[] lotusFountainFlip(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = Math.abs(Math.sin(rotMod) - (i / ((float) numPoint - 1)) * 0.15);
			double radius = Math.sqrt(1 - y * y) * 1;
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			double exp = 3;
			points[i] = new Vector3d(x * Math.pow(Math.cos(rotThing) * radMod, exp),
					y * Math.pow(Math.sin(rotThing), exp * 2) * radMod, z * Math.pow(Math.cos(rotThing), exp) * radMod);

		}
		return points;
	}

	/*
	 * Like a School of swimming tetras
	 */
	public static Vector3d[] randomSwimming(int numPoint, double rotMod, double radMod, boolean isRand) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = !isRand ? Math.PI * (2.6 - Math.sqrt(.1135)) : Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = !isRand ? Math.PI * (3.0 - Math.sqrt(.75)) : Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = !isRand ? Math.PI * (2.0 - Math.sqrt(.35)) : Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2;
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			double rotThing = rotMod * radius;
			points[i] = new Vector3d(x + Math.cos(rotThing) * radMod, y * Math.pow(Math.sin(radius), 3),
					z + Math.sin(rotThing) * radMod);

		}
		return points;
	}

	/*
	 * Fibbonachi Sphere with vertical Lines
	 */
	public static Vector3d[] fibboSphere(int numPoint, double rotMod, double radMod) {
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
			points[i] = new Vector3d(x * radMod, y * radMod, z * radMod);

		}
		return points;
	}

	/*
	 * A cloud of ever changing particles
	 */
	public static Vector3d[] randomSphere(int numPoint, double rotMod, double radMod) {
		Vector3d[] points = new Vector3d[numPoint];
		double phiX = Math.PI * (2.6 - Math.sqrt(Math.random()));
		double phiO = Math.PI * (3.0 - Math.sqrt(Math.random()));
		double phiE = Math.PI * (2.0 - Math.sqrt(Math.random()));
		for (int i = 0; i < numPoint; i++) {
			double y = 1 - (i / ((float) numPoint - 1)) * 2;
			double radius = Math.sqrt(1 - y * y);
			double theta = i % 2 == 0 ? phiE * i : Math.random() > 0.5 ? phiO * i : phiX * i;
			double x = Math.cos(theta) * radius;
			double z = Math.sin(theta) * radius;
			points[i] = new Vector3d(x * radMod, y * radMod, z * radMod);

		}
		return points;
	}
/*
	public static Vector3d[] test(BlockPos pos, int radMod) {
		Vector3d[] points = new Vector3d[radMod * radMod * radMod];
		double count = 0;
		double values[][][] = new double[radMod][radMod][radMod];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values.length; j++) {
				for (int k = 0; k < values.length; k++) {
					values[i][j][k] = count / 100;
					count++;
				}
			}
		}
		Arrays.fill(points, new Vector3d(0, 0, 0));
		
		 * for (int x = 0; x < radMod; x++) { for (int y = 0; y < radMod; y++) { for
		 * (int z = 0; z < radMod; z++) { points[(x + 1) * (y + 1) * (z + 1)] = new
		 * Vector3d(pos.getX() + x, pos.getY() + y, pos.getZ() + z); } } }
		 
		return points;
	}*/

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