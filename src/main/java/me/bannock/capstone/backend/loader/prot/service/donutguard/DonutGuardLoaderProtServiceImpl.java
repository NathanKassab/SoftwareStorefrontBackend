package me.bannock.capstone.backend.loader.prot.service.donutguard;

import com.google.inject.Guice;
import com.google.inject.Injector;
import me.bannock.capstone.backend.loader.prot.service.LoaderProtJobDto;
import me.bannock.capstone.backend.loader.prot.service.LoaderProtService;
import me.bannock.capstone.backend.loader.prot.service.LoaderProtServiceException;
import me.bannock.capstone.backend.loader.prot.service.donutguard.plugin.WatermarkerConfigGroup;
import me.bannock.capstone.backend.loader.prot.service.donutguard.plugin.WatermarkerModule;
import me.bannock.donutguard.obf.Obfuscator;
import me.bannock.donutguard.obf.ObfuscatorModule;
import me.bannock.donutguard.obf.config.Configuration;
import me.bannock.donutguard.obf.config.DefaultConfigGroup;
import me.bannock.donutguard.obf.job.JobStatus;
import me.bannock.donutguard.obf.job.ObfuscatorJob;
import me.bannock.donutguard.obf.job.ObfuscatorJobFactory;
import me.bannock.donutguard.utils.ConfigurationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class DonutGuardLoaderProtServiceImpl implements LoaderProtService {

    public DonutGuardLoaderProtServiceImpl(){
        this.obfuscatorInjector = Guice.createInjector(new ObfuscatorModule(), new WatermarkerModule());
        this.obfuscator = obfuscatorInjector.getInstance(Obfuscator.class);
        this.jobFactory = obfuscatorInjector.getInstance(ObfuscatorJobFactory.class);
        this.jobs = new HashMap<>();

        // Due to a bug, a configuration must be created using the injector to ensure
        // the groups are loaded correctly
        obfuscatorInjector.getInstance(Configuration.class);
    }

    private final Logger logger = LogManager.getLogger();

    @Value("${backend.loader.defaultUnprotectedLoaderPath}")
    private String defaultUnprotectedLoaderPath;

    @Value("${backend.loader.protService.outputDir}")
    private String outputDir;

    @Value("${backend.loader.protService.configFile}")
    private String configFilePath;

    @Value("${backend.loader.protService.timeoutMillis}")
    private long timeoutMillis;

    private final Injector obfuscatorInjector;
    private final Obfuscator obfuscator;
    private final ObfuscatorJobFactory jobFactory;
    private final Map<String, ObfuscatorJob> jobs;
    private final Timer timer = new Timer();

    @Override
    public LoaderProtJobDto startLoaderCreationJob(String apiKey, long uid) throws LoaderProtServiceException {
        Objects.requireNonNull(apiKey);

        File configFile = new File(configFilePath);
        if (!configFile.exists()){
            logger.error("Config file does not exist, cfgPath={}", configFilePath);
            try { // In this case we generate a fresh configuration
                Configuration newConfig = obfuscatorInjector.getInstance(Configuration.class);
                DefaultConfigGroup.INPUT.setFile(newConfig, new File(defaultUnprotectedLoaderPath));
                configFile.getParentFile().mkdirs(); // To ensure the parent dir exists
                Files.write(configFile.toPath(), ConfigurationUtils.getConfigBytes(newConfig));
            } catch (IOException e) {
                logger.warn("Unable to write default DonutGuard configuration", e);
                throw new LoaderProtServiceException("Config file does not exist", null);
            }
        }

        // What are the chances that I made a decent obfuscator with integrated
        // 3rd party plugin support the very project before this?
        Configuration config;
        try {
            config = ConfigurationUtils.loadConfig(configFile);
        } catch (IOException e) {
            logger.warn("Was unable to load protection config");
            throw new LoaderProtServiceException(e.getMessage(), null);
        }

        // In order to pass the api key and uid values into the watermark mutator,
        // we set these specific values in the config
        WatermarkerConfigGroup.API_KEY.setString(config, apiKey);
        WatermarkerConfigGroup.UID.setString(config, uid + "");

        String jobId = "job-%s".formatted(System.currentTimeMillis());
        if (this.jobs.containsKey(jobId))
            throw new LoaderProtServiceException("Key already exists in system", jobId);

        File outputFile = getOutputFile(jobId);
        outputFile.getParentFile().mkdirs(); // To ensure the parent dir exists
        outputFile.deleteOnExit();
        DefaultConfigGroup.OUTPUT.setFile(config, outputFile);
        ObfuscatorJob job = jobFactory.create(config, new WatermarkerModule());
        obfuscator.submitJob(job);
        this.jobs.put(jobId, job);

        // We need to schedule a timeout for later that will remove the job as well as its output file.
        // The reason for this is that this assists with cleanup and also ensures threads on the
        // obfuscator don't turn into a vegetable on the off chance that they were to hang or get stuck.
        TimerTask timeoutTask = new TimerTask() {
            @Override
            public void run() {
                obfuscator.removeJob(job);
                if (outputFile.exists())
                    outputFile.delete();
            }
        };
        timer.schedule(timeoutTask, timeoutMillis);

        return new LoaderProtJobDto(jobId, uid, obfuscator.getJobStatus(job).getFriendlyName(), jobId);
    }

    @Override
    public Optional<String> getJobStatus(String id) {
        Objects.requireNonNull(id);
        if (!this.jobs.containsKey(id))
            return Optional.empty();

        ObfuscatorJob job = jobs.get(id);
        return Optional.of(obfuscator.getJobStatus(job).getFriendlyName());
    }

    @Override
    public Optional<File> getFinishedJobOutput(String id) {
        Objects.requireNonNull(id);
        if (!this.jobs.containsKey(id))
            throw new RuntimeException("Could not find id %s on job mapping".formatted(id));

        ObfuscatorJob job = jobs.get(id);
        if (obfuscator.getJobStatus(job).equals(JobStatus.COMPLETED)){
            File output = getOutputFile(id);
            return Optional.of(output);
        }
        return Optional.empty();
    }

    /**
     * Creates the output file based on the public job id
     * @param jobId The publicly facing job id
     * @return The output file
     */
    private File getOutputFile(String jobId){
        return new File("%s/%s.jar".formatted(outputDir, jobId));
    }

}
