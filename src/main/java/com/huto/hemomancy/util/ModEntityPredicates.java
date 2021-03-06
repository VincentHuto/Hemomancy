package com.huto.hemomancy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.huto.hemomancy.entity.mob.EntityChitinite;
import com.huto.hemomancy.entity.mob.EntityChthonian;
import com.huto.hemomancy.entity.mob.EntityChthonianQueen;
import com.huto.hemomancy.entity.mob.EntityFargone;
import com.huto.hemomancy.entity.mob.EntityFungling;
import com.huto.hemomancy.entity.mob.EntityThirster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.monster.SilverfishEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;

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
			if (e instanceof AnimalEntity || e instanceof AmbientEntity || e instanceof AbstractVillagerEntity
					|| e instanceof EntityThirster || e instanceof EntityFargone || e instanceof PlayerEntity) {
				return true;
			} else {
				return false;
			}
		}
	};
	public static Predicate<Entity> VORPAL = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof AbstractRaiderEntity || e instanceof ZombieEntity || e instanceof SkeletonEntity
					|| e instanceof WitherSkeletonEntity || e instanceof SpiderEntity || e instanceof WitherEntity
					|| e instanceof CreeperEntity || e instanceof WolfEntity || e instanceof AbstractPiglinEntity
					|| e instanceof PolarBearEntity) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> COLDBLOODED = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof AbstractRaiderEntity || e instanceof ZombieEntity || e instanceof WaterMobEntity
					|| e instanceof SnowGolemEntity) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> NOBLOOD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof SkeletonEntity || e instanceof WitherSkeletonEntity || e instanceof IronGolemEntity
					|| e instanceof WitherEntity || e instanceof SnowGolemEntity || e instanceof BlazeEntity
					|| e instanceof EndermanEntity || e instanceof ShulkerEntity) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> UNDEAD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof SkeletonEntity || e instanceof WitherSkeletonEntity || e instanceof SpiderEntity
					|| e instanceof EntityChitinite || e instanceof EntityChthonian || e instanceof EntityChthonianQueen
					|| e instanceof IronGolemEntity || e instanceof SilverfishEntity || e instanceof WitherEntity) {
				return true;
			} else {
				return false;
			}
		}
	};
	public static Predicate<Entity> ENDERBLOOD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof EndermanEntity || e instanceof EndermiteEntity || e instanceof EndermiteEntity
					|| e instanceof EnderDragonEntity || e instanceof ShulkerEntity || e instanceof PhantomEntity) {
				return true;
			} else {
				return false;
			}
		}
	};
	public static Predicate<Entity> INFERNALBLOOD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof BlazeEntity || e instanceof AbstractPiglinEntity || e instanceof MagmaCubeEntity
					|| e instanceof StriderEntity || e instanceof GhastEntity || e instanceof HoglinEntity
					|| e instanceof ZombifiedPiglinEntity) {
				return true;
			} else {
				return false;
			}
		}
	};

	public static Predicate<Entity> PLANTBLOOD = new Predicate<Entity>() {
		@Override
		public boolean test(Entity e) {
			if (e instanceof CreeperEntity || e instanceof SlimeEntity || e instanceof EntityFungling) {
				return true;
			} else {
				return false;
			}
		}
	};

}
