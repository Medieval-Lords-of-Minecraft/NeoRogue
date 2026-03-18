package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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

public class SteadyBleed extends Equipment {
	private static final String ID = "SteadyBleed";
	private static final TargetProperties tp = TargetProperties.radius(7, false, TargetType.ENEMY);
	private double damageMult, shieldPercent;
	private static final ParticleContainer bleed = new ParticleContainer(Particle.DAMAGE_INDICATOR)
			.count(50).spread(0.5, 0.5).offsetY(1);
	private static final ParticleContainer shield = new ParticleContainer(Particle.WAX_OFF)
			.count(30).spread(0.5, 0.5).offsetY(1);
	
	public SteadyBleed(boolean isUpgraded) {
		super(ID, "Steady Bleed", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 12, tp.range));
		damageMult = isUpgraded ? 2.0 : 1.0;
		shieldPercent = isUpgraded ? 0.4 : 0.2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Player p = data.getPlayer();
			int totalShields = 0;
			
			// Process all enemies in radius
			for (LivingEntity trg : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightData fd = FightInstance.getFightData(trg);
				if (fd == null) continue;
				
				// Get current rend stacks
				int rendStacks = fd.getStatus(StatusType.REND).getStacks();
				if (rendStacks <= 0) continue;
				
				// Calculate damage based on rend stacks
				double damage = rendStacks * damageMult;
				
				// Deal damage
				bleed.play(p, trg.getLocation());
				Sounds.attackSweep.play(p, trg.getLocation());
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.SLASHING, 
						DamageStatTracker.of(id + slot, this)), trg);
				
				// Accumulate shields based on damage dealt
				totalShields += (int) (damage * shieldPercent);
				
				// Remove all rend stacks from target
				fd.removeStatus(StatusType.REND);
			}
			
			// Grant accumulated shields
			if (totalShields > 0) {
				shield.play(p, p.getLocation());
				data.addSimpleShield(p.getUniqueId(), totalShields, 300); // 15 seconds
			}
			
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DAMAGED_ANVIL,
				"On cast, deal " + DescUtil.yellow((int)(damageMult * 100) + "%") + " of each enemy's current " +
				GlossaryTag.REND.tag(this) + " stacks as " + GlossaryTag.SLASHING.tag(this) + " damage to all nearby enemies," +
				" gain " + GlossaryTag.SHIELDS.tag(this) + " equal to " + 
				DescUtil.yellow((int)(shieldPercent * 100) + "%") + " of the damage dealt [<white>15s</white>], " +
				"and remove all their " + GlossaryTag.REND.tag(this) + " stacks.");
	}
}
