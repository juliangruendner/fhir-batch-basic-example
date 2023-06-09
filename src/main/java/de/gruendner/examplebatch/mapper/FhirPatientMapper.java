package de.gruendner.examplebatch.mapper;

import java.util.HashMap;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

public class FhirPatientMapper extends FhirMapper {


  public FhirPatientMapper(
      HashMap<String, String> icd10Snomed) {
    super(icd10Snomed);
  }

  public Resource map(Resource resource){

    Patient out = new Patient();
    Patient in = (Patient) resource;

    out.setGender(in.getGender());
    out.setBirthDate(in.getBirthDate());
    out.setId(in.getId());

    return out;
  }
}
