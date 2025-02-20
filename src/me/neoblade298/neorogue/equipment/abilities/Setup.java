package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Setup extends Equipment {
	private static final String ID = "setup";
	private int time, inc, damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(1, 1).offsetY(1),
		trap = new ParticleContainer(Particle.CRIT).count(50).spread(1, 0.2);
	private static final TargetProperties tp = TargetProperties.radius(2, true, TargetType.ENEMY);
	
	public Setup(boolean isUpgraded) {
		super(ID, "Setup", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		time = 6;
		inc = isUpgraded ? 15 : 10;
		damage = isUpgraded ? 225 : 150;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		ItemStack icon = item.clone();
		act.setAction((pdata, in) -> {
			if (!p.isSneaking()) return TriggerResult.keep();
			act.addCount(1);
			if (act.getCount() >= time) {
				pc.play(p, p);
				Sounds.enchant.play(p, p);
				initTrap(p, data);
				data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL, DamageOrigin.TRAP), new Buff(data, 0, inc * 0.01, StatTracker.damageBuffAlly(this)));
				act.addCount(-time);

				if (act.getBool()) {
					icon.setAmount(icon.getAmount() + 1);
				}
				else {
					act.setBool(true);
				}
				p.getInventory().setItem(EquipSlot.convertSlot(es, slot), icon);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PLAYER_TICK, act);
	}

	private void initTrap(Player p, PlayerFightData data) {
		Location loc = p.getLocation();
		data.addTrap(new Trap(data, loc, 200) {
			@Override
			public void tick() {
				trap.play(p, loc);
				LivingEntity trg = TargetHelper.getNearest(p, loc, tp);
				if (trg != null) {
					Sounds.breaks.play(p, trg);
					DamageMeta dm = new DamageMeta(data, damage, DamageType.BLUNT, DamageOrigin.TRAP);
					FightInstance.dealDamage(dm, trg);
					data.removeTrap(this);
				}
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SCAFFOLDING,
				"Passive. Every " + DescUtil.white(time +"s") + " spent crouched during a fight, drop a " + GlossaryTag.TRAP.tag(this) + " at your feet [<white>10s</white>] that deals " +
				GlossaryTag.BLUNT.tag(this, damage, true) + " damage and deactivates when walked over, and increase your " + GlossaryTag.TRAP.tag(this) + 
				" damage by " + DescUtil.yellow(inc + "%") + ".");
	}
}
