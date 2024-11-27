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

public class BluntedArrow extends Ammunition {
	private static final String ID = "bluntedArrow";
	private int stacks;
	
	public BluntedArrow(boolean isUpgraded) {
		super(ID, "Blunted Arrow", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(5, 0.1, DamageType.BLUNT));
				stacks = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity trg) {
		FightData fd = FightInstance.getFightData(trg);
		fd.applyStatus(StatusType.INJURY, inst.getOwner(), stacks, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW,
				"Applies " + GlossaryTag.INJURY.tag(this, stacks, true) + " on hit.");
	}
}
