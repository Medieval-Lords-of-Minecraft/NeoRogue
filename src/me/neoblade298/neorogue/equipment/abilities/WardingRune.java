package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;

public class WardingRune extends Equipment {
	private static final String ID = "wardingRune";
	private int reduc = 10, mana = 5;
	
	public WardingRune(boolean isUpgraded) {
		super(ID, "Warding Rune", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 0, isUpgraded ? 15 : 11, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		Trigger tr = data.getSessionData().getPlayerClass() == EquipmentClass.ARCHER ? Trigger.LEFT_CLICK : Trigger.RIGHT_CLICK;
		data.addTrigger(id, tr, (pdata, in) -> {
			Sounds.fire.play(p, p);
			if (tr == Trigger.LEFT_CLICK) p.swingOffHand();
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, reduc, StatTracker.defenseBuffAlly(am.getId(), this)), 100);
			am.setBool(true);
			data.addTask(new BukkitRunnable() {
				public void run() {
					am.setBool(false);
				}
			}.runTaskLater(NeoRogue.inst(), 100));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			if (ev.getMeta().containsType(DamageCategory.MAGICAL) && am.getBool()) {
				data.addMana(mana);
				Sounds.enchant.play(p, p);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.QUARTZ_SLAB,
				"On right click (left click for <gold>Archer</gold>), gain " + GlossaryTag.SHELL.tag(this, reduc, false) + " for <white>5s</white>. Receiving "
				+ GlossaryTag.MAGICAL.tag(this) + " damage during this time grants " + DescUtil.white(mana) + " mana.");
	}
}
