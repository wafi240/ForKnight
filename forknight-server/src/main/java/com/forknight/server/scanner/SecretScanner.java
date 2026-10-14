package com.forknight.server.scanner;

import com.forknight.common.enums.SecretSeverity;
import com.forknight.common.enums.SecretType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@Component
public class SecretScanner {

    private final List<SecretPattern> rules;

    public SecretScanner() {
        this.rules = SecretPattern.getDefaultRules();
    }

    public record Finding(
            String filePath,
            int lineNumber,
            SecretType secretType,
            String ruleName,
            String maskedSnippet,
            SecretSeverity severity
    ) {}

    /**
     * Scans content string line by line against registered secret patterns and entropy checks.
     */
    public List<Finding> scanContent(String filePath, String content) {
        List<Finding> findings = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return findings;
        }

        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String line = lines[i];

            // 1. Regex rule checks
            for (SecretPattern rule : rules) {
                Matcher matcher = rule.pattern().matcher(line);
                if (matcher.find()) {
                    String matchedText = matcher.group();
                    findings.add(new Finding(
                            filePath,
                            lineNumber,
                            rule.type(),
                            rule.ruleName(),
                            maskSecret(matchedText),
                            rule.severity()
                    ));
                    break; // Move to next line once a critical pattern matches
                }
            }

            // 2. Shannon entropy check for potential unclassified high-entropy keys
            checkHighEntropyTokens(filePath, lineNumber, line, findings);
        }

        return findings;
    }

    private void checkHighEntropyTokens(String filePath, int lineNumber, String line, List<Finding> findings) {
        // Skip lines that are too long or comment lines
        String trimmed = line.trim();
        if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("*")) {
            return;
        }

        // Look for tokens between quotes or assignment
        String[] tokens = line.split("[\\s\"'=:;,]+");
        for (String token : tokens) {
            if (token.length() >= 32 && calculateShannonEntropy(token) > 4.5) {
                // Ensure it wasn't already caught by regex
                boolean alreadyCaught = findings.stream().anyMatch(f -> f.lineNumber() == lineNumber);
                if (!alreadyCaught) {
                    findings.add(new Finding(
                            filePath,
                            lineNumber,
                            SecretType.HIGH_ENTROPY_STRING,
                            "High Entropy String",
                            maskSecret(token),
                            SecretSeverity.MEDIUM
                    ));
                    break;
                }
            }
        }
    }

    /**
     * Calculates Shannon entropy of a string (measures randomness).
     */
    public static double calculateShannonEntropy(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : input.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }
        double entropy = 0.0;
        int length = input.length();
        for (int count : freq.values()) {
            double p = (double) count / length;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    /**
     * Masks sensitive text for safe display (e.g. "AKIA****************3XYZ").
     */
    public static String maskSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            return "***";
        }
        if (secret.length() <= 8) {
            return "*".repeat(secret.length());
        }
        int keep = Math.min(4, secret.length() / 4);
        String prefix = secret.substring(0, keep);
        String suffix = secret.substring(secret.length() - keep);
        return prefix + "*".repeat(Math.max(4, secret.length() - (keep * 2))) + suffix;
    }
}
