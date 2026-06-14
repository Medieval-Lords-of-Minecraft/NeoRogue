package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.Marker;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class Scavenger extends Equipment implements Power {
	private static final String ID = "Scavenger";
	private static final ParticleContainer stackParticle = new ParticleContainer(Particle.END_ROD)
			.count(30).spread(0.2, 0.2).offsetY(0.5);
	private static final ParticleContainer collectParticle = new ParticleContainer(Particle.HAPPY_VILLAGER)
			.count(30).spread(0.5, 0.5).offsetY(1);
	
	private int stamina;
	private double damageBuff;
	
	public Scavenger(boolean isUpgraded) {
		super(ID, "Scavenger", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		stamina = isUpgraded ? 25 : 15;
		damageBuff = isUpgraded ? 0.04 : 0.02;
	}

	@Override
	public void setupReforges() {
		addReforge(Saboteur.get(), TrappersEssence.get(), HuntersEssence.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}
	
	private class ScavengerStack extends Marker {
		private Player player;
		private int slot;
		private int staminaReward;
		private double damageReward;
		private Equipment eq;
		private EquipmentInstance inst;
		private ItemStack icon;
		
		public ScavengerStack(PlayerFightData owner, Location loc, Player p, int slot, int stamina, double damage, Equipment eq, EquipmentInstance inst, ItemStack icon) {
			super(owner, loc, 200); // 10 seconds duration
			this.player = p;
			this.slot = slot;
			this.staminaReward = stamina;
			this.damageReward = damage;
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
			owner.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					new Buff(owner, 0, damageReward, StatTracker.damageBuffAlly(id + slot, eq, true)));
			
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
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.KILL, (pdata2, in2) -> {
					Player p2 = data.getPlayer();
					KillEvent ev = (KillEvent) in2;
					Location deathLoc = ev.getTarget().getLocation();
					data.addMarker(new ScavengerStack(data, deathLoc, p2, slot, stamina, damageBuff, Scavenger.this, inst, icon));
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.WHEAT,
				GlossaryTag.POWER.tag(this) + ". Activates after killing an enemy. When you kill an enemy, they drop a stack. Standing on stacks collects them. " +
				"Each stack grants you " + DescUtil.yellow(stamina) + " stamina and " + 
				DescUtil.yellow((int)(damageBuff * 100) + "%") + " general damage.");
	}
}
