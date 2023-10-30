package com.vincenthuto.hemomancy.client.particle.util;

import java.util.Random;
import java.util.function.Predicate;

import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.world.entity.Entity;

public class EntityParticleUtils {
	public static ParticleColor genRandomColor() {
		Random rand = new Random();
		return new ParticleColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
	}

	public static ParticleColor getColorFromPredicate(Predicate<Entity> pred) {
		if (pred == HemoEntityPredicates.COLDBLOODED) {
			return ParticleColor.CYAN;
		}
		if (pred == HemoEntityPredicates.ENDERBLOOD) {
			return ParticleColor.PURPLE;
		}
		if (pred == HemoEntityPredicates.INFERNALBLOOD) {
			return ParticleColor.ORANGE;
		}
		if (pred == HemoEntityPredicates.NOBLOOD) {
			return ParticleColor.BLACK;
		}
		if (pred == HemoEntityPredicates.PLANTBLOOD) {
			return ParticleColor.GREEN;
		}
		if (pred == HemoEntityPredicates.UNDEAD) {
			return ParticleColor.UNDEADBLOOD;
		}
		if (pred == HemoEntityPredicates.WARMBLOODED) {
			return ParticleColor.RED;
		}
		return ParticleColor.BLOOD;

	}

	public static Predicate<Entity> getEntityPredicate(Entity ent) {
		for (Predicate<Entity> pred : HemoEntityPredicates.BLOODTYPES) {
			if (pred.test(ent)) {
				return pred;
			}
		}
		return HemoEntityPredicates.WARMBLOODED;

	}
}
