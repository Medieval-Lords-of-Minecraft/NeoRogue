package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ManaBlitz extends Equipment {
	private static final String ID = "manaBlitz";
	private int inc;

	public ManaBlitz(boolean isUpgraded) {
		super(ID, "Mana Blitz", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(10, 0, 16, 0));
		inc = isUpgraded ? 50 : 30;
	}

	@Override
	public void setupReforges() {
		addReforge(Manabending.get(), Flashfire.get());
		addReforge(Intuition.get(), ManaArc.get());
		addReforge(CalculatingGaze.get(), AvatarState.get());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.fire.play(p, p);
			data.addDamageBuff(DamageBuffType.of(DamageCategory.MAGICAL),
					Buff.increase(data, inc, BuffStatTracker.damageBuffAlly(this)), 160);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH, "On cast, increase your " + GlossaryTag.MAGICAL.tag(this) + " by "
				+ DescUtil.yellow(inc) + " for <white>8s</white>.");
	}
}
