package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class FerociousDraw extends Equipment {
	private static final String ID = "FerociousDraw";
	private int range, damage;

	public FerociousDraw(boolean isUpgraded) {
		super(ID, "Ferocious Draw", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER, EquipmentType.ABILITY,
				EquipmentProperties.none());
		range = 5;
		damage = isUpgraded ? 80 : 60;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			Player p = data.getPlayer();
			BasicAttackEvent ev = (BasicAttackEvent) in;
			if (!ev.isProjectile())
				return TriggerResult.keep();

			LivingEntity target = ev.getTarget();
			if (target == null)
				return TriggerResult.keep();

			// Check if target is within 5 blocks
			System.out.println("Dist: " + p.getLocation().distance(target.getLocation()));
			if (p.getLocation().distance(target.getLocation()) <= range) {
				ProjectileInstance inst = ev.getProjectile();
				if (inst != null) {
					inst.getMeta().addDamageSlice(
							new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id + slot, this)));
					inst.addPierce(1);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW,
				"Passive. Basic attack projectiles that hit an enemy within <white>" + range + "</white> blocks "
						+ "pierce that enemy and deal an additional " + GlossaryTag.PIERCING.tag(this, damage, true)
						+ " damage.");
	}
}
