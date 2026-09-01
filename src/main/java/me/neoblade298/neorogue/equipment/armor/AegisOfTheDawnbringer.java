package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class AegisOfTheDawnbringer extends Equipment {
	private static final String ID = "AegisOfTheDawnbringer";
	private static final int MAGICAL_REDUCTION = 4;
	private static final int SANCTIFIED_THRESHOLD = 200;
	private static final int HEAL = 20;
	private static final Circle DAWN_RING = new Circle(1.6);
	private static final ParticleContainer DAWN = new ParticleContainer(Particle.DUST).count(1).spread(0, 0).speed(0)
			.dustOptions(new DustOptions(Color.fromRGB(255, 235, 145), 1.1F));
	private static final ParticleContainer DAWN_SPARK = new ParticleContainer(Particle.FIREWORK).count(12)
			.spread(0.1, 0.1).offsetY(1).speed(0.01);
	private int shields;

	public AegisOfTheDawnbringer(boolean isUpgraded) {
		super(ID, "Aegis of the Dawnbringer", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ARMOR);
		shields = isUpgraded ? 150 : 100;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, MAGICAL_REDUCTION,
				StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		ActionMeta applied = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (applied.getBool() || !ev.getStatusId().equals(StatusType.SANCTIFIED.name())) {
				return TriggerResult.keep();
			}
			if (applied.addCount(ev.getStacks()) < SANCTIFIED_THRESHOLD) return TriggerResult.keep();

			applied.setBool(true);
			Player p = data.getPlayer();
			data.addPermanentShield(p.getUniqueId(), shields, this);
			FightInstance.giveHeal(p, HEAL, this, p);
			DAWN_RING.play(DAWN, p.getLocation(), LocalAxes.xz(), null);
			DAWN_SPARK.play(p, p);
			Sounds.levelup.play(p, p);
			Sounds.block.play(p, p);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_CHESTPLATE,
				GlossaryTag.PASSIVE.tag(this) + ". Reduce " + GlossaryTag.MAGICAL.tag(this)
						+ " damage taken by " + DescUtil.val(MAGICAL_REDUCTION) + ". Once you have applied "
						+ GlossaryTag.SANCTIFIED.tag(this, SANCTIFIED_THRESHOLD) + " this fight, gain "
						+ GlossaryTag.SHIELDS.tag(this, shields) + " permanently and heal " + DescUtil.val(HEAL) + ".");
	}
}