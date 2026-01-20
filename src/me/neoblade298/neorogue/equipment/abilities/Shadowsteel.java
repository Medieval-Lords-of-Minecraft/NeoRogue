package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Shadowsteel extends Equipment {
	private static final String ID = "Shadowsteel";
	private static final ParticleContainer shadowBall = new ParticleContainer(Particle.DUST)
		.dustOptions(new org.bukkit.Particle.DustOptions(Color.BLACK, 1.5F))
		.count(15).spread(0.3, 0.3);
	private static final ParticleContainer slashLine = new ParticleContainer(Particle.DUST)
		.dustOptions(new org.bukkit.Particle.DustOptions(Color.fromRGB(30, 30, 30), 1.2F))
		.count(3).spread(0.1, 0.1);
	private static TargetProperties tp = TargetProperties.line(20, 1, TargetType.ENEMY);
	
	private int damage;

	public Shadowsteel(boolean isUpgraded) {
		super(ID, "Shadowsteel", isUpgraded, Rarity.EPIC, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 1, 0));
		damage = isUpgraded ? 350 : 250;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		// Queue to track player positions - stores last 4 positions (2 seconds worth at 0.5s intervals)
		LinkedList<Location> locationQueue = new LinkedList<Location>();
		ActionMeta cooldown = new ActionMeta();
		
		// Task to track player position every half second and display shadow ball
		data.addTask(new BukkitRunnable() {
			public void run() {
				// Add current location to queue
				locationQueue.add(p.getLocation().clone().add(0, 1, 0));
				
				// Keep queue at exactly 4 positions (2 seconds)
				if (locationQueue.size() > 4) {
					locationQueue.removeFirst();
				}
				
				// Display shadow ball at the position from 2 seconds ago
				if (locationQueue.size() == 4) {
					Location shadowLoc = locationQueue.getFirst();
					shadowBall.play(p, shadowLoc);
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 10L)); // Run every half second (10 ticks)
		
		// Trigger when dealing physical damage
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageCategory.PHYSICAL)) return TriggerResult.keep();
			
			// Check cooldown (2 second = 2000ms)
			if (System.currentTimeMillis() - cooldown.getTime() < 2000) {
				return TriggerResult.keep();
			}
			
			// Only fire if we have a shadow position (2 seconds have passed)
			if (locationQueue.size() < 4) return TriggerResult.keep();
			
			// Get shadow location
			Location shadowLoc = locationQueue.getFirst();
			
			// Calculate end location: 3 blocks in front of player
			Vector forward = p.getEyeLocation().getDirection().setY(0).normalize().multiply(3);
			Location endLoc = p.getLocation().add(forward).add(0, 1, 0);
			
			// Draw line and deal damage
			ParticleUtil.drawLine(p, slashLine, shadowLoc, endLoc, 0.3);
			Sounds.wither.play(p, p);
			
			for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, shadowLoc, endLoc, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK, DamageStatTracker.of(id + slot, this)), ent);
			}
			
			// Teleport shadow ball to end location (update queue)
			locationQueue.clear();
			locationQueue.add(endLoc.clone());
			
			Sounds.attackSweep.play(p, shadowLoc);
			cooldown.setTime(System.currentTimeMillis());
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_INGOT,
				"Passive. A ball of darkness follows <white>2s</white> behind you. Whenever you deal " + GlossaryTag.PHYSICAL.tag(this) + 
				" damage <white>(2s cooldown)</white>, deal " + GlossaryTag.DARK.tag(this, damage, true) + 
				" damage in a line from the ball to <white>3</white> blocks in front of you and teleport the ball there.");
	}
}
