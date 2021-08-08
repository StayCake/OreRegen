import hazae41.minecraft.kutils.bukkit.keys
import hazae41.minecraft.kutils.bukkit.msg
import hazae41.minecraft.kutils.bukkit.section
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPistonEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.math.nextDown

class Events : Listener {
    private val pickaxe = listOf(
        Material.WOODEN_PICKAXE,
        Material.STONE_PICKAXE,
        Material.IRON_PICKAXE,
        Material.GOLDEN_PICKAXE,
        Material.DIAMOND_PICKAXE,
        Material.NETHERITE_PICKAXE
    )

    private fun getareas() : YamlConfiguration {
        return Main.areas
    }
    private fun getInstance() : Plugin {
        return Main.instance
    }
    private fun getPreset() : YamlConfiguration {
        return Main.genpreset
    }

    private fun usetool(mh: ItemStack,p: Player) {
        val unbreaking = mh.getEnchantmentLevel(Enchantment.DURABILITY)
        if (Math.random() * 100 <= 100/(unbreaking + 1)) {
            mh.itemMeta = mh.itemMeta.apply {
                if (this is Damageable) {
                    damage += 1
                    if (mh.type.maxDurability <= damage) {
                        p.inventory.setItemInMainHand(ItemStack(Material.AIR))
                        p.playSound(p.location, Sound.ENTITY_ITEM_BREAK,1F,1F)
                        return
                    }
                }
            }
        }
    }

    @EventHandler
    private fun act1(e: BlockBreakEvent) {
        val p = e.player
        val tbl = e.block.location
        if (getareas().getBoolean("mode.${p.uniqueId}.setup")) {
            e.isCancelled = true
            getareas().set("mode.${p.uniqueId}.pos1", tbl)
            p.msg("${tbl.x},${tbl.y},${tbl.z} 이 1번 위치입니다.")
        }
        val a = getareas().section("areas")?.keys
        a?.forEach { key ->
            @Suppress("UNCHECKED_CAST")
            val loclist = getareas().getList("areas.$key.pos") as List<Location>
            val pos = listOf(loclist[0],loclist[1])
            val px = listOf(pos[0].blockX, pos[1].blockX).sorted()
            val py = listOf(pos[0].blockY, pos[1].blockY).sorted()
            val pz = listOf(pos[0].blockZ, pos[1].blockZ).sorted()
            if (e.block.x in px[0]..px[1] && e.block.y in py[0]..py[1] && e.block.z in pz[0]..pz[1]) {
                val rand = (Math.random() * 100)/1.00.nextDown()
                val preset = getareas().getString("areas.$key.preset") ?: "default"
                val b = getPreset().section(preset)?.keys
                if (getInstance().config.getBoolean("debug")) println("Area [$key] : $rand | Preset : $preset")
                var block : Material? = null
                var total = 0.0
                b?.forEach { keys ->
                    val v = getPreset().getDouble("$preset.$keys")
                    if (getInstance().config.getBoolean("debug")) println("$keys : ${rand in total.rangeTo(total + v)} ($total..${total + v})")
                    if (rand in total.rangeTo(total + v)) block = Material.matchMaterial(keys)
                    total += v
                }
                if (e.player.gameMode != GameMode.CREATIVE) {
                    p.giveExp(e.expToDrop,true)
                    e.block.getDrops(e.player.inventory.itemInMainHand,e.player).forEach {
                        e.block.world.dropItemNaturally(e.block.location,it)
                    }
                    if (pickaxe.contains(p.inventory.itemInOffHand.type)) usetool(p.inventory.itemInOffHand,p)
                    else if (pickaxe.contains(p.inventory.itemInMainHand.type)) usetool(p.inventory.itemInMainHand,p)
                }
                e.isCancelled = true
                if (block != null) e.block.type = block as Material
            }
        }
    }

    @EventHandler
    private fun act2(e: PlayerInteractEvent) {
        val p = e.player
        val clb = e.clickedBlock
        if (clb != null && e.action == Action.RIGHT_CLICK_BLOCK && e.hand == EquipmentSlot.HAND) {
            val tbl = clb.location
            if (getareas().getBoolean("mode.${p.uniqueId}.setup")) {
                e.isCancelled = true
                getareas().set("mode.${p.uniqueId}.pos2",tbl)
                p.msg("${tbl.x},${tbl.y},${tbl.z} 이 2번 위치입니다.")
            }
        }
    }
}