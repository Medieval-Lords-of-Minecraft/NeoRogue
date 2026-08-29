package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class EtherDraw extends Equipment implements Power {
	private static final String ID = "EtherDraw";
	private static final Circle SHIELD_RING = new Circle(1.05);
	private static final ParticleContainer SHIELD_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(80, 155, 255), 1F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer ACTIVATION = new ParticleContainer(Particle.ENCHANT)
			.count(10).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private static final SoundContainer ACTIVATION_SOUND = new SoundContainer(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6F, 1.4F);
	private static final SoundContainer SHIELD_SOUND = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.25F, 1.6F);
	private int manaRequired, shields, durationTicks;

	public EtherDraw(boolean isUpgraded) {
		super(ID, "Ether Draw", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.none());
		manaRequired = 100;
		shields = 3;
		durationTicks = (isUpgraded ? 8 : 6) * 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta spent = new ActionMeta();
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			spent.addDouble(ev.getInstance().getManaCost());
			if (spent.getDouble() < manaRequired) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ACTIVATION.play(data.getPlayer(), data.getPlayer().getLocation());
		ACTIVATION_SOUND.play(data.getPlayer(), data.getPlayer());
		data.addTrigger(id + "-active", Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) return TriggerResult.keep();
			Player p = data.getPlayer();
			data.addSimpleShield(p.getUniqueId(), shields, durationTicks, this);
			SHIELD_RING.play(SHIELD_EDGE, p.getLocation().clone().add(0, 1, 0), LocalAxes.xz(), null);
			SHIELD_SOUND.play(p, p);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LAPIS_LAZULI,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after spending "
				+ DescUtil.val(manaRequired) + " base mana. While active, casting an ability grants "
				+ GlossaryTag.SHIELDS.tag(this, shields) + " ["
				+ DescUtil.val((durationTicks / 20) + "s") + "].");
	}
}
