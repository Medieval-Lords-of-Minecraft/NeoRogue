package me.neoblade298.neorogue.equipment.weapons;


import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class CrimsonBlade extends Equipment {
	private static final String ID = "crimsonBlade";
	private int heal;
	
	public CrimsonBlade(boolean isUpgraded) {
		super(ID, "Crimson Blade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 40 : 35, 1, 0.4, DamageType.SLASHING, Sounds.attackSweep));
		properties.addUpgrades(PropertyType.DAMAGE);
		heal = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new CrimsonBladeInstance(id, p));
	}
	
	private class CrimsonBladeInstance extends PriorityAction {
		private int count;
		private long start;
		public CrimsonBladeInstance(String id, Player p) {
			super(id);
			start = System.currentTimeMillis();
			action = (pdata, inputs) -> {
				LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
				weaponSwingAndDamage(p, pdata, ev.getTarget());
				if (++count >= 5) {
					if (System.currentTimeMillis() - start < 30000) {
						FightInstance.giveHeal(p, heal, p);
					}
					Sounds.enchant.play(p, p);
					count = 0;
				}
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, "For the first <white>30s</white> of a fight, every 5 basic attacks with this weapon heals you for <yellow>" + heal + "</yellow>.");
	}
}
