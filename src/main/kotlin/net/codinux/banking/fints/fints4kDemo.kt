package net.codinux.banking.fints

import net.dankito.banking.bankfinder.InMemoryBankFinder
import net.dankito.banking.fints.FinTsClientForCustomer
import net.dankito.banking.fints.callback.SimpleFinTsClientCallback
import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.client.AddAccountResponse
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess


fun main(args: Array<String>) {
  fints4kDemo().run()
}


class fints4kDemo {

  // TODO: set your bank code (BLZ), login name (Online Banking Nutzernamen) and password here

  private val bankCode = ""

  private val loginName = ""

  private val password = ""


  private val callback = SimpleFinTsClientCallback({ bank, tanChallenge -> handleTanRequired(bank, tanChallenge) }) { supportedTanMethods, suggestedTanMethod -> selectTanMethod(supportedTanMethods, suggestedTanMethod) }

  private fun handleTanRequired(bank: BankData, tanChallenge: TanChallenge): EnterTanResult {
    // if a TAN is requested, enter the TAN here. For information what to do see parameter tanChallenge
    // return EnterTanResult.userEnteredTan("")

    return EnterTanResult.userDidNotEnterTan() // action will be aborted then
  }

  private fun selectTanMethod(supportedTanMethods: List<TanMethod>, suggestedTanMethod: TanMethod?): TanMethod? {
    // select your TAN method here by choosing one from supportedTanMethods. In most cases suggestedTanMethod should be a good choice
    return suggestedTanMethod
  }


  fun run() {

    // step 1: get the FinTS server address of your bank
    val bankInfos = InMemoryBankFinder().findBankByBankCode(bankCode)
    val bankSupportingFinTs = bankInfos.firstOrNull { it.pinTanAddress != null }
    if (bankSupportingFinTs == null) {
      exitWithError(if (bankInfos.isEmpty()) "No bank found for bank code $bankCode" else "Your bank ${bankInfos.first().name} does not support FinTS")
      return
    }


    // step 2: get account data (like bank accounts etc.)
    val bank = BankData(bankCode, loginName, password, bankSupportingFinTs.pinTanAddress!!, bankSupportingFinTs.bic, bankSupportingFinTs.name)

    val client = FinTsClientForCustomer(bank, callback)

    val countDownLatch = CountDownLatch(1)

    client.addAccountAsync(AddAccountParameter(bank)) { addAccountResponse ->
      if (addAccountResponse.successful == false) {
        exitWithError("Could not get account data: ${addAccountResponse.errorMessage ?: addAccountResponse.errorsToShowToUser.joinToString("\n")}")
      } else {
        logRetrievedAccountData(addAccountResponse)

        // step 3: get account transactions. The account transactions of the last 90 days should already be included in AddAccountResponse if it's possible to retrieve them
        // without TAN. If not then it's required to enter a TAN which could get difficult at a command line program (use e.g. the debugging window and set the TAN there).
        // client.getTransactionsAsync(GetTransactionsParameter(bank.accounts.first())) { getTransactionsResponse ->  } // retrieves all account transactions that server holds. But requires a TAN

        // step 4: transfer some money
//        client.doBankTransferAsync(BankTransferData("Christian Dankl", "DE11720512100560165557", "BYLADEM1AIC", Money(Amount("100"), "EUR"), "Thanks for your great library"),
//          bank.accounts.first()) { transferMoneyResponse -> }

        countDownLatch.countDown()
      }
    }

    countDownLatch.await() // wait for asynchronous operation to finish
  }


  private fun logRetrievedAccountData(response: AddAccountResponse) {
    val accounts = response.retrievedData

    println("Retrieved ${accounts.size} account(s):")

    println("\nIdentifier\t\tName\t\t\t\tBalance\t\t\tRetrieve transactions | Retrieve balance | Transfer money | Realtime transfer")

    accounts.forEach { accountData ->
      val account = accountData.account
      println("${account.accountIdentifier}\t\t${account.productName}\t${accountData.balance}\t\t\t${account.supportsRetrievingAccountTransactions}\t\t\t\t" +
        "${account.supportsRetrievingBalance}\t\t\t\t${account.supportsTransferringMoney}\t\t\t\t${account.supportsRealTimeTransfer}")
    }

    println("\n\nAccount transactions:")

    accounts.forEach { accountData ->
      println("\n${accountData.account.productName ?: accountData.account.accountIdentifier}\n")

      accountData.bookedTransactions.forEach { transaction ->
        println("${transaction.bookingDate} ${transaction.amount}\t${transaction.otherPartyName ?: "\t\t"}\t${transaction.unparsedReference}")
      }
    }
  }

  private fun exitWithError(errorMessage: String) {
    System.err.println("\nExisting due to fatal error: $errorMessage")

    exitProcess(1)
  }

}