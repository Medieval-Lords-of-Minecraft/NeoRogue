package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class CrossbowCareKit extends Equipment {
	private static final String ID = "CrossbowCareKit";
	private static final int QUICK_DRAW = 1;
	private double damageIncrease;
	private int damagePercent;

	public CrossbowCareKit(boolean isUpgraded) {
		super(ID, "Crossbow Care Kit", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		damagePercent = isUpgraded ? 30 : 20;
		damageIncrease = damagePercent * 0.01;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		for (ItemStack stack : data.getPlayer().getInventory().getContents()) {
			if (stack == null || stack.getType() != Material.CROSSBOW) continue;
			stack.addUnsafeEnchantment(Enchantment.QUICK_CHARGE,
					stack.getEnchantmentLevel(Enchantment.QUICK_CHARGE) + QUICK_DRAW);
		}

		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent event = (PreDealDamageEvent) in;
			Equipment weapon = event.getMeta().getWeapon();
			if (!event.getMeta().isBasicAttack() || weapon == null || weapon.getItem().getType() != Material.CROSSBOW) {
				return TriggerResult.keep();
			}
			event.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
					Buff.multiplier(data, damageIncrease, BuffStatTracker.damageBuffAlly(id + slot, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHEARS, GlossaryTag.PASSIVE.tag(this) + ". All equipped crossbows gain Quick Draw +"
				+ DescUtil.white(QUICK_DRAW) + " and deal " + DescUtil.yellow(damagePercent + "%") + " more damage.");
	}
}