package org.mskcc.cbio.biogene;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.transaction.annotation.Transactional;

public class BioGeneAdmin
{
    private static final Log log = LogFactory.getLog(BioGeneAdmin.class);
    private static final String helpText = BioGeneAdmin.class.getSimpleName();

    private static final ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
        "classpath*:META-INF/spring/adminApplicationContext.xml",
        "classpath*:META-INF/spring/geneDataApplicationContext.xml"
    );

    @Transactional
    public static void main(String[] args) {

        final CommandLineParser parser = new GnuParser();
        Options gnuOptions = new Options();
        gnuOptions
                .addOption("h", "help", false, "shows this help document and quits.")
			    .addOption("g", "gene-data", false, "imports gene data.")
        ;

        // Here goes the parsing attempt
        try {
            CommandLine commandLine = parser.parse(gnuOptions, args);

            if (commandLine.getOptions().length == 0) {
                // Here goes help message about running admin
                throw new ParseException("Nothing to do!");
            }

            if (commandLine.hasOption("h")) {
                printHelpAndExit(gnuOptions, 0);
            }

			if (commandLine.hasOption("g")) {
                launchJob("geneDataImporterJob");
			}

            log.info("All done.");
        }
		catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelpAndExit(gnuOptions, -1);
        }
    }

    private static void printHelpAndExit(Options gnuOptions, int exitStatus)
	{
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(helpText, gnuOptions);
        System.exit(exitStatus);
    }

    private static void launchJob(String jobName)
	{
        log.info("launchJob: jobName:" + jobName);
        try {
            Job job = (Job)appContext.getBean(jobName);
            JobLauncher jobLauncher = (JobLauncher)appContext.getBean("jobLauncher");
            JobParametersBuilder builder = new JobParametersBuilder();
            JobExecution jobExecution = jobLauncher.run(job, builder.toJobParameters());
            log.info("launchJob: exit code: " + jobExecution.getExitStatus().getExitCode());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
