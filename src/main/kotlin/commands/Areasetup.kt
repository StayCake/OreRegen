package commands

import Main
import com.github.monun.kommand.KommandBuilder
import com.github.monun.kommand.argument.string
import hazae41.minecraft.kutils.bukkit.msg
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File

object Areasetup {

    private fun getareas() : YamlConfiguration {
        return Main.areas
    }

    private fun getareasloc() : File {
        return Main.areasloc
    }

    private fun getInstance() : Plugin {
        return Main.instance
    }

    fun register(builder: KommandBuilder) {
        builder.apply {
            require { sender -> sender is Player }
            then("reload") {
                executes {
                    val p = it.sender as Player
                    getInstance().reloadConfig()
                    if (getareasloc().canRead()) {
                        getareas().load(getareasloc())
                    }
                    p.sendMessage("리로드 완료!")
                    p.playSound(p.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F)
                }
            }
            then("debug") {
                executes {
                    val p = it.sender as Player
                    val t = getInstance().config.getBoolean("debug")
                    getInstance().config.set("debug",!t)
                    getInstance().saveConfig()
                    p.msg("debug : ${!t}")
                }
            }
            then("delete") {
                then("name" to string()) {
                    executes {
                        val p = it.sender as Player
                        getareas().set("areas.${it.getArgument("name")}",null)
                        getareas().save(getareasloc())
                        p.msg("구역 ${it.getArgument("name")} 삭제 완료!")
                    }
                }
                executes {
                    (it.sender as Player).msg("대상을 입력하세요.")
                }
            }
            then("create") {
                then("name" to string()) {
                    executes {
                        val p = it.sender as Player
                        val loc1 = getareas().getLocation("mode.${p.uniqueId}.pos1")
                        val loc2 = getareas().getLocation("mode.${p.uniqueId}.pos2")
                        if (loc1 != null && loc2 != null) {
                            if (loc1.world != loc2.world) {
                                p.msg("적어도 같은 월드에서 두 지점을 찍어주세요!")
                            } else {
                                getareas().set(
                                    "areas.${it.getArgument("name")}", listOf(loc1, loc2)
                                )
                                getareas().save(getareasloc())
                                p.msg("구역 ${it.getArgument("name")} 지정 완료!")
                            }
                        } else {
                            p.msg("적어도 두 지점을 찍어주세요!")
                        }
                    }
                }
                executes {
                    (it.sender as Player).msg("대상을 입력하세요.")
                }
            }
            executes {
                val p = it.sender as Player
                val setup = getareas().getBoolean("mode.${p.uniqueId}.setup")
                getareas().set("mode.${p.uniqueId}.setup",!setup)
                getareas().save(getareasloc())
                when (setup) {
                    true -> {
                        p.msg("구역 설정 모드 꺼짐")
                    }
                    false -> {
                        p.msg("구역 설정 모드 켜짐")
                    }
                }
            }
        }
    }
}