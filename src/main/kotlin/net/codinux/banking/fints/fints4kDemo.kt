package net.codinux.banking.fints

import net.dankito.banking.client.model.parameter.TransferMoneyParameter
import net.dankito.banking.client.model.response.GetAccountDataResponse
import net.dankito.banking.fints.FinTsClient
import net.dankito.banking.fints.callback.SimpleFinTsClientCallback
import net.dankito.banking.fints.getAccountData
import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.transferMoney
import net.dankito.banking.fints.util.toBigDecimal
import kotlin.system.exitProcess


fun main(args: Array<String>) {
  fints4kDemo().run()
}


class fints4kDemo {

  // TODO: set your bank code (BLZ), login name (Online Banking Nutzernamen) and password here

  private val bankCode = ""

  private val loginName = ""

  private val password = ""


  private val callback = SimpleFinTsClientCallback( { tanChallenge -> handleTanRequired(tanChallenge) }) { supportedTanMethods, suggestedTanMethod -> selectTanMethod(supportedTanMethods, suggestedTanMethod) }

  private fun handleTanRequired(tanChallenge: TanChallenge) {
    // if a TAN is requested, enter the TAN here. For information what to do see parameter tanChallenge
    // tanChallenge.userEnteredTan("")

    tanChallenge.userDidNotEnterTan() // action will be aborted then
  }

  private fun selectTanMethod(supportedTanMethods: List<TanMethod>, suggestedTanMethod: TanMethod?): TanMethod? {
    // select your TAN method here by choosing one from supportedTanMethods. In most cases suggestedTanMethod should be a good choice
    return suggestedTanMethod
  }


  private val client = FinTsClient(callback) // uses the callback defined above; if you don't want to handle the callback methods simply use: val client = FinTsClient(SimpleFinTsClientCallback())


  fun run() {
    // gets account data (like bank accounts etc.), the balance (Saldo) and account transactions (Kontoumsätze) of last 90 days as most banks don't
    // afford a TAN for transactions of last 90 days
    val accountDataResponse = client.getAccountData(bankCode, loginName, password)

    logRetrievedAccountData(accountDataResponse)
  }


  fun transferMoneyDemo() {
    // transfer money - make a donation to Médecins Sans Frontières of 10 Euros
    val response = client.transferMoney(bankCode, loginName, password, "Ärzte ohne Grenzen e.V.", "DE72 3702 0500 0009 7097 00", Money("10.00", "EUR"), "Spende")
    // then check response object if transfer succeeded or not

    // for non-German receiver banks the BIC is required
//    val paramBic = TransferMoneyParameter(bankCode, loginName, password, null, "Médecins Sans Frontières", "BE73 0000 0000 6060", "BPOTBEB1", Money("10.00", "EUR"), "Donation")
//    client.transferMoney(paramBic)

    // if you have more than one bank account that supports money transfer you either have to specify which bank account to use ...
//    val paramSpecifyAccount = TransferMoneyParameter(bankCode, loginName, password, BankAccountIdentifierImpl("Kontonummer", null, "IBAN"), "Ärzte ohne Grenzen e.V.", "DE72 3702 0500 0009 7097 00", null, Money("10.00", "EUR"), "Donation")
//    client.transferMoney(paramSpecifyAccount)

    // ... or set the selectAccount callback and choose which bank account to use there:
//    val paramSelectAccount = TransferMoneyParameter(bankCode, loginName, password, null, "Ärzte ohne Grenzen e.V.", "DE72 3702 0500 0009 7097 00", null, Money("10.00", "EUR"), "Donation") { accounts ->
//      accounts.first()
//    }
//    client.transferMoney(paramSelectAccount)
  }


  private fun logRetrievedAccountData(response: GetAccountDataResponse) {
    if (response.customerAccount == null) {
      println("Could not retrieve account data: ${response.error} ${response.errorMessage}")
      return
    }

    val customer = response.customerAccount!!
    val accounts = customer.accounts

    println()
    println("${customer.bankName} ${accounts.sumOf { it.balance.toBigDecimal() }} ${accounts.firstOrNull()?.balance?.currency}")
    println()

    println("Retrieved ${accounts.size} account(s):")

    println("\nIdentifier\t\tName\t\t\t\tBalance\t\t\tRetrieve transactions | Retrieve balance | Transfer money | Realtime transfer")

    accounts.forEach { account ->
      println("${account.identifier}\t\t${account.productName}\t${account.balance}\t\t\t${account.supportsRetrievingTransactions}\t\t\t\t" +
        "${account.supportsRetrievingBalance}\t\t\t\t${account.supportsTransferringMoney}\t\t\t\t${account.supportsInstantPayment}")
    }

    println("\n\nAccount transactions:")

    accounts.forEach { account ->
      println("\n${account.productName ?: account.identifier}\n")

      account.bookedTransactions.forEach { transaction ->
        println("${transaction.bookingDate} ${transaction.amount}\t${transaction.otherPartyName ?: "\t\t"}\t${transaction.unparsedReference}")
      }
    }
  }

  private fun exitWithError(errorMessage: String) {
    System.err.println("\nExisting due to fatal error: $errorMessage")

    exitProcess(1)
  }

}