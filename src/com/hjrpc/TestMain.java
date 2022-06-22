package com.hjrpc;

import java.util.*;

public class TestMain {
    // 一致性hash对象
    private static final ConsistentHash consistentHash = new ConsistentHash();
    // 随机对象，方便后续生成随机IP
    private static final Random random = new Random();
    // 避免随机生成重复的IP
    private static final Set<Integer> existIp = new HashSet<>();
    // 服务端IP前缀
    private static final String serverIpPrefix = "192.168.56.";
    // 客户端IP前缀
    private static final String clientIpPrefix = "192.168.1.";
    // 随机生成的服务端IP
    private static final List<String> serverIps = getBatchRandomIps(5, serverIpPrefix);
    // 随机生成的客户端IP
    private static final List<String> clientIps = getBatchRandomIps(200, clientIpPrefix);
    // 待打印的结果
    private static final Map<String, String> printMap = new TreeMap<>();
    // 记录新增加的IP（方便打印）
    private static String newIp;
    // 记录移除的IP（方便打印）
    private static String removeIp;

    public static void main(String[] args) {
        // 注册所有服务
        serverIps.forEach(TestMain::registerServer);
        // 批量测试客户端获取IP
        batchTestClientGetAndPrint();
        // 重新获取2个IP进行注册
        getBatchRandomIps(1, serverIpPrefix).forEach(x -> {
            newIp = x;
            registerServer(x);
        });
        // 批量测试客户端获取IP
        batchTestClientGetAndPrint();
        // 随机下线2个IP并打印
        removeServer(1);
        // 批量测试客户端获取IP
        batchTestClientGetAndPrint();
        // 打印结果
        printResult();
    }

    private static void printResult() {
        System.out.println("    客户端IP                 服务端IP           +[" + String.format("%-14s", newIp)
                + "]       -[" + String.format("%-14s", removeIp) + "]");
        printMap.values().forEach(System.out::println);
    }

    private static List<String> getBatchRandomIps(Integer num, String ipPrefix) {
        List<String> ips = new ArrayList<>();
        int i = 1;
        while (i <= num) {
            int randomCode = random.nextInt(255) + 1;
            if (!existIp.contains(randomCode)) {
                String ipString = ipPrefix + randomCode;
                ips.add(ipString);
                existIp.add(randomCode);
                i++;
            }
        }
        return ips;
    }

    private static void getAndPrintIp(String clientIp) {
        String serverIpByClientIp = consistentHash.getServerIpByClientIp(clientIp);
        String printStr = printMap.get(clientIp);
        if (printStr == null) {
            printStr = "[" + String.format("%-13s", clientIp) + "]  ===>  [" + String.format("%-14s", serverIpByClientIp) + "]";
        } else {
            printStr += "  ===>  [" + String.format("%-14s", serverIpByClientIp) + "]";
        }
        printMap.put(clientIp, printStr);
    }

    private static void registerServer(String serverIp) {
        consistentHash.registerServer(serverIp);
    }


    private static void removeServer(int num) {
        for (int i = 0; i < num; i++) {
            String serverIp = serverIps.get(random.nextInt(serverIps.size()));
            consistentHash.removeServer(serverIp);
            removeIp = serverIp;
        }
    }

    private static void batchTestClientGetAndPrint() {
        clientIps.forEach(TestMain::getAndPrintIp);
    }
}
