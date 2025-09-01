package me.neoblade298.neorogue.equipment.abilities;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class HerosLanding extends Equipment {
	private static final String ID = "herosLanding";
	private static final TargetProperties tp = TargetProperties.radius(4, true);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			expl = new ParticleContainer(Particle.EXPLOSION).count(20).spread(tp.range / 2, 0.5);
	private static final Circle circ = new Circle(tp.range);
	private int str, damage, conc;
	private static final ParticleContainer strPart = new ParticleContainer(Particle.FLAME).count(25).spread(0.5, 0.5).offsetY(1).speed(0.1);
	
	public HerosLanding(boolean isUpgraded) {
		super(ID, "Hero's Landing", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 8, 0, tp.range));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			p.setVelocity(new Vector(0, 1, 0));
			Sounds.jump.play(p, p);
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.FALL_DAMAGE, (pdata, in) -> {
			strPart.play(p, p);
			Sounds.fire.play(p, p);
			data.applyStatus(StatusType.STRENGTH, data, str, -1);
			
			Location loc = p.getLocation();
			circ.play(pc, loc, LocalAxes.xz(), null);
			expl.play(p, loc);
			Sounds.explode.play(p, loc);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightInstance.dealDamage(
						new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, this)), ent);
				FightInstance.applyStatus(ent, StatusType.CONCUSSED, data, conc, -1);
			}
			
			return TriggerResult.of(false, true);
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ANVIL,
				"On cast, jump into the air. Passively, whenever you take fall damage, negate it, deal " + GlossaryTag.BLUNT.tag(this, damage, true) + " damage and apply " + 
				GlossaryTag.CONCUSSED.tag(this, conc, true) + " to nearby enemies, and gain " + GlossaryTag.STRENGTH.tag(this, str, true) + ".");
	}
}
