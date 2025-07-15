package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Disappear extends Equipment {
	private static final String ID = "disappear";
	private int damage;
	
	public Disappear(boolean isUpgraded) {
		super(ID, "Disappear", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		damage = isUpgraded ? 100 : 70;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		DisappearInstance inst = new DisappearInstance(id, p, data, slot, this);
		data.addTrigger(ID, Trigger.KILL, inst);
		
		data.addTrigger(ID, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			inst.cancelPrime();
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			inst.cancelPrime();
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			inst.handleAttack((PreBasicAttackEvent) in);
			return TriggerResult.keep();
		});
	}

	private class DisappearInstance extends PriorityAction {
		private Player p;
		private PlayerFightData data;
		private Disappear eq;
		private BukkitTask timer;
		private boolean primed = false;
		private int slot;
		public DisappearInstance(String id, Player p, PlayerFightData data, int slot, Disappear eq) {
			super(id);
			this.p = p;
			this.data = data;
			this.slot = slot;
			this.eq = eq;
			action = (pdata, in) -> {
				timer = new BukkitRunnable() {
					public void run() {
						Sounds.success.play(p, p);
						primed = true;
					}
				}.runTaskLater(NeoRogue.inst(), 40L);
				return TriggerResult.keep();
			};
		}
		
		public void handleAttack(PreBasicAttackEvent ev) {
			if (!primed) return;
			
			Sounds.anvil.play(p, p);
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(ID + slot, eq)));
			primed = false;
		}
		
		public void cancelPrime() {
			if (timer == null && !timer.isCancelled()) return;
			Sounds.error.play(p, p);
			timer.cancel();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLASS_BOTTLE,
				"Passive. On kill, gain " + GlossaryTag.STEALTH.tag(this, 1, false) + "."
				+ " Afterwards, if you don't deal (ignoring poison) or take health damage for <white>2</white> seconds,"
				+ " you gain " + GlossaryTag.PIERCING.tag(this, damage, true) + " on your next basic attack.");
		
	}
}
