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
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class IronDagger extends Weapon {
	
	public IronDagger(boolean isUpgraded) {
		super("ironDagger", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		display = "Iron Dagger";
		damage = isUpgraded ? 45 : 35;
		type = DamageType.SLASHING;
		attackSpeed = 0.75;
		item = createItem(Material.STONE_SWORD, null, null);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_HIT, new IronDaggerInstance(this, p, data));
	}
	
	private class IronDaggerInstance extends EquipmentInstance {
		private Player p;
		private int count = 0;
		private PlayerFightData data;
		public IronDaggerInstance(IronDagger eq, Player p, PlayerFightData data) {
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
				FightInstance.getFightData(((Entity) inputs[1]).getUniqueId()).applyStatus("BLEED", p.getUniqueId(), 6, 0);
			}
			return true;
		}
	}
}