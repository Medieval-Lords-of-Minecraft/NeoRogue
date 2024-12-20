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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class SerratedArrow extends Ammunition {
	private static final String ID = "serratedArrow";

	private double damage;
	private int stacks;
	
	public SerratedArrow(boolean isUpgraded) {
		super(ID, "Serrated Arrow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(8, 0.1, DamageType.PIERCING));
				damage = 0.2;
				stacks = isUpgraded ? 4 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity trg) {
		FightData fd = FightInstance.getFightData(trg);
		fd.applyStatus(StatusType.REND, inst.getOwner(), stacks, -1);
		int total = fd.getStatus(StatusType.REND).getStacks();
		meta.addDamageSlice(new DamageSlice(inst.getOwner(), damage * total, DamageType.PIERCING));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW, "Applies " + GlossaryTag.REND.tag(this, stacks, true) + ". Deals an additional " + 
		GlossaryTag.REND.tag(this, damage, false) + " damage for every stack of " + 
		GlossaryTag.REND.tag(this) + " the target has.");
	}
}
