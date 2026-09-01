package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class HightowerArmor extends Equipment {
	private static final String ID = "HightowerArmor";
	private static final int SPRINT_COST = 2, DIRECT_REDUCTION = 2, THORNS = 50, REFLECT = 50;
	private int shields;

	public HightowerArmor(boolean isUpgraded) {
		super(ID, "Hightower Armor", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 30 : 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSprintCost(SPRINT_COST);
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, DIRECT_REDUCTION,
				StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		data.applyStatus(StatusType.THORNS, data, THORNS, -1, this);
		data.applyStatus(StatusType.REFLECT, data, REFLECT, -1, this);
		Player player = data.getPlayer();
		data.addPermanentShield(player.getUniqueId(), shields, this);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_CHESTPLATE, "Increase sprinting stamina cost by " + DescUtil.val(SPRINT_COST)
				+ ". Reduce " + GlossaryTag.DIRECT.tag(this) + " damage taken by " + DescUtil.val(DIRECT_REDUCTION)
				+ ". Start fights with " + GlossaryTag.THORNS.tag(this, THORNS) + ", "
				+ GlossaryTag.REFLECT.tag(this, REFLECT) + ", and " + GlossaryTag.SHIELDS.tag(this, shields) + ".");
	}
}
