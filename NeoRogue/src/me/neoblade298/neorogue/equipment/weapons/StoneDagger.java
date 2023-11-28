package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.player.Status.StatusType;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class StoneDagger extends Weapon {
	
	public StoneDagger(boolean isUpgraded) {
		super("stoneDagger", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		display = "Stone Dagger";
		damage = !isUpgraded ? 25 : 35;
		type = DamageType.SLASHING;
		attackSpeed = 0.75;
		item = createItem(Material.STONE_SWORD, null, null);
		reforgeOptions.add("ironDagger");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_HIT, new StoneDaggerInstance(this, p, data));
	}
	
	private class StoneDaggerInstance extends EquipmentInstance {
		private Player p;
		private int count = 0;
		private PlayerFightData data;
		public StoneDaggerInstance(StoneDagger eq, Player p, PlayerFightData data) {
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
				FightInstance.getFightData(((Entity) inputs[1]).getUniqueId()).applyStatus(StatusType.BLEED, p.getUniqueId(), 2, 0);
			}
			return true;
		}
	}
}
