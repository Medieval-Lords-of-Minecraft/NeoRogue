package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class EscapePlan extends Equipment {
	private static final String ID = "EscapePlan";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD).count(50).spread(0.5, 0);
	
	public EscapePlan(boolean isUpgraded) {
		super(ID, "Escape Plan", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 10, 15, 0));
		damage = isUpgraded ? 100 : 70;
	}

	@Override
	public void setupReforges() {
		addReforge(BasicDarkArts.get(), Flicker.get(), EscapePlan2.get());
		addReforge(Resourcefulness.get(), Preparation.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EscapePlanInstance inst = new EscapePlanInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
	}
	
	private class EscapePlanInstance extends EquipmentInstance {
		boolean active = false;
		public EscapePlanInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata1, in1) -> {
				Player p = data.getPlayer();
				Sounds.equip.play(p, p);
				Location loc = p.getLocation();
				active = true;
				
				pdata1.addTask(new BukkitRunnable() {
					public void run() {
						if (active) {
							pc.play(p, loc);
						}
						else {
							this.cancel();
						}
					}
				}.runTaskTimer(NeoRogue.inst(), 20L, 0L));
				
				pdata1.addGuaranteedTask(UUID.randomUUID(), new Runnable() {
					public void run() {
						active = false;
					}
				}, 100L);
				
				pdata1.addTrigger(ID, Trigger.PRE_RECEIVE_DAMAGE, (pdata2, in2) -> {
					if (!active) return TriggerResult.remove();
					active = false;
					ReceiveDamageEvent ev = (ReceiveDamageEvent) in2;
					DamageMeta dm = new DamageMeta(pdata1, damage, DamageType.PIERCING, DamageStatTracker.of(id + slot, eq));
					FightInstance.dealDamage(dm, ev.getDamager().getEntity());
					Sounds.teleport.play(p, p);
					p.teleport(loc);
					return TriggerResult.of(true, true);
				});
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, drop a marker on the ground. It stays active <white>5</white> seconds."
				+ " If you take damage while it's active, negate the damage, deal " + GlossaryTag.PIERCING.tag(this, damage, true) +
				" damage to the attacker, teleport to the marker, and deactivate it.");
	}
}
