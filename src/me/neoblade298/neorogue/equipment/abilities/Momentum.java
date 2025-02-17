package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

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
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			if (am.getLocation() != null && am.getLocation().distanceSquared(p.getLocation()) >= distSq) {
				LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
				for (IProjectileInstance ipi : ev.getInstances()) {
					if (!(ipi instanceof ProjectileInstance)) continue;
					ProjectileInstance pi = (ProjectileInstance) ipi;
					pi.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(this)));
				}
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
