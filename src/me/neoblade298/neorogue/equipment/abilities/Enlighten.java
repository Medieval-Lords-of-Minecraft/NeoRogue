package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Enlighten extends Equipment {
	private static final String ID = "Enlighten";
	private int sanct;
	
	public Enlighten(boolean isUpgraded) {
		super(ID, "Enlighten", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		
		sanct = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.GRANT_SHIELDS, (pdata, in) -> {
			am.setBool(true);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().containsType(DamageType.LIGHT)) {
				am.addCount(1);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (!am.getBool() || am.getCount() < 1) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			ActionMeta am2 = new ActionMeta();
			data.addTrigger(id, Trigger.GRANT_SHIELDS, (pdata2, in2) -> {
				am2.setBool(true);
				return TriggerResult.keep();
			});

			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata3, in3) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in3;
				if (am2.getBool()) {
					FightInstance.applyStatus(ev.getTarget(), StatusType.SANCTIFIED, data, sanct, -1);
					am2.setBool(false);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SEA_LANTERN,
				GlossaryTag.POWER.tag(this) + ". Every time you apply " + GlossaryTag.SHIELDS.tag(this) + ", your next basic attack applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ". Does not stack.");
	}
}
