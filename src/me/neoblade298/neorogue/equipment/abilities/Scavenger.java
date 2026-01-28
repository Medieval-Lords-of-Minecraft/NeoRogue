package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.Marker;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class Scavenger extends Equipment {
	private static final String ID = "Scavenger";
	private static final ParticleContainer stackParticle = new ParticleContainer(Particle.ENCHANT)
			.count(20).spread(0.5, 0.5).offsetY(0.5);
	private static final ParticleContainer collectParticle = new ParticleContainer(Particle.HAPPY_VILLAGER)
			.count(30).spread(0.5, 0.5).offsetY(1);
	
	private int stamina;
	private double damageBuff;
	
	public Scavenger(boolean isUpgraded) {
		super(ID, "Scavenger", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		stamina = isUpgraded ? 30 : 20;
		damageBuff = isUpgraded ? 0.05 : 0.03;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// When player kills an enemy, drop a collectable stack at their location
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			KillEvent ev = (KillEvent) in;
			Location deathLoc = ev.getTarget().getLocation();
			
			// Create a collectible marker at death location
			data.addMarker(new ScavengerStack(data, deathLoc, p, stamina, damageBuff, this));
			
			return TriggerResult.keep();
		});
	}
	
	private class ScavengerStack extends Marker {
		private Player player;
		private int staminaReward;
		private double damageReward;
		private Equipment eq;
		
		public ScavengerStack(PlayerFightData owner, Location loc, Player p, int stamina, double damage, Equipment eq) {
			super(owner, loc, 200); // 10 seconds duration
			this.player = p;
			this.staminaReward = stamina;
			this.damageReward = damage;
			this.eq = eq;
		}
		
		@Override
		public void tick() {
			// Play particle effect
			stackParticle.play(player, loc);
			
			// Check if player is close enough to collect (1 block radius)
			if (player.getLocation().distanceSquared(loc) <= 1) {
				collect();
			}
		}
		
		private void collect() {
			Sounds.equip.play(player, player);
			// Grant stamina
			owner.addStamina(staminaReward);
			
			// Grant damage buff
			String buffId = UUID.randomUUID().toString();
			owner.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					new Buff(owner, damageReward, 0, StatTracker.damageBuffAlly(buffId, eq)));
			
			// Visual/audio feedback
			collectParticle.play(player, loc);
			Sounds.levelup.play(player, player);
			
			// Remove the marker
			owner.removeMarker(this);
		}
		
		@Override
		public void onDeactivate() {
			// Optional: Play expiration effect if stack despawns without being collected
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WHEAT,
				"Passive. When you kill an enemy, they drop a stack. Standing on stacks collects them. " +
				"Each stack grants you " + DescUtil.yellow(stamina) + " stamina and " + 
				DescUtil.yellow((int)(damageBuff * 100) + "%") + " general damage, permanently.");
	}
}
