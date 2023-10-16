package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.Weapon;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.session.fights.DamageType;
import me.neoblade298.neorogue.session.fights.FightInstance;
import me.neoblade298.neorogue.session.fights.PlayerFightData;

public class IronSword extends Weapon {
	
	public IronSword(boolean isUpgraded) {
		super("ironSword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);
		display = "Iron Sword";
		damage = isUpgraded ? 90 : 60;
		type = DamageType.SLASHING;
		attackSpeed = 1;
		item = createItem(Material.STONE_SWORD, null, null);
		reforgeOptions.add("stoneSword");
		reforgeOptions.add("stoneAxe");
		reforgeOptions.add("stoneDagger");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int hotbar) {
		data.addHotbarTrigger(id, hotbar, Trigger.LEFT_CLICK_HIT, new IronSwordInstance(this, p, data));
	}
	
	private class IronSwordInstance extends EquipmentInstance {
		private Player p;
		private int count = 0;
		private PlayerFightData data;
		public IronSwordInstance(IronSword eq, Player p, PlayerFightData data) {
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
				new BukkitRunnable() {
					public void run() {
						FightInstance.dealDamage(p, type, damage, ((Damageable) inputs[1]));
					}
				}.runTaskLater(NeoRogue.inst(), 5L);
			}
			return true;
		}
	}
}
