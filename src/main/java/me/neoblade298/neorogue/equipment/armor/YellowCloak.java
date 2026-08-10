package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
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
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class YellowCloak extends Equipment {
	private static final String ID = "YellowCloak";
	private static final int BASE_REDUCTION = 1, DURATION_TICKS = 100;
	private static final ParticleContainer MITIGATION = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(245, 210, 70), 1F))
			.count(8).spread(0.3, 0.7).speed(0.01).offsetY(0.8);
	private static final SoundContainer MITIGATION_SOUND = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.55F, 1.25F);
	private final int electrifiedReduction;

	public YellowCloak(boolean isUpgraded) {
		super(ID, "Yellow Cloak", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.ARMOR);
		electrifiedReduction = isUpgraded ? 2 : 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		String baseBuffId = id + slot + "-base";
		String electrifiedBuffId = id + slot + "-electrified";
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, BASE_REDUCTION,
				StatTracker.defenseBuffAlly(baseBuffId, this)));
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data, electrifiedReduction,
					StatTracker.defenseBuffAlly(electrifiedBuffId, this)), DURATION_TICKS);
			Player player = data.getPlayer();
			MITIGATION.play(player, player.getLocation());
			MITIGATION_SOUND.play(player, player);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Reduce incoming " + GlossaryTag.DIRECT.tag(this)
				+ " damage by " + DescUtil.white(BASE_REDUCTION) + ". Applying "
				+ GlossaryTag.ELECTRIFIED.tag(this) + " further reduces it by "
				+ DescUtil.yellow(electrifiedReduction) + " " + DescUtil.white("[5s]") + ".");
	}
}