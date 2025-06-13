package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.weapons.Gravity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Blast extends Equipment {
	private static final String ID = "blast";
	private static final TargetProperties tp = TargetProperties.radius(4, false);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			expl = new ParticleContainer(Particle.EXPLOSION).count(20).spread(tp.range / 2, 0.5);
	private static final Circle circ = new Circle(tp.range);
	private int damage;
	
	public Blast(boolean isUpgraded) {
		super(ID, "Blast", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 10, 14, tp.range));
				damage = isUpgraded ? 120 : 80;
	}
	
	@Override
	public void setupReforges() {
		addReforge(CalculatingGaze.get(), GroundLance.get(), Gravity.get());
		addReforge(Intuition.get(), ArcaneBlast.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			data.addTask(new BukkitRunnable() {
				private int count = 0;
				public void run() {
					circ.play(pc, p.getTargetBlockExact((int) properties.get(PropertyType.RANGE)).getLocation().add(0, 1, 0), LocalAxes.xz(), null);
					if (++count >= 2) {
						cancel();
						return;
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 20, 20));

			data.charge(40).then(new Runnable() {
				public void run() {
					Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
					if (b.getType().isAir()) {
						data.addMana(properties.get(PropertyType.MANA_COST));
						inst.setCooldown(0);
						Sounds.error.play(p, p);
						return;
					}

					Location loc = b.getLocation().add(0, 1, 0);
					circ.play(pc, loc, LocalAxes.xz(), null);
					expl.play(p, loc);
					Sounds.explode.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE), ent);
					}
				}
			});
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TNT,
				"On cast, " + DescUtil.charge(this, 1, 2) + " before dealing " + GlossaryTag.FIRE.tag(this, damage, true) + 
				" in an area around the block you aim at.");
	}
}
