# fints4k Demo

Shows the usage of fints4k.

In essence:

```kotlin
    val client = FinTsClient(SimpleFinTsClientCallback())

    val accountData = client.getAccountData("<Bankleitzahl>", "<Onlinebanking Login / Anmeldename>", "<Password>")

    // have fun with your shiny transactions (Kontoumsaetze) and other account data
```

For detailed usage like TAN handling see file [fints4kDemo.kt](src/main/kotlin/net/codinux/banking/fints/fints4kDemo.kt).