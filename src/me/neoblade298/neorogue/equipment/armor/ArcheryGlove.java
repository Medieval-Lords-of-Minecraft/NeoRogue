package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ArcheryGlove extends Equipment {
	private static final String ID = "archeryGlove";
	private int thres, dec;
	
	public ArcheryGlove(boolean isUpgraded) {
		super(ID, "Archery Glove", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		thres = isUpgraded ? 6 : 4;
		dec = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!(ev.getInstances().getFirst() instanceof ProjectileInstance)) return TriggerResult.keep();
			act.addCount(ev.getInstances().size());
			while (act.getCount() >= thres) {
				data.addBuff(data, id, false, false, BuffType.PHYSICAL, dec, 100);
				act.addCount(-thres);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, act);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Decrease " + GlossaryTag.PHYSICAL.tag(this) + " damage by " + DescUtil.yellow(dec) + " [<white>5s</white>] for every " +
				DescUtil.yellow(thres) + " projectiles you launch, unstackable.");
	}
}
