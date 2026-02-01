package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Brightshell extends Equipment {
	private static final String ID = "Brightshell";
	private int shields, damage, sanct;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
		aoe = new ParticleContainer(Particle.FIREWORK).count(100).spread(4, 1).speed(0.01);
	private static final TargetProperties tp = TargetProperties.radius(4, true);
	private static final Circle circ = new Circle(tp.range);
	
	public Brightshell(boolean isUpgraded) {
		super(ID, "Brightshell", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 10, 10, 0, tp.range));
		shields = isUpgraded ? 15 : 10;
		damage = 100;
		sanct = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Player p = data.getPlayer();
					Sounds.fire.play(p, p);
					circ.play(pc, p.getLocation(), LocalAxes.xz(), null);
					aoe.play(p, p);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHT, DamageStatTracker.of(id, eq)), ent);
						FightInstance.applyStatus(ent, StatusType.SANCTIFIED, data, sanct, -1);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 60));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"On cast, gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>]. After <white>3s</white>, deal " +
				GlossaryTag.LIGHT.tag(this, damage, false) + " damage and apply " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + " to all nearby enemies.");
	}
}
