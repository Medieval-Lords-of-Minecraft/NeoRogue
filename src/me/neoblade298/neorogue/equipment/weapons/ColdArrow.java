package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class ColdArrow extends Ammunition {
	private static final String ID = "coldArrow";
	private int frost;
	
	public ColdArrow(boolean isUpgraded) {
		super(ID, "Cold Arrow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(5, 0.1, DamageType.PIERCING));
				frost = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity trg) {
		FightData fd = FightInstance.getFightData(trg);
		fd.applyStatus(StatusType.FROST, inst.getOwner(), frost, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW,
				"Applies " + GlossaryTag.FROST.tag(this, frost, true) + " on hit.");
	}
}
