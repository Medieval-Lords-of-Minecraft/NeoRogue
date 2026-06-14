package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DyingStar extends Equipment implements Power {
	private static final String ID = "DyingStar";
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private static final ParticleContainer pullPart = new ParticleContainer(Particle.SOUL)
		.count(1).spread(0, 0);
	private static final ParticleContainer explodeBurst = new ParticleContainer(Particle.EXPLOSION)
		.count(5).spread(1.5, 0.1);
	private static final ParticleContainer explodeEdge = new ParticleContainer(Particle.SOUL)
		.count(1).spread(0, 0);
	private static final SoundContainer pullSound = new SoundContainer(Sound.BLOCK_PORTAL_TRIGGER);
	private static final ParticleAnimation pullAnim;

	static {
		pullAnim = new ParticleAnimation(pullPart, (loc, tick) -> {
			LinkedList<Location> locs = new LinkedList<>();
			double radius = tp.range * (1.0 - (double) tick / 15.0);
			if (radius < 0.3) radius = 0.3;
			int points = 16;
			for (int i = 0; i < points; i++) {
				double angle = 2 * Math.PI * i / points;
				locs.add(loc.clone().add(Math.cos(angle) * radius, 0.5, Math.sin(angle) * radius));
			}
			return locs;
		}, 15);
	}

	private int damage;
	
	public DyingStar(boolean isUpgraded) {
		super(ID, "Dying Star", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		damage = isUpgraded ? 375 : 250;
		properties.add(PropertyType.DAMAGE, damage);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		// Create a rift on activation (delayed to avoid ConcurrentModificationException
		// from re-entering CREATE_RIFT triggers while the list is being iterated)
		new BukkitRunnable() {
			public void run() {
				data.addRift(new Rift(data, data.getPlayer().getLocation(), 100));
			}
		}.runTaskLater(NeoRogue.inst(), 1L);

		// Handle rift expiration - pull enemies and explode
		data.addTrigger(id, Trigger.REMOVE_RIFT, (pdata2, in2) -> {
			Player p2 = data.getPlayer();
			Rift rift = (Rift) in2;
			Location riftLoc = rift.getLocation();

			// Pull enemies toward rift
			pullSound.play(p2, riftLoc);
			data.runAnimation(id, p2, pullAnim, riftLoc);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p2, riftLoc, tp)) {
				Vector direction = riftLoc.toVector().subtract(ent.getLocation().toVector());
				if (!direction.isZero()) {
					direction = direction.normalize().setY(0.3);
					ent.setVelocity(direction);
				}
			}

			// Explode 1 second later
			data.addTask(new BukkitRunnable() {
				public void run() {
					Player p3 = data.getPlayer();
					Sounds.explode.play(p3, riftLoc);
					circ.play(explodeEdge, riftLoc, LocalAxes.xz(), null);
					explodeBurst.play(p3, riftLoc);

					boolean riftCreated = false;
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p3, riftLoc, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK,
							DamageStatTracker.of(id + slot, DyingStar.this), DamageOrigin.RIFT), ent);
						if (!riftCreated && ent.getHealth() <= 0) {
							riftCreated = true;
							Sounds.equip.play(p3, riftLoc);
							data.addRift(new Rift(data, riftLoc.clone(), 200));
						}
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20L));

			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				GlossaryTag.POWER.tag(this) + ". Activates after creating " + DescUtil.white(2) + " " + GlossaryTag.RIFT.tagPlural(this) + ". Create a " + GlossaryTag.RIFT.tag(this) + ". Afterwards, when any " + GlossaryTag.RIFT.tag(this) +
				" expires, pull in nearby enemies, then explode " + DescUtil.white("1s") + " later dealing " +
				GlossaryTag.DARK.tag(this, damage, true) + " damage. If an enemy is killed, spawn a new " +
				GlossaryTag.RIFT.tag(this) + " [" + DescUtil.white("10s") + "] in the same place (one per explosion max).");
	}

	private static final int ACTIVATION_THRES = 2;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		int[] riftCount = {0};
		data.addTrigger(id, Trigger.CREATE_RIFT, (pdata, in) -> {
			riftCount[0]++;
			if (riftCount[0] < ACTIVATION_THRES) return TriggerResult.keep();
			if (riftCount[0] > ACTIVATION_THRES) return TriggerResult.remove();
			System.out.println(riftCount[0]);

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}
}
