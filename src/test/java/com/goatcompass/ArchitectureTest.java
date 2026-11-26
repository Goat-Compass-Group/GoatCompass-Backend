package com.goatcompass;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Khóa quy tắc phụ thuộc của hexagonal architecture trong MỌI module: phụ thuộc chỉ được hướng vào trong
 * (infrastructure → application → domain). Ai thêm import sai tầng thì build fail ngay,
 * thay vì kiến trúc mục dần theo thời gian. Ranh giới GIỮA các module do ModularityTests (Spring Modulith) kiểm tra.
 */
@AnalyzeClasses(packages = "com.goatcompass", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /**
     * Domain là lõi nghiệp vụ thuần: không biết đến use case, framework hay cách lưu trữ.
     * Trừ package-info: chỉ mang metadata ranh giới module của Spring Modulith (@NamedInterface).
     */
    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses().that().resideInAPackage("com.goatcompass..domain..")
            .and().doNotHaveSimpleName("package-info")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.goatcompass..application..", "com.goatcompass..infrastructure..",
                    "org.springframework..", "jakarta.persistence..", "org.hibernate..", "com.fasterxml..")
            .allowEmptyShould(true);

    /** Application chỉ nói chuyện với bên ngoài qua port (interface); adapter nằm ở infrastructure. */
    @ArchTest
    static final ArchRule applicationDependsOnPortsOnly = noClasses().that().resideInAPackage("com.goatcompass..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.goatcompass..infrastructure..",
                    "jakarta.persistence..", "org.springframework.web..", "org.springframework.data.jpa..")
            .allowEmptyShould(true);

    /** Controller gọi use case, không chạm thẳng vào repository JPA. */
    @ArchTest
    static final ArchRule webGoesThroughApplication = noClasses().that().resideInAPackage("com.goatcompass..infrastructure.web..")
            .should().dependOnClassesThat().resideInAnyPackage("com.goatcompass..infrastructure.persistence..")
            .allowEmptyShould(true);

    /** Adapter lưu trữ không được phụ thuộc ngược lên tầng giao tiếp HTTP. */
    @ArchTest
    static final ArchRule persistenceDoesNotKnowWeb = noClasses().that().resideInAPackage("com.goatcompass..infrastructure.persistence..")
            .should().dependOnClassesThat().resideInAPackage("com.goatcompass..infrastructure.web..")
            .allowEmptyShould(true);
}
