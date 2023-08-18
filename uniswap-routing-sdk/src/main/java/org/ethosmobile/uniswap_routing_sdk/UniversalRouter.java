package org.ethosmobile.uniswap_routing_sdk;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
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
public class UniversalRouter extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_COLLECTREWARDS = "collectRewards";

    public static final String FUNC_execute = "execute";

    public static final String FUNC_ONERC1155BATCHRECEIVED = "onERC1155BatchReceived";

    public static final String FUNC_ONERC1155RECEIVED = "onERC1155Received";

    public static final String FUNC_ONERC721RECEIVED = "onERC721Received";

    public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

    public static final String FUNC_UNISWAPV3SWAPCALLBACK = "uniswapV3SwapCallback";

    public static final Event REWARDSSENT_EVENT = new Event("RewardsSent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected UniversalRouter(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected UniversalRouter(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected UniversalRouter(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected UniversalRouter(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }


    public Flowable<RewardsSentEventResponse> rewardsSentEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RewardsSentEventResponse>() {
            @Override
            public RewardsSentEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(REWARDSSENT_EVENT, log);
                RewardsSentEventResponse typedResponse = new RewardsSentEventResponse();
                typedResponse.log = log;
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RewardsSentEventResponse> rewardsSentEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REWARDSSENT_EVENT));
        return rewardsSentEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> collectRewards(byte[] looksRareClaim) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_COLLECTREWARDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(looksRareClaim)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> execute(byte[] commands, List<byte[]> inputs, BigInteger weiValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_execute, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(commands), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.DynamicBytes>(
                        org.web3j.abi.datatypes.DynamicBytes.class,
                        org.web3j.abi.Utils.typeMap(inputs, org.web3j.abi.datatypes.DynamicBytes.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> execute(byte[] commands, List<byte[]> inputs, BigInteger deadline, BigInteger weiValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_execute, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(commands), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.DynamicBytes>(
                        org.web3j.abi.datatypes.DynamicBytes.class,
                        org.web3j.abi.Utils.typeMap(inputs, org.web3j.abi.datatypes.DynamicBytes.class)), 
                new Uint256(deadline)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<byte[]> onERC1155BatchReceived(String param0, String param1, List<BigInteger> param2, List<BigInteger> param3, byte[] param4) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ONERC1155BATCHRECEIVED, 
                Arrays.<Type>asList(new Address(160, param0),
                new Address(160, param1),
                new org.web3j.abi.datatypes.DynamicArray<Uint256>(
                        Uint256.class,
                        org.web3j.abi.Utils.typeMap(param2, Uint256.class)),
                new org.web3j.abi.datatypes.DynamicArray<Uint256>(
                        Uint256.class,
                        org.web3j.abi.Utils.typeMap(param3, Uint256.class)),
                new org.web3j.abi.datatypes.DynamicBytes(param4)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes4>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<byte[]> onERC1155Received(String param0, String param1, BigInteger param2, BigInteger param3, byte[] param4) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ONERC1155RECEIVED, 
                Arrays.<Type>asList(new Address(160, param0),
                new Address(160, param1),
                new Uint256(param2),
                new Uint256(param3),
                new org.web3j.abi.datatypes.DynamicBytes(param4)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes4>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<byte[]> onERC721Received(String param0, String param1, BigInteger param2, byte[] param3) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ONERC721RECEIVED, 
                Arrays.<Type>asList(new Address(160, param0),
                new Address(160, param1),
                new Uint256(param2),
                new org.web3j.abi.datatypes.DynamicBytes(param3)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes4>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SUPPORTSINTERFACE, 
                Arrays.<Type>asList(new Bytes4(interfaceId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> uniswapV3SwapCallback(BigInteger amount0Delta, BigInteger amount1Delta, byte[] data) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_UNISWAPV3SWAPCALLBACK, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Int256(amount0Delta), 
                new org.web3j.abi.datatypes.generated.Int256(amount1Delta), 
                new org.web3j.abi.datatypes.DynamicBytes(data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static UniversalRouter load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new UniversalRouter(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static UniversalRouter load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new UniversalRouter(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static UniversalRouter load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new UniversalRouter(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static UniversalRouter load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new UniversalRouter(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class RouterParameters extends StaticStruct {
        public String permit2;

        public String weth9;

        public String seaport;

        public String seaportV1_4;

        public String openseaConduit;

        public String nftxZap;

        public String x2y2;

        public String foundation;

        public String sudoswap;

        public String elementMarket;

        public String nft20Zap;

        public String cryptopunks;

        public String looksRare;

        public String routerRewardsDistributor;

        public String looksRareRewardsDistributor;

        public String looksRareToken;

        public String v2Factory;

        public String v3Factory;

        public byte[] pairInitCodeHash;

        public byte[] poolInitCodeHash;

        public RouterParameters(String permit2, String weth9, String seaport, String seaportV1_4, String openseaConduit, String nftxZap, String x2y2, String foundation, String sudoswap, String elementMarket, String nft20Zap, String cryptopunks, String looksRare, String routerRewardsDistributor, String looksRareRewardsDistributor, String looksRareToken, String v2Factory, String v3Factory, byte[] pairInitCodeHash, byte[] poolInitCodeHash) {
            super(new Address(160, permit2),
                    new Address(160, weth9),
                    new Address(160, seaport),
                    new Address(160, seaportV1_4),
                    new Address(160, openseaConduit),
                    new Address(160, nftxZap),
                    new Address(160, x2y2),
                    new Address(160, foundation),
                    new Address(160, sudoswap),
                    new Address(160, elementMarket),
                    new Address(160, nft20Zap),
                    new Address(160, cryptopunks),
                    new Address(160, looksRare),
                    new Address(160, routerRewardsDistributor),
                    new Address(160, looksRareRewardsDistributor),
                    new Address(160, looksRareToken),
                    new Address(160, v2Factory),
                    new Address(160, v3Factory),
                    new Bytes32(pairInitCodeHash),
                    new Bytes32(poolInitCodeHash));
            this.permit2 = permit2;
            this.weth9 = weth9;
            this.seaport = seaport;
            this.seaportV1_4 = seaportV1_4;
            this.openseaConduit = openseaConduit;
            this.nftxZap = nftxZap;
            this.x2y2 = x2y2;
            this.foundation = foundation;
            this.sudoswap = sudoswap;
            this.elementMarket = elementMarket;
            this.nft20Zap = nft20Zap;
            this.cryptopunks = cryptopunks;
            this.looksRare = looksRare;
            this.routerRewardsDistributor = routerRewardsDistributor;
            this.looksRareRewardsDistributor = looksRareRewardsDistributor;
            this.looksRareToken = looksRareToken;
            this.v2Factory = v2Factory;
            this.v3Factory = v3Factory;
            this.pairInitCodeHash = pairInitCodeHash;
            this.poolInitCodeHash = poolInitCodeHash;
        }

        public RouterParameters(Address permit2, Address weth9, Address seaport, Address seaportV1_4, Address openseaConduit, Address nftxZap, Address x2y2, Address foundation, Address sudoswap, Address elementMarket, Address nft20Zap, Address cryptopunks, Address looksRare, Address routerRewardsDistributor, Address looksRareRewardsDistributor, Address looksRareToken, Address v2Factory, Address v3Factory, Bytes32 pairInitCodeHash, Bytes32 poolInitCodeHash) {
            super(permit2, weth9, seaport, seaportV1_4, openseaConduit, nftxZap, x2y2, foundation, sudoswap, elementMarket, nft20Zap, cryptopunks, looksRare, routerRewardsDistributor, looksRareRewardsDistributor, looksRareToken, v2Factory, v3Factory, pairInitCodeHash, poolInitCodeHash);
            this.permit2 = permit2.getValue();
            this.weth9 = weth9.getValue();
            this.seaport = seaport.getValue();
            this.seaportV1_4 = seaportV1_4.getValue();
            this.openseaConduit = openseaConduit.getValue();
            this.nftxZap = nftxZap.getValue();
            this.x2y2 = x2y2.getValue();
            this.foundation = foundation.getValue();
            this.sudoswap = sudoswap.getValue();
            this.elementMarket = elementMarket.getValue();
            this.nft20Zap = nft20Zap.getValue();
            this.cryptopunks = cryptopunks.getValue();
            this.looksRare = looksRare.getValue();
            this.routerRewardsDistributor = routerRewardsDistributor.getValue();
            this.looksRareRewardsDistributor = looksRareRewardsDistributor.getValue();
            this.looksRareToken = looksRareToken.getValue();
            this.v2Factory = v2Factory.getValue();
            this.v3Factory = v3Factory.getValue();
            this.pairInitCodeHash = pairInitCodeHash.getValue();
            this.poolInitCodeHash = poolInitCodeHash.getValue();
        }
    }

    public static class RewardsSentEventResponse extends BaseEventResponse {
        public BigInteger amount;
    }
}
