package com.trifork.ehealth;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.dstu3.conformance.ProfileUtilities;
import org.hl7.fhir.dstu3.context.IWorkerContext;
import org.hl7.fhir.dstu3.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.PrePopulatedValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.Communication;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.r5.utils.IResourceValidator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;

public class ReferenceCheckExists {

    private final static FhirContext FHIR_CONTEXT = new FhirContext(FhirVersionEnum.DSTU3);
    private final DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport();


    static class ReferenceFactory
    {

        public static Reference createReference(Class<Patient> patientClass) {
            return new Reference(new Patient().setId("1"));
        }
    }

    @Test
    public void validateReferenceType_withNotSupportedReferencedType() {
        // Arrange
        Reference notSupportedReference = ReferenceFactory.createReference(Patient.class);

        Communication communication = new Communication();
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);
        communication.setDefinition(Collections.singletonList(notSupportedReference));

        FhirInstanceValidator fhirInstanceValidator = new FhirInstanceValidator();
        fhirInstanceValidator.setValidationSupport(defaultProfileValidationSupport);

        FhirValidator fhirValidator = FHIR_CONTEXT.newValidator();
        fhirValidator.registerValidatorModule(fhirInstanceValidator);

        // Act
        ValidationResult validationResult = fhirValidator.validateWithResult(communication);

        // Assert
        assertThat(validationResult.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void validateReferenceType_withNotSupportedContainedType() {
        // Arrange
        Patient patient = new Patient();
        patient.setActive(true);

        Communication communication = new Communication();
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);
        communication.setDefinition(Collections.singletonList(new Reference(patient)));

        FhirInstanceValidator fhirInstanceValidator = new FhirInstanceValidator();
        fhirInstanceValidator.setValidationSupport(defaultProfileValidationSupport);

        FhirValidator fhirValidator = FHIR_CONTEXT.newValidator();
        fhirValidator.registerValidatorModule(fhirInstanceValidator);

        // Act
        ValidationResult validationResult = fhirValidator.validateWithResult(communication);

        // Assert
        assertThat(validationResult.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void validateReferenceAggregationMode_Referenced_withContained() {
        // Arrange
        String differentialSubjectReferenced = "<StructureDefinition xmlns=\"http://hl7.org/fhir\">\n" +
                "<url value=\"http://example.org/fhir/StructureDefinition/MyCommunication\"/>\n" +
                "<name value=\"MyCommunication\"/>\n" +
                "<status value=\"draft\"/>\n" +
                "<fhirVersion value=\"3.0.1\"/>\n" +
                "<kind value=\"resource\"/>\n" +
                "<abstract value=\"false\"/>\n" +
                "<type value=\"Communication\"/>\n" +
                "<baseDefinition value=\"http://hl7.org/fhir/StructureDefinition/Communication\"/>\n" +
                "<derivation value=\"constraint\"/>\n" +
                " <differential>\n" +
                " <element id=\"Communication.subject\">\n" +
                "<path value=\"Communication.subject\"/>\n" +
                " <type>\n" +
                "<code value=\"Reference\"/>\n" +
                "<targetProfile value=\"http://hl7.org/fhir/StructureDefinition/Patient\"/>\n" +
                "<aggregation value=\"referenced\"/>\n" +
                "</type>\n" +
                " <type>\n" +
                "<code value=\"Reference\"/>\n" +
                "<targetProfile value=\"http://hl7.org/fhir/StructureDefinition/Group\"/>\n" +
                "</type>\n" +
                "</element>\n" +
                "</differential>\n" +
                "</StructureDefinition>";

        Patient containedPatient = new Patient();
        containedPatient.setActive(true);

        Communication communication = new Communication();
        communication.getMeta().addProfile("http://example.org/fhir/StructureDefinition/MyCommunication");
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);
        communication.setSubject(new Reference(containedPatient));

        FhirValidator fhirValidator = createFhirValidator(differentialSubjectReferenced);

        // Act
        ValidationResult validationResult = fhirValidator.validateWithResult(communication);

        // Assert
        assertThat(validationResult.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void validateReferenceAggregationMode_Contained_withReference() {
        // Arrange
        String differentialSubjectReferenced = "<StructureDefinition xmlns=\"http://hl7.org/fhir\">\n" +
                "<url value=\"http://example.org/fhir/StructureDefinition/MyCommunication\"/>\n" +
                "<name value=\"MyCommunication\"/>\n" +
                "<status value=\"draft\"/>\n" +
                "<fhirVersion value=\"3.0.1\"/>\n" +
                "<kind value=\"resource\"/>\n" +
                "<abstract value=\"false\"/>\n" +
                "<type value=\"Communication\"/>\n" +
                "<baseDefinition value=\"http://hl7.org/fhir/StructureDefinition/Communication\"/>\n" +
                "<derivation value=\"constraint\"/>\n" +
                " <differential>\n" +
                " <element id=\"Communication.subject\">\n" +
                "<path value=\"Communication.subject\"/>\n" +
                " <type>\n" +
                "<code value=\"Reference\"/>\n" +
                "<targetProfile value=\"http://hl7.org/fhir/StructureDefinition/Patient\"/>\n" +
                "<aggregation value=\"contained\"/>\n" +
                "</type>\n" +
                " <type>\n" +
                "<code value=\"Reference\"/>\n" +
                "<targetProfile value=\"http://hl7.org/fhir/StructureDefinition/Group\"/>\n" +
                "</type>\n" +
                "</element>\n" +
                "</differential>\n" +
                "</StructureDefinition>";

        Communication communication = new Communication();
        communication.getMeta().addProfile("http://example.org/fhir/StructureDefinition/MyCommunication");
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);
        communication.setSubject(ReferenceFactory.createReference(Patient.class));

        FhirValidator fhirValidator = createFhirValidator(differentialSubjectReferenced);

        // Act
        ValidationResult validationResult = fhirValidator.validateWithResult(communication);

        // Assert
        assertThat(validationResult.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void validateReferenceAggregationMode_ContainedOrReferenced_withReferenced() {
        // Arrange
        String differentialSubjectReferenced = "<StructureDefinition xmlns=\"http://hl7.org/fhir\">\n" +
                "<url value=\"http://example.org/fhir/StructureDefinition/MyCommunication\"/>\n" +
                "<name value=\"MyCommunication\"/>\n" +
                "<status value=\"draft\"/>\n" +
                "<fhirVersion value=\"3.0.1\"/>\n" +
                "<kind value=\"resource\"/>\n" +
                "<abstract value=\"false\"/>\n" +
                "<type value=\"Communication\"/>\n" +
                "<baseDefinition value=\"http://hl7.org/fhir/StructureDefinition/Communication\"/>\n" +
                "<derivation value=\"constraint\"/>\n" +
                " <differential>\n" +
                " <element id=\"Communication.subject\">\n" +
                "<path value=\"Communication.subject\"/>\n" +
                " <type>\n" +
                "<code value=\"Reference\"/>\n" +
                "<targetProfile value=\"http://hl7.org/fhir/StructureDefinition/Patient\"/>\n" +
                "<aggregation value=\"contained\"/>\n" +
                "<aggregation value=\"referenced\"/>\n" +
                "</type>\n" +
                " <type>\n" +
                "<code value=\"Reference\"/>\n" +
                "<targetProfile value=\"http://hl7.org/fhir/StructureDefinition/Group\"/>\n" +
                "</type>\n" +
                "</element>\n" +
                "</differential>\n" +
                "</StructureDefinition>";

        Communication communication = new Communication();
        communication.getMeta().addProfile("http://example.org/fhir/StructureDefinition/MyCommunication");
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);
        communication.setSubject(ReferenceFactory.createReference(Patient.class));

        FhirValidator fhirValidator = createFhirValidator(differentialSubjectReferenced);

        // Act
        ValidationResult validationResult = fhirValidator.validateWithResult(communication);

        // Assert
        assertThat(validationResult.getMessages().isEmpty()).isTrue();
    }

    @Test
    public void validateReferenceAggregationMode_ContainedOrReferenced_withContained() {
        // Arrange
        String differentialSubjectReferenced = "<StructureDefinition xmlns=\"http://hl7.org/fhir\">\n" +
                "<url value=\"http://example.org/fhir/StructureDefinition/MyCommunication\"/>\n" +
                "<name value=\"MyCommunication\"/>\n" +
                "<status value=\"draft\"/>\n" +
                "<fhirVersion value=\"3.0.1\"/>\n" +
                "<kind value=\"resource\"/>\n" +
                "<abstract value=\"false\"/>\n" +
                "<type value=\"Communication\"/>\n" +
                "<baseDefinition value=\"http://hl7.org/fhir/StructureDefinition/Communication\"/>\n" +
                "<derivation value=\"constraint\"/>\n" +
                " <differential>\n" +
                " <element id=\"Communication.subject\">\n" +
                "<path value=\"Communication.subject\"/>\n" +
                " <type>\n" +
                "<code value=\"Reference\"/>\n" +
                "<targetProfile value=\"http://hl7.org/fhir/StructureDefinition/Patient\"/>\n" +
                "<aggregation value=\"contained\"/>\n" +
                "<aggregation value=\"referenced\"/>\n" +
                "</type>\n" +
                " <type>\n" +
                "<code value=\"Reference\"/>\n" +
                "<targetProfile value=\"http://hl7.org/fhir/StructureDefinition/Group\"/>\n" +
                "</type>\n" +
                "</element>\n" +
                "</differential>\n" +
                "</StructureDefinition>";

        Patient containedPatient = new Patient();
        containedPatient.setActive(true);

        Communication communication = new Communication();
        communication.getMeta().addProfile("http://example.org/fhir/StructureDefinition/MyCommunication");
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);
        communication.setSubject(new Reference(containedPatient));

        FhirValidator fhirValidator = createFhirValidator(differentialSubjectReferenced);

        // Act
        ValidationResult validationResult = fhirValidator.validateWithResult(communication);

        // Assert
        assertThat(validationResult.getMessages().isEmpty()).isTrue();
    }

    private FhirValidator createFhirValidator(String differentialActivityDefinitionReferenced) {
        PrePopulatedValidationSupport customValidationSupport = new PrePopulatedValidationSupport();
        customValidationSupport.addStructureDefinition(createSnapshot(FHIR_CONTEXT, differentialActivityDefinitionReferenced));

        FhirInstanceValidator fhirInstanceValidator = new FhirInstanceValidator();
        fhirInstanceValidator.setNoTerminologyChecks(false);
        fhirInstanceValidator.setAnyExtensionsAllowed(true);
        fhirInstanceValidator.setErrorForUnknownProfiles(true);
        fhirInstanceValidator.setBestPracticeWarningLevel(IResourceValidator.BestPracticeWarningLevel.Error);
        fhirInstanceValidator.setValidationSupport(new ValidationSupportChain(defaultProfileValidationSupport, customValidationSupport));

        FhirValidator fhirValidator = FHIR_CONTEXT.newValidator();
        fhirValidator.registerValidatorModule(fhirInstanceValidator);
        return fhirValidator;
    }

    private StructureDefinition createSnapshot(FhirContext fhirContext, String differential) {
        StructureDefinition structureDefinition = fhirContext.newXmlParser().parseResource(StructureDefinition.class, differential);
        StructureDefinition baseStructureDefinition = defaultProfileValidationSupport.fetchStructureDefinition(fhirContext, structureDefinition.getBaseDefinition());

        IWorkerContext workerContext = new HapiWorkerContext(fhirContext, defaultProfileValidationSupport);
        ProfileUtilities profileUtilities = new ProfileUtilities(workerContext, new ArrayList<>(), null);
        profileUtilities.generateSnapshot(baseStructureDefinition, structureDefinition, "", "");

        return structureDefinition;
    }
}
