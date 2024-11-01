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
import me.neoblade298.neorogue.session.fight.DamageSlice;
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
		super(ID, "Twin Shiv", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
		
		damage = isUpgraded ? 100 : 80;
		bonus = isUpgraded ? 80 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new TwinShivInstance(data, this, slot, es));
	}
	
	private class TwinShivInstance extends EquipmentInstance {
		public UUID firstHit;
		public boolean isFirstProj = true;

		public TwinShivInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);

			ProjectileGroup proj = new ProjectileGroup(new TwinShivProjectile(data, this));
			action = (pdata, in) -> {
				if (isFirstProj) {
					firstHit = null;
					this.setCooldown(1);
					this.setTempStaminaCost(0);
					this.setTempManaCost(0);
					proj.start(pdata);
					isFirstProj = false;
				}
				else {
					proj.start(pdata);
					isFirstProj = true;
				}
				return TriggerResult.keep();
			};
		}
		
	}
	
	private class TwinShivProjectile extends Projectile {
		private TwinShivInstance inst;
		private Player p;
		private PlayerFightData data;

		public TwinShivProjectile(PlayerFightData data, TwinShivInstance inst) {
			super(0.5, 10, 3);
			this.size(0.5, 0.5);
			this.data = data;
			this.p = data.getPlayer();
			this.inst = inst;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			if (Boolean.getBoolean(proj.getTag()) || inst.firstHit == null) {
				inst.firstHit = hit.getUniqueId();
			}
			else {
				if (inst.firstHit.equals(hit.getUniqueId())) {
					meta.addDamageSlice(new DamageSlice(data, bonus, DamageType.PIERCING));
					Sounds.anvil.play(p, hit.getEntity());
				}
			}
			Sounds.breaks.play(p, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyProperties(data, getProperties());
			Sounds.attackSweep.play(p, p);
			proj.setTag("" + inst.isFirstProj);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET,
				"On cast, fire a projectile that deals " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage. Recast to fire " +
				"a second projectile that does the same. If both hit the same target, deal " + GlossaryTag.PIERCING.tag(this, bonus, true)
				+ " additional damage.");
	}
}
