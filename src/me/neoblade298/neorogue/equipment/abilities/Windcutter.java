package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Windcutter extends Equipment {
	private static final String ID = "windcutter";
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_DEATH, 2F);
	private static final ParticleContainer part = new ParticleContainer(Particle.SWEEP_ATTACK);
	private static final int PROJECTILE_AMOUNT = 5;
	
	private ProjectileGroup projs = new ProjectileGroup();
	private int damage;
	
	public Windcutter(boolean isUpgraded) {
		super(ID, "Windcutter", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 5));
		damage = isUpgraded ? 50 : 30;
		for (int i = 0; i < PROJECTILE_AMOUNT; i++) {
			projs.add(new WindcutterProjectile(i, PROJECTILE_AMOUNT / 2));
		}
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, new WindcutterInstance(id, p, data));
	}
	
	private class WindcutterInstance extends PriorityAction {
		private int count = 0;
		public WindcutterInstance(String id, Player p, PlayerFightData data) {
			super(id);
			action = (pdata, in) -> {
				if (++count >= 3 && (data.getStamina() / data.getMaxStamina()) > 0.5) {
					sound.play(p, p);
					projs.start(data, p.getLocation().add(0, 1, 0), p.getLocation().getDirection().setY(0).normalize());
					data.addStamina(-10);
					count = 0;
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BAMBOO,
				"Passive. When above 50% max stamina, every third basic attack fires five piercing projectiles in a cone that deal " + GlossaryTag.SLASHING.tag(this, damage, true) +
				" damage and costs <white>10</white> stamina. Unaffected by stamina cost reduction.");
	}
	
	private class WindcutterProjectile extends Projectile {
		public WindcutterProjectile(int i, int center) {
			super(0.5, properties.get(PropertyType.RANGE), 2);
			this.size(1, 1).pierce(-1);
			int iter = i - center;
			this.rotation(iter * 25);
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			if (proj.getTick() % 3 == 0) Sounds.flap.play(p, proj.getLocation());
			part.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.SLASHING));
		}
	}
}
