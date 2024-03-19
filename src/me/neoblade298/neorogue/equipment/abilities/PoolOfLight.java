package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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

public class PoolOfLight extends Equipment {
	private static final String ID = "poolOfLight";
	private int sanct;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORKS_SPARK).count(50).spread(2.5, 0.1);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.6F);
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	
	public PoolOfLight(boolean isUpgraded) {
		super(ID, "Empowered Edge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 10, 20, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		sanct = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			sc.play(p, p);
			Location loc = p.getLocation();
			data.addTask(new BukkitRunnable() {
				public void run() {
					pc.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
						FightInstance.applyStatus(ent, StatusType.SANCTIFIED, p, sanct, -1);
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"On cast, drop a radius <white>5</white> pool on the ground that lasts <white>10</white> seconds and grants "
				+ GlossaryTag.SANCTIFIED.tag(this, sanct, true) + " per second to all enemies that walk through it.");
	}
}
