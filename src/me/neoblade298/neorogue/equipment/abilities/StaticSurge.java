package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StaticSurge extends Equipment {
	private static final String ID = "StaticSurge";
	private int damage, electrified;
	
	public StaticSurge(boolean isUpgraded) {
		super(ID, "Static Surge", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		damage = isUpgraded ? 40 : 25;
		electrified = isUpgraded ? 7 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd == null || !fd.hasStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
			if (am.addCount(1) < 3) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			ActionMeta sprintMeta = new ActionMeta();
			data.addTrigger(id + "-sprint", Trigger.TOGGLE_SPRINT, (pdata2, in2) -> {
				Player p2 = data.getPlayer();
				if (p2.isSprinting()) {
					sprintMeta.setTime(System.currentTimeMillis());
				} else {
					sprintMeta.setTime(0);
				}
				return TriggerResult.keep();
			});

			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTrigger(id + "-active", Trigger.PRE_BASIC_ATTACK, (pdata3, in3) -> {
						if (sprintMeta.getTime() == 0 || System.currentTimeMillis() - sprintMeta.getTime() < 1000) {
							return TriggerResult.keep();
						}
						PreBasicAttackEvent ev2 = (PreBasicAttackEvent) in3;
						ev2.getMeta().addDamageSlice(
								new DamageSlice(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, StaticSurge.this)));
						FightInstance.applyStatus(ev2.getTarget(), StatusType.ELECTRIFIED, data, electrified, -1);
						return TriggerResult.keep();
					});
				}
			}.runTask(NeoRogue.inst()));

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.white(3) + " " + GlossaryTag.ELECTRIFIED.tag(this) + " enemies. If you have been sprinting for at least " + DescUtil.white("1s") + ", your basic attacks deal an additional " + 
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage and apply " + 
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) + ".");
	}
}
