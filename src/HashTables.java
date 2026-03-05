import java.util.*;
import java.util.concurrent.*;

class DNSCache {

    private static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int maxSize;
    private final Map<String, DNSEntry> cache;
    private long hits = 0;
    private long misses = 0;

    public DNSCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = Collections.synchronizedMap(
                new LinkedHashMap<String, DNSEntry>(16, 0.75f, true) {
                    protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                        return size() > DNSCache.this.maxSize;
                    }
                }
        );
        startCleanupThread();
    }

    public String resolve(String domain) {
        long start = System.nanoTime();
        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            long time = System.nanoTime() - start;
            return "Cache HIT → " + entry.ipAddress + " (" + (time / 1_000_000.0) + " ms)";
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
        }

        misses++;
        String ip = queryUpstreamDNS(domain);
        DNSEntry newEntry = new DNSEntry(domain, ip, 5);
        cache.put(domain, newEntry);
        return "Cache MISS → " + ip;
    }

    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    public String getCacheStats() {
        long total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0) / total;
        return "Hit Rate: " + String.format("%.2f", hitRate) + "%";
    }

    private void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    synchronized (cache) {
                        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                        while (it.hasNext()) {
                            if (it.next().getValue().isExpired()) {
                                it.remove();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleaner.setDaemon(true);
        cleaner.start();
    }
}

public class HashTables {
    public static void main(String[] args) throws InterruptedException {

        DNSCache dnsCache = new DNSCache(3);

        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("google.com"));

        Thread.sleep(6000);

        System.out.println(dnsCache.resolve("google.com"));

        System.out.println(dnsCache.getCacheStats());
    }
}