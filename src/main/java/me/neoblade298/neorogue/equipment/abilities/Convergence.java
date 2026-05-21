package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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
	
	private static final int ACTIVATION_THRES = 2;
	private int intel, heal;
	
	public Convergence(boolean isUpgraded) {
		super(ID, "Convergence", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		intel = 3;
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
		int[] killCount = {0};
		ActionMeta lastDamageType = new ActionMeta();
		
		// Power: activates after 2 kills
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			killCount[0]++;
			if (killCount[0] < ACTIVATION_THRES) return TriggerResult.keep();
			
			Player p = data.getPlayer();
			Sounds.enchant.play(p, p);
			pc.play(p, p);
			me.neoblade298.neocore.bukkit.util.Util.msgRaw(p, net.kyori.adventure.text.Component.text("").append(hoverable).append(net.kyori.adventure.text.Component.text(" was activated", net.kyori.adventure.text.format.NamedTextColor.GRAY)));
			
			// After activation: gain intellect on kill, fire projectiles on type change
			data.addTrigger(id, Trigger.KILL, (pdata2, in2) -> {
				Player p2 = data.getPlayer();
				data.applyStatus(StatusType.INTELLECT, data, intel, -1);
				Sounds.enchant.play(p2, p2);
				pc.play(p2, p2);
				return TriggerResult.keep();
			});
			
			data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
				DealDamageEvent ev = (DealDamageEvent) in2;
				Player p2 = data.getPlayer();
				
				if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
				DamageSlice primarySlice = ev.getMeta().getSlices().getFirst();
				DamageType currentType = primarySlice.getPostBuffType();
				
				DamageType lastType = (DamageType) lastDamageType.getObject();
				if (lastType == null || currentType != lastType) {
					FightInstance.giveHeal(p2, heal, p2);
					
					ProjectileGroup proj = new ProjectileGroup();
					for (int angle : new int[] { -15, 0, 15 }) {
						proj.add(new ConvergenceProjectile(data, angle, this, slot, currentType));
					}
					proj.start(data);
					Sounds.shoot.play(p2, p2);
					
					lastDamageType.setObject(currentType);
				}
				
				return TriggerResult.keep();
			});
			
			return TriggerResult.remove();
		});
	}
	
	private class ConvergenceProjectile extends Projectile {
		private PlayerFightData data;
		private Equipment eq;
		private int slot;
		private DamageType damageType;
		private ParticleContainer particleTrail;
		
		public ConvergenceProjectile(PlayerFightData data, int angleOffset, Equipment eq, int slot, DamageType damageType) {
			super(1.5, 12, 1); // Speed, range, tickSpeed
			this.rotation(angleOffset);
			this.size(0.2, 0.2);
			this.data = data;
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
			particleTrail.play(data.getPlayer(), proj.getLocation());
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
				GlossaryTag.POWER.tag(this) + ". Activates after " + DescUtil.white(ACTIVATION_THRES) + " kills. " +
				"Gain " + GlossaryTag.INTELLECT.tag(this, intel, true) + " on kill. " +
				"Whenever you deal a different damage type from your previous type, " +
				"heal for " + DescUtil.yellow(heal) + " and fire " + DescUtil.white(3) + " " +
				"projectiles in a cone dealing " + DescUtil.yellow(PROJECTILE_DAMAGE) + 
				" damage of the same type.");
	}
}
