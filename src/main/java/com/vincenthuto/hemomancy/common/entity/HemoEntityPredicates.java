package com.vincenthuto.hemomancy.common.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HemoEntityPredicates {
	public static List<Predicate<Entity>> PREDICATES = new ArrayList<>();
	public static List<Predicate<Entity>> BLOODTYPES = new ArrayList<>();

	public static Predicate<Entity> WARMBLOODED = new Predicate<>() {
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

	public static Predicate<Entity> VORPAL = new Predicate<>() {
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
	public static Predicate<Entity> COLDBLOODED = new Predicate<>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Drowned || e instanceof Stray || e instanceof WaterAnimal || e instanceof SnowGolem) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> LUMINOUS = new Predicate<>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Guardian || e instanceof GlowSquid || e instanceof Witch || e instanceof Allay) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> IRONCLAD = new Predicate<>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof IronGolem || e instanceof Vindicator || e instanceof Pillager) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> NOBLOOD = new Predicate<>() {
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

	public static Predicate<Entity> UNDEAD = new Predicate<>() {
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

	public static Predicate<Entity> ENDERBLOOD = new Predicate<>() {
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
	public static Predicate<Entity> INFERNALBLOOD = new Predicate<>() {
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
	public static Predicate<Entity> PLANTBLOOD = new Predicate<>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof Creeper || e instanceof Slime) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static void init() {
		PREDICATES.add(COLDBLOODED);
		PREDICATES.add(ENDERBLOOD);
		PREDICATES.add(WARMBLOODED);
		PREDICATES.add(VORPAL);
		PREDICATES.add(NOBLOOD);
		PREDICATES.add(UNDEAD);
		PREDICATES.add(INFERNALBLOOD);
		PREDICATES.add(PLANTBLOOD);
		PREDICATES.add(LUMINOUS);
		PREDICATES.add(IRONCLAD);
		BLOODTYPES.add(COLDBLOODED);
		BLOODTYPES.add(ENDERBLOOD);
		BLOODTYPES.add(WARMBLOODED);
		BLOODTYPES.add(NOBLOOD);
		BLOODTYPES.add(UNDEAD);
		BLOODTYPES.add(INFERNALBLOOD);
		BLOODTYPES.add(PLANTBLOOD);
		BLOODTYPES.add(LUMINOUS);
		BLOODTYPES.add(IRONCLAD);
	}

}
