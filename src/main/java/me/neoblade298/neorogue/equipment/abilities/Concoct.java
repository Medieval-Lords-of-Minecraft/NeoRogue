package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Concoct extends Equipment {
	private static final String ID = "Concoct";
	private int poison, duration, poisonDuration;
	
	public Concoct(boolean isUpgraded) {
		super(ID, "Concoct", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 12, 0));
		poison = isUpgraded ? 25 : 15;
		duration = 3;
		poisonDuration = 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, sessionEq, slot, es);
		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.water.play(p, p);
			inst.setTime(System.currentTimeMillis());
			new BukkitRunnable() {
				int count = 0;
				float[] pitches = new float[] {1F, 1.0594F, 1.1224F, 1.1892F, 1.2599F};
				public void run() {
					if (inst.getTime() == -1) {
						this.cancel();
						return;
					}
					p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, pitches[count]);
					if (++count >= duration) this.cancel();
				}
			}.runTaskTimer(NeoRogue.inst(), 20L, 20L);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, bind, inst);
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			Player p = data.getPlayer();
			if (inst.getTime() <= 0) return TriggerResult.keep();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			Sounds.extinguish.play(p, p);
			int mult = Math.min((int) ((System.currentTimeMillis() - inst.getTime()) / 1000), duration);
			FightInstance.applyStatus(ev.getTarget(), StatusType.POISON, data, poison * mult, poisonDuration, this);
			inst.setTime(-1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DRAGON_BREATH,
				"On cast, start charging. Your next basic attack applies " + GlossaryTag.POISON.tag(this, poison, true) + " [" + DescUtil.white(poisonDuration / 20 + "s") + "] "
						+ "for every second you charged, up to " + DescUtil.white(duration) + " seconds.");
	}
}
