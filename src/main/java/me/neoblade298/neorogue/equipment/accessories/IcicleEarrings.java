package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class IcicleEarrings extends Equipment {
	private static final String ID = "IcicleEarrings";
	private static final Circle SHIELD_RING = new Circle(1.4);
	private static final ParticleContainer SHIELD_PARTICLE = new ParticleContainer(Particle.DUST).count(1)
			.spread(0, 0).speed(0).dustOptions(new DustOptions(Color.fromRGB(125, 220, 255), 1.1F));
	private static final ParticleContainer ICE_SPARK = new ParticleContainer(Particle.SNOWFLAKE).count(10)
			.spread(0.1, 0.1).offsetY(1).speed(0.01);
	private int shields;

	public IcicleEarrings(boolean isUpgraded) {
		super(ID, "Icicle Earrings", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
		shields = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta activated = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent event = (ApplyStatusEvent) in;
			if (activated.getBool() || !event.isStatus(StatusType.FROST)) return TriggerResult.keep();

			activated.setBool(true);
			Player player = data.getPlayer();
			data.addPermanentShield(player.getUniqueId(), shields, this);
			SHIELD_RING.play(SHIELD_PARTICLE, player.getLocation(), LocalAxes.xz(), null);
			ICE_SPARK.play(player, player);
			Sounds.block.play(player, player);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS, "The first time you apply "
				+ GlossaryTag.FROST.tag(this) + " each fight, gain " + GlossaryTag.SHIELDS.tag(this, shields) + ".");
	}
}