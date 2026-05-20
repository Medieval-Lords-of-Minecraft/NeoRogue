package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;

public class StoneArrow extends Ammunition {
	private static final String ID = "StoneArrow";
	private static final int thres = 5;

	private int damage;
	
	public StoneArrow(boolean isUpgraded) {
		super(ID, "Stone Arrow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(8, 0.1, DamageType.PIERCING));
				damage = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity trg) {
		if (inst.getOrigin().distanceSquared(inst.getLocation()) >= (thres * thres)) {
			meta.addDamageSlice(new DamageSlice(inst.getOwner(), damage, DamageType.PIERCING, DamageStatTracker.of(id, this)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW, "Deals an additional " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage if it travels at least " + DescUtil.white(thres) + " blocks.");
	}
}
