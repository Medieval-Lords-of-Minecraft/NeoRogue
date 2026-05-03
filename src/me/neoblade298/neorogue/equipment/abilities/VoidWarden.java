package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreCastUsableEvent;

public class VoidWarden extends Equipment {
	private static final String ID = "VoidWarden";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT);
	private static final int PASSIVE_INTERVAL = 5; // Player ticks; 1 player tick = 1 second
	private static final int PASSIVE_SHIELD_DURATION = 100; // 5s

	private final int stacks;
	private final int manaIncrease;
	private final int passiveThreshold;

	static {
		pc.count(20).spread(0.5, 0.8).speed(0.05);
	}

	public VoidWarden(boolean isUpgraded) {
		super(ID, "Void Warden", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 0, 0, 0));
		stacks = isUpgraded ? 5 : 3;
		manaIncrease = isUpgraded ? 20 : 30;
		passiveThreshold = isUpgraded ? 2 : 3;
		addTags(GlossaryTag.PROTECT, GlossaryTag.SHELL, GlossaryTag.SHIELDS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta casts = new ActionMeta();
		ActionMeta passiveTimer = new ActionMeta();
		String procId = id + slot;
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			data.applyStatus(StatusType.PROTECT, data, stacks, -1);
			data.applyStatus(StatusType.SHELL, data, stacks, -1);
			pc.play(p, p);
			Sounds.enchant.play(p, p);
			casts.addCount(1);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			PreCastUsableEvent ev = (PreCastUsableEvent) in;
			if (ev.getInstance() != inst) return TriggerResult.keep();

			int extraMana = casts.getCount() * manaIncrease;
			if (extraMana <= 0) return TriggerResult.keep();

			ev.addBuff(PropertyType.MANA_COST, procId,
					Buff.increase(data, -extraMana,
							BuffStatTracker.of(procId, this, PropertyType.MANA_COST.getDisplay() + " increased")));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			passiveTimer.addCount(1);
			if (passiveTimer.getCount() < PASSIVE_INTERVAL) return TriggerResult.keep();
			passiveTimer.setCount(0);

			int combinedStacks = data.getStatus(StatusType.PROTECT).getStacks()
					+ data.getStatus(StatusType.SHELL).getStacks();
			int shields = combinedStacks / passiveThreshold;
			if (shields > 0) {
				Player p = data.getPlayer();
				data.addSimpleShield(p.getUniqueId(), shields, PASSIVE_SHIELD_DURATION);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLACK_DYE,
				"On cast, apply " + GlossaryTag.PROTECT.tag(this, stacks, true) + " and "
						+ GlossaryTag.SHELL.tag(this, stacks, true) + " to yourself. Base mana cost is "
						+ DescUtil.white((int) properties.get(PropertyType.MANA_COST)) + ". This ability's mana cost increases by "
						+ DescUtil.yellow(manaIncrease) + " each time you cast it. "
						+ "Passive. Every " + DescUtil.white("5s") + ", gain " + GlossaryTag.SHIELDS.tag(this, 1, false)
						+ " [" + DescUtil.white("5s") + "] for every " + DescUtil.yellow(passiveThreshold)
						+ " combined " + GlossaryTag.PROTECT.tag(this) + " and " + GlossaryTag.SHELL.tag(this) + " you have.");
	}
}
