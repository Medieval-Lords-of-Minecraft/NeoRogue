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
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class LightningStrike extends Equipment {
	private static final String ID = "LightningStrike";
	private static final ParticleContainer tick = new ParticleContainer(Particle.FIREWORK).count(3).spread(0.3, 0.3),
			explode = new ParticleContainer(Particle.EXPLOSION).count(5).spread(3, 0.2);
	private static final TargetProperties cursor = TargetProperties.line(7, 2, TargetType.ENEMY),
			aoe = TargetProperties.radius(3, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(aoe.range);

	private int damage, thres, bonusDamage;

	public LightningStrike(boolean isUpgraded) {
		super(ID, "Lightning Strike", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 0, 12, cursor.range, aoe.range));
		damage = 100;
		thres = isUpgraded ? 40 : 50;
		bonusDamage = isUpgraded ? 200 : 100;
		properties.setCastType(CastType.POST_TRIGGER);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		ActionMeta am = new ActionMeta();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			data.channel(20).then(new Runnable() {
				public void run() {
					Block b = p.getTargetBlockExact((int) cursor.range);
					CastUsableEvent last = inst.getLastCastEvent();

					// Reset cooldown and refund mana and stamina
					if (b == null || !b.getType().isSolid()) {
						Sounds.extinguish.play(p, p);
						data.addMana(last.getManaCost());
						inst.setCooldown(0);
					} else {
						am.setLocation(p.getTargetBlockExact((int) cursor.range).getLocation());
						data.runActions(data, Trigger.CAST_USABLE, new CastUsableEvent(inst, CastType.POST_TRIGGER, last.getManaCost(), last.getStaminaCost(), last.getCooldown(), last.getTags()));
						data.addTask(new BukkitRunnable() {
							public void run() {
								Location loc = am.getLocation();
								Location top = loc.add(0, 4, 1);
								Location middle1 = loc.add(0, 1.5, 1);
								Location middle2 = loc.add(0, 2, 0);
								ParticleUtil.drawLine(p, tick, top, middle1, 0.3);
								ParticleUtil.drawLine(p, tick, middle1, middle2, 0.3);
								ParticleUtil.drawLine(p, tick, middle1, middle2, 0.3);
								explode.play(p, loc);
								double fDamage = damage + (data.getMana() >= thres ? bonusDamage : 0);
								Sounds.firework.play(p, loc);
								Sounds.explode.play(p, loc);
								circ.play(tick, loc, LocalAxes.xz(), null);
								for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, aoe)) {
									FightInstance.dealDamage(new DamageMeta(data, fDamage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, eq)), ent);
								}
							}
						}.runTaskLater(NeoRogue.inst(), 20));
					}
				}
			});
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

	}

	@Override
	public void setupItem() {
		item = createItem(Material.YELLOW_DYE, GlossaryTag.CHANNEL.tag(this)
				+ " for <white>1s</white> before marking a ground location. After <white>1s</white>, that location explodes, dealing "
				+ GlossaryTag.LIGHTNING.tag(this, damage, true) + " in an area. If you are above "
				+ DescUtil.yellow(thres) + " mana, increase the damage by " + DescUtil.yellow(bonusDamage) + ".");
	}
}
