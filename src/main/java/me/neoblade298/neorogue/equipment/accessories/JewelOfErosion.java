package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class JewelOfErosion extends Equipment {
	private static final String ID = "JewelOfErosion";
	private static final int RANGE = 8, CONCUSSED_THRESHOLD = 5;
	private static final TargetProperties TARGETS = TargetProperties.radius(RANGE, false, TargetType.ENEMY);
	private static final Circle EROSION_AREA = new Circle(RANGE);
	private static final ParticleContainer AREA_EDGE = new ParticleContainer(Particle.DUST)
			.dustOptions(new DustOptions(Color.fromRGB(105, 135, 75), 0.8F)).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer EROSION = new ParticleContainer(Particle.BLOCK)
			.blockData(Material.TUFF.createBlockData()).count(6).spread(0.1, 0.1).speed(0.01).offsetY(1);
	private static final SoundContainer EROSION_SOUND = new SoundContainer(Sound.BLOCK_DEEPSLATE_BREAK, 0.55F, 0.8F);
	private final int damage;

	public JewelOfErosion(boolean isUpgraded) {
		super(ID, "Jewel of Erosion", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		damage = isUpgraded ? 30 : 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			Player player = data.getPlayer();
			boolean activated = false;
			for (LivingEntity target : TargetHelper.getEntitiesInRadius(player, TARGETS)) {
				FightData targetData = FightInstance.getFightData(target);
				if (targetData == null || !targetData.hasStatus(StatusType.CONCUSSED)
						|| targetData.getStatus(StatusType.CONCUSSED).getStacks() < CONCUSSED_THRESHOLD) continue;
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.EARTHEN,
						DamageStatTracker.of(id + slot, this)), target);
				EROSION.play(player, target.getLocation());
				activated = true;
			}
			if (activated) {
				EROSION_AREA.play(AREA_EDGE, player.getLocation().add(0, 0.1, 0), LocalAxes.xz(), null);
				EROSION_SOUND.play(player, player);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.EMERALD, "Once per second, enemies within " + DescUtil.val(RANGE)
				+ " blocks with at least " + GlossaryTag.CONCUSSED.tag(this, CONCUSSED_THRESHOLD)
				+ " take " + GlossaryTag.EARTHEN.tag(this, damage) + " damage.");
	}
}