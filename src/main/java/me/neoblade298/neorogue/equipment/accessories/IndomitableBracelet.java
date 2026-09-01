package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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
import me.neoblade298.neorogue.session.fight.trigger.event.ShieldsEvent;

public class IndomitableBracelet extends Equipment {
	private static final String ID = "IndomitableBracelet";
	private static final int DIRECT_DAMAGE_REDUCTION = 2;
	private static final int STATUS_GAIN = 1;
	private static final ParticleContainer PROTECT_PROC = new ParticleContainer(Particle.DUST).count(12)
			.spread(0.35, 0.55).speed(0.01).offsetY(1)
			.dustOptions(new DustOptions(Color.fromRGB(235, 190, 95), 1F));
	private static final ParticleContainer SHELL_PROC = new ParticleContainer(Particle.DUST).count(12)
			.spread(0.35, 0.55).speed(0.01).offsetY(1)
			.dustOptions(new DustOptions(Color.fromRGB(105, 190, 220), 1F));
	private static final SoundContainer STATUS_SOUND = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN, 0.45F, 1.2F);
	private final int shieldThreshold;

	public IndomitableBracelet(boolean isUpgraded) {
		super(ID, "Indomitable Bracelet", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ACCESSORY, EquipmentProperties.none());
		shieldThreshold = isUpgraded ? 40 : 50;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.increase(data,
				DIRECT_DAMAGE_REDUCTION, StatTracker.defenseBuffAlly(id + slot, this)));

		ActionMeta progress = new ActionMeta();
		data.addTrigger(id, Trigger.GRANT_SHIELDS, (pdata, in) -> {
			ShieldsEvent event = (ShieldsEvent) in;
			double amount = event.getShield().getTotal();
			if (amount <= 0) return TriggerResult.keep();

			progress.addDouble(amount);
			int activations = (int) (progress.getDouble() / shieldThreshold);
			progress.setDouble(progress.getDouble() % shieldThreshold);
			StatusType lastType = null;
			for (int i = 0; i < activations; i++) {
				StatusType type = progress.getBool() ? StatusType.SHELL : StatusType.PROTECT;
				data.applyStatus(type, data, STATUS_GAIN, -1, this);
				progress.toggleBool();
				lastType = type;
			}
			if (lastType != null) {
				Player player = data.getPlayer();
				(lastType == StatusType.PROTECT ? PROTECT_PROC : SHELL_PROC).play(player, player);
				STATUS_SOUND.play(player, player);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET,
				"Decrease " + GlossaryTag.DIRECT.tag(this) + " damage taken by "
						+ DescUtil.val(DIRECT_DAMAGE_REDUCTION) + ". For every "
						+ GlossaryTag.SHIELDS.tag(this, shieldThreshold) + " you apply, gain "
						+ GlossaryTag.PROTECT.tag(this, STATUS_GAIN) + " or "
						+ GlossaryTag.SHELL.tag(this, STATUS_GAIN) + ", alternating between them.");
	}
}