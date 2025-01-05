package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.BasicStatus;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;

public class HexingShot extends Equipment {
	private static final String ID = "hexingShot";
	private int dec = 15, damage;
	private ItemStack activeIcon;
	private static TargetProperties tp = TargetProperties.radius(3, false, TargetType.ENEMY);
	private static ParticleContainer spike = new ParticleContainer(Particle.FIREWORK).count(50).spread(1, 0.4);
	
	public HexingShot(boolean isUpgraded) {
		super(ID, "Hexing Shot", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 3, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
			damage = isUpgraded ? 90 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			if (inst.getCount() == 0) {
				inst.setCount(1);
				Sounds.equip.play(p, p);
				inst.setIcon(activeIcon);
			}
			else {
				inst.setCount(0);
				inst.setIcon(item);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			if (inst.getCount() == 1) {
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, -dec, 0, StatTracker.damageDebuffAlly(this)));
				FightData trg = FightInstance.getFightData(ev.getTarget());
				trg.applyStatus(new BasicStatus(ID + p.getName(), trg, StatusClass.NEGATIVE, true), data, 1, 10);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			KillEvent ev = (KillEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd.hasStatus(ID + p.getName())) {
				initTrap(p, data, ev.getTarget().getLocation());
			}
			return TriggerResult.keep();
		});
	}

	private void initTrap(Player p, PlayerFightData data, Location loc) {
		Sounds.equip.play(p, p);
		data.addTrap(new Trap(data, loc, 200) {
			@Override
			public void tick() {
				spike.play(p, loc);
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.PIERCING, DamageOrigin.TRAP), TargetHelper.getEntitiesInRadius(p, loc, tp));
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POLISHED_BLACKSTONE_BRICK_SLAB,
				"Toggleable, off by default. When active, your basic attacks are weakened by " + DescUtil.white(dec) + ". Killing an enemy with a basic attack while active " +
				"drops a " + GlossaryTag.TRAP.tag(this) + " on the killed enemy that deals " + GlossaryTag.PIERCING.tag(this, damage, true) + " damage per second for " +
				DescUtil.white("10s") + ".");
		activeIcon = item.withType(Material.SCULK_SENSOR);
	}
}
