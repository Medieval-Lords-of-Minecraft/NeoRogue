package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class DarkShroud extends Equipment {
	private static final String ID = "DarkShroud";
	private int damage;
	
	public DarkShroud(boolean isUpgraded) {
		super(ID, "Dark Shroud", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ACCESSORY,
				EquipmentProperties.ofUsable(20, 0, 0, 0));
		damage = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata2, in2) -> {
				LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in2;
				
				for (IProjectileInstance pi : ev.getInstances()) {
					ProjectileInstance proj = (ProjectileInstance) pi;
					proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, 
							DamageStatTracker.of(id + slot, this)));
				}
				
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				GlossaryTag.POWER.tag(this) + ". Your projectiles deal an additional " + GlossaryTag.DARK.tag(this, damage, true) + " damage.");
	}
}
