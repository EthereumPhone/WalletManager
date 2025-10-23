package com.core.data.util


data class Eip681(
    val isPay: Boolean,
    val target: TargetAddress,  // HexAddress or EnsName
    val chainId: Long?,         // null if absent
    val functionName: String?,  // null if absent
    val params: List<Param>     // preserves order and duplicates
) {
    sealed interface TargetAddress
    @JvmInline value class HexAddress(val value: String) : TargetAddress
    @JvmInline value class EnsName(val value: String) : TargetAddress

    data class Param(
        val key: String,               // "value", "gas", "gasLimit", "gasPrice", or TYPE
        val rawValue: String,          // original decoded string
        val valueKind: ValueKind       // best-effort classification
    )
    enum class ValueKind { NUMBER, ADDRESS, ENS, STRING }
}

class Eip681ParseException(msg: String) : IllegalArgumentException(msg)

private val HEX_ADDR = Regex("^0x[0-9a-fA-F]{40}$")
private val ENS_LABEL = Regex("^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$", RegexOption.IGNORE_CASE)
private val FUNC_NAME = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
private val NUMBER = Regex("""^[+\-]?\d*(?:\.\d+)?(?:[eE]\d+)?$""")

fun parseEip681(input: String): Eip681 {
    // 1) scheme
    val prefix = "ethereum:"
    require(input.startsWith(prefix, ignoreCase = true)) { "Missing 'ethereum:' scheme" }
    var i = prefix.length
    val n = input.length

    // 2) optional pay-
    val isPay = input.regionMatches(i, "pay-", 0, 4, ignoreCase = true).also { if (it) i += 4 }

    // helper to peek / read
    fun peek(): Char? = if (i < n) input[i] else null
    fun readUntil(vararg stops: Char): String {
        val stopSet = stops.toSet()
        val start = i
        while (i < n && input[i] !in stopSet) i++
        return input.substring(start, i)
    }

    // 3) target_address
    val targetStr = readUntil('@', '/', '?')
    if (targetStr.isEmpty()) throw Eip681ParseException("Missing target_address")

    val target: Eip681.TargetAddress = when {
        HEX_ADDR.matches(targetStr) -> Eip681.HexAddress(targetStr)
        isValidEns(targetStr) -> Eip681.EnsName(targetStr.lowercase())
        else -> throw Eip681ParseException("Invalid target_address: '$targetStr'")
    }

    // 4) optional @chain_id
    var chainId: Long? = null
    if (peek() == '@') {
        i++
        val chainStr = readUntil('/', '?')
        if (chainStr.isEmpty() || chainStr.any { it !in '0'..'9' }) {
            throw Eip681ParseException("Invalid chain_id")
        }
        chainId = chainStr.toLong() // throws if out of range -> good
    }

    // 5) optional /function_name
    var functionName: String? = null
    if (peek() == '/') {
        i++
        val fn = readUntil('?')
        if (fn.isEmpty() || !FUNC_NAME.matches(fn)) {
            throw Eip681ParseException("Invalid function_name")
        }
        functionName = fn
    }

    // 6) optional ?parameters
    val params = mutableListOf<Eip681.Param>()
    if (peek() == '?') {
        i++
        val query = input.substring(i)
        // Split on '&' only; values may be percent-encoded, not including '&' or '=' unencoded
        val pairs = if (query.isEmpty()) emptyList() else query.split('&')
        for (p in pairs) {
            val eq = p.indexOf('=')
            if (eq <= 0) throw Eip681ParseException("Invalid parameter: '$p'")
            val rawKey = urlDecode(p.substring(0, eq))
            val rawVal = urlDecode(p.substring(eq + 1))

            if (!isValidKey(rawKey)) throw Eip681ParseException("Invalid key: '$rawKey'")

            val kind = when {
                NUMBER.matches(rawVal) -> Eip681.ValueKind.NUMBER
                HEX_ADDR.matches(rawVal) -> Eip681.ValueKind.ADDRESS
                isValidEns(rawVal) -> Eip681.ValueKind.ENS
                else -> Eip681.ValueKind.STRING
            }
            params += Eip681.Param(rawKey, rawVal, kind)
        }
        i = n
    }

    if (i != n) throw Eip681ParseException("Trailing garbage at position $i")
    return Eip681(isPay, target, chainId, functionName, params)
}

private fun isValidEns(name: String): Boolean {
    val parts = name.split('.')
    if (parts.size < 2) return false
    return parts.all { it.length in 1..63 && ENS_LABEL.matches(it) } &&
            name.length <= 253
}

private fun isValidKey(k: String): Boolean {
    if (k == "value" || k == "gas" || k == "gasLimit" || k == "gasPrice") return true
    // TYPE token (coarse): starts with a letter, then letters/digits/[] , commas
    // Examples: address, uint256, uint256[], tuple,address
    return k.matches(Regex("^[A-Za-z][A-Za-z0-9\\[\\],]*$"))
}

private fun urlDecode(s: String): String =
    java.net.URLDecoder.decode(s, Charsets.UTF_8.name())
