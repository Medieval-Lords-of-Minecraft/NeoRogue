package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
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

public class DarkShroud extends Equipment implements Power {
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
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.LAUNCH_PROJECTILE_GROUP, (pdata2, in2) -> {
					LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in2;
					
					for (IProjectileInstance pi : ev.getInstances()) {
						ProjectileInstance proj = (ProjectileInstance) pi;
						proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, 
								DamageStatTracker.of(id + slot, DarkShroud.this)));
					}
					
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				GlossaryTag.POWER.tag(this) + ". Activates after launching " + DescUtil.white(3) + " projectile groups. Your projectiles deal an additional " + GlossaryTag.DARK.tag(this, damage, true) + " damage.");
	}
}
