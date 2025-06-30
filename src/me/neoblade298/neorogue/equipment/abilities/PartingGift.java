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

public class PartingGift extends Equipment {
	private static final String ID = "partingGift";
	private static final ParticleContainer placePart = new ParticleContainer(Particle.CLOUD).count(10).spread(0.1, 0.1),
			smoke = new ParticleContainer(Particle.CLOUD).count(50).spread(2.5, 2.5).offsetY(1.5),
			smokeEdge = new ParticleContainer(Particle.CLOUD).count(2).spread(0.1, 0),
			explode = new ParticleContainer(Particle.EXPLOSION).count(25).spread(2.5, 0.1);
	private static final Circle circ = new Circle(5);
	private static final SoundContainer place = new SoundContainer(Sound.ENTITY_CREEPER_PRIMED);
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	
	private static final int MAX = 5;
	private int damage;
	
	public PartingGift(boolean isUpgraded) {
		super(ID, "Parting Gift", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 15, 0));
		damage = isUpgraded ? 60 : 40;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Farewell.get(), DarkPulse.get(), DarkLance.get());
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
				private int tick = 0;
				public void run() {
					if (p.getLocation().distanceSquared(loc) > tp.range * tp.range) {
						Sounds.explode.play(p, loc);
						explode.play(p, loc);
						for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
							FightInstance.dealDamage(pdata, DamageType.DARK, damage * tick, ent, DamageStatTracker.of(id + slot, eq));
						}
						this.cancel();
						return;
					}
					if (tick < MAX) tick++;
					smoke.play(p, loc);
					circ.play(smokeEdge, loc, LocalAxes.xz(), null);
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
			return TriggerResult.keep();
		}));
	}

	// add effects
	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"On cast, drop a bomb that detonates and deals " + GlossaryTag.DARK.tag(this) + " damage after you get out of range of it." +
				" For every second you stay in range, up to " + DescUtil.white(MAX + "s") + ", increase damage dealt by " + DescUtil.yellow(damage) + ".");
	}
}
