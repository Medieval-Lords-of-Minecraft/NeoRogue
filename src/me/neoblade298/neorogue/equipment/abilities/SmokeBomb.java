package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SmokeBomb extends Equipment {
	private static final String ID = "smokeBomb";
	private static final ParticleContainer placePart = new ParticleContainer(Particle.CLOUD).count(10).spread(0.1, 0.1),
			smoke = new ParticleContainer(Particle.CLOUD).count(50).spread(2.5, 2.5).offsetY(1.5),
			smokeEdge = new ParticleContainer(Particle.CLOUD).count(2).spread(0.1, 0);
	private static final Circle circ = new Circle(5);
	private static final SoundContainer place = new SoundContainer(Sound.ENTITY_CREEPER_PRIMED);
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	
	public SmokeBomb(boolean isUpgraded) {
		super(ID, "Smoke Bomb", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 10, 0));
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Darkness.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			placePart.play(p, p);
			place.play(p, p);
			Location loc = p.getLocation();
			
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.explode.play(p, loc);
					data.addTask(new BukkitRunnable() {
						public void run() {
							smoke.play(p, loc);
							circ.play(smokeEdge, loc, LocalAxes.xz(), null);
							if (p.getLocation().distanceSquared(loc) <= tp.range * tp.range) {
								data.applyStatus(StatusType.INVISIBLE, data, 1, 20);
							}
						}
					}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
				}
			}.runTaskLater(NeoRogue.inst(), 60L));
			return TriggerResult.keep();
		}));
	}

	// add effects
	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"On cast, drop a smoke bomb that detonates after <white>3</white> seconds. After detonation, for <white>5</white> seconds,"
				+ " standing within the radius grants " + GlossaryTag.INVISIBLE.tag(this, 1, false) + " [<white>1s</white].");
	}
}
