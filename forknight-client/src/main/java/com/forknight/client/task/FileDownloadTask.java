package com.forknight.client.task;

import com.forknight.client.api.FileDownloadApi;
import com.forknight.common.dto.repo.FileContentDTO;
import com.forknight.common.dto.repo.SingleFileDownloadRequest;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Background task to download a single file from a remote repository with progress updates.
 */
public class FileDownloadTask extends Task<Path> {

    private final SingleFileDownloadRequest request;
    private final File destinationDirectory;
    private final FileDownloadApi api = new FileDownloadApi();

    public FileDownloadTask(SingleFileDownloadRequest request, File destinationDirectory) {
        this.request = request;
        this.destinationDirectory = destinationDirectory;
    }

    @Override
    protected Path call() throws Exception {
        updateMessage("Connecting to repository: " + request.owner() + "/" + request.repo() + "...");
        updateProgress(10, 100);

        updateMessage("Fetching file: " + request.filePath() + " (without cloning repo)...");
        updateProgress(30, 100);

        FileContentDTO fileDto = api.downloadSingleFile(request).get();

        updateMessage("File fetched (" + fileDto.size() + " bytes). Writing to disk...");
        updateProgress(70, 100);

        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }

        File targetFile = new File(destinationDirectory, fileDto.fileName());
        Files.writeString(targetFile.toPath(), fileDto.content(), StandardCharsets.UTF_8);

        updateProgress(100, 100);
        updateMessage("Saved successfully to: " + targetFile.getName());

        return targetFile.toPath();
    }
}
