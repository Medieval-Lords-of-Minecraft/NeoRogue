package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class PiercingShot2 extends Equipment {
	private static final String ID = "piercingShot2";
	private int damage;
	
	public PiercingShot2(boolean isUpgraded) {
		super(ID, "Piercing Shot II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 20 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta md = new ActionMeta();
		data.addTrigger(ID, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata2, in) -> {
			md.addCount(1);
			if (md.getCount() < 3) return TriggerResult.keep();
			md.addCount(-3);
			LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
			if (!ev.isBowProjectile()) return TriggerResult.keep();
			ProjectileInstance inst = (ProjectileInstance) ev.getInstances().getFirst();
			inst.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING));
			BowProjectile settings = (BowProjectile) inst.getParent();
			settings.pierce(1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"Passive. Every <white>third</white> basic attack deals an additional " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage and pierce " +
				DescUtil.white(1) + " enemy.");
	}
}
