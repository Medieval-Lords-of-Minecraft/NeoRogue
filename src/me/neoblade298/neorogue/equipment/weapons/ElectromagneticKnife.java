package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class ElectromagneticKnife extends Equipment {
	private static final String ID = "ElectromagneticKnife";
	private static int elec, inc;
	
	public ElectromagneticKnife(boolean isUpgraded) {
		super(ID, "Electromagnetic Knife", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(30, 0.5, 0.2, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		elec = 30;
		inc = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new ElectromagneticKnifeInstance(ID));
	}
	
	private class ElectromagneticKnifeInstance extends PriorityAction {
		private int stacks = elec;

		public ElectromagneticKnifeInstance(String id) {
			super(id);

			action = (data, in) -> {
				Player p = data.getPlayer();
				LeftClickHitEvent ev = (LeftClickHitEvent) in;
				weaponSwingAndDamage(p, data, ev.getTarget());
				FightData fd = FightInstance.getFightData(ev.getTarget());
				if (fd.hasStatus(StatusType.ELECTRIFIED)) stacks += inc;
				FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, stacks, -1);
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Every basic attack applies " + GlossaryTag.ELECTRIFIED.tag(this, elec, false) + ", increased "
				+ "by <yellow>" + inc + "</yellow> for every time you basic attack an enemy with " + GlossaryTag.ELECTRIFIED.tag(this) + " (checked before the weapon"
						+ " applies it).");
	}
}
