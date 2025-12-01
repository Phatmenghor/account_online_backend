package com.internal.config.init;

import com.internal.enumation.RoleEnum;
import com.internal.feature.setting.models.Menu;
import com.internal.feature.setting.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(100) // Run after DefaultUserInitializer
public class MenuDataInitializer implements CommandLineRunner {

    private final MenuRepository menuRepository;

    @Override
    public void run(String... args) {
        if (menuRepository.count() > 0) {
            log.info("Menu data already exists, skipping initialization");
            return;
        }

        log.info("Initializing default menu structure...");

        try {
            // Create root menus
            Menu dashboard = createMenu("Dashboard", "Home", "/dashboard", null, 1, 
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(dashboard);

            // AML parent
            Menu amlParent = createMenu("AML", "Shield", null, null, 2,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(amlParent);

            // AML children
            Menu amlHistory = createMenu("AML History", null, "/dashboard/aml/history", amlParent, 1,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(amlHistory);

            Menu amlManagement = createMenu("AML Management", null, "/dashboard/aml/management", amlParent, 2,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(amlManagement);

            // Master Data parent
            Menu masterDataParent = createMenu("Master Data", "Database", null, null, 3,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(masterDataParent);

            // Master Data children
            int order = 1;
            menuRepository.save(createMenu("Marital Status", null, "/dashboard/static/marital", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Occupation", null, "/dashboard/static/occupation", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Reference", null, "/dashboard/static/reference", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Branch", null, "/dashboard/static/branch", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Legal Type", null, "/dashboard/static/legal-type", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Province", null, "/dashboard/static/province", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("District", null, "/dashboard/static/district", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Commune", null, "/dashboard/static/commune", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Village", null, "/dashboard/static/village", masterDataParent, order++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));

            // User Management (SUPER only)
            Menu userManagement = createMenu("User Management", "Users", "/dashboard/user", null, 4,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.DEVELOPER)));
            menuRepository.save(userManagement);

            log.info("Default menu structure initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize menu data", e);
            throw new RuntimeException("Menu initialization failed", e);
        }
    }

    private Menu createMenu(String title, String icon, String href, Menu parent, int displayOrder, Set<RoleEnum> roles) {
        return Menu.builder()
                .title(title)
                .icon(icon)
                .href(href)
                .parent(parent)
                .displayOrder(displayOrder)
                .roles(new HashSet<>(roles))
                .isActive(true)
                .build();
    }
}
