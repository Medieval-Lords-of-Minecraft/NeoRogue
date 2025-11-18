package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AcidBomb extends Equipment {
	private static final String ID = "AcidBomb";
	private static final ParticleContainer placePart = new ParticleContainer(Particle.CLOUD).count(10).spread(0.1, 0.1),
			smoke = new ParticleContainer(Particle.DUST).count(50).spread(2.5, 2.5).offsetY(1.5).dustOptions(new DustOptions(Color.LIME, 1F)),
			smokeEdge = new ParticleContainer(Particle.DUST).count(2).spread(0.1, 0).dustOptions(new DustOptions(Color.LIME, 1F));
	private static final Circle circ = new Circle(5);
	private static final SoundContainer place = new SoundContainer(Sound.ENTITY_CREEPER_PRIMED);
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	
	private int poison;
	
	public AcidBomb(boolean isUpgraded) {
		super(ID, "Acid Bomb", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 0, 12, 0));
		poison = isUpgraded ? 90 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			placePart.play(p, p);
			place.play(p, p);
			Location loc = p.getLocation();
			
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.explode.play(p, loc);
					data.addTask(new BukkitRunnable() {
						private static final int TICKS = 5;
						private int tick = 0;
						public void run() {
							smoke.play(p, loc);
							circ.play(smokeEdge, loc, LocalAxes.xz(), null);
							for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
								FightInstance.applyStatus(ent, StatusType.POISON, data, poison, 60);
							}
							
							if (++tick == TICKS) this.cancel();
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
		item = createItem(Material.POTION,
				"On cast, drop an acid bomb that detonates after <white>3</white> seconds. After detonation, every second for <white>5</white> seconds,"
				+ " enemies within the radius get " + GlossaryTag.POISON.tag(this, poison, false) + ".");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.LIME);
		item.setItemMeta(pm);
	}
}
