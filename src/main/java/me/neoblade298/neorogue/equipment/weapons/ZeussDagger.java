package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class ZeussDagger extends Equipment {
	private static final String ID = "ZeussDagger";
	private int electrified;

	public ZeussDagger(boolean isUpgraded) {
		super(ID, "Zeus's Dagger", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 240 : 160, 0.3, 0.2, DamageType.ELECTRIFIED,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		electrified = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ItemStack cooldownIcon = item.clone().withType(Material.WOODEN_SWORD);
		StandardPriorityAction state = new StandardPriorityAction(ID);
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, sessionEq, slot, es);
		state.setAction((pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().containsType(DamageType.LIGHTNING)) {
				state.addCount(1);
			}
			if (!ev.getMeta().containsType(DamageCategory.DIRECT)) {
				return TriggerResult.keep();
			}
			inst.setIcon(cooldownIcon);
			state.setTime(System.currentTimeMillis());
			data.addTask(new BukkitRunnable() {
				public void run() {
					if (state.getTime() + 3000 <= System.currentTimeMillis()) {
						inst.setIcon(item);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 60));
			return TriggerResult.keep();
		});
		data.addTrigger(ID, Trigger.DEAL_DAMAGE, state);

		inst.setAction((pdata, inputs) -> {
			Player player = data.getPlayer();
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			int electrifiedToApply = state.getCount() * electrified;
			state.setCount(0);

			weaponSwingAndDamage(player, data, ev.getTarget());
			if (electrifiedToApply > 0) {
				FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, electrifiedToApply, -1, this);
			}
			return TriggerResult.keep();
		});

		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD,
				"Apply " + GlossaryTag.ELECTRIFIED.tag(this, electrified) + " for each time you have dealt "
						+ GlossaryTag.LIGHTNING.tag(this) + " damage since the last weapon use.");
	}
}