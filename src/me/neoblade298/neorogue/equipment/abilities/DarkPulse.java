package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class DarkPulse extends Equipment {
	private static final String ID = "darkPulse";
	private static final ParticleContainer placePart = new ParticleContainer(Particle.CLOUD).count(10).spread(0.1, 0.1),
			smoke = new ParticleContainer(Particle.CLOUD).count(50).spread(2.5, 2.5).offsetY(1.5),
			smokeEdge = new ParticleContainer(Particle.CLOUD).count(2).spread(0.1, 0);
	private static final Circle circ = new Circle(5);
	private static final SoundContainer place = new SoundContainer(Sound.ENTITY_CREEPER_PRIMED),
			pulseSound = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_HURT);
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	
	private int thres, pulse;
	
	public DarkPulse(boolean isUpgraded) {
		super(ID, "Dark Pulse", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 15, tp.range));
		thres = isUpgraded ? 80 : 100;
		pulse = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		DarkPulseInstance inst = new DarkPulseInstance(p, data, this, es, slot);
		data.addTrigger(id, bind, inst);

		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (!inst.active) return TriggerResult.keep();
			DealtDamageEvent ev = (DealtDamageEvent) in;
			inst.pulse(ev.getTotalDamage(), slot);
			return TriggerResult.keep();
		});
	}

	private class DarkPulseInstance extends EquipmentInstance {
		private Player p;
		private PlayerFightData data;
		private boolean active = false;
		private Location loc;
		private int dealt = 0;
		public DarkPulseInstance(Player p, PlayerFightData data, Equipment eq, EquipSlot es, int slot) {
			super(data, eq, slot, es);
			this.p = p;
			this.data = data;

			action = (pdata, in) -> {
				active = true;
				placePart.play(p, p);
				place.play(p, p);
				loc = p.getLocation();
				
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						if (p.getLocation().distanceSquared(loc) > tp.range * tp.range) {
							Sounds.extinguish.play(p, loc);
							active = false;
							this.cancel();
							return;
						}
						smoke.play(p, loc);
						circ.play(smokeEdge, loc, LocalAxes.xz(), null);
					}
				}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
				return TriggerResult.keep();
			};
		}

		public void pulse(double damage, int slot) {
			dealt += damage;
			double pulseDamage = 0;
			if (dealt >= thres) {
				while (dealt >= thres) {
					pulseDamage++;
					dealt -= thres;
				}
				pulseDamage *= damage;

				pulseSound.play(p, loc);
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
					FightInstance.dealDamage(data, DamageType.DARK, pulseDamage, ent, DamageStatTracker.of(id + slot, eq));
				}
			}
			
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SCRAP,
				"On cast, drop a marker that pulses and deals " + GlossaryTag.DARK.tag(this, pulse, true) + " damage for every "
				+ DescUtil.yellow(thres) + " damage you deal while in range of the marker. The marker deactivates after you leave range.");
	}
}
