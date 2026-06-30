package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.EvadeEvent;

public class Evanesce extends Equipment implements Power {
	private static final String ID = "Evanesce";
	private int damage, stealthDuration;
	private double damageBuff;
	
	public Evanesce(boolean isUpgraded) {
		super(ID, "Evanesce", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.none());
		damage = isUpgraded ? 150 : 100;
		stealthDuration = isUpgraded ? 200 : 120;
		damageBuff = isUpgraded ? 0.3 : 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			if (!data.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 1) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.EVADE, (pdata2, in2) -> {
					EvadeEvent ev = (EvadeEvent) in2;
					if (ev.getDamageMeta() == null || ev.getDamageMeta().getOwner() == null) {
						return TriggerResult.keep();
					}
					Player p = data.getPlayer();
					LivingEntity damager = ev.getDamageMeta().getOwner().getEntity();
					Location playerLoc = p.getLocation();
					Location damagerLoc = damager.getLocation();
					Vector awayFromEnemy = playerLoc.toVector().subtract(damagerLoc.toVector()).normalize();
					data.dash(awayFromEnemy);
					FightInstance.dealDamage(pdata2, DamageType.PIERCING, damage, damager, 
							DamageStatTracker.of(id + slot, Evanesce.this));
					FightInstance.applyStatus(p, StatusType.STEALTH, data, 1, stealthDuration, Evanesce.this);
					data.addTask(new BukkitRunnable() {
						public void run() {
							data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
								new Buff(data, 0, damageBuff, StatTracker.damageBuffAlly(id, Evanesce.this)),
								100);
						}
					}.runTaskLater(NeoRogue.inst(), 20L));
					Sounds.attackSweep.play(p, p);
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after evading once while in " + GlossaryTag.STEALTH.tag(this) + ". Upon " + GlossaryTag.EVADE.tag(this) + ", deal " + 
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage to the attacker and " + 
				GlossaryTag.DASH.tag(this) + " away from them. Gain " +
				GlossaryTag.STEALTH.tag(this, 1, false) + " [" + DescUtil.yellow(
                                stealthDuration / 20) + "]. " +
				"After " + DescUtil.white("1s") + ", gain " + DescUtil.yellow((int)(damageBuff * 100) + "%") + " increased " +
				GlossaryTag.GENERAL.tag(this) + " damage " + DescUtil.duration(5, false) + ".");
	}
}
