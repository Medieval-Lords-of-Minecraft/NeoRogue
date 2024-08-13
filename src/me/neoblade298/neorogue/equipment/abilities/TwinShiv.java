package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class TwinShiv extends Equipment {
	private static final String ID = "twinShiv";
	private int damage, bonus;
	private static final ParticleContainer tick = new ParticleContainer(Particle.CRIT).count(3).speed(0.01).spread(0.1, 0.1);
	
	public TwinShiv(boolean isUpgraded) {
		super(ID, "twinShiv", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
	}
	
	@Override
	public void setupReforges() {
		addSelfReforge(Brace2.get(), Bide.get(), Parry.get());
		addReforge(Provoke.get(), Challenge.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new TwinShivInstance(p, this, slot, es));
	}
	
	private class TwinShivInstance extends EquipmentInstance {
		private UUID firstHit;
		private boolean isFirstProj = true;

		public TwinShivInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);

			ProjectileGroup proj = new ProjectileGroup(new TwinShivProjectile(p, this));
			action = (pdata, in) -> {
				if (firstHit == null) {
					isFirstProj = true;
					this.setCooldown(1);
					this.setTempStaminaCost(0);
					this.setTempManaCost(0);
					proj.start(pdata);
				}
				else {
					isFirstProj = false;
					proj.start(pdata);
				}
				return TriggerResult.keep();
			};
		}
		
	}
	
	private class TwinShivProjectile extends Projectile {
		private TwinShivInstance inst;
		private Player p;

		public TwinShivProjectile(Player p, TwinShivInstance inst) {
			super(0.5, 10, 3);
			this.size(0.5, 0.5);
			this.p = p;
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			double finalDmg = 0;
			if (inst.isFirstProj) {
				finalDmg = damage;
			}
			else {
				finalDmg = damage + (inst.firstHit.equals(hit.getUniqueId()) ? bonus : 0);
				Sounds.anvil.play(p, hit.getEntity());
			}
			DamageMeta dm = new DamageMeta(proj.getOwner(), finalDmg, DamageType.PIERCING);
			weaponDamageProjectile(hit.getEntity(), proj, dm, hitBarrier);
			Sounds.breaks.play(p, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.attackSweep.play(p, p);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"On cast, fire a projectile that deals " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage. Recast to fire " +
				"a second projectile that does the same. If both hit the same target, deal " + GlossaryTag.PIERCING.tag(this, bonus, true)
				+ " additional damage.");
	}
}
