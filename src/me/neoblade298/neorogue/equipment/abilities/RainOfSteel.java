package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
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

public class RainOfSteel extends Equipment {
	private static final String ID = "rainOfSteel";
	private static TargetProperties tp = TargetProperties.radius(4, false, TargetType.ENEMY);
	private static ParticleContainer pc = new ParticleContainer(Particle.SWEEP_ATTACK).count(10).spread(0.5, 0.5);
	private int damage;
	
	public RainOfSteel(boolean isUpgraded) {
		super(ID, "Rain of Steel", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 20, 5, tp.range));
				damage = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(ID, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			pdata.addTask(new BukkitRunnable() {
				int count = 0;
				public void run() {
					if (++count >= 5) this.cancel();
					LivingEntity trg = TargetHelper.getNearest(p, tp);
					if (trg == null) return;
					FightInstance.dealDamage(pdata, DamageType.PIERCING, damage, trg, DamageStatTracker.of(id + slot, eq));
					Sounds.attackSweep.play(p, trg);
					pc.play(p, trg);
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 5L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_SHARD,
				"On cast, deal " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage to the nearest enemy 5 times.");
	}
}
