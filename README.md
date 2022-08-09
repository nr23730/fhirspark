# FhirSpark

FhirSpark is a tool that offers a reversible translation between the JSON sent via REST from [MIRACUM-cBioPortal](https://github.com/buschlab/MIRACUM-cbioportal) and FHIR resources. Using this method it is possible to store therapy recommendations and follow up data from Molecular Tumor Boards in an interoperable way.

It is reccommended to deploy and use FhirSaprk within the complete stack of [MIRACUM-cBioPortal](https://github.com/buschlab/MIRACUM-cbioportal).

## Get started

Most hospital information systems follow an order/entry based structure. Therefore (if present) the patient attribute `ORDER_ID` will be exported to FhirSpark and a FHIR `ServiceRequest` this will later on ensure that the therapy recommendation can be successfully linked with the originating order in the hospital information system.

To get started, copy the `settings.yaml.example` file to `settings.yaml`. This avoids future conflicts within the git tree. You need to tailor the configuration parameters to fit your environment first. The following configuration parameters are available:

| Parameter (yaml / Environment)                            | Description                                                                                                                                                                                                                                |
|-----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| port / FHIRSPARK_PORT                                     | Port for the FhirSpark integrated webserver                                                                                                                                                                                                |
| fhirDbBase / FHIRSPARK_FHIRBASE                           | Base of a FHIR server that should store the generated FHIR resources                                                                                                                                                                       |
| specimenSystem / FHIRSPARK_SPECIMENSYSTEM                 | System URI that is assigned to the identifier of Specimen resources                                                                                                                                                                        |
| diagnosticReportSystem / FHIRSPARK_DIAGNOSTICREPORTSYSTEM | System URI that is assigned to the identifier of DiagnosticReport resources                                                                                                                                                                |
| observationSystem / FHIRSPARK_OBSERVATIONSYSTEM           | System URI that is assigned to the identifier of Observation resources                                                                                                                                                                     |
| patientSystem / FHIRSPARK_PATIENTSYSTEM                   | System URI that is assigned to the identifier of Patient resources                                                                                                                                                                         |
| serviceRequestSystem / FHIRSPARK_SERVICEREQUESTSYSTEM     | System URI that is assigned to the identifier of ServiceRequest resources                                                                                                                                                                  |
| portalUrl / FHIRSPARK_PORTALURL                           | URL where FhirSpark can access cBioPortal                                                                                                                                                                                                  |
| loginRequired / FHIRSPARK_LOGINREQUIRED                   | Set true/false whether users should be logged in in order to view/save therapy recommendations                                                                                                                                             |
| hgncPath / FHIRSPARK_HGNCPATH                             | Path to the hgnc lookup database                                                                                                                                                                                                           |
| oncokbPath / FHIRSPARK_ONCOKBPATH                         | Path to the oncokb drug lookup database                                                                                                                                                                                                    |
| regex                                                     | Regular expressions to eliminate illegal characters from sample ids. Note: This conversion should be reversible!                                                                                                                           |
| regex.his / FHIRSPARK_REGEX_HIS                           | Character that needs to be eliminated                                                                                                                                                                                                   \| |
| regex.cbio / FHIRSPARK_REGEX_CBIO                         | Character that replaces the illegal character                                                                                                                                                                                              |

## Citation

This work was published and presented at the 31st [Medical Informatics Europe Conference (EFMI)](https://efmi.org/event/mie-2021-31st-medical-informatics-europe-conference-mie2021-athens-greece/).

[![DOI:10.3233/SHTI210169](https://img.shields.io/badge/DOI-10.3233%2FSHTI210169-brightgreen.svg)](http://dx.doi.org/10.3233/SHTI210169)

```
@incollection{Reimer_2021,
	doi = {10.3233/shti210169},
	url = {https://doi.org/10.3233%2Fshti210169},
	year = 2021,
	month = {may},
	publisher = {{IOS} Press},
	author = {Niklas Reimer and Philipp Unberath and Hauke Busch and Josef Ingenerf},
	title = {{FhirSpark} {\textendash} Implementing a Mediation Layer to Bring {FHIR} to the {cBioPortal} for Cancer Genomics},
	booktitle = {Studies in Health Technology and Informatics}
}
```

## License

GNU Affero General Public License v3 (AGPL-3.0)

## Acknowledgment

This work is funded by the German Federal Ministry of Education and Research (BMBF), grant ids 01ZZ1802Z (HiGHmed) and 01ZZ1801A (MIRACUM).
We acknowledge the support thorugh both HiGHmed and MIRACUM consortia as part of the Medical Informatics Initiative Germany.