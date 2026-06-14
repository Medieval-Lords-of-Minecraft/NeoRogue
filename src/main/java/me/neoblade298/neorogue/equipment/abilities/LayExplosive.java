package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LayExplosive extends Equipment {
	private static final String ID = "LayExplosive";
	private static TargetProperties tp = TargetProperties.radius(3, false, TargetType.ENEMY);
	private static ParticleContainer trap = new ParticleContainer(Particle.SMOKE).count(50).spread(1, 0.2),
		exp = new ParticleContainer(Particle.EXPLOSION).count(50).spread(1.5, 1);
	private int damage;
	
	public LayExplosive(boolean isUpgraded) {
		super(ID, "Lay Explosive", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 5, 12, tp.range));
		
		damage = isUpgraded ? 90 : 70;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new LayExplosiveInstance(data, this, slot, es));
	}


	private class LayExplosiveInstance extends EquipmentInstance {
		private int ticks;
		private Location loc;
		private boolean active = false;
		private Trap tr;
		
		public LayExplosiveInstance(PlayerFightData data, Equipment e, int slot, EquipSlot es) {
			super(data, e, slot, es);
			action = (pd, in) -> {
				if (!active) {
					cast(data);
					this.setCooldown(0);
				}
				else {
					recast(data);
				}
				return TriggerResult.keep();
			};
		}

		private void cast(PlayerFightData data) {
			active = true;
			LayExplosiveInstance inst = this;
			data.charge(20).then(new Runnable() {
				public void run() {
					Player p = data.getPlayer();
					Sounds.equip.play(p, p);
					loc = p.getLocation();
					tr = new Trap(data, loc, -1) {
						@Override
						public void tick() {
							trap.play(data.getPlayer(), loc);
							if (inst.ticks < 5) {
								inst.ticks++;
							}
						}
					};
					data.addTrap(tr);
				}
			});
		}

		private void recast(PlayerFightData data) {
			if (ticks == 0) return;
			active = false;
			Player p = data.getPlayer();
			Sounds.explode.play(p, loc);
			exp.play(p, loc);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage * ticks, DamageType.BLUNT, DamageStatTracker.of(id + slot, eq), DamageOrigin.TRAP), ent);
			}
			data.removeTrap(tr);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OAK_TRAPDOOR,
				"On cast, " + DescUtil.charge(this, 1, 1) + ". Then drop a " + GlossaryTag.TRAP.tag(this) + " that explodes on recast, dealing " +
				GlossaryTag.BLUNT.tag(this, damage, true) + " damage multiplied by how many seconds it's been active, up to " + DescUtil.white("5s") + "," +
				" to all nearby enemies.");
	}
}
