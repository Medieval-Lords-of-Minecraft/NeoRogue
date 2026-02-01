package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SpeedBlitz extends Equipment {
	private static final String ID = "SpeedBlitz";
	private static final int TOTAL_ATTACKS = 5;
	private static final int ATTACK_INTERVAL = 4; // 20 ticks / 5 attacks = 4 ticks between attacks
	private static final TargetProperties tp = TargetProperties.radius(20, true, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.SWEEP_ATTACK).count(10).spread(0.5, 0.5);
	private static final ParticleContainer chargePc = new ParticleContainer(Particle.CRIT).count(20).spread(0.5, 0.5);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP);
	private int damage;
	
	public SpeedBlitz(boolean isUpgraded) {
		super(ID, "Speed Blitz", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 0, isUpgraded ? 12 : 15, 0));
		damage = isUpgraded ? 120 : 80;
		properties.addUpgrades(PropertyType.COOLDOWN);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new StandardEquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			// Apply charging effect
			Location loc = p.getLocation();
			chargePc.play(p, loc);
			Sounds.enchant.play(p, loc);
            data.charge(20).then(new Runnable() {
                @Override
                public void run() {
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));
                    // After 1 second charge, start the blitz
                    data.addTask(new BukkitRunnable() {
                        int attackCount = 0;
                        
                        public void run() {
                            attackCount++;
                            if (attackCount >= TOTAL_ATTACKS) {
                                return;
                            }
                            
                            LivingEntity target = TargetHelper.getNearest(p, tp);
                            if (target == null) {
                                return;
                            }
                            
                            // Deal damage
                            FightInstance.dealDamage(data, DamageType.PIERCING, damage, target, DamageStatTracker.of(id + slot, SpeedBlitz.this));
                            
                            // Visual effects
                            pc.play(p, target);
                            sc.play(p, target);
                            
                            // Apply slowness on first attack
                            if (attackCount > 5) {
                                cancel();
                            }
                        }
                    }.runTaskTimer(NeoRogue.inst(), 0, ATTACK_INTERVAL));
                }
            });
			
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DIAMOND_SWORD,
				"On cast, charge for <white>1s</white>. Then, with " + DescUtil.potion("Slowness", 0, 1) +
                ", deal " + GlossaryTag.PIERCING.tag(this, damage, true) + 
				" damage <white>5</white> times over <white>1s</white> to the nearest enemy.");
	}
}
