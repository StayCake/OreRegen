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
            usetool(p.inventory.itemInMainHand,p)
            getareas().set("mode.${p.uniqueId}.pos1", tbl)
            p.msg("${tbl.x},${tbl.y},${tbl.z} 이 1번 위치입니다.")
        }
        val a = getareas().section("areas")?.keys
        a?.forEach { key ->
            var preset = getareas().getString("areas.$key.preset")
            if (preset == null) preset = "default"
            val b = getPreset().section(preset)?.keys
            @Suppress("UNCHECKED_CAST")
            val loclist = getareas().getList("areas.$key.pos") as List<Location>
            val pos1 = loclist[0]
            val pos2 = loclist[1]
            val px = listOf(pos1.blockX, pos2.blockX).sorted()
            val py = listOf(pos1.blockY, pos2.blockY).sorted()
            val pz = listOf(pos1.blockZ, pos2.blockZ).sorted()
            if (e.block.x in px[0]..px[1] && e.block.y in py[0]..py[1] && e.block.z in pz[0]..pz[1]) {
                val rand = (Math.random() * 100)/1.00.nextDown()
                if (getInstance().config.getBoolean("debug")) println("Area [$key] : $rand | Preset : $preset")
                var block : Material = Material.AIR
                var total = 0.0
                var checked = true
                b?.forEach { keys ->
                    val v = getPreset().getDouble("$preset.$keys")
                    val final = total + v
                    if (getInstance().config.getBoolean("debug")) println("$keys : ${rand in total.rangeTo(final)} ($total..${final})")
                    if (rand in total.rangeTo(final) && checked) {
                        block = Material.matchMaterial(keys) ?: Material.AIR
                        checked = false
                    }
                    total += v
                }
                e.isCancelled = true
                if (e.player.gameMode != GameMode.CREATIVE) {
                    p.giveExp(e.expToDrop,true)
                    e.block.getDrops(e.player.inventory.itemInMainHand,e.player).forEach {
                        e.block.world.dropItemNaturally(e.block.location,it)
                    }
                    when {
                        pickaxe.contains(p.inventory.itemInOffHand.type) -> {
                            usetool(p.inventory.itemInOffHand,p)
                        }
                        pickaxe.contains(p.inventory.itemInMainHand.type) -> {
                            usetool(p.inventory.itemInMainHand,p)
                        }
                    }
                }
                e.block.type = block
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