package com.hjrpc;

import java.util.*;

public class ConsistentHash {
    // 虚拟节点数，用户指定
    private int virtualNum = 100;
    // 物理节点集合
    private final Set<String> realNodes = new HashSet<>();
    // 物理节点与虚拟节点的对应关系存储(方便移除服务器)
    private final Map<String, List<Integer>> real2VirtualMap = new HashMap<>();
    // 排序存储结构红黑树，key是虚拟节点的hash值，value是物理节点
    private final SortedMap<Integer, String> sortedMap = new TreeMap<>();

    public ConsistentHash() {
    }

    public ConsistentHash(int virtualNum) {
        this.virtualNum = virtualNum;
    }

    public void registerServer(String serverIp) {
        if(realNodes.contains(serverIp)){
            return;
        }
        realNodes.add(serverIp);
        // 根据物理节点生成指定个虚拟节点
        int i = 0;
        List<Integer> virtualNodeList = new ArrayList<>();
        while (i < virtualNum) {
            String virtualName = serverIp + "-" + i;
            int hash = FNV1_32_HASH.getHash(virtualName);
            if (!sortedMap.containsKey(hash)) {
                this.sortedMap.put(hash, serverIp);
                virtualNodeList.add(hash);
                i++;
            }
        }
        // 物理节点与虚拟节点的对应关系存储(方便移除服务器)
        real2VirtualMap.put(serverIp, virtualNodeList);
    }

    public String getServerIpByClientIp(String clientIp) {
        int hash = FNV1_32_HASH.getHash(clientIp);
        SortedMap<Integer, String> currentSortedMap = this.sortedMap.tailMap(hash);
        // 最后一个元素，返回第一个node
        if (currentSortedMap.isEmpty()) {
            return sortedMap.get(sortedMap.firstKey());
        }
        return currentSortedMap.get(currentSortedMap.firstKey());
    }

    public void removeServer(String serverIp) {
        realNodes.remove(serverIp);
        List<Integer> keys = real2VirtualMap.get(serverIp);
        for (Integer key : keys) {
            sortedMap.remove(key);
        }
        real2VirtualMap.remove(serverIp);
    }
}
