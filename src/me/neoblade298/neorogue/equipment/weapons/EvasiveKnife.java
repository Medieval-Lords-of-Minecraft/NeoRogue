package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class EvasiveKnife extends Equipment {
	private static final String ID = "evasiveKnife";
	private static final int base = 35;
	private int dmg, stam, hits;
	public EvasiveKnife(boolean isUpgraded) {
		super(ID, "Evasive Knife", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(base, 1.25, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		
		dmg = 20;
		stam = 25;
		hits = isUpgraded ? 3 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			weaponSwingAndDamage(p, data, ev.getTarget(), base + (data.getStamina() >= stam ? dmg : 0));
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, act);

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			if (ev.getWeapon() != this) return TriggerResult.keep();
			act.addCount(1);
			if (act.getCount() >= hits) {
				act.setCount(0);
				data.applyStatus(StatusType.EVADE, data, 1, 60);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Deal an additional <white>" + dmg + "</white> damage if above "
				+ "<white>" + stam + "</white> stamina. Additionally, every <yellow>" + hits + " </yellow>basic attacks grants "
				+ GlossaryTag.EVADE.tag(this, 1, false) + " [<white>3s</white>].");
	}
}
