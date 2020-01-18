Maven aggregator project that illustrates issues and solutions to current FHIR validation regarding reference check on narrowed types when using the validator through HAPI and its bridging classes. Project contains 3 child projects - all projects runs the same unit tests buth with different versions of the classes that does the validation:

# new
Runs on HAPI FHIR SNAPSHOT 4.2.0. This currently have 3 bugs according to the tests

# referenceCheckClassShadowing
Runs on HAPI FHIR 4.1.0 but contains a class shadowed version of InstanceValidator. Changes to this class are from line 1923 to 1944, line 1964 and lines 2018 to 2035. Changes means that validation runs 'as expected'.

# regular
Runs on HAPI FHIR 4.1.0. This currently have 3 bugs according to the tests

Run it using ```mvn clean --fail-at-end verify```