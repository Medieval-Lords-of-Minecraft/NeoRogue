package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class Momentum extends Equipment implements Power {
	private static final String ID = "Momentum";
	private static final int DISTANCE = 5;
	private int damage, dur;
	
	public Momentum(boolean isUpgraded) {
		super(ID, "Momentum", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
				damage = isUpgraded ? 20 : 10;
				dur = 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();
			if (count.addCount(1) < 5) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am = new ActionMeta();
		double distSq = DISTANCE * DISTANCE;
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.LAUNCH_PROJECTILE_GROUP, (pdata2, in2) -> {
					Player p2 = data.getPlayer();
					if (am.getLocation() != null && am.getLocation().distanceSquared(p2.getLocation()) >= distSq) {
						LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in2;
						for (ProjectileInstance pi : ev.getInstances()) {
							pi.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(am.getId(), Momentum.this)));
						}
					}
					am.setLocation(p2.getLocation());
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.GLOW_ITEM_FRAME,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after firing " + DescUtil.white(5) + " projectiles while above " + DescUtil.white("50%") + " stamina. Upon firing a projectile, if you are at least " + DescUtil.white(DISTANCE) + " blocks away from where you last fired a projectile, " +
				"increase your damage by " + DescUtil.yellow(damage) + " " + DescUtil.duration(dur, false) + ".");
	}
}
