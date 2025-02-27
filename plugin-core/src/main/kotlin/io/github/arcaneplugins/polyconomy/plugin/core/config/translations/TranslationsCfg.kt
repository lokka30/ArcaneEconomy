package io.github.arcaneplugins.polyconomy.plugin.core.config.translations

import io.github.arcaneplugins.polyconomy.plugin.core.Platform
import io.github.arcaneplugins.polyconomy.plugin.core.config.Config
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@OptIn(ExperimentalPathApi::class)
class TranslationsCfg(
    plugin: Platform,
) : Config(
    plugin = plugin,
    name = "Translations",
    resourcePath = Path("translations.yml")
) {

    override fun load() {
        read()
    }

}