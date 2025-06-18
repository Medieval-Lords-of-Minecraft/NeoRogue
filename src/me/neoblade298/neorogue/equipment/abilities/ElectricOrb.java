package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ElectricOrb extends Equipment {
	private static final String ID = "electricOrb";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK);
	private static final SoundContainer shoot = new SoundContainer(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
	private static final TargetProperties tp = TargetProperties.radius(12, false);
	private int elec, damage;
	
	public ElectricOrb(boolean isUpgraded) {
		super(ID, "Electric Orb", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 0, 16, tp.range));
		elec = isUpgraded ? 50 : 30;
		damage = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup group = new ProjectileGroup(new ElectricOrbProjectile(data));
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			group.start(data);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD, "On cast, launch a projectile that applies " + DescUtil.potion("Slowness", 2, 1) + " and " + 
		GlossaryTag.ELECTRIFIED.tag(this, elec, true) + " to enemies it passes through, once per enemy, and deals " + 
		GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage in a line between you and the projectile every second.");
	}

	private class ElectricOrbProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private HashSet<UUID> hitEntities = new HashSet<UUID>();

		public ElectricOrbProjectile(PlayerFightData data) {
			super(0.3, tp.range, 2);
			this.data = data;
			this.p = data.getPlayer();
			this.ignore(false, false, true);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			pc.play(p, proj.getLocation());
			if (proj.getTick() % 10 == 0 && proj.getTick() > 0) {
				for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, p.getLocation().add(0, 1, 0), proj.getLocation(), tp)) {
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING), ent);
				}
			}
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			if (!hitEntities.contains(hit.getUniqueId())) return;

			hit.applyStatus(StatusType.ELECTRIFIED, data, elec, -1);
			hit.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 2));
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			shoot.play(p, p);
			hitEntities.clear();
		}
	}
}
