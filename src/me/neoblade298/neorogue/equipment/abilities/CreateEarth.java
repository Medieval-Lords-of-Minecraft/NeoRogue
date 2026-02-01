package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class CreateEarth extends Equipment {
	private static final String ID = "CreateEarth";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			earth = new ParticleContainer(Particle.BLOCK).blockData(Material.DIRT.createBlockData());
	private static final TargetProperties tp = TargetProperties.radius(3, false);
	private static final Circle circ = new Circle(tp.range);
	private int damage, conc;

	public CreateEarth(boolean isUpgraded) {
		super(ID, "Create Earth", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(15, 15, 15, 12));
		damage = isUpgraded ? 160 : 120;
		conc = isUpgraded ? 60 : 40;
		properties.setCastType(CastType.POST_TRIGGER);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			circ.play(pc, p.getTargetBlockExact((int) properties.get(PropertyType.RANGE)).getLocation().add(0, 1, 0),
					LocalAxes.xz(), null);
			data.charge(20).then(new Runnable() {
				public void run() {
					CastUsableEvent last = inst.getLastCastEvent();
					Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
					if (b == null) {
						Sounds.error.play(p, p);
						data.addMana(last.getManaCost());
						data.addStamina(last.getStaminaCost());
						inst.setCooldown(0);
						return;
					}
					Location loc = b.getLocation().add(0, 1, 0);
					circ.play(pc, loc, LocalAxes.xz(), earth);
					Sounds.explode.play(p, loc);
					data.runActions(data, Trigger.CAST_USABLE, new CastUsableEvent(inst, CastType.POST_TRIGGER, last.getManaCost(), last.getStaminaCost(), last.getCooldown(), last.getTags()));

					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT, DamageStatTracker.of(id + slot, eq)), ent);
						FightInstance.applyStatus(ent, StatusType.CONCUSSED, data, conc, -1);
						FightInstance.knockback(ent, new Vector(0, 2, 0));
					}
				}
			});
			return TriggerResult.keep();
		});

		inst.setCondition((pl, pdata, in) -> {
			Player p = data.getPlayer();
			return p.getTargetBlockExact((int) properties.get(PropertyType.RANGE)) != null;
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DIRT,
				"On cast, " + GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before dealing "
						+ GlossaryTag.BLUNT.tag(this, damage, true) + " damage, applying "
						+ GlossaryTag.CONCUSSED.tag(this, conc, true)
						+ ", and knocking up all enemies in a radius around a targeted block.");
	}
}
