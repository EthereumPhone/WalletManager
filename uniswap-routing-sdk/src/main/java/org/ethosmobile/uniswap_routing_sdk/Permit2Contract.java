package org.ethosmobile.uniswap_routing_sdk;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint160;
import org.web3j.abi.datatypes.generated.Uint48;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Permit2Contract extends Contract {
    public static final String BINARY = "Bin file was not provided";

    protected Permit2Contract(String contractBinary, String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(contractBinary, contractAddress, web3j, transactionManager, gasProvider);
    }

    protected Permit2Contract(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Permit2Contract load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Permit2Contract(contractAddress, web3j, credentials, contractGasProvider);
    }

    public List<Type> allowance(String user, String token, String spender) throws IOException {
        final Function function = new Function("allowance",
                Arrays.<Type>asList(new Address(160, user),
                        new Address(160, token),
                        new Address(160, spender)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint160>() {}, new TypeReference<Uint48>() {}, new TypeReference<Uint48>() {}));
        return executeCallMultipleValueReturn(function);
    }

}
