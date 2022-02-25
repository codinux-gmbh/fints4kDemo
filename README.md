# fints4k Demo

Shows the usage of fints4k.

In essence:

## Get account data (account info, balance and transactions)

```kotlin
    val client = FinTsClient(SimpleFinTsClientCallback())

    val accountData = client.getAccountData("<Bankleitzahl>", "<Onlinebanking Login / Anmeldename>", "<Password>")

    // have fun with your shiny transactions (Kontoumsaetze) and other account data
```

## Transfer money

```kotlin
    // transfer money - make a donation to Médecins Sans Frontières of 10 Euros
    client.transferMoney(bankCode, loginName, password, "Ärzte ohne Grenzen e.V.", "DE72 3702 0500 0009 7097 00", Money("10.00", "EUR"), "Spende")
```

For detailed usage like TAN handling see file [fints4kDemo.kt](src/main/kotlin/net/codinux/banking/fints/fints4kDemo.kt).