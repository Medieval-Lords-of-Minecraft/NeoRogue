package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Fissure extends Equipment {
	private static final String ID = "Fissure";
	private static final TargetProperties tp = TargetProperties.line(6, 2, TargetType.ENEMY);
	private static final ParticleContainer part = new ParticleContainer(Particle.CLOUD).spread(1, 0.1).speed(0.02).count(20);
	private int damage, concussed;
	
	public Fissure(boolean isUpgraded) {
		super(ID, "Fissure", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, isUpgraded ? 11 : 14, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		damage = isUpgraded ? 250 : 180;
		concussed = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Player p = data.getPlayer();
			data.charge(20, 0);
			Sounds.equip.play(p, p);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.explode.play(p, p);
					Vector forward = p.getEyeLocation().getDirection().setY(0).normalize().multiply(tp.range);
					Location end = p.getLocation().add(forward);
					ParticleUtil.drawLine(p, part, p.getLocation(), end, 0.5);
					
					for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, p.getLocation(), end, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN, DamageStatTracker.of(id + slot, eq)), ent);
						FightInstance.getFightData(ent.getUniqueId()).applyStatus(StatusType.CONCUSSED, data, concussed, -1);
						FightInstance.knockback(ent, new Vector(0, 0.2, 0));
					}
				}
			}.runTaskLater(NeoRogue.inst(), 20L));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.COAL,
				"On cast, " + DescUtil.charge(this, 0, 1) + ", then deal " + GlossaryTag.EARTHEN.tag(this, damage, true) + " damage and knock up in a line."
				+ " All enemies damaged are given " + GlossaryTag.CONCUSSED.tag(this, concussed, true) + ".");
	}
}
