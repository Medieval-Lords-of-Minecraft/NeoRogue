package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LayTrap extends Equipment {
	private static final String ID = "layTrap";
	private static TargetProperties tp = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static ParticleContainer trap = new ParticleContainer(Particle.CRIT).count(50).spread(1, 0.2),
		hit = new ParticleContainer(Particle.CRIT).count(50).spread(1, 1);
	private int damage, secs;
	
	public LayTrap(boolean isUpgraded) {
		super(ID, "Lay Trap", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 5, 15, 0));
		
		damage = isUpgraded ? 150 : 120;
		secs = 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			Sounds.equip.play(p, p);
			data.channel(40);
			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTask(initTrap(p, data));
				}
			}.runTaskLater(NeoRogue.inst(), 40L));
			return TriggerResult.keep();
		}));
	}

	private BukkitTask initTrap(Player p, PlayerFightData data) {
		return new BukkitRunnable() {
			Location loc = p.getLocation();
			private int tick = 0;
			public void run() {
				trap.play(p, loc);
				LivingEntity trg = TargetHelper.getNearest(p, loc, tp);
				if (trg != null) {
					Sounds.breaks.play(p, trg);
					hit.play(p, trg);
					FightInstance.dealDamage(data, DamageType.BLUNT	, damage, trg);
					trg.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, secs * 20, 2));
					this.cancel();
				}
				if (++tick >= 20) {
					this.cancel();
				}
			}
		}.runTaskTimer(NeoRogue.inst(), 0L, 10L);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OAK_TRAPDOOR,
				"On cast, " + GlossaryTag.CHARGE.tag(this) + " for <white>2s</white>. Afterwards, drop a trap that lasts for " + DescUtil.white("10s") +
				". If an enemy steps on the trap, they take " + GlossaryTag.BLUNT.tag(this, damage, true) +
				" damage, receive " + DescUtil.potion("Slowness", 3, secs) + ", and deactivate the trap.");
	}
}
