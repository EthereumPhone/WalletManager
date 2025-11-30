# WalletManager Swap ContentProviders

This document explains how other Android apps can use WalletManager's swap functionality via ContentProviders.

## Overview

WalletManager exposes two ContentProviders for swap operations:

| Provider | Authority | Purpose |
|----------|-----------|---------|
| **SwapQuoteContentProvider** | `com.walletmanager.swapquote.provider` | Get swap quotes (pricing info, expected output) |
| **SwapDataContentProvider** | `com.walletmanager.swapdata.provider` | Get transaction data (to, value, data) ready for execution |

Both providers use the 0x API and support these chains:
- Ethereum Mainnet (chainId: 1)
- Optimism (chainId: 10)
- Polygon (chainId: 137)
- Arbitrum (chainId: 42161)
- Base (chainId: 8453)

---

## SwapDataContentProvider

**Use this to get transaction parameters that can be executed directly.**

### URI Format

```
content://com.walletmanager.swapdata.provider/swapdata?sellToken=...&buyToken=...&sellAmount=...&chainId=...&sellDecimals=...&buyDecimals=...&sellSymbol=...&buySymbol=...
```

### Query Parameters

| Parameter | Required | Description | Example |
|-----------|----------|-------------|---------|
| `sellToken` | ✅ | Token address to sell | `0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48` |
| `buyToken` | ✅ | Token address to buy | `0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2` |
| `sellAmount` | ✅ | Amount to sell (human-readable) | `100` (for 100 USDC) |
| `chainId` | ✅ | Chain ID | `1` |
| `sellDecimals` | ✅ | Decimals of sell token | `6` |
| `buyDecimals` | ✅ | Decimals of buy token | `18` |
| `sellSymbol` | ❌ | Symbol (for ETH detection) | `USDC` |
| `buySymbol` | ❌ | Symbol (for ETH detection) | `WETH` |

### Response Columns

The cursor returns **multiple rows** - one for each transaction to execute in order:

| Column | Type | Description |
|--------|------|-------------|
| `to` | String | Target address for the transaction |
| `value` | String | ETH value to send (in wei) |
| `data` | String | Transaction calldata (hex) |
| `tx_type` | String | `"approval"` or `"swap"` |
| `tx_index` | Int | 0-based index of this transaction |
| `tx_count` | Int | Total number of transactions |
| `buy_amount` | String | Expected output (only on swap row) |
| `sell_amount` | String | Input amount in wei (only on swap row) |
| `error` | String | Error message (empty on success) |

### Example: Basic Usage

```kotlin
// Build the URI
val uri = Uri.parse("content://com.walletmanager.swapdata.provider/swapdata")
    .buildUpon()
    .appendQueryParameter("sellToken", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48") // USDC
    .appendQueryParameter("buyToken", "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2")  // WETH
    .appendQueryParameter("sellAmount", "100")  // 100 USDC
    .appendQueryParameter("chainId", "1")
    .appendQueryParameter("sellDecimals", "6")
    .appendQueryParameter("buyDecimals", "18")
    .appendQueryParameter("sellSymbol", "USDC")
    .appendQueryParameter("buySymbol", "WETH")
    .build()

// Query the provider
val cursor = contentResolver.query(uri, null, null, null, null)

// Parse the transactions
data class TxParams(val to: String, val value: String, val data: String)
val transactions = mutableListOf<TxParams>()
var buyAmount = ""
var error = ""

cursor?.use {
    while (it.moveToNext()) {
        error = it.getString(it.getColumnIndexOrThrow("error"))
        if (error.isNotEmpty()) break
        
        transactions.add(TxParams(
            to = it.getString(it.getColumnIndexOrThrow("to")),
            value = it.getString(it.getColumnIndexOrThrow("value")),
            data = it.getString(it.getColumnIndexOrThrow("data"))
        ))
        
        val txType = it.getString(it.getColumnIndexOrThrow("tx_type"))
        if (txType == "swap") {
            buyAmount = it.getString(it.getColumnIndexOrThrow("buy_amount"))
        }
    }
}

// Execute transactions in order
if (error.isEmpty() && transactions.isNotEmpty()) {
    for (tx in transactions) {
        // Use your wallet SDK to send each transaction
        walletSDK.sendTransaction(
            to = tx.to,
            value = tx.value,
            data = tx.data
        )
    }
    Log.d("Swap", "Expected output: $buyAmount")
} else {
    Log.e("Swap", "Error: $error")
}
```

### Example: Swapping ETH for Tokens

For native ETH, use the zero address or chain ID as the token address:

```kotlin
val uri = Uri.parse("content://com.walletmanager.swapdata.provider/swapdata")
    .buildUpon()
    .appendQueryParameter("sellToken", "0x0000000000000000000000000000000000000000") // ETH
    .appendQueryParameter("buyToken", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48")  // USDC
    .appendQueryParameter("sellAmount", "0.1")  // 0.1 ETH
    .appendQueryParameter("chainId", "1")
    .appendQueryParameter("sellDecimals", "18")
    .appendQueryParameter("buyDecimals", "6")
    .appendQueryParameter("sellSymbol", "ETH")
    .appendQueryParameter("buySymbol", "USDC")
    .build()
```

> **Note:** When selling ETH, no approval transaction is needed, so you'll get only 1 row (the swap).

### Understanding the Response

**Scenario 1: Selling ERC20 token (approval needed)**
```
Row 0: to=<token_address>, value="0", data=<approve_calldata>, tx_type="approval"
Row 1: to=<0x_contract>, value="0", data=<swap_calldata>, tx_type="swap"
```

**Scenario 2: Selling ETH (no approval needed)**
```
Row 0: to=<0x_contract>, value="100000000000000000", data=<swap_calldata>, tx_type="swap"
```

---

## SwapQuoteContentProvider

**Use this to get pricing information without executing a swap.**

### URI Format

```
content://com.walletmanager.swapquote.provider/quote?sellToken=...&buyToken=...&sellAmount=...&chainId=...&sellDecimals=...&buyDecimals=...&sellSymbol=...&buySymbol=...
```

### Query Parameters

Same as SwapDataContentProvider.

### Response Columns

Returns a **single row** with quote information:

| Column | Type | Description |
|--------|------|-------------|
| `sell_token` | String | Normalized sell token address |
| `buy_token` | String | Normalized buy token address |
| `sell_amount` | String | Sell amount (in smallest unit) |
| `buy_amount` | String | Expected buy amount (in smallest unit) |
| `min_buy_amount` | String | Minimum buy amount (with slippage) |
| `price` | String | Exchange rate |
| `guaranteed_price` | String | Guaranteed price |
| `estimated_price_impact` | String | Price impact percentage |
| `liquidity_available` | Int | 1 if liquidity available, 0 otherwise |
| `gas` | String | Estimated gas |
| `gas_price` | String | Gas price |
| `total_network_fee` | String | Total network fee |
| `allowance_target` | String | Spender address for approvals |
| `chain_id` | Int | Chain ID |
| `error` | String | Error message (empty on success) |

### Example

```kotlin
val uri = Uri.parse("content://com.walletmanager.swapquote.provider/quote")
    .buildUpon()
    .appendQueryParameter("sellToken", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48")
    .appendQueryParameter("buyToken", "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2")
    .appendQueryParameter("sellAmount", "100")
    .appendQueryParameter("chainId", "1")
    .appendQueryParameter("sellDecimals", "6")
    .appendQueryParameter("buyDecimals", "18")
    .build()

val cursor = contentResolver.query(uri, null, null, null, null)

cursor?.use {
    if (it.moveToFirst()) {
        val error = it.getString(it.getColumnIndexOrThrow("error"))
        if (error.isEmpty()) {
            val buyAmount = it.getString(it.getColumnIndexOrThrow("buy_amount"))
            val price = it.getString(it.getColumnIndexOrThrow("price"))
            val priceImpact = it.getString(it.getColumnIndexOrThrow("estimated_price_impact"))
            
            Log.d("Quote", "Expected output: $buyAmount")
            Log.d("Quote", "Price: $price")
            Log.d("Quote", "Price impact: $priceImpact%")
        }
    }
}
```

---

## Contract Classes (Optional)

You can copy these helper classes to your app for easier integration:

### SwapDataProviderContract.kt

```kotlin
object SwapDataProviderContract {
    
    const val AUTHORITY = "com.walletmanager.swapdata.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
    
    data class TxParams(
        val to: String,
        val value: String,
        val data: String
    )
    
    data class SwapTransactionsResult(
        val transactions: List<TxParams>,
        val buyAmount: String,
        val sellAmount: String,
        val error: String
    ) {
        val isSuccess: Boolean get() = error.isEmpty() && transactions.isNotEmpty()
        val hasApproval: Boolean get() = transactions.size > 1
    }
    
    fun getSwapTransactions(
        contentResolver: ContentResolver,
        sellToken: String,
        buyToken: String,
        sellAmount: BigDecimal,
        chainId: Int,
        sellDecimals: Int,
        buyDecimals: Int,
        sellSymbol: String = "",
        buySymbol: String = ""
    ): SwapTransactionsResult {
        val uri = CONTENT_URI.buildUpon()
            .appendPath("swapdata")
            .appendQueryParameter("sellToken", sellToken)
            .appendQueryParameter("buyToken", buyToken)
            .appendQueryParameter("sellAmount", sellAmount.toPlainString())
            .appendQueryParameter("chainId", chainId.toString())
            .appendQueryParameter("sellDecimals", sellDecimals.toString())
            .appendQueryParameter("buyDecimals", buyDecimals.toString())
            .appendQueryParameter("sellSymbol", sellSymbol)
            .appendQueryParameter("buySymbol", buySymbol)
            .build()
        
        val cursor = contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            val transactions = mutableListOf<TxParams>()
            var buyAmountResult = ""
            var sellAmountResult = ""
            var errorResult = ""
            
            while (it.moveToNext()) {
                val error = it.getString(it.getColumnIndexOrThrow("error"))
                if (error.isNotEmpty()) {
                    errorResult = error
                    break
                }
                
                transactions.add(TxParams(
                    to = it.getString(it.getColumnIndexOrThrow("to")),
                    value = it.getString(it.getColumnIndexOrThrow("value")),
                    data = it.getString(it.getColumnIndexOrThrow("data"))
                ))
                
                if (it.getString(it.getColumnIndexOrThrow("tx_type")) == "swap") {
                    buyAmountResult = it.getString(it.getColumnIndexOrThrow("buy_amount"))
                    sellAmountResult = it.getString(it.getColumnIndexOrThrow("sell_amount"))
                }
            }
            
            SwapTransactionsResult(transactions, buyAmountResult, sellAmountResult, errorResult)
        } ?: SwapTransactionsResult(emptyList(), "", "", "Failed to query")
    }
}
```

### Usage with Contract Class

```kotlin
val result = SwapDataProviderContract.getSwapTransactions(
    contentResolver = context.contentResolver,
    sellToken = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
    buyToken = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
    sellAmount = BigDecimal("100"),
    chainId = 1,
    sellDecimals = 6,
    buyDecimals = 18,
    sellSymbol = "USDC",
    buySymbol = "WETH"
)

if (result.isSuccess) {
    Log.d("Swap", "Transactions to execute: ${result.transactions.size}")
    Log.d("Swap", "Has approval: ${result.hasApproval}")
    Log.d("Swap", "Expected output: ${result.buyAmount}")
    
    for (tx in result.transactions) {
        // Execute with your wallet
        wallet.sendTransaction(to = tx.to, value = tx.value, data = tx.data)
    }
}
```

---

## Error Handling

Common error messages:

| Error | Cause |
|-------|-------|
| `Missing required parameters: ...` | Required query parameter not provided |
| `Invalid sellAmount format` | sellAmount is not a valid number |
| `Unsupported chain: X` | Chain ID not supported by 0x |
| `No liquidity available for this swap pair` | No DEX liquidity for this pair |
| `Insufficient balance: expected X, actual Y` | User doesn't have enough tokens |
| `Failed to get quote from 0x API` | API error or network issue |

---

## Common Token Addresses

### Ethereum Mainnet (chainId: 1)

| Token | Address | Decimals |
|-------|---------|----------|
| ETH | `0x0000000000000000000000000000000000000000` | 18 |
| WETH | `0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2` | 18 |
| USDC | `0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48` | 6 |
| USDT | `0xdAC17F958D2ee523a2206206994597C13D831ec7` | 6 |
| DAI | `0x6B175474E89094C44Da98b954EesfdFCE2CE36D8` | 18 |

### Base (chainId: 8453)

| Token | Address | Decimals |
|-------|---------|----------|
| ETH | `0x0000000000000000000000000000000000000000` | 18 |
| USDC | `0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913` | 6 |

---

## Notes

- The swap includes a 0.15% fee that goes to the WalletManager fee recipient
- Transactions should be executed **in order** - approval first (if present), then swap
- The `value` field is in wei (string format) and represents ETH to send with the transaction
- For ERC20 → ERC20 swaps, `value` will be `"0"` for both transactions
- For ETH → Token swaps, `value` will contain the ETH amount on the swap transaction

