package plugin;

import pluginable.PluginInterface;

import java.util.Locale;
import java.util.ResourceBundle;

public class Main implements PluginInterface {

    static {
        Locale locale = Locale.ENGLISH;
        ResourceBundle main = ResourceBundle.getBundle("mainLoadedInStatic", locale);
        //only uncomment to check, that it would work if loaded in static
//        ResourceBundle mainNotLoadedInStatic = ResourceBundle.getBundle("mainNotLoadedInStatic", locale);
    }
    @Override
    public void loadResourceBundle(Locale locale) {
        ResourceBundle mainLoadedInStatic = ResourceBundle.getBundle("mainLoadedInStatic", locale);
        ResourceBundle mainNotLoadedInStatic = ResourceBundle.getBundle("mainNotLoadedInStatic", locale);
    }
}
