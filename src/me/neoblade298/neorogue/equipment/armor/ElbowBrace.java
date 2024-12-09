package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
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

public class ElbowBrace extends Equipment {
	private static final String ID = "elbowBrace";
	private int damageReduction;
	
	public ElbowBrace(boolean isUpgraded) {
		super(ID, "Elbow Brace", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ARMOR);
		damageReduction = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, damageReduction, StatTracker.defenseBuffAlly(this)));
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!(ev.getInstances().getFirst() instanceof ProjectileInstance)) return TriggerResult.keep();
			for (IProjectileInstance inst : ev.getInstances()) {
				((ProjectileInstance) inst).addMaxRange(-2);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_HIDE, "Decrease all " + GlossaryTag.PHYSICAL.tag(this) + " damage taken by <yellow>" + damageReduction + "</yellow>, but " +
				"decrease projectile range by <white>2</white>.");
	}
}
