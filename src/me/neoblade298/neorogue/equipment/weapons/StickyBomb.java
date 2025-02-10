package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;

public class StickyBomb extends Ammunition {
	private static final String ID = "stickyBomb";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private ParticleContainer explode;
	private TargetProperties tp;
	private int damage;
	
	public StickyBomb(boolean isUpgraded) {
		super(ID, "Sticky Bomb", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(1, 0.1, DamageType.FIRE).add(PropertyType.AREA_OF_EFFECT, isUpgraded ? 4 : 2));
		tp = TargetProperties.radius(properties.get(PropertyType.AREA_OF_EFFECT), false, TargetType.ENEMY);
		properties.addUpgrades(PropertyType.AREA_OF_EFFECT);
		explode = new ParticleContainer(Particle.EXPLOSION).count(10).spread(tp.range / 2, 0);
		damage = 70;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void onStart(ProjectileInstance inst) {
		inst.getVelocity().add(new Vector(0, 0.08, 0));
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		proj.getVelocity().add(new Vector(0, -0.002, 0));
		pc.play(p, proj.getLocation());
	}

	@Override
	public void onHitBlock(ProjectileInstance inst, Block b) {
		Location loc = b.getLocation();
		inst.getOwner().addTask(new BukkitRunnable() {
			public void run() {
				Player p = (Player) inst.getOwner().getEntity();
				Sounds.explode.play(p, loc);
				explode.play(p, loc);
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(inst.getOwner().getEntity(), loc, tp)) {
					FightInstance.dealDamage(new DamageMeta(inst.getOwner(), damage, DamageType.FIRE, DamageOrigin.PROJECTILE), ent);
				}
			}
		}.runTaskLater(NeoRogue.inst(), 20));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TIPPED_ARROW, "Launches in an arc. If this arrow hits a block instead of an enemy, instead deal " + GlossaryTag.FIRE.tag(this, damage, false) + " damage " +
		"(unaffected by bow) to all nearby enemies after <white>1s</white>.");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.RED);
		item.setItemMeta(pm);
	}
}
