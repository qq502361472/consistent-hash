package com.hjrpc;

import java.util.*;

public class TestMain {
    private static final ConsistentHash consistentHash = new ConsistentHash();
    private static final Random random = new Random();
    private static final Set<Integer> existIp = new HashSet<>();
    private static final String ipPrefix = "192.168.56.";
    private static final List<String> ipsString = new ArrayList<>();

    public static void main(String[] args) {
        getBatchRandomIps(5).forEach(TestMain::registerServer);
        batchTest();
        getBatchRandomIps(1).forEach(TestMain::registerServer);
        batchTest();
        removeServer();
        batchTest();
    }

    private static List<String> getBatchRandomIps(Integer num) {
        List<String> ips = new ArrayList<>();
        int i = 1;
        while (i <= num) {
            int randomCode = random.nextInt(255);
            if (!existIp.contains(randomCode)) {
                String ipString = ipPrefix + randomCode;
                ips.add(ipString);
                existIp.add(randomCode);
                i++;
                System.out.println("注册新的IP:["+ipString+"]");
            }
        }
        ipsString.addAll(ips);
        return ips;
    }

    private static void batchTest() {
        getAndPrintIp("192.168.1.101");
        getAndPrintIp("192.168.1.102");
        getAndPrintIp("192.168.1.103");
        getAndPrintIp("192.168.1.104");
        getAndPrintIp("192.168.1.105");
        getAndPrintIp("192.168.1.106");
        System.out.println("-------------------------------------------------");
    }

    private static void getAndPrintIp(String clientIp) {
        String serverIpByClientIp = consistentHash.getServerIpByClientIp(clientIp);
        System.out.println("clientIp[" + clientIp + "],serverIp[" + serverIpByClientIp + "].");
    }

    private static void registerServer(String serverIp) {
        consistentHash.registerServer(serverIp);
    }

    private static void removeServer() {
        String ipStr = null;
        for (Integer ip : existIp) {
            ipStr = ipPrefix + ip;
            existIp.remove(ip);
            ipsString.remove(ipStr);
            break;
        }
        consistentHash.removeServer(ipStr);
        System.out.println("remove[" + ipStr + "]");
    }
}
