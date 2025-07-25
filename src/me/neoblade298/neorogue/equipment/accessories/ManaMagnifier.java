package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ManaMagnifier extends Equipment {
	private static final String ID = "manaMagnifier";
	private int buff;

	public ManaMagnifier(boolean isUpgraded) {
		super(ID, "Mana Magnifier", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ACCESSORY);
		buff = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!(ev.getInstances().getFirst() instanceof ProjectileInstance))
				return TriggerResult.keep();
			for (IProjectileInstance inst : ev.getInstances()) {
				((ProjectileInstance) inst).addMaxRange(buff);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI, "Increase range of all projectiles by " + DescUtil.yellow(buff) + ".");
	}
}
