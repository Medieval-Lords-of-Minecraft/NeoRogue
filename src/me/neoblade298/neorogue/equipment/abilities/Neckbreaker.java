package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Neckbreaker extends Equipment {
	private static final String ID = "Neckbreaker";
	private int damage, injury, shields, mult;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIT)
		.count(20).spread(0.5, 0.5);
	private static final ParticleContainer successPc = new ParticleContainer(Particle.SOUL_FIRE_FLAME)
		.count(50).spread(1.0, 1.0);
	
	public Neckbreaker(boolean isUpgraded) {
		super(ID, "Neckbreaker", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 15, 10, 0));
		damage = isUpgraded ? 150 : 100;
		injury = isUpgraded ? 150 : 100;
		shields = isUpgraded ? 200 : 150;
		mult = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		UUID[] firstTargetId = new UUID[1]; // Track first target
		boolean[] allSameTarget = new boolean[1]; // Track if all attacks hit same target
		
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			pc.play(p, p);
			inst.addCount(3);
			firstTargetId[0] = null;
			allSameTarget[0] = true;
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, bind, inst);
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (inst.getCount() <= 0) return TriggerResult.keep();
			
			Player p = data.getPlayer();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			LivingEntity target = ev.getTarget();
			
			// Add bonus damage
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, 
				DamageStatTracker.of(ID + slot, this)));
			
			// Apply injury
			FightInstance.applyStatus(target, StatusType.INJURY, data, injury, -1);
			
			// Track target
			if (firstTargetId[0] == null) {
				// First attack
				firstTargetId[0] = target.getUniqueId();
			} else if (!firstTargetId[0].equals(target.getUniqueId())) {
				// Hit a different target
				allSameTarget[0] = false;
			}
			
			inst.addCount(-1);
			Sounds.anvil.play(p, p);
			
			// Check if all 3 attacks are done
			if (inst.getCount() == 0 && allSameTarget[0]) {
				// All 3 attacks hit the same target
				Player player = data.getPlayer();
				FightData fd = FightInstance.getFightData(target);
				
				// Get current injury stacks
				int currentInjury = fd.getStatus(StatusType.INJURY).getStacks();
				
				// Grant shields
				data.addSimpleShield(player.getUniqueId(), shields, 100);
				
				// Apply multiplied injury
				FightInstance.applyStatus(target, StatusType.INJURY, data, currentInjury * mult, -1);
				
				// Success effects
				Sounds.success.play(player, player);
				successPc.play(player, target.getLocation());
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BONE,
				"On cast, your next <white>3</white> basic attacks deal " + 
				GlossaryTag.PIERCING.tag(this, damage, true) + " bonus damage and apply " +
				GlossaryTag.INJURY.tag(this, injury, true) + ". If they all hit the same enemy, " +
				"gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>] and apply " +
				DescUtil.yellow(mult + "x") + " the enemy's current " + GlossaryTag.INJURY.tag(this) + ".");
	}
}
