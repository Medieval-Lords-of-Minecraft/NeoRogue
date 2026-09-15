package me.neoblade298.neorogue.equipment.weapons;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class ResonatingKnife extends Equipment {
	private static final String ID = "ResonatingKnife";
	private static final int DAMAGE = 100;
	private static final int DURABILITY = 15;
	private static final int DAMAGE_PER_BANK = 20;
	private static final int DAMAGE_TICKS = 2;
	private static final int TICK_PERIOD = 20;

	public ResonatingKnife(boolean isUpgraded) {
		super(ID, "Resonating Knife", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(DAMAGE, 0.5, 0.2, DamageType.PIERCING,
						Sound.ENTITY_PLAYER_ATTACK_SWEEP));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta bankedDamage = new ActionMeta();
		boolean[] broken = { false };
		StandardPriorityAction hitAction = new StandardPriorityAction(ID);
		hitAction.setAction((pdata, in) -> {
			Player player = data.getPlayer();
			LeftClickHitEvent event = (LeftClickHitEvent) in;
			weaponSwingAndDamage(player, data, event.getTarget());

			int banks = bankedDamage.getCount();
			bankedDamage.setCount(0);
			if (banks > 0) dealBankedDamage(data, event.getTarget().getUniqueId(), banks, slot);

			if (hitAction.addCount(1) >= DURABILITY) {
				broken[0] = true;
				Sounds.breaks.play(player, player);
				player.getInventory().setItem(slot, null);
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, hitAction);

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			if (broken[0]) return TriggerResult.remove();
			DealDamageEvent event = (DealDamageEvent) in;
			if (!event.getMeta().isBasicAttack() && event.getMeta().containsType(DamageType.PIERCING)
					&& !event.getMeta().hasTag(ID)) {
				bankedDamage.addCount(1);
			}
			return TriggerResult.keep();
		});
	}

	private void dealBankedDamage(PlayerFightData data, UUID targetId, int banks, int slot) {
		double damagePerTick = (double) banks * DAMAGE_PER_BANK / DAMAGE_TICKS;
		data.addTask(new BukkitRunnable() {
			private int ticks;

			@Override
			public void run() {
				FightData targetData = FightInstance.getFightData(targetId);
				if (targetData == null || targetData.getInstance() != data.getInstance()) {
					cancel();
					return;
				}
				LivingEntity target = targetData.getEntity();
				if (target == null || !target.isValid() || target.isDead()) {
					cancel();
					return;
				}
				DamageMeta meta = new DamageMeta(data, damagePerTick, DamageType.PIERCING,
						DamageStatTracker.of(id + slot, ResonatingKnife.this));
				meta.addTag(ID);
				FightInstance.dealDamage(meta, target);
				if (++ticks >= DAMAGE_TICKS) cancel();
			}
		}.runTaskTimer(NeoRogue.inst(), TICK_PERIOD, TICK_PERIOD));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD,
				"Has " + DescUtil.val(DURABILITY) + " uses per fight. Non-basic-attack "
				+ GlossaryTag.PIERCING.tag(this) + " damage instances bank resonance. On hit, consume all resonance to deal "
				+ GlossaryTag.PIERCING.tag(this, DAMAGE_PER_BANK) + " damage per bank "
				+ DescUtil.duration(DAMAGE_TICKS) + ".");
	}
}