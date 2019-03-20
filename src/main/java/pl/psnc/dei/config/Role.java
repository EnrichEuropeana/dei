package pl.psnc.dei.config;

public class Role {
    public static final String OPERATOR = "operator";

    private Role() {
        // Static methods and fields only
    }

    public static String[] getAllRoles() {
        return new String[] { OPERATOR };
    }
}
