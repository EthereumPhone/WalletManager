package org.ethereumphone.walletmanager.core.web3.swap;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Int24;
import org.web3j.abi.datatypes.generated.Uint160;
import org.web3j.abi.datatypes.generated.Uint24;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.4.2.
 */
@SuppressWarnings("rawtypes")
public class IUniswapV3Router extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_WETH9 = "WETH9";

    public static final String FUNC_APPROVEMAX = "approveMax";

    public static final String FUNC_APPROVEMAXMINUSONE = "approveMaxMinusOne";

    public static final String FUNC_APPROVEZEROTHENMAX = "approveZeroThenMax";

    public static final String FUNC_APPROVEZEROTHENMAXMINUSONE = "approveZeroThenMaxMinusOne";

    public static final String FUNC_CALLPOSITIONMANAGER = "callPositionManager";

    public static final String FUNC_checkOracleSlippage = "checkOracleSlippage";

    public static final String FUNC_EXACTINPUT = "exactInput";

    public static final String FUNC_EXACTINPUTSINGLE = "exactInputSingle";

    public static final String FUNC_EXACTOUTPUT = "exactOutput";

    public static final String FUNC_EXACTOUTPUTSINGLE = "exactOutputSingle";

    public static final String FUNC_FACTORY = "factory";

    public static final String FUNC_FACTORYV2 = "factoryV2";

    public static final String FUNC_GETAPPROVALTYPE = "getApprovalType";

    public static final String FUNC_INCREASELIQUIDITY = "increaseLiquidity";

    public static final String FUNC_MINT = "mint";

    public static final String FUNC_multicall = "multicall";

    public static final String FUNC_POSITIONMANAGER = "positionManager";

    public static final String FUNC_PULL = "pull";

    public static final String FUNC_REFUNDETH = "refundETH";

    public static final String FUNC_SELFPERMIT = "selfPermit";

    public static final String FUNC_SELFPERMITALLOWED = "selfPermitAllowed";

    public static final String FUNC_SELFPERMITALLOWEDIFNECESSARY = "selfPermitAllowedIfNecessary";

    public static final String FUNC_SELFPERMITIFNECESSARY = "selfPermitIfNecessary";

    public static final String FUNC_SWAPEXACTTOKENSFORTOKENS = "swapExactTokensForTokens";

    public static final String FUNC_SWAPTOKENSFOREXACTTOKENS = "swapTokensForExactTokens";

    public static final String FUNC_sweepToken = "sweepToken";

    public static final String FUNC_sweepTokenWithFee = "sweepTokenWithFee";

    public static final String FUNC_UNISWAPV3SWAPCALLBACK = "uniswapV3SwapCallback";

    public static final String FUNC_unwrapWETH9 = "unwrapWETH9";

    public static final String FUNC_unwrapWETH9WithFee = "unwrapWETH9WithFee";

    public static final String FUNC_WRAPETH = "wrapETH";

    @Deprecated
    protected IUniswapV3Router(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected IUniswapV3Router(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected IUniswapV3Router(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected IUniswapV3Router(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<String> WETH9() {
        final Function function = new Function(FUNC_WETH9,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> approveMax(String token, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_APPROVEMAX,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> approveMaxMinusOne(String token, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_APPROVEMAXMINUSONE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> approveZeroThenMax(String token, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_APPROVEZEROTHENMAX,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> approveZeroThenMaxMinusOne(String token, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_APPROVEZEROTHENMAXMINUSONE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> callPositionManager(byte[] data, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_CALLPOSITIONMANAGER,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(data)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> exactInput(ExactInputParams params, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_EXACTINPUT,
                Arrays.<Type>asList(params),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> exactInputSingle(ExactInputSingleParams params, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_EXACTINPUTSINGLE,
                Arrays.<Type>asList(params),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> exactOutput(ExactOutputParams params, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_EXACTOUTPUT,
                Arrays.<Type>asList(params),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> exactOutputSingle(ExactOutputSingleParams params, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_EXACTOUTPUTSINGLE,
                Arrays.<Type>asList(params),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<String> factory() {
        final Function function = new Function(FUNC_FACTORY,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> factoryV2() {
        final Function function = new Function(FUNC_FACTORYV2,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> getApprovalType(String token, BigInteger amount) {
        final Function function = new Function(
                FUNC_GETAPPROVALTYPE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> increaseLiquidity(IncreaseLiquidityParams params, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_INCREASELIQUIDITY,
                Arrays.<Type>asList(params),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> mint(MintParams params, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_MINT,
                Arrays.<Type>asList(params),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> multicall(byte[] previousBlockhash, List<byte[]> data, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_multicall,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(previousBlockhash),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.DynamicBytes>(
                                org.web3j.abi.datatypes.DynamicBytes.class,
                                org.web3j.abi.Utils.typeMap(data, org.web3j.abi.datatypes.DynamicBytes.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> multicall(BigInteger deadline, List<byte[]> data, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_multicall,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(deadline),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.DynamicBytes>(
                                org.web3j.abi.datatypes.DynamicBytes.class,
                                org.web3j.abi.Utils.typeMap(data, org.web3j.abi.datatypes.DynamicBytes.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> multicall(List<byte[]> data, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_multicall,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.DynamicBytes>(
                        org.web3j.abi.datatypes.DynamicBytes.class,
                        org.web3j.abi.Utils.typeMap(data, org.web3j.abi.datatypes.DynamicBytes.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<String> positionManager() {
        final Function function = new Function(FUNC_POSITIONMANAGER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> pull(String token, BigInteger value, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_PULL,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> refundETH(BigInteger weiValue) {
        final Function function = new Function(
                FUNC_REFUNDETH,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> selfPermit(String token, BigInteger value, BigInteger deadline, BigInteger v, byte[] r, byte[] s, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_SELFPERMIT,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(value),
                        new org.web3j.abi.datatypes.generated.Uint256(deadline),
                        new org.web3j.abi.datatypes.generated.Uint8(v),
                        new org.web3j.abi.datatypes.generated.Bytes32(r),
                        new org.web3j.abi.datatypes.generated.Bytes32(s)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> selfPermitAllowed(String token, BigInteger nonce, BigInteger expiry, BigInteger v, byte[] r, byte[] s, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_SELFPERMITALLOWED,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(nonce),
                        new org.web3j.abi.datatypes.generated.Uint256(expiry),
                        new org.web3j.abi.datatypes.generated.Uint8(v),
                        new org.web3j.abi.datatypes.generated.Bytes32(r),
                        new org.web3j.abi.datatypes.generated.Bytes32(s)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> selfPermitAllowedIfNecessary(String token, BigInteger nonce, BigInteger expiry, BigInteger v, byte[] r, byte[] s, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_SELFPERMITALLOWEDIFNECESSARY,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(nonce),
                        new org.web3j.abi.datatypes.generated.Uint256(expiry),
                        new org.web3j.abi.datatypes.generated.Uint8(v),
                        new org.web3j.abi.datatypes.generated.Bytes32(r),
                        new org.web3j.abi.datatypes.generated.Bytes32(s)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> selfPermitIfNecessary(String token, BigInteger value, BigInteger deadline, BigInteger v, byte[] r, byte[] s, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_SELFPERMITIFNECESSARY,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(value),
                        new org.web3j.abi.datatypes.generated.Uint256(deadline),
                        new org.web3j.abi.datatypes.generated.Uint8(v),
                        new org.web3j.abi.datatypes.generated.Bytes32(r),
                        new org.web3j.abi.datatypes.generated.Bytes32(s)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> swapExactTokensForTokens(BigInteger amountIn, BigInteger amountOutMin, List<String> path, String to, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_SWAPEXACTTOKENSFORTOKENS,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amountIn),
                        new org.web3j.abi.datatypes.generated.Uint256(amountOutMin),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                                org.web3j.abi.datatypes.Address.class,
                                org.web3j.abi.Utils.typeMap(path, org.web3j.abi.datatypes.Address.class)),
                        new org.web3j.abi.datatypes.Address(160, to)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> swapTokensForExactTokens(BigInteger amountOut, BigInteger amountInMax, List<String> path, String to, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_SWAPTOKENSFOREXACTTOKENS,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amountOut),
                        new org.web3j.abi.datatypes.generated.Uint256(amountInMax),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                                org.web3j.abi.datatypes.Address.class,
                                org.web3j.abi.Utils.typeMap(path, org.web3j.abi.datatypes.Address.class)),
                        new org.web3j.abi.datatypes.Address(160, to)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> sweepToken(String token, BigInteger amountMinimum, String recipient, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_sweepToken,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(amountMinimum),
                        new org.web3j.abi.datatypes.Address(160, recipient)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> sweepToken(String token, BigInteger amountMinimum, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_sweepToken,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(amountMinimum)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> sweepTokenWithFee(String token, BigInteger amountMinimum, BigInteger feeBips, String feeRecipient, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_sweepTokenWithFee,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(amountMinimum),
                        new org.web3j.abi.datatypes.generated.Uint256(feeBips),
                        new org.web3j.abi.datatypes.Address(160, feeRecipient)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> sweepTokenWithFee(String token, BigInteger amountMinimum, String recipient, BigInteger feeBips, String feeRecipient, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_sweepTokenWithFee,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(amountMinimum),
                        new org.web3j.abi.datatypes.Address(160, recipient),
                        new org.web3j.abi.datatypes.generated.Uint256(feeBips),
                        new org.web3j.abi.datatypes.Address(160, feeRecipient)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> uniswapV3SwapCallback(BigInteger amount0Delta, BigInteger amount1Delta, byte[] _data) {
        final Function function = new Function(
                FUNC_UNISWAPV3SWAPCALLBACK,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Int256(amount0Delta),
                        new org.web3j.abi.datatypes.generated.Int256(amount1Delta),
                        new org.web3j.abi.datatypes.DynamicBytes(_data)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> unwrapWETH9(BigInteger amountMinimum, String recipient, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_unwrapWETH9,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amountMinimum),
                        new org.web3j.abi.datatypes.Address(160, recipient)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> unwrapWETH9(BigInteger amountMinimum, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_unwrapWETH9,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amountMinimum)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> unwrapWETH9WithFee(BigInteger amountMinimum, String recipient, BigInteger feeBips, String feeRecipient, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_unwrapWETH9WithFee,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amountMinimum),
                        new org.web3j.abi.datatypes.Address(160, recipient),
                        new org.web3j.abi.datatypes.generated.Uint256(feeBips),
                        new org.web3j.abi.datatypes.Address(160, feeRecipient)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> unwrapWETH9WithFee(BigInteger amountMinimum, BigInteger feeBips, String feeRecipient, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_unwrapWETH9WithFee,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amountMinimum),
                        new org.web3j.abi.datatypes.generated.Uint256(feeBips),
                        new org.web3j.abi.datatypes.Address(160, feeRecipient)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> wrapETH(BigInteger value, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_WRAPETH,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    @Deprecated
    public static IUniswapV3Router load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new IUniswapV3Router(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static IUniswapV3Router load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new IUniswapV3Router(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static IUniswapV3Router load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new IUniswapV3Router(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static IUniswapV3Router load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new IUniswapV3Router(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class ExactInputParams extends DynamicStruct {
        public byte[] path;

        public String recipient;

        public BigInteger amountIn;

        public BigInteger amountOutMinimum;

        public ExactInputParams(byte[] path, String recipient, BigInteger amountIn, BigInteger amountOutMinimum) {
            super(new org.web3j.abi.datatypes.DynamicBytes(path),
                    new org.web3j.abi.datatypes.Address(160, recipient),
                    new org.web3j.abi.datatypes.generated.Uint256(amountIn),
                    new org.web3j.abi.datatypes.generated.Uint256(amountOutMinimum));
            this.path = path;
            this.recipient = recipient;
            this.amountIn = amountIn;
            this.amountOutMinimum = amountOutMinimum;
        }

        public ExactInputParams(DynamicBytes path, Address recipient, Uint256 amountIn, Uint256 amountOutMinimum) {
            super(path, recipient, amountIn, amountOutMinimum);
            this.path = path.getValue();
            this.recipient = recipient.getValue();
            this.amountIn = amountIn.getValue();
            this.amountOutMinimum = amountOutMinimum.getValue();
        }
    }

    public static class ExactInputSingleParams extends StaticStruct {
        public String tokenIn;

        public String tokenOut;

        public BigInteger fee;

        public String recipient;

        public BigInteger amountIn;

        public BigInteger amountOutMinimum;

        public BigInteger sqrtPriceLimitX96;

        public ExactInputSingleParams(String tokenIn, String tokenOut, BigInteger fee, String recipient, BigInteger amountIn, BigInteger amountOutMinimum, BigInteger sqrtPriceLimitX96) {
            super(new org.web3j.abi.datatypes.Address(160, tokenIn),
                    new org.web3j.abi.datatypes.Address(160, tokenOut),
                    new org.web3j.abi.datatypes.generated.Uint24(fee),
                    new org.web3j.abi.datatypes.Address(160, recipient),
                    new org.web3j.abi.datatypes.generated.Uint256(amountIn),
                    new org.web3j.abi.datatypes.generated.Uint256(amountOutMinimum),
                    new org.web3j.abi.datatypes.generated.Uint160(sqrtPriceLimitX96));
            this.tokenIn = tokenIn;
            this.tokenOut = tokenOut;
            this.fee = fee;
            this.recipient = recipient;
            this.amountIn = amountIn;
            this.amountOutMinimum = amountOutMinimum;
            this.sqrtPriceLimitX96 = sqrtPriceLimitX96;
        }

        public ExactInputSingleParams(Address tokenIn, Address tokenOut, Uint24 fee, Address recipient, Uint256 amountIn, Uint256 amountOutMinimum, Uint160 sqrtPriceLimitX96) {
            super(tokenIn, tokenOut, fee, recipient, amountIn, amountOutMinimum, sqrtPriceLimitX96);
            this.tokenIn = tokenIn.getValue();
            this.tokenOut = tokenOut.getValue();
            this.fee = fee.getValue();
            this.recipient = recipient.getValue();
            this.amountIn = amountIn.getValue();
            this.amountOutMinimum = amountOutMinimum.getValue();
            this.sqrtPriceLimitX96 = sqrtPriceLimitX96.getValue();
        }
    }



    public static class ExactOutputParams extends DynamicStruct {
        public byte[] path;

        public String recipient;

        public BigInteger amountOut;

        public BigInteger amountInMaximum;

        public ExactOutputParams(byte[] path, String recipient, BigInteger amountOut, BigInteger amountInMaximum) {
            super(new org.web3j.abi.datatypes.DynamicBytes(path),
                    new org.web3j.abi.datatypes.Address(160, recipient),
                    new org.web3j.abi.datatypes.generated.Uint256(amountOut),
                    new org.web3j.abi.datatypes.generated.Uint256(amountInMaximum));
            this.path = path;
            this.recipient = recipient;
            this.amountOut = amountOut;
            this.amountInMaximum = amountInMaximum;
        }

        public ExactOutputParams(DynamicBytes path, Address recipient, Uint256 amountOut, Uint256 amountInMaximum) {
            super(path, recipient, amountOut, amountInMaximum);
            this.path = path.getValue();
            this.recipient = recipient.getValue();
            this.amountOut = amountOut.getValue();
            this.amountInMaximum = amountInMaximum.getValue();
        }
    }

    public static class ExactOutputSingleParams extends StaticStruct {
        public String tokenIn;

        public String tokenOut;

        public BigInteger fee;

        public String recipient;

        public BigInteger amountOut;

        public BigInteger amountInMaximum;

        public BigInteger sqrtPriceLimitX96;

        public ExactOutputSingleParams(String tokenIn, String tokenOut, BigInteger fee, String recipient, BigInteger amountOut, BigInteger amountInMaximum, BigInteger sqrtPriceLimitX96) {
            super(new org.web3j.abi.datatypes.Address(160, tokenIn),
                    new org.web3j.abi.datatypes.Address(160, tokenOut),
                    new org.web3j.abi.datatypes.generated.Uint24(fee),
                    new org.web3j.abi.datatypes.Address(160, recipient),
                    new org.web3j.abi.datatypes.generated.Uint256(amountOut),
                    new org.web3j.abi.datatypes.generated.Uint256(amountInMaximum),
                    new org.web3j.abi.datatypes.generated.Uint160(sqrtPriceLimitX96));
            this.tokenIn = tokenIn;
            this.tokenOut = tokenOut;
            this.fee = fee;
            this.recipient = recipient;
            this.amountOut = amountOut;
            this.amountInMaximum = amountInMaximum;
            this.sqrtPriceLimitX96 = sqrtPriceLimitX96;
        }

        public ExactOutputSingleParams(Address tokenIn, Address tokenOut, Uint24 fee, Address recipient, Uint256 amountOut, Uint256 amountInMaximum, Uint160 sqrtPriceLimitX96) {
            super(tokenIn, tokenOut, fee, recipient, amountOut, amountInMaximum, sqrtPriceLimitX96);
            this.tokenIn = tokenIn.getValue();
            this.tokenOut = tokenOut.getValue();
            this.fee = fee.getValue();
            this.recipient = recipient.getValue();
            this.amountOut = amountOut.getValue();
            this.amountInMaximum = amountInMaximum.getValue();
            this.sqrtPriceLimitX96 = sqrtPriceLimitX96.getValue();
        }
    }

    public static class IncreaseLiquidityParams extends StaticStruct {
        public String token0;

        public String token1;

        public BigInteger tokenId;

        public BigInteger amount0Min;

        public BigInteger amount1Min;

        public IncreaseLiquidityParams(String token0, String token1, BigInteger tokenId, BigInteger amount0Min, BigInteger amount1Min) {
            super(new org.web3j.abi.datatypes.Address(160, token0),
                    new org.web3j.abi.datatypes.Address(160, token1),
                    new org.web3j.abi.datatypes.generated.Uint256(tokenId),
                    new org.web3j.abi.datatypes.generated.Uint256(amount0Min),
                    new org.web3j.abi.datatypes.generated.Uint256(amount1Min));
            this.token0 = token0;
            this.token1 = token1;
            this.tokenId = tokenId;
            this.amount0Min = amount0Min;
            this.amount1Min = amount1Min;
        }

        public IncreaseLiquidityParams(Address token0, Address token1, Uint256 tokenId, Uint256 amount0Min, Uint256 amount1Min) {
            super(token0, token1, tokenId, amount0Min, amount1Min);
            this.token0 = token0.getValue();
            this.token1 = token1.getValue();
            this.tokenId = tokenId.getValue();
            this.amount0Min = amount0Min.getValue();
            this.amount1Min = amount1Min.getValue();
        }
    }

    public static class MintParams extends StaticStruct {
        public String token0;

        public String token1;

        public BigInteger fee;

        public BigInteger tickLower;

        public BigInteger tickUpper;

        public BigInteger amount0Min;

        public BigInteger amount1Min;

        public String recipient;

        public MintParams(String token0, String token1, BigInteger fee, BigInteger tickLower, BigInteger tickUpper, BigInteger amount0Min, BigInteger amount1Min, String recipient) {
            super(new org.web3j.abi.datatypes.Address(160, token0),
                    new org.web3j.abi.datatypes.Address(160, token1),
                    new org.web3j.abi.datatypes.generated.Uint24(fee),
                    new org.web3j.abi.datatypes.generated.Int24(tickLower),
                    new org.web3j.abi.datatypes.generated.Int24(tickUpper),
                    new org.web3j.abi.datatypes.generated.Uint256(amount0Min),
                    new org.web3j.abi.datatypes.generated.Uint256(amount1Min),
                    new org.web3j.abi.datatypes.Address(160, recipient));
            this.token0 = token0;
            this.token1 = token1;
            this.fee = fee;
            this.tickLower = tickLower;
            this.tickUpper = tickUpper;
            this.amount0Min = amount0Min;
            this.amount1Min = amount1Min;
            this.recipient = recipient;
        }

        public MintParams(Address token0, Address token1, Uint24 fee, Int24 tickLower, Int24 tickUpper, Uint256 amount0Min, Uint256 amount1Min, Address recipient) {
            super(token0, token1, fee, tickLower, tickUpper, amount0Min, amount1Min, recipient);
            this.token0 = token0.getValue();
            this.token1 = token1.getValue();
            this.fee = fee.getValue();
            this.tickLower = tickLower.getValue();
            this.tickUpper = tickUpper.getValue();
            this.amount0Min = amount0Min.getValue();
            this.amount1Min = amount1Min.getValue();
            this.recipient = recipient.getValue();
        }
    }
}