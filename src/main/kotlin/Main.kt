import commands.Areasetup
import hazae41.minecraft.kutils.get
import io.github.monun.kommand.kommand
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class Main : JavaPlugin() {

    companion object {
        lateinit var instance: Plugin
            private set
        lateinit var areas: YamlConfiguration
            private set
        lateinit var areasloc: File
            private set
    }

    override fun onEnable() {

        println(String.format("[%s] - 가동 시작!", description.name))

        server.pluginManager.registerEvents(Events(), this)

        saveDefaultConfig()

        instance = this
        areas = YamlConfiguration.loadConfiguration(dataFolder["areas.yml"])
        areasloc = dataFolder["areas.yml"]

        kommand {
            register("areasetup") {
                Areasetup.register(this)
            }
        }
    }

    override fun onDisable() {
        saveConfig()
        println(String.format("[%s] - 가동 중지.", description.name))

    }
}