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
        lateinit var genpreset: YamlConfiguration
            private set
        lateinit var genpresetloc: File
            private set
    }

    override fun onEnable() {

        println(String.format("[%s] - 가동 시작!", description.name))

        server.pluginManager.registerEvents(Events(), this)

        saveDefaultConfig()

        instance = this
        areasloc = dataFolder["areas.yml"]
        areas = YamlConfiguration.loadConfiguration(areasloc)
        if (!areasloc.canRead()) areas.save(areasloc)

        genpresetloc = dataFolder["presets.yml"]
        genpreset = YamlConfiguration.loadConfiguration(genpresetloc)
        val t = getResource("presets.yml")
        if (t != null) {
            val def = YamlConfiguration.loadConfiguration(t.reader())
            if (!genpresetloc.canRead()) def.save(genpresetloc)
            t.close()
        } else {
            println(String.format("[%s] - 플러그인 구성 파일 오류! 개발자에게 문의하세요.", description.name))
            server.pluginManager.disablePlugin(this)
        }

        kommand {
            register("oreregen","rga") {
                Areasetup.register(this)
            }
        }
    }

    override fun onDisable() {
        saveConfig()
        println(String.format("[%s] - 가동 중지.", description.name))

    }
}