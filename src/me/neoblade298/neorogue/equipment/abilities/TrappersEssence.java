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
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class TrappersEssence extends Equipment {
	private static final String ID = "TrappersEssence";
	private static final int TRAP_DURATION = 200; // 10 seconds
	private static final int SHIELD_DURATION = 100; // 5 seconds
	private static final TargetProperties tp = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static final ParticleContainer trapParticle = new ParticleContainer(Particle.CRIT)
			.count(30).spread(0.5, 0.2);
	private static final ParticleContainer hitParticle = new ParticleContainer(Particle.EXPLOSION)
			.count(50).spread(1, 1);
	
	private int damage;
	private int shields;
	
	public TrappersEssence(boolean isUpgraded) {
		super(ID, "Trapper's Essence", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		damage = isUpgraded ? 400 : 300;
		shields = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// When player kills an enemy, drop a trap at their location
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			KillEvent ev = (KillEvent) in;
			Location deathLoc = ev.getTarget().getLocation();
			
			// Create a trap at death location
			initTrap(deathLoc, data, this, slot);
			
			return TriggerResult.keep();
		});
	}
	
	private void initTrap(Location loc, PlayerFightData data, Equipment eq, int slot) {
		data.addTrap(new Trap(data, loc, TRAP_DURATION) {
			@Override
			public void tick() {
				Player p = data.getPlayer();
				trapParticle.play(p, loc);
				LivingEntity trg = TargetHelper.getNearest(p, loc, tp);
				if (trg != null) {
					Sounds.breaks.play(p, trg);
					hitParticle.play(p, trg);
					
					// Deal trap damage
					DamageMeta dm = new DamageMeta(data, damage, DamageType.BLUNT, 
							DamageStatTracker.of(ID + slot, eq), DamageOrigin.TRAP);
					FightInstance.dealDamage(dm, trg);
					
					// Grant shields to player
					data.addSimpleShield(p.getUniqueId(), shields, SHIELD_DURATION);
					Sounds.equip.play(p, p);
					
					data.removeTrap(this);
				}
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_TRAPDOOR,
				"Passive. When you kill an enemy, they drop a " + GlossaryTag.TRAP.tag(this) + " [<white>10s</white>]. " +
				"When triggered, the trap deals " + GlossaryTag.BLUNT.tag(this, damage, true) + " damage " +
				"and grants you " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>].");
	}
}
