package de.gruendner.examplebatch.processor;

import de.gruendner.examplebatch.mapper.FhirResourceMapper;
import de.gruendner.examplebatch.util.FhirBundleBuilder;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class FhirBundleProcessor implements ItemProcessor<Bundle, Bundle> {

  FhirResourceMapper mapper;

  public FhirBundleProcessor(FhirResourceMapper mapper) {
    this.mapper = mapper;
  }


  @Override
    public Bundle process(final Bundle bundle) {

    List<Resource> resources = bundle.getEntry().stream().map((entry) -> entry.getResource()).toList();
    List<Resource> mappedResources = mapper.map(resources);

    FhirBundleBuilder fhirBundleBuilder = new FhirBundleBuilder();
    fhirBundleBuilder
        .id(UUID.randomUUID().toString())
        .add(mappedResources);

    return fhirBundleBuilder.build();

  }
}
