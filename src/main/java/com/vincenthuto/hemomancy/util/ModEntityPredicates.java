package com.vincenthuto.hemomancy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;

public class ModEntityPredicates {
	public static List<Predicate<Entity>> PREDICATES = new ArrayList<Predicate<Entity>>();
	public static List<Predicate<Entity>> BLOODTYPES = new ArrayList<Predicate<Entity>>();

	public static void init() {
		PREDICATES.add(COLDBLOODED);
		PREDICATES.add(ENDERBLOOD);
		PREDICATES.add(WARMBLOODED);
		PREDICATES.add(VORPAL);
		PREDICATES.add(NOBLOOD);
		PREDICATES.add(UNDEAD);
		PREDICATES.add(INFERNALBLOOD);
		PREDICATES.add(PLANTBLOOD);
		BLOODTYPES.add(COLDBLOODED);
		BLOODTYPES.add(ENDERBLOOD);
		BLOODTYPES.add(WARMBLOODED);
		BLOODTYPES.add(NOBLOOD);
		BLOODTYPES.add(UNDEAD);
		BLOODTYPES.add(INFERNALBLOOD);
		BLOODTYPES.add(PLANTBLOOD);
	}

	public static Predicate<Entity> WARMBLOODED = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Animal || e instanceof AmbientCreature || e instanceof AbstractVillager
					|| e instanceof Player) {
//|| e instanceof Player) {
				return true;
			} else {
				return false;
			}
		}
	};
	public static Predicate<Entity> VORPAL = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Raider || e instanceof Zombie || e instanceof Skeleton || e instanceof WitherSkeleton
					|| e instanceof Spider || e instanceof WitherBoss || e instanceof Creeper || e instanceof Wolf
					|| e instanceof AbstractPiglin || e instanceof PolarBear) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> COLDBLOODED = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Raider || e instanceof Zombie || e instanceof WaterAnimal || e instanceof SnowGolem) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> NOBLOOD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Skeleton || e instanceof WitherSkeleton || e instanceof IronGolem
					|| e instanceof WitherBoss || e instanceof SnowGolem || e instanceof Blaze || e instanceof EnderMan
					|| e instanceof Shulker) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> UNDEAD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Skeleton || e instanceof WitherSkeleton || e instanceof Spider
			// || e instanceof EntityChitinite || e instanceof EntityChthonian || e
			// instanceof EntityChthonianQueen
					|| e instanceof IronGolem || e instanceof Silverfish || e instanceof WitherBoss) {
				return true;
			} else {
				return false;
			}
		}
	};
	public static Predicate<Entity> ENDERBLOOD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof EnderMan || e instanceof Endermite || e instanceof Endermite || e instanceof EnderDragon
					|| e instanceof Shulker || e instanceof Phantom) {
				return true;
			} else {
				return false;
			}
		}
	};
	public static Predicate<Entity> INFERNALBLOOD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Blaze || e instanceof AbstractPiglin || e instanceof MagmaCube || e instanceof Strider
					|| e instanceof Ghast || e instanceof Hoglin || e instanceof ZombifiedPiglin) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> PLANTBLOOD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Creeper || e instanceof Slime) {
				return true;
			} else {
				return false;
			}
		}
	};

}
