package com.forknight.server.scanner;

import com.forknight.common.enums.SecretSeverity;
import com.forknight.common.enums.SecretType;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Defines secret detection rules with compiled regex patterns and severities.
 */
public record SecretPattern(
        String ruleName,
        SecretType type,
        SecretSeverity severity,
        Pattern pattern,
        String description
) {
    public static List<SecretPattern> getDefaultRules() {
        return List.of(
                new SecretPattern(
                        "AWS Access Key",
                        SecretType.AWS_SECRET,
                        SecretSeverity.CRITICAL,
                        Pattern.compile("\\b(AKIA[0-9A-Z]{16})\\b"),
                        "AWS Access Key ID detected"
                ),
                new SecretPattern(
                        "AWS Secret Key",
                        SecretType.AWS_SECRET,
                        SecretSeverity.CRITICAL,
                        Pattern.compile("(?i)(aws_secret_access_key|aws_secret_key)\\s*[:=]\\s*['\"]?([A-Za-z0-9/+=]{40})['\"]?"),
                        "AWS Secret Access Key assignment detected"
                ),
                new SecretPattern(
                        "GitHub Personal Access Token",
                        SecretType.TOKEN,
                        SecretSeverity.CRITICAL,
                        Pattern.compile("\\b(ghp_[a-zA-Z0-9]{36}|github_pat_[a-zA-Z0-9_]{82})\\b"),
                        "GitHub Personal Access Token detected"
                ),
                new SecretPattern(
                        "Slack Token",
                        SecretType.TOKEN,
                        SecretSeverity.HIGH,
                        Pattern.compile("\\b(xox[baprs]-[0-9]{10,13}-[0-9a-zA-Z-]+)\\b"),
                        "Slack API token detected"
                ),
                new SecretPattern(
                        "Private Key",
                        SecretType.PRIVATE_KEY,
                        SecretSeverity.CRITICAL,
                        Pattern.compile("-----BEGIN (?:RSA|OPENSSH|EC|DSA|PGP|ENCRYPTED)?\\s?PRIVATE KEY-----"),
                        "Embedded Private Key block detected"
                ),
                new SecretPattern(
                        "Database Connection URI with Credentials",
                        SecretType.DATABASE_URI,
                        SecretSeverity.HIGH,
                        Pattern.compile("(?i)(?:postgres|postgresql|mysql|mongodb|redis)://[^:\\s]+:[^@\\s]+@[^\\s]+"),
                        "Database URI containing embedded credentials"
                ),
                new SecretPattern(
                        "Hardcoded Password Assignment",
                        SecretType.PASSWORD,
                        SecretSeverity.HIGH,
                        Pattern.compile("(?i)(?:password|passwd|pwd)\\s*[:=]\\s*['\"]([^'\"]{8,})['\"]"),
                        "Hardcoded password assignment detected"
                ),
                new SecretPattern(
                        "Generic API Key / Secret Token",
                        SecretType.API_KEY,
                        SecretSeverity.MEDIUM,
                        Pattern.compile("(?i)(?:api_key|apikey|secret_key|auth_token|client_secret)\\s*[:=]\\s*['\"]([a-zA-Z0-9_\\-\\.]{20,})['\"]"),
                        "Generic API key or secret assignment"
                )
        );
    }
}
