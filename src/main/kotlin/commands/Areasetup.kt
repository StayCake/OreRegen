package commands

import Main
import hazae41.minecraft.kutils.bukkit.msg
import io.github.monun.kommand.getValue
import io.github.monun.kommand.node.LiteralNode
import org.bukkit.Sound
import org.bukkit.configuration.file.YamlConfiguration
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

    fun register(builder: LiteralNode) {
        builder.requires { playerOrNull != null }
        builder.then("reload") {
            executes {
                val p = player
                getInstance().reloadConfig()
                if (getareasloc().canRead()) {
                    getareas().load(getareasloc())
                }
                p.sendMessage("리로드 완료!")
                p.playSound(p.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F)
            }
        }
        builder.then("debug") {
            executes {
                val p = player
                val t = getInstance().config.getBoolean("debug")
                getInstance().config.set("debug",!t)
                getInstance().saveConfig()
                p.msg("debug : ${!t}")
            }
        }
        builder.then("delete") {
            then("name" to string()) {
                executes { ctx ->
                    val name : String by ctx
                    val p = player
                    getareas().set("areas.$name",null)
                    getareas().save(getareasloc())
                    p.msg("구역 $name 삭제 완료!")
                }
            }
            executes {
                (player).msg("대상을 입력하세요.")
            }
        }
        builder.then("create") {
            then("name" to string()) {
                executes { ctx ->
                    val name : String by ctx
                    val p = player
                    val loc1 = getareas().getLocation("mode.${p.uniqueId}.pos1")
                    val loc2 = getareas().getLocation("mode.${p.uniqueId}.pos2")
                    if (loc1 != null && loc2 != null) {
                        if (loc1.world != loc2.world) {
                            p.msg("적어도 같은 월드에서 두 지점을 찍어주세요!")
                        } else {
                            getareas().set(
                                "areas.$name", listOf(loc1, loc2)
                            )
                            getareas().save(getareasloc())
                            p.msg("구역 $name 지정 완료!")
                        }
                    } else {
                        p.msg("적어도 두 지점을 찍어주세요!")
                    }
                }
            }
            executes {
                (player).msg("대상을 입력하세요.")
            }
        }
        builder.executes {
            val p = player
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