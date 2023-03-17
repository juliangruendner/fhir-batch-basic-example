package de.gruendner.examplebatch.mapper;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
public class FhirResourceMapper {

  private final FhirSpecimenMapper specimenMapper;
  private FhirPatientMapper patientMapper;
  private FhirConditionMapper conditionMapper;

  public FhirResourceMapper(FhirPatientMapper patientMapper, FhirConditionMapper conditionMapper, FhirSpecimenMapper specimenMapper){
    this.patientMapper = patientMapper;
    this.conditionMapper = conditionMapper;
    this.specimenMapper = specimenMapper;
  }

  public List<Resource> map(List<Resource> resources){

    return resources.stream().map( (resource) -> {
      ResourceType resType = resource.getResourceType();
      Resource res = null;
      switch (resType) {
        case Patient: res = patientMapper.map(resource);
          break;
        case Condition: res = conditionMapper.map(resource);
          break;
        case Specimen: res = specimenMapper.map(resource);
          break;
      }

      if (res != null){
      }

      return  res;
        }
    ).filter((res1) -> res1 != null).collect(Collectors.toList());

  }

}
