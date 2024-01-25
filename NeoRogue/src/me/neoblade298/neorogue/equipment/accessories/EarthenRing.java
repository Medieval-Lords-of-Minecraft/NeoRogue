package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class EarthenRing extends Equipment {
	private int damage;
	
	public EarthenRing(boolean isUpgraded) {
		super("earthenRing", "Earthen Ring", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY);
		damage = isUpgraded ? 6 : 4;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			LivingEntity target = ev.getTarget();
			FightInstance.dealDamage(data, DamageType.EARTH, damage, target);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_NUGGET, "Basic attacks additionally deal <yellow>" + damage + "</yellow> earthen damage.");
	}
}
