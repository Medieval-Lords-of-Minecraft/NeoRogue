package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class TacticiansDagger extends Equipment {
	private static final String ID = "tacticiansDagger";
	private int damage;
	
	public TacticiansDagger(boolean isUpgraded) {
		super(ID, "Tactician's Dagger", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(30, 1, 0.2, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		damage = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction timer = new StandardPriorityAction(ID);
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		timer.setAction((pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.GENERAL)) {
				return TriggerResult.keep();
			}
			timer.setTime(System.currentTimeMillis());
			inst.setCooldown(3);
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, timer);
		
		inst.setAction((pdata, inputs) -> {
			DamageMeta dm;
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			if (timer.getTime() + 3000 >= System.currentTimeMillis()) {
				dm = new DamageMeta(data, 20, DamageType.PIERCING);
			}
			else {
				dm = new DamageMeta(data, 20 + damage, DamageType.PIERCING);
			}
			weaponSwingAndDamage(p, data, ev.getTarget(), dm);
			return TriggerResult.keep();
		});

		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "Deal an additional " + GlossaryTag.PIERCING.tag(this, damage, true) + " if "
				+ "you haven't dealt " + GlossaryTag.GENERAL.tag(this) + " damage in <white>3</white> seconds.");
	}
}
