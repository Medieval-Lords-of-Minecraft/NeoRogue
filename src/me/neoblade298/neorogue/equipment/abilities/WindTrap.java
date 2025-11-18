package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WindTrap extends Equipment {
	private static final String ID = "WindTrap";
	private static TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static ParticleContainer trap = new ParticleContainer(Particle.SWEEP_ATTACK).count(15).spread(2.5, 0.1);
	private int secs;
	
	public WindTrap(boolean isUpgraded) {
		super(ID, "Wind Trap", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 5, 15, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		secs = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Sounds.equip.play(p, p);
			Location loc = p.getLocation().add(0, 0.5, 0);
			data.addTrap(new Trap(data, loc, secs * 20) {
				@Override
				public void tick() {
					trap.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
						ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, secs * 20, 0));
					}
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WIND_CHARGE,
				"On cast, drop a " + GlossaryTag.TRAP.tag(this) + 
				" " + DescUtil.duration(secs, true) + ". Nearby enemies are given Slowness <white>1</white>.");
	}
}
