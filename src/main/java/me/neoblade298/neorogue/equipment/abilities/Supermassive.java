package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Supermassive extends Equipment implements Power {
	private static final String ID = "Supermassive";
	private static final Circle GRAVITY_RING = new Circle(1.35);
	private static final ParticleContainer GRAVITY_EDGE = new ParticleContainer(Particle.REVERSE_PORTAL)
			.count(1).spread(0, 0).speed(0);
	private static final ParticleContainer MASS_BURST = new ParticleContainer(Particle.SOUL)
			.count(8).spread(0.1, 0.1).speed(0.01).offsetY(0.8);
	private static final SoundContainer ACTIVATION_SOUND = new SoundContainer(Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.65F, 0.8F);
	private static final SoundContainer STACK_SOUND = new SoundContainer(Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.35F, 0.7F);
	private int riftsPerStack, damageIncrease;
	private double damageMultiplier;

	public Supermassive(boolean isUpgraded) {
		super(ID, "Supermassive", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(45, 0, 0, 0));
		riftsPerStack = 2;
		damageIncrease = isUpgraded ? 40 : 30;
		damageMultiplier = damageIncrease / 100.0;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		}));
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		GRAVITY_RING.play(GRAVITY_EDGE, data.getPlayer().getLocation().clone().add(0, 0.1, 0), LocalAxes.xz(), null);
		MASS_BURST.play(data.getPlayer(), data.getPlayer().getLocation());
		ACTIVATION_SOUND.play(data.getPlayer(), data.getPlayer());
		ActionMeta rifts = new ActionMeta();
		ActionMeta stacks = new ActionMeta();
		String buffId = id + slot;
		data.addTrigger(id + "-rifts", Trigger.CREATE_RIFT, (pdata, in) -> {
			if (rifts.addCount(1) >= riftsPerStack) {
				rifts.setCount(0);
				stacks.addCount(1);
				MASS_BURST.play(data.getPlayer(), data.getPlayer().getLocation());
				STACK_SOUND.play(data.getPlayer(), data.getPlayer());
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id + "-damage", Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			if (stacks.getCount() <= 0 || !ev.getMeta().hasOrigin(DamageOrigin.RIFT)) return TriggerResult.keep();
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.ALL),
					Buff.multiplier(data, damageMultiplier * stacks.getCount(),
							BuffStatTracker.damageBuffAlly(buffId, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HEAVY_CORE,
				GlossaryTag.POWER.tag(this) + ". Every " + DescUtil.val(riftsPerStack) + " "
				+ GlossaryTag.RIFT.tagPlural(this) + " created permanently increases "
				+ GlossaryTag.RIFT.tag(this) + " damage by " + DescUtil.val(damageIncrease + "%") + ".");
	}
}
