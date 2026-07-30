package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Radiance extends Equipment implements Power {
	private static final String ID = "Radiance";
	private static final int LIGHT_DAMAGE_PER_THRESHOLD = 10;
	private int activationThreshold, sanctifiedThreshold;

	public Radiance(boolean isUpgraded) {
		super(ID, "Radiance", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		activationThreshold = isUpgraded ? 30 : 40;
		sanctifiedThreshold = isUpgraded ? 6 : 8;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activation = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
			if (activation.addCount(ev.getStacks()) < activationThreshold) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta progress = new ActionMeta();
		int[] lightDamage = {0};
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				data.addTrigger(id + "-progress", Trigger.APPLY_STATUS, (pdata, in) -> {
					ApplyStatusEvent ev = (ApplyStatusEvent) in;
					if (!ev.isStatus(StatusType.SANCTIFIED)) return TriggerResult.keep();
					progress.addCount(ev.getStacks());
					while (progress.getCount() >= sanctifiedThreshold) {
						progress.addCount(-sanctifiedThreshold);
						lightDamage[0] += LIGHT_DAMAGE_PER_THRESHOLD;
						Player player = data.getPlayer();
						Sounds.enchant.play(player, player);
					}
					return TriggerResult.keep();
				});

				data.addTrigger(id + "-damage", Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
					if (lightDamage[0] <= 0) return TriggerResult.keep();
					PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
					ev.getMeta().addDamageSlice(new DamageSlice(data, lightDamage[0], DamageType.LIGHT,
							DamageStatTracker.of(id + slot, Radiance.this)));
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOWSTONE_DUST,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying "
				+ GlossaryTag.SANCTIFIED.tag(this, activationThreshold, true) + ". After activation, every "
				+ GlossaryTag.SANCTIFIED.tag(this, sanctifiedThreshold, true) + " you apply permanently adds "
				+ GlossaryTag.LIGHT.tag(this, LIGHT_DAMAGE_PER_THRESHOLD) + " damage to your basic attacks.");
	}
}