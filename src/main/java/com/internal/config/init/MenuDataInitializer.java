package com.internal.config.init;

import com.internal.enumation.RoleEnum;
import com.internal.feature.setting.models.Menu;
import com.internal.feature.setting.repository.MenuRepository;
import com.internal.feature.setting.constant.MenuConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
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
        log.info("Initializing menu structure...");

        try {
            // Dashboard
            Menu dashboard = findOrCreateMenu(MenuConstant.Dashboard.TITLE, MenuConstant.Dashboard.ICON, MenuConstant.Dashboard.HREF, null, MenuConstant.Dashboard.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));

            // Users
            Menu users = findOrCreateMenu(MenuConstant.Users.TITLE, MenuConstant.Users.ICON, MenuConstant.Users.HREF, null, MenuConstant.Users.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.DEVELOPER)));

            // Account parent
            Menu accountParent = findOrCreateMenu(MenuConstant.Account.TITLE, MenuConstant.Account.ICON, MenuConstant.Account.HREF, null, MenuConstant.Account.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));

            // Account children
            findOrCreateMenu(MenuConstant.Account.Final.TITLE, MenuConstant.Account.Final.ICON, MenuConstant.Account.Final.HREF, accountParent, MenuConstant.Account.Final.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            findOrCreateMenu(MenuConstant.Account.Success.TITLE, MenuConstant.Account.Success.ICON, MenuConstant.Account.Success.HREF, accountParent, MenuConstant.Account.Success.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));

            // AML parent
            Menu amlParent = findOrCreateMenu(MenuConstant.Aml.TITLE, MenuConstant.Aml.ICON, MenuConstant.Aml.HREF, null, MenuConstant.Aml.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));

            // AML children
            findOrCreateMenu(MenuConstant.Aml.Management.TITLE, MenuConstant.Aml.Management.ICON, MenuConstant.Aml.Management.HREF, amlParent, MenuConstant.Aml.Management.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            findOrCreateMenu(MenuConstant.Aml.History.TITLE, MenuConstant.Aml.History.ICON, MenuConstant.Aml.History.HREF, amlParent, MenuConstant.Aml.History.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));

            // Master Data parent
            Menu masterDataParent = findOrCreateMenu(MenuConstant.MasterData.TITLE, MenuConstant.MasterData.ICON, MenuConstant.MasterData.HREF, null, MenuConstant.MasterData.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));

            // Master Data children
            findOrCreateMenu(MenuConstant.MasterData.Branch.TITLE, MenuConstant.MasterData.Branch.ICON, MenuConstant.MasterData.Branch.HREF, masterDataParent, MenuConstant.MasterData.Branch.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
            findOrCreateMenu(MenuConstant.MasterData.Reference.TITLE, MenuConstant.MasterData.Reference.ICON, MenuConstant.MasterData.Reference.HREF, masterDataParent, MenuConstant.MasterData.Reference.ORDER);
            findOrCreateMenu(MenuConstant.MasterData.Marital.TITLE, MenuConstant.MasterData.Marital.ICON, MenuConstant.MasterData.Marital.HREF, masterDataParent, MenuConstant.MasterData.Marital.ORDER);
            findOrCreateMenu(MenuConstant.MasterData.Occupation.TITLE, MenuConstant.MasterData.Occupation.ICON, MenuConstant.MasterData.Occupation.HREF, masterDataParent, MenuConstant.MasterData.Occupation.ORDER);
            findOrCreateMenu(MenuConstant.MasterData.LegalType.TITLE, MenuConstant.MasterData.LegalType.ICON, MenuConstant.MasterData.LegalType.HREF, masterDataParent, MenuConstant.MasterData.LegalType.ORDER);

            // Location parent
            Menu locationParent = findOrCreateMenu(MenuConstant.Location.TITLE, MenuConstant.Location.ICON, MenuConstant.Location.HREF, null, MenuConstant.Location.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));

            // Location children
            findOrCreateMenu(MenuConstant.Location.Province.TITLE, MenuConstant.Location.Province.ICON, MenuConstant.Location.Province.HREF, locationParent, MenuConstant.Location.Province.ORDER);
            findOrCreateMenu(MenuConstant.Location.District.TITLE, MenuConstant.Location.District.ICON, MenuConstant.Location.District.HREF, locationParent, MenuConstant.Location.District.ORDER);
            findOrCreateMenu(MenuConstant.Location.Commune.TITLE, MenuConstant.Location.Commune.ICON, MenuConstant.Location.Commune.HREF, locationParent, MenuConstant.Location.Commune.ORDER);
            findOrCreateMenu(MenuConstant.Location.Village.TITLE, MenuConstant.Location.Village.ICON, MenuConstant.Location.Village.HREF, locationParent, MenuConstant.Location.Village.ORDER);

            // Report
            findOrCreateMenu(MenuConstant.Report.TITLE, MenuConstant.Report.ICON, MenuConstant.Report.HREF, null, MenuConstant.Report.ORDER,
                    new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));

            // Menu Configuration
            findOrCreateMenu(MenuConstant.MenuConfig.TITLE, MenuConstant.MenuConfig.ICON, MenuConstant.MenuConfig.HREF, null, MenuConstant.MenuConfig.ORDER,
                    new HashSet<>(Collections.singletonList(RoleEnum.DEVELOPER)));

            log.info("Menu initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize menu data", e);
            throw new RuntimeException("Menu initialization failed", e);
        }
    }

    private Menu findOrCreateMenu(String title, String icon, String href, Menu parent, int displayOrder, Set<RoleEnum> roles) {
        Menu existing = parent == null ?
                menuRepository.findByTitleAndParentIsNull(title) :
                menuRepository.findByTitleAndParent(title, parent);

        if (existing != null) {
            log.info("Menu '{}' already exists{}", title, parent != null ? " under parent '" + parent.getTitle() + "'" : "");
            return existing;
        }

        Menu newMenu = Menu.builder()
                .title(title)
                .icon(icon)
                .href(href)
                .parent(parent)
                .displayOrder(displayOrder)
                .roles(new HashSet<>(roles))
                .isActive(true)
                .build();

        Menu saved = menuRepository.save(newMenu);
        log.info("Created new menu '{}'{}", title, parent != null ? " under parent '" + parent.getTitle() + "'" : "");
        return saved;
    }

    // Overload for default roles
    private void findOrCreateMenu(String title, String icon, String href, Menu parent, int displayOrder) {
        findOrCreateMenu(title, icon, href, parent, displayOrder,
                new HashSet<>(Arrays.asList(RoleEnum.SUPER, RoleEnum.COMPLIANCE, RoleEnum.DEVELOPER)));
    }

}
