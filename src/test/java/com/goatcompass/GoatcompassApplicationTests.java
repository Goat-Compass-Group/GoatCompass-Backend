package com.goatcompass;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Khởi động toàn bộ ứng dụng trên Postgres + pgvector thật (Testcontainers):
 * Liquibase chạy hết changelog, sau đó Hibernate (ddl-auto=validate) kiểm tra entity khớp với schema.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class GoatcompassApplicationTests {

	@Test
	void contextLoadsWithMigratedSchema() {
	}

}
