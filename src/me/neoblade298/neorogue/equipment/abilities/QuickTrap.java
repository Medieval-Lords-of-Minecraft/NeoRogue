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
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class QuickTrap extends Equipment {
	private static final String ID = "quickTrap";
	private static TargetProperties tp = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static ParticleContainer trap = new ParticleContainer(Particle.CRIT).count(50).spread(1, 0.2),
		hit = new ParticleContainer(Particle.CRIT).count(50).spread(1, 1);
	private int damage, thres, secs = 5;
	
	public QuickTrap(boolean isUpgraded) {
		super(ID, "Quick Trap", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 10, 8, 0));
		
		damage = isUpgraded ? 150 : 100;
		thres = isUpgraded ? 200 : 300;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		ItemStack charged = item.clone().withType(Material.STICKY_PISTON);
		inst.setAction((pd, in) -> {
			if (am.getDouble() >= thres) {
				am.addDouble(-thres);
				Sounds.equip.play(p, p);
				initTrap(p, data, this, slot);
				if (am.getDouble() < thres) {
					inst.setIcon(item);
				}
				else {
					charged.setAmount((int) (am.getDouble() / thres));
					inst.setIcon(charged);
				}
				inst.setIcon(item);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.LEFT_CLICK, inst);

		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			am.addDouble(ev.getTotalDamage());
			if (am.getDouble() >= thres) {
				charged.setAmount((int) (am.getDouble() / thres));
				inst.setIcon(charged);
			}
			return TriggerResult.keep();
		});
	}

	private void initTrap(Player p, PlayerFightData data, Equipment eq, int slot) {
		Location loc = p.getLocation();
		data.addTrap(new Trap(data, loc, secs * 20) {
			@Override
			public void tick() {
				trap.play(p, loc);
				LivingEntity trg = TargetHelper.getNearest(p, loc, tp);
				if (trg != null) {
					Sounds.breaks.play(p, trg);
					hit.play(p, trg);
					DamageMeta dm = new DamageMeta(data, damage, DamageType.BLUNT, DamageStatTracker.of(id + slot, eq), DamageOrigin.TRAP);
					FightInstance.dealDamage(dm, trg);
					data.removeTrap(this);
				}
			}
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PISTON,
				"Every time you deal " + DescUtil.yellow(thres) + " damage, left clicking drops a trap " + DescUtil.duration(secs, false) + " that deals "
				+ GlossaryTag.BLUNT.tag(this, damage, true) + " damage to the first enemy that steps on it.");
	}
}
