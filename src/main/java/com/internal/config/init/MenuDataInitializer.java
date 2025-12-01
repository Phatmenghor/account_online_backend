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
            int rootOrder = 1;
            
            // Dashboard
            Menu dashboard = createMenu("Dashboard", "LayoutDashboard", "/dashboard", null, rootOrder++, 
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(dashboard);

            // Users (SUPER and DEVELOPER only)
            Menu users = createMenu("Users", "User2", "/user", null, rootOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.DEVELOPER)));
            menuRepository.save(users);

            // Account parent
            Menu accountParent = createMenu("Account", "IdCard", null, null, rootOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(accountParent);

            // Account children
            menuRepository.save(createMenu("Account Final", null, "/account-online", accountParent, 1,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Success Accounts", null, "/account-online-success", accountParent, 2,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));

            // AML parent
            Menu amlParent = createMenu("AML", "FolderClosed", null, null, rootOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(amlParent);

            // AML children
            menuRepository.save(createMenu("Management", null, "/aml/management", amlParent, 1,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("History", null, "/aml/history", amlParent, 2,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));

            // Master Data parent
            Menu masterDataParent = createMenu("Master Data", "Calendar1Icon", null, null, rootOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(masterDataParent);

            // Master Data children
            int masterOrder = 1;
            menuRepository.save(createMenu("Branch", null, "/branch", masterDataParent, masterOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Reference", null, "/reference", masterDataParent, masterOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Marital", null, "/marital", masterDataParent, masterOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Occupation", null, "/occupation", masterDataParent, masterOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Legal Type", null, "/legal-type", masterDataParent, masterOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));

            // Location parent (separate from Master Data)
            Menu locationParent = createMenu("Location", "MapPin", null, null, rootOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(locationParent);

            // Location children
            int locationOrder = 1;
            menuRepository.save(createMenu("Province", null, "/province", locationParent, locationOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("District", null, "/district", locationParent, locationOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Commune", null, "/commune", locationParent, locationOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));
            menuRepository.save(createMenu("Village", null, "/village", locationParent, locationOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER))));

            // Report
            Menu report = createMenu("Report", "File", "/report", null, rootOrder++,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            menuRepository.save(report);

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
