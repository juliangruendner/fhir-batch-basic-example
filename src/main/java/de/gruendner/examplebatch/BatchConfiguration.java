package de.gruendner.examplebatch;

import ca.uhn.fhir.context.FhirContext;
import de.gruendner.examplebatch.mapper.FhirResourceMapper;
import de.gruendner.examplebatch.mapper.FhirConditionMapper;
import de.gruendner.examplebatch.mapper.FhirPatientMapper;
import de.gruendner.examplebatch.mapper.FhirSpecimenMapper;
import de.gruendner.examplebatch.processor.FhirBundleProcessor;
import de.gruendner.examplebatch.reader.FhirBundleReader;
import de.gruendner.examplebatch.writer.FhirBundleWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ConceptMap;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class BatchConfiguration {

  @Autowired
  private FhirProperties fhirProperties;

  private FhirContext ctx = FhirContext.forR4();

  @Bean
  public HashMap<String,String> icd10Snomed() throws FileNotFoundException {
    HashMap<String,String> icd10Snomed = new HashMap<>();
    ConceptMap cp = (ConceptMap) ctx.newJsonParser().parseResource(new FileInputStream("example-concept.json"));
    cp.getGroup().get(0).getElement().forEach(
        (elem) -> icd10Snomed.put(elem.getCode(), elem.getTarget().get(0).getCode())
    );

    return icd10Snomed;
  }

  @Bean
  public FhirResourceMapper fhirMapper(HashMap<String,String> icd10Snomed) {
    return new FhirResourceMapper(new FhirPatientMapper(icd10Snomed), new FhirConditionMapper(icd10Snomed),
        new FhirSpecimenMapper(icd10Snomed));
  }

  @Bean
  public ItemReader<Bundle> reader() {
    return new FhirBundleReader(ctx.newRestfulGenericClient(fhirProperties.getInput().getUrl()));
  }

  @Bean
  public FhirBundleProcessor processor(FhirResourceMapper fhirMapper) {
    return new FhirBundleProcessor(fhirMapper);
  }

  @Bean
  @StepScope
  public ItemWriter<Bundle> writer() {
    return new FhirBundleWriter(ctx.newRestfulGenericClient(fhirProperties.getOutput().getUrl()));
  }

  @Bean
  public Job importUserJob(JobRepository jobRepository, Step step1) {
    return new JobBuilder("importUserJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .flow(step1)
        .end()
        .build();
  }

  @Bean
  public Step step1(JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      FhirBundleProcessor processor) {
    return new StepBuilder("step1", jobRepository)
        .<Bundle, Bundle> chunk(1, transactionManager)
        .reader(reader())
        .processor(processor)
        .writer(writer())
        .build();
  }

}