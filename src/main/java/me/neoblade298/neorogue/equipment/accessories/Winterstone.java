package me.neoblade298.neorogue.equipment.accessories;

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
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Winterstone extends Equipment {
	private static final String ID = "Winterstone";
	private static final int FROST_THRESHOLD = 10, COOLDOWN_REDUCTION = 1;
	private static final ParticleContainer PROC = new ParticleContainer(Particle.SNOWFLAKE).count(8)
			.spread(0.35, 0.5).offsetY(1).speed(0.01);
	private int mana;

	public Winterstone(boolean isUpgraded) {
		super(ID, "Winterstone", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		mana = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta frostApplied = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (!event.isStatus(StatusType.FROST)) return TriggerResult.keep();

			int total = frostApplied.getCount() + event.getStacks();
			int activations = total / FROST_THRESHOLD;
			frostApplied.setCount(total % FROST_THRESHOLD);
			if (activations == 0) return TriggerResult.keep();

			data.addMana(mana * activations);
			for (EquipmentInstance instance : data.getActiveEquipment().values()) {
				instance.addCooldown(-COOLDOWN_REDUCTION * activations);
			}
			Player player = data.getPlayer();
			PROC.play(player, player);
			Sounds.enchant.play(player, player);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PACKED_ICE, GlossaryTag.PASSIVE.tag(this) + ". For every "
				+ GlossaryTag.FROST.tag(this, FROST_THRESHOLD) + " you apply, gain " + DescUtil.val(mana)
				+ " mana and reduce all ability cooldowns by " + DescUtil.val(COOLDOWN_REDUCTION + "s") + ".");
	}
}