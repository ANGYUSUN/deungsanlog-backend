package com.example.test1.batch.config;

import com.example.test1.batch.dto.MountainMeta;
import com.example.test1.batch.processor.MountainProcessor;
import com.example.test1.domain.Mountain;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchJobConfig {

    private final MountainProcessor processor;

    @Bean
    public FlatFileItemReader<MountainMeta> mountainMetaReader() {
        System.out.println("📥 Reader Bean Created");

        BeanWrapperFieldSetMapper<MountainMeta> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(MountainMeta.class);

        return new FlatFileItemReaderBuilder<MountainMeta>()
                .name("mountainMetaReader")
                .resource(new ClassPathResource("mountain_metadata.csv"))
                .delimited()
                .names("name", "height")
                .linesToSkip(1) // name, height 건너뛰기, 첫행 띄기
                .fieldSetMapper(mapper)
                .build();
    }

    @Bean
    public JpaItemWriter<Mountain> mountainWriter(EntityManagerFactory entityManagerFactory) throws Exception {
        JpaItemWriter<Mountain> writer = new JpaItemWriter<>();
        writer.setUsePersist(true);
        writer.setEntityManagerFactory(entityManagerFactory);
        writer.afterPropertiesSet();
        return writer;
    }

    @Bean
    public Step mountainStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             FlatFileItemReader<MountainMeta> reader,
                             JpaItemWriter<Mountain> writer) {

        return new StepBuilder("mountainStep", jobRepository)
                .<MountainMeta, Mountain>chunk(10, transactionManager)
                .reader(reader)
                .processor(item -> {
                    System.out.println("📦 Processor reached for: " + item.getName());
                    Mountain result = processor.process(item);
                    if (result == null) {
                        System.out.println("❌ Processor returned null for: " + item.getName());
                    } else {
                        System.out.println("✅ Processor produced result for: " + result.getName());
                    }
                    return result;
                })
                .writer(items -> {
                    System.out.println("💾 Writing " + items.size() + " items to DB");
                    writer.write(items);
                    writer.afterPropertiesSet(); // flush 강제
                })

                .build();
    }


    @Bean
    public Job mountainJob(JobRepository jobRepository, Step mountainStep) {
        return new JobBuilder("mountainJob", jobRepository)
                .start(mountainStep)
                .build();
    }
}
