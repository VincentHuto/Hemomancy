package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentEntityEventHandler;
import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * P-1: a held modifier that does not change leaves its attribute clean. A dirty syncable attribute is what makes the
 * server send {@code ClientboundUpdateAttributesPacket} to the player and every tracker, so a clean attribute here
 * means no attribute packet.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AttributeSyncGameTests {
	private static final ResourceLocation ID = Hemomancy.rloc("attribute_sync_probe");
	private static final AttributeModifier.Operation ADD = AttributeModifier.Operation.ADD_VALUE;

	private AttributeSyncGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "attribute_sync", timeoutTicks = 20)
	public static void syncTouchesTheAttributeOnlyOnChange(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
			double base = armor.getValue();
			clearDirty(player);

			AttributeModifiers.sync(armor, ID, 2.0D, ADD, true);
			helper.assertTrue(isDirty(player, Attributes.ARMOR), "Adding the modifier must sync the attribute");
			helper.assertTrue(armor.getValue() == base + 2.0D, "The modifier must apply");
			clearDirty(player);
			for (int tick = 0; tick < 20; tick++) AttributeModifiers.sync(armor, ID, 2.0D, ADD, true);
			helper.assertFalse(isDirty(player, Attributes.ARMOR), "An unchanged modifier must not dirty the attribute");

			AttributeModifiers.sync(armor, ID, 3.0D, ADD, true);
			helper.assertTrue(armor.getModifier(ID).amount() == 3.0D, "A new amount must replace the old one");
			AttributeModifiers.sync(armor, ID, 3.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, true);
			helper.assertTrue(armor.getModifier(ID).operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
					"A new operation must replace the old one");

			AttributeModifiers.sync(armor, ID, 3.0D, ADD, false);
			helper.assertTrue(armor.getModifier(ID) == null, "Absent must remove the modifier");
			clearDirty(player);
			AttributeModifiers.sync(armor, ID, 3.0D, ADD, false);
			helper.assertFalse(isDirty(player, Attributes.ARMOR), "Removing a missing modifier must not dirty the attribute");

			AttributeModifiers.syncPermanent(armor, ID, 2.0D, ADD, true);
			helper.assertTrue(savedModifiers(armor) == 1, "syncPermanent must save the modifier with the entity");
			clearDirty(player);
			AttributeModifiers.syncPermanent(armor, ID, 2.0D, ADD, true);
			helper.assertFalse(isDirty(player, Attributes.ARMOR), "An unchanged permanent modifier must stay put");
			AttributeModifiers.syncPermanent(armor, ID, 2.0D, ADD, false);
			AttributeModifiers.sync(armor, new AttributeModifier(ID, 2.0D, ADD), true);
			helper.assertTrue(savedModifiers(armor) == 0, "sync must hold a transient modifier");
			AttributeModifiers.sync(null, ID, 1.0D, ADD, true);
			helper.succeed();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "attribute_sync", timeoutTicks = 20)
	public static void aHeldElytraSendsNoAttributeUpdate(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			Holder<Attribute> flight = AttributeInit.getFlightAttribute();
			ResourceLocation elytra = AttributeInit.getElytraModifier().id();

			player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
			tick(player);
			helper.assertTrue(player.getAttribute(flight).hasModifier(elytra), "An equipped elytra must grant flight");
			clearDirty(player);
			for (int tick = 0; tick < 5; tick++) tick(player);
			helper.assertFalse(isDirty(player, flight), "A worn elytra must not resync the flight attribute every tick");

			// The effect brings its own flight modifier, which syncs once when it is applied and once when it ends.
			player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
			player.addEffect(new MobEffectInstance(EffectInit.fungal_elytra, 200));
			clearDirty(player);
			tick(player);
			helper.assertTrue(player.getAttribute(flight).hasModifier(elytra), "Fungal Elytra must keep flight");
			helper.assertFalse(isDirty(player, flight), "Swapping elytra for Fungal Elytra must not resync flight");
			for (int tick = 0; tick < 5; tick++) tick(player);
			helper.assertFalse(isDirty(player, flight), "Fungal Elytra must not resync the flight attribute every tick");

			player.removeEffect(EffectInit.fungal_elytra);
			clearDirty(player);
			tick(player);
			helper.assertFalse(player.getAttribute(flight).hasModifier(elytra), "Flight must end with its source");
			helper.assertTrue(isDirty(player, flight), "Losing flight must reach the client");
			helper.succeed();
		}
	}

	private static void tick(ServerPlayer player) {
		HarbingerEquipmentEntityEventHandler.playerTick(new PlayerTickEvent.Post(player));
		HarbingerEquipmentEntityEventHandler.onGlideTick(new PlayerTickEvent.Post(player));
	}

	private static boolean isDirty(ServerPlayer player, Holder<Attribute> attribute) {
		return player.getAttributes().getAttributesToSync().contains(player.getAttribute(attribute));
	}

	private static void clearDirty(ServerPlayer player) {
		player.getAttributes().getAttributesToSync().clear();
	}

	private static int savedModifiers(AttributeInstance attribute) {
		return attribute.save().getList("modifiers", Tag.TAG_COMPOUND).size();
	}
}
