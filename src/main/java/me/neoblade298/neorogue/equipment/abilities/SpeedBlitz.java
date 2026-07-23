package me.neoblade298.neorogue.equipment.abilities;
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
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
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
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP);
	private int damage;
	
	public SpeedBlitz(boolean isUpgraded) {
		super(ID, "Speed Blitz", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(5, 35, isUpgraded ? 12 : 15, 0));
		damage = isUpgraded ? 75 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new StandardEquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));
			data.addTask(new BukkitRunnable() {
				int attackCount = 0;

				public void run() {
					attackCount++;
					if (attackCount >= TOTAL_ATTACKS) {
						return;
					}

					Player p2 = data.getPlayer();
					LivingEntity target = TargetHelper.getNearest(p2, tp);
					if (target == null) {
						return;
					}

					FightInstance.dealDamage(data, DamageType.PIERCING, damage, target, DamageStatTracker.of(id + slot, SpeedBlitz.this));
					pc.play(p2, target);
					sc.play(p2, target);

					if (attackCount > 5) {
						cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0, ATTACK_INTERVAL));

			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DIAMOND_SWORD,
				"On cast, with " + DescUtil.potion("Slowness", 0, 1) +
				", deal " + GlossaryTag.PIERCING.tag(this, damage) +
				" damage " + DescUtil.val(5) + " times over " + DescUtil.val("1s") + " to the nearest enemy.");
	}
}
