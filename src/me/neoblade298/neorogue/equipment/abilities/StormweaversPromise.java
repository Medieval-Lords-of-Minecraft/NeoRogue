package me.neoblade298.neorogue.equipment.abilities;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class StormweaversPromise extends Equipment {
	private static final String ID = "StormweaversPromise";
	private static final int BASE_DURATION_TICKS = 60; // 3 seconds
	private static final int BONUS_DURATION_TICKS = 100; // 5 seconds
	private static final int EXTENDED_DURATION_TICKS = BASE_DURATION_TICKS + BONUS_DURATION_TICKS; // 8 seconds total
	private static final long CONSECUTIVE_WINDOW_MS = 1000; // 1 second window
	private int shields;
	
	public StormweaversPromise(boolean isUpgraded) {
		super(ID, "Stormweaver's Promise", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 20, 0, 0));
		shields = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			LinkedList<Long> recentHitTimes = new LinkedList<>();
			data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
				DealDamageEvent ev = (DealDamageEvent) in2;
				if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
				Player p = data.getPlayer();
				long currentTime = System.currentTimeMillis();
				recentHitTimes.add(currentTime);
				while (!recentHitTimes.isEmpty() && currentTime - recentHitTimes.getFirst() > CONSECUTIVE_WINDOW_MS) {
					recentHitTimes.removeFirst();
				}
				int duration = recentHitTimes.size() >= 2 ? EXTENDED_DURATION_TICKS : BASE_DURATION_TICKS;
				data.addSimpleShield(p.getUniqueId(), shields, duration);
				if (duration == EXTENDED_DURATION_TICKS) {
					Sounds.levelup.play(p, p);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD,
				GlossaryTag.POWER.tag(this) + ". Gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>3s</white>] whenever you deal projectile damage. " +
				"Increase the shield duration by " + DescUtil.white("5s") + " if you've dealt damage at least twice in the past second.");
	}
}
