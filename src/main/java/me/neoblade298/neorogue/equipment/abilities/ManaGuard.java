package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
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

public class ManaGuard extends Equipment {
	private static final String ID = "ManaGuard";
	private int reduc;

	public ManaGuard(boolean isUpgraded) {
		super(ID, "Mana Guard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		reduc = isUpgraded ? 9 : 6;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getMana() < data.getMaxMana() * 0.5) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msgRaw(p, Component.text("").append(hoverable).append(Component.text(" was activated", NamedTextColor.GRAY)));

			String buffId = id + slot;
			data.addManaRegen(-1);
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.increase(data, reduc, StatTracker.defenseBuffAlly(buffId, this)));

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, GlossaryTag.POWER.tag(this) + ". Activates after reaching " + DescUtil.white("50%") + " mana. Reduces damage taken by " + DescUtil.yellow(reduc)
				+ " but decrease mana regen by " + DescUtil.white(1) + ".");
	}
}
