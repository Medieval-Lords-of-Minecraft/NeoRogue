package me.neoblade298.neorogue.equipment.weapons;

import java.util.Comparator;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WoodenGreataxe extends Equipment {
	private static final String ID = "WoodenGreataxe";
	private static final int TARGETS = 2;
	private static final TargetProperties TARGET_PROPERTIES = TargetProperties.cone(90, 3, false, TargetType.ENEMY);

	public WoodenGreataxe(boolean isUpgraded) {
		super(ID, "Wooden Greataxe", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(80, isUpgraded ? 0.7 : 0.5, 0.4,
						TARGET_PROPERTIES.range, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		restrictsOffhand = true;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		if (data.getSessionData().getSessionEquipment(EquipSlot.OFFHAND)[0] != null) {
			Player p = data.getPlayer();
			Util.msgRaw(p, Component.text("").append(hoverable).append(Component.text(
					" couldn't be equipped as you have equipment in your offhand!", NamedTextColor.RED)));
			p.getInventory().setItem(slot, null);
			return;
		}

		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			Player p = data.getPlayer();
			LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInCone(p, TARGET_PROPERTIES);
			targets.sort(Comparator.comparingDouble(target -> target.getLocation().distanceSquared(p.getLocation())));
			weaponSwing(p, data);
			for (int i = 0; i < Math.min(TARGETS, targets.size()); i++) {
				LivingEntity target = targets.get(i);
				if (i == 0) {
					weaponDamage(p, data, target);
				} else {
					FightInstance.dealDamage(new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType(),
							DamageStatTracker.of(id + slot, this)), target);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WOODEN_AXE,
				"Can only be used without an offhand. Basic attacks damage the nearest 2 enemies in a cone in front of you.");
	}
}