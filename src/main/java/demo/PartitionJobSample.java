package demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PartitionJobSample {

  private final PlatformTransactionManager transactionManager;
  private final JobRepository jobRepository;

  public PartitionJobSample(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
    this.transactionManager = transactionManager;
    this.jobRepository = jobRepository;
  }

  @Bean
  public Step managerStep() {
    return new StepBuilder("managerStep", jobRepository)
        .partitioner(workerStep().getName(), new FixedRangePartitioner())
//        .partitioner(workerStep().getName(), new RangePartitioner())
        .step(workerStep())
        .gridSize(3)
        .taskExecutor(new SimpleAsyncTaskExecutor())
        .build();
  }

  @Bean
  public Step workerStep() {
    return new StepBuilder("workerStep", jobRepository)
        .<String, String>chunk(100, transactionManager)
        .reader(itemReader(null, null))
        .writer(itemWriter())
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<String> itemReader(
      @Value("#{stepExecutionContext['linesToSkip']}") Integer linesToSkip,
      @Value("#{stepExecutionContext['maxItemCount']}") Integer maxItemCount
  ) {
    return new FlatFileItemReaderBuilder<String>()
        .resource(new FileSystemResource("src/main/resources/data.txt"))
        .saveState(false)
        .lineMapper(new PassThroughLineMapper())
        .linesToSkip(linesToSkip)
        .maxItemCount(maxItemCount)
        .build();
  }

  @Bean
  public ItemWriter<String> itemWriter() {
    return items -> {
      var message = Thread.currentThread().getName() + " writing items = " + String.join(", ", items);
      System.out.println(message);
    };
  }

  @Bean
  public Job job() {
    return new JobBuilder("job", jobRepository)
        .start(managerStep())
        .build();
  }

}
