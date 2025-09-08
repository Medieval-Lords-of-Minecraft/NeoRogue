package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.RightClickHitEvent;

public class Hullbreaker extends Equipment {
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR);
	private static final String ID = "hullbreaker";
	private int damage, reduc;
	private static final int THRES = 150;
	
	public Hullbreaker(boolean isUpgraded) {
		super(ID, "Hullbreaker", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.none());
				damage = 100;
				reduc = isUpgraded ? 50 : 30;

	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		ActionMeta am = new ActionMeta();

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			am.addInt(ev.getStacks());
			while (am.getInt() >= THRES) {
				am.addInt(-THRES);
				am.addCount(1);
			}
			icon.setAmount(am.getCount());
			p.getInventory().setItemInOffHand(icon);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.RIGHT_CLICK_HIT, (pdata, in) -> {
			sc.play(p, p);
			RightClickHitEvent ev = (RightClickHitEvent) in;
			LivingEntity trg = ev.getTarget();
			FightData fd = FightInstance.getFightData(trg);
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT, DamageStatTracker.of(id + slot, this)), trg);
			int count = am.getCount() / THRES;
			fd.addDefenseBuff(DamageBuffType.of(DamageCategory.PHYSICAL), Buff.increase(data, -reduc * count, BuffStatTracker.defenseDebuffEnemy(id, this, true)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ANVIL,
				"Right click to deal " + GlossaryTag.BLUNT.tag(this, damage, false) + " to an enemy, knock them back, and lower their " + GlossaryTag.PHYSICAL.tag(this) +
				" defense by " + DescUtil.yellow(reduc) + " for every " + DescUtil.white(THRES) + " " + GlossaryTag.CONCUSSED.tag(this) + " you have applied during the fight.");
	}
}
