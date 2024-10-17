package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class PointBlank extends Equipment {
	private static final String ID = "pointBlank";
	private int thres, damage;
	
	public PointBlank(boolean isUpgraded) {
		super(ID, "Point Blank", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = isUpgraded ? 5 : 4;
		damage = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			DamageMeta dm = ev.getMeta();
			if (dm.getOrigin() != DamageOrigin.PROJECTILE) return TriggerResult.keep();
			IProjectileInstance ip = dm.getProjectile();
			if (ip.getOrigin().distanceSquared(ev.getTarget().getLocation()) > thres * thres) return TriggerResult.keep();
			dm.addBuff(BuffType.GENERAL, new Buff(data, damage, 0), BuffOrigin.NORMAL, true);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLACKSTONE_SLAB,
				"Passive. Dealing damage via projectile from at most " + DescUtil.yellow(thres) + " blocks away increases damage by " +
				DescUtil.yellow(damage) + ".");
	}
}
