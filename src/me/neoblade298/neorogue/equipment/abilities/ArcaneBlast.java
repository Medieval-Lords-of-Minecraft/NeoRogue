package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class ArcaneBlast extends Equipment {
	private static final String ID = "arcaneBlast";
	private static final TargetProperties tp = TargetProperties.radius(4, true);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			expl = new ParticleContainer(Particle.EXPLOSION).count(20).spread(tp.range / 2, 0.5);
	private static final Circle circ = new Circle(tp.range);
	private int damage;
	
	public ArcaneBlast(boolean isUpgraded) {
		super(ID, "Arcane Blast", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(25, 0, 10, 14, tp.range));
				damage = isUpgraded ? 120 : 80;
		properties.setCastType(CastType.POST_TRIGGER);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone().withType(Material.TNT_MINECART);
		ActionMeta am = new ActionMeta();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setCondition((pl, pdata, in) -> {
			return am.getBool() || p.getTargetBlockExact((int) properties.get(PropertyType.RANGE)) != null;
		});
		inst.setAction((pdata, in) -> {
			// First cast
			if (!am.getBool()) {
				// Cast indicator
				circ.play(pc,
						p.getTargetBlockExact((int) properties.get(PropertyType.RANGE)).getLocation().add(0, 1, 0), LocalAxes.xz(), null);

				data.charge(20).then(new Runnable() {
					public void run() {
						Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
						if (b == null) {
							data.addMana(properties.get(PropertyType.MANA_COST));
							inst.setCooldown(0);
							Sounds.error.play(p, p);
							return;
						}

						data.runActions(data, Trigger.CAST_USABLE, new CastUsableEvent(inst, CastType.POST_TRIGGER));
						Location loc = b.getLocation().add(0, 1, 0);
						Sounds.equip.play(p, loc);
						circ.play(pc, loc, LocalAxes.xz(), null);
						am.setCount(0);
						am.setBool(true);
						am.setLocation(loc);
						BukkitTask task = new BukkitRunnable() {
							public void run() {
								if (!am.getBool()) {
									cancel();
									return;
								}
								if (am.getCount() < 5) {
									am.addCount(1);
									icon.setAmount(am.getCount());
									inst.setIcon(icon);
									inst.setCooldown(0);
								}
								circ.play(pc, loc, LocalAxes.xz(), null);
							}
						}.runTaskTimer(NeoRogue.inst(), 20, 20);
						am.setTask(task);
						data.addTask(task);
					}
				});
			}
			// Recast
			else {
				if (am.getCount() == 0) {
					Sounds.error.play(p, p);
					return TriggerResult.keep();
				}
				expl.play(p, am.getLocation());
				Sounds.explode.play(p, am.getLocation());
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, am.getLocation(), tp)) {
					FightInstance.dealDamage(new DamageMeta(data, damage * am.getCount(), DamageType.FIRE), ent);
				}
				am.setBool(false);
				am.setCount(0);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TNT,
				"On cast, " + DescUtil.charge(this, 1, 1) + " before dropping a marker at the block you aim at. It gains a stack every second, up to <white>5</white>. Recast to " +
				"detonate, dealing " + GlossaryTag.FIRE.tag(this, damage, true) + " damage multiplied by your stacks to all nearby enemies.");
	}
}
