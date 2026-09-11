package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.LimitedAmmunition;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;

public class SplinteringArrow extends LimitedAmmunition {
	private static final String ID = "SplinteringArrow";
	private int rend, bonusDamage;

	public SplinteringArrow(boolean isUpgraded) {
		super(ID, "Splintering Arrow", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(40, 0.2, DamageType.PIERCING), isUpgraded ? 20 : 15);
		rend = isUpgraded ? 3 : 2;
		bonusDamage = 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onHit(ProjectileInstance inst, DamageMeta meta, LivingEntity target) {
		FightData fd = FightInstance.getFightData(target);
		fd.applyStatus(StatusType.REND, inst.getOwner(), rend, -1, this);
		int totalRend = fd.getStatus(StatusType.REND).getStacks();
		meta.addDamageSlice(new DamageSlice(inst.getOwner(), bonusDamage * totalRend, DamageType.PIERCING,
				DamageStatTracker.of(id, this)));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ARROW,
				"Limited to " + DescUtil.val(uses) + " uses per fight. Applies " + GlossaryTag.REND.tag(this, rend)
				+ ", then deals an additional " + GlossaryTag.PIERCING.tag(this, bonusDamage)
				+ " damage for every stack of " + GlossaryTag.REND.tag(this) + " the target has.");
	}
}