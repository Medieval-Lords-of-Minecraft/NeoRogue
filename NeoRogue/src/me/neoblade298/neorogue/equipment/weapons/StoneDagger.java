package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

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
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, new StoneDaggerInstance(this, p));
	}
	
	private class StoneDaggerInstance extends EquipmentInstance {
		private Player p;
		private int count = 0;
		public StoneDaggerInstance(StoneDagger eq, Player p) {
			super(eq);
			this.p = p;
		}
		
		@Override
		public TriggerResult run(PlayerFightData data, Object[] inputs) {
			FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
			data.runActions(data, Trigger.BASIC_ATTACK, inputs);
			if (++count >= 3) {
				count = 0;
				FightInstance.getFightData(((Entity) inputs[1]).getUniqueId()).applyStatus(StatusType.BLEED, p.getUniqueId(), 2, 0);
			}
			return TriggerResult.keep();
		}
	}
}
