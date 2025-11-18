package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
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
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class DarkLance extends Equipment {
	private static final String ID = "DarkLance";
	private static final ParticleContainer placePart = new ParticleContainer(Particle.CLOUD).count(10).spread(0.1, 0.1),
			smoke = new ParticleContainer(Particle.CLOUD).count(50).spread(1.5, 1.5).offsetY(1.5),
			smokeEdge = new ParticleContainer(Particle.CLOUD).count(2).spread(0.1, 0),
			explode = new ParticleContainer(Particle.DUST).count(150).spread(1.5, 2).dustOptions(new DustOptions(Color.BLACK, 1F));
	private static final Circle circ = new Circle(3);
	private static final SoundContainer place = new SoundContainer(Sound.ENTITY_CREEPER_PRIMED);
	private static final TargetProperties tp = TargetProperties.radius(3, true, TargetType.ENEMY);
	
	private int damage;
	
	public DarkLance(boolean isUpgraded) {
		super(ID, "Dark Lance", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 8, 0));
		damage = isUpgraded ? 400 : 300;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			placePart.play(p, p);
			place.play(p, p);
			Location loc = p.getLocation();

			data.addTask(new BukkitRunnable() {
				int tick = 0;
				public void run() {
					smoke.play(p, loc);
					circ.play(smokeEdge, loc, LocalAxes.xz(), null);
					if (++tick >= 2) this.cancel();
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
			
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.explode.play(p, loc);
					explode.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
						FightInstance.dealDamage(pdata, DamageType.DARK, damage, ent, DamageStatTracker.of(id + slot, eq));
					}
				}
			}.runTaskLater(NeoRogue.inst(), 40L));
			return TriggerResult.keep();
		}));
	}

	// add effects
	@Override
	public void setupItem() {
		item = createItem(Material.BLACK_DYE,
				"On cast, drop a bomb that detonates after <white>2s</white> and deals " + GlossaryTag.DARK.tag(this, damage, true) + " damage to enemies in the area.");
	}
}
