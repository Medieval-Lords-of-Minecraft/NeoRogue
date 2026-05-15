package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DarkShroud extends Equipment {
	private static final String ID = "DarkShroud";
	private int damage;
	
	public DarkShroud(boolean isUpgraded) {
		super(ID, "Dark Shroud", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ACCESSORY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		damage = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < 3) return TriggerResult.keep();
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

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
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				GlossaryTag.POWER.tag(this) + ". Your projectiles deal an additional " + GlossaryTag.DARK.tag(this, damage, true) + " damage.");
	}
}
