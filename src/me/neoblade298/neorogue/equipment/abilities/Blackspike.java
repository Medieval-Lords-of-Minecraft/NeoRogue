package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Blackspike extends Equipment {
	private static final String ID = "Blackspike";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST)
		.dustOptions(new DustOptions(Color.BLACK, 1F));
	
	public Blackspike(boolean isUpgraded) {
		super(ID, "Blackspike", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(25, 10, 8, 8));
		damage = isUpgraded ? 300 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup();
		// Create 3 projectiles in a cone spread
		for (int angle : new int[] { -15, 0, 15 }) {
			proj.add(new BlackspikeProjectile(data, angle, this, slot));
		}
		
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.attackSweep.play(p, p);
			proj.start(data);
			return TriggerResult.keep();
		}));
	}
	
	private class BlackspikeProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;

		public BlackspikeProjectile(PlayerFightData data, int angleOffset, Equipment eq, int slot) {
			super(1.5, properties.get(PropertyType.RANGE), 1);
			this.rotation(angleOffset);
			this.size(0.3, 0.3);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Sounds.extinguish.play(p, hit.getEntity().getLocation());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, 
				DamageStatTracker.of(id + slot, eq)));
		}
	}

	@Override
	public void setupReforges() {
		addReforge(Obfuscation.get(), BlackRain.get());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SCRAP,
			"On cast, throw <white>3</white> projectiles in a cone that each deal " + 
			GlossaryTag.DARK.tag(this, damage, true) + " damage.");
	}
}