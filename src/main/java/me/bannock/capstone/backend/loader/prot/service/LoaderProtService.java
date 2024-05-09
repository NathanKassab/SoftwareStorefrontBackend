package me.bannock.capstone.backend.loader.prot.service;


import org.springframework.security.access.annotation.Secured;

import java.io.File;
import java.util.Optional;

public interface LoaderProtService {

    /**
     * Starts a loader creation job. This includes watermarking and obfuscating the loader.
     * @param apiKey The api key the launcher will use
     * @param uid The uid of the user with this api key
     * @return The loader job
     * @throws LoaderProtServiceException If something goes wrong while creating the loader
     */
    @Secured("PRIV_LAUNCH_LOADER")
    LoaderProtJobDto startLoaderCreationJob(String apiKey, long uid) throws LoaderProtServiceException;

    /**
     * Gets the dto of a job with an id
     * @param id The id of the job
     * @return The dto of the job, if it could be found
     */
    Optional<LoaderProtJobDto> getJobDto(String id);

    /**
     * @param id The id of the job
     * @return The current status of the job, if it was found
     */
    Optional<String> getJobStatus(String id);

    /**
     * Gets the file containing the built loader
     * @param id THe job id
     * @return The file location, if the job was found
     */
    Optional<File> getFinishedJobOutput(String id);

    /**
     * Checks if a specific user is allowed to access a specific job
     * @param uid The user's id
     * @param jobId The job id they are trying to access
     * @return True if they're allowed to access it, otherwise false
     */
    boolean canUserAccessJob(long uid, String jobId);

}
