package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/** Shared beats of echo casts: projectile stand-ins that fly a fixed line, and short multi-point patterns. */
final class EchoMoves {
	private EchoMoves() {
	}

	/**
	 * A projectile stand-in flying straight from `from` to `to` in `ticks`, with no collision (it hits nothing, so
	 * its arrival is played by `arrive`) and no gravity. Its live renderer and trail draw it as the real projectile.
	 */
	static void fly(EchoCast.EchoContext ctx, AbstractArrow arrow, Vec3 from, Vec3 to, int ticks, Runnable arrive) {
		arrow.setPos(from.x, from.y, from.z);
		arrow.setNoGravity(true);
		arrow.setNoPhysics(true);
		if (EchoDummies.spawn(ctx.level(), arrow, to.subtract(from).scale(1.0D / ticks), ticks + 1)) EchoScheduler.later(ticks, arrive);
		else arrive.run();
	}

	static void shot(EchoCast.EchoContext ctx, Vec3 from, Vec3 to, int ticks, Consumer<BloodShotEntity> setup, Runnable arrive) {
		BloodShotEntity shot = new BloodShotEntity(ctx.level(), from.x, from.y, from.z);
		setup.accept(shot);
		fly(ctx, shot, from, to, ticks, arrive);
	}

	static void needle(EchoCast.EchoContext ctx, Vec3 from, Vec3 to, int ticks, Consumer<BloodNeedleEntity> setup, Runnable arrive) {
		BloodNeedleEntity needle = new BloodNeedleEntity(ctx.level(), from.x, from.y, from.z);
		setup.accept(needle);
		fly(ctx, needle, from, to, ticks, arrive);
	}

	/** `count` points on a horizontal ring of `radius` (life size) around `centre`, starting at `phase` radians. */
	static Vec3[] ring(EchoCast.EchoContext ctx, Vec3 centre, double radius, int count, double phase) {
		Vec3[] points = new Vec3[count];
		for (int i = 0; i < count; i++) {
			double a = phase + i * Math.PI * 2 / count;
			points[i] = centre.add(Math.cos(a) * ctx.scaled(radius), 0, Math.sin(a) * ctx.scaled(radius));
		}
		return points;
	}

	/** `count` points fanned across the loom's front, `spread` (life size) either side of the target. */
	static Vec3[] fan(EchoCast.EchoContext ctx, int count, double spread) {
		Vec3[] points = new Vec3[count];
		for (int i = 0; i < count; i++) {
			double t = count == 1 ? 0 : (i / (double) (count - 1)) * 2 - 1;
			points[i] = ctx.target().add(ctx.side().scale(ctx.scaled(spread) * t));
		}
		return points;
	}
}
