package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Momentum extends Equipment {
	private static final String ID = "momentum";
	private static final int DISTANCE = 5;
	private int damage, dur;
	
	public Momentum(boolean isUpgraded) {
		super(ID, "Momentum", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
				damage = isUpgraded ? 20 : 10;
				dur = 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		double distSq = DISTANCE * DISTANCE;
		String uuid = UUID.randomUUID().toString();
		data.addTrigger(id, bind, (pdata, in) -> {
			if (am.getLocation() != null && am.getLocation().distanceSquared(p.getLocation()) < distSq) {
				data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(this)), uuid, dur * 20);
			}
			am.setLocation(p.getLocation());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOW_ITEM_FRAME,
				"Passive. Upon firing a projectile, if you are at least " + DescUtil.white(DISTANCE) + " blocks away from where you last fired a projectile, " +
				"increase your damage by " + DescUtil.yellow(damage) + " " + DescUtil.duration(dur, false) + ".");
	}
}
