package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class MasterTheElements extends Equipment implements Power {
	private static final String ID = "MasterTheElements";
	private static final ParticleContainer ACTIVATION = new ParticleContainer(Particle.FIREWORK)
			.count(12).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private static final ParticleContainer SHIFT = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(120, 225, 255), 1F))
			.count(5).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private static final SoundContainer ACTIVATION_SOUND = new SoundContainer(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.65F, 1.35F);
	private int requiredTypes, intellect;

	public MasterTheElements(boolean isUpgraded) {
		super(ID, "Master the Elements", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		requiredTypes = 3;
		intellect = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		HashSet<DamageType> types = new HashSet<>();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
			for (DamageSlice slice : ev.getMeta().getSlices()) types.add(slice.getPostBuffType());
			if (types.size() < requiredTypes) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ACTIVATION.play(data.getPlayer(), data.getPlayer().getLocation());
		ACTIVATION_SOUND.play(data.getPlayer(), data.getPlayer());
		ActionMeta previous = new ActionMeta();
		data.addTrigger(id + "-active", Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (ev.getMeta().getSlices().isEmpty()) return TriggerResult.keep();
			DamageType current = ev.getMeta().getSlices().getFirst().getPostBuffType();
			DamageType last = (DamageType) previous.getObject();
			if (last != null && current != last) {
				data.applyStatus(StatusType.INTELLECT, data, intellect, -1, this);
				SHIFT.play(data.getPlayer(), data.getPlayer().getLocation());
			}
			previous.setObject(current);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing at least "
				+ DescUtil.val(requiredTypes) + " damage types. Whenever your damage type differs from the previous one, gain "
				+ GlossaryTag.INTELLECT.tag(this, intellect) + ".");
	}
}
