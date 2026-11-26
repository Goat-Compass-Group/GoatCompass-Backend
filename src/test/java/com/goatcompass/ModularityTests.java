package com.goatcompass;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Kiểm tra ranh giới module Spring Modulith: không module nào dùng phần nội bộ của module khác. */
class ModularityTests {

	@Test
	void verifiesModuleStructure() {
		ApplicationModules.of(GoatcompassApplication.class).verify();
	}

}
