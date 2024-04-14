package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class EscapePlan extends Equipment {
	private static final String ID = "escapePlan";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD).count(50).spread(0.5, 0);
	
	public EscapePlan(boolean isUpgraded) {
		super(ID, "Escape Plan", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 25, 15, 0));
		damage = isUpgraded ? 100 : 70;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Substitution.get(), Preparation.get(), Darkness.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EscapePlanInstance inst = new EscapePlanInstance(p, this, slot, es);
		data.addTrigger(id, bind, inst);
		data.addTrigger(ID, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.getStatusId().equals(StatusType.INVISIBLE.name())) {
				inst.reduceCooldown(3);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (!inst.basicAttack) return TriggerResult.keep();
			BasicAttackEvent ev = (BasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage, DamageType.SLASHING));
			Sounds.anvil.play(p, p);
			return TriggerResult.keep();
		});
	}
	
	private class EscapePlanInstance extends EquipmentInstance {
		private Location loc;
		private boolean active = false, basicAttack = false;
		public EscapePlanInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			action = (pdata, in) -> {
				if (active) {
					activeCast(pdata);
				}
				else {
					inactiveCast(pdata);
				}
				return TriggerResult.keep();
			};
		}
		
		private void inactiveCast(PlayerFightData pdata) {
			active = true;
			Sounds.equip.play(p, p);
			loc = p.getLocation();
			basicAttack = true;
			pdata.addTask(new BukkitRunnable() {
				private int count;
				public void run() {
					if (++count > 10) {
						this.cancel();
						basicAttack = false;
					}
					pc.play(p, loc);
				}
			}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
			this.setCooldown(0);
			this.setNextStaminaCost(0);
			this.setNextManaCost(0);
		}
		
		private void activeCast(PlayerFightData pdata) {
			active = false;
			p.teleport(loc);
			Sounds.teleport.play(p, p);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, drop a marker on the ground that you can teleport to and deactivate on re-cast. It stays active <white>10</white> seconds."
				+ " While active, basic attacks deal an additional " + GlossaryTag.SLASHING.tag(this, damage, true)
						+ " damage. Becoming " + GlossaryTag.INVISIBLE.tag(this) + " reduces the cooldown of this ability by <white>3</white>.");
	}
}
