package blockchain;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static boolean validate = false;
    private static boolean manipulate = false;
    private static boolean printOutput = false;
    private static boolean outDebugInformation = false;
    private static boolean serializeChain = false;
    private static boolean getNumbersOfZeroesFromInput = false;

    enum PRINT_OPTIONS {
        ALL, RANGE, FROM_START
    }

    private static PRINT_OPTIONS printOptions = PRINT_OPTIONS.RANGE;
    private static int printStart = 0;
    private static int printEnd = 5;

    private final static String filename = "blockchain.ser";

    public static void main(String[] args) throws NullPointerException, ExecutionException, InterruptedException, IOException, ClassNotFoundException {

        Map<String, String> arguments = new HashMap<>();

        for (String arg : args) {
            String[] keyValue = arg.split("=");
            arguments.put(keyValue[0], keyValue.length == 2 ? keyValue[1] : "on");
        }

        if (arguments.containsKey("-validate") && "on".equals(arguments.get("-validate"))) {
            validate = true;
        }

        if (arguments.containsKey("-manipulate") && "on".equals(arguments.get("-manipulate"))) {
            manipulate = true;
        }

        if (arguments.containsKey("-serialize") && "on".equals(arguments.get("-serialize"))) {
            serializeChain = true;
        }

        if (arguments.containsKey("-inputNumbers") && "on".equals(arguments.get("-inputNumbers"))) {
            getNumbersOfZeroesFromInput = true;
        }

        if (arguments.containsKey("-p")) {
            String printArg = arguments.get("-p");

            if (printArg.matches("[0-9]+:[0-9]+")) {
                printOptions = PRINT_OPTIONS.RANGE;
                String[] printArgs = printArg.split(":");
                printStart = Integer.parseInt(printArgs[0]);
                printEnd = Integer.parseInt(printArgs[1]);
            } else if (printArg.matches("[0-9]+")) {
                printOptions = PRINT_OPTIONS.FROM_START;
                printStart = Integer.parseInt(printArg);
            } else if ("all".equals(printArg)){
                printOptions = PRINT_OPTIONS.ALL;
            }
        }

        if (arguments.containsKey("-v") && "on".equals(arguments.get("-v"))) {
            printOutput = true;
            printOptions = PRINT_OPTIONS.ALL;
            validate = true;
        }

        if (arguments.containsKey("-vv") && "on".equals(arguments.get("-vv"))) {
            printOutput = true;
            outDebugInformation = true;
            printOptions = PRINT_OPTIONS.ALL;
            validate = true;
        }

        long start;
        Blockchain blockchain;

        int numberOfZeroes = 1;

        if (getNumbersOfZeroesFromInput) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter how many zeros the hash must starts with:");
            numberOfZeroes = Integer.parseInt(scanner.next());
        }

        start = new Date().getTime();

        if (serializeChain) {
            try {
                FileInputStream fileInputStream = new FileInputStream(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                blockchain = (Blockchain) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
                if (!Blockchain.isValid(blockchain.blocks)) {
                    throw new BlockchainException("Invalid Blockchain, starting new.");
                }
                blockchain.setNumbersOfZeroes(numberOfZeroes);
            } catch (InvalidClassException | BlockchainException | FileNotFoundException e) {
                blockchain = new Blockchain(numberOfZeroes);
            }
        } else {
            blockchain = new Blockchain(numberOfZeroes);
        }

        Blockchain.setOutputOptions(printOutput, outDebugInformation);

        int numberOfBlocks;

        if (arguments.containsKey("-blocks")) {
            numberOfBlocks = Math.max(Math.abs(Integer.parseInt(arguments.get("-blocks"))), 1);
        } else {
            numberOfBlocks = 10;
        }

        int numberOfMiners;

        if (arguments.containsKey("-miners")) {
            numberOfMiners = Math.max(Math.abs(Integer.parseInt(arguments.get("-miners"))), 1);
        } else {
            numberOfMiners = 5;
        }

        // mining
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < numberOfBlocks; i++) {

            Blockchain.debugOutput("A new round of mining: " + (i+1), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);

            String superSecretData = "Super secure Data: " + new Date().getTime();

            // @TODO Add data to chain, not to miner

            List<Callable<Void>> callables = new ArrayList<>();

            for (int j = 1; j <= numberOfMiners; j++) {
                callables.add(toCallable(new Miner(blockchain, j, superSecretData)));
            }

            if (executorService.isShutdown()) {
                executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            }

            executorService.invokeAny(callables);

            Blockchain.debugOutput("Sending \"Give up\" to other miners...", Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);

            executorService.shutdownNow();
        }

        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        long endGenerating = new Date().getTime();

        switch (printOptions) {
            case FROM_START:
                blockchain.print(printStart);
                break;
            case ALL:
                blockchain.print();
                break;
            case RANGE:
            default:
                blockchain.print(printStart, printEnd);
                break;
        }

        long endPrinting = new Date().getTime();

        long endManipulating = 0L;
        long endValidatingAfterManipulation = 0L;

        if (validate) {
            // validating
            blockchain.validate();
        }

        long endValidating = new Date().getTime();

        if (manipulate) {
            // manipulating data
            if (blockchain.blocks.size() > 2) {
                Blockchain.debugOutput("Manipulating the data", Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);
                blockchain.blocks.get(blockchain.blocks.size() - 2).information.put("data", "super_secure_data");

                endManipulating = new Date().getTime();
                blockchain.validate();
                endValidatingAfterManipulation = new Date().getTime();
            } else {
                manipulate = false;
            }
        }

        long end = new Date().getTime();

        Blockchain.debugOutput(String.format("Running time:                       %f seconds", (float) (end - start) / 1000.0), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);
        Blockchain.debugOutput(String.format("Generating time:                    %f seconds", (float) (endGenerating - start) / 1000.0), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);
        Blockchain.debugOutput(String.format("Printing time:                      %f seconds", (float) (endPrinting - endGenerating) / 1000.0), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);

        if (validate) {
            Blockchain.debugOutput(String.format("Validating time:                    %f seconds", (float) (endValidating - endPrinting) / 1000.0), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);
        }
        if (manipulate) {
            Blockchain.debugOutput(String.format("Manipulating time:                  %f seconds", (float) (endManipulating - endValidating) / 1000.0), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);
            Blockchain.debugOutput(String.format("Validating time after Manipulation: %f seconds", (float) (endValidatingAfterManipulation - endManipulating) / 1000.0), Blockchain.LOG_TYPE.INFO, Blockchain.LOG_SENDER.APP);
        }

        if (serializeChain) {
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream outstr = new ObjectOutputStream(file);
            outstr.writeObject(blockchain);
            outstr.close();
            file.close();
        }
    }

    private static Callable<Void> toCallable(final Runnable runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }
}

