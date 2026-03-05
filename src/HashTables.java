import java.util.*;
import java.util.concurrent.*;

class UsernameChecker {

    private ConcurrentHashMap<String, Integer> usernameMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> attemptFrequency = new ConcurrentHashMap<>();

    public void registerUser(String username, int userId) {
        usernameMap.put(username.toLowerCase(), userId);
    }

    public boolean checkAvailability(String username) {
        username = username.toLowerCase();
        attemptFrequency.merge(username, 1, Integer::sum);
        return !usernameMap.containsKey(username);
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        username = username.toLowerCase();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!usernameMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        if (username.contains("_")) {
            String dotVersion = username.replace("_", ".");
            if (!usernameMap.containsKey(dotVersion)) {
                suggestions.add(dotVersion);
            }
        }

        return suggestions;
    }

    public String getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}

public class HashTables {
    public static void main(String[] args) {

        UsernameChecker system = new UsernameChecker();

        system.registerUser("john_doe", 101);
        system.registerUser("admin", 1);
        system.registerUser("player1", 202);

        System.out.println(system.checkAvailability("john_doe"));
        System.out.println(system.checkAvailability("jane_smith"));

        system.checkAvailability("admin");
        system.checkAvailability("admin");
        system.checkAvailability("admin");

        System.out.println(system.suggestAlternatives("john_doe"));
        System.out.println(system.getMostAttempted());
    }
}