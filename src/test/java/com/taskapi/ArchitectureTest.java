package com.taskapi;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Garante, de forma automática, que ninguém quebre as regras da arquitetura hexagonal. */
@AnalyzeClasses(packages = "com.taskapi", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainDoesNotDependOnFrameworksOrOuterLayers =
            noClasses().that().resideInAPackage("com.taskapi.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..", "jakarta..",
                            "com.taskapi.application..", "com.taskapi.adapter..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnFrameworksOrAdapters =
            noClasses().that().resideInAPackage("com.taskapi.application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..", "jakarta..", "com.taskapi.adapter..");

    @ArchTest
    static final ArchRule inboundAdaptersDoNotDependOnOutboundAdapters =
            noClasses().that().resideInAPackage("com.taskapi.adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("com.taskapi.adapter.out..");

    @ArchTest
    static final ArchRule outboundAdaptersDoNotDependOnInboundAdapters =
            noClasses().that().resideInAPackage("com.taskapi.adapter.out..")
                    .should().dependOnClassesThat().resideInAPackage("com.taskapi.adapter.in..");
}
