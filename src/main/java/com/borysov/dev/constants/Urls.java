package com.borysov.dev.constants;

public interface Urls extends Version {
    String ROOT = "";
    String UUID_PATH_VARIABLE = "uuid";
    String UUID = "{" + UUID_PATH_VARIABLE + "}";

    interface User {

        interface Home {
            String PART = "home";
            String FULL = URI_SEPARATOR + PART;
        }
        interface Index {
            String PART = "index";
            String FULL = URI_SEPARATOR + PART;
        }

        interface Login {
            String PART = "login";
            String FULL = URI_SEPARATOR + PART;
        }
    }

    interface Item {
        String PART = "items";
        String FULL = ROOT + URI_SEPARATOR + PART;

        interface ID {
            String PART = UUID;
        }

        interface SaveItem {
            String PART = "saveItem";
        }

        interface DeleteItem {
            String PART = "delete";
            String AlL = "deleteAll";
            String FULL = PART + URI_SEPARATOR + UUID;
        }

        interface UpdateAll {
            String PART = "updateAll";
        }
    }

}
