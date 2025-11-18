package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ArcheryGlove extends Equipment {
	private static final String ID = "ArcheryGlove";
	private int thres, dec;
	
	public ArcheryGlove(boolean isUpgraded) {
		super(ID, "Archery Glove", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		thres = isUpgraded ? 4 : 6;
		dec = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		StandardPriorityAction act = new StandardPriorityAction(id);
		act.setAction((pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!(ev.getInstances().getFirst() instanceof ProjectileInstance)) return TriggerResult.keep();
			act.addCount(ev.getInstances().size());
			while (act.getCount() >= thres) {
				data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, dec, StatTracker.defenseBuffAlly(buffId, this)), 100);
				act.addCount(-thres);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, act);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Decrease " + GlossaryTag.PHYSICAL.tag(this) + " damage taken by " + DescUtil.yellow(dec) + " [<white>5s</white>] for every " +
				DescUtil.yellow(thres) + " projectiles you launch, unstackable.");
	}
}
