package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.BasicStatus;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BitterCold extends Equipment {
	private static final String ID = "BitterCold";
	private int stacks, damage;
	private static final int THRESHOLD = 10;
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK).blockData(Material.ICE.createBlockData()).count(50).spread(1, 1).offsetY(1);
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_GLASS_BREAK);
	
	public BitterCold(boolean isUpgraded) {
		super(ID, "Bitter Cold", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		stacks = isUpgraded ? 3 : 2;
		damage = isUpgraded ? 300 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 3;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.ICE)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata2, in2) -> {
				PreApplyStatusEvent ev2 = (PreApplyStatusEvent) in2;
				if (!ev2.isStatus(StatusType.FROST)) return TriggerResult.keep();
				ev2.getStacksBuffList().add(new Buff(data, stacks, 0, BuffStatTracker.ignored(this)));
				return TriggerResult.keep();
			});

			data.addTrigger(id, Trigger.APPLY_STATUS, (pdata3, in3) -> {
				Player p2 = data.getPlayer();
				ApplyStatusEvent ev3 = (ApplyStatusEvent) in3;
				if (!ev3.isStatus(StatusType.FROST)) return TriggerResult.keep();
				FightData fd = ev3.getTarget();
				if (fd.getStatus(StatusType.FROST).getStacks() < THRESHOLD) return TriggerResult.keep();
				if (fd.hasStatus(p2.getName() + "-bitterCold")) return TriggerResult.keep();
				Status s = new BasicStatus(p2.getName() + "-bitterCold", data, StatusClass.NONE, true);
				fd.applyStatus(s, data, 1, -1);
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.ICE, DamageStatTracker.of(id + slot, this)), fd.getEntity());
				pc.play(p2, fd.getEntity());
				sc.play(p2, fd.getEntity());
				return TriggerResult.keep();
			});
			
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PACKED_ICE,
				GlossaryTag.POWER.tag(this) + ". Activates after dealing " + GlossaryTag.ICE.tag(this) + " damage " + DescUtil.white(3) + " times. All applications of " + GlossaryTag.FROST.tag(this) + " are increased by " + DescUtil.yellow(stacks) + ". Once per enemy, applying " +
				GlossaryTag.FROST.tag(this, THRESHOLD, false) + " to them will deal " + GlossaryTag.ICE.tag(this, damage, true) + " damage to them.");
	}
}
