package com.internal.config.init;

import com.internal.enumation.RoleEnum;
import com.internal.feature.setting.models.Menu;
import com.internal.feature.setting.repository.MenuRepository;
import com.internal.utils.constants.MenuConstant;
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
@Order(100)
public class DefaultMenuInitializer implements CommandLineRunner {

    private final MenuRepository menuRepository;

    /**
     * All roles that should have access to every menu.
     * Add or remove roles here to control global default access.
     */
    private static final Set<RoleEnum> ALL_ROLES = new HashSet<>(Arrays.asList(
            RoleEnum.BUSINESS,
            RoleEnum.COMPLIANCE,
            RoleEnum.DEVELOPER,
            RoleEnum.ADMIN
    ));

    @Override
    public void run(String... args) {
        log.info("Initializing menu structure...");

        try {
            // ── Dashboard ──────────────────────────────────────────────────────
            findOrCreateMenu(MenuConstant.Dashboard.TITLE, MenuConstant.Dashboard.ICON,
                    MenuConstant.Dashboard.HREF, null, MenuConstant.Dashboard.ORDER);

            // ── Users ──────────────────────────────────────────────────────────
            findOrCreateMenu(MenuConstant.Users.TITLE, MenuConstant.Users.ICON,
                    MenuConstant.Users.HREF, null, MenuConstant.Users.ORDER);

            // ── Account (parent + children) ────────────────────────────────────
            Menu accountParent = findOrCreateMenu(MenuConstant.Account.TITLE, MenuConstant.Account.ICON,
                    MenuConstant.Account.HREF, null, MenuConstant.Account.ORDER);
            findOrCreateMenu(MenuConstant.Account.Final.TITLE, MenuConstant.Account.Final.ICON,
                    MenuConstant.Account.Final.HREF, accountParent, MenuConstant.Account.Final.ORDER);
            findOrCreateMenu(MenuConstant.Account.Success.TITLE, MenuConstant.Account.Success.ICON,
                    MenuConstant.Account.Success.HREF, accountParent, MenuConstant.Account.Success.ORDER);

            // ── AML (parent + children) ────────────────────────────────────────
            Menu amlParent = findOrCreateMenu(MenuConstant.Aml.TITLE, MenuConstant.Aml.ICON,
                    MenuConstant.Aml.HREF, null, MenuConstant.Aml.ORDER);
            findOrCreateMenu(MenuConstant.Aml.Management.TITLE, MenuConstant.Aml.Management.ICON,
                    MenuConstant.Aml.Management.HREF, amlParent, MenuConstant.Aml.Management.ORDER);
            findOrCreateMenu(MenuConstant.Aml.History.TITLE, MenuConstant.Aml.History.ICON,
                    MenuConstant.Aml.History.HREF, amlParent, MenuConstant.Aml.History.ORDER);

            // ── Master Data (parent + children) ───────────────────────────────
            Menu masterDataParent = findOrCreateMenu(MenuConstant.MasterData.TITLE, MenuConstant.MasterData.ICON,
                    MenuConstant.MasterData.HREF, null, MenuConstant.MasterData.ORDER);
            findOrCreateMenu(MenuConstant.MasterData.Branch.TITLE, MenuConstant.MasterData.Branch.ICON,
                    MenuConstant.MasterData.Branch.HREF, masterDataParent, MenuConstant.MasterData.Branch.ORDER);
            findOrCreateMenu(MenuConstant.MasterData.Reference.TITLE, MenuConstant.MasterData.Reference.ICON,
                    MenuConstant.MasterData.Reference.HREF, masterDataParent, MenuConstant.MasterData.Reference.ORDER);
            findOrCreateMenu(MenuConstant.MasterData.Marital.TITLE, MenuConstant.MasterData.Marital.ICON,
                    MenuConstant.MasterData.Marital.HREF, masterDataParent, MenuConstant.MasterData.Marital.ORDER);
            findOrCreateMenu(MenuConstant.MasterData.Occupation.TITLE, MenuConstant.MasterData.Occupation.ICON,
                    MenuConstant.MasterData.Occupation.HREF, masterDataParent, MenuConstant.MasterData.Occupation.ORDER);
            findOrCreateMenu(MenuConstant.MasterData.LegalType.TITLE, MenuConstant.MasterData.LegalType.ICON,
                    MenuConstant.MasterData.LegalType.HREF, masterDataParent, MenuConstant.MasterData.LegalType.ORDER);

            // ── Location (parent + children) ───────────────────────────────────
            Menu locationParent = findOrCreateMenu(MenuConstant.Location.TITLE, MenuConstant.Location.ICON,
                    MenuConstant.Location.HREF, null, MenuConstant.Location.ORDER);
            findOrCreateMenu(MenuConstant.Location.Province.TITLE, MenuConstant.Location.Province.ICON,
                    MenuConstant.Location.Province.HREF, locationParent, MenuConstant.Location.Province.ORDER);
            findOrCreateMenu(MenuConstant.Location.District.TITLE, MenuConstant.Location.District.ICON,
                    MenuConstant.Location.District.HREF, locationParent, MenuConstant.Location.District.ORDER);
            findOrCreateMenu(MenuConstant.Location.Commune.TITLE, MenuConstant.Location.Commune.ICON,
                    MenuConstant.Location.Commune.HREF, locationParent, MenuConstant.Location.Commune.ORDER);
            findOrCreateMenu(MenuConstant.Location.Village.TITLE, MenuConstant.Location.Village.ICON,
                    MenuConstant.Location.Village.HREF, locationParent, MenuConstant.Location.Village.ORDER);

            // ── Report ─────────────────────────────────────────────────────────
            findOrCreateMenu(MenuConstant.Report.TITLE, MenuConstant.Report.ICON,
                    MenuConstant.Report.HREF, null, MenuConstant.Report.ORDER);

            // ── Menu Configuration ─────────────────────────────────────────────
            findOrCreateMenu(MenuConstant.MenuConfig.TITLE, MenuConstant.MenuConfig.ICON,
                    MenuConstant.MenuConfig.HREF, null, MenuConstant.MenuConfig.ORDER);

            log.info("Menu initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize menu data", e);
            throw new RuntimeException("Menu initialization failed", e);
        }
    }

    /**
     * Finds an existing menu and ALWAYS syncs its roles to ALL_ROLES,
     * or creates it fresh with ALL_ROLES if it does not exist.
     *
     * Syncing on every startup means deleted/empty role tables are
     * automatically repaired without needing to drop the menus themselves.
     */
    private Menu findOrCreateMenu(String title, String icon, String href,
                                  Menu parent, int displayOrder) {
        Menu existing = parent == null
                ? menuRepository.findByTitleAndParentIsNull(title)
                : menuRepository.findByTitleAndParent(title, parent);

        if (existing != null) {
            // Always repair roles in case the role table was cleared
            if (!ALL_ROLES.equals(existing.getRoles())) {
                existing.setRoles(new HashSet<>(ALL_ROLES));
                existing.setIsActive(true);
                menuRepository.save(existing);
                log.info("Repaired roles for menu '{}'", title);
            } else {
                log.debug("Menu '{}' is up to date", title);
            }
            return existing;
        }

        Menu newMenu = Menu.builder()
                .title(title)
                .icon(icon)
                .href(href)
                .parent(parent)
                .displayOrder(displayOrder)
                .roles(new HashSet<>(ALL_ROLES))
                .isActive(true)
                .build();

        Menu saved = menuRepository.save(newMenu);
        log.info("Created menu '{}'{}",
                title, parent != null ? " under '" + parent.getTitle() + "'" : "");
        return saved;
    }
}