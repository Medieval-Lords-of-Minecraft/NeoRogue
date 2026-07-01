package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Posturing extends Equipment {
	private static final String ID = "Posturing";
	private int time, inc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(1, 1).offsetY(1);
	
	public Posturing(boolean isUpgraded) {
		super(ID, "Posturing", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 5, 0, 0));
		time = isUpgraded ? 3 : 4;
		inc = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	public void setupReforges() {
		addReforge(KeenSenses.get(), Posturing2.get(), Setup.get());
		addReforge(AgilityTraining.get(), FlashDraw.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			Util.msgRaw(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			ItemStack icon = item.clone();
			String buffId = UUID.randomUUID().toString();
			StandardPriorityAction act = new StandardPriorityAction(id);
			act.setAction((pdata2, in2) -> {
				Player p2 = data.getPlayer();
				if (!p2.isSneaking()) return TriggerResult.keep();
				act.addCount(1);
				if (act.getCount() >= time) {
					pc.play(p2, p2);
					Sounds.enchant.play(p2, p2);
					data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, inc, 0, StatTracker.damageBuffAlly(buffId, this)));
					act.addCount(-time);
					if (act.getBool()) {
						icon.setAmount(icon.getAmount() + 1);
						p2.getInventory().setItem(EquipSlot.convertSlot(es, slot), icon);
					}
					else {
						act.setBool(true);
					}
				}
				return TriggerResult.keep();
			});
			data.addTrigger(id, Trigger.PLAYER_TICK, act);

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SCAFFOLDING,
				GlossaryTag.POWER.tag(this) + ". Every " + DescUtil.yellow(time +"s") + " spent crouched during a fight, increase your damage by " + DescUtil.yellow(inc) + ".");
	}
}
