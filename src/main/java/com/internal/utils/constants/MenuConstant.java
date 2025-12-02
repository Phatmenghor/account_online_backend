package com.internal.utils.constants;

public class MenuConstant {

    public static class Dashboard {
        public static final String TITLE = "Dashboard";
        public static final String ICON = "LayoutDashboard";
        public static final String HREF = "/dashboard";
        public static final int ORDER = 1;
    }

    public static class Users {
        public static final String TITLE = "Users";
        public static final String ICON = "User2";
        public static final String HREF = "/user";
        public static final int ORDER = 2;
    }

    public static class Account {
        public static final String TITLE = "Account";
        public static final String ICON = "IdCard";
        public static final String HREF = null;
        public static final int ORDER = 3;

        public static class Final {
            public static final String TITLE = "Account Final";
            public static final String ICON = null;
            public static final String HREF = "/account-online";
            public static final int ORDER = 1;
        }

        public static class Success {
            public static final String TITLE = "Success Accounts";
            public static final String ICON = null;
            public static final String HREF = "/account-online-success";
            public static final int ORDER = 2;
        }
    }

    public static class Aml {
        public static final String TITLE = "AML";
        public static final String ICON = "FolderClosed";
        public static final String HREF = null;
        public static final int ORDER = 4;

        public static class Management {
            public static final String TITLE = "Management";
            public static final String ICON = null;
            public static final String HREF = "/aml-management";
            public static final int ORDER = 1;
        }

        public static class History {
            public static final String TITLE = "History";
            public static final String ICON = null;
            public static final String HREF = "/aml-history";
            public static final int ORDER = 2;
        }
    }

    public static class MasterData {
        public static final String TITLE = "Master Data";
        public static final String ICON = "Calendar1Icon";
        public static final String HREF = null;
        public static final int ORDER = 5;

        public static class Branch {
            public static final String TITLE = "Branch";
            public static final String ICON = null;
            public static final String HREF = "/branch";
            public static final int ORDER = 1;
        }

        public static class Reference {
            public static final String TITLE = "Reference";
            public static final String ICON = null;
            public static final String HREF = "/reference";
            public static final int ORDER = 2;
        }

        public static class Marital {
            public static final String TITLE = "Marital";
            public static final String ICON = null;
            public static final String HREF = "/marital";
            public static final int ORDER = 3;
        }

        public static class Occupation {
            public static final String TITLE = "Occupation";
            public static final String ICON = null;
            public static final String HREF = "/occupation";
            public static final int ORDER = 4;
        }

        public static class LegalType {
            public static final String TITLE = "Legal Type";
            public static final String ICON = null;
            public static final String HREF = "/legal-type";
            public static final int ORDER = 5;
        }
    }

    public static class Location {
        public static final String TITLE = "Location";
        public static final String ICON = "MapPin";
        public static final String HREF = null;
        public static final int ORDER = 6;

        public static class Province {
            public static final String TITLE = "Province";
            public static final String ICON = null;
            public static final String HREF = "/province";
            public static final int ORDER = 1;
        }

        public static class District {
            public static final String TITLE = "District";
            public static final String ICON = null;
            public static final String HREF = "/district";
            public static final int ORDER = 2;
        }

        public static class Commune {
            public static final String TITLE = "Commune";
            public static final String ICON = null;
            public static final String HREF = "/commune";
            public static final int ORDER = 3;
        }

        public static class Village {
            public static final String TITLE = "Village";
            public static final String ICON = null;
            public static final String HREF = "/village";
            public static final int ORDER = 4;
        }
    }

    public static class Report {
        public static final String TITLE = "Report";
        public static final String ICON = "File";
        public static final String HREF = "/report";
        public static final int ORDER = 7;
    }

    public static class MenuConfig {
        public static final String TITLE = "menu-config";
        public static final String ICON = "Menu Config";
        public static final String HREF = "/menu-config";
        public static final int ORDER = 8;
    }
}
