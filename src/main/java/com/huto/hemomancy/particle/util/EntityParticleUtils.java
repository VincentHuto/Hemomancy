package com.huto.hemomancy.particle.util;

import java.util.Random;
import java.util.function.Predicate;

import com.huto.hemomancy.util.ModEntityPredicates;
import com.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.world.entity.Entity;

import ParticleColor;

public class EntityParticleUtils {
	public static Predicate<Entity> getEntityPredicate(Entity ent) {
		for (Predicate<Entity> pred : ModEntityPredicates.BLOODTYPES) {
			if (pred.test(ent)) {
				return pred;
			}
		}
		return ModEntityPredicates.WARMBLOODED;

	}

	public static ParticleColor genRandomColor() {
		Random rand = new Random();
		return new ParticleColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
	}

	public static ParticleColor getColorFromPredicate(Predicate<Entity> pred) {
		if (pred == ModEntityPredicates.COLDBLOODED) {
			return ParticleColor.CYAN;
		}
		if (pred == ModEntityPredicates.ENDERBLOOD) {
			return ParticleColor.PURPLE;
		}
		if (pred == ModEntityPredicates.INFERNALBLOOD) {
			return ParticleColor.ORANGE;
		}
		if (pred == ModEntityPredicates.NOBLOOD) {
			return ParticleColor.BLACK;
		}
		if (pred == ModEntityPredicates.PLANTBLOOD) {
			return ParticleColor.GREEN;
		}
		if (pred == ModEntityPredicates.UNDEAD) {
			return ParticleColor.UNDEADBLOOD;
		}
		if (pred == ModEntityPredicates.WARMBLOODED) {
			return ParticleColor.RED;
		}
		return ParticleColor.BLOOD;

	}
}
