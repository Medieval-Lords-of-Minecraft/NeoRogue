package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class EarthenLeatherGauntlets extends Equipment {
	private int concuss;
	
	public EarthenLeatherGauntlets(boolean isUpgraded) {
		super("earthenLeatherGauntlets", "Earthen Leather Gauntlets", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON, EquipmentProperties.ofWeapon(20, 2, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		concuss = isUpgraded ? 4 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new EarthenLeatherGauntletsInstance(p));
	}
	
	private class EarthenLeatherGauntletsInstance implements TriggerAction {
		private Player p;
		private int count = 0;
		public EarthenLeatherGauntletsInstance(Player p) {
			this.p = p;
		}
		
		@Override
		public TriggerResult trigger(PlayerFightData data, Object inputs) {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			if (++count >= 3) {
				count = 0;
				FightInstance.getFightData(ev.getTarget().getUniqueId()).applyStatus(StatusType.CONCUSSED, p.getUniqueId(), concuss, 0);
			}
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER);
	}
}
