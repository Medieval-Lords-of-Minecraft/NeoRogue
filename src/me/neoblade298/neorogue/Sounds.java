package me.neoblade298.neorogue;

import org.bukkit.Sound;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;

public class Sounds {
	public static final SoundContainer explode = new SoundContainer(Sound.ENTITY_GENERIC_EXPLODE),
			anvil = new SoundContainer(Sound.BLOCK_ANVIL_LAND),
			roar = new SoundContainer(Sound.ENTITY_ENDER_DRAGON_AMBIENT),
			crit = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT),
			block = new SoundContainer(Sound.ITEM_SHIELD_BLOCK),
			breaks = new SoundContainer(Sound.ITEM_SHIELD_BREAK),
			blazeDeath = new SoundContainer(Sound.ENTITY_BLAZE_DEATH),
			attackSweep = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP),
			equip = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN),
			infect = new SoundContainer(Sound.ENTITY_ZOMBIE_INFECT),
			jump = new SoundContainer(Sound.ENTITY_SHULKER_SHOOT),
			shoot = new SoundContainer(Sound.ENTITY_ARROW_SHOOT),
			success = new SoundContainer(Sound.ENTITY_ARROW_HIT_PLAYER),
			fire = new SoundContainer(Sound.ENTITY_BLAZE_SHOOT),
			levelup = new SoundContainer(Sound.ENTITY_PLAYER_LEVELUP),
			error = new SoundContainer(Sound.BLOCK_NOTE_BLOCK_BASS, 0.7F),
			flap = new SoundContainer(Sound.ENTITY_ENDER_DRAGON_FLAP),
			threw = new SoundContainer(Sound.ENTITY_SNOWBALL_THROW),
			turnPage = new SoundContainer(Sound.ITEM_BOOK_PAGE_TURN),
			enchant = new SoundContainer(Sound.BLOCK_ENCHANTMENT_TABLE_USE),
			firework = new SoundContainer(Sound.ENTITY_FIREWORK_ROCKET_BLAST),
			water = new SoundContainer(Sound.ENTITY_GENERIC_SWIM),
			extinguish = new SoundContainer(Sound.BLOCK_FIRE_EXTINGUISH),
			ice = new SoundContainer(Sound.BLOCK_GLASS_BREAK),
			teleport = new SoundContainer(Sound.ENTITY_ENDERMAN_TELEPORT);
}
