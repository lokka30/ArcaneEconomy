package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper.PtAccountAccessor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper.PtCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper.TreasuryUtil.convertNamespacedKeyFromTreasury
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper.TreasuryUtil.convertNamespacedKeyToTreasury
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageHandler
import kotlinx.coroutines.runBlocking
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.economy.EconomyProvider
import me.lokka30.treasury.api.economy.account.AccountData
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.currency.Currency
import java.math.BigDecimal
import java.util.*
import java.util.Locale.getDefault
import java.util.concurrent.CompletableFuture
import kotlin.streams.toList

class TreasuryEconomyProvider(
    val plugin: Polyconomy,
) : EconomyProvider {

    val accountAccessor = PtAccountAccessor(plugin, this)

    fun storageHandler(): StorageHandler {
        return plugin.storageManager.currentHandler!!
    }

    suspend fun getPolyCurrency(currency: Currency): io.github.arcaneplugins.polyconomy.api.currency.Currency? {
        return storageHandler().getCurrency(currency.identifier)
    }

    override fun accountAccessor(): AccountAccessor = accountAccessor

    override fun hasAccount(accountData: AccountData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync runBlocking {
                return@runBlocking if (accountData.isPlayerAccount) {
                    storageHandler()
                        .hasPlayerAccount(accountData.playerIdentifier.get())
                } else {
                    storageHandler()
                        .hasNonPlayerAccount(convertNamespacedKeyFromTreasury(accountData.nonPlayerIdentifier.get()))
                }
            }
        }
    }

    override fun retrievePlayerAccountIds(): CompletableFuture<Collection<UUID>> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync runBlocking {
                return@runBlocking storageHandler()
                    .getPlayerAccountIds()
            }
        }
    }

    override fun retrieveNonPlayerAccountIds(): CompletableFuture<Collection<NamespacedKey>> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync runBlocking {
                return@runBlocking storageHandler()
                    .getNonPlayerAccountIds()
                    .stream()
                    .map(::convertNamespacedKeyToTreasury)
                    .toList()
            }
        }
    }

    override fun getPrimaryCurrency(): Currency {
        return PtCurrency(
            currency = runBlocking {
                storageHandler().getPrimaryCurrency()
            }
        )
    }

    override fun findCurrency(identifier: String): Optional<Currency> {
        val currency = runBlocking {
            return@runBlocking storageHandler().getCurrency(identifier)
        }

        return if (currency == null) {
            Optional.empty()
        } else {
            Optional.of(PtCurrency(currency = currency))
        }
    }

    override fun getCurrencies(): Set<Currency> {
        return runBlocking {
            return@runBlocking storageHandler()
                .getCurrencies()
                .map { PtCurrency(currency = it) }
                .toSet()
        }
    }

    override fun registerCurrency(currency: Currency): CompletableFuture<TriState> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync runBlocking {
                if (getPolyCurrency(currency) != null) {
                    // already registered
                    return@runBlocking TriState.UNSPECIFIED
                }

                storageHandler().registerCurrency(
                    name = currency.identifier,
                    amountFormat = "#,##0.00",
                    conversionRate = currency.conversionRate,
                    decimalLocaleMap = currency.localeDecimalMap.mapValues { it.value.toString() }.toMap(),
                    displayNamePluralLocaleMap = mapOf(
                        getDefault() to currency.getDisplayName(
                            BigDecimal.TEN,
                            getDefault()
                        )
                    ),
                    displayNameSingularLocaleMap = mapOf(
                        getDefault() to currency.getDisplayName(
                            BigDecimal.ONE,
                            getDefault()
                        )
                    ),
                    presentationFormat = "%symbol%%amount%",
                    startingBalance = BigDecimal.ZERO,
                    symbol = currency.symbol,
                )

                return@runBlocking TriState.TRUE
            }
        }
    }

    override fun unregisterCurrency(currency: Currency): CompletableFuture<TriState> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync runBlocking {
                val polyCurr = getPolyCurrency(currency)

                return@runBlocking if (polyCurr == null) {
                    // already unregistered
                    TriState.FALSE
                } else {
                    storageHandler().unregisterCurrency(polyCurr)
                    TriState.TRUE
                }
            }
        }
    }
}