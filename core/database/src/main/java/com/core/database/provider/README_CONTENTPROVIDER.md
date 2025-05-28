# TokenMetadata ContentProvider

This ContentProvider allows other apps to query token metadata from the WalletManager database.

## Setup in Your App

### 1. Add Permission to Your App's Manifest

Add this permission to your app's AndroidManifest.xml:

```xml
<uses-permission android:name="com.walletmanager.permission.READ_TOKEN_METADATA" />
```

### 2. Copy the Contract Class

Copy the `TokenMetadataProviderContract.kt` file to your app. This provides helper methods for querying the ContentProvider.

### 3. Query Token Metadata

#### Query a specific token:

```kotlin
val tokenData = TokenMetadataProviderContract.getTokenMetadata(
    context.contentResolver,
    chainId = 1,  // Ethereum mainnet
    contractAddress = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48" // USDC
)

if (tokenData != null) {
    Log.d("Token", "Name: ${tokenData.name}")
    Log.d("Token", "Symbol: ${tokenData.symbol}")
    Log.d("Token", "Decimals: ${tokenData.decimals}")
    Log.d("Token", "Logo URL: ${tokenData.logo}")
}
```

#### Query all tokens for a chain:

```kotlin
val tokens = TokenMetadataProviderContract.getTokensByChain(
    context.contentResolver,
    chainId = 1  // Ethereum mainnet
)

tokens.forEach { token ->
    Log.d("Token", "${token.symbol}: ${token.name}")
}
```

### 4. Direct URI Query (Without Contract)

If you prefer not to use the contract class, you can query directly:

```kotlin
// Query specific token
val uri = Uri.parse("content://com.walletmanager.tokenmetadata.provider/token/1/0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48")
val cursor = contentResolver.query(uri, null, null, null, null)

cursor?.use {
    if (it.moveToFirst()) {
        val name = it.getString(it.getColumnIndexOrThrow("name"))
        val symbol = it.getString(it.getColumnIndexOrThrow("symbol"))
        val decimals = it.getInt(it.getColumnIndexOrThrow("decimals"))
        // ... etc
    }
}
```

## Available Columns

- `contract_address` (String): The token's contract address
- `decimals` (Int): Number of decimal places
- `name` (String): Token name
- `symbol` (String): Token symbol
- `logo` (String?): URL to token logo (nullable)
- `chain_id` (Int): Blockchain chain ID
- `swappable` (Int): 1 if swappable, 0 if not

## URI Patterns

- Get specific token: `content://com.walletmanager.tokenmetadata.provider/token/{chainId}/{contractAddress}`
- Get all tokens for a chain: `content://com.walletmanager.tokenmetadata.provider/tokens/{chainId}`

## Supported Chain IDs

Common chain IDs:
- 1: Ethereum Mainnet
- 137: Polygon
- 10: Optimism
- 42161: Arbitrum One
- 8453: Base

## Error Handling

Always check if the returned data is null, as tokens might not exist in the database:

```kotlin
val tokenData = TokenMetadataProviderContract.getTokenMetadata(
    context.contentResolver,
    chainId = 1,
    contractAddress = "0x..."
)

if (tokenData != null) {
    // Use token data
} else {
    // Token not found in database
}
``` 