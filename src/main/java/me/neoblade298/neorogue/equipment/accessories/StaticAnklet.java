package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class StaticAnklet extends Equipment {
	private static final String ID = "StaticAnklet";
	private static final double LIGHTNING_MULTIPLIER = 0.1;
	private static final ParticleContainer MANA_SPARKS = new ParticleContainer(Particle.FIREWORK)
			.count(5).spread(0.1, 0.25).speed(0.01).offsetY(0.35);
	private static final SoundContainer MANA_SOUND = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.45F, 1.7F);
	private final int mana;

	public StaticAnklet(boolean isUpgraded) {
		super(ID, "Static Anklet", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		mana = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDamageBuff(DamageBuffType.of(DamageCategory.LIGHTNING), Buff.multiplier(data, LIGHTNING_MULTIPLIER,
				BuffStatTracker.damageBuffAlly(id + slot, this)));
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (event.isStatus(StatusType.ELECTRIFIED)) {
				data.addMana(mana);
				Player player = data.getPlayer();
				MANA_SPARKS.play(player, player.getLocation());
				MANA_SOUND.play(player, player);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TRIPWIRE_HOOK, "Increase " + GlossaryTag.LIGHTNING.tag(this) + " damage by "
				+ DescUtil.white("10%") + ". Applying " + GlossaryTag.ELECTRIFIED.tag(this)
				+ " grants " + DescUtil.yellow(mana) + " mana.");
	}
}