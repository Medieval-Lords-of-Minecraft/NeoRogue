package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Stormspike extends Equipment {
	private static final String ID = "Stormspike";
	private int damage, basicLightning, shields, electrified;
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.fromRGB(135, 206, 250), 1F));
	
	public Stormspike(boolean isUpgraded) {
		super(ID, "Stormspike", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(30, 15, 8, 8));
		damage = isUpgraded ? 350 : 250;
		basicLightning = isUpgraded ? 100 : 75;
        electrified = isUpgraded ? 90 : 60;
		shields = isUpgraded ? 25 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup();
		ActionMeta am = new ActionMeta();
		
		// Create 3 projectiles in a cone spread
		for (int angle : new int[] { -30, 0, 30 }) {
			proj.add(new StormspikeProjectile(data, angle, this, slot, am));
		}
		
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.attackSweep.play(p, p);
			am.setCount(0); // Reset hit counter
			proj.start(data);
			return TriggerResult.keep();
		}));
	}
	
	private class StormspikeProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private ActionMeta am;

		public StormspikeProjectile(PlayerFightData data, int angleOffset, Equipment eq, int slot, ActionMeta am) {
			super(1.5, properties.get(PropertyType.RANGE), 1);
			this.rotation(angleOffset);
			this.size(0.3, 0.3);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
			this.am = am;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Sounds.blazeDeath.play(p, hit.getEntity().getLocation());
			am.addCount(1);
			
			// If we hit 2 enemies, grant buffs
			if (am.getCount() == 2) {
				// Grant shields
				data.addSimpleShield(p.getUniqueId(), shields, 120);
				
				// Track when empowerment started
				long startTime = System.currentTimeMillis();
				
				// Add lightning damage and electrified on basic attack for 6 seconds
				String triggerId = id + slot + "-empowered";
				data.addTrigger(triggerId, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
					// Check if 6 seconds have passed
					if (System.currentTimeMillis() - startTime >= 6000) {
						return TriggerResult.remove();
					}
					
					PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
					ev.getMeta().addDamageSlice(new DamageSlice(data, basicLightning, DamageType.LIGHTNING,
						DamageStatTracker.of(triggerId, eq)));
					Sounds.firework.play(p, p);
					FightInstance.applyStatus(hit.getEntity(), StatusType.ELECTRIFIED, data, electrified, -1);
					return TriggerResult.keep();
				});
				
				Sounds.success.play(p, p);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.LIGHTNING, 
				DamageStatTracker.of(id + slot, eq)));
		}
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Blackspike.get(), Mastermind.get());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
			"On cast, throw <white>3</white> projectiles in a cone that each deal " + 
			GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage. If you hit at least <white>2</white> enemies, " +
			"gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>6s</white>] and basic attacks deal " +
			GlossaryTag.LIGHTNING.tag(this, basicLightning, true) + " damage and apply " + 
			GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + " [<white>5s</white>] for <white>6s</white>.");
	}
}
