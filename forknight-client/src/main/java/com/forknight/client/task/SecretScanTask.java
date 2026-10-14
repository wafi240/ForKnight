package com.forknight.client.task;

import com.forknight.client.api.SecretScanApi;
import com.forknight.common.dto.secret.SecretScanResultDTO;
import javafx.concurrent.Task;

import java.util.List;

/**
 * Background task to run secret detection on a repository.
 */
public class SecretScanTask extends Task<List<SecretScanResultDTO>> {

    private final Long repoId;
    private final SecretScanApi secretScanApi = new SecretScanApi();

    public SecretScanTask(Long repoId) {
        this.repoId = repoId;
    }

    @Override
    protected List<SecretScanResultDTO> call() throws Exception {
        updateMessage("Initializing secret scanner engine...");
        updateProgress(15, 100);

        updateMessage("Scanning repository code for hardcoded secrets, tokens, and keys...");
        updateProgress(50, 100);

        List<SecretScanResultDTO> results = secretScanApi.scanRepository(repoId).get();

        updateProgress(100, 100);
        updateMessage("Scan complete. Detected " + results.size() + " findings.");

        return results;
    }
}
