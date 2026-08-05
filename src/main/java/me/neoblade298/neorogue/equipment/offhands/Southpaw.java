package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;

public class Southpaw extends Equipment {
	private static final String ID = "Southpaw";
	private int damage, concussed;

	public Southpaw(boolean isUpgraded) {
		super(ID, "Southpaw", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR, EquipmentType.OFFHAND,
				EquipmentProperties.custom(0, 0, 10, 0, isUpgraded ? 90 : 60, 0, 0.8, DamageType.BLUNT,
						new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_STRONG)));
		damage = isUpgraded ? 90 : 60;
		concussed = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.RIGHT_CLICK_HIT,
				new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			RightClickHitEvent ev = (RightClickHitEvent) in;
			if (ev.getTarget() instanceof Player) return TriggerResult.keep();
			Player p = data.getPlayer();
			p.swingOffHand();
			DamageMeta meta = new DamageMeta(data, properties.get(PropertyType.DAMAGE), DamageType.BLUNT,
					DamageStatTracker.of(id + slot, this)).setKnockback(properties.get(PropertyType.KNOCKBACK));
			FightInstance.dealDamage(meta, ev.getTarget());
			FightInstance.applyStatus(ev.getTarget(), StatusType.CONCUSSED, data, concussed, -1, this);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Right click an enemy to deal " + GlossaryTag.BLUNT.tag(this, damage)
				+ " damage, apply " + GlossaryTag.CONCUSSED.tag(this, concussed) + ", and knock them back.");
	}
}