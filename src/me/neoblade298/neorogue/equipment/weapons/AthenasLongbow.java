package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.abilities.Emberhail;
import me.neoblade298.neorogue.equipment.abilities.Saboteur;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class AthenasLongbow extends Bow {
	private static final String ID = "AthenasLongbow";
	private double damageBuff;
	
	public AthenasLongbow(boolean isUpgraded) {
		super(ID, "Athena's Longbow", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(50, 1, 0, 14, 0.2, 2));
		damageBuff = isUpgraded ? 1.0 : 0.5;
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(Emberhail.get(), Frostreaver.get());
		addReforge(Saboteur.get(), EdgeOfHorizon.get());
		addReforge(DoubleTap.get(), DaedalusStormbow.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Standard bow shooting
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			Vector arrowVelocity = ((ProjectileLaunchEvent) in).getEntity().getVelocity();
			if (!canShoot(data, arrowVelocity)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			ProjectileGroup proj = new ProjectileGroup(new BowProjectile(data, ev.getEntity().getVelocity(), this, id + slot));
			proj.start(data);
			return TriggerResult.keep();
		});
		
		// Increase non-basic attack damage by 50% or 100%
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (ev.getMeta().isBasicAttack()) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.multiplier(data, damageBuff, BuffStatTracker.damageBuffAlly(id + slot, getUnupgraded())));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW, "Passive. Increase non-basic attack damage by <yellow>" + 
				(int)(damageBuff * 100) + "%</yellow>.");
	}
}
