package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EarthenLeatherGauntlets extends Weapon {
	private int concuss;
	
	public EarthenLeatherGauntlets(boolean isUpgraded) {
		super("earthenLeatherGauntlets", "Earthen Leather Gauntlets", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		damage = 15;
		type = DamageType.BLUNT;
		attackSpeed = 0.5;
		concuss = isUpgraded ? 5 : 2;
		item = createItem(Material.LEATHER, null, null);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new EarthenLeatherGauntletsInstance(p));
	}
	
	private class EarthenLeatherGauntletsInstance implements TriggerAction {
		private Player p;
		private int count = 0;
		public EarthenLeatherGauntletsInstance(Player p) {
			this.p = p;
		}
		
		@Override
		public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.runActions(data, Trigger.BASIC_ATTACK, inputs);
			if (++count >= 3) {
				count = 0;
				FightInstance.getFightData(((Entity) inputs[1]).getUniqueId()).applyStatus(StatusType.CONCUSSED, p.getUniqueId(), concuss, 0);
			}
			return TriggerResult.keep();
		}
	}
}
