package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Marker;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SelfDestruct extends Equipment {
	private static final String ID = "SelfDestruct";
	private int damage;
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.EXPLOSION).count(5).spread((double) tp.range / 2, 0);
	
	public SelfDestruct(boolean isUpgraded) {
		super(ID, "Self-Destruct", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none().add(PropertyType.AREA_OF_EFFECT, tp.range));
		damage = isUpgraded ? 120 : 90;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEACTIVATE_TRAP, (pdata, in) -> {
			Player p = data.getPlayer();
			Marker t = (Marker) in;
			Location loc = t.getLocation();
			pc.play(p, loc);
			Sounds.explode.play(p, loc);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
				FightInstance.dealDamage(data, DamageType.FIRE, damage, ent, DamageStatTracker.of(id + slot, this));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CAMPFIRE,
				"Passive. Any time a " + GlossaryTag.TRAP.tag(this) + " is removed, " +
				"including removal on activation, deal " + GlossaryTag.FIRE.tag(this, damage, true) + " to all enemies near the trap.");
	}
}
