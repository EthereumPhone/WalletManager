package org.ethereumphone.swaptestapp


import com.esaulpaugh.headlong.abi.Address
import com.esaulpaugh.headlong.abi.Function
import com.esaulpaugh.headlong.abi.Tuple
import com.esaulpaugh.headlong.abi.TupleType
import com.esaulpaugh.headlong.abi.util.Uint
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.nio.ByteBuffer

class RealEncoder {

    fun encodePermit(tokenIn: String, amount: BigInteger, expiration: BigInteger, nonce: BigInteger, spender: String, sigDeadline: BigInteger, signature: ByteArray): String {
        try {
            var f = Function("(((address,uint160,uint48,uint48),address,uint256),bytes)")
            val args = Tuple.of(
                Tuple.of(
                    Tuple.of(
                        com.esaulpaugh.headlong.abi.Address.wrap(tokenIn),
                        amount,
                        expiration.longValueExact(),
                        nonce.longValueExact()
                    ),
                    com.esaulpaugh.headlong.abi.Address.wrap(spender),
                    sigDeadline
                ),
                signature
            )
            val result = f.inputs.encode(args)
            if (result.hasArray()) {
                return Numeric.toHexString(result.array())
            }
            val buffer = ByteBuffer.allocate(result.remaining())
            result.get(buffer.array())
            return Numeric.toHexString(buffer.array())
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return "0"
    }

    fun encodePermitTransfer(token: String, fee: BigInteger, chainId: Int): String {
        try {
            val checksumAddress = if (chainId == 1) {
                "0x55A4E5123F8923500fF7AA97e75231a54A9c233a"
            } else {
                "0x12dC5fF4AB146D9f70e50bd5f9854114a115e6c9"
            }
            val f = Function("(address,address,uint256)")
            val args = Tuple.of(
                com.esaulpaugh.headlong.abi.Address.wrap(token),
                com.esaulpaugh.headlong.abi.Address.wrap(checksumAddress), // Multisig address
                fee
            )
            val result = f.encodeCall(args)
            if (result.hasArray()) {
                return Numeric.toHexString(setFirstFourBitsToZero(result.array()))
            }
            val buffer = ByteBuffer.allocate(result.remaining())
            result.get(buffer.array())
            return Numeric.toHexString(setFirstFourBitsToZero(buffer.array()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0"
    }

    fun encodeSwap(receipient: String, amountIn: BigInteger, minOut: BigInteger, path: ByteArray, flag: Boolean): String {
        try {
            val f = Function("(address,uint256,uint256,bytes,bool)")
            val args = Tuple.of(
                com.esaulpaugh.headlong.abi.Address.wrap(receipient),
                amountIn,
                minOut,
                path,
                flag
            )
            val result = f.encodeCall(args)
            if (result.hasArray()) {
                return Numeric.toHexString(setFirstFourBitsToZero(result.array()))
            }
            val buffer = ByteBuffer.allocate(result.remaining())
            result.get(buffer.array())
            return Numeric.toHexString(setFirstFourBitsToZero(buffer.array()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0"
    }

    fun encodeWEthCommand(address: String, amount: BigInteger): String {
        try {
            val f = Function("(address,uint256)")
            val args = Tuple.of(
                com.esaulpaugh.headlong.abi.Address.wrap(address),
                amount
            )
            val result = f.encodeCall(args)
            if (result.hasArray()) {
                return Numeric.toHexString(result.array())
            }
            val buffer = ByteBuffer.allocate(result.remaining())
            result.get(buffer.array())
            return Numeric.toHexString(buffer.array())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0"
    }

    fun getNonceData(user: String, token: String, spender: String): String {
        try {
            val f = Function("allowance(address,address,address)", "(uint160,uint48,uint48)")
            val args = Tuple.of(
                com.esaulpaugh.headlong.abi.Address.wrap(user),
                com.esaulpaugh.headlong.abi.Address.wrap(token),
                com.esaulpaugh.headlong.abi.Address.wrap(spender)
            )
            val result = f.encodeCall(args)
            if (result.hasArray()) {
                return Numeric.toHexString(setFirstFourBitsToZero(result.array()))
            }
            val buffer = ByteBuffer.allocate(result.remaining())
            result.get(buffer.array())
            return Numeric.toHexString(setFirstFourBitsToZero(buffer.array()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0"
    }

    fun getCurrentNonce(user: String, token: String, spender: String, web3j: Web3j): String {
        val encodedData = getNonceData(user, token, spender)
        val result = web3j.ethCall(
            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                user,
                token,
                encodedData
            ),
            DefaultBlockParameterName.LATEST
        ).sendAsync().get()

        return result.rawResponse
    }

    /**
     * Function to set first 8 Bits to 0
     */
    fun setFirstFourBitsToZero(bytes: ByteArray): ByteArray {
        bytes[0] = 0
        bytes[1] = 0
        bytes[2] = 0
        bytes[3] = 0

        // And remove the first 4 bytes
        val newBytes = ByteArray(bytes.size - 4)
        System.arraycopy(bytes, 4, newBytes, 0, newBytes.size)
        return newBytes
    }
}