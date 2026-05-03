package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Convergence extends Equipment {
	private static final String ID = "Convergence";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(0.5, 0.5).speed(0.1);
	private static final int PROJECTILE_DAMAGE = 30;
	private static final HashMap<DamageType, Color> DAMAGE_TYPE_COLORS = new HashMap<>();
	
	static {
		// Map damage types to colors for projectiles
		DAMAGE_TYPE_COLORS.put(DamageType.FIRE, Color.fromRGB(255, 100, 0)); // Orange-red
		DAMAGE_TYPE_COLORS.put(DamageType.ICE, Color.fromRGB(100, 200, 255)); // Light blue
		DAMAGE_TYPE_COLORS.put(DamageType.LIGHTNING, Color.fromRGB(255, 255, 100)); // Yellow
		DAMAGE_TYPE_COLORS.put(DamageType.EARTHEN, Color.fromRGB(139, 90, 43)); // Brown
		DAMAGE_TYPE_COLORS.put(DamageType.DARK, Color.fromRGB(75, 0, 130)); // Dark purple
		DAMAGE_TYPE_COLORS.put(DamageType.LIGHT, Color.fromRGB(255, 255, 255)); // White
		DAMAGE_TYPE_COLORS.put(DamageType.POISON, Color.fromRGB(50, 205, 50)); // Green
		DAMAGE_TYPE_COLORS.put(DamageType.SLASHING, Color.fromRGB(192, 192, 192)); // Silver
		DAMAGE_TYPE_COLORS.put(DamageType.PIERCING, Color.fromRGB(220, 20, 60)); // Crimson
		DAMAGE_TYPE_COLORS.put(DamageType.BLUNT, Color.fromRGB(218, 165, 32)); // Goldenrod
	}
	
	private int intel, riftThres, heal;
	
	public Convergence(boolean isUpgraded) {
		super(ID, "Convergence", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		intel = 3;
		riftThres = isUpgraded ? 3 : 4;
		heal = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Entropy.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta killTracker = new ActionMeta();
		ActionMeta lastDamageType = new ActionMeta(); // Stores the last damage type
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		// Original Entropy mechanics - gain intellect on kill, spawn rifts
		data.addTrigger(id, Trigger.KILL, inst);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			if (killTracker.getTime() + (properties.get(PropertyType.COOLDOWN) * 1000) > System.currentTimeMillis()) {
				return TriggerResult.keep();
			}
			killTracker.addCount(1);
			data.applyStatus(StatusType.INTELLECT, data, intel, -1);
			Sounds.enchant.play(p, p);
			pc.play(p, p);
			if (killTracker.getCount() % riftThres == 0) {
				Sounds.fire.play(p, p);
				data.addRift(new Rift(data, p.getLocation(), 160));
			}
			icon.setAmount(killTracker.getCount());
			inst.setIcon(icon);
			killTracker.setTime(System.currentTimeMillis());
			return TriggerResult.keep();
		});
		
		// New mechanic - track damage types and fire projectiles when type changes
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			Player p = data.getPlayer();
			
			// Get the primary damage type from the first slice
			if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
			DamageSlice primarySlice = ev.getMeta().getSlices().getFirst();
			DamageType currentType = primarySlice.getPostBuffType();
			
			// Check if this is a different type from the last damage dealt
			DamageType lastType = (DamageType) lastDamageType.getObject();
			if (lastType == null || currentType != lastType) {
				// Heal player
				FightInstance.giveHeal(p, heal, p);
				
				// Fire 3 projectiles in a cone
				ProjectileGroup proj = new ProjectileGroup();
				for (int angle : new int[] { -15, 0, 15 }) {
					proj.add(new ConvergenceProjectile(data, angle, this, slot, currentType));
				}
				proj.start(data);
				Sounds.shoot.play(p, p);
				
				// Update last damage type
				lastDamageType.setObject(currentType);
			}
			
			return TriggerResult.keep();
		});
	}
	
	private class ConvergenceProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Equipment eq;
		private int slot;
		private DamageType damageType;
		private ParticleContainer particleTrail;
		
		public ConvergenceProjectile(PlayerFightData data, int angleOffset, Equipment eq, int slot, DamageType damageType) {
			super(1.5, 12, 1); // Speed, range, tickSpeed
			this.rotation(angleOffset);
			this.size(0.2, 0.2);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
			this.damageType = damageType;
			
			// Create color-coded particle trail
			Color color = DAMAGE_TYPE_COLORS.getOrDefault(damageType, Color.WHITE);
			this.particleTrail = new ParticleContainer(Particle.DUST)
				.dustOptions(new DustOptions(color, 1F))
				.count(3).spread(0.2, 0.2);
		}
		
		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			particleTrail.play(p, proj.getLocation());
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			// Deal damage of the same type that was just dealt
			FightInstance.dealDamage(data, damageType, PROJECTILE_DAMAGE, hit.getEntity(),
				DamageStatTracker.of(ID + slot, eq));
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(data, PROJECTILE_DAMAGE, damageType,
				DamageStatTracker.of(ID + slot, eq)));
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ECHO_SHARD,
				"Passive. Gain " + GlossaryTag.INTELLECT.tag(this, intel, true) + " on kill. Every " + 
				DescUtil.yellow(riftThres) + " kills, spawn a " + GlossaryTag.RIFT.tag(this) + 
				" [<white>8s</white>] at your location. Whenever you deal a damage type that is different " +
				"from your previous damage type, heal for " + DescUtil.yellow(heal) + " and fire <white>3</white> " +
				"color-coded projectiles in a cone dealing " + DescUtil.yellow(PROJECTILE_DAMAGE) + 
				" damage of the same damage type.");
	}
}
