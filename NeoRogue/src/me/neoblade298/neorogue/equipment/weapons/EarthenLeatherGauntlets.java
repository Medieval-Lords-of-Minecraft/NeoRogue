package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.player.Status.StatusType;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class EarthenLeatherGauntlets extends Weapon {
	private int concuss;
	
	public EarthenLeatherGauntlets(boolean isUpgraded) {
		super("earthenLeatherGauntlets", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		display = "Earthen Leather Gauntlets";
		damage = 15;
		type = DamageType.BLUNT;
		attackSpeed = 0.5;
		concuss = isUpgraded ? 5 : 2;
		item = createItem(Material.LEATHER, null, null);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_HIT, new EarthenLeatherGauntletsInstance(this, p, data));
	}
	

	
	private class EarthenLeatherGauntletsInstance extends EquipmentInstance {
		private Player p;
		private int count = 0;
		private PlayerFightData data;
		public EarthenLeatherGauntletsInstance(Equipment eq, Player p, PlayerFightData data) {
			super(eq);
			this.p = p;
			this.data = data;
		}
		
		@Override
		public boolean run(Object[] inputs) {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.runActions(Trigger.BASIC_ATTACK, inputs);
			if (++count >= 3) {
				count = 0;
				FightInstance.getFightData(((Entity) inputs[1]).getUniqueId()).applyStatus(StatusType.CONCUSSED, p.getUniqueId(), concuss, 0);
			}
			return true;
		}
	}
}
