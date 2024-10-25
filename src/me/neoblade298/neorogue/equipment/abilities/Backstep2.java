package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Backstep2 extends Equipment {
	private static final String ID = "backstep2";
	private int thres, damage, rend;
	
	public Backstep2(boolean isUpgraded) {
		super(ID, "Backstep II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 1, 0));
		damage = isUpgraded ? 20 : 10;
		rend = 10;
		thres = isUpgraded ? 60 : 45;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BOOTS,
				"This ability can be stored and cast once for every " + GlossaryTag.REND.tag(this, thres, true) + " stacks you apply. " +
				"On cast, jump backwards and gain " + DescUtil.potion("Speed", 0, 3) + " and fire <white>3</white> projectiles that deal " +
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage and apply " + GlossaryTag.REND.tag(this, rend, false) +".");
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			inst.addCount(-1);
			icon.setAmount(Math.min(1, inst.getCount()));
			inst.setIcon(icon);
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.setY(0).setX(-v.getX()).setZ(-v.getZ()).normalize().multiply(0.7).setY(0.3));
			Sounds.jump.play(p, p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
			data.addBuff(data, UUID.randomUUID().toString(), true, false, BuffType.GENERAL, damage, 100);
			return TriggerResult.keep();
		});
		inst.setCondition((pl, pdata) -> {
			return inst.getCount() >= 0;
		});
		data.addTrigger(id, bind, inst);

		StandardEquipmentInstance counter = new StandardEquipmentInstance(data, this, slot, es);
		counter.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.REND)) return TriggerResult.keep();
			counter.addCount(ev.getStacks());
			if (counter.getCount() >= thres) {
				inst.addCount(inst.getCount() / thres);
				icon.setAmount(inst.getCount());
				inst.setIcon(icon);
				counter.setCount(inst.getCount() % thres);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.APPLY_STATUS, counter);
	}
}
