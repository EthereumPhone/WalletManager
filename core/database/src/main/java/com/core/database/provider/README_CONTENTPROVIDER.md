# TokenMetadata ContentProvider

This ContentProvider allows other apps to query token metadata from the WalletManager database.

## Features

- Query token metadata by chain ID and contract address
- Query all tokens for a specific chain
- Automatic logo fallback system for tokens without logos in the database
- Support for both URL and local resource logos
- Real-time token prices in USD (when available)

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
    Log.d("Token", "Price USD: ${tokenData.price}")
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
        val price = it.getDouble(it.getColumnIndexOrThrow("price"))
        // ... etc
    }
}
```

## Available Columns

- `contract_address` (String): The token's contract address
- `decimals` (Int): Number of decimal places
- `name` (String): Token name
- `symbol` (String): Token symbol (e.g., "ETH", "USDC")
- `logo` (String): Token logo URL or resource URI (see Logo Handling below)
- `chain_id` (Int): Chain ID where the token exists
- `swappable` (Int): 1 if token is swappable, 0 otherwise
- `price` (Double): Current USD price per token (0.0 if price not available)

## Logo Handling

The ContentProvider automatically provides logo fallbacks for tokens that don't have logos in the database:

1. **Database Logo**: If a token has a logo URL in the database, it's returned as-is
2. **URL Fallback**: If no database logo exists, checks for a hardcoded fallback URL (e.g., from Alchemy or CoinGecko)
3. **Local Resource**: For tokens with local drawable resources, returns a URI in format: `android.resource://[packageName]/[resourceId]`

### Handling Different Logo Types in Your App

```kotlin
when {
    logo.startsWith("http") -> {
        // It's a URL - load with your image loading library
        Glide.with(context).load(logo).into(imageView)
    }
    logo.startsWith("android.resource://") -> {
        // It's a local resource from the provider app
        val uri = Uri.parse(logo)
        imageView.setImageURI(uri)
    }
    logo.isNullOrEmpty() -> {
        // No logo available - use your default placeholder
        imageView.setImageResource(R.drawable.default_token_icon)
    }
}
```

## Price Data

The ContentProvider includes real-time USD prices for tokens:

- Prices are fetched from the latest exchange rates stored in the database
- If no price data is available for a token, it returns 0.0
- Prices represent the value of 1 full token in USD
- Price data is updated periodically by the WalletManager app

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