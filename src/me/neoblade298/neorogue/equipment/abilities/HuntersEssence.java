package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.Marker;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class HuntersEssence extends Equipment {
	private static final String ID = "HuntersEssence";
	private static final ParticleContainer stackParticle = new ParticleContainer(Particle.ENCHANT)
			.count(30).spread(0.2, 0.2).offsetY(0.5);
	private static final ParticleContainer collectParticle = new ParticleContainer(Particle.ENCHANTED_HIT)
			.count(30).spread(0.5, 0.5).offsetY(1);
	
	private int stamina;
	private double damageBuff;
	private double focusChance;
	
	public HuntersEssence(boolean isUpgraded) {
		super(ID, "Hunter's Essence", isUpgraded, Rarity.EPIC, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		stamina = isUpgraded ? 30 : 20;
		damageBuff = isUpgraded ? 0.10 : 0.05;
		focusChance = isUpgraded ? 0.60 : 0.30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// When player kills an enemy, drop a collectable stack at their location
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			Player p = data.getPlayer();
			KillEvent ev = (KillEvent) in;
			Location deathLoc = ev.getTarget().getLocation();
			
			// Create a collectible marker at death location
			data.addMarker(new HuntersEssenceStack(data, deathLoc, p, stamina, damageBuff, focusChance, this, inst, icon));
			
			return TriggerResult.keep();
		});
	}
	
	private class HuntersEssenceStack extends Marker {
		private Player player;
		private int staminaReward;
		private double damageReward;
		private double focusChance;
		private Equipment eq;
		private EquipmentInstance inst;
		private ItemStack icon;
		
		public HuntersEssenceStack(PlayerFightData owner, Location loc, Player p, int stamina, double damage, 
				double focusChance, Equipment eq, EquipmentInstance inst, ItemStack icon) {
			super(owner, loc, 200); // 10 seconds duration
			this.player = p;
			this.staminaReward = stamina;
			this.damageReward = damage;
			this.focusChance = focusChance;
			this.eq = eq;
			this.inst = inst;
			this.icon = icon;
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
					new Buff(owner, 0, damageReward, StatTracker.damageBuffAlly(buffId, eq, true)));
			
			// Chance to increase focus by 1
			if (Math.random() < focusChance) {
				owner.applyStatus(StatusType.FOCUS, owner, 1, -1);
			}
			
			// Increment icon count
			ItemStack newIcon = icon.clone();
			newIcon.setAmount(newIcon.getAmount() + 1);
			inst.setIcon(newIcon);
			
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
		item = createItem(Material.ENDER_EYE,
				"Passive. When you kill an enemy, they drop a stack. Standing on stacks collects them. " +
				"Each stack grants you " + DescUtil.yellow(stamina) + " stamina, " + 
				DescUtil.yellow((int)(damageBuff * 100) + "%") + " general damage permanently, and has a " +
				DescUtil.yellow((int)(focusChance * 100) + "%") + " chance to increase " + GlossaryTag.FOCUS.tag(this) + " by <white>1</white>.");
	}
}
