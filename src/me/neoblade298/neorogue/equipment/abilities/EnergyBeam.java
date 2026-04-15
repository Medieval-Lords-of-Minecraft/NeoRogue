package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class EnergyBeam extends Equipment {
	private static final String ID = "EnergyBeam";
	private static final TargetProperties tp = TargetProperties.line(14, 2, TargetType.ENEMY);
	private static final ParticleContainer beam = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(25).spread(0.2, 0.2).speed(0.15);
	private int damage;
	private int manaCostReduction;
	
	public EnergyBeam(boolean isUpgraded) {
		super(ID, "Energy Beam", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 30, 8, tp.range));
		damage = isUpgraded ? 100 : 70;
		manaCostReduction = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta nextAbilityFreeCost = new ActionMeta();
		Equipment eq = this;
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// On cast, fire beam toward nearest enemy
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			LivingEntity target = TargetHelper.getNearestInSight(p, tp);
			
			if (target != null) {
				Location start = p.getEyeLocation();
				Vector dir = p.getEyeLocation().getDirection();
				Location end = start.clone().add(dir.clone().multiply(properties.get(PropertyType.RANGE)));
				
				// Draw beam
				ParticleUtil.drawLine(p, beam, start, end, 0.3);
				Sounds.thunder.play(p, p);
				
				// Check all entities in line
				for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
					FightData targetData = FightInstance.getFightData(ent.getUniqueId());
					
					// Deal damage
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING, 
							DamageStatTracker.of(id + slot, eq)), ent);
					
					// If target has Electrified, flag for mana cost reduction
					if (targetData != null && targetData.hasStatus(StatusType.ELECTRIFIED)) {
						nextAbilityFreeCost.setBool(true);
					}
				}
			}
			return TriggerResult.keep();
		});
		
		// Hook on PRE_CAST_USABLE to reduce mana cost if flag is set
		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			if (nextAbilityFreeCost.getBool()) {
				// Refund mana cost for next ability
				data.addMana(manaCostReduction);
				nextAbilityFreeCost.setBool(false);
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_CLUSTER,
				"On cast, fire a beam of lightning dealing " + GlossaryTag.LIGHTNING.tag(this, damage, true) + 
				" in a line in front of you. If the beam hits an " + GlossaryTag.ELECTRIFIED.tag(this) + 
				" enemy, the next ability you cast costs " + DescUtil.yellow(manaCostReduction) + " less mana.");
	}
}
