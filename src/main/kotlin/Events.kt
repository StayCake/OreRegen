import hazae41.minecraft.kutils.bukkit.keys
import hazae41.minecraft.kutils.bukkit.msg
import hazae41.minecraft.kutils.bukkit.section
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.math.nextDown

class Events : Listener {

    private fun getareas() : YamlConfiguration {
        return Main.areas
    }
    private fun getInstance() : Plugin {
        return Main.instance
    }
    private fun getPreset() : YamlConfiguration {
        return Main.genpreset
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
                e.block.getDrops(e.player.inventory.itemInMainHand,e.player).forEach {
                    e.block.world.dropItemNaturally(e.block.location,it)
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