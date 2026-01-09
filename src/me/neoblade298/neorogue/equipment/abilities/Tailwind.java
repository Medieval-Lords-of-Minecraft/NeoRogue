package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Tailwind extends Equipment {
	private static final String ID = "Tailwind";
	private static final int radius = 6;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final Circle circ = new Circle(radius);
	private int shields;
	
	public Tailwind(boolean isUpgraded) {
		super(ID, "Tailwind", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(isUpgraded ? 15 : 20, 5, isUpgraded ? 17 : 20, 0).add(PropertyType.AREA_OF_EFFECT, radius));
		properties.addUpgrades(PropertyType.MANA_COST, PropertyType.COOLDOWN);
		shields = isUpgraded ? 3 : 2;
	}
	
	@Override
	public void setupReforges() {

	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			data.addTask(new BukkitRunnable() {
				Location loc = p.getLocation();
				int tick = 0;
				public void run() {
					circ.play(pc, loc, LocalAxes.xz(), null);
					if (loc.distanceSquared(p.getLocation()) <= radius * radius) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 0));
						data.addSimpleShield(p.getUniqueId(), shields, 20);
					}
					if (++tick >= 8) {
						this.cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0, 20));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"On cast, place a circle [<white>8s</white>] that grants you <white>Speed 1</white> and " +
				GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>1s</white>] while you're in it.");
	}
}
