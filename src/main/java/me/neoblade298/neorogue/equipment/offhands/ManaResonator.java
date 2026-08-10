package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class ManaResonator extends Equipment {
	private static final String ID = "ManaResonator";
	private static final int ELECTRIFIED = 4;
	private static final ParticleContainer activeTrail = new ParticleContainer(Particle.FIREWORK)
			.count(2).spread(0.1, 0.1).speed(0).offsetY(1);
	private static final SoundContainer castSound = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.7F, 1.4F);
	private static final SoundContainer damageSound = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7F, 1.6F);
	private int damage;

	public ManaResonator(boolean isUpgraded) {
		super(ID, "Mana Resonator", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(10, 10, 10, 0));
		damage = isUpgraded ? 150 : 100;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta charged = new ActionMeta();
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			charged.setBool(true);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1));
			castSound.play(p, p);
			return TriggerResult.keep();
		}));
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (!charged.getBool()) return TriggerResult.keep();
			Player p = data.getPlayer();
			activeTrail.play(p, p.getLocation());
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (!charged.getBool()) return TriggerResult.keep();
			charged.setBool(false);
			Player p = data.getPlayer();
			p.removePotionEffect(PotionEffectType.SPEED);
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.LIGHTNING,
					DamageStatTracker.of(id + slot, this)));
			FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, ELECTRIFIED, -1, this);
			damageSound.play(p, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHTNING_ROD, "Right click to gain " + DescUtil.white("Speed 2")
				+ " until your next basic attack, which gains " + GlossaryTag.LIGHTNING.tag(this, damage)
				+ " damage and applies " + GlossaryTag.ELECTRIFIED.tag(this, ELECTRIFIED) + ".");
	}
}